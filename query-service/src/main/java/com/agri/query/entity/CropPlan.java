package com.agri.query.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_plans")
public class CropPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(length = 50)
    private String season;

    @Column(name = "land_area_acres", precision = 6, scale = 2)
    private BigDecimal landAreaAcres;

    @Column(name = "total_budget_inr", precision = 10, scale = 2)
    private BigDecimal totalBudgetInr;

    @Column(name = "estimated_cost_inr", precision = 10, scale = 2)
    private BigDecimal estimatedCostInr;

    @Column(name = "expected_savings_inr", precision = 10, scale = 2)
    private BigDecimal expectedSavingsInr;

    @Column(name = "plan_type", nullable = false, length = 50)
    private String planType; // BUDGET_OPTIMIZED, LOW_COST_FARMING

    @Column(name = "cost_breakdown_json", columnDefinition = "TEXT")
    private String costBreakdownJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public CropPlan() {}

    public CropPlan(User user, String cropName, String season, BigDecimal landAreaAcres,
                    BigDecimal totalBudgetInr, BigDecimal estimatedCostInr,
                    BigDecimal expectedSavingsInr, String planType, String costBreakdownJson) {
        this.user = user;
        this.cropName = cropName;
        this.season = season;
        this.landAreaAcres = landAreaAcres;
        this.totalBudgetInr = totalBudgetInr;
        this.estimatedCostInr = estimatedCostInr;
        this.expectedSavingsInr = expectedSavingsInr;
        this.planType = planType;
        this.costBreakdownJson = costBreakdownJson;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public BigDecimal getLandAreaAcres() { return landAreaAcres; }
    public void setLandAreaAcres(BigDecimal landAreaAcres) { this.landAreaAcres = landAreaAcres; }

    public BigDecimal getTotalBudgetInr() { return totalBudgetInr; }
    public void setTotalBudgetInr(BigDecimal totalBudgetInr) { this.totalBudgetInr = totalBudgetInr; }

    public BigDecimal getEstimatedCostInr() { return estimatedCostInr; }
    public void setEstimatedCostInr(BigDecimal estimatedCostInr) { this.estimatedCostInr = estimatedCostInr; }

    public BigDecimal getExpectedSavingsInr() { return expectedSavingsInr; }
    public void setExpectedSavingsInr(BigDecimal expectedSavingsInr) { this.expectedSavingsInr = expectedSavingsInr; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public String getCostBreakdownJson() { return costBreakdownJson; }
    public void setCostBreakdownJson(String costBreakdownJson) { this.costBreakdownJson = costBreakdownJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
