package com.just.print.simclient;

import org.json.JSONObject;

/**
 * Dedicated Android implementation of the AikaPos stock-plan rule.
 *
 * This class deliberately contains no Android UI or persistence code.  The
 * Windows POS has its own implementation of the same rule; keeping the rule
 * here makes the calculation auditable and prevents it from being duplicated
 * inside StockingPlanPage.
 */
final class StockingPlanCalculator {
    private StockingPlanCalculator() {}

    static int quantity(JSONObject product, String category, int targetWeeks) {
        int savedWeekly = product.optInt("weekly_sales_qty", product.optInt("weeklySalesQty", 0));
        int lastSevenDays = product.optInt("lastSevenDaysQty", 0);
        int weekly = Math.max(savedWeekly, lastSevenDays);
        JSONObject sales = product.optJSONObject("movingSales");
        if (sales != null) {
            int recent = 0;
            for (java.util.Iterator<String> it = sales.keys(); it.hasNext();) {
                recent += Math.max(0, sales.optInt(it.next(), 0));
            }
            weekly = Math.max(weekly, recent);
        }
        weekly = effectiveWeekly(product, category, weekly, lastSevenDays);
        if (shouldIgnore(product, category)) return 0;
        int stock = product.optInt("stock1", 0) + product.optInt("stock2", 0) + product.optInt("stock3", 0);
        int pack = quantityUnit(product, category);
        if (isRetail(category)) return weekly <= 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, ceilDiv(weekly, pack));
        long required = (long) weekly * Math.max(1, targetWeeks) - stock;
        return required <= 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, ceilDiv(required, pack));
    }

    private static long ceilDiv(long value, long divisor) {
        return (value + divisor - 1L) / divisor;
    }

    private static boolean isRetail(String category) {
        return category != null && "RETAIL".equals(category.trim().toUpperCase(java.util.Locale.ROOT));
    }

    private static int effectiveWeekly(JSONObject p, String category, int weekly, int lastSevenDays) {
        String c = upper(category);
        if (c.contains("LAIT") || c.contains("MILK") || c.contains("DAIRY")) return Math.max(0, lastSevenDays);
        String name = upper(displayName(p));
        if (isAlcohol(c) && (name.contains("710") || name.contains("740"))) {
            return Math.max(Math.max(0, lastSevenDays), Math.max(0, p.optInt("lastWeekQty", 0)));
        }
        return Math.max(0, weekly);
    }

    private static int quantityUnit(JSONObject p, String category) {
        String name = upper(displayName(p));
        if (isAlcohol(category) && (name.contains("710") || name.contains("740"))) return 12;
        return Math.max(1, p.optInt("case_qty", p.optInt("caseQty", 1)));
    }

    private static boolean shouldIgnore(JSONObject p, String category) {
        String c = upper(category), name = upper(displayName(p));
        if (isAlcohol(c) && !name.contains("710") && !name.contains("740")) {
            // AikaPos does not independently order loose alcohol units; they are
            // folded into an explicit 6/12/24-pack row.
            boolean explicitPack = name.matches(".*(?:^|\\s)(?:6|12|24)\\s*[X×*].*");
            if (!explicitPack && !name.matches(".*[X×*]\\s*(?:6|12|24)(?:\\s|$).*") ) return true;
        }
        if (isTobacco(c) && (name.contains("CARTON") || name.contains("CARTOUCHE"))) return true;
        return false;
    }

    private static boolean isAlcohol(String c) {
        return c.contains("BIERE") || c.contains("MOLSON") || c.contains("LABATT") || c.contains("SLEEMAN") || c.contains("POPPERS");
    }

    private static boolean isTobacco(String c) {
        return c.equals("TABAC") || c.equals("IMPERIAL") || c.equals("BENSON") || c.contains("JTI") || c.contains("TOBACCO");
    }

    private static String displayName(JSONObject p) {
        String value = p.optString("mnemonic", "");
        if (value.isEmpty()) value = p.optString("subject", "");
        if (value.isEmpty()) value = p.optString("code", "");
        return value;
    }

    private static String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
