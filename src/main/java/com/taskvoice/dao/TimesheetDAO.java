package com.taskvoice.dao;

import com.taskvoice.model.TimesheetEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimesheetDAO {
    Optional<TimesheetEntry> findById(int id);
    List<TimesheetEntry> findByUserAndDate(int userId, LocalDate date);
    List<TimesheetEntry> findByUserAndRange(int userId, LocalDate from, LocalDate to);
    List<TimesheetEntry> findByManagerTeamAndRange(int managerId, LocalDate from, LocalDate to);
    List<TimesheetEntry> findByTaskId(int taskId);
    double sumHoursByUserAndDate(int userId, LocalDate date);
    double sumHoursByTaskId(int taskId);
    double sumHoursByUserAndRange(int userId, LocalDate from, LocalDate to);
    boolean hasEntryForToday(int userId);
    int insert(TimesheetEntry entry);
    void update(TimesheetEntry entry);
    void confirm(int id);
    void delete(int id);
}
