package com.agri.query.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sources")
public class SourceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id")
    private ResponseEntityData response;

    private String name;
    private String type;
    private String url;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public ResponseEntityData getResponse() { return response; }
    public void setResponse(ResponseEntityData response) { this.response = response; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
