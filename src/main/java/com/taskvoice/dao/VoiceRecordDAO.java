package com.taskvoice.dao;

import com.taskvoice.model.VoiceRecord;
import java.util.List;
import java.util.Optional;

public interface VoiceRecordDAO {
    Optional<VoiceRecord> findById(int id);
    List<VoiceRecord> findDraftsByUserId(int userId);
    int insert(VoiceRecord record);
    void updateTranscript(int id, String transcript);
    void updateParsedJson(int id, String json, String status);
    void updateStatus(int id, String status, String errorMessage);
    void incrementRetryCount(int id);
}
