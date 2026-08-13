package com.taskvoice.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtil {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATETIME_FMT);
    }

    public static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s); // ISO format yyyy-MM-dd from HTML date inputs
    }

    public static long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    public static boolean isToday(LocalDate date) {
        return LocalDate.now().equals(date);
    }

    public static boolean isPast(LocalDate date) {
        return date != null && LocalDate.now().isAfter(date);
    }

    public static boolean isDueSoon(LocalDate date) {
        if (date == null) return false;
        LocalDate today = LocalDate.now();
        return !today.isAfter(date) && ChronoUnit.DAYS.between(today, date) <= 2;
    }
}
