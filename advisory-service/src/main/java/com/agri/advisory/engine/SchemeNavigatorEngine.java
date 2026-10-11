package com.agri.advisory.engine;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SchemeNavigatorEngine {

    public enum EligibilityStatus {
        LIKELY_ELIGIBLE,
        CONDITIONALLY_ELIGIBLE,
        REQUIRES_VERIFICATION,
        INELIGIBLE
    }

    public record SchemeEvaluation(
            String schemeCode,
            String schemeName,
            String sponsoringAuthority,
            EligibilityStatus status,
            String benefitSummary,
            List<String> satisfiedCriteria,
            List<String> pendingOrMissingCriteria,
            List<String> mandatoryDocuments,
            String officialPortalUrl,
            String applicationProcedure
    ) {}

    public record SchemeAssessmentReport(
            double landholdingAcres,
            String landCategory, // Marginal (<2.5 ac), Small (2.5-5 ac), Medium/Large (>5 ac)
            String state,
            String district,
            String crop,
            List<SchemeEvaluation> evaluations,
            String officialDisclaimer
    ) {}

    public SchemeAssessmentReport assessSchemes(Double acresInput, String stateInput, String districtInput, String cropInput) {
        double acres = (acresInput != null && acresInput > 0.0) ? acresInput : 2.0;
        String state = (stateInput != null && !stateInput.trim().isEmpty()) ? stateInput.trim() : "Tamil Nadu";
        String district = (districtInput != null && !districtInput.trim().isEmpty()) ? districtInput.trim() : "Thanjavur";
        String crop = (cropInput != null && !cropInput.trim().isEmpty()) ? cropInput.trim() : "Paddy";

        String landCategory;
        if (acres <= 2.47) {
            landCategory = "Marginal Farmer (<= 1 Hectare / 2.47 Acres)";
        } else if (acres <= 4.94) {
            landCategory = "Small Farmer (1 - 2 Hectares / 2.47 - 4.94 Acres)";
        } else {
            landCategory = "Medium / Large Farmer (> 2 Hectares / 4.94 Acres)";
        }

        List<SchemeEvaluation> evaluations = new ArrayList<>();

        // 1. PM-KISAN
        List<String> pmkisanSat = new ArrayList<>();
        pmkisanSat.add(String.format("Landholding of %.2f acres qualifies under agricultural landholding guidelines.", acres));
        pmkisanSat.add("State and district located within Indian agricultural operational territory.");

        List<String> pmkisanPending = new ArrayList<>();
        pmkisanPending.add("Farmer must not be an institutional landholder or income-tax payer in the preceding assessment year.");
        pmkisanPending.add("Land records (Patta/Chitta/RoR) must be digitally updated with applicant's name before cut-off date.");
        pmkisanPending.add("Bank account must be active, Aadhaar-seeded, and NPCI-enabled for Direct Benefit Transfer (DBT).");

        evaluations.add(new SchemeEvaluation(
                "PM-KISAN",
                "Pradhan Mantri Kisan Samman Nidhi",
                "Ministry of Agriculture & Farmers Welfare, Government of India",
                EligibilityStatus.LIKELY_ELIGIBLE,
                "Direct cash transfer of Rs 6,000 per year delivered in 3 equal four-monthly installments of Rs 2,000 directly into Aadhaar-linked bank account.",
                pmkisanSat,
                pmkisanPending,
                List.of("Aadhaar Card", "Land Ownership Record (Patta/Chitta/Khatauni)", "Bank Passbook with Aadhaar linkage", "Mobile number linked with Aadhaar for OTP/eKYC"),
                "https://pmkisan.gov.in",
                "Self-registration via 'Farmer Corner' on pmkisan.gov.in or through nearest Common Service Centre (CSC) / e-Seva center."
        ));

        // 2. PMFBY (Crop Insurance)
        List<String> pmfbySat = new ArrayList<>();
        pmfbySat.add(String.format("Crop '%s' in district '%s' is eligible under seasonal notified crop schedules.", crop, district));
        pmfbySat.add("Coverage applicable to both loanee and non-loanee cultivating farmers.");

        List<String> pmfbyPending = new ArrayList<>();
        pmfbyPending.add("Application must be submitted before seasonal cut-off deadline (usually 15-30 days post-sowing).");
        pmfbyPending.add("Requires Village Administrative Officer (VAO) crop cultivation certificate or sowing certificate for non-loanee farmers.");

        evaluations.add(new SchemeEvaluation(
                "PMFBY",
                "Pradhan Mantri Fasal Bima Yojana (Crop Insurance)",
                "Department of Agriculture & Farmers Welfare, GoI in collaboration with State Govt",
                EligibilityStatus.LIKELY_ELIGIBLE,
                "Comprehensive yield risk & localized calamity coverage. Farmer pays subsidized premium of only 2.0% for Kharif food/oilseeds, 1.5% for Rabi crops, and 5.0% for horticultural/commercial crops.",
                pmfbySat,
                pmfbyPending,
                List.of("Aadhaar Card", "Land Possession Certificate / Patta / Land Lease Agreement", "Crop Sowing Certificate issued by VAO/Agriculture Officer", "Cancelled cheque / Bank passbook"),
                "https://pmfby.gov.in",
                "Enroll through national portal (pmfby.gov.in), Primary Agricultural Credit Society (PACS), commercial bank branch, or CSC."
        ));

        // 3. PMKSY - Per Drop More Crop (Micro Irrigation)
        boolean isSmallOrMarginal = acres <= 4.94;
        List<String> pmksySat = new ArrayList<>();
        pmksySat.add(String.format("Cultivated land area (%.2f acres) qualifies for micro-irrigation installation.", acres));
        if (isSmallOrMarginal) {
            pmksySat.add("Qualifies for priority Small & Marginal Farmer maximum subsidy tier (55% government subsidy).");
        } else {
            pmksySat.add("Qualifies for standard farmer tier (45% government subsidy).");
        }

        List<String> pmksyPending = new ArrayList<>();
        pmksyPending.add("Requires confirmed perennial irrigation source (borewell, open well, farm pond, or canal access).");
        pmksyPending.add("Water and soil electrical conductivity (EC) must be within permissible limits for drip/sprinkler components.");

        evaluations.add(new SchemeEvaluation(
                "PMKSY-PDMC",
                "Per Drop More Crop - Micro Irrigation (Drip / Sprinkler)",
                "Ministry of Agriculture & Farmers Welfare & State Horticulture/Agri Engineering Dept",
                EligibilityStatus.LIKELY_ELIGIBLE,
                isSmallOrMarginal
                        ? "55% government subsidy on certified Drip/Sprinkler micro-irrigation system for Small and Marginal farmers."
                        : "45% government subsidy on certified Drip/Sprinkler micro-irrigation system.",
                pmksySat,
                pmksyPending,
                List.of("Aadhaar Card", "Land Patta & Field FMB sketch map", "EB connection card / Borewell certificate / Water source proof", "Soil & Water Test Analysis Report", "Passport photo"),
                "https://pmksy.gov.in",
                "Submit online application on State Horticulture / Agriculture portal or visit Block Assistant Director of Horticulture (ADH) office."
        ));

        // 4. Kisan Credit Card (KCC)
        List<String> kccSat = new ArrayList<>();
        kccSat.add(String.format("Active cultivation of %.2f acres of %s qualifies for institutional crop production credit.", acres, crop));
        kccSat.add("Applicable to owner cultivators, registered tenant farmers, and sharecroppers.");

        List<String> kccPending = new ArrayList<>();
        pmksyPending.add("Must not have active unpaid non-performing loan default with institutional commercial banks.");
        kccPending.add("Credit limit scaled against district Scale of Finance (SoF) approved by District Level Technical Committee (DLTC).");

        evaluations.add(new SchemeEvaluation(
                "KCC",
                "Kisan Credit Card (Subsidized Institutional Crop Credit)",
                "National Bank for Agriculture and Rural Development (NABARD) & Commercial/Cooperative Banks",
                EligibilityStatus.LIKELY_ELIGIBLE,
                "Concessional working capital crop loan up to Rs 3,00,000 at a benchmark interest rate of 7%, further reduced to an effective 4% per annum upon prompt repayment within 12 months.",
                kccSat,
                kccPending,
                List.of("Duly completed KCC Application Form", "Aadhaar Card & PAN Card", "Land ownership records (Patta/RoR) and current adangal/sowing record", "No-dues certificate from service-area bank branch"),
                "https://myscheme.gov.in/schemes/kcc",
                "Apply at nearest public sector bank branch, Regional Rural Bank (RRB), or Primary Agricultural Cooperative Credit Society (PACCS)."
        ));

        // 5. State-specific Scheme (e.g. Tamil Nadu KAVIADP)
        if ("tamil nadu".equalsIgnoreCase(state) || "tn".equalsIgnoreCase(state)) {
            evaluations.add(new SchemeEvaluation(
                    "KAVIADP-TN",
                    "Kalaignarin All Village Integrated Agriculture Development Programme",
                    "Department of Agriculture & Farmers Welfare, Government of Tamil Nadu",
                    EligibilityStatus.CONDITIONALLY_ELIGIBLE,
                    "Free distribution of coconut saplings, vegetable seed minikits, subsidized battery sprayers, and farm pond excavation support in selected village panchayats.",
                    List.of("Resident farmer in Tamil Nadu operational village.", "Possesses cultivable land holding."),
                    List.of("Village must be notified under the current phase of KAVIADP implementation.", "One benefit per farmer family per fiscal year."),
                    List.of("Aadhaar Card", "Ration Card (Smart Card)", "Patta / Chitta document"),
                    "https://agritech.tnau.ac.in",
                    "Register through Block Assistant Director of Agriculture (ADA) office or via Uzhavan Mobile App."
            ));
        }

        String disclaimer = "DISCLAIMER: This assessment provides preliminary informational guidance based on officially published guidelines. " +
                "Final eligibility, sanction, and subsidy disbursement are subject to field verification by competent government revenue and agricultural officers.";

        return new SchemeAssessmentReport(
                acres, landCategory, state, district, crop, evaluations, disclaimer
        );
    }
}
