package com.taskvoice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Project {
    private int id;
    private String title;
    private String description;
    private int managerId;
    private String managerName;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String aiSummary;
    private String aiSentiment;
    private Double aiSentimentConfidence;
    private String aiSentimentExplanation;
    private LocalDateTime sentimentGeneratedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields (populated by service/DAO)
    private int totalTasks;
    private int completedTasks;
    private int memberCount;
    private double progressPct;
    private double totalHours;

    public Project() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public String getAiSentiment() { return aiSentiment; }
    public void setAiSentiment(String aiSentiment) { this.aiSentiment = aiSentiment; }

    public Double getAiSentimentConfidence() { return aiSentimentConfidence; }
    public void setAiSentimentConfidence(Double aiSentimentConfidence) { this.aiSentimentConfidence = aiSentimentConfidence; }

    public String getAiSentimentExplanation() { return aiSentimentExplanation; }
    public void setAiSentimentExplanation(String aiSentimentExplanation) { this.aiSentimentExplanation = aiSentimentExplanation; }

    public LocalDateTime getSentimentGeneratedAt() { return sentimentGeneratedAt; }
    public void setSentimentGeneratedAt(LocalDateTime sentimentGeneratedAt) { this.sentimentGeneratedAt = sentimentGeneratedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public double getProgressPct() { return progressPct; }
    public void setProgressPct(double progressPct) { this.progressPct = progressPct; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public boolean isCompleted() { return "COMPLETED".equals(status); }
    public boolean isActive() { return "ACTIVE".equals(status); }
    public boolean hasSentiment() { return sentimentGeneratedAt != null; }
}
