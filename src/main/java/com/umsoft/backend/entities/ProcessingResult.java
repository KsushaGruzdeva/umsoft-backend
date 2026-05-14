package com.umsoft.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processing_results")
public class ProcessingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "confidence_score")
    private Double confidenceScore;

//    @Column(name = "generated_email_text", columnDefinition = "TEXT")
//    private String generatedEmailText;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private Request request;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }

    public ProcessingResult() {}

    public ProcessingResult(String summary, Request request) {
        this.summary = summary;
        this.request = request;
    }

    public Long getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

//    public String getGeneratedEmailText() {
//        return generatedEmailText;
//    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public Request getRequest() {
        return request;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

//    public void setGeneratedEmailText(String generatedEmailText) {
//        this.generatedEmailText = generatedEmailText;
//    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}