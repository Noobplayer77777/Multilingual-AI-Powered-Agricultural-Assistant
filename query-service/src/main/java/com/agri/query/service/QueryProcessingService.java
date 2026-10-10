package com.agri.query.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QueryProcessingService {

    private final AdvisoryClient advisoryClient;
    private final LanguageDetectionService languageDetectionService;
    private final UserRepository userRepository;
    private final QueryRepository queryRepository;
    private final ResponseRepository responseRepository;
    private final SourceRepository sourceRepository;

    public QueryProcessingService(AdvisoryClient advisoryClient,
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

    @Transactional
    public QueryResponse processQuery(QueryRequest request) {
        // 1. Language Detection if not provided
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty() || "auto".equalsIgnoreCase(request.getLanguage())) {
            request.setLanguage(languageDetectionService.detectLanguage(request.getQuestion()));
        }

        // 2. Fetch or create a default user for phase 1
        User user = userRepository.findByUsername("farmer_1").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("farmer_1");
            return userRepository.save(newUser);
        });

        // 3. Save Query to DB
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setUser(user);
        queryEntity.setQuestion(request.getQuestion());
        queryEntity.setLanguage(request.getLanguage());
        queryEntity.setLocation(request.getLocation());
        queryEntity.setSeason(request.getSeason());
        queryRepository.save(queryEntity);

        // 4. Call Advisory Service
        QueryResponse advisoryResponse = advisoryClient.getAdvisory(request);

        // 5. Save Response and Sources to DB
        ResponseEntityData responseEntity = new ResponseEntityData();
        responseEntity.setQuery(queryEntity);
        responseEntity.setAnswer(advisoryResponse.getAnswer());
        responseRepository.save(responseEntity);

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

        return advisoryResponse;
    }

    @Transactional(readOnly = true)
    public List<QueryEntity> getHistory() {
        return queryRepository.findAllByOrderByCreatedAtDesc();
    }
}
