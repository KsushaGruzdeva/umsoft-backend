package com.umsoft.backend.dtos;

public class ClassificationResult {
    private String category;
    private String categoryName;
    private String summary;
    private String assignedEmail;
    private Double confidenceScore;

    public ClassificationResult() {}

    public ClassificationResult(String category, String categoryName, String summary, String assignedEmail, Double confidenceScore) {
        this.category = category;
        this.categoryName = categoryName;
        this.summary = summary;
        this.assignedEmail = assignedEmail;
        this.confidenceScore = confidenceScore;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getSummary() {
        return summary;
    }

    public String getAssignedEmail() {
        return assignedEmail;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setAssignedEmail(String assignedEmail) {
        this.assignedEmail = assignedEmail;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}