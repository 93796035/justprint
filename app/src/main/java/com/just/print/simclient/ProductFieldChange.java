package com.just.print.simclient;

import org.json.JSONObject;

/** Semantic comparison used before an edit field starts an automatic sync. */
public final class ProductFieldChange {
    private ProductFieldChange() {}

    public static boolean changed(JSONObject product, String key, String text, boolean price) {
        return changed(product, key, text, price, false);
    }

    public static boolean changed(JSONObject product, String key, String text, boolean price, boolean name) {
        if (product == null || !product.has(key)) return false;
        String value = text == null ? "" : text.trim();
        if (name) return !value.equals(product.optString(key, "").trim());
        try {
            if (price) {
                if (value.isEmpty()) return !product.isNull(key);
                if (product.isNull(key)) return true;
                return ProductCatalog.moneyCents(value) != product.getInt(key);
            }
            return !value.matches("-?[0-9]+") || Integer.parseInt(value) != product.getInt(key);
        } catch (Exception invalidOrOutOfRange) {
            // Let the normal save validation attach the precise error to the field.
            return true;
        }
    }
}
