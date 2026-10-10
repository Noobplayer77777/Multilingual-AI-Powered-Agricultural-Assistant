package com.agri.query.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QueryResponse {
    private Integer id;
    private String question;
    private String answer;
    private String language;
    private String location;
    private String season;
    private LocalDateTime createdAt;
    private List<Source> sources;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Source> getSources() { return sources; }
    public void setSources(List<Source> sources) { this.sources = sources; }
}
