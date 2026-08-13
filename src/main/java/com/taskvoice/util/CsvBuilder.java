package com.taskvoice.util;

import com.taskvoice.model.TimesheetEntry;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvBuilder {

    public static byte[] generateTimesheetCsv(List<TimesheetEntry> entries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        // Header
        writer.println("Entry ID,User Name,Entry Date,Title,Category,Project,Task,Duration (Hours),Confirmed");

        // Rows
        for (TimesheetEntry e : entries) {
            writer.printf("%d,\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%b%n",
                e.getId(),
                escape(e.getUserName()),
                e.getEntryDate(),
                escape(e.getTitle()),
                escape(e.getCategoryName() != null ? e.getCategoryName() : "General"),
                escape(e.getProjectTitle() != null ? e.getProjectTitle() : "-"),
                escape(e.getTaskTitle() != null ? e.getTaskTitle() : "-"),
                e.getDurationHours(),
                e.isConfirmed()
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}
