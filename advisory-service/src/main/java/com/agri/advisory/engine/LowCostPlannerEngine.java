package com.agri.advisory.engine;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LowCostPlannerEngine {

    public record CostIntervention(
            String practiceCategory,
            String conventionalPractice,
            double conventionalCostPerAcre,
            String lowCostAlternative,
            double alternativeCostPerAcre,
            double netSavingsPerAcre,
            String agronomicBasis,
            String safetyBoundary
    ) {}

    public record LowCostPlanResult(
            String crop,
            double acres,
            double totalConventionalCost,
            double totalOptimizedCost,
            double totalSavingsInr,
            double savingsPercentage,
            List<CostIntervention> interventions,
            List<String> keyActionItems,
            String complianceNotice
    ) {}

    public LowCostPlanResult planLowCost(String cropInput, Double acresInput) {
        String crop = cropInput != null ? cropInput.trim().toLowerCase() : "paddy";
        double acres = (acresInput != null && acresInput > 0.0) ? acresInput : 1.0;

        List<CostIntervention> items = new ArrayList<>();

        if (crop.contains("rice") || crop.contains("paddy")) {
            items.add(new CostIntervention(
                    "Nutrient Management",
                    "Blanket application of Urea (100 kg) + DAP (50 kg) + MOP (35 kg)",
                    4200.0,
                    "Soil-Test Crop Response (STCR) targeted dosage + Green Manuring (Dhaincha)",
                    2800.0,
                    1400.0,
                    "TNAU STCR trials prove blanket over-application leads to 25-30% nitrogen leaching without yield gains. Green manure fixes 60-80 kg atmospheric N/ha.",
                    "Do not reduce potassium below baseline in zinc-deficient or saline soils."
            ));

            items.add(new CostIntervention(
                    "Water & Energy Management",
                    "Continuous deep submergence irrigation (5-7 cm standing water) with diesel/electric pumps",
                    2200.0,
                    "Alternate Wetting and Drying (AWD) utilizing field water tube (pani pipe)",
                    1400.0,
                    800.0,
                    "IRRI & TNAU validated AWD reduces irrigation pumping frequency by 25-35% with zero yield loss while curbing methane emissions.",
                    "Ensure standing water is maintained during critical flowering/heading stage (10 days)."
            ));

            items.add(new CostIntervention(
                    "Crop Protection & Pest Management",
                    "3 calendar-based prophylactic broad-spectrum insecticide sprays",
                    2800.0,
                    "Pheromone traps (5 traps/acre) + Neem Oil 1500 ppm @ 1.2 L/acre (spray only when ETL reached)",
                    1200.0,
                    1600.0,
                    "Routine calendar spraying kills beneficial predatory spiders and mirid bugs. Pheromone monitoring initiates sprays only when stem borer moth catch exceeds Economic Threshold Level (ETL: 1 moth/trap/day).",
                    "If yellow stem borer damage exceeds 10% dead hearts, apply targeted CIBRC-registered chemical."
            ));

            items.add(new CostIntervention(
                    "Establishment & Nursery",
                    "Manual nursery uprooting and puddled manual transplanting (20-25 laborers/acre)",
                    5500.0,
                    "Modified Mat Nursery + Walk-behind Mechanical Transplanter or Direct Seeded Rice (DSR)",
                    3300.0,
                    2200.0,
                    "Custom hiring mechanical transplanter reduces nursery labor by 40% and ensures optimal hill spacing (20 x 10 cm) with 2-3 seedlings/hill.",
                    "Maintain precise land leveling (laser leveller) for successful direct seeding."
            ));
        } else if (crop.contains("tomato") || crop.contains("vegetable") || crop.contains("chilli")) {
            items.add(new CostIntervention(
                    "Fertigation & Nutrient",
                    "Manual soil broadcasting of complex fertilizers (10:26:26 & Urea)",
                    6500.0,
                    "Soil-test based split fertigation + Farmyard Manure & Jeevamrutha foliar spray",
                    4200.0,
                    2300.0,
                    "Broadcasting fertilizers leads to 40% volatilization and weed uptake. Split fertigation increases nutrient absorption efficiency to >70%.",
                    "Do not substitute micronutrient (Boron/Calcium) sprays if blossom end rot occurs."
            ));

            items.add(new CostIntervention(
                    "Crop Protection",
                    "4-5 intensive chemical insecticide/fungicide sprays",
                    5200.0,
                    "Yellow & Blue sticky traps (25/acre) + Neem Oil 1500ppm + Trichoderma viride drenching",
                    2200.0,
                    3000.0,
                    "Traps reduce adult whitefly and thrips vector populations before viral transmission occurs.",
                    "If fruit borer perforation exceeds 5% of fruits, use CIBRC-approved registered larvicide immediately."
            ));

            items.add(new CostIntervention(
                    "Weed Management",
                    "3 manual hand weedings (15 laborers/acre)",
                    4500.0,
                    "Silver-black reflective plastic mulch film (25-30 micron) with drip lateral",
                    2800.0,
                    1700.0,
                    "Mulching suppresses 95% weed growth, conserves 40% soil moisture, and repels whiteflies via reflection.",
                    "Dispose of or recycle mulch film properly post-harvest."
            ));
        } else {
            // Default General Crop (Maize / Cotton / Pulses)
            items.add(new CostIntervention(
                    "Nutrient Optimization",
                    "Conventional unverified complex fertilizer purchase",
                    4500.0,
                    "Soil health card calibrated NPK + Rhizobium/Azospirillum bio-fertilizer seed inoculation",
                    2900.0,
                    1600.0,
                    "Seed treatment with biofertilizers (Rs 150/acre) supplements 20-25 kg N and solubilizes native soil phosphorus.",
                    "Follow State Agriculture Department Soil Health Card recommendations."
            ));

            items.add(new CostIntervention(
                    "Integrated Pest Management",
                    "Uncalibrated retail pesticide cocktail spray",
                    3200.0,
                    "Cultural sanitation + Light/Pheromone traps + Botanical neem spray",
                    1400.0,
                    1800.0,
                    "Non-chemical controls protect pollinator colonies and delay insecticide resistance.",
                    "Always wear protective gloves and mask when preparing any agricultural mixture."
            ));
        }

        double totalConv = items.stream().mapToDouble(CostIntervention::conventionalCostPerAcre).sum() * acres;
        double totalOpt = items.stream().mapToDouble(CostIntervention::alternativeCostPerAcre).sum() * acres;
        double totalSav = totalConv - totalOpt;
        double savingsPct = totalConv > 0.0 ? (totalSav / totalConv) * 100.0 : 0.0;

        List<String> actions = List.of(
                "Collect and test your field soil sample at the nearest Block Agricultural Extension Center (SHC cost: Rs 20-50).",
                "Substitute routine chemical sprays with Neem Oil 1500 ppm and sticky traps for early-season pests.",
                "Adopt water-conserving irrigation (AWD in rice, drip in row crops) to cut electricity and pumping bills.",
                "Incorporate green manure (Sunn hemp / Dhaincha) before main crop to generate 4-6 tonnes of organic biomass/acre."
        );

        String notice = "ASSUMPTION DISCLOSURE: All cost savings are calculated deterministically using ICAR/TNAU agronomic field cost norms. " +
                "Savings represent operational reductions against uncalibrated conventional farm management. Never reduce pesticide dosage below mandatory CIBRC label minimums.";

        return new LowCostPlanResult(
                crop.toUpperCase(), acres, round(totalConv), round(totalOpt),
                round(totalSav), round(savingsPct), items, actions, notice
        );
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
