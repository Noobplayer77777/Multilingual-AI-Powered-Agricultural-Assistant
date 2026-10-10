package com.agri.query.controller;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.dto.Source;
import com.agri.query.entity.QueryEntity;
import com.agri.query.service.QueryProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueryController.class)
public class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryProcessingService queryProcessingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testChatInterface_RendersIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void testHistoryView_RendersHistory() throws Exception {
        QueryEntity entity = new QueryEntity();
        entity.setId(1);
        entity.setQuestion("What crop to grow?");
        entity.setLanguage("en");
        entity.setLocation("Thanjavur");
        entity.setCreatedAt(LocalDateTime.now());

        Mockito.when(queryProcessingService.getHistory()).thenReturn(List.of(entity));

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"))
                .andExpect(model().attributeExists("queries"));
    }

    @Test
    public void testSubmitQuery_ValidRequest() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What fertilizer for wheat?");
        request.setLanguage("en");
        request.setLocation("Punjab");

        QueryResponse response = new QueryResponse();
        response.setAnswer("Use NPK fertilizer in balanced amounts.");
        response.setLanguage("en");
        Source source = new Source();
        source.setName("ICAR Wheat Guidelines");
        source.setType("DOCUMENT");
        source.setUrl("https://icar.gov.in/wheat");
        response.setSources(List.of(source));

        Mockito.when(queryProcessingService.processQuery(any(QueryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Use NPK fertilizer in balanced amounts."))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.sources[0].name").value("ICAR Wheat Guidelines"));
    }

    @Test
    public void testSubmitQuery_InvalidRequest_BlankQuestion() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setQuestion(""); // Blank question violates @NotBlank

        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetHistoryApi_ReturnsJsonList() throws Exception {
        QueryEntity entity = new QueryEntity();
        entity.setId(1);
        entity.setQuestion("Pest control for rice?");
        entity.setLanguage("ta");
        entity.setLocation("Madurai");
        entity.setCreatedAt(LocalDateTime.now());

        Mockito.when(queryProcessingService.getHistory()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].question").value("Pest control for rice?"))
                .andExpect(jsonPath("$[0].language").value("ta"))
                .andExpect(jsonPath("$[0].location").value("Madurai"));
    }
}
