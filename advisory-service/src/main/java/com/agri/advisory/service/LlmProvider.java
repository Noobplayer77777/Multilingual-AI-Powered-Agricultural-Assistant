package com.agri.advisory.service;

public interface LlmProvider {
    String generateResponse(String prompt, String language);
}
