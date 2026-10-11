package com.agri.query.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmer_profiles")
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private String district;
    private String state = "Tamil Nadu";

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    @Column(name = "primary_crop")
    private String primaryCrop;

    @Column(name = "total_land_acres", precision = 6, scale = 2)
    private BigDecimal totalLandAcres;

    @Column(name = "soil_type")
    private String soilType;

    @Column(name = "irrigation_source")
    private String irrigationSource;

    @Column(name = "annual_budget_inr", precision = 10, scale = 2)
    private BigDecimal annualBudgetInr;

    @Column(name = "consent_data_retention")
    private Boolean consentDataRetention = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FarmerProfile() {}

    public FarmerProfile(User user) {
        this.user = user;
        this.state = "Tamil Nadu";
        this.preferredLanguage = "en";
        this.consentDataRetention = true;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getPrimaryCrop() { return primaryCrop; }
    public void setPrimaryCrop(String primaryCrop) { this.primaryCrop = primaryCrop; }

    public BigDecimal getTotalLandAcres() { return totalLandAcres; }
    public void setTotalLandAcres(BigDecimal totalLandAcres) { this.totalLandAcres = totalLandAcres; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public String getIrrigationSource() { return irrigationSource; }
    public void setIrrigationSource(String irrigationSource) { this.irrigationSource = irrigationSource; }

    public BigDecimal getAnnualBudgetInr() { return annualBudgetInr; }
    public void setAnnualBudgetInr(BigDecimal annualBudgetInr) { this.annualBudgetInr = annualBudgetInr; }

    public Boolean getConsentDataRetention() { return consentDataRetention; }
    public void setConsentDataRetention(Boolean consentDataRetention) { this.consentDataRetention = consentDataRetention; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
