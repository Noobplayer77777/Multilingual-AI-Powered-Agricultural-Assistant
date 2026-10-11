package com.agri.query.service.impl;

import com.agri.query.client.AdvisoryClient;
import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.dto.Source;
import com.agri.query.entity.QueryEntity;
import com.agri.query.entity.ResponseEntityData;
import com.agri.query.entity.SourceEntity;
import com.agri.query.entity.User;
import com.agri.query.repository.QueryRepository;
import com.agri.query.repository.ResponseRepository;
import com.agri.query.repository.SourceRepository;
import com.agri.query.repository.UserRepository;
import com.agri.query.service.LanguageDetectionService;
import com.agri.query.service.QueryProcessingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QueryProcessingServiceImpl implements QueryProcessingService {

    private final AdvisoryClient advisoryClient;
    private final LanguageDetectionService languageDetectionService;
    private final UserRepository userRepository;
    private final QueryRepository queryRepository;
    private final ResponseRepository responseRepository;
    private final SourceRepository sourceRepository;

    public QueryProcessingServiceImpl(AdvisoryClient advisoryClient,
                                      LanguageDetectionService languageDetectionService,
                                      UserRepository userRepository,
                                      QueryRepository queryRepository,
                                      ResponseRepository responseRepository,
                                      SourceRepository sourceRepository) {
        this.advisoryClient = advisoryClient;
        this.languageDetectionService = languageDetectionService;
        this.userRepository = userRepository;
        this.queryRepository = queryRepository;
        this.responseRepository = responseRepository;
        this.sourceRepository = sourceRepository;
    }

    @Override
    @Transactional
    public QueryResponse processQuery(QueryRequest request) {
        // 1. Language Detection if not provided
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty() || "auto".equalsIgnoreCase(request.getLanguage())) {
            request.setLanguage(languageDetectionService.detectLanguage(request.getQuestion()));
        }

        // 2. Resolve current user (authenticated farmer or fallback guest)
        User user = resolveCurrentUser();

        // 3. Call Advisory Service FIRST
        QueryResponse advisoryResponse = advisoryClient.getAdvisory(request);

        // 4. Save Query to DB (resolve location from weather source if omitted)
        String effectiveLocation = request.getLocation();
        if ((effectiveLocation == null || effectiveLocation.trim().isEmpty()) && advisoryResponse.getSources() != null) {
            for (Source s : advisoryResponse.getSources()) {
                if ("API".equalsIgnoreCase(s.getType()) && s.getName() != null && s.getName().contains("—")) {
                    String[] parts = s.getName().split("—");
                    if (parts.length > 1) {
                        effectiveLocation = parts[1].split(":")[0].trim();
                        break;
                    }
                }
            }
        }

        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setUser(user);
        queryEntity.setQuestion(request.getQuestion());
        queryEntity.setLanguage(request.getLanguage());
        queryEntity.setLocation(effectiveLocation);
        queryEntity.setSeason(request.getSeason());
        queryEntity = queryRepository.save(queryEntity);

        // 5. Save Response and Sources to DB
        ResponseEntityData responseEntity = new ResponseEntityData();
        responseEntity.setQuery(queryEntity);
        responseEntity.setAnswer(advisoryResponse.getAnswer());
        responseEntity = responseRepository.save(responseEntity);

        if (advisoryResponse.getSources() != null) {
            for (Source s : advisoryResponse.getSources()) {
                SourceEntity sourceEntity = new SourceEntity();
                sourceEntity.setResponse(responseEntity);
                sourceEntity.setName(s.getName());
                sourceEntity.setType(s.getType());
                sourceEntity.setUrl(s.getUrl());
                sourceRepository.save(sourceEntity);
            }
        }

        advisoryResponse.setId(queryEntity.getId());
        advisoryResponse.setQuestion(queryEntity.getQuestion());
        advisoryResponse.setLocation(queryEntity.getLocation());
        advisoryResponse.setSeason(queryEntity.getSeason());
        advisoryResponse.setCreatedAt(queryEntity.getCreatedAt());

        return advisoryResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueryResponse> getHistory() {
        List<QueryEntity> queries;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
            if (userOpt.isPresent()) {
                queries = queryRepository.findByUserIdOrderByCreatedAtDesc(userOpt.get().getId());
            } else {
                queries = new ArrayList<>();
            }
        } else {
            queries = queryRepository.findAllByOrderByCreatedAtDesc();
        }

        List<QueryResponse> history = new ArrayList<>();
        for (QueryEntity q : queries) {
            QueryResponse item = new QueryResponse();
            item.setId(q.getId());
            item.setQuestion(q.getQuestion());
            item.setLanguage(q.getLanguage());
            item.setLocation(q.getLocation());
            item.setSeason(q.getSeason());
            item.setCreatedAt(q.getCreatedAt());

            Optional<ResponseEntityData> respOpt = responseRepository.findByQueryId(q.getId());
            if (respOpt.isPresent()) {
                ResponseEntityData resp = respOpt.get();
                item.setAnswer(resp.getAnswer());
                List<SourceEntity> sourceEntities = sourceRepository.findByResponseId(resp.getId());
                List<Source> sources = new ArrayList<>();
                for (SourceEntity se : sourceEntities) {
                    Source s = new Source();
                    s.setName(se.getName());
                    s.setType(se.getType());
                    s.setUrl(se.getUrl());
                    sources.add(s);
                }
                item.setSources(sources);
            }
            history.add(item);
        }
        return history;
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        // Fallback default farmer account for public/unauthenticated queries
        return userRepository.findByEmailIgnoreCase("farmer_1@kisan.local").orElseGet(() -> {
            User guest = new User("farmer_1@kisan.local", "$2a$12$N.zMDJ4W7ODYdpm8K7LqeuuY9Nn9P7zYI.c0e8Oa8F5m7Q1wZl6Ie", "Kisan Farmer");
            guest.setRole("ROLE_FARMER");
            return userRepository.save(guest);
        });
    }
}
