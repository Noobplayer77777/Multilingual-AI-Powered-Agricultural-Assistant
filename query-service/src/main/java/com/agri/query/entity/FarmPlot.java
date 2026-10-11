package com.agri.query.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "farm_plots")
public class FarmPlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plot_name", nullable = false, length = 100)
    private String plotName;

    @Column(name = "area_acres", nullable = false, precision = 6, scale = 2)
    private BigDecimal areaAcres;

    @Column(name = "current_crop", length = 100)
    private String currentCrop;

    @Column(name = "crop_stage", length = 100)
    private String cropStage;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "soil_test_ph", precision = 4, scale = 2)
    private BigDecimal soilTestPh;

    @Column(name = "soil_organic_carbon", precision = 4, scale = 2)
    private BigDecimal soilOrganicCarbon;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public FarmPlot() {}

    public FarmPlot(User user, String plotName, BigDecimal areaAcres) {
        this.user = user;
        this.plotName = plotName;
        this.areaAcres = areaAcres;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPlotName() { return plotName; }
    public void setPlotName(String plotName) { this.plotName = plotName; }

    public BigDecimal getAreaAcres() { return areaAcres; }
    public void setAreaAcres(BigDecimal areaAcres) { this.areaAcres = areaAcres; }

    public String getCurrentCrop() { return currentCrop; }
    public void setCurrentCrop(String currentCrop) { this.currentCrop = currentCrop; }

    public String getCropStage() { return cropStage; }
    public void setCropStage(String cropStage) { this.cropStage = cropStage; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getSoilTestPh() { return soilTestPh; }
    public void setSoilTestPh(BigDecimal soilTestPh) { this.soilTestPh = soilTestPh; }

    public BigDecimal getSoilOrganicCarbon() { return soilOrganicCarbon; }
    public void setSoilOrganicCarbon(BigDecimal soilOrganicCarbon) { this.soilOrganicCarbon = soilOrganicCarbon; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
