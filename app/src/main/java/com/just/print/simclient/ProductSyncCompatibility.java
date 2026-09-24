package com.just.print.simclient;

import org.json.JSONObject;

/** Compatibility with AikaPos inventory endpoints released before price editing. */
public final class ProductSyncCompatibility {
    private static final String[] STOCK_KEYS = {"stock1", "stock2", "stock3", "monque"};
    private ProductSyncCompatibility() {}

    public static boolean needsStockOnlyRetry(int status, String message, JSONObject request) {
        if (status != 400 || request.optJSONObject("values") == null
                || request.optJSONObject("values").length() <= STOCK_KEYS.length) return false;
        String text = message == null ? "" : message.toLowerCase(java.util.Locale.ROOT);
        return text.contains("需要四个库存字段") || text.contains("four inventory fields");
    }

    public static JSONObject stockOnly(JSONObject request) throws Exception {
        JSONObject values = new JSONObject(), expected = new JSONObject();
        JSONObject originalValues = request.getJSONObject("values");
        JSONObject originalExpected = request.getJSONObject("expected");
        for (String key : STOCK_KEYS) {
            values.put(key, originalValues.get(key));
            expected.put(key, originalExpected.get(key));
        }
        return new JSONObject().put("productId", request.getInt("productId"))
                .put("values", values).put("expected", expected);
    }
}
