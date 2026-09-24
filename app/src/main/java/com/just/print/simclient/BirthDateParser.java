package com.just.print.simclient;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Date extraction with labels and preserved ambiguity for the caller to resolve. */
public final class BirthDateParser {
    private BirthDateParser() {}
    private static final Pattern BIRTH = Pattern.compile(
            "\\b(?:DATE\\s+(?:OF\\s+)?BIRTH|BIRTH\\s*DATE|DOB|D\\.?O\\.?B\\.?|NAISSANCE|NE[E]?\\s+LE)\\b");
    private static final Pattern OTHER = Pattern.compile(
            "\\b(?:EXPIR\\w*|EXP\\.?|ISSU\\w*|DELIVR\\w*|VALIDE|VALID|PERMANENT\\s+RESIDENT\\s+SINCE|RESIDENT\\s+PERMANENT\\s+DEPUIS)\\b|\\b4[AB]\\b");
    private static final String SEP = "[\\s./-]+";
    private static final String MONTH = "([A-Z]{2,9})(?:\\s*/\\s*[A-Z]{2,9})?\\.?";
    private static final Pattern YMD = Pattern.compile("(?<![A-Z0-9])((?:19|20)\\d{2})" + SEP + "(\\d{1,2})" + SEP + "(\\d{1,2})(?![A-Z0-9])");
    private static final Pattern DMY = Pattern.compile("(?<![A-Z0-9])(\\d{1,2})" + SEP + "(\\d{1,2})" + SEP + "((?:19|20)\\d{2})(?![A-Z0-9])");
    private static final Pattern Y_TEXT_M_D = Pattern.compile("(?<![A-Z0-9])((?:19|20)\\d{2})" + SEP + MONTH + SEP + "(\\d{1,2})(?![A-Z0-9])");
    private static final Pattern D_TEXT_M_Y = Pattern.compile("(?<![A-Z0-9])(\\d{1,2})" + SEP + MONTH + SEP + "((?:19|20)\\d{2})(?![A-Z0-9])");

    public static final class Candidate {
        public final LocalDate date;
        public final boolean birthLabel;
        private Candidate(LocalDate date, boolean birthLabel) {
            this.date = date;
            this.birthLabel = birthLabel;
        }
    }

    public static List<Candidate> extract(String text, LocalDate today) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT)
                .replace('\u2013', '-').replace('\u2014', '-');
        String[] lines = normalized.split("\\R");
        Map<LocalDate, Candidate> dates = new LinkedHashMap<>();
        boolean precedingBirth = false;
        boolean precedingOther = false;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            Matcher birth = BIRTH.matcher(line);
            boolean labeled = birth.find();
            Matcher other = OTHER.matcher(line);
            // Mixed columns are deliberately left for manual review, never prioritized.
            boolean hasOther = other.find();
            boolean trustedLabel = (labeled || precedingBirth) && !hasOther;
            if ((!hasOther && !precedingOther) || labeled) {
                String value = labeled && !hasOther ? line.substring(birth.end()) : line;
                parseLine(value, today, trustedLabel, dates);
            }
            precedingBirth = labeled && !hasOther && !containsDate(line, today);
            precedingOther = hasOther && !labeled && !containsDate(line, today);
        }
        List<Candidate> result = new ArrayList<>(dates.values());
        result.sort((a, b) -> Boolean.compare(b.birthLabel, a.birthLabel));
        return result;
    }

    private static boolean containsDate(String line, LocalDate today) {
        Map<LocalDate, Candidate> dates = new LinkedHashMap<>();
        parseLine(line, today, false, dates);
        return !dates.isEmpty();
    }

    private static void parseLine(String line, LocalDate today, boolean labeled,
                                  Map<LocalDate, Candidate> dates) {
        Matcher m = YMD.matcher(line);
        while (m.find()) add(dates, number(m, 1), number(m, 2), number(m, 3), today, labeled);
        m = DMY.matcher(line);
        while (m.find()) {
            // Both interpretations survive when day/month order is ambiguous.
            add(dates, number(m, 3), number(m, 2), number(m, 1), today, labeled);
            add(dates, number(m, 3), number(m, 1), number(m, 2), today, labeled);
        }
        m = Y_TEXT_M_D.matcher(line);
        while (m.find()) add(dates, number(m, 1), month(m.group(2)), number(m, 3), today, labeled);
        m = D_TEXT_M_Y.matcher(line);
        while (m.find()) add(dates, number(m, 3), month(m.group(2)), number(m, 1), today, labeled);
    }

    private static int number(Matcher m, int group) { return Integer.parseInt(m.group(group)); }

    private static int month(String value) {
        String[][] names = {
                {"JAN", "JANUARY", "JANVIER", "JANV", "JA"},
                {"FEB", "FEBRUARY", "FEV", "FEVRIER", "FEVR", "FE"},
                {"MAR", "MARCH", "MARS", "MR"},
                {"APR", "APRIL", "AVR", "AVRIL", "AL"},
                {"MAY", "MAI", "MA"}, {"JUN", "JUNE", "JUIN", "JN"},
                {"JUL", "JULY", "JUIL", "JUILLET", "JL"},
                {"AUG", "AUGUST", "AOU", "AOUT", "AU"},
                {"SEP", "SEPT", "SEPTEMBER", "SEPTEMBRE", "SE"},
                {"OCT", "OCTOBER", "OCTOBRE", "OC"},
                {"NOV", "NOVEMBER", "NOVEMBRE", "NO"},
                {"DEC", "DECEMBER", "DECEMBRE", "DE"}
        };
        for (int i = 0; i < names.length; i++) {
            for (String name : names[i]) if (name.equals(value)) return i + 1;
        }
        return 0;
    }

    private static void add(Map<LocalDate, Candidate> dates, int year, int month, int day,
                            LocalDate today, boolean labeled) {
        try {
            LocalDate date = LocalDate.of(year, month, day);
            if (!isPlausible(date, today)) return;
            Candidate existing = dates.get(date);
            dates.put(date, new Candidate(date, labeled || (existing != null && existing.birthLabel)));
        } catch (DateTimeException ignored) { /* Never repair OCR digits or impossible dates. */ }
    }

    public static boolean isPlausible(LocalDate birth, LocalDate today) {
        return !birth.isAfter(today) && !birth.isBefore(today.minusYears(120));
    }

    /** Completed calendar years; a Feb 29 birth advances on March 1 in a non-leap year. */
    public static int age(LocalDate birth, LocalDate today) {
        if (!isPlausible(birth, today)) throw new IllegalArgumentException("Invalid birth date");
        return Period.between(birth, today).getYears();
    }
}
