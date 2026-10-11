package com.agri.advisory.engine;

import com.agri.advisory.dto.ContextItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class AgriMultilingualFormatter {

    public String detectLanguage(String text, String requestedLanguage) {
        if (requestedLanguage != null && !requestedLanguage.trim().isEmpty() && !"auto".equalsIgnoreCase(requestedLanguage)) {
            return requestedLanguage.trim().toLowerCase();
        }
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }
        for (char c : text.toCharArray()) {
            if (c >= '\u0B80' && c <= '\u0BFF') {
                return "ta";
            }
            if (c >= '\u0900' && c <= '\u097F') {
                return "hi";
            }
        }
        return "en";
    }

    public String normalizeCrop(String cropInput, String question) {
        String combined = ((cropInput != null ? cropInput : "") + " " + (question != null ? question : "")).toLowerCase();
        
        // Tamil crop names
        if (combined.contains("நெல்") || combined.contains("அரிசி") || combined.contains("nellu")) return "paddy";
        if (combined.contains("தக்காளி") || combined.contains("thakkali")) return "tomato";
        if (combined.contains("மாம்பழம்") || combined.contains("மாமரம்") || combined.contains("மாங்காய்") || combined.contains("மா")) return "mango";
        if (combined.contains("கரும்பு") || combined.contains("karumbu")) return "sugarcane";
        if (combined.contains("பருத்தி") || combined.contains("paruthi")) return "cotton";
        if (combined.contains("வாழை") || combined.contains("vazhai")) return "banana";
        if (combined.contains("மக்காச்சோளம்") || combined.contains("சோளம்") || combined.contains("cholam")) return "maize";
        if (combined.contains("நிலக்கடலை") || combined.contains("வேர்க்கடலை") || combined.contains("kadalai")) return "groundnut";
        if (combined.contains("மிளகாய்") || combined.contains("milagai")) return "chilli";

        // Hindi crop names
        if (combined.contains("धान") || combined.contains("चावल") || combined.contains("dhan")) return "paddy";
        if (combined.contains("टमाटर") || combined.contains("tamatar")) return "tomato";
        if (combined.contains("आम") || combined.contains("aam")) return "mango";
        if (combined.contains("गन्ना") || combined.contains("ganna")) return "sugarcane";
        if (combined.contains("कपास") || combined.contains("kapas")) return "cotton";
        if (combined.contains("केला") || combined.contains("kela")) return "banana";
        if (combined.contains("मक्का") || combined.contains("भुट्टा") || combined.contains("makka")) return "maize";
        if (combined.contains("मूंगफली") || combined.contains("mungfali")) return "groundnut";
        if (combined.contains("मिर्च") || combined.contains("mirch")) return "chilli";

        // Standard English
        if (combined.contains("rice") || combined.contains("paddy")) return "paddy";
        if (combined.contains("tomato")) return "tomato";
        if (combined.contains("mango")) return "mango";
        if (combined.contains("sugarcane")) return "sugarcane";
        if (combined.contains("cotton")) return "cotton";
        if (combined.contains("banana")) return "banana";
        if (combined.contains("maize") || combined.contains("corn")) return "maize";
        if (combined.contains("groundnut") || combined.contains("peanut")) return "groundnut";
        if (combined.contains("chilli") || combined.contains("chili")) return "chilli";

        return cropInput != null && !cropInput.trim().isEmpty() ? cropInput.trim() : "paddy";
    }

    public String formatPesticideAdvisory(PesticideSafetyEngine.SafetyEvaluation eval, String language) {
        if ("ta".equalsIgnoreCase(language)) {
            if (!eval.approved()) {
                return String.format(
                        "🚨 கடுமையான சட்டரீதியான பாதுகாப்பு மறுப்பு: பரிந்துரைக்கப்படாத (Off-Label) பூச்சிக்கொல்லி பயன்பாடு!\n" +
                        "- இலக்கு பயிர்: %s | கோரப்பட்ட மருந்து: %s\n" +
                        "- மறுப்புக்கான காரணம்: பூச்சிக்கொல்லி சட்டம் 1968-ன் கீழ் மத்திய பூச்சிக்கொல்லி வாரியம் மற்றும் பதிவு குழுவினால் (CIBRC) %s மருந்து %s பயிருக்கு பதிவு செய்யப்படவில்லை.\n" +
                        "- சட்ட அறிவிப்பு: பதிவு செய்யப்படாத பயிர்களில் பூச்சிக்கொல்லிகளைப் பயன்படுத்துவது பூச்சிக்கொல்லி சட்டம் 1968 மற்றும் இந்திய தண்டனைச் சட்டத்தின் கீழ் தண்டனைக்குரிய குற்றமாகும்.\n\n" +
                        "🌿 பாதுகாப்பான மாற்று முறை (IPM Alternative):\n%s\n\n" +
                        "⚠️ அங்கீகரிக்கப்படாத ரசாயனங்களை வயலில் தெளிக்க வேண்டாம். அருகிலுள்ள வேளாண் விரிவாக்க மையத்தை அணுகவும்.",
                        eval.targetCrop().toUpperCase(), eval.pesticideName(), eval.pesticideName(), eval.targetCrop().toUpperCase(), eval.ipmAlternative()
                );
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🌿 CIBRC / TNAU பதிவு செய்யப்பட்ட பூச்சிக்கொல்லி அளவு மற்றும் பாதுகாப்பு வழிகாட்டுதல்\n");
            sb.append(String.format("- பயிர்: %s | சாகுபடி பரப்பளவு: %.2f ஏக்கர்\n", eval.targetCrop().toUpperCase(), eval.requestedAcres()));
            sb.append(String.format("- பரிந்துரைக்கப்பட்ட மருந்து: %s\n", eval.pesticideName()));
            sb.append(String.format("- CIBRC பதிவு எண்: %s\n", eval.warningNotice() != null ? "CIBRC அங்கீகரிக்கப்பட்டது" : "அங்கீகரிக்கப்பட்டது"));
            sb.append(String.format("- நச்சு வகை: %s\n\n", eval.toxicityBand()));

            sb.append("📏 துல்லிய கணக்கிடப்பட்ட மருந்தளவு (Deterministic Dosage):\n");
            sb.append(String.format("  • மொத்த மருந்து தேவை: %.2f %s (%.2f ஏக்கருக்கு)\n", eval.totalChemicalAmount(), eval.unit(), eval.requestedAcres()));
            sb.append(String.format("  • தெளிப்பான் தொட்டி அளவு (Tank Dose): 10 லிட்டர் தெளிப்பானுக்கு %.2f %s\n", eval.dosePer16LTank(), eval.unit()));
            sb.append(String.format("  • தேவைப்படும் தண்ணீர் அளவு: %.0f லிட்டர் (தோராயமாக %d தெளிப்பான் தொட்டிகள்)\n\n", eval.totalWaterLiters(), eval.knapsackTanksNeeded()));

            sb.append("⏳ கட்டாய காத்திருப்பு இடைவெளிகள் (Mandatory Intervals):\n");
            sb.append(String.format("  • அறுவடைக்கு முந்தைய இடைவெளி (PHI): %d நாட்கள் (மருந்து தெளித்த பின் அறுவடைக்கு முன் காத்திருக்க வேண்டிய நாட்கள்)\n", eval.phiDays()));
            sb.append(String.format("  • மறுநுழைவு இடைவெளி (REI): %d மணிநேரம் (மருந்து தெளித்த பின் வயலில் மனிதர்கள் நுழையக்கூடாத நேரம்)\n\n", eval.reiHours()));

            sb.append(String.format("🛡️ பாதுகாப்பு உபகரணங்கள் (PPE): %s\n", eval.ppe()));
            sb.append(String.format("⚠️ முன்னெச்சரிக்கைகள்: %s\n", eval.cautions()));
            sb.append(String.format("🌾 மாற்று இயற்கை முறை (IPM): %s\n", eval.ipmAlternative()));
            sb.append("\n📌 சட்ட எச்சரிக்கை: மத்திய பூச்சிக்கொல்லி வாரியம் (CIBRC) மற்றும் பூச்சிக்கொல்லி சட்டம் 1968 விதிகளின்படி அங்கீகரிக்கப்பட்ட அளவை மட்டும் பயன்படுத்தவும்.");
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            if (!eval.approved()) {
                return String.format(
                        "🚨 गंभीर वैधानिक सुरक्षा अस्वीकृति: अनाधिकृत (Off-Label) कीटनाशक प्रयोग निषेध!\n" +
                        "- लक्षित फसल: %s | मांगी गई दवा: %s\n" +
                        "- अस्वीकृति का कारण: कीटनाशक अधिनियम 1968 के तहत केंद्रीय कीटनाशक बोर्ड एवं पंजीकरण समिति (CIBRC) द्वारा %s रसायन %s फसल के लिए पंजीकृत नहीं है।\n" +
                        "- वैधानिक सूचना: अपंजीकृत फसलों पर रासायनिक कीटनाशकों का प्रयोग कीटनाशक अधिनियम 1968 के अंतर्गत दंडनीय अपराध है।\n\n" +
                        "🌿 सुरक्षित अनुशंसित विकल्प (IPM Alternative):\n%s\n\n" +
                        "⚠️ कृपया गैर-पंजीकृत रसायनों का छिड़काव न करें। नजदीकी कृषि विज्ञान केंद्र से संपर्क करें।",
                        eval.targetCrop().toUpperCase(), eval.pesticideName(), eval.pesticideName(), eval.targetCrop().toUpperCase(), eval.ipmAlternative()
                );
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🌿 CIBRC / TNAU अनुमोदित कीटनाशक मात्रा एवं सुरक्षा परामर्श\n");
            sb.append(String.format("- फसल: %s | खेत का क्षेत्रफल: %.2f एकड़\n", eval.targetCrop().toUpperCase(), eval.requestedAcres()));
            sb.append(String.format("- स्वीकृत रसायन: %s\n", eval.pesticideName()));
            sb.append(String.format("- विषाक्तता श्रेणी: %s\n\n", eval.toxicityBand()));

            sb.append("📏 सटीक निर्धारित खुराक (Deterministic Dosage):\n");
            sb.append(String.format("  • कुल आवश्यक रसायन: %.2f %s (%.2f एकड़ के लिए)\n", eval.totalChemicalAmount(), eval.unit(), eval.requestedAcres()));
            sb.append(String.format("  • स्प्रे टैंक खुराक: 10 लीटर नैपसैक टैंक के लिए %.2f %s\n", eval.dosePer16LTank(), eval.unit()));
            sb.append(String.format("  • कुल आवश्यक पानी: %.0f लीटर (लगभग %d स्प्रे टैंक)\n\n", eval.totalWaterLiters(), eval.knapsackTanksNeeded()));

            sb.append("⏳ अनिवार्य सुरक्षा प्रतीक्षा अवधि (Mandatory Safety Intervals):\n");
            sb.append(String.format("  • कटाई पूर्व प्रतीक्षा अवधि (PHI): %d दिन (छिड़काव के बाद फसल काटने से पूर्व प्रतीक्षा)\n", eval.phiDays()));
            sb.append(String.format("  • खेत में पुनः प्रवेश अवधि (REI): %d घंटे (छिड़काव के बाद खेत में जाने से परहेज करें)\n\n", eval.reiHours()));

            sb.append(String.format("🛡️ व्यक्तिगत सुरक्षा उपकरण (PPE): %s\n", eval.ppe()));
            sb.append(String.format("⚠️ सावधानियां: %s\n", eval.cautions()));
            sb.append(String.format("🌾 एकीकृत कीट प्रबंधन (IPM): %s\n", eval.ipmAlternative()));
            sb.append("\n📌 वैधानिक चेतावनी: कीटनाशक अधिनियम 1968 के अनुसार स्वीकृत लेबल मात्रा एवं सुरक्षा निर्देशों का कड़ाई से पालन करें।");
            return sb.toString();
        }

        // Default English
        return eval.detailedAdvisory();
    }

    public String formatLowCostAdvisory(LowCostPlannerEngine.LowCostPlanResult result, String language) {
        if ("ta".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌾 குறைந்த செலவு நீடித்த வேளாண்மை உத்திகள் (Low-Cost Optimization)\n");
            sb.append(String.format("- பயிர்: %s | சாகுபடி பரப்பளவு: %.2f ஏக்கர்\n", result.crop().toUpperCase(), result.acres()));
            sb.append(String.format("- வழக்கமான ரசாயன சாகுபடி செலவு: ரூ. %.2f\n", result.totalConventionalCost()));
            sb.append(String.format("- உகந்த குறைந்த செலவு திட்டம்:    ரூ. %.2f\n", result.totalOptimizedCost()));
            sb.append(String.format("- VERIFIED நிகர சேமிப்பு:           ரூ. %.2f (%.1f%% செலவு குறைப்பு!)\n\n",
                    result.totalSavingsInr(), result.savingsPercentage()));

            sb.append("📉 செலவு குறைக்கும் முதன்மை உத்திகள் (Actionable Cost-Reduction Interventions):\n");
            for (LowCostPlannerEngine.CostIntervention ci : result.interventions()) {
                sb.append(String.format("\n🔹 %s:\n", ci.practiceCategory()));
                sb.append(String.format("  • வழக்கமான முறை: %s (ரூ. %.0f/ஏக்கர்)\n", ci.conventionalPractice(), ci.conventionalCostPerAcre()));
                sb.append(String.format("  • குறைந்த செலவு மாற்று: %s (ரூ. %.0f/ஏக்கர்)\n", ci.lowCostAlternative(), ci.alternativeCostPerAcre()));
                sb.append(String.format("  • நிகர சேமிப்பு: ரூ. %.0f/ஏக்கர்\n", ci.netSavingsPerAcre()));
                sb.append(String.format("  • வேளாண் அடிப்படை: %s\n", ci.agronomicBasis()));
            }

            sb.append("\n🚜 உடனடி கள நடவடிக்கைகள்:\n");
            for (String a : result.keyActionItems()) {
                sb.append("  ✔ ").append(a).append("\n");
            }
            sb.append("\n⚠️ அறிவிப்பு: ICAR மற்றும் தமிழ்நாடு வேளாண் பல்கலைக்கழக (TNAU) பரிந்துரைப்படி விளைச்சல் குறையாமல் செலவு குறைக்க வழிகாட்டப்படுகிறது.");
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌾 कम लागत वाली टिकाऊ कृषि योजना (Low-Cost Farming Optimization)\n");
            sb.append(String.format("- फसल: %s | कुल क्षेत्रफल: %.2f एकड़\n", result.crop().toUpperCase(), result.acres()));
            sb.append(String.format("- पारंपरिक रासायनिक खेती खर्च: ₹ %.2f\n", result.totalConventionalCost()));
            sb.append(String.format("- अनुकूलित कम लागत योजना:      ₹ %.2f\n", result.totalOptimizedCost()));
            sb.append(String.format("- कुल सत्यापित शुद्ध बचत:      ₹ %.2f (%.1f%% खर्च में बचत!)\n\n",
                    result.totalSavingsInr(), result.savingsPercentage()));

            sb.append("📉 लागत घटाने के प्रमुख तकनीकी उपाय (Cost-Reduction Interventions):\n");
            for (LowCostPlannerEngine.CostIntervention ci : result.interventions()) {
                sb.append(String.format("\n🔹 %s:\n", ci.practiceCategory()));
                sb.append(String.format("  • पारंपरिक विधि: %s (₹ %.0f/एकड़)\n", ci.conventionalPractice(), ci.conventionalCostPerAcre()));
                sb.append(String.format("  • कम लागत विकल्प: %s (₹ %.0f/एकड़)\n", ci.lowCostAlternative(), ci.alternativeCostPerAcre()));
                sb.append(String.format("  • शुद्ध बचत: ₹ %.0f/एकड़\n", ci.netSavingsPerAcre()));
                sb.append(String.format("  • कृषि वैज्ञानिक आधार: %s\n", ci.agronomicBasis()));
            }

            sb.append("\n🚜 त्वरित मैदानी कदम:\n");
            for (String a : result.keyActionItems()) {
                sb.append("  ✔ ").append(a).append("\n");
            }
            sb.append("\n⚠️ सूचना: ICAR और राज्य कृषि विश्वविद्यालय के मानकों पर आधारित, जिससे पैदावार घटे बिना खेती का खर्च कम हो।");
            return sb.toString();
        }

        // Default English
        StringBuilder sb = new StringBuilder();
        sb.append("🌾 LOW-COST SUSTAINABLE FARMING OPTIMIZATION\n");
        sb.append(String.format("- Crop: %s | Cultivated Area: %.2f acres\n", result.crop(), result.acres()));
        sb.append(String.format("- Baseline Conventional Cost: Rs %.2f\n", result.totalConventionalCost()));
        sb.append(String.format("- Optimized Low-Cost Plan:    Rs %.2f\n", result.totalOptimizedCost()));
        sb.append(String.format("- VERIFIED NET SAVINGS:        Rs %.2f (%.1f%% reduction!)\n\n",
                result.totalSavingsInr(), result.savingsPercentage()));

        sb.append("📉 ACTIONABLE COST-REDUCTION INTERVENTIONS:\n");
        for (LowCostPlannerEngine.CostIntervention ci : result.interventions()) {
            sb.append(String.format("\n🔹 %s:\n", ci.practiceCategory()));
            sb.append(String.format("  • Conventional: %s (Rs %.0f/ac)\n", ci.conventionalPractice(), ci.conventionalCostPerAcre()));
            sb.append(String.format("  • Low-Cost Alt: %s (Rs %.0f/ac)\n", ci.lowCostAlternative(), ci.alternativeCostPerAcre()));
            sb.append(String.format("  • Net Savings:  Rs %.0f/acre\n", ci.netSavingsPerAcre()));
            sb.append(String.format("  • Agronomic Basis: %s\n", ci.agronomicBasis()));
        }

        sb.append("\n🚜 KEY IMMEDIATE ACTIONS:\n");
        for (String a : result.keyActionItems()) {
            sb.append("  ✔ ").append(a).append("\n");
        }
        sb.append("\n⚠️ ").append(result.complianceNotice()).append("\n");
        return sb.toString();
    }

    public String formatBudgetAdvisory(BudgetPlanningEngine.BudgetPlanResult planResult, String language) {
        if ("ta".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("💰 பட்ஜெட் அடிப்படையிலான பயிர் சாகுபடி திட்டம்\n");
            sb.append(String.format("- பயிர்: %s | நிலப் பரப்பளவு: %.2f ஏக்கர்\n", planResult.crop().toUpperCase(), planResult.acres()));
            if (planResult.availableBudget() > 0) {
                sb.append(String.format("- விவசாயிடம் உள்ள இருப்பு பட்ஜெட்: ரூ. %.2f\n", planResult.availableBudget()));
            }
            sb.append(String.format("- தேவைப்படும் மொத்த செலவு: ரூ. %.2f (ஏக்கருக்கு ரூ. %.2f)\n", planResult.totalRequiredCost(), planResult.costPerAcre()));
            sb.append(String.format("- பயிர் காலம்: %d நாட்கள் | நிதி சாத்தியக்கூறு: %s\n\n",
                    planResult.cropDurationDays(),
                    "FEASIBLE".equalsIgnoreCase(planResult.feasibilityStatus()) ? "சாத்தியமானது (Feasible)" : "நிதி பற்றாக்குறை (Deficit)"));

            if (planResult.budgetWarning() != null) {
                sb.append("⚠️ எச்சரிக்கை: ").append(planResult.budgetWarning()).append("\n\n");
            }

            sb.append("📊 உருப்படி வாரியான செலவு விவரங்கள் (INR):\n");
            planResult.costBreakdown().forEach((item, cost) ->
                    sb.append(String.format("  • %-35s : ரூ. %10.2f\n", item, cost)));

            sb.append("\n💡 வேளாண் பரிந்துரைகள்:\n");
            for (String rec : planResult.recommendations()) {
                sb.append("  ✔ ").append(rec).append("\n");
            }
            sb.append("\n📌 குறிப்பு: TNAU / ICAR சாகுபடி செலவு ஆய்வு அறிக்கைகளின் அடிப்படையில் கணக்கிடப்பட்டது.");
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("💰 बजट-आधारित फसल उत्पादन एवं लागत योजना\n");
            sb.append(String.format("- फसल: %s | खेत का क्षेत्रफल: %.2f एकड़\n", planResult.crop().toUpperCase(), planResult.acres()));
            if (planResult.availableBudget() > 0) {
                sb.append(String.format("- किसान के पास उपलब्ध बजट: ₹ %.2f\n", planResult.availableBudget()));
            }
            sb.append(String.format("- कुल अनुमानित लागत: ₹ %.2f (प्रति एकड़ ₹ %.2f)\n", planResult.totalRequiredCost(), planResult.costPerAcre()));
            sb.append(String.format("- फसल अवधि: %d दिन | वित्तीय स्थिति: %s\n\n",
                    planResult.cropDurationDays(),
                    "FEASIBLE".equalsIgnoreCase(planResult.feasibilityStatus()) ? "पर्याप्त बजट (Feasible)" : "बजट कमी (Deficit)"));

            if (planResult.budgetWarning() != null) {
                sb.append("⚠️ चेतावनी: ").append(planResult.budgetWarning()).append("\n\n");
            }

            sb.append("📊 मद-वार विस्तृत लागत विवरण (INR):\n");
            planResult.costBreakdown().forEach((item, cost) ->
                    sb.append(String.format("  • %-35s : ₹ %10.2f\n", item, cost)));

            sb.append("\n💡 कृषि वित्तीय सिफारिशें:\n");
            for (String rec : planResult.recommendations()) {
                sb.append("  ✔ ").append(rec).append("\n");
            }
            sb.append("\n📌 नोट: ICAR और कृषि लागत अध्ययन रिपोर्टों के आधार पर निर्धारित।");
            return sb.toString();
        }

        // Default English
        StringBuilder sb = new StringBuilder();
        sb.append("💰 BUDGET-AWARE CROP CULTIVATION PLAN\n");
        sb.append(String.format("- Crop: %s | Land Area: %.2f acres\n", planResult.crop(), planResult.acres()));
        if (planResult.availableBudget() > 0) {
            sb.append(String.format("- Farmer Available Budget: Rs %.2f\n", planResult.availableBudget()));
        }
        sb.append(String.format("- Total Estimated Required Cost: Rs %.2f (Rs %.2f/acre)\n", planResult.totalRequiredCost(), planResult.costPerAcre()));
        sb.append(String.format("- Crop Duration: %d days | Feasibility Status: %s\n\n", planResult.cropDurationDays(), planResult.feasibilityStatus()));

        if (planResult.budgetWarning() != null) {
            sb.append("⚠️ ").append(planResult.budgetWarning()).append("\n\n");
        }

        sb.append("📊 DETERMINISTIC ITEM-WISE COST BREAKDOWN (INR):\n");
        planResult.costBreakdown().forEach((item, cost) ->
                sb.append(String.format("  • %-38s : Rs %10.2f\n", item, cost)));

        sb.append("\n💡 STRATEGIC AGRONOMIC RECOMMENDATIONS:\n");
        for (String rec : planResult.recommendations()) {
            sb.append("  ✔ ").append(rec).append("\n");
        }
        sb.append("\n📌 Note: ").append(planResult.calculationNotes()).append("\n");
        return sb.toString();
    }

    public String formatSchemeAdvisory(SchemeNavigatorEngine.SchemeAssessmentReport report, String language) {
        if ("ta".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🏛️ மத்திய & மாநில அரசு விவசாய திட்டங்கள் மற்றும் மானிய வழிகாட்டி\n");
            sb.append(String.format("- நில உடமை: %.2f ஏக்கர் (%s)\n", report.landholdingAcres(), report.landCategory()));
            sb.append(String.format("- இருப்பிடம்: %s, %s | முதன்மை பயிர்: %s\n\n", report.district(), report.state(), report.crop()));

            for (SchemeNavigatorEngine.SchemeEvaluation se : report.evaluations()) {
                sb.append(String.format("📋 %s (%s) — நிலை: [%s]\n", se.schemeName(), se.schemeCode(), se.status()));
                sb.append(String.format("  • பலன்கள்: %s\n", se.benefitSummary()));
                sb.append("  • பூர்த்தியான தகுதிகள்:\n");
                for (String sat : se.satisfiedCriteria()) sb.append("    ✔ ").append(sat).append("\n");
                sb.append("  • சரிபார்க்க வேண்டியவை:\n");
                for (String pen : se.pendingOrMissingCriteria()) sb.append("    ⏳ ").append(pen).append("\n");
                sb.append(String.format("  • அதிகாரப்பூர்வ இணையதளம்: %s\n", se.officialPortalUrl()));
                sb.append(String.format("  • விண்ணப்பிக்கும் முறை: %s\n\n", se.applicationProcedure()));
            }
            sb.append("⚠️ குறிப்பு: அரசு திட்ட விதிகளின்படி ஆதார், பட்டா/சிட்டா ஆவணங்கள் சரிபார்ப்புக்கு உட்பட்டது.");
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🏛️ सरकारी कृषि योजनाएं एवं सब्सिडी मार्गदर्शक\n");
            sb.append(String.format("- भूमि जोत: %.2f एकड़ (%s)\n", report.landholdingAcres(), report.landCategory()));
            sb.append(String.format("- स्थान: %s, %s | मुख्य फसल: %s\n\n", report.district(), report.state(), report.crop()));

            for (SchemeNavigatorEngine.SchemeEvaluation se : report.evaluations()) {
                sb.append(String.format("📋 %s (%s) — पात्रता स्थिति: [%s]\n", se.schemeName(), se.schemeCode(), se.status()));
                sb.append(String.format("  • योजना लाभ: %s\n", se.benefitSummary()));
                sb.append("  • पूर्ण पात्रता शर्तें:\n");
                for (String sat : se.satisfiedCriteria()) sb.append("    ✔ ").append(sat).append("\n");
                sb.append("  • आवश्यक दस्तावेज/सत्यापन:\n");
                for (String pen : se.pendingOrMissingCriteria()) sb.append("    ⏳ ").append(pen).append("\n");
                sb.append(String.format("  • आधिकारिक पोर्टल: %s\n", se.officialPortalUrl()));
                sb.append(String.format("  • आवेदन प्रक्रिया: %s\n\n", se.applicationProcedure()));
            }
            sb.append("⚠️ नोट: अंतिम लाभ संबंधित सरकारी विभाग एवं पोर्टल सत्यापन के अधीन है।");
            return sb.toString();
        }

        // Default English
        StringBuilder sb = new StringBuilder();
        sb.append("🏛️ GOVERNMENT AGRICULTURAL SCHEMES & SUBSIDY NAVIGATOR\n");
        sb.append(String.format("- Landholding: %.2f acres (%s)\n", report.landholdingAcres(), report.landCategory()));
        sb.append(String.format("- Location: %s, %s | Primary Crop: %s\n\n", report.district(), report.state(), report.crop()));

        for (SchemeNavigatorEngine.SchemeEvaluation se : report.evaluations()) {
            sb.append(String.format("📋 %s (%s) — Status: [%s]\n", se.schemeName(), se.schemeCode(), se.status()));
            sb.append(String.format("  • Benefits: %s\n", se.benefitSummary()));
            sb.append("  • Satisfied Criteria:\n");
            for (String sat : se.satisfiedCriteria()) sb.append("    ✔ ").append(sat).append("\n");
            sb.append("  • Pending / Mandatory Verification:\n");
            for (String pen : se.pendingOrMissingCriteria()) sb.append("    ⏳ ").append(pen).append("\n");
            sb.append(String.format("  • Official Portal: %s\n", se.officialPortalUrl()));
            sb.append(String.format("  • Application Mode: %s\n\n", se.applicationProcedure()));
        }
        sb.append("⚠️ ").append(report.officialDisclaimer()).append("\n");
        return sb.toString();
    }

    public String formatWeatherAdvisory(WeatherRiskEngine.WeatherRiskAssessment wr, String language) {
        String dirText = wr.directive().name();
        String dirTa = "PROCEED".equals(dirText) ? "தொடங்கலாம் (PROCEED)" :
                      "WAIT".equals(dirText) ? "காத்திருக்கவும் (WAIT)" : "தயார் செய்யவும் (PREPARE)";
        String dirHi = "PROCEED".equals(dirText) ? "आगे बढ़ें (PROCEED)" :
                      "WAIT".equals(dirText) ? "प्रतीक्षा करें (WAIT)" : "तैयारी करें (PREPARE)";

        if ("ta".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌦️ வானிலை இடர் மற்றும் வேளாண் ஆலோசனை\n");
            sb.append(String.format("- இருப்பிடம்: %s | பயிர்: %s | வழிகாட்டுதல்: [%s]\n", wr.location(), wr.crop().toUpperCase(), dirTa));
            sb.append(String.format("- விதைப்பு சாளர நிலை (Sowing Window): %s\n", wr.sowingWindowStatus()));
            sb.append(String.format("- பூச்சிக்கொல்லி தெளிப்பு உகந்த நிலை: %s\n\n", wr.spraySuitabilityNotice()));

            if (!wr.alerts().isEmpty()) {
                sb.append("🚨 செயலில் உள்ள வானிலை எச்சரிக்கைகள்:\n");
                for (WeatherRiskEngine.WeatherRiskAlert a : wr.alerts()) {
                    sb.append(String.format("  ⚠️ [%s - %s]: %s\n", a.riskType(), a.severity(), a.description()));
                    sb.append(String.format("     • வேளாண் தாக்கம்: %s\n", a.agronomicImpact()));
                    sb.append(String.format("     • பாதுகாப்பு நடவடிக்கை: %s\n", a.suggestedMitigation()));
                }
                sb.append("\n");
            }
            sb.append("ℹ️ விளக்கம்: ").append(wr.directiveExplanation()).append("\n");
            sb.append(String.format("📡 தகவல் ஆதாரம்: %s (%s)\n", wr.forecastSource(), wr.metadataFreshness()));
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌦️ मौसम जोखिम एवं कृषि बुवाई परामर्श\n");
            sb.append(String.format("- स्थान: %s | फसल: %s | मौसम निर्देश: [%s]\n", wr.location(), wr.crop().toUpperCase(), dirHi));
            sb.append(String.format("- बुवाई उपयुक्तता स्थिति: %s\n", wr.sowingWindowStatus()));
            sb.append(String.format("- कीटनाशक छिड़काव उपयुक्तता: %s\n\n", wr.spraySuitabilityNotice()));

            if (!wr.alerts().isEmpty()) {
                sb.append("🚨 सक्रिय मौसम चेतावनी:\n");
                for (WeatherRiskEngine.WeatherRiskAlert a : wr.alerts()) {
                    sb.append(String.format("  ⚠️ [%s - %s]: %s\n", a.riskType(), a.severity(), a.description()));
                    sb.append(String.format("     • कृषि प्रभाव: %s\n", a.agronomicImpact()));
                    sb.append(String.format("     • बचाव उपाय: %s\n", a.suggestedMitigation()));
                }
                sb.append("\n");
            }
            sb.append("ℹ️ विवरण: ").append(wr.directiveExplanation()).append("\n");
            sb.append(String.format("📡 मौसम स्रोत: %s (%s)\n", wr.forecastSource(), wr.metadataFreshness()));
            return sb.toString();
        }

        // Default English
        StringBuilder sb = new StringBuilder();
        sb.append("🌦️ WEATHER-RISK & AGROMET SOWING ADVISORY\n");
        sb.append(String.format("- Location: %s | Crop: %s | Directive: [%s]\n", wr.location(), wr.crop(), wr.directive()));
        sb.append(String.format("- Sowing Window Status: %s\n", wr.sowingWindowStatus()));
        sb.append(String.format("- Spray Suitability: %s\n\n", wr.spraySuitabilityNotice()));

        if (!wr.alerts().isEmpty()) {
            sb.append("🚨 ACTIVE AGROMET RISK ALERTS:\n");
            for (WeatherRiskEngine.WeatherRiskAlert a : wr.alerts()) {
                sb.append(String.format("  ⚠️ [%s - %s]: %s\n", a.riskType(), a.severity(), a.description()));
                sb.append(String.format("     • Impact: %s\n", a.agronomicImpact()));
                sb.append(String.format("     • Mitigation: %s\n", a.suggestedMitigation()));
            }
            sb.append("\n");
        }
        sb.append("ℹ️ Explanation: ").append(wr.directiveExplanation()).append("\n");
        sb.append(String.format("📡 Source: %s (%s)\n", wr.forecastSource(), wr.metadataFreshness()));
        return sb.toString();
    }

    public String formatTriageAdvisory(CropProblemTriageEngine.TriageReport tr, String language) {
        if ("ta".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🔍 பயிர் பிரச்சனை மற்றும் நோய்/பூச்சி அறிதல் (Problem Triage)\n");
            sb.append(String.format("- பயிர்: %s | தெரிவிக்கப்பட்ட அறிகுறிகள்: '%s'\n\n", tr.crop().toUpperCase(), tr.reportedSymptoms()));

            sb.append("📊 நோயறிதல் மதிப்பீடு மற்றும் நம்பிக்கை சதவீதம் (Differential Ranking):\n");
            for (CropProblemTriageEngine.DiagnosticPossibility dp : tr.rankedPossibilities()) {
                sb.append(String.format("  • %s [நம்பிக்கை: %d%% | வகை: %s]\n", dp.probableCause(), dp.confidencePercentage(), dp.category()));
                sb.append(String.format("    - வயல்வெளி அறிகுறிகள்: %s\n", dp.distinguishingSigns()));
                sb.append(String.format("    - உடனடி நிவாரண நடவடிக்கை: %s\n\n", dp.initialAction()));
            }

            if (!tr.targetedFollowUpQuestions().isEmpty()) {
                sb.append("❓ துல்லியமாக கண்டறிய வயல்வெளி கேள்விகள்:\n");
                for (String q : tr.targetedFollowUpQuestions()) {
                    sb.append("  👉 ").append(q).append("\n");
                }
                sb.append("\n");
            }
            sb.append("⚠️ அறிவிப்பு: வயலில் நேரில் பரிசோதித்து, தீவிர தாக்குதல் இருப்பின் வேளாண் அலுவலரை அணுகவும்.");
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🔍 फसल समस्या निदान एवं संभावित कारण (Problem Triage)\n");
            sb.append(String.format("- फसल: %s | रिपोर्ट किए गए लक्षण: '%s'\n\n", tr.crop().toUpperCase(), tr.reportedSymptoms()));

            sb.append("📊 संभावित कारण एवं विश्वास प्रतिशत (Diagnostic Ranking):\n");
            for (CropProblemTriageEngine.DiagnosticPossibility dp : tr.rankedPossibilities()) {
                sb.append(String.format("  • %s [विश्वास: %d%% | श्रेणी: %s]\n", dp.probableCause(), dp.confidencePercentage(), dp.category()));
                sb.append(String.format("    - मुख्य मैदानी लक्षण: %s\n", dp.distinguishingSigns()));
                sb.append(String.format("    - प्राथमिक उपचार: %s\n\n", dp.initialAction()));
            }

            if (!tr.targetedFollowUpQuestions().isEmpty()) {
                sb.append("❓ सही पहचान के लिए प्रमुख मैदानी प्रश्न:\n");
                for (String q : tr.targetedFollowUpQuestions()) {
                    sb.append("  👉 ").append(q).append("\n");
                }
                sb.append("\n");
            }
            sb.append("⚠️ सूचना: खेत में प्रत्यक्ष निरीक्षण करें एवं संशय की स्थिति में कृषि विज्ञान केंद्र से संपर्क करें।");
            return sb.toString();
        }

        // Default English
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 UNCERTAINTY-AWARE CROP PROBLEM TRIAGE\n");
        sb.append(String.format("- Crop: %s | Reported Symptoms: '%s'\n\n", tr.crop(), tr.reportedSymptoms()));

        sb.append("📊 DIFFERENTIAL DIAGNOSTIC RANKING:\n");
        for (CropProblemTriageEngine.DiagnosticPossibility dp : tr.rankedPossibilities()) {
            sb.append(String.format("  • %s [Confidence: %d%% | %s]\n", dp.probableCause(), dp.confidencePercentage(), dp.category()));
            sb.append(String.format("    - Distinguishing In-Field Signs: %s\n", dp.distinguishingSigns()));
            sb.append(String.format("    - Immediate Remedial Action: %s\n\n", dp.initialAction()));
        }

        if (!tr.targetedFollowUpQuestions().isEmpty()) {
            sb.append("❓ TARGETED FIELD QUESTIONS TO RESOLVE DIAGNOSIS:\n");
            for (String q : tr.targetedFollowUpQuestions()) {
                sb.append("  👉 ").append(q).append("\n");
            }
            sb.append("\n");
        }
        sb.append("⚠️ ").append(tr.uncertaintyDisclaimer()).append("\n");
        return sb.toString();
    }

    public String formatDecisionAdvisory(DecisionSupportEngine.DecisionAnalysis da, String language) {
        if ("ta".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌾 வேளாண் முடிவெடுக்கும் வழிகாட்டி (Decision Support Assistant)\n");
            sb.append(String.format("- பகுப்பாய்வு நோக்கம்: %s\n", da.objective()));
            sb.append(String.format("- வள வரம்புகள்: %s\n\n", da.constraintsSummary()));

            sb.append("⚖️ சாத்தியமான சாகுபடி மாற்றுகள் ஒப்பீடு:\n");
            for (DecisionSupportEngine.FeasibleAlternative fa : da.comparedAlternatives()) {
                sb.append(String.format("🔹 %s\n", fa.optionName()));
                sb.append(String.format("   • உத்தி: %s\n", fa.agronomicStrategy()));
                sb.append(String.format("   • ஏக்கர் செலவு: ரூ. %.2f | நீர் தேவை: %s | உழைப்பு: %s\n",
                        fa.estimatedCostPerAcre(), fa.waterRequirement(), fa.laborIntensity()));
                sb.append(String.format("   • காலநிலை தாங்கும் திறன்: %s | விலை இடர்: %s\n\n",
                        fa.climateResilience(), fa.marketAndPriceRisk()));
            }

            sb.append("🎯 பரிந்துரைக்கப்பட்ட உத்தி:\n");
            sb.append("  ").append(da.tradeOffRationale()).append("\n\n");

            sb.append("🛡️ இடர் மேலாண்மை பாதுகாப்பு வழிகாட்டுதல்கள்:\n");
            for (String step : da.riskMitigationSteps()) {
                sb.append("  ✔ ").append(step).append("\n");
            }

            sb.append("\n❓ திட்டத்தை மேம்படுத்த தொடர் கேள்விகள்:\n");
            for (String q : da.targetedFollowUpQuestions()) {
                sb.append("  👉 ").append(q).append("\n");
            }

            sb.append("\n📚 ஆதாரங்கள்: ").append(da.evidenceCitations()).append("\n");
            sb.append("⚠️ அறிவிப்பு: வானிலை மற்றும் சந்தை நிலவரங்களுக்கு ஏற்ப விவசாய முடிவுகளை சரிபார்த்து எடுக்கவும்.");
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌾 कृषि निर्णय सहयोग सहायक (Decision Support Assistant)\n");
            sb.append(String.format("- विश्लेषण उद्देश्य: %s\n", da.objective()));
            sb.append(String.format("- संसाधन सीमाएं: %s\n\n", da.constraintsSummary()));

            sb.append("⚖️ व्यावहारिक कृषि विकल्पों की तुलना:\n");
            for (DecisionSupportEngine.FeasibleAlternative fa : da.comparedAlternatives()) {
                sb.append(String.format("🔹 %s\n", fa.optionName()));
                sb.append(String.format("   • रणनीति: %s\n", fa.agronomicStrategy()));
                sb.append(String.format("   • लागत प्रति एकड़: ₹ %.2f | जल आवश्यकता: %s | श्रम: %s\n",
                        fa.estimatedCostPerAcre(), fa.waterRequirement(), fa.laborIntensity()));
                sb.append(String.format("   • जलवायु अनुकूलता: %s | मूल्य जोखिम: %s\n\n",
                        fa.climateResilience(), fa.marketAndPriceRisk()));
            }

            sb.append("🎯 अनुशंसित निर्णय:\n");
            sb.append("  ").append(da.tradeOffRationale()).append("\n\n");

            sb.append("🛡️ जोखिम प्रबंधन एवं सुरक्षा कदम:\n");
            for (String step : da.riskMitigationSteps()) {
                sb.append("  ✔ ").append(step).append("\n");
            }

            sb.append("\n❓ योजना को बेहतर बनाने हेतु अनुवर्ती प्रश्न:\n");
            for (String q : da.targetedFollowUpQuestions()) {
                sb.append("  👉 ").append(q).append("\n");
            }

            sb.append("\n📚 संदर्भ: ").append(da.evidenceCitations()).append("\n");
            sb.append("⚠️ नोट: प्राकृतिक मौसम एवं बाजार परिस्थितियों के अनुसार निर्णय लें।");
            return sb.toString();
        }

        // Default English
        StringBuilder fallback = new StringBuilder();
        fallback.append("🌾 RISK-AWARE AGRICULTURAL DECISION ASSISTANT\n");
        fallback.append(String.format("- Analysis Objective: %s\n", da.objective()));
        fallback.append(String.format("- Resource Constraints: %s\n\n", da.constraintsSummary()));

        fallback.append("⚖️ COMPARISON OF FEASIBLE AGRONOMIC ALTERNATIVES:\n");
        for (DecisionSupportEngine.FeasibleAlternative fa : da.comparedAlternatives()) {
            fallback.append(String.format("🔹 %s\n", fa.optionName()));
            fallback.append(String.format("   • Strategy: %s\n", fa.agronomicStrategy()));
            fallback.append(String.format("   • Cost per Acre: Rs %.2f | Water: %s | Labor: %s\n",
                    fa.estimatedCostPerAcre(), fa.waterRequirement(), fa.laborIntensity()));
            fallback.append(String.format("   • Climate Resilience: %s | Price Risk: %s\n\n",
                    fa.climateResilience(), fa.marketAndPriceRisk()));
        }

        fallback.append("🎯 RECOMMENDED DECISION:\n");
        fallback.append("  ").append(da.tradeOffRationale()).append("\n\n");

        fallback.append("🛡️ RISK MITIGATION PROTOCOLS:\n");
        for (String step : da.riskMitigationSteps()) {
            fallback.append("  ✔ ").append(step).append("\n");
        }

        fallback.append("\n❓ TARGETED FOLLOW-UP QUESTIONS TO REFINE PLAN:\n");
        for (String q : da.targetedFollowUpQuestions()) {
            fallback.append("  👉 ").append(q).append("\n");
        }

        fallback.append("\n📚 Citations: ").append(da.evidenceCitations()).append("\n");
        fallback.append("⚠️ ").append(da.noGuaranteeDisclaimer()).append("\n");
        return fallback.toString();
    }

    public String formatDocumentRAGAdvisory(List<ContextItem> docItems, String language, String crop) {
        String cropDisplay = crop != null && !crop.isBlank() ? crop.toUpperCase() : "AGRI";
        if ("ta".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌾 உள்ளூர் வேளாண் ஆவணங்களிலிருந்து பெறப்பட்ட தீர்வுகள் (Local Knowledge Base RAG)\n");
            sb.append(String.format("- பயிர்: %s | தொடர்புடைய ஆவணப் பகுதிகள்: %d\n\n", cropDisplay, docItems.size()));

            for (int i = 0; i < docItems.size(); i++) {
                ContextItem item = docItems.get(i);
                sb.append(String.format("📄 [%d] %s:\n", i + 1, item.getSourceName()));
                sb.append(item.getText()).append("\n\n");
            }
            sb.append("💡 வேளாண் வழிகாட்டுதல்: ஆவணங்களில் குறிப்பிடப்பட்டுள்ள அங்கீகரிக்கப்பட்ட விதையளவு, உரம் மற்றும் பாதுகாப்பு முறைகளைப் பின்பற்றவும்.\n");
            sb.append("⚠️ அறிவிப்பு: விரிவான களப் பரிசோதனைக்கு உங்கள் அருகிலுள்ள வேளாண் விரிவாக்க அலுவலர் அல்லது KVK மையத்தை அணுகவும்.");
            return sb.toString();

        } else if ("hi".equalsIgnoreCase(language)) {
            StringBuilder sb = new StringBuilder();
            sb.append("🌾 स्थानीय कृषि ज्ञानकोष फ़ाइलों से प्राप्त समाधान (Local Knowledge Base RAG)\n");
            sb.append(String.format("- फसल: %s | संदर्भित दस्तावेज़ अंश: %d\n\n", cropDisplay, docItems.size()));

            for (int i = 0; i < docItems.size(); i++) {
                ContextItem item = docItems.get(i);
                sb.append(String.format("📄 [%d] %s:\n", i + 1, item.getSourceName()));
                sb.append(item.getText()).append("\n\n");
            }
            sb.append("💡 कृषि वैज्ञानिक परामर्श: स्थानीय फाइलों में उल्लिखित सटीक बीज दर, उर्वरक एवं पौध सुरक्षा उपायों का पालन करें।\n");
            sb.append("⚠️ नोट: अंतिम मैदानी निर्णय हेतु स्थानीय कृषि विज्ञान केंद्र (KVK) के वैज्ञानिकों से संपर्क करें।");
            return sb.toString();
        }

        // Default English
        StringBuilder sb = new StringBuilder();
        sb.append("🌾 VERIFIED ADVISORY FROM LOCAL KNOWLEDGE BASE DOCUMENTS (Local RAG)\n");
        sb.append(String.format("- Crop: %s | Referenced Document Excerpts: %d\n\n", cropDisplay, docItems.size()));
        for (int i = 0; i < docItems.size(); i++) {
            ContextItem item = docItems.get(i);
            sb.append(String.format("📄 [%d] %s:\n", i + 1, item.getSourceName()));
            sb.append(item.getText()).append("\n\n");
        }
        sb.append("💡 Agronomic Guidance: Adhere strictly to the certified dosages, seed rates, and practices specified in the documents above.\n");
        sb.append("⚠️ Notice: For phytosanitary laboratory certification, consult your local Krishi Vigyan Kendra (KVK).");
        return sb.toString();
    }
}
