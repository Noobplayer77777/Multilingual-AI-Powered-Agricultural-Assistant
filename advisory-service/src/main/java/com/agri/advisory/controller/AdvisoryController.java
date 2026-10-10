package com.agri.advisory.controller;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;
import com.agri.advisory.service.AdvisoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdvisoryController {

    private final AdvisoryService advisoryService;

    public AdvisoryController(AdvisoryService advisoryService) {
        this.advisoryService = advisoryService;
    }

    @PostMapping("/advise")
    public ResponseEntity<AdvisoryResponse> generateAdvisory(@Valid @RequestBody AdvisoryRequest request) {
        AdvisoryResponse response = advisoryService.generateAdvisory(request);
        return ResponseEntity.ok(response);
    }
}
