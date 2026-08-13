package com.taskvoice.model;

import java.time.LocalDateTime;

public class TaskUpdate {
    private int id;
    private int taskId;
    private String taskTitle;
    private int userId;
    private String userName;
    private int updateSeq;
    private String rawText;
    private String aiRephrasedText;
    private Integer completionPct;
    private String problemsFaced;
    private String note;
    private Integer voiceRecordId;
    private LocalDateTime createdAt;

    public TaskUpdate() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getUpdateSeq() { return updateSeq; }
    public void setUpdateSeq(int updateSeq) { this.updateSeq = updateSeq; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getAiRephrasedText() { return aiRephrasedText; }
    public void setAiRephrasedText(String aiRephrasedText) { this.aiRephrasedText = aiRephrasedText; }

    public Integer getCompletionPct() { return completionPct; }
    public void setCompletionPct(Integer completionPct) { this.completionPct = completionPct; }

    public String getProblemsFaced() { return problemsFaced; }
    public void setProblemsFaced(String problemsFaced) { this.problemsFaced = problemsFaced; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getVoiceRecordId() { return voiceRecordId; }
    public void setVoiceRecordId(Integer voiceRecordId) { this.voiceRecordId = voiceRecordId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isFirstUpdate()  { return updateSeq == 1; }
    public boolean isSecondUpdate() { return updateSeq == 2; }
    public boolean isThirdUpdate()  { return updateSeq == 3; }
    public boolean hasProblems()    { return problemsFaced != null && !problemsFaced.isBlank(); }
    public boolean hasAiVersion()   { return aiRephrasedText != null && !aiRephrasedText.isBlank(); }
}
