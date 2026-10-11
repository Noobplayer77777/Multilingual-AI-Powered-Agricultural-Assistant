package com.agri.advisory.engine;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PesticideSafetyEngine {

    public record PesticideLabel(
            String name,
            String formulation,
            String cibrcNumber,
            List<String> registeredCrops,
            List<String> registeredPests,
            double dosePerAcre,
            String unit, // "g" or "ml"
            double waterLitersPerAcre,
            int phiDays, // Pre-Harvest Interval
            int reiHours, // Re-entry Interval
            String toxicityBand,
            String ppeRequired,
            String safetyCautions,
            String ipmAlternative
    ) {}

    public record SafetyEvaluation(
            boolean matched,
            boolean approved,
            String pesticideName,
            String targetCrop,
            double requestedAcres,
            Double totalChemicalAmount,
            String unit,
            Double totalWaterLiters,
            Integer knapsackTanksNeeded,
            Double dosePer16LTank,
            Integer phiDays,
            Integer reiHours,
            String toxicityBand,
            String ppe,
            String cautions,
            String ipmAlternative,
            String warningNotice,
            String detailedAdvisory
    ) {}

    private final List<PesticideLabel> registry = new ArrayList<>();

    public PesticideSafetyEngine() {
        initRegistry();
    }

    private void initRegistry() {
        registry.add(new PesticideLabel(
                "Mancozeb 75% WP",
                "75% Wettable Powder (WP)",
                "CIBRC/CIR-12845/89/Mancozeb(WP)-124",
                List.of("rice", "paddy", "tomato", "potato", "groundnut", "chilli"),
                List.of("blast", "early blight", "late blight", "leaf spot", "tikka leaf spot", "damping off"),
                700.0,
                "g",
                200.0,
                15,
                24,
                "Class III - Slightly Toxic (Blue Triangle)",
                "Nitrile gloves, safety goggles/face shield, dust mask (N95/FFP2), rubber boots.",
                "Toxic to fish and aquatic invertebrates. Maintain a 30m buffer zone from waterways. Do not spray during mid-day heat or wind > 10 km/h.",
                "Seed treatment with Trichoderma viride @ 4 g/kg seed or Pseudomonas fluorescens @ 10 g/kg seed; crop rotation with non-host crops."
        ));

        registry.add(new PesticideLabel(
                "Chlorantraniliprole 18.5% SC",
                "18.5% Suspension Concentrate (SC)",
                "CIBRC/CIR-58932/2008/Chlorantraniliprole(SC)-341",
                List.of("rice", "paddy", "sugarcane", "tomato", "cotton", "cabbage", "chilli"),
                List.of("stem borer", "leaf folder", "fruit borer", "diamond back moth", "early shoot borer"),
                60.0,
                "ml",
                200.0,
                14,
                24,
                "Class IV - Low Hazard (Green Triangle)",
                "Protective chemical gloves, long-sleeved shirt, long pants, eye protection.",
                "Safe for honeybees once spray deposits are dry, but do NOT spray directly onto flowering crops during active bee foraging hours (10 AM - 4 PM).",
                "Pheromone traps @ 5 traps/acre for stem borer monitoring; release Trichogramma japonicum parasitoids @ 1,00,000/ha."
        ));

        registry.add(new PesticideLabel(
                "Cartap Hydrochloride 50% SP",
                "50% Soluble Powder (SP)",
                "CIBRC/CIR-24310/96/Cartap(SP)-19",
                List.of("rice", "paddy"),
                List.of("stem borer", "leaf folder", "whorl maggot"),
                400.0,
                "g",
                200.0,
                15,
                24,
                "Class II - Moderately Toxic (Yellow Triangle)",
                "Chemical-resistant apron, nitrile gloves, certified vapor respirator, rubber boots.",
                "Highly toxic to aquatic life, bees, and silkworms. Strictly prohibited in integrated paddy-cum-fish farming zones.",
                "Erect bird perches @ 20 perches/acre; Alternate Wetting and Drying (AWD) reduces whorl maggot oviposition."
        ));

        registry.add(new PesticideLabel(
                "Emamectin Benzoate 5% SG",
                "5% Soluble Granules (SG)",
                "CIBRC/CIR-41029/2003/Emamectin(SG)-88",
                List.of("cotton", "tomato", "chilli", "cabbage", "cauliflower", "okra", "brinjal"),
                List.of("fruit borer", "bollworm", "diamond back moth", "pod borer", "thrips"),
                80.0,
                "g",
                200.0,
                3,
                12,
                "Class III - Slightly Toxic (Blue Triangle)",
                "Nitrile gloves, safety glasses, particulate mask, protective coverall.",
                "Highly toxic to bees on direct contact. Spray strictly at dusk/sunset when pollinators have ceased activity. Keep animals away for 12 hours.",
                "Pheromone traps @ 8-10 traps/acre; spray Bacillus thuringiensis (Bt) kurstaki @ 2 g/L during first-instar caterpillar emergence."
        ));

        registry.add(new PesticideLabel(
                "Copper Oxychloride 50% WP",
                "50% Wettable Powder (WP)",
                "CIBRC/CIR-3211/78/Copper Oxychloride(WP)-41",
                List.of("tomato", "potato", "citrus", "banana", "coffee", "grapes"),
                List.of("bacterial leaf spot", "early blight", "canker", "downy mildew", "sigatoka"),
                1000.0,
                "g",
                250.0,
                10,
                24,
                "Class III - Slightly Toxic (Blue Triangle)",
                "Face shield, heavy-duty rubber gloves, dust respirator, rubber boots.",
                "Do NOT mix with acidic pesticides or alkaline foliar fertilizers (e.g. Bordeaux mixture). Phyto-toxic risk if applied during damp overcast stagnation.",
                "Spray Pseudomonas fluorescens @ 5 g/L as a preventive bio-bactericide; avoid overhead sprinkler irrigation."
        ));

        registry.add(new PesticideLabel(
                "Neem Oil / Azadirachtin 1500 ppm",
                "0.15% Emulsifiable Concentrate (EC)",
                "CIBRC Certified Bio-Botanical / Organic Grade",
                List.of("rice", "paddy", "tomato", "cotton", "chilli", "vegetables", "pulses", "maize", "groundnut"),
                List.of("whitefly", "aphids", "thrips", "jassids", "leaf miner", "early caterpillars"),
                1200.0,
                "ml",
                200.0,
                0,
                4,
                "Non-Toxic (Bio-Botanical, Green Category)",
                "Standard work gloves, eye glasses, clean cotton clothing.",
                "Safe for non-target organisms and natural predators (ladybird beetles, chrysoperla). Mix with 1 ml/L mild soap or surfactant for uniform emulsification.",
                "Erect yellow sticky traps (15 traps/acre) for whiteflies/aphids; blue sticky traps for thrips."
        ));
    }

    public SafetyEvaluation evaluate(String cropInput, String textInput, Double requestedAcres) {
        String crop = cropInput != null ? cropInput.trim().toLowerCase() : "";
        String text = textInput != null ? textInput.trim().toLowerCase() : "";
        double acres = (requestedAcres != null && requestedAcres > 0.0) ? requestedAcres : 1.0;

        PesticideLabel matchedLabel = findMatchedPesticide(text);

        if (matchedLabel == null) {
            // Check if user is asking for general pest control for a crop
            matchedLabel = findDefaultLabelForCropPest(crop, text);
            if (matchedLabel == null) {
                return new SafetyEvaluation(
                        false, false, null, crop, acres, null, null, null, null, null,
                        null, null, null, null, null, null, null,
                        "No specific registered pesticide detected. Please consult CIBRC label or specify chemical name for verification."
                );
            }
        }

        // Validate whether crop is registered
        boolean isCropApproved = isCropRegistered(matchedLabel, crop);

        if (!isCropApproved && !crop.isEmpty()) {
            // STRICT REJECTION: Off-label application attempt detected!
            String warning = String.format(
                    "CRITICAL SAFETY REFUSAL: %s is NOT officially approved or registered by CIBRC / TNAU for use on '%s'. " +
                    "Off-label usage is a statutory violation of the Insecticides Act, 1968, risks illegal pesticide residues exceeding MRLs (Maximum Residue Limits), " +
                    "can cause crop phytotoxicity, and can cause rejection in domestic and export markets.",
                    matchedLabel.name(), cropInput
            );

            String advisory = String.format(
                    "❌ %s\n\n" +
                    "- Chemical: %s\n" +
                    "- Requested Crop: %s (NOT REGISTERED)\n" +
                    "- Officially Approved Crops for %s: %s\n\n" +
                    "RECOMMENDED SAFE ACTION:\n" +
                    "1. Do NOT spray %s on %s.\n" +
                    "2. Recommended Eco-Friendly IPM alternative: %s\n" +
                    "3. For approved chemical solutions, consult your local Block Assistant Director of Agriculture (ADA) or Krishi Vigyan Kendra (KVK).",
                    warning,
                    matchedLabel.name(), cropInput, matchedLabel.name(),
                    String.join(", ", matchedLabel.registeredCrops()),
                    matchedLabel.name(), cropInput, matchedLabel.ipmAlternative()
            );

            return new SafetyEvaluation(
                    true, false, matchedLabel.name(), crop, acres, null, matchedLabel.unit(),
                    null, null, null, null, null, matchedLabel.toxicityBand(),
                    matchedLabel.ppeRequired(), matchedLabel.safetyCautions(),
                    matchedLabel.ipmAlternative(), warning, advisory
            );
        }

        // Approved calculation
        double totalChemical = matchedLabel.dosePerAcre() * acres;
        double totalWater = matchedLabel.waterLitersPerAcre() * acres;
        int tanks = (int) Math.ceil(totalWater / 16.0); // 16L knapsack sprayer
        double dosePerTank = totalChemical / tanks;

        String formattedAdvisory = buildApprovedAdvisory(matchedLabel, crop, acres, totalChemical, totalWater, tanks, dosePerTank);

        return new SafetyEvaluation(
                true, true, matchedLabel.name(), crop, acres,
                round(totalChemical, 1), matchedLabel.unit(),
                round(totalWater, 1), tanks, round(dosePerTank, 2),
                matchedLabel.phiDays(), matchedLabel.reiHours(),
                matchedLabel.toxicityBand(), matchedLabel.ppeRequired(),
                matchedLabel.safetyCautions(), matchedLabel.ipmAlternative(),
                null, formattedAdvisory
        );
    }

    private PesticideLabel findMatchedPesticide(String text) {
        for (PesticideLabel label : registry) {
            String labelLower = label.name().toLowerCase();
            String activeLower = label.name().split(" ")[0].toLowerCase();
            if (text.contains(labelLower) || text.contains(activeLower)) {
                return label;
            }
        }
        if (text.contains("coragen")) {
            return registry.stream().filter(l -> l.name().startsWith("Chlorantraniliprole")).findFirst().orElse(null);
        }
        if (text.contains("dithane") || text.contains("m-45")) {
            return registry.stream().filter(l -> l.name().startsWith("Mancozeb")).findFirst().orElse(null);
        }
        if (text.contains("cartap") || text.contains("padan")) {
            return registry.stream().filter(l -> l.name().startsWith("Cartap")).findFirst().orElse(null);
        }
        if (text.contains("proclaim") || text.contains("emamectin")) {
            return registry.stream().filter(l -> l.name().startsWith("Emamectin")).findFirst().orElse(null);
        }
        if (text.contains("blitox") || text.contains("copper oxychloride")) {
            return registry.stream().filter(l -> l.name().startsWith("Copper Oxychloride")).findFirst().orElse(null);
        }
        if (text.contains("neem") || text.contains("azadirachtin")) {
            return registry.stream().filter(l -> l.name().startsWith("Neem Oil")).findFirst().orElse(null);
        }
        return null;
    }

    private PesticideLabel findDefaultLabelForCropPest(String crop, String text) {
        if (text.contains("stem borer") || text.contains("leaf folder")) {
            return registry.stream().filter(l -> l.name().startsWith("Chlorantraniliprole")).findFirst().orElse(null);
        }
        if (text.contains("blast") || text.contains("blight") || text.contains("leaf spot")) {
            return registry.stream().filter(l -> l.name().startsWith("Mancozeb")).findFirst().orElse(null);
        }
        if (text.contains("fruit borer") || text.contains("bollworm") || text.contains("caterpillar")) {
            return registry.stream().filter(l -> l.name().startsWith("Emamectin")).findFirst().orElse(null);
        }
        if (text.contains("whitefly") || text.contains("aphid") || text.contains("thrip") || text.contains("organic spray")) {
            return registry.stream().filter(l -> l.name().startsWith("Neem Oil")).findFirst().orElse(null);
        }
        return null;
    }

    private boolean isCropRegistered(PesticideLabel label, String crop) {
        if (crop.isEmpty()) return true;
        for (String rc : label.registeredCrops()) {
            if (crop.contains(rc) || rc.contains(crop)) {
                return true;
            }
        }
        return false;
    }

    private String buildApprovedAdvisory(PesticideLabel label, String crop, double acres,
                                         double totalChemical, double totalWater, int tanks, double dosePerTank) {
        return String.format("""
                🌿 CIBRC / TNAU VERIFIED PESTICIDE DOSAGE & SAFETY ADVISORY
                - Target Crop: %s | Farm Area: %.2f acres
                - Chemical: %s [%s]
                - CIBRC Registration: %s
                - Toxicity Category: %s
                
                📏 DETERMINISTIC DOSAGE CALCULATIONS:
                - Recommended Field Dose: %.1f %s in %.0f Litres water per acre
                - Total Chemical Required: %.1f %s (for %.2f acres)
                - Total Water Required: %.0f Litres
                - Knapsack Tank Calibration (16L tank): %d full tanks
                - Concentration per 16L Tank: %.2f %s / tank
                
                ⚠️ SAFETY, HARVEST & RE-ENTRY INTERVALS:
                - Pre-Harvest Interval (PHI): %d days (Do NOT harvest within %d days of spraying!)
                - Re-Entry Interval (REI): %d hours (Do NOT enter treated field without PPE for %d hours)
                - Mandatory PPE: %s
                - Environmental & Pollinator Warnings: %s
                
                🌱 NON-CHEMICAL / INTEGRATED PEST MANAGEMENT (IPM) FIRST-LINE:
                - %s
                """,
                crop.toUpperCase(), acres, label.name(), label.formulation(), label.cibrcNumber(), label.toxicityBand(),
                label.dosePerAcre(), label.unit(), label.waterLitersPerAcre(),
                totalChemical, label.unit(), acres, totalWater,
                tanks, dosePerTank, label.unit(),
                label.phiDays(), label.phiDays(),
                label.reiHours(), label.reiHours(),
                label.ppeRequired(), label.safetyCautions(),
                label.ipmAlternative()
        );
    }

    private double round(double val, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(val * factor) / factor;
    }

    public List<PesticideLabel> getRegistry() {
        return Collections.unmodifiableList(registry);
    }
}
