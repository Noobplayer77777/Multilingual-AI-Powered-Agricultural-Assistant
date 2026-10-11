package com.agri.query.service;

import com.agri.query.entity.*;

import java.util.List;

public interface FarmMemoryService {
    FarmerProfile getProfile(User user);
    FarmerProfile updateProfile(User user, FarmerProfile updated);

    List<FarmPlot> getPlots(User user);
    FarmPlot createPlot(User user, FarmPlot plot);
    FarmPlot updatePlot(User user, Integer plotId, FarmPlot updated);
    void deletePlot(User user, Integer plotId);

    List<FarmReminder> getReminders(User user);
    FarmReminder createReminder(User user, FarmReminder reminder);
    FarmReminder toggleReminder(User user, Integer reminderId);
    void deleteReminder(User user, Integer reminderId);

    List<CropPlan> getCropPlans(User user);
    CropPlan saveCropPlan(User user, CropPlan plan);

    List<SchemeAssessment> getSchemeAssessments(User user);
    SchemeAssessment saveSchemeAssessment(User user, SchemeAssessment assessment);
}
