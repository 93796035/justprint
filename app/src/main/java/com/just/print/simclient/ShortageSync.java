package com.just.print.simclient;

import org.json.JSONObject;

/** Keeps the phone's signed shortage while using the website's negative wire format. */
public final class ShortageSync {
    private ShortageSync() { }

    public static JSONObject forWebsite(JSONObject local) throws Exception {
        JSONObject upload = new JSONObject(local.toString());
        if (!upload.has("monque") || upload.isNull("monque"))
            upload.put("monque", upload.optInt("stock1_shortage", 0));
        int shortage = upload.getInt("monque");
        if (shortage > 0) upload.put("monque", -shortage);
        return upload;
    }

    public static int afterSale(int current, int quantity) {
        return afterSale(current, quantity, true);
    }

    public static int afterSale(int current, int quantity, boolean negativeWhenZero) {
        if (quantity <= 0) throw new IllegalArgumentException("Sale quantity must be positive");
        long magnitude = Math.abs((long) current) + quantity;
        if (magnitude > Integer.MAX_VALUE) throw new IllegalArgumentException("Shortage exceeds supported range");
        // Zero has no sign; choose the receiving system's existing convention.
        return current > 0 || (current == 0 && !negativeWhenZero)
                ? (int) magnitude : -(int) magnitude;
    }
}
