package com.agri.query.controller;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.entity.FarmPlot;
import com.agri.query.entity.FarmerProfile;
import com.agri.query.entity.User;
import com.agri.query.exception.AdvisoryServiceUnavailableException;
import com.agri.query.service.AuthService;
import com.agri.query.service.FarmMemoryService;
import com.agri.query.service.QueryProcessingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class QueryController {

    private final QueryProcessingService queryProcessingService;
    private final AuthService authService;
    private final FarmMemoryService farmMemoryService;

    public QueryController(QueryProcessingService queryProcessingService,
                           AuthService authService,
                           FarmMemoryService farmMemoryService) {
        this.queryProcessingService = queryProcessingService;
        this.authService = authService;
        this.farmMemoryService = farmMemoryService;
    }

    // UI Routes
    @GetMapping("/")
    public String chatInterface(Model model) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            try {
                FarmerProfile profile = farmMemoryService.getProfile(user);
                model.addAttribute("profile", profile);
                List<FarmPlot> plots = farmMemoryService.getPlots(user);
                model.addAttribute("plots", plots);
            } catch (Exception ignored) {
                model.addAttribute("plots", Collections.emptyList());
            }
        } else {
            model.addAttribute("plots", Collections.emptyList());
        }
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
        } catch (AdvisoryServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Advisory service is unavailable: " + e.getMessage(), "status", 503));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred: " + e.getMessage(), "status", 500));
        }
    }

    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<List<QueryResponse>> getHistoryApi() {
        return ResponseEntity.ok(queryProcessingService.getHistory());
    }
}
