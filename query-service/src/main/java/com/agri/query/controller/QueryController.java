package com.agri.query.controller;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.entity.QueryEntity;
import com.agri.query.service.QueryProcessingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class QueryController {

    private final QueryProcessingService queryProcessingService;

    public QueryController(QueryProcessingService queryProcessingService) {
        this.queryProcessingService = queryProcessingService;
    }

    // UI Routes
    @GetMapping("/")
    public String chatInterface() {
        return "index";
    }

    @GetMapping("/history")
    public String historyView(Model model) {
        model.addAttribute("queries", queryProcessingService.getHistory());
        return "history";
    }

    // API Routes
    @PostMapping("/api/query")
    @ResponseBody
    public ResponseEntity<?> submitQuery(@Valid @RequestBody QueryRequest request) {
        try {
            QueryResponse response = queryProcessingService.processQuery(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<List<QueryEntity>> getHistoryApi() {
        return ResponseEntity.ok(queryProcessingService.getHistory());
    }
}
