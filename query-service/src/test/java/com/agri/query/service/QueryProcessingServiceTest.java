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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class QueryProcessingServiceTest {

    private AdvisoryClient advisoryClient;
    private LanguageDetectionService languageDetectionService;
    private UserRepository userRepository;
    private QueryRepository queryRepository;
    private ResponseRepository responseRepository;
    private SourceRepository sourceRepository;

    private QueryProcessingService queryProcessingService;

    @BeforeEach
    public void setup() {
        advisoryClient = Mockito.mock(AdvisoryClient.class);
        languageDetectionService = new LanguageDetectionService();
        userRepository = Mockito.mock(UserRepository.class);
        queryRepository = Mockito.mock(QueryRepository.class);
        responseRepository = Mockito.mock(ResponseRepository.class);
        sourceRepository = Mockito.mock(SourceRepository.class);

        queryProcessingService = new QueryProcessingService(
                advisoryClient,
                languageDetectionService,
                userRepository,
                queryRepository,
                responseRepository,
                sourceRepository
        );

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("farmer_1");
        when(userRepository.findByUsername("farmer_1")).thenReturn(Optional.of(mockUser));
        when(queryRepository.save(any(QueryEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(responseRepository.save(any(ResponseEntityData.class))).thenAnswer(i -> i.getArgument(0));
        when(sourceRepository.save(any(SourceEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    public void testProcessQuery_Success() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What crop should I grow?");
        request.setLanguage("en");
        request.setLocation("Thanjavur");

        QueryResponse advisoryResponse = new QueryResponse();
        advisoryResponse.setAnswer("Paddy is suitable for Thanjavur.");
        advisoryResponse.setLanguage("en");
        Source source = new Source();
        source.setName("TNAU Guidelines");
        source.setType("DOCUMENT");
        source.setUrl("https://tnau.ac.in");
        advisoryResponse.setSources(Collections.singletonList(source));

        when(advisoryClient.getAdvisory(request)).thenReturn(advisoryResponse);

        QueryResponse result = queryProcessingService.processQuery(request);

        assertNotNull(result);
        assertEquals("Paddy is suitable for Thanjavur.", result.getAnswer());
        assertEquals(1, result.getSources().size());

        verify(queryRepository, times(1)).save(any(QueryEntity.class));
        verify(responseRepository, times(1)).save(any(ResponseEntityData.class));
        verify(sourceRepository, times(1)).save(any(SourceEntity.class));
    }

    @Test
    public void testProcessQuery_AutoDetectLanguage() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("நான் என்ன பயிர் வளர்க்க வேண்டும்?");
        request.setLanguage("auto");

        QueryResponse advisoryResponse = new QueryResponse();
        advisoryResponse.setAnswer("பயிர் பரிந்துரை");
        advisoryResponse.setLanguage("ta");
        advisoryResponse.setSources(Collections.emptyList());

        when(advisoryClient.getAdvisory(request)).thenReturn(advisoryResponse);

        QueryResponse result = queryProcessingService.processQuery(request);

        assertEquals("ta", request.getLanguage());
        assertNotNull(result);
    }

    @Test
    public void testProcessQuery_HandlesAdvisoryServiceFallback() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is the market price of cotton?");
        request.setLanguage("en");

        QueryResponse fallback = new QueryResponse();
        fallback.setAnswer("The advisory service is currently unavailable. Please try again later.");
        fallback.setLanguage("en");
        Source errorSource = new Source();
        errorSource.setName("System Fallback");
        errorSource.setType("INTERNAL");
        errorSource.setUrl("");
        fallback.setSources(Collections.singletonList(errorSource));

        when(advisoryClient.getAdvisory(request)).thenReturn(fallback);

        QueryResponse result = queryProcessingService.processQuery(request);

        assertNotNull(result);
        assertEquals("The advisory service is currently unavailable. Please try again later.", result.getAnswer());
        assertEquals("System Fallback", result.getSources().get(0).getName());

        verify(queryRepository, times(1)).save(any(QueryEntity.class));
        verify(responseRepository, times(1)).save(any(ResponseEntityData.class));
        verify(sourceRepository, times(1)).save(any(SourceEntity.class));
    }
}
