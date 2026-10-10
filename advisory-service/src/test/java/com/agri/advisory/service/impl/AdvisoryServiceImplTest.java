package com.agri.advisory.service.impl;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;
import com.agri.advisory.dto.ContextItem;
import com.agri.advisory.service.LlmProvider;
import com.agri.advisory.service.RetrievalClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AdvisoryServiceImplTest {

    private RetrievalClient retrievalClient;
    private LlmProvider llmProvider;
    private AdvisoryServiceImpl advisoryService;

    @BeforeEach
    public void setup() {
        retrievalClient = Mockito.mock(RetrievalClient.class);
        llmProvider = Mockito.mock(LlmProvider.class);
        advisoryService = new AdvisoryServiceImpl(retrievalClient, llmProvider);
    }

    @Test
    public void testGenerateAdvisory_WithContext() {
        AdvisoryRequest request = new AdvisoryRequest();
        request.setQuestion("Test question");
        request.setLanguage("en");

        ContextItem item = new ContextItem();
        item.setText("Context text");
        item.setSourceName("Test Source");
        item.setType("DOCUMENT");
        item.setSourceUrl("http://test");

        when(retrievalClient.retrieveContext(any())).thenReturn(Collections.singletonList(item));
        when(llmProvider.generateResponse(anyString(), anyString())).thenReturn("Grounded Answer");

        AdvisoryResponse response = advisoryService.generateAdvisory(request);

        assertNotNull(response);
        assertEquals("Grounded Answer", response.getAnswer());
        assertEquals("en", response.getLanguage());
        assertEquals(1, response.getSources().size());
        assertEquals("Test Source", response.getSources().get(0).getName());
    }

    @Test
    public void testGenerateAdvisory_EmptyContext() {
        AdvisoryRequest request = new AdvisoryRequest();
        request.setQuestion("Test question");
        request.setLanguage("ta");

        when(retrievalClient.retrieveContext(any())).thenReturn(Collections.emptyList());
        when(llmProvider.generateResponse(anyString(), anyString())).thenReturn("No context answer");

        AdvisoryResponse response = advisoryService.generateAdvisory(request);

        assertNotNull(response);
        assertEquals("No context answer", response.getAnswer());
        assertEquals("ta", response.getLanguage());
        assertEquals(0, response.getSources().size());
    }
}
