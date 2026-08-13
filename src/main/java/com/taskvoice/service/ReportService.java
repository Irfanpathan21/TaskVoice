package com.taskvoice.service;

import com.taskvoice.dao.TaskDAO;
import com.taskvoice.dao.TimesheetDAO;
import com.taskvoice.dao.UserDAO;
import com.taskvoice.dao.impl.TaskDAOImpl;
import com.taskvoice.dao.impl.TimesheetDAOImpl;
import com.taskvoice.dao.impl.UserDAOImpl;
import com.taskvoice.model.Task;
import com.taskvoice.model.TimesheetEntry;
import com.taskvoice.model.User;
import com.taskvoice.util.CsvBuilder;
import com.taskvoice.util.PdfBuilder;

import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final TimesheetDAO timesheetDAO = new TimesheetDAOImpl();
    private final TaskDAO      taskDAO      = new TaskDAOImpl();
    private final UserDAO      userDAO      = new UserDAOImpl();

    public byte[] generatePdfReport(int userId, LocalDate from, LocalDate to) throws Exception {
        User user = userDAO.findById(userId).orElseThrow();
        List<TimesheetEntry> entries = timesheetDAO.findByUserAndRange(userId, from, to);
        List<Task> tasks = taskDAO.findByAssigneeId(userId, 1, 1000);
        return PdfBuilder.generateWorkStatement(user, from, to, entries, tasks);
    }

    public byte[] generateTeamPdfReport(int managerId, LocalDate from, LocalDate to) throws Exception {
        User manager = userDAO.findById(managerId).orElseThrow();
        List<TimesheetEntry> entries = timesheetDAO.findByManagerTeamAndRange(managerId, from, to);
        List<Task> tasks = taskDAO.findByManagerTeam(managerId, 1, 1000);
        return PdfBuilder.generateWorkStatement(manager, from, to, entries, tasks);
    }

    public byte[] generateCsvReport(int userId, LocalDate from, LocalDate to) {
        List<TimesheetEntry> entries = timesheetDAO.findByUserAndRange(userId, from, to);
        return CsvBuilder.generateTimesheetCsv(entries);
    }

    public byte[] generateTeamCsvReport(int managerId, LocalDate from, LocalDate to) {
        List<TimesheetEntry> entries = timesheetDAO.findByManagerTeamAndRange(managerId, from, to);
        return CsvBuilder.generateTimesheetCsv(entries);
    }
}
