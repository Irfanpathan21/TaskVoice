package com.taskvoice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimesheetEntry {
    private int id;
    private int userId;
    private String userName;
    private Integer voiceRecordId;
    private Integer taskId;
    private String taskTitle;
    private Integer projectId;
    private String projectTitle;
    private Integer categoryId;
    private String categoryName;
    private LocalDate entryDate;
    private String title;
    private String description;
    private double durationHours;
    private boolean confirmed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TimesheetEntry() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Integer getVoiceRecordId() { return voiceRecordId; }
    public void setVoiceRecordId(Integer voiceRecordId) { this.voiceRecordId = voiceRecordId; }

    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getDurationHours() { return durationHours; }
    public void setDurationHours(double durationHours) { this.durationHours = durationHours; }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isVoiceDerived() { return voiceRecordId != null; }
    public boolean isLinkedToTask() { return taskId != null; }
}
