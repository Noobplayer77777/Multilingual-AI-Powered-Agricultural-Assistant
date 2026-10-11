package com.agri.advisory.engine;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BudgetPlanningEngine {

    public record CropCostBenchmark(
            String cropName,
            double seedCostPerAcre,
            double landPrepCostPerAcre,
            double fertilizerCostPerAcre,
            double laborCostPerAcre,
            double irrigationCostPerAcre,
            double plantProtectionCostPerAcre,
            double harvestThreshingCostPerAcre,
            double contingencyCostPerAcre,
            int cropDurationDays,
            String sourceReference
    ) {
        public double getTotalCostPerAcre() {
            return seedCostPerAcre + landPrepCostPerAcre + fertilizerCostPerAcre +
                    laborCostPerAcre + irrigationCostPerAcre + plantProtectionCostPerAcre +
                    harvestThreshingCostPerAcre + contingencyCostPerAcre;
        }
    }

    public record BudgetPlanResult(
            String crop,
            double acres,
            double availableBudget,
            double totalRequiredCost,
            double costPerAcre,
            Map<String, Double> costBreakdown,
            boolean isFeasible,
            double surplusOrDeficit,
            int cropDurationDays,
            String feasibilityStatus,
            String budgetWarning,
            List<String> recommendations,
            String calculationNotes
    ) {}

    private final Map<String, CropCostBenchmark> benchmarks = new HashMap<>();

    public BudgetPlanningEngine() {
        initBenchmarks();
    }

    private void initBenchmarks() {
        benchmarks.put("paddy", new CropCostBenchmark(
                "Paddy (Rice - Irrigated)",
                1400.0, 3500.0, 4400.0, 5600.0, 2000.0, 1800.0, 4800.0, 1500.0,
                125, "TNAU Cost of Cultivation Studies (2025-2026)"
        ));
        benchmarks.put("rice", benchmarks.get("paddy"));

        benchmarks.put("maize", new CropCostBenchmark(
                "Maize (Corn)",
                2200.0, 3200.0, 4800.0, 3800.0, 1600.0, 1500.0, 3500.0, 1400.0,
                110, "ICAR-IIMR Comprehensive Agronomic Costing"
        ));

        benchmarks.put("tomato", new CropCostBenchmark(
                "Tomato (Hybrid Staked)",
                4500.0, 4000.0, 6500.0, 11000.0, 3000.0, 4000.0, 6000.0, 3000.0,
                135, "TNAU Horticultural Crop Guidelines"
        ));

        benchmarks.put("cotton", new CropCostBenchmark(
                "Cotton (Bt Hybrid)",
                2400.0, 3600.0, 5000.0, 7500.0, 2000.0, 3500.0, 5000.0, 2000.0,
                160, "CICR Nagpur Production Cost Norms"
        ));

        benchmarks.put("groundnut", new CropCostBenchmark(
                "Groundnut (Peanut)",
                4200.0, 3000.0, 3600.0, 4500.0, 1800.0, 1500.0, 3800.0, 1600.0,
                115, "TNAU Oilseeds Agronomic Norms"
        ));

        benchmarks.put("blackgram", new CropCostBenchmark(
                "Blackgram / Urad (Pulses)",
                1200.0, 2200.0, 1800.0, 2500.0, 800.0, 1200.0, 2200.0, 1100.0,
                75, "ICAR-IIPR Pulses Economic Review"
        ));
        benchmarks.put("pulses", benchmarks.get("blackgram"));
    }

    public BudgetPlanResult calculateBudget(String cropInput, Double acresInput, Double availableBudgetInput) {
        String cropKey = resolveCropKey(cropInput);
        CropCostBenchmark benchmark = benchmarks.getOrDefault(cropKey, benchmarks.get("paddy"));

        double acres = (acresInput != null && acresInput > 0.0) ? acresInput : 1.0;
        double budget = (availableBudgetInput != null && availableBudgetInput > 0.0) ? availableBudgetInput : 0.0;

        double seedTotal = benchmark.seedCostPerAcre() * acres;
        double landPrepTotal = benchmark.landPrepCostPerAcre() * acres;
        double fertTotal = benchmark.fertilizerCostPerAcre() * acres;
        double laborTotal = benchmark.laborCostPerAcre() * acres;
        double irrigTotal = benchmark.irrigationCostPerAcre() * acres;
        double protectionTotal = benchmark.plantProtectionCostPerAcre() * acres;
        double harvestTotal = benchmark.harvestThreshingCostPerAcre() * acres;
        double contingencyTotal = benchmark.contingencyCostPerAcre() * acres;

        double totalRequired = seedTotal + landPrepTotal + fertTotal + laborTotal +
                irrigTotal + protectionTotal + harvestTotal + contingencyTotal;

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Quality Certified Seed", round(seedTotal));
        breakdown.put("Tractor & Land Preparation", round(landPrepTotal));
        breakdown.put("NPK Chemical & Basal Fertilizers", round(fertTotal));
        breakdown.put("Hired & Operational Labour", round(laborTotal));
        breakdown.put("Irrigation & Power/Diesel", round(irrigTotal));
        breakdown.put("Plant Protection / Crop Health", round(protectionTotal));
        breakdown.put("Harvesting & Threshing", round(harvestTotal));
        breakdown.put("Unforeseen Agronomic Contingency (5%)", round(contingencyTotal));

        boolean isFeasible = budget <= 0.0 || budget >= totalRequired;
        double diff = budget > 0.0 ? (budget - totalRequired) : 0.0;

        String status;
        String warning = null;
        List<String> recs = new ArrayList<>();

        if (budget <= 0.0) {
            status = "ESTIMATE_CALCULATED (No farmer budget entered)";
            recs.add("Provide your available capital to verify complete cash-flow sufficiency.");
            recs.add("Apply for Kisan Credit Card (KCC) at 4% subsidized interest to finance operational costs.");
        } else if (isFeasible) {
            status = "FEASIBLE";
            recs.add(String.format("Budget is sufficient with an estimated surplus buffer of Rs %.2f.", diff));
            recs.add("Reserve surplus funds for critical stage-specific inputs (panicle initiation / flowering).");
            recs.add("Adopt soil-test-based fertilization to further optimize expenses.");
        } else {
            status = "BUDGET_DEFICIT";
            warning = String.format("CRITICAL BUDGET DEFICIT: Required cost (Rs %.2f) exceeds available budget (Rs %.2f) by Rs %.2f (%.1f%% deficit).",
                    totalRequired, budget, Math.abs(diff), (Math.abs(diff) / totalRequired) * 100.0);
            recs.add(String.format("Option 1: Scale down target cultivated area to %.2f acres to remain strictly within cash budget.", budget / benchmark.getTotalCostPerAcre()));
            recs.add("Option 2: Apply for institutional crop loan via Kisan Credit Card (KCC) at your local cooperative/bank (interest subvention reduces rate to 4%).");
            recs.add("Option 3: Switch to low-capital pulse crop (Blackgram/Greengram) requiring only Rs 13,000/acre.");
            recs.add("Option 4: Implement Direct Seeded Rice (DSR) or custom hiring to eliminate nursery transplanting labour (saves ~Rs 4,000/acre).");
        }

        String notes = String.format("Source Norms: %s. Crop Duration: %d days. Cost per Acre: Rs %.2f.",
                benchmark.sourceReference(), benchmark.cropDurationDays(), benchmark.getTotalCostPerAcre());

        return new BudgetPlanResult(
                benchmark.cropName(), acres, budget, round(totalRequired),
                round(benchmark.getTotalCostPerAcre()), breakdown, isFeasible,
                round(diff), benchmark.cropDurationDays(), status, warning, recs, notes
        );
    }

    private String resolveCropKey(String crop) {
        if (crop == null) return "paddy";
        String lower = crop.toLowerCase().trim();
        for (String key : benchmarks.keySet()) {
            if (lower.contains(key)) return key;
        }
        return "paddy";
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
