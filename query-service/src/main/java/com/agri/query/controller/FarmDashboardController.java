package com.agri.query.controller;

import com.agri.query.entity.*;
import com.agri.query.service.AuthService;
import com.agri.query.service.FarmMemoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class FarmDashboardController {

    private final AuthService authService;
    private final FarmMemoryService farmMemoryService;

    public FarmDashboardController(AuthService authService, FarmMemoryService farmMemoryService) {
        this.authService = authService;
        this.farmMemoryService = farmMemoryService;
    }

    // --- MVC Views ---

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        FarmerProfile profile = farmMemoryService.getProfile(user);
        List<FarmPlot> plots = farmMemoryService.getPlots(user);
        List<FarmReminder> reminders = farmMemoryService.getReminders(user);
        List<CropPlan> cropPlans = farmMemoryService.getCropPlans(user);
        List<SchemeAssessment> schemeAssessments = farmMemoryService.getSchemeAssessments(user);

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("plots", plots);
        model.addAttribute("reminders", reminders);
        model.addAttribute("cropPlans", cropPlans);
        model.addAttribute("schemeAssessments", schemeAssessments);
        model.addAttribute("newPlot", new FarmPlot());
        model.addAttribute("newReminder", new FarmReminder());

        return "dashboard";
    }

    @GetMapping("/profile")
    public String profileRedirect() {
        return "redirect:/dashboard";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute FarmerProfile updatedProfile, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        try {
            farmMemoryService.updateProfile(userOpt.get(), updatedProfile);
            redirectAttributes.addFlashAttribute("successMessage", "Farm profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not update farm profile: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/plots")
    public String addPlot(@ModelAttribute FarmPlot plot, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        try {
            farmMemoryService.createPlot(userOpt.get(), plot);
            redirectAttributes.addFlashAttribute("successMessage", "New plot '" + plot.getPlotName() + "' recorded.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not record plot: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/plots/{id}/delete")
    public String deletePlot(@PathVariable("id") Integer plotId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        try {
            farmMemoryService.deletePlot(userOpt.get(), plotId);
            redirectAttributes.addFlashAttribute("successMessage", "Plot removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/reminders")
    public String addReminder(@RequestParam("taskTitle") String taskTitle,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "dueDate", required = false) String dueDateStr,
                              @RequestParam(value = "plotId", required = false) Integer plotId,
                              RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        try {
            FarmReminder reminder = new FarmReminder();
            reminder.setTitle(taskTitle);
            reminder.setDescription(description);
            if (dueDateStr != null && !dueDateStr.isEmpty()) {
                reminder.setDueDate(LocalDate.parse(dueDateStr));
            }
            if (plotId != null) {
                farmMemoryService.getPlots(userOpt.get()).stream()
                        .filter(p -> p.getId().equals(plotId))
                        .findFirst()
                        .ifPresent(p -> reminder.setRelatedCrop(p.getCurrentCrop()));
            }
            farmMemoryService.createReminder(userOpt.get(), reminder);
            redirectAttributes.addFlashAttribute("successMessage", "Reminder scheduled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not schedule reminder: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/reminders/{id}/toggle")
    public String toggleReminder(@PathVariable("id") Integer reminderId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        try {
            farmMemoryService.toggleReminder(userOpt.get(), reminderId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/reminders/{id}/delete")
    public String deleteReminder(@PathVariable("id") Integer reminderId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        try {
            farmMemoryService.deleteReminder(userOpt.get(), reminderId);
            redirectAttributes.addFlashAttribute("successMessage", "Reminder deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // --- REST APIs for Farm Memory ---

    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<?> getApiProfile() {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(farmMemoryService.getProfile(userOpt.get()));
    }

    @PutMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<?> updateApiProfile(@RequestBody FarmerProfile profile) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(farmMemoryService.updateProfile(userOpt.get(), profile));
    }

    @GetMapping("/api/plots")
    @ResponseBody
    public ResponseEntity<?> getApiPlots() {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(farmMemoryService.getPlots(userOpt.get()));
    }

    @PostMapping("/api/plots")
    @ResponseBody
    public ResponseEntity<?> createApiPlot(@RequestBody FarmPlot plot) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(farmMemoryService.createPlot(userOpt.get(), plot));
    }

    @PutMapping("/api/plots/{id}")
    @ResponseBody
    public ResponseEntity<?> updateApiPlot(@PathVariable("id") Integer plotId, @RequestBody FarmPlot plot) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.ok(farmMemoryService.updatePlot(userOpt.get(), plotId, plot));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/plots/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteApiPlot(@PathVariable("id") Integer plotId) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            farmMemoryService.deletePlot(userOpt.get(), plotId);
            return ResponseEntity.ok(Map.of("message", "Plot deleted"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/reminders")
    @ResponseBody
    public ResponseEntity<?> getApiReminders() {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(farmMemoryService.getReminders(userOpt.get()));
    }

    @PostMapping("/api/reminders")
    @ResponseBody
    public ResponseEntity<?> createApiReminder(@RequestBody FarmReminder reminder) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(farmMemoryService.createReminder(userOpt.get(), reminder));
    }

    @PutMapping("/api/reminders/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleApiReminder(@PathVariable("id") Integer reminderId) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.ok(farmMemoryService.toggleReminder(userOpt.get(), reminderId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/reminders/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteApiReminder(@PathVariable("id") Integer reminderId) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            farmMemoryService.deleteReminder(userOpt.get(), reminderId);
            return ResponseEntity.ok(Map.of("message", "Reminder deleted"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/plans")
    @ResponseBody
    public ResponseEntity<?> getApiPlans() {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(farmMemoryService.getCropPlans(userOpt.get()));
    }

    @PostMapping("/api/plans")
    @ResponseBody
    public ResponseEntity<?> saveApiPlan(@RequestBody CropPlan plan) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(farmMemoryService.saveCropPlan(userOpt.get(), plan));
    }

    @GetMapping("/api/schemes")
    @ResponseBody
    public ResponseEntity<?> getApiSchemes() {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(farmMemoryService.getSchemeAssessments(userOpt.get()));
    }

    @PostMapping("/api/schemes")
    @ResponseBody
    public ResponseEntity<?> saveApiScheme(@RequestBody SchemeAssessment assessment) {
        Optional<User> userOpt = authService.getCurrentAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(farmMemoryService.saveSchemeAssessment(userOpt.get(), assessment));
    }
}
