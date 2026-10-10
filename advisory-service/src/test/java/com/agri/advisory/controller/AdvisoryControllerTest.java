package com.agri.advisory.controller;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;
import com.agri.advisory.dto.Source;
import com.agri.advisory.service.AdvisoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdvisoryController.class)
public class AdvisoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdvisoryService advisoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGenerateAdvisory_ValidRequest() throws Exception {
        AdvisoryRequest request = new AdvisoryRequest();
        request.setQuestion("What crop?");
        request.setLanguage("en");
        request.setCategory("CROP");

        AdvisoryResponse response = new AdvisoryResponse();
        response.setAnswer("Test Answer");
        response.setLanguage("en");
        Source src = new Source();
        src.setName("Test Source");
        src.setType("DOCUMENT");
        src.setUrl("http://test");
        response.setSources(Collections.singletonList(src));

        Mockito.when(advisoryService.generateAdvisory(any(AdvisoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/advise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Test Answer"))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.sources[0].name").value("Test Source"));
    }

    @Test
    public void testGenerateAdvisory_InvalidRequest() throws Exception {
        AdvisoryRequest request = new AdvisoryRequest();
        // Missing question should trigger validation error
        
        mockMvc.perform(post("/advise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
