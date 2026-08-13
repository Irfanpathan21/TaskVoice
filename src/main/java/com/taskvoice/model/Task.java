package com.taskvoice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private int id;
    private int projectId;
    private String projectTitle;
    private String title;
    private String description;
    private int assigneeId;
    private String assigneeName;
    private String assigneeNo;
    private Integer categoryId;
    private String categoryName;
    private String priority;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private double expectedHours;
    private double actualHours;
    private int completionPct;
    private String managerGrade;
    private Double managerScore;
    private String managerRemark;
    private LocalDateTime gradedAt;
    private Integer gradedBy;
    private String gradedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAssigneeId() { return assigneeId; }
    public void setAssigneeId(int assigneeId) { this.assigneeId = assigneeId; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public String getAssigneeNo() { return assigneeNo; }
    public void setAssigneeNo(String assigneeNo) { this.assigneeNo = assigneeNo; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public double getExpectedHours() { return expectedHours; }
    public void setExpectedHours(double expectedHours) { this.expectedHours = expectedHours; }

    public double getActualHours() { return actualHours; }
    public void setActualHours(double actualHours) { this.actualHours = actualHours; }

    public int getCompletionPct() { return completionPct; }
    public void setCompletionPct(int completionPct) { this.completionPct = completionPct; }

    public String getManagerGrade() { return managerGrade; }
    public void setManagerGrade(String managerGrade) { this.managerGrade = managerGrade; }

    public Double getManagerScore() { return managerScore; }
    public void setManagerScore(Double managerScore) { this.managerScore = managerScore; }

    public String getManagerRemark() { return managerRemark; }
    public void setManagerRemark(String managerRemark) { this.managerRemark = managerRemark; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public Integer getGradedBy() { return gradedBy; }
    public void setGradedBy(Integer gradedBy) { this.gradedBy = gradedBy; }

    public String getGradedByName() { return gradedByName; }
    public void setGradedByName(String gradedByName) { this.gradedByName = gradedByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCompleted() { return "COMPLETED".equals(status); }
    public boolean isOverdue() {
        return dueDate != null && !isCompleted() && LocalDate.now().isAfter(dueDate);
    }
    public boolean isDueSoon() {
        return dueDate != null && !isCompleted() && !isOverdue()
            && !LocalDate.now().isAfter(dueDate.minusDays(2));
    }

    /** Returns the grade label formatted for display (A_PLUS → A+) */
    public String getGradeDisplay() {
        if (managerGrade == null) return null;
        return switch (managerGrade) {
            case "A_PLUS"            -> "A+";
            case "A"                 -> "A";
            case "B_PLUS"            -> "B+";
            case "B"                 -> "B";
            case "C"                 -> "C";
            case "NEEDS_IMPROVEMENT" -> "Needs Improvement";
            default -> managerGrade;
        };
    }
}
