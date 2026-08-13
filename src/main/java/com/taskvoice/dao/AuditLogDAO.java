package com.taskvoice.dao;

import com.taskvoice.model.AuditLog;
import java.util.List;

public interface AuditLogDAO {
    void log(int actorId, String actorName, String action, String entityType, Integer entityId, String detail, String ipAddress);
    List<AuditLog> findAll(int page, int pageSize, String actionFilter);
    int countAll(String actionFilter);
}
