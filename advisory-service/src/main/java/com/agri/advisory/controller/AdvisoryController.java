package com.agri.advisory.controller;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;
import com.agri.advisory.dto.Source;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
public class AdvisoryController {

    @PostMapping("/advise")
    public ResponseEntity<AdvisoryResponse> generateAdvisory(@Valid @RequestBody AdvisoryRequest request) {
        // TODO: Integrate LLM and Retrieval logic
        
        AdvisoryResponse response = new AdvisoryResponse();
        response.setAnswer("This is a placeholder advisory response.");
        response.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        
        Source source = new Source();
        source.setName("Example Agricultural Guide");
        source.setType("DOCUMENT");
        source.setUrl("https://example.org/agricultural-guide");
        response.setSources(Collections.singletonList(source));
        
        return ResponseEntity.ok(response);
    }
}
