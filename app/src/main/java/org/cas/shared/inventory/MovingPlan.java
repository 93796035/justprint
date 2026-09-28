package org.cas.shared.inventory;

import org.json.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Pure local BMoving calculation. Input contains product facts and daily sales, never a plan. */
public final class MovingPlan {
    private MovingPlan() {}
    public static JSONObject generate(JSONObject input, JSONObject previous) throws Exception {
        Map<String, List<JSONObject>> groups = groups(input);
        Map<String, JSONObject> old = new LinkedHashMap<>();
        if (previous != null) {
            JSONArray rows = previous.getJSONArray("rows");
            for (int i = 0; i < rows.length(); i++) old.put(rows.getJSONObject(i).getString("group"), rows.getJSONObject(i));
        }
        List<JSONObject> result = new ArrayList<>();
        for (Map.Entry<String, List<JSONObject>> entry : groups.entrySet()) {
            String key = entry.getKey();
            JSONObject saved = old.remove(key);
            // Actual completed work is fixed; only unfinished suggestions are recalculated.
            if (saved != null && (saved.getString("state").equals("done") || saved.optBoolean("manualQuantity"))) {
                result.add(new JSONObject(saved.toString())); continue;
            }
            JSONObject best = null;
            long shortage = 0;
            TreeMap<String, Long> sales = new TreeMap<>();
            JSONArray shortageBreakdown = new JSONArray();
            for (JSONObject p : entry.getValue()) {
                JSONObject facts = p.getJSONObject("moving");
                long shortageCount = Math.max(0L, p.getLong("monque"));
                long shortageUnits = Math.multiplyExact(shortageCount, facts.getLong("shortageBottles"));
                shortage = Math.addExact(shortage, shortageUnits);
                if (shortageCount > 0) shortageBreakdown.put(new JSONObject()
                        .put("name", facts.getString("name"))
                        .put("count", shortageCount)
                        .put("unitsPerItem", facts.getLong("shortageBottles"))
                        .put("units", shortageUnits));
                JSONObject days = p.optJSONObject("movingSales");
                if (days != null) for (Iterator<String> it = days.keys(); it.hasNext();) {
                    String date = it.next();
                    long bottles = Math.multiplyExact(days.getLong(date), facts.getLong("saleBottles"));
                    if (bottles > 0) sales.put(date, Math.addExact(sales.containsKey(date) ? sales.get(date) : 0L, bottles));
                }
                int priority = facts.getInt("priority");
                int units = facts.getInt("unitsPerCase");
                if (priority == Integer.MAX_VALUE || p.getLong("stock3") < units) continue;
                if (best == null || priority < best.getJSONObject("moving").getInt("priority")) best = p;
            }
            JSONObject candidate = null;
            if (best != null) {
                JSONObject facts = best.getJSONObject("moving");
                int caseBottles = facts.getInt("caseBottles"), units = facts.getInt("unitsPerCase");
                long cases = shortage / caseBottles;
                long recentSales = sales.isEmpty() ? 0 : sales.lastEntry().getValue();
                long stockBottles = Math.multiplyExact(Math.max(0L, best.getLong("stock1")), Math.max(1, caseBottles / units));
                if (Math.addExact(stockBottles, Math.multiplyExact(cases, caseBottles)) < recentSales) cases = Math.max(1, cases);
                cases = Math.min(cases, Math.max(0L, best.getLong("stock3")) / units);
                if (cases > 0) candidate = new JSONObject().put("group", key).put("productId", best.getInt("id"))
                        .put("name", facts.getString("name")).put("code", best.optString("code"))
                        .put("cases", cases).put("unitsPerCase", units).put("units", Math.multiplyExact(cases, units))
                        .put("shortage", shortage).put("shortageBreakdown", shortageBreakdown).put("state", "pending");
            }
            if (saved != null && saved.getString("state").equals("skipped")) {
                if (candidate == null) candidate = new JSONObject(saved.toString());
                candidate.put("state", "skipped");
            }
            if (candidate != null) result.add(candidate);
        }
        // Never discard recorded work or exclusions because a product disappeared from inputs.
        for (JSONObject row : old.values()) if (!row.getString("state").equals("pending") || row.optBoolean("manualQuantity")) result.add(new JSONObject(row.toString()));
        Collections.sort(result, (a, b) -> {
            int quantity = Long.compare(b.optLong("cases"), a.optLong("cases"));
            if (quantity != 0) return quantity;
            int manque = Long.compare(b.optLong("shortage"), a.optLong("shortage"));
            return manque != 0 ? manque : a.optString("name").compareToIgnoreCase(b.optString("name"));
        });
        JSONArray rows = new JSONArray(); for (JSONObject row : result) rows.put(row);
        return new JSONObject().put("draftId", previous == null ? UUID.randomUUID().toString() : previous.getString("draftId"))
                .put("rows", rows).put("calculatedAt", input.optString("capturedAt"));
    }

    public static Map<String, List<JSONObject>> groups(JSONObject input) throws Exception {
        Map<String, List<JSONObject>> groups = new LinkedHashMap<>();
        JSONArray products = input.getJSONArray("products");
        for (int i = 0; i < products.length(); i++) {
            JSONObject p = products.getJSONObject(i);
            JSONObject facts = p.getJSONObject("moving");
            if (!facts.getBoolean("eligible")) continue;
            String group = facts.getString("group");
            if (!groups.containsKey(group)) groups.put(group, new ArrayList<JSONObject>());
            groups.get(group).add(p);
        }
        // Windows inserts sold variants first, then the remaining products, in category/name/ID order.
        for (List<JSONObject> variants : groups.values()) Collections.sort(variants, (a, b) -> {
            JSONObject aSales = a.optJSONObject("movingSales"), bSales = b.optJSONObject("movingSales");
            int sold = Boolean.compare(bSales != null && bSales.length() > 0, aSales != null && aSales.length() > 0);
            if (sold != 0) return sold;
            int category = a.optString("category", "").compareTo(b.optString("category", ""));
            if (category != 0) return category;
            int name = orderName(a).compareTo(orderName(b));
            return name != 0 ? name : Integer.compare(a.optInt("id"), b.optInt("id"));
        });
        return groups;
    }
    private static String orderName(JSONObject p) {
        String name = !p.isNull("mnemonic") ? p.optString("mnemonic") : !p.isNull("subject") ? p.optString("subject") : p.optString("code");
        return name.trim().toUpperCase(Locale.ROOT);
    }

    public static String version(List<JSONObject> products) throws Exception {
        if (products == null) return "missing";
        List<JSONObject> ordered = new ArrayList<>(products);
        Collections.sort(ordered, (a, b) -> Integer.compare(a.optInt("id"), b.optInt("id")));
        JSONArray values = new JSONArray();
        for (JSONObject p : ordered) {
            JSONObject f = p.getJSONObject("moving");
            values.put(new JSONArray().put(p.getInt("id")).put(p.getLong("stock1")).put(p.getLong("stock2"))
                    .put(p.getLong("stock3")).put(p.getLong("monque")).put(p.optString("category"))
                    .put(f.getString("name")).put(f.getInt("unitsPerCase")).put(f.getInt("caseBottles"))
                    .put(f.getInt("shortageBottles")).put(f.getString("group")));
        }
        return hash(values.toString());
    }

    public static JSONObject command(JSONObject input, JSONObject draft) throws Exception {
        Map<String, List<JSONObject>> groups = groups(input);
        JSONObject request = new JSONObject(draft.toString()).put("action", "submit");
        JSONArray rows = request.getJSONArray("rows");
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            row.put("expected", version(groups.get(row.getString("group"))));
        }
        return request;
    }
    public static String hash(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) result.append(String.format(Locale.ROOT, "%02x", b & 255));
        return result.toString();
    }
}
