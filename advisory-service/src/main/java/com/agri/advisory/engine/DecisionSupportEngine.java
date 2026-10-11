package com.agri.advisory.engine;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DecisionSupportEngine {

    public record FeasibleAlternative(
            String optionName,
            String agronomicStrategy,
            double estimatedCostPerAcre,
            String waterRequirement,
            String laborIntensity,
            String marketAndPriceRisk,
            String climateResilience,
            List<String> keyBenefits,
            List<String> primaryRisks
    ) {}

    public record DecisionAnalysis(
            String objective,
            String constraintsSummary,
            List<FeasibleAlternative> comparedAlternatives,
            String recommendedOption,
            String tradeOffRationale,
            List<String> riskMitigationSteps,
            List<String> targetedFollowUpQuestions,
            String evidenceCitations,
            String noGuaranteeDisclaimer
    ) {}

    public DecisionAnalysis analyzeDecision(String cropInput, String locationInput, String seasonInput,
                                            Double acresInput, Double budgetInput, String queryInput) {
        String crop = (cropInput != null && !cropInput.trim().isEmpty()) ? cropInput.trim() : "Paddy";
        String location = (locationInput != null && !locationInput.trim().isEmpty()) ? locationInput.trim() : "Field Location";
        String season = (seasonInput != null && !seasonInput.trim().isEmpty()) ? seasonInput.trim() : "Current Season";
        double acres = (acresInput != null && acresInput > 0.0) ? acresInput : 2.0;
        double budget = (budgetInput != null && budgetInput > 0.0) ? budgetInput : 40000.0;

        String objective = String.format("Optimize net financial return while minimizing climatic & pest risks for %.2f acres of %s in %s (%s).",
                acres, crop, location, season);

        String constraints = String.format("Available budget: Rs %.2f (Rs %.2f/acre) | Cultivable area: %.2f acres | Agro-climatic zone: %s.",
                budget, budget / acres, acres, location);

        List<FeasibleAlternative> alternatives = new ArrayList<>();

        if (crop.equalsIgnoreCase("paddy") || crop.equalsIgnoreCase("rice")) {
            alternatives.add(new FeasibleAlternative(
                    "Option A: High-Yielding Variety / Hybrid Paddy (Transplanted)",
                    "Short-to-medium duration high-yielding variety (e.g. CO 51, ADT 53) with standard nursery transplanting and complex NPK management.",
                    25000.0,
                    "High (1100 - 1250 mm total seasonal water; continuous submergence or frequent irrigation)",
                    "High (25-30 person-days per acre for manual transplanting, weeding, harvesting)",
                    "Moderate (Supported by Government Minimum Support Price [MSP] at designated procurement centers)",
                    "Moderate-to-Low (Vulnerable to late-season dry spells and terminal water shortage)",
                    List.of("Maximum yield potential under assured irrigation", "Government MSP price safety net", "Established local grain market and milling demand"),
                    List.of("High upfront capital and labor intensity", "Severe yield loss if canal or borewell supply fails during flowering", "Susceptible to blast under humid overcast weather")
            ));

            alternatives.add(new FeasibleAlternative(
                    "Option B: Climate-Resilient Direct Seeded Rice (DSR) or Pulse Crop Transition (Blackgram/Greengram)",
                    "Direct seeding using seed drill with pre-emergence herbicide, or rotation with pulses requiring 1/3rd water.",
                    14500.0,
                    "Low-to-Moderate (600 - 750 mm water; 35-40% savings compared to puddled rice)",
                    "Low-to-Moderate (eliminates nursery labor; 12-15 person-days per acre)",
                    "Moderate (Pulse prices fluctuate but provide quick 70-75 day cash realization)",
                    "High (Tolerates moderate moisture stress; deep taproot system in pulses improves soil nitrogen)",
                    List.of("Rs 10,500/acre lower operational investment", "Leaves soil enriched with 30-40 kg residual nitrogen for next crop", "Protects against sudden borewell yield drops"),
                    List.of("Requires strict early weed management (0-20 days post-seeding)", "Lower peak gross tonnage compared to intensive hybrid rice")
            ));
        } else {
            alternatives.add(new FeasibleAlternative(
                    "Option A: Intensive Commercial Cultivation (" + crop + ")",
                    "Certified commercial hybrid seed with recommended chemical NPK and preventive plant protection schedule.",
                    32000.0,
                    "Moderate to High",
                    "High",
                    "High (Subject to daily mandi auction price volatility)",
                    "Moderate",
                    List.of("High market revenue potential during peak festival/seasonal demand", "High grade produce"),
                    List.of("High input expenditure can deplete budget buffer", "Perishable produce risk if local mandi is flooded")
            ));

            alternatives.add(new FeasibleAlternative(
                    "Option B: Integrated Low-Input Diversified Cultivation (" + crop + " + Intercrop)",
                    "Standard variety combined with 1:4 legume intercrop, soil-test nutrition, and organic IPM.",
                    19000.0,
                    "Low to Moderate",
                    "Moderate",
                    "Moderate-to-Low (Diversified income risk across two separate commodities)",
                    "High (Intercrop canopy buffers soil evaporation and reduces erosion)",
                    List.of("40% lower capital requirement", "Dual harvest reduces complete crop failure risk", "Reduced pest flare-up via natural predator conservation"),
                    List.of("Requires manual selective harvesting for mixed crops")
            ));
        }

        String recommended;
        String tradeOff;
        double costPerAcreOptA = alternatives.get(0).estimatedCostPerAcre();
        if (budget / acres < costPerAcreOptA) {
            recommended = "Option B (Climate-Resilient / Low-Input Strategy)";
            tradeOff = String.format("Given your available budget of Rs %.2f/acre (below Option A's Rs %.2f/acre benchmark), " +
                    "Option B guarantees complete operational completion without debt distress, while reducing climate and water vulnerability.",
                    budget / acres, costPerAcreOptA);
        } else {
            recommended = "Option A with Option B Risk-Mitigation Protocols";
            tradeOff = "Your budget allows Option A's higher yield potential, but adopting Option B's soil-test fertilization and water management will preserve an essential 20-30% cash buffer.";
        }

        List<String> mitigations = List.of(
                "Enroll in Pradhan Mantri Fasal Bima Yojana (PMFBY) before the seasonal cut-off deadline to insure invested capital against natural calamities.",
                "Conduct field soil testing (SHC) prior to basal fertilizer application to prevent unneeded complex fertilizer expenditure.",
                "Erect pheromone traps at 5 traps/acre to scout insect pests before committing to expensive chemical sprays.",
                "Set aside 10% of total working capital for harvest and emergency pest intervention."
        );

        List<String> followUps = List.of(
                "What is your confirmed irrigation source (perennial borewell, seasonal canal, or rainfed tank)?",
                "Do you have confirmed family labour, or will all operations depend strictly on hired daily-wage labour?",
                "What crop was grown in this specific plot during the previous season?"
        );

        String citations = "Evidence grounded in TNAU Crop Production Guide (2025), ICAR Agronomic Package of Practices, and IMD Agromet Advisory Bulletins.";

        String disclaimer = "AGRONOMIC DISCLAIMER: All financial figures and duration metrics are deterministic estimates derived from institutional field research. " +
                "Agricultural yields and commodity prices vary according to local weather, soil dynamics, and market conditions. No yield, profit, or weather guarantees are made.";

        return new DecisionAnalysis(
                objective, constraints, alternatives, recommended, tradeOff,
                mitigations, followUps, citations, disclaimer
        );
    }
}
