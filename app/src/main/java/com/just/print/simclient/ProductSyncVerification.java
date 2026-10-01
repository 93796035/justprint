package com.just.print.simclient;

import java.io.IOException;
import java.util.Iterator;
import org.json.JSONObject;

/** Check a fresh server read, rather than trusting a successful POST status. */
public final class ProductSyncVerification {
    private ProductSyncVerification() {}

    public static void verify(JSONObject request, JSONObject product) throws Exception {
        if (product.getInt("id") != request.getInt("productId"))
            throw new IOException("Sync could not be verified: server returned a different product.");
        JSONObject values = request.getJSONObject("values");
        Iterator<String> keys = values.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            boolean matches;
            if (values.get(key) instanceof String)
                matches = !product.isNull(key) && values.getString(key).equals(product.optString(key));
            else
                matches = !product.isNull(key) && product.getLong(key) == values.getLong(key);
            if (!matches) throw new IOException("Sync could not be verified: " + key + " requested "
                    + values.get(key) + ", server has " + product.opt(key) + ".");
        }
    }
}
