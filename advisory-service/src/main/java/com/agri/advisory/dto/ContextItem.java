package com.agri.advisory.dto;

public class ContextItem {
    private String text;
    private String sourceName;
    private String sourceUrl;
    private String type;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
