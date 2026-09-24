package com.just.print.simclient;

import java.util.*;
import java.util.regex.*;

/** Barcode identity first; incomplete printed digits always need confirmation. */
public final class BarcodeProductMatcher {
    private BarcodeProductMatcher() {}
    public static final class Match {
        public final PhotoProductMatcher.Item item;
        public final boolean exact;
        Match(PhotoProductMatcher.Item item, boolean exact) { this.item = item; this.exact = exact; }
    }
    private static String digits(String value) {
        String clean = value == null ? "" : value.replaceAll("[\\s-]", "");
        return clean.matches("[0-9]{6,14}") ? clean : "";
    }
    private static String canonical(String value) {
        // UPC-A and its EAN-13 representation encode the same item.
        return value.length() == 13 && value.startsWith("0") ? value.substring(1) : value;
    }
    public static List<String> printedCodes(String text) {
        Set<String> codes = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("(?<![\\p{L}0-9])[0-9](?:[ \\t]*[0-9]){5,13}(?![ \\t]*[0-9]|\\p{L})").matcher(text);
        while (matcher.find()) codes.add(digits(matcher.group()));
        return new ArrayList<>(codes);
    }
    public static List<Match> match(List<String> observed, List<PhotoProductMatcher.Item> products) {
        List<Match> result = new ArrayList<>();
        boolean hasExact = false;
        for (PhotoProductMatcher.Item item : products) {
            String code = digits(item.code);
            if (code.isEmpty()) continue;
            boolean exact = false, partial = false;
            for (String raw : observed) {
                String seen = digits(raw);
                if (seen.isEmpty()) continue;
                if (canonical(code).equals(canonical(seen))) exact = true;
                else if (seen.length() < code.length()) {
                    int start = code.indexOf(seen), missing = code.length() - seen.length();
                    if (start >= 0 && start <= 2 && missing - start <= 2 && missing <= 3) partial = true;
                }
            }
            if (exact || partial) result.add(new Match(item, exact));
            hasExact |= exact;
        }
        if (hasExact) result.removeIf(match -> !match.exact);
        result.sort(Comparator.comparing((Match match) -> !match.item.preferred)
                .thenComparing(match -> match.item.name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }
}
