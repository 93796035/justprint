package com.just.print.simclient;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/** Validated, immutable snapshots shared by local search and durable storage. */
public final class ProductCatalog {
    interface Progress {
        void update(int completed, int total);
    }
    public static int moneyCents(String text) {
        if (!text.matches("[0-9]+(?:[.,][0-9]{1,2})?"))
            throw new IllegalArgumentException("Enter a non-negative amount with up to 2 decimal places");
        try { return new java.math.BigDecimal(text.replace(',', '.')).movePointRight(2).intValueExact(); }
        catch (ArithmeticException ex) { throw new IllegalArgumentException("Amount is out of range"); }
    }
    /** POS stores prices as integer cents; missing old-cache values are not zero prices. */
    public static String money(JSONObject row, String key) {
        Object value = row.opt(key);
        if (!(value instanceof Number) || !value.toString().matches("-?[0-9]+")) return "—";
        return new java.math.BigDecimal(value.toString()).movePointLeft(2).setScale(2).toPlainString();
    }
    public static final class Product {
        public final int id;
        public final String name, code, category;
        private final String json, searchName;
        private Product(JSONObject row) throws Exception {
            int storedLocalId = row.optInt("local_id", 0);
            int storedId = row.optInt("id", 0);
            id = storedLocalId > 0 ? storedLocalId : storedId > 0 ? storedId : stableId(row);
            if (id <= 0) throw new IllegalArgumentException("Product has no usable local ID");
            // Normalize the two historical names once at the cache boundary.
            row.put("id", id);
            String mnemonic = row.optString("mnemonic", "").trim();
            String subject = row.optString("subject", "").trim();
            name = !mnemonic.isEmpty() ? mnemonic : !subject.isEmpty() ? subject : row.optString("code", "") ;
            category = row.optString("category", "").trim();
            code = row.optString("code");
            for (String key : new String[]{"stock1", "stock2", "stock3", "monque"}) {
                // Older/server-side product rows may omit an unused stock
                // column. Treat a missing stock value as zero so one sparse
                // row cannot invalidate the entire downloaded catalog.
                if (!row.has(key) || row.isNull(key)) row.put(key, 0);
                Object value = row.get(key);
                if (!(value instanceof Number) || !value.toString().matches("-?[0-9]+")
                        || ((Number) value).longValue() < Integer.MIN_VALUE
                        || ((Number) value).longValue() > Integer.MAX_VALUE)
                    throw new IllegalArgumentException("Invalid stock value in product data");
            }
            json = row.toString();
            searchName = name.toUpperCase(Locale.ROOT);
        }
        private static int stableId(JSONObject row) {
            String identity = row.optString("code", "").trim();
            if (identity.isEmpty()) identity = row.optString("subject", "").trim();
            if (identity.isEmpty()) throw new IllegalArgumentException("Product has no code or subject for local ID");
            int value = identity.hashCode() & 0x7fffffff;
            return value == 0 ? 1 : value;
        }
        public JSONObject row() throws Exception { return new JSONObject(json); }
        @Override public String toString() { return name + "  [" + (code.isEmpty() ? id : code) + "]"; }
    }
    private final List<Product> products = new ArrayList<>();
    public ProductCatalog(JSONArray rows) throws Exception {
        this(rows, null);
    }

    public ProductCatalog(JSONArray rows, Progress progress) throws Exception {
        HashSet<Integer> ids = new HashSet<>();
        for (int i = 0; i < rows.length(); i++) {
            Product product;
            try { product = new Product(rows.getJSONObject(i)); }
            catch (Exception ex) {
                throw new IllegalArgumentException("Invalid product at row " + (i + 1) + "/" + rows.length() + ": " + ex.getMessage(), ex);
            }
            if (!ids.add(product.id)) throw new IllegalArgumentException("Duplicate product ID at row " + (i + 1) + "/" + rows.length() + ": " + product.id);
            products.add(product);
            if (progress != null) progress.update(i + 1, rows.length());
        }
    }
    public int size() { return products.size(); }
    public List<Product> all() { return new ArrayList<>(products); }
    public List<Product> search(String query) {
        return search(query, null);
    }
    public List<String> categories() {
        java.util.TreeSet<String> names = new java.util.TreeSet<>();
        for (Product product : products) names.add(product.category);
        List<String> ordered = new ArrayList<>(names);
        java.util.Collections.sort(ordered, (a, b) -> {
            int rank = Integer.compare(categoryRank(a), categoryRank(b));
            return rank != 0 ? rank : a.compareToIgnoreCase(b);
        });
        return ordered;
    }
    private static int categoryRank(String name) {
        String key = normalizeCategory(name);
        if (beerRoot(key)) return 0;
        if (tobaccoRoot(key)) return 5;
        if (key.equals("JTI") || key.equals("GTI") || key.equals("GTI MD")) key = "JTI MD";
        String[] order = {"BIERE", "MOLSON", "LABATT", "SLEEMAN", "POPPERS",
                "TABAC", "IMPERIAL", "BENSON", "JTI MD", "TABAC KIT",
                "EPICERIE TAX", "FRITO", "QUECAN", "MONDOUX", "BREYERS",
                "EPICERIE", "LAIT", "PAIN", "INSTANT", "LOTO", "OTHER"};
        for (int i = 0; i < order.length; i++) if (order[i].equals(key)) return i;
        return order.length;
    }
    public static int categoryColor(String name) {
        String key = normalizeCategory(name);
        // Colours identify menu roots only. Brand/product subcategories stay neutral.
        if (beerRoot(key)) return 0xffffd700;
        if (tobaccoRoot(key)) return 0xff6495ed;
        if (key.equals("EPICERIE TAX")) return 0xffdc143c;
        if (key.equals("EPICERIE")) return 0xff228b22;
        return 0;
    }
    public String defaultCategory() {
        List<String> names = categories();
        for (String name : names) {
            String normalized = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
            if (normalized.contains("BEER") || normalized.contains("BIERE") || normalized.contains("啤酒")) return name;
        }
        return names.isEmpty() ? null : names.get(0);
    }
    public List<Product> search(String query, String category) {
        List<Product> matches = new ArrayList<>();
        String term = query.trim().toUpperCase(Locale.ROOT);
        if (!term.isEmpty()) for (Product product : products) {
            if (matchesCategory(category, product.category) && product.searchName.contains(term)) matches.add(product);
        }
        return matches;
    }
    private static String normalizeCategory(String category) {
        return java.text.Normalizer.normalize(category.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
    private static boolean beerRoot(String category) {
        return category.equals("BIERE") || category.equals("BIA") || category.equals("BEER") || category.equals("啤酒");
    }
    private static boolean tobaccoRoot(String category) {
        return category.equals("TOBACCO") || category.equals("TABAC");
    }
    static boolean matchesCategory(String selected, String actual) {
        if (selected == null) return true;
        String root = normalizeCategory(selected), child = normalizeCategory(actual);
        if (root.equals(child)) return true;
        if (beerRoot(root)) return beerRoot(child) || child.equals("LABATT") || child.equals("MOLSON")
                || child.equals("SLEEMAN") || child.equals("POPPERS");
        if (tobaccoRoot(root)) return tobaccoRoot(child) || child.equals("IMPERIAL") || child.equals("BENSON")
                || child.equals("GTI") || child.equals("JTI") || child.equals("GTI MD") || child.equals("JTI MD")
                || child.equals("TABAC KIT");
        // The first column groups the remaining categories on that Menu Panel row.
        if (root.equals("EPICERIE TAX")) return child.equals("FRITO") || child.equals("QUECAN")
                || child.equals("MONDOUX") || child.equals("BREYERS");
        if (root.equals("EPICERIE")) return child.equals("LAIT") || child.equals("PAIN")
                || child.equals("INSTANT") || child.equals("LOTO");
        return false;
    }
    public String serialize() throws Exception {
        JSONArray rows = new JSONArray();
        for (Product product : products) rows.put(product.row());
        return rows.toString();
    }
    /** Extend only text shared after every occurrence in every matching name. */
    public static String complete(String query, List<Product> matches) {
        if (query.trim().isEmpty() || matches.isEmpty()) return query;
        String common = null;
        for (Product product : matches) {
            boolean found = false;
            for (int start = 0; start <= product.name.length() - query.length(); start++) {
                if (!product.name.regionMatches(true, start, query, 0, query.length())) continue;
                found = true;
                String suffix = product.name.substring(start + query.length());
                if (common == null) common = suffix;
                else {
                    int length = 0;
                    while (length < common.length() && length < suffix.length()
                            && common.regionMatches(true, length, suffix, length, 1)) length++;
                    // Never append half of a supplementary Unicode character.
                    if (length > 0 && Character.isHighSurrogate(common.charAt(length - 1))) length--;
                    common = common.substring(0, length);
                }
                if (common.isEmpty()) return query;
            }
            if (!found) return query;
        }
        return common == null ? query : query + common;
    }
    public ProductCatalog updated(JSONObject row) throws Exception {
        Product replacement = new Product(row);
        JSONArray rows = new JSONArray();
        boolean found = false;
        for (Product product : products) {
            if (product.id == replacement.id) {
                JSONObject saved = replacement.row(), previous = product.row();
                if (!saved.has("movingSales") && previous.has("movingSales")) saved.put("movingSales", previous.get("movingSales"));
                rows.put(saved); found = true;
            }
            else rows.put(product.row());
        }
        if (!found) throw new IllegalArgumentException("Updated product is not in the local catalog");
        return new ProductCatalog(rows);
    }
}
