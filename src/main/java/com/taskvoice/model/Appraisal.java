package com.taskvoice.model;

import java.time.LocalDateTime;

public class Appraisal {
    private int id;
    private int periodId;
    private String periodTitle;
    private int employeeId;
    private String employeeName;
    private String employeeNo;
    private int managerId;
    private String managerName;

    // AI-generated values (stored, never overwritten)
    private Double aiScore;
    private String aiGrade;
    private String aiPromotionRec;
    private String aiIncrementRange;
    private String aiSummary;
    private String aiStrengths;
    private String aiImprovements;
    private String aiProductivityAnalysis;
    private String aiReliabilityAnalysis;
    private String aiConsistencyAnalysis;
    private String aiProblemSolving;
    private LocalDateTime aiGeneratedAt;

    // Manager final values
    private Double managerScore;
    private String managerGrade;
    private String managerDecision; // ACCEPTED, MODIFIED, REJECTED
    private String managerRemark;
    private LocalDateTime managerReviewedAt;

    private String finalStatus; // PENDING_AI, PENDING_REVIEW, FINALIZED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Appraisal() {}

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }

    public String getPeriodTitle() { return periodTitle; }
    public void setPeriodTitle(String periodTitle) { this.periodTitle = periodTitle; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public Double getAiScore() { return aiScore; }
    public void setAiScore(Double aiScore) { this.aiScore = aiScore; }

    public String getAiGrade() { return aiGrade; }
    public void setAiGrade(String aiGrade) { this.aiGrade = aiGrade; }

    public String getAiPromotionRec() { return aiPromotionRec; }
    public void setAiPromotionRec(String aiPromotionRec) { this.aiPromotionRec = aiPromotionRec; }

    public String getAiIncrementRange() { return aiIncrementRange; }
    public void setAiIncrementRange(String aiIncrementRange) { this.aiIncrementRange = aiIncrementRange; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public String getAiStrengths() { return aiStrengths; }
    public void setAiStrengths(String aiStrengths) { this.aiStrengths = aiStrengths; }

    public String getAiImprovements() { return aiImprovements; }
    public void setAiImprovements(String aiImprovements) { this.aiImprovements = aiImprovements; }

    public String getAiProductivityAnalysis() { return aiProductivityAnalysis; }
    public void setAiProductivityAnalysis(String aiProductivityAnalysis) { this.aiProductivityAnalysis = aiProductivityAnalysis; }

    public String getAiReliabilityAnalysis() { return aiReliabilityAnalysis; }
    public void setAiReliabilityAnalysis(String aiReliabilityAnalysis) { this.aiReliabilityAnalysis = aiReliabilityAnalysis; }

    public String getAiConsistencyAnalysis() { return aiConsistencyAnalysis; }
    public void setAiConsistencyAnalysis(String aiConsistencyAnalysis) { this.aiConsistencyAnalysis = aiConsistencyAnalysis; }

    public String getAiProblemSolving() { return aiProblemSolving; }
    public void setAiProblemSolving(String aiProblemSolving) { this.aiProblemSolving = aiProblemSolving; }

    public LocalDateTime getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(LocalDateTime aiGeneratedAt) { this.aiGeneratedAt = aiGeneratedAt; }

    public Double getManagerScore() { return managerScore; }
    public void setManagerScore(Double managerScore) { this.managerScore = managerScore; }

    public String getManagerGrade() { return managerGrade; }
    public void setManagerGrade(String managerGrade) { this.managerGrade = managerGrade; }

    public String getManagerDecision() { return managerDecision; }
    public void setManagerDecision(String managerDecision) { this.managerDecision = managerDecision; }

    public String getManagerRemark() { return managerRemark; }
    public void setManagerRemark(String managerRemark) { this.managerRemark = managerRemark; }

    public LocalDateTime getManagerReviewedAt() { return managerReviewedAt; }
    public void setManagerReviewedAt(LocalDateTime managerReviewedAt) { this.managerReviewedAt = managerReviewedAt; }

    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPendingAi()     { return "PENDING_AI".equals(finalStatus); }
    public boolean isPendingReview() { return "PENDING_REVIEW".equals(finalStatus); }
    public boolean isFinalized()     { return "FINALIZED".equals(finalStatus); }

    public String getAiGradeDisplay() { return formatGrade(aiGrade); }
    public String getManagerGradeDisplay() { return formatGrade(managerGrade); }

    private String formatGrade(String g) {
        if (g == null) return null;
        return switch (g) {
            case "OUTSTANDING"       -> "Outstanding";
            case "EXCELLENT"         -> "Excellent";
            case "VERY_GOOD"         -> "Very Good";
            case "GOOD"              -> "Good";
            case "AVERAGE"           -> "Average";
            case "NEEDS_IMPROVEMENT" -> "Needs Improvement";
            default -> g;
        };
    }

    public String getPromotionRecDisplay() {
        if (aiPromotionRec == null) return null;
        return switch (aiPromotionRec) {
            case "STRONGLY_RECOMMEND" -> "Strongly Recommend";
            case "RECOMMEND"          -> "Recommend";
            case "CONSIDER"           -> "Consider";
            case "NOT_RECOMMENDED"    -> "Not Recommended";
            default -> aiPromotionRec;
        };
    }
}
