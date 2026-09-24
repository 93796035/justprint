import com.just.print.simclient.ProductCatalog;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProductCatalogCheck {
    public static void main(String[] args) throws Exception {
        JSONObject a = row(1, "BEER Alpha", 1), b = row(2, "BEER Alpha", 8);
        ProductCatalog original = new ProductCatalog(new JSONArray().put(a).put(b));
        check(original.search("alpha").size() == 2, "Case-insensitive substring search");
        check(original.search("missing").isEmpty(), "No match");
        ProductCatalog updated = original.updated(row(1, "BEER Alpha", 3));
        check(original.search("alpha").get(0).row().getInt("stock1") == 1, "Original snapshot remains unchanged");
        // Exact bytes handed to persistent storage, then reconstructed on application startup.
        java.nio.file.Path file = java.nio.file.Files.createTempFile("echo-catalog-check", ".json");
        try {
            java.nio.file.Files.write(file, updated.serialize().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            ProductCatalog restarted = new ProductCatalog(new JSONArray(new String(java.nio.file.Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8)));
            check(restarted.search("alpha").get(0).row().getInt("stock1") == 3, "Saved quantity survives reload");
            check(restarted.search("alpha").get(1).row().getInt("stock1") == 8, "Same-name product is unchanged");
            check(restarted.search("alpha").get(0).row().getString("extra").equals("kept"), "Preserve other product fields");
        } finally { java.nio.file.Files.deleteIfExists(file); }
        try { new ProductCatalog(new JSONArray().put(a).put(a)); throw new AssertionError("Duplicate accepted"); }
        catch (IllegalArgumentException expected) {}
        try { original.updated(row(99, "unknown", 1)); throw new AssertionError("Unknown update accepted"); }
        catch (IllegalArgumentException expected) {}
        try { new ProductCatalog(new JSONArray().put(row(3, "invalid", 1).put("stock2", "bad"))); throw new AssertionError("Invalid stock accepted"); }
        catch (IllegalArgumentException expected) {}
        check(new ProductCatalog(new JSONArray()).size() == 0, "Empty refresh supported");
        ProductCatalog beers = new ProductCatalog(new JSONArray()
                .put(row(10, "BUDWEISER 355", 1)).put(row(11, "BUDWEISER 473", 1)));
        check(ProductCatalog.complete("bud", beers.search("bud")).equals("budWEISER "), "Complete shared letters and space, preserve typed case");
        check(ProductCatalog.complete("WEI", beers.search("WEI")).equals("WEISER "), "Substring completion");
        check(ProductCatalog.complete("BUDWEISER ", beers.all()).equals("BUDWEISER "), "Stop at differing sizes");
        check(ProductCatalog.complete("355", beers.search("355")).equals("355"), "Already complete");
        check(ProductCatalog.complete("BUDWEISER 3", beers.search("BUDWEISER 3")).equals("BUDWEISER 355"), "One match");
        check(ProductCatalog.complete("", beers.all()).equals(""), "Empty input");
        check(ProductCatalog.complete("missing", beers.search("missing")).equals("missing"), "No matches");
        ProductCatalog ambiguous = new ProductCatalog(new JSONArray().put(row(12, "ABA ABX", 1)));
        check(ProductCatalog.complete("AB", ambiguous.all()).equals("AB"), "Repeated substring must agree at every occurrence");
        ProductCatalog shorter = new ProductCatalog(new JSONArray().put(row(13, "BEER", 1)).put(row(14, "BEER LIGHT", 1)));
        check(ProductCatalog.complete("B", shorter.all()).equals("BEER"), "Do not extend past shortest name");
        ProductCatalog mixed = new ProductCatalog(new JSONArray()
                .put(row(20, "BUDWEISER 355", 1).put("category", "BIÈRE").put("price",1299).put("cost",875))
                .put(row(21, "BUD glass", 1).put("category", "SUPPLIES"))
                .put(row(22, "", 1).put("category", JSONObject.NULL).put("subject", "Fallback")));
        check(mixed.categories().size() == 3 && mixed.defaultCategory().equals("BIÈRE"), "Categories and default beer");
        check(mixed.search("BUD", "BIÈRE").size() == 1, "Category isolates search");
        check(ProductCatalog.complete("BUD", mixed.search("BUD", "BIÈRE")).equals("BUDWEISER 355"), "Completion respects category");
        check(mixed.search("Fallback", "").size() == 1, "Uncategorized and fallback name");
        ProductCatalog restored = new ProductCatalog(new JSONArray(mixed.serialize()));
        check(restored.search("BUD", "BIÈRE").get(0).row().getInt("price") == 1299
                && restored.search("BUD", "BIÈRE").get(0).row().getInt("cost") == 875, "Prices survive persistence");
        JSONObject priced = restored.search("BUD", "BIÈRE").get(0).row();
        check(ProductCatalog.money(priced, "price").equals("12.99") && ProductCatalog.money(priced, "cost").equals("8.75"), "Persisted cents display as money");
        check(ProductCatalog.money(new JSONObject().put("price", 0), "price").equals("0.00"), "Zero price is displayed");
        check(ProductCatalog.money(new JSONObject(), "price").equals("—")
                && ProductCatalog.money(new JSONObject().put("price", JSONObject.NULL), "price").equals("—"), "Missing price is not zero");
        check(ProductCatalog.moneyCents("12.99") == 1299 && ProductCatalog.moneyCents("8,75") == 875
                && ProductCatalog.moneyCents("0") == 0 && ProductCatalog.moneyCents("21474836.47") == Integer.MAX_VALUE, "Exact decimal price input");
        for (String invalid : new String[]{"", "-1", "1.001", "NaN", "21474836.48"}) {
            try { ProductCatalog.moneyCents(invalid); throw new AssertionError("Invalid price accepted: " + invalid); }
            catch (IllegalArgumentException expected) {}
        }
        JSONArray groupedRows = new JSONArray();
        String[] groups = {"BIÈRE", "LABATT", "MOLSON", "SLEEMAN", "POPPERS", "TABAC", "IMPERIAL", "JTI MD", "BENSON", "SUPPLIES",
                "TABAC KIT", "EPICERIE TAX", "FRITO", "QUECAN", "MONDOUX", "BREYERS", "EPICERIE", "LAIT", "PAIN", "INSTANT", "LOTO"};
        for (int i = 0; i < groups.length; i++) groupedRows.put(row(100+i, "ITEM " + i, 1).put("category", groups[i]));
        ProductCatalog grouped = new ProductCatalog(groupedRows);
        check(grouped.search("ITEM", "BIERE").size() == 5, "Beer includes all five categories");
        check(grouped.search("ITEM", "BIA").size() == 5, "Beer root alias");
        check(grouped.search("ITEM", "Tobacco").size() == 5, "Tobacco includes TABAC and brands and kits");
        check(grouped.search("ITEM", "EPICERIE TAX").size() == 5, "Taxable grocery row");
        check(grouped.search("ITEM", "EPICERIE").size() == 5, "Grocery row");
        check(grouped.search("ITEM", "JTI MD").size() == 1, "Tobacco child stays narrow");
        check(grouped.search("ITEM", "PAIN").size() == 1, "Grocery child stays narrow");
        check(grouped.categories().subList(0, 10).equals(java.util.Arrays.asList("BIÈRE", "MOLSON", "LABATT", "SLEEMAN", "POPPERS",
                "TABAC", "IMPERIAL", "BENSON", "JTI MD", "TABAC KIT")), "Menu category order");
        check(ProductCatalog.categoryColor("BIÈRE") == 0xffffd700 && ProductCatalog.categoryColor("TABAC") == 0xff6495ed,
                "Desktop primary category colors");
        check(ProductCatalog.categoryColor("MOLSON") == 0, "Child category has neutral background");
        check(grouped.search("ITEM", "LABATT").size() == 1, "Individual brand remains narrow");
        check(grouped.search("ITEM", "SUPPLIES").size() == 1, "Other categories remain isolated");
        check(ProductCatalog.complete("IT", grouped.search("IT", "BIERE")).equals("ITEM "), "Completion uses entire group");
        System.out.println("PASS: category groups, completion, local matching, update by ID, persisted reload and validation.");
    }
    private static JSONObject row(int id, String name, int stock) throws Exception {
        return new JSONObject().put("id", id).put("mnemonic", name).put("stock1", stock)
                .put("stock2", 2).put("stock3", 4).put("monque", -1).put("extra", "kept");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
