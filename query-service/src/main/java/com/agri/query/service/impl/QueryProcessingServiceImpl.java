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

        // 2. Fetch or create a default user
        User user = userRepository.findByUsername("farmer_1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("farmer_1");
            return userRepository.save(newUser);
        });

        // 3. Call Advisory Service FIRST
        QueryResponse advisoryResponse = advisoryClient.getAdvisory(request);

        // 4. Save Query to DB
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setUser(user);
        queryEntity.setQuestion(request.getQuestion());
        queryEntity.setLanguage(request.getLanguage());
        queryEntity.setLocation(request.getLocation());
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
        List<QueryEntity> queries = queryRepository.findAllByOrderByCreatedAtDesc();
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
}
