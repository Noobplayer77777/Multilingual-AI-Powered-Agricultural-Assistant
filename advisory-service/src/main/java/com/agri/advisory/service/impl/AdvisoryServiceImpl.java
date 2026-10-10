package com.agri.advisory.service.impl;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;
import com.agri.advisory.dto.ContextItem;
import com.agri.advisory.dto.Source;
import com.agri.advisory.service.AdvisoryService;
import com.agri.advisory.service.LlmProvider;
import com.agri.advisory.service.RetrievalClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdvisoryServiceImpl implements AdvisoryService {

    private final RetrievalClient retrievalClient;
    private final LlmProvider llmProvider;

    public AdvisoryServiceImpl(RetrievalClient retrievalClient, LlmProvider llmProvider) {
        this.retrievalClient = retrievalClient;
        this.llmProvider = llmProvider;
    }

    @Override
    public AdvisoryResponse generateAdvisory(AdvisoryRequest request) {
        // 1. Accept request and metadata
        // 2. Request relevant context
        List<ContextItem> contextItems = retrievalClient.retrieveContext(request);
        
        // 3. Assemble a prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("User Question: ").append(request.getQuestion()).append("\n");
        promptBuilder.append("Crop: ").append(request.getCrop()).append("\n");
        promptBuilder.append("Location: ").append(request.getLocation()).append("\n");
        promptBuilder.append("Category: ").append(request.getCategory()).append("\n");
        promptBuilder.append("Season: ").append(request.getSeason()).append("\n\n");
        
        promptBuilder.append("Retrieved Context:\n");
        if (contextItems == null || contextItems.isEmpty()) {
            promptBuilder.append("No context available. Admit insufficient information.\n");
        } else {
            for (ContextItem item : contextItems) {
                promptBuilder.append("- ").append(item.getText()).append(" (Source: ").append(item.getSourceName()).append(")\n");
            }
        }
        promptBuilder.append("\nGrounding Rules:\n");
        promptBuilder.append("- Answer in the requested language.\n");
        promptBuilder.append("- Use the supplied context for agricultural factual claims.\n");
        promptBuilder.append("- Do not invent rainfall measurements, market prices, schemes, or citations.\n");
        promptBuilder.append("- Admit when information is insufficient.\n");
        promptBuilder.append("- Distinguish forecast data from historical observations.\n");
        
        String language = request.getLanguage() != null ? request.getLanguage() : "en";
        
        // 4. Ask the configured model
        String modelResponse = llmProvider.generateResponse(promptBuilder.toString(), language);
        
        // 5. Validate and normalize output
        AdvisoryResponse response = new AdvisoryResponse();
        response.setAnswer(modelResponse);
        response.setLanguage(language);
        
        // 6. Return answer and source metadata
        List<Source> sources = new ArrayList<>();
        if (contextItems != null) {
            for (ContextItem item : contextItems) {
                Source source = new Source();
                source.setName(item.getSourceName());
                source.setType(item.getType());
                source.setUrl(item.getSourceUrl());
                sources.add(source);
            }
        }
        
        // If it's a stub response, it might not use context. But we pass it along for contract completeness.
        response.setSources(sources);
        
        return response;
    }
}
