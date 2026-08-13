package com.taskvoice.service;

import com.taskvoice.dao.*;
import com.taskvoice.dao.impl.*;
import com.taskvoice.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TimesheetService {

    private final TimesheetDAO timesheetDAO = new TimesheetDAOImpl();
    private final TaskDAO      taskDAO      = new TaskDAOImpl();

    public List<TimesheetEntry> getForDate(int userId, LocalDate date) {
        return timesheetDAO.findByUserAndDate(userId, date);
    }

    public List<TimesheetEntry> getForRange(int userId, LocalDate from, LocalDate to) {
        return timesheetDAO.findByUserAndRange(userId, from, to);
    }

    public List<TimesheetEntry> getTeamForRange(int managerId, LocalDate from, LocalDate to) {
        return timesheetDAO.findByManagerTeamAndRange(managerId, from, to);
    }

    public double getTodayHours(int userId) {
        return timesheetDAO.sumHoursByUserAndDate(userId, LocalDate.now());
    }

    public double getRangeHours(int userId, LocalDate from, LocalDate to) {
        return timesheetDAO.sumHoursByUserAndRange(userId, from, to);
    }

    public boolean hasEntryToday(int userId) { return timesheetDAO.hasEntryForToday(userId); }

    public int saveEntry(TimesheetEntry entry) {
        int id = timesheetDAO.insert(entry);
        // Rollup actual hours on task if linked
        if (entry.getTaskId() != null) {
            double hours = timesheetDAO.sumHoursByTaskId(entry.getTaskId());
            taskDAO.updateActualHours(entry.getTaskId(), hours);
        }
        return id;
    }

    public void updateEntry(TimesheetEntry entry) {
        timesheetDAO.update(entry);
        if (entry.getTaskId() != null) {
            double hours = timesheetDAO.sumHoursByTaskId(entry.getTaskId());
            taskDAO.updateActualHours(entry.getTaskId(), hours);
        }
    }

    public void deleteEntry(int id) { timesheetDAO.delete(id); }

    public Optional<TimesheetEntry> findById(int id) { return timesheetDAO.findById(id); }
}
