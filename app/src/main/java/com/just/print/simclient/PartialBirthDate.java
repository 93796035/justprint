package com.just.print.simclient;

import java.time.LocalDate;
import java.time.YearMonth;

/** The youngest and oldest possible completed ages for a partially entered DOB. */
public final class PartialBirthDate {
    private PartialBirthDate() {}

    public static int[] ages(int year, Integer month, Integer day, LocalDate today) {
        if (day != null && month == null) throw new IllegalArgumentException("Enter month first");
        LocalDate earliest;
        LocalDate latest;
        if (month == null) {
            earliest = LocalDate.of(year, 1, 1);
            latest = LocalDate.of(year, 12, 31);
        } else {
            YearMonth ym = YearMonth.of(year, month);
            earliest = day == null ? ym.atDay(1) : ym.atDay(day);
            latest = day == null ? ym.atEndOfMonth() : earliest;
        }
        LocalDate oldestAllowed = today.minusYears(120);
        if (latest.isBefore(oldestAllowed) || earliest.isAfter(today))
            throw new IllegalArgumentException("Birth date out of range");
        if (earliest.isBefore(oldestAllowed)) earliest = oldestAllowed;
        if (latest.isAfter(today)) latest = today;
        return new int[]{BirthDateParser.age(latest, today), BirthDateParser.age(earliest, today)};
    }
}
