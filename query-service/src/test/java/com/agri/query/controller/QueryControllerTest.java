package com.agri.query.controller;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.dto.Source;
import com.agri.query.exception.AdvisoryServiceUnavailableException;
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
    public void testGetChatInterface() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void testGetHistoryView() throws Exception {
        Mockito.when(queryProcessingService.getHistory()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"))
                .andExpect(model().attributeExists("queries"));
    }

    @Test
    public void testSubmitQuery_Valid() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What crop should I grow?");
        request.setLanguage("en");

        QueryResponse response = new QueryResponse();
        response.setAnswer("Grow tomatoes");
        response.setLanguage("en");
        Source src = new Source();
        src.setName("ICAR Guide");
        src.setType("DOCUMENT");
        src.setUrl("https://icar.org.in");
        response.setSources(List.of(src));

        Mockito.when(queryProcessingService.processQuery(any(QueryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Grow tomatoes"))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.sources[0].name").value("ICAR Guide"));
    }

    @Test
    public void testSubmitQuery_InvalidMissingQuestion() throws Exception {
        QueryRequest request = new QueryRequest();
        // question is missing

        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSubmitQuery_AdvisoryServiceDown() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setQuestion("Test question");

        Mockito.when(queryProcessingService.processQuery(any(QueryRequest.class)))
                .thenThrow(new AdvisoryServiceUnavailableException("Service connection refused"));

        mockMvc.perform(post("/api/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testGetHistoryApi() throws Exception {
        QueryResponse item = new QueryResponse();
        item.setId(1);
        item.setQuestion("Past Question");
        item.setAnswer("Past Answer");
        item.setLanguage("ta");
        item.setCreatedAt(LocalDateTime.now());

        Mockito.when(queryProcessingService.getHistory()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].question").value("Past Question"))
                .andExpect(jsonPath("$[0].answer").value("Past Answer"))
                .andExpect(jsonPath("$[0].language").value("ta"));
    }
}
