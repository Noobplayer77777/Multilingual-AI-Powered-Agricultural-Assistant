package com.agri.query.service.impl;

import com.agri.query.entity.*;
import com.agri.query.repository.*;
import com.agri.query.service.FarmMemoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FarmMemoryServiceImpl implements FarmMemoryService {

    private final FarmerProfileRepository farmerProfileRepository;
    private final FarmPlotRepository farmPlotRepository;
    private final FarmReminderRepository farmReminderRepository;
    private final CropPlanRepository cropPlanRepository;
    private final SchemeAssessmentRepository schemeAssessmentRepository;

    public FarmMemoryServiceImpl(FarmerProfileRepository farmerProfileRepository,
                                 FarmPlotRepository farmPlotRepository,
                                 FarmReminderRepository farmReminderRepository,
                                 CropPlanRepository cropPlanRepository,
                                 SchemeAssessmentRepository schemeAssessmentRepository) {
        this.farmerProfileRepository = farmerProfileRepository;
        this.farmPlotRepository = farmPlotRepository;
        this.farmReminderRepository = farmReminderRepository;
        this.cropPlanRepository = cropPlanRepository;
        this.schemeAssessmentRepository = schemeAssessmentRepository;
    }

    @Override
    @Transactional
    public FarmerProfile getProfile(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        return farmerProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
            FarmerProfile profile = new FarmerProfile();
            profile.setUser(user);
            profile.setPreferredLanguage("en");
            profile.setTotalLandAcres(java.math.BigDecimal.ZERO);
            return farmerProfileRepository.save(profile);
        });
    }

    @Override
    @Transactional
    public FarmerProfile updateProfile(User user, FarmerProfile updated) {
        FarmerProfile profile = getProfile(user);
        profile.setState(updated.getState());
        profile.setDistrict(updated.getDistrict());
        profile.setPrimaryCrop(updated.getPrimaryCrop());
        profile.setTotalLandAcres(updated.getTotalLandAcres());
        profile.setSoilType(updated.getSoilType());
        profile.setIrrigationSource(updated.getIrrigationSource());
        profile.setAnnualBudgetInr(updated.getAnnualBudgetInr());
        profile.setPreferredLanguage(updated.getPreferredLanguage() != null ? updated.getPreferredLanguage() : "en");
        profile.setConsentDataRetention(updated.getConsentDataRetention());
        return farmerProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmPlot> getPlots(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        return farmPlotRepository.findByUserIdOrderByPlotNameAsc(user.getId());
    }

    @Override
    @Transactional
    public FarmPlot createPlot(User user, FarmPlot plot) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        plot.setUser(user);
        return farmPlotRepository.save(plot);
    }

    @Override
    @Transactional
    public FarmPlot updatePlot(User user, Integer plotId, FarmPlot updated) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        FarmPlot existing = farmPlotRepository.findByIdAndUserId(plotId, user.getId())
                .orElseThrow(() -> new SecurityException("Plot not found or access denied"));

        existing.setPlotName(updated.getPlotName());
        existing.setAreaAcres(updated.getAreaAcres());
        existing.setCurrentCrop(updated.getCurrentCrop());
        existing.setCropStage(updated.getCropStage());
        existing.setLocation(updated.getLocation());
        existing.setSoilTestPh(updated.getSoilTestPh());
        existing.setSoilOrganicCarbon(updated.getSoilOrganicCarbon());
        return farmPlotRepository.save(existing);
    }

    @Override
    @Transactional
    public void deletePlot(User user, Integer plotId) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        FarmPlot existing = farmPlotRepository.findByIdAndUserId(plotId, user.getId())
                .orElseThrow(() -> new SecurityException("Plot not found or access denied"));
        farmPlotRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmReminder> getReminders(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        return farmReminderRepository.findByUserIdOrderByDueDateAsc(user.getId());
    }

    @Override
    @Transactional
    public FarmReminder createReminder(User user, FarmReminder reminder) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        reminder.setUser(user);
        return farmReminderRepository.save(reminder);
    }

    @Override
    @Transactional
    public FarmReminder toggleReminder(User user, Integer reminderId) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        FarmReminder existing = farmReminderRepository.findByIdAndUserId(reminderId, user.getId())
                .orElseThrow(() -> new SecurityException("Reminder not found or access denied"));
        existing.setCompleted(!existing.isCompleted());
        return farmReminderRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteReminder(User user, Integer reminderId) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        FarmReminder existing = farmReminderRepository.findByIdAndUserId(reminderId, user.getId())
                .orElseThrow(() -> new SecurityException("Reminder not found or access denied"));
        farmReminderRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropPlan> getCropPlans(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        return cropPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    @Transactional
    public CropPlan saveCropPlan(User user, CropPlan plan) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        plan.setUser(user);
        return cropPlanRepository.save(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeAssessment> getSchemeAssessments(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        return schemeAssessmentRepository.findByUserIdOrderByAssessedAtDesc(user.getId());
    }

    @Override
    @Transactional
    public SchemeAssessment saveSchemeAssessment(User user, SchemeAssessment assessment) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User context is required");
        }
        assessment.setUser(user);
        return schemeAssessmentRepository.save(assessment);
    }
}
