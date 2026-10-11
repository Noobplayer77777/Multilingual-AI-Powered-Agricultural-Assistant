package com.agri.query.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheme_assessments")
public class SchemeAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "scheme_code", nullable = false, length = 100)
    private String schemeCode;

    @Column(name = "scheme_name", nullable = false)
    private String schemeName;

    @Column(name = "is_eligible", nullable = false)
    private boolean isEligible;

    @Column(name = "satisfied_criteria_json", columnDefinition = "TEXT")
    private String satisfiedCriteriaJson;

    @Column(name = "missing_criteria_json", columnDefinition = "TEXT")
    private String missingCriteriaJson;

    @Column(name = "official_url", length = 1024)
    private String officialUrl;

    @Column(name = "assessed_at", insertable = false, updatable = false)
    private LocalDateTime assessedAt;

    public SchemeAssessment() {}

    public SchemeAssessment(User user, String schemeCode, String schemeName, boolean isEligible,
                            String satisfiedCriteriaJson, String missingCriteriaJson, String officialUrl) {
        this.user = user;
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.isEligible = isEligible;
        this.satisfiedCriteriaJson = satisfiedCriteriaJson;
        this.missingCriteriaJson = missingCriteriaJson;
        this.officialUrl = officialUrl;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSchemeCode() { return schemeCode; }
    public void setSchemeCode(String schemeCode) { this.schemeCode = schemeCode; }

    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }

    public boolean isEligible() { return isEligible; }
    public void setEligible(boolean eligible) { isEligible = eligible; }

    public String getSatisfiedCriteriaJson() { return satisfiedCriteriaJson; }
    public void setSatisfiedCriteriaJson(String satisfiedCriteriaJson) { this.satisfiedCriteriaJson = satisfiedCriteriaJson; }

    public String getMissingCriteriaJson() { return missingCriteriaJson; }
    public void setMissingCriteriaJson(String missingCriteriaJson) { this.missingCriteriaJson = missingCriteriaJson; }

    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }

    public LocalDateTime getAssessedAt() { return assessedAt; }
    public void setAssessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; }
}
