package com.agri.advisory.engine;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CropProblemTriageEngine {

    public record DiagnosticPossibility(
            String probableCause,
            String category, // "FUNGAL_PATHOGEN", "INSECT_PEST", "NUTRIENT_DEFICIENCY", "VIRAL_DISEASE"
            int confidencePercentage,
            String distinguishingSigns,
            String initialAction
    ) {}

    public record TriageReport(
            String crop,
            String reportedSymptoms,
            List<DiagnosticPossibility> rankedPossibilities,
            List<String> targetedFollowUpQuestions,
            String uncertaintyDisclaimer,
            List<String> recommendedNextSteps
    ) {}

    public TriageReport triageProblem(String cropInput, String symptomInput) {
        String crop = cropInput != null ? cropInput.trim().toLowerCase() : "crop";
        String symptoms = symptomInput != null ? symptomInput.trim().toLowerCase() : "";

        List<DiagnosticPossibility> possibilities = new ArrayList<>();
        List<String> followUps = new ArrayList<>();

        if (symptoms.contains("yellow") || symptoms.contains("chlorosis")) {
            if (symptoms.contains("lower") || symptoms.contains("older") || symptoms.contains("bottom")) {
                possibilities.add(new DiagnosticPossibility(
                        "Nitrogen (N) Deficiency",
                        "NUTRIENT_DEFICIENCY",
                        75,
                        "Uniform yellowing starting from older leaves at the bottom, progressing into a characteristic V-shape pattern towards the leaf tip.",
                        "Apply top-dressing of Urea @ 25-30 kg/acre (or spray 1-2% Urea foliar solution) if soil test indicates low available nitrogen."
                ));
            } else if (symptoms.contains("young") || symptoms.contains("top") || symptoms.contains("new")) {
                possibilities.add(new DiagnosticPossibility(
                        "Iron (Fe) or Micronutrient Deficiency",
                        "NUTRIENT_DEFICIENCY",
                        70,
                        "Interveinal chlorosis appearing specifically on newly emerged young leaves while leaf veins remain distinctly green.",
                        "Foliar spray of Ferrous Sulphate (FeSO4) @ 0.5% (5 g/L) + Citric Acid (1 g/L) during morning hours."
                ));
            } else {
                possibilities.add(new DiagnosticPossibility(
                        "Nutrient Deficiency (Nitrogen or Zinc) vs Moisture Stress",
                        "NUTRIENT_DEFICIENCY",
                        55,
                        "Check whether yellowing is uniform across older leaves (Nitrogen) or exhibiting brown necrotic blotches (Zinc deficiency).",
                        "Check root health for waterlogging; send soil/leaf sample for nutrient analysis."
                ));
                possibilities.add(new DiagnosticPossibility(
                        "Early Sucking Pest Infestation (Thrips / Jassids / Mites)",
                        "INSECT_PEST",
                        50,
                        "Silvering, stippling, or downward cupping accompanying the leaf discoloration on the lower leaf surface.",
                        "Spray Neem Oil 1500 ppm @ 1.2 L/acre + install yellow sticky traps."
                ));
            }
            followUps.add("Are the yellow leaves appearing at the bottom (older leaves) or at the top of the canopy (new tender leaves)?");
            followUps.add("Do you observe any brown spots, concentric rings, or webbing on the underside of the leaves?");
        } else if (symptoms.contains("wilt") || symptoms.contains("drying") || symptoms.contains("droop")) {
            possibilities.add(new DiagnosticPossibility(
                    "Bacterial Wilt (Ralstonia) or Fusarium Wilt",
                    "FUNGAL_PATHOGEN",
                    65,
                    "Vascular browning when the lower stem is slit open. Rapid daytime wilting even in moist soil.",
                    "Perform glass beaker ooze test: suspend stem cutting in clear water; milky bacterial streaming confirms bacterial wilt."
            ));
            possibilities.add(new DiagnosticPossibility(
                    "Root Grub / Nematode / Collar Rot Injury",
                    "INSECT_PEST",
                    55,
                    "Galled/knotted roots, chewed rootlets, or dark constricting collar rot lesion at soil line.",
                    "Drench root zone with Trichoderma viride @ 5 g/L or registered bio-nematicide."
            ));
            followUps.add("When you cut open the lower stem near the soil line, is the vascular tissue inside brown or discolored?");
            followUps.add("Does wilting occur rapidly during the midday sun and recover overnight, or is the plant permanently dry?");
        } else if (symptoms.contains("hole") || symptoms.contains("borer") || symptoms.contains("caterpillar") || symptoms.contains("chew")) {
            possibilities.add(new DiagnosticPossibility(
                    "Lepidopteran Borer or Defoliator (Stem Borer / Fruit Borer / Leaf Folder)",
                    "INSECT_PEST",
                    80,
                    "Excreta (frass) visible near entry holes, central shoot drying (dead heart in cereals), or folded leaf margins.",
                    "Install pheromone traps @ 5-8 traps/acre; apply CIBRC-approved registered larvicide if damage exceeds ETL."
            ));
            followUps.add("Do you notice fresh sawdust-like excreta (frass) near the holes or folded leaves?");
            followUps.add("What percentage of plants or tillers in the field are showing this symptom?");
        } else {
            // General symptom
            possibilities.add(new DiagnosticPossibility(
                    "Abiotic Stress or Early Foliar Infection",
                    "PATHOGEN_OR_STRESS",
                    50,
                    "Inspect both leaf surfaces with a 10x magnifying hand lens for microscopic fungal fruiting bodies or mites.",
                    "Send clear, high-resolution photographs of healthy vs affected plants to your local KVK."
            ));
            followUps.add("When did the symptoms first appear, and how rapidly are they spreading across the field?");
            followUps.add("What was your most recent fertilizer, pesticide, or irrigation application in this field?");
        }

        String disclaimer = "DIAGNOSTIC UNCERTAINTY NOTICE: Digital visual triage cannot substitute for laboratory phytosanitary examination. " +
                "Confidence ratings indicate probability based on reported symptoms. Confirm diagnosis via the physical tests indicated before chemical application.";

        List<String> nextSteps = List.of(
                "Isolate affected plants if localized to prevent secondary field spread.",
                "Verify distinguishing signs listed above before purchasing or applying any chemical.",
                "Consult the nearest Krishi Vigyan Kendra (KVK) or Assistant Director of Agriculture with a fresh symptomatic plant sample in a sealed plastic bag."
        );

        return new TriageReport(crop.toUpperCase(), symptoms, possibilities, followUps, disclaimer, nextSteps);
    }
}
