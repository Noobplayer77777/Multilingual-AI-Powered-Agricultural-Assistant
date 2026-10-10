package com.agri.advisory.dto;

import jakarta.validation.constraints.NotBlank;

public class AdvisoryRequest {
    @NotBlank
    private String question;
    private String language;
    private String category;
    private String crop;
    private String location;
    private String season;

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
}
