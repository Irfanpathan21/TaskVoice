package com.taskvoice.dao;

import com.taskvoice.model.TaskUpdate;
import java.util.List;

public interface ProgressUpdateDAO {
    List<TaskUpdate> findByTaskId(int taskId);
    int countByTaskId(int taskId);
    int nextSeqForTask(int taskId);
    int insert(TaskUpdate update);
}
