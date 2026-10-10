package com.agri.advisory.dto;

import java.util.List;

public class AdvisoryResponse {
    private String answer;
    private String language;
    private List<Source> sources;

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<Source> getSources() { return sources; }
    public void setSources(List<Source> sources) { this.sources = sources; }
}
