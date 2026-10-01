package com.just.print.simclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/** Read-only comparison of phone and server product snapshots. */
final class ProductSyncDiff {
    static final class Item {
        final JSONObject server;
        final JSONObject local;
        final boolean serverNewer;
        final boolean localNewer;
        Item(JSONObject server, JSONObject local, boolean serverNewer, boolean localNewer) {
            this.server = server; this.local = local;
            this.serverNewer = serverNewer; this.localNewer = localNewer;
        }
    }

    private static final List<String> FIELDS = Arrays.asList(
            "subject", "mnemonic", "code", "category", "unit", "cost", "price",
            "stock", "stock1", "stock2", "stock3", "monque", "weekly_sales_qty",
            "monthly_sales_qty", "case_qty", "deleted", "last_modified", "server_modified");

    private ProductSyncDiff() {}

    static List<Item> compare(JSONArray server, JSONArray local) throws Exception {
        Map<String, JSONObject> servers = index(server);
        Map<String, JSONObject> locals = index(local);
        Map<String, Boolean> seen = new HashMap<>();
        List<Item> result = new ArrayList<>();
        for (String id : servers.keySet()) {
            JSONObject s = servers.get(id), l = locals.get(id);
            if (l == null || different(s, l)) {
                result.add(new Item(s, l, time(s) > time(l), time(l) > time(s)));
            }
            seen.put(id, true);
        }
        for (String id : locals.keySet()) if (!seen.containsKey(id))
            result.add(new Item(null, locals.get(id), false, true));
        return result;
    }

    private static Map<String, JSONObject> index(JSONArray rows) throws Exception {
        Map<String, JSONObject> result = new HashMap<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            long localId = row.optLong("local_id", 0);
            String key = localId > 0 ? "id:" + localId : "code:" + row.optString("code", "");
            if (key.length() <= 5) throw new IllegalArgumentException("Product has neither local_id nor code");
            result.put(key, row);
        }
        return result;
    }

    private static boolean different(JSONObject a, JSONObject b) {
        for (String field : FIELDS) {
            Object av = a.opt(field), bv = b.opt(field);
            if (av == JSONObject.NULL) av = null;
            if (bv == JSONObject.NULL) bv = null;
            if (av == null ? bv != null : !String.valueOf(av).equals(String.valueOf(bv))) return true;
        }
        return false;
    }

    private static long time(JSONObject row) {
        if (row == null) return Long.MIN_VALUE;
        String value = row.optString("server_modified", row.optString("last_modified", ""));
        try { return Long.parseLong(value); } catch (Exception ignored) { }
        String[] formats = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX"};
        for (String format : formats) try {
            SimpleDateFormat parser = new SimpleDateFormat(format, java.util.Locale.ROOT);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            return parser.parse(value).getTime();
        } catch (Exception ignored) { }
        return Long.MIN_VALUE;
    }
}
