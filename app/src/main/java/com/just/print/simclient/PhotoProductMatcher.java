package com.just.print.simclient;

import java.text.Normalizer;
import java.util.*;

/** Local OCR-to-catalog matching; fuzzy matches always require the user's choice. */
public final class PhotoProductMatcher {
    private PhotoProductMatcher() {}
    public static final class Item {
        public final int id;
        public final String name, category, code;
        public final boolean preferred;
        public Item(int id, String name, String category, String code, boolean preferred) {
            this.id = id; this.name = name; this.category = category; this.code = code; this.preferred = preferred;
        }
    }
    public static final class Match {
        public final Item item;
        public final boolean exact;
        private final double score;
        private Match(Item item, boolean exact, double score) {
            this.item = item; this.exact = exact; this.score = score;
        }
    }
    private static final Set<String> PACKAGING = new HashSet<>(Arrays.asList(
            "BEER", "BIERE", "BIERES", "CAN", "CANS", "CANETTE", "CANETTES", "BTL", "BOTTLE", "BOTTLES",
            "BOUTEILLE", "BOUTEILLES", "PACK", "PK", "CASE", "CAISSE", "ML", "CL", "L", "OZ", "X"));
    private static final Set<String> GENERIC = new HashSet<>(Arrays.asList(
            "LIGHT", "LITE", "LAGER", "STRONG", "MALT", "LIQUOR", "ALCOHOL", "ALCOOL", "VOL", "THE", "DE", "LA"));

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT).replaceAll("(?<=[A-Z])(?=[0-9])|(?<=[0-9])(?=[A-Z])", " ")
                .replaceAll("[^A-Z0-9]+", " ").trim();
    }
    private static Set<String> words(String value) {
        Set<String> result = new LinkedHashSet<>(Arrays.asList(normalize(value).split("\\s+")));
        result.remove("");
        return result;
    }
    private static Set<String> identity(String name) {
        String value = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
        value = value.replaceAll("\\b\\d+\\s*[X×]\\s*\\d+(?:[.,]\\d+)?\\s*(?:ML|CL|L|OZ)\\b", " ")
                .replaceAll("\\b\\d+(?:[.,]\\d+)?\\s*(?:ML|CL|L|OZ|PK|PACK|CANS|CANETTES)\\b", " ")
                .replaceAll("\\b\\d+(?:[.,]\\d+)?\\s*%", " ");
        Set<String> result = words(value);
        result.removeAll(PACKAGING);
        return result;
    }
    public static List<Match> match(String photoText, List<Item> products) {
        Set<String> seen = words(photoText);
        List<Match> all = new ArrayList<>(), preferred = new ArrayList<>();
        for (Item item : products) {
            Set<String> tokens = identity(item.name);
            if (tokens.isEmpty()) continue;
            int exact = 0, fuzzy = 0;
            boolean brand = false;
            for (String token : tokens) {
                boolean hit = seen.contains(token);
                boolean approximate = !hit && token.matches("[A-Z]{4,}")
                        && seen.stream().anyMatch(word -> oneEdit(token, word));
                if (hit) exact++;
                else if (approximate) fuzzy++;
                if ((hit || approximate) && token.matches("[A-Z]{2,}") && !GENERIC.contains(token)) brand = true;
            }
            double coverage = (exact + fuzzy * 0.65) / tokens.size();
            if (!brand || coverage < 0.45) continue;
            Match match = new Match(item, exact == tokens.size(), coverage);
            all.add(match);
            if (item.preferred) preferred.add(match);
        }
        List<Match> matches = preferred.isEmpty() ? all : preferred;
        matches.sort((a, b) -> {
            int result = Double.compare(b.score, a.score);
            return result != 0 ? result : a.item.name.compareToIgnoreCase(b.item.name);
        });
        return matches;
    }
    private static boolean oneEdit(String a, String b) {
        if (b.length() < 3 || Math.abs(a.length() - b.length()) > 1) return false;
        int i = 0, j = 0, edits = 0;
        while (i < a.length() && j < b.length()) {
            if (a.charAt(i) == b.charAt(j)) { i++; j++; continue; }
            if (++edits > 1) return false;
            if (a.length() >= b.length()) i++;
            if (b.length() >= a.length()) j++;
        }
        return edits + (a.length() - i) + (b.length() - j) == 1;
    }
}
