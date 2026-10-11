package com.agri.advisory.controller;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;
import com.agri.advisory.service.AdvisoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdvisoryController {

    private final AdvisoryService advisoryService;
    private final String expectedKey;
    private final boolean internalKeyRequired;

    public AdvisoryController(AdvisoryService advisoryService,
                              @Value("${advisory.security.internal-key:kisan-agri-secure-internal-secret-key-2026}") String expectedKey,
                              @Value("${advisory.security.internal-key-required:true}") boolean internalKeyRequired) {
        this.advisoryService = advisoryService;
        this.expectedKey = expectedKey;
        this.internalKeyRequired = internalKeyRequired;
    }

    @PostMapping("/advise")
    public ResponseEntity<?> generateAdvisory(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @Valid @RequestBody AdvisoryRequest request) {

        if (internalKeyRequired && (serviceKey == null || !expectedKey.equals(serviceKey))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing X-Internal-Service-Key");
        }

        AdvisoryResponse response = advisoryService.generateAdvisory(request);
        return ResponseEntity.ok(response);
    }
}
