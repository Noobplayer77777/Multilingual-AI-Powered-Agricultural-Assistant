package com.agri.query.controller;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.dto.Source;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class QueryController {

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> submitQuery(@Valid @RequestBody QueryRequest request) {
        // TODO: Call Advisory Service over HTTP and persist to Database (Phase 1 Integration points)
        
        // Mock response
        QueryResponse response = new QueryResponse();
        response.setAnswer("This is a placeholder advisory response.");
        response.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        
        Source source = new Source();
        source.setName("Example Agricultural Guide");
        source.setType("DOCUMENT");
        source.setUrl("https://example.org/agricultural-guide");
        response.setSources(Collections.singletonList(source));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QueryResponse>> getHistory() {
        // TODO: Fetch from Database
        return ResponseEntity.ok(Collections.emptyList());
    }
}
