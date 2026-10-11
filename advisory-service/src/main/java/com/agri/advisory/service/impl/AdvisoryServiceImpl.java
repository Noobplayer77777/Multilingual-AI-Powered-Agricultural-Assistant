package com.agri.advisory.service.impl;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;
import com.agri.advisory.dto.ContextItem;
import com.agri.advisory.dto.Source;
import com.agri.advisory.engine.*;
import com.agri.advisory.service.AdvisoryService;
import com.agri.advisory.service.LlmProvider;
import com.agri.advisory.service.RetrievalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdvisoryServiceImpl implements AdvisoryService {

    private static final Logger logger = LoggerFactory.getLogger(AdvisoryServiceImpl.class);

    private final RetrievalClient retrievalClient;
    private final LlmProvider llmProvider;
    private final PesticideSafetyEngine pesticideSafetyEngine;
    private final BudgetPlanningEngine budgetPlanningEngine;
    private final LowCostPlannerEngine lowCostPlannerEngine;
    private final SchemeNavigatorEngine schemeNavigatorEngine;
    private final WeatherRiskEngine weatherRiskEngine;
    private final CropProblemTriageEngine cropProblemTriageEngine;
    private final DecisionSupportEngine decisionSupportEngine;
    private final AgriMultilingualFormatter multilingualFormatter;

    public AdvisoryServiceImpl(RetrievalClient retrievalClient,
                               LlmProvider llmProvider,
                               PesticideSafetyEngine pesticideSafetyEngine,
                               BudgetPlanningEngine budgetPlanningEngine,
                               LowCostPlannerEngine lowCostPlannerEngine,
                               SchemeNavigatorEngine schemeNavigatorEngine,
                               WeatherRiskEngine weatherRiskEngine,
                               CropProblemTriageEngine cropProblemTriageEngine,
                               DecisionSupportEngine decisionSupportEngine) {
        this(retrievalClient, llmProvider, pesticideSafetyEngine, budgetPlanningEngine,
             lowCostPlannerEngine, schemeNavigatorEngine, weatherRiskEngine,
             cropProblemTriageEngine, decisionSupportEngine, new AgriMultilingualFormatter());
    }

    @Autowired
    public AdvisoryServiceImpl(RetrievalClient retrievalClient,
                               LlmProvider llmProvider,
                               PesticideSafetyEngine pesticideSafetyEngine,
                               BudgetPlanningEngine budgetPlanningEngine,
                               LowCostPlannerEngine lowCostPlannerEngine,
                               SchemeNavigatorEngine schemeNavigatorEngine,
                               WeatherRiskEngine weatherRiskEngine,
                               CropProblemTriageEngine cropProblemTriageEngine,
                               DecisionSupportEngine decisionSupportEngine,
                               AgriMultilingualFormatter multilingualFormatter) {
        this.retrievalClient = retrievalClient;
        this.llmProvider = llmProvider;
        this.pesticideSafetyEngine = pesticideSafetyEngine;
        this.budgetPlanningEngine = budgetPlanningEngine;
        this.lowCostPlannerEngine = lowCostPlannerEngine;
        this.schemeNavigatorEngine = schemeNavigatorEngine;
        this.weatherRiskEngine = weatherRiskEngine;
        this.cropProblemTriageEngine = cropProblemTriageEngine;
        this.decisionSupportEngine = decisionSupportEngine;
        this.multilingualFormatter = multilingualFormatter != null ? multilingualFormatter : new AgriMultilingualFormatter();
    }

    @Override
    public AdvisoryResponse generateAdvisory(AdvisoryRequest request) {
        // 1. Retrieve RAG and weather context
        List<ContextItem> contextItems = retrievalClient.retrieveContext(request);
        String question = request.getQuestion() != null ? request.getQuestion().trim() : "";
        String qLower = question.toLowerCase();
        String rawCrop = request.getCrop() != null ? request.getCrop().trim() : "";
        String crop = multilingualFormatter.normalizeCrop(rawCrop, question);
        String category = request.getCategory() != null ? request.getCategory().trim().toUpperCase() : "";
        String language = multilingualFormatter.detectLanguage(question, request.getLanguage());

        Double acres = extractAcres(question);
        Double budget = extractBudget(question);

        // 2. FEATURE 6: Pesticide Safety & Off-Label Check (Zero Hallucination Priority)
        boolean isPesticideQuery = "PESTICIDE_SAFETY".equals(category) ||
                qLower.contains("pesticide") || qLower.contains("spray") || qLower.contains("fungicide") ||
                qLower.contains("insecticide") || qLower.contains("dose") || qLower.contains("mancozeb") ||
                qLower.contains("coragen") || qLower.contains("chlorantraniliprole") || qLower.contains("cartap") ||
                qLower.contains("emamectin") || qLower.contains("copper oxychloride") || qLower.contains("blitox") ||
                qLower.contains("neem oil") || qLower.contains("phi") || qLower.contains("rei") ||
                question.contains("பூச்சிக்கொல்லி") || question.contains("பூச்சி மருந்து") || question.contains("பூஞ்சாணக்கொல்லி") ||
                question.contains("மருந்து") || question.contains("தெளிப்பு") || question.contains("தெளிக்க") ||
                question.contains("மான்கோசெப்") || question.contains("வேப்ப எண்ணெய்") || question.contains("மருந்தளவு") ||
                qLower.contains("marunthu") || qLower.contains("poochi marunthu") ||
                question.contains("कीटनाशक") || question.contains("कीट नाशक") || question.contains("फफूंदनाशक") ||
                question.contains("दवा") || question.contains("दवाई") || question.contains("छिड़काव") ||
                question.contains("स्प्रे") || question.contains("खुराक") || question.contains("मैनकोजेब") ||
                qLower.contains("keetnashak") || qLower.contains("dawai");

        if (isPesticideQuery) {
            PesticideSafetyEngine.SafetyEvaluation eval = pesticideSafetyEngine.evaluate(crop, question, acres);
            if (eval.matched()) {
                if (!eval.approved()) {
                    // Critical safety rejection: Off-label chemical usage attempt
                    logger.warn("Pesticide safety off-label violation rejected: chemical={}, crop={}", eval.pesticideName(), crop);
                    AdvisoryResponse rejectionResponse = new AdvisoryResponse();
                    rejectionResponse.setAnswer(multilingualFormatter.formatPesticideAdvisory(eval, language));
                    rejectionResponse.setLanguage(language);
                    rejectionResponse.setSources(buildSources(contextItems, "CIBRC / TNAU Registered Chemical Database"));
                    return rejectionResponse;
                } else {
                    AdvisoryResponse approvedResponse = new AdvisoryResponse();
                    approvedResponse.setAnswer(multilingualFormatter.formatPesticideAdvisory(eval, language));
                    approvedResponse.setLanguage(language);
                    approvedResponse.setSources(buildSources(contextItems, "CIBRC Registered Label Database"));
                    return approvedResponse;
                }
            }
        }

        // 3. FEATURE 4: Low-Cost Farming Planner (Checked before budget to prevent keyword shadowing)
        boolean isLowCostQuery = "LOW_COST_FARMING".equals(category) || "LOW_COST".equals(category) ||
                qLower.contains("low cost") || qLower.contains("low-cost") ||
                qLower.contains("reduce cost") || qLower.contains("cut expenses") ||
                qLower.contains("reduce expenses") || qLower.contains("save money") ||
                qLower.contains("cheaper") || qLower.contains("soil test savings") ||
                qLower.contains("awd") || qLower.contains("stcr") ||
                question.contains("குறைந்த செலவு") || question.contains("செலவு குறைக்க") || question.contains("செலவை குறைக்க") ||
                question.contains("சிக்கனம்") || question.contains("குறைந்த செலவில்") ||
                qLower.contains("selavu kuraikka") ||
                question.contains("कम लागत") || question.contains("लागत घटाने") || question.contains("खर्च कम") ||
                question.contains("सस्ती खेती") || question.contains("बचत") || question.contains("कम खर्च") ||
                qLower.contains("kam lagat") || qLower.contains("kharch kam");

        if (isLowCostQuery) {
            LowCostPlannerEngine.LowCostPlanResult lowCostResult = lowCostPlannerEngine.planLowCost(crop, acres);
            AdvisoryResponse resp = new AdvisoryResponse();
            resp.setAnswer(multilingualFormatter.formatLowCostAdvisory(lowCostResult, language));
            resp.setLanguage(language);
            resp.setSources(buildSources(contextItems, "TNAU & ICAR Sustainable Agronomic Management Norms"));
            return resp;
        }

        // 4. FEATURE 3: Budget-Aware Crop Planning
        boolean isBudgetQuery = "BUDGET_PLANNING".equals(category) || "BUDGET".equals(category) ||
                qLower.contains("budget") || qLower.contains("cost of cultivation") ||
                qLower.contains("cost per acre") || qLower.contains("expenses") || qLower.contains("expenditure") ||
                question.contains("வரவு செலவு") || question.contains("பட்ஜெட்") || question.contains("சாகுபடி செலவு") ||
                question.contains("ஏக்கர் செலவு") || question.contains("மொத்த செலவு") ||
                question.contains("बजट") || question.contains("खेती की लागत") || question.contains("प्रति एकड़ खर्च") ||
                question.contains("कुल लागत") || question.contains("उत्पादन लागत") ||
                qLower.contains("kheti ki lagat");

        if (isBudgetQuery) {
            BudgetPlanningEngine.BudgetPlanResult planResult = budgetPlanningEngine.calculateBudget(crop, acres, budget);
            AdvisoryResponse resp = new AdvisoryResponse();
            resp.setAnswer(multilingualFormatter.formatBudgetAdvisory(planResult, language));
            resp.setLanguage(language);
            resp.setSources(buildSources(contextItems, "TNAU / ICAR Comprehensive Cost of Cultivation Studies"));
            return resp;
        }

        // 5. FEATURE 5: Government Agricultural Scheme Navigator
        boolean isSchemeQuery = "GOVERNMENT_SCHEME".equals(category) ||
                qLower.contains("scheme") || qLower.contains("subsidy") ||
                qLower.contains("pmkisan") || qLower.contains("pm-kisan") ||
                qLower.contains("pmfby") || qLower.contains("pmksy") ||
                qLower.contains("kcc") || qLower.contains("insurance") ||
                question.contains("திட்டம்") || question.contains("மானிய") || question.contains("மானியம்") ||
                question.contains("விவசாய கடன்") || question.contains("பயிர் காப்பீடு") || question.contains("காப்பீடு") ||
                question.contains("பிஎம் கிசான்") || question.contains("கிசான் கிரெடிட்") ||
                question.contains("योजना") || question.contains("सब्सिडी") || question.contains("अनुदान") ||
                question.contains("फसल बीमा") || question.contains("केसीसी") || question.contains("बीमा") ||
                question.contains("पीएम किसान") || question.contains("किसान क्रेडिट");

        if (isSchemeQuery) {
            SchemeNavigatorEngine.SchemeAssessmentReport report =
                    schemeNavigatorEngine.assessSchemes(acres, request.getLocation(), request.getLocation(), crop);

            AdvisoryResponse resp = new AdvisoryResponse();
            resp.setAnswer(multilingualFormatter.formatSchemeAdvisory(report, language));
            resp.setLanguage(language);
            resp.setSources(buildSources(contextItems, "myScheme / Ministry of Agriculture & Farmers Welfare, GoI"));
            return resp;
        }

        // 6. FEATURE 2: Weather-Risk-Aware Sowing & Crop Advisory
        boolean isWeatherQuery = "WEATHER_RISK".equals(category) ||
                qLower.contains("sowing window") || qLower.contains("rain") ||
                qLower.contains("weather") || qLower.contains("forecast") ||
                qLower.contains("temperature") || qLower.contains("spray weather") ||
                question.contains("மழை") || question.contains("வானிலை") || question.contains("விதைப்பு") ||
                question.contains("வெப்பநிலை") || question.contains("முன்னறிவிப்பு") ||
                qLower.contains("mazhai") || qLower.contains("vanilai") ||
                question.contains("बारिश") || question.contains("मौसम") || question.contains("बुवाई") ||
                question.contains("तापमान") || question.contains("पूर्वानुमान") || question.contains("वर्षा") ||
                qLower.contains("barish") || qLower.contains("mausam");

        if (isWeatherQuery) {
            Double rain = extractWeatherMetric(contextItems, question, "Precipitation", "Rain", "mm");
            Double tMax = extractWeatherMetric(contextItems, question, "Max Temp", "Temperature", "°C");
            Double rh = extractWeatherMetric(contextItems, question, "Humidity", "%");
            Double wind = extractWeatherMetric(contextItems, question, "Wind", "kph");

            WeatherRiskEngine.WeatherRiskAssessment wr =
                    weatherRiskEngine.evaluateWeatherRisk(request.getLocation(), crop, request.getSeason(), rain, tMax, 24.0, rh, wind);

            AdvisoryResponse resp = new AdvisoryResponse();
            resp.setAnswer(multilingualFormatter.formatWeatherAdvisory(wr, language));
            resp.setLanguage(language);
            resp.setSources(buildSources(contextItems, "IMD Agromet Grid / WeatherAPI.com"));
            return resp;
        }

        // 7. FEATURE 6 (Problem Triage): Symptom Diagnosis
        boolean isExplicitRAGOrGuideQuery = qLower.contains("guide") || qLower.contains("manual") ||
                qLower.contains("document") || qLower.contains("file") || qLower.contains("rag") ||
                qLower.contains("how to") || qLower.contains("cultivat") || qLower.contains("package of practices") ||
                question.contains("வழிகாட்டி") || question.contains("ஆவணம்") || question.contains("சாகுபடி") ||
                question.contains("निर्देशिका") || question.contains("दस्तावेज़") || question.contains("खेती");

        boolean isTriageQuery = !isExplicitRAGOrGuideQuery && ("PROBLEM_TRIAGE".equals(category) ||
                qLower.contains("yellow") || qLower.contains("wilt") || qLower.contains("drying") ||
                qLower.contains("leaf spot") || qLower.contains("curl") ||
                question.contains("மஞ்சள்") || question.contains("இலை கருகல்") || question.contains("வாடல்") ||
                question.contains("புள்ளி") || question.contains("சுருட்டை") || question.contains("காய்ந்து") ||
                qLower.contains("manjal") || qLower.contains("ilai") ||
                question.contains("पीली") || question.contains("पीला") || question.contains("मुरझाना") ||
                question.contains("धब्बे") || question.contains("मुड़ना") || question.contains("सूखना") ||
                qLower.contains("peeli patti") || qLower.contains("peela"));

        if (isTriageQuery) {
            CropProblemTriageEngine.TriageReport tr = cropProblemTriageEngine.triageProblem(crop, question);
            AdvisoryResponse resp = new AdvisoryResponse();
            resp.setAnswer(multilingualFormatter.formatTriageAdvisory(tr, language));
            resp.setLanguage(language);
            resp.setSources(buildSources(contextItems, "TNAU Agritech Portal / ICAR Crop Protection Guide"));
            return resp;
        }

        // 8. FEATURE 1 & RAG: Local File-Grounded Agricultural Advisory (Zero-Hallucination RAG Priority)
        List<ContextItem> docItems = (contextItems != null) ?
                contextItems.stream().filter(item -> "DOCUMENT".equalsIgnoreCase(item.getType())).toList() :
                Collections.emptyList();

        if (!docItems.isEmpty()) {
            StringBuilder ragPrompt = new StringBuilder();
            ragPrompt.append("You are Kisan AI, an expert agricultural decision assistant. ")
                     .append("Answer the farmer's question directly, accurately, and thoroughly using the facts, dosages, timelines, and recommendations provided in the Retrieved Knowledge Base Excerpts below.\n\n");

            ragPrompt.append("User Question: ").append(question).append("\n");
            ragPrompt.append("Crop: ").append(crop).append("\n");
            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                ragPrompt.append("Location: ").append(request.getLocation()).append("\n");
            }
            if (request.getSeason() != null && !request.getSeason().isBlank()) {
                ragPrompt.append("Season: ").append(request.getSeason()).append("\n");
            }

            ragPrompt.append("\n=======================================================\n");
            ragPrompt.append("RETRIEVED KNOWLEDGE BASE EXCERPTS (FROM LOCAL FILES):\n");
            for (ContextItem item : docItems) {
                ragPrompt.append("\n--- Source: ").append(item.getSourceName()).append(" ---\n");
                ragPrompt.append(item.getText()).append("\n");
            }
            ragPrompt.append("=======================================================\n\n");

            ragPrompt.append("STRICT GROUNDING & RESPONSE RULES:\n");
            ragPrompt.append("1. Answer Grounding: Answer the question directly using the information from the retrieved file excerpts above.\n");
            ragPrompt.append("2. Citations: Explicitly cite the document source titles [Document Title (Organization)] in your response.\n");
            ragPrompt.append("3. No Hallucination / No Scraping: Strictly rely on the retrieved knowledge base facts. Do not invent unapproved dosages or chemicals.\n");
            String langName = "ta".equalsIgnoreCase(language) ? "Tamil (தமிழ்)" :
                              "hi".equalsIgnoreCase(language) ? "Hindi (हिंदी)" : "English";
            ragPrompt.append("4. Language: You MUST write your complete response strictly in ").append(langName).append(" using native script.\n");
            ragPrompt.append("5. Practical Advice: Provide bullet points with actionable step-by-step guidance for the farmer.\n");

            String modelResponse = null;
            try {
                modelResponse = llmProvider.generateResponse(ragPrompt.toString(), language);
            } catch (Exception e) {
                logger.warn("RAG LLM generation failed or timed out: {}", e.getMessage());
            }

            if (modelResponse == null || modelResponse.trim().isEmpty() || modelResponse.contains("This is a stub")) {
                modelResponse = multilingualFormatter.formatDocumentRAGAdvisory(docItems, language, crop);
            }

            AdvisoryResponse response = new AdvisoryResponse();
            response.setAnswer(modelResponse);
            response.setLanguage(language);
            response.setSources(buildSources(contextItems, "Local Agricultural Knowledge Base"));
            return response;
        }

        // Fallback for open-ended queries when no document chunks match: Deterministic Agronomic Analysis
        DecisionSupportEngine.DecisionAnalysis da =
                decisionSupportEngine.analyzeDecision(crop, request.getLocation(), request.getSeason(), acres, budget, question);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("User Question: ").append(question).append("\n");
        promptBuilder.append("Crop: ").append(crop).append("\n");
        promptBuilder.append("Location: ").append(request.getLocation()).append("\n");
        promptBuilder.append("Season: ").append(request.getSeason()).append("\n\n");

        promptBuilder.append("Deterministic Agronomic Analysis Baseline:\n");
        promptBuilder.append("- Objective: ").append(da.objective()).append("\n");
        promptBuilder.append("- Recommended Strategy: ").append(da.recommendedOption()).append("\n");
        promptBuilder.append("- Trade-Off Rationale: ").append(da.tradeOffRationale()).append("\n");
        for (DecisionSupportEngine.FeasibleAlternative alt : da.comparedAlternatives()) {
            promptBuilder.append(String.format("- %s: Cost Rs %.0f/ac, Water: %s, Labor: %s\n",
                    alt.optionName(), alt.estimatedCostPerAcre(), alt.waterRequirement(), alt.laborIntensity()));
        }

        promptBuilder.append("\nGrounding Rules:\n");
        promptBuilder.append("- Answer concisely in the requested language (").append(language).append(").\n");
        promptBuilder.append("- Preserve all numerical figures and alternatives from the deterministic analysis.\n");
        promptBuilder.append("- Never guarantee yield, rainfall, or profits.\n");
        promptBuilder.append("- Include trade-offs and practical risk mitigation.\n");

        String modelResponse = null;
        try {
            modelResponse = llmProvider.generateResponse(promptBuilder.toString(), language);
        } catch (Exception e) {
            logger.warn("LLM generation failed or timed out: {}", e.getMessage());
        }

        // If LLM returned generic or stub text or failed, format with comprehensive decision analysis
        if (modelResponse == null || modelResponse.trim().isEmpty() || modelResponse.contains("This is a stub")) {
            modelResponse = multilingualFormatter.formatDecisionAdvisory(da, language);
        }

        AdvisoryResponse response = new AdvisoryResponse();
        response.setAnswer(modelResponse);
        response.setLanguage(language);
        response.setSources(buildSources(contextItems, "TNAU Crop Production Guide (2025) / ICAR"));
        return response;
    }

    private Double extractAcres(String text) {
        if (text == null) return null;
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(acre|acres|ac)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Double extractBudget(String text) {
        if (text == null) return null;
        Pattern pattern = Pattern.compile("(?:rs\\.?|inr|budget\\s*(?:of|is|:)?)\\s*(\\d{3,7})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Double extractWeatherMetric(List<ContextItem> items, String questionText, String... keywords) {
        if (items != null) {
            for (ContextItem item : items) {
                String text = item.getText();
                Double val = parseMetric(text, keywords);
                if (val != null) return val;
            }
        }
        if (questionText != null) {
            Double val = parseMetric(questionText, keywords);
            if (val != null) return val;
            for (String kw : keywords) {
                if (kw.equalsIgnoreCase("mm") || kw.equalsIgnoreCase("rain") || kw.equalsIgnoreCase("precipitation")) {
                    Matcher m = Pattern.compile("(\\d+(\\.\\d+)?)\\s*mm", Pattern.CASE_INSENSITIVE).matcher(questionText);
                    if (m.find()) {
                        try {
                            return Double.parseDouble(m.group(1));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return null;
    }

    private Double parseMetric(String text, String... keywords) {
        if (text == null) return null;
        for (String kw : keywords) {
            if (text.toLowerCase().contains(kw.toLowerCase())) {
                Matcher m = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(text);
                if (m.find()) {
                    try {
                        return Double.parseDouble(m.group(1));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return null;
    }

    private List<Source> buildSources(List<ContextItem> contextItems, String fallbackName) {
        List<Source> sources = new ArrayList<>();
        if (contextItems != null && !contextItems.isEmpty()) {
            for (ContextItem item : contextItems) {
                Source s = new Source();
                s.setName(item.getSourceName());
                s.setType(item.getType());
                s.setUrl(item.getSourceUrl());
                sources.add(s);
            }
        }
        if (sources.isEmpty()) {
            Source fallback = new Source();
            fallback.setName(fallbackName);
            fallback.setType("OFFICIAL_AGRONOMIC_STANDARD");
            fallback.setUrl("https://agritech.tnau.ac.in");
            sources.add(fallback);
        }
        return sources;
    }
}
