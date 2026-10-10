package com.agri.advisory.service.impl;

import com.agri.advisory.service.LlmProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "stub", matchIfMissing = true)
public class StubLlmProvider implements LlmProvider {

    @Override
    public String generateResponse(String prompt, String language) {
        return "This is a deterministic placeholder response from the STUB AI provider. " +
               "It is not genuine agricultural advice. Language requested: " + language;
    }
}
