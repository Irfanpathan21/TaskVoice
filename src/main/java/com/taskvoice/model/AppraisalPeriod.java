package com.taskvoice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppraisalPeriod {
    private int id;
    private String title;
    private String periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int createdBy;
    private String createdByName;
    private String status;
    private LocalDateTime createdAt;

    // Stats populated on list view
    private int totalAppraisals;
    private int finalizedAppraisals;

    public AppraisalPeriod() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getTotalAppraisals() { return totalAppraisals; }
    public void setTotalAppraisals(int totalAppraisals) { this.totalAppraisals = totalAppraisals; }

    public int getFinalizedAppraisals() { return finalizedAppraisals; }
    public void setFinalizedAppraisals(int finalizedAppraisals) { this.finalizedAppraisals = finalizedAppraisals; }

    public boolean isOpen()   { return "OPEN".equals(status); }
    public boolean isClosed() { return "CLOSED".equals(status); }
}
