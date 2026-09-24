package com.just.print.simclient;

import org.json.*;
import java.util.Locale;
import java.util.regex.*;

/** Derives the desktop's product facts from the ordinary phone catalog, without HTTP. */
public final class LocalMovingInputs {
    private LocalMovingInputs() {}
    public static JSONObject fromCatalog(JSONArray catalog) throws Exception {
        JSONArray rows = new JSONArray();
        boolean salesAvailable = true;
        for (int i = 0; i < catalog.length(); i++) {
            JSONObject p = new JSONObject(catalog.getJSONObject(i).toString());
            p.put("moving", facts(p));
            if (!p.has("movingSales")) {
                if (p.getJSONObject("moving").getBoolean("eligible")) salesAvailable = false;
                p.put("movingSales", new JSONObject());
            }
            rows.put(p);
        }
        return new JSONObject().put("products", rows).put("rulesVersion", 1)
                .put("salesAvailable", salesAvailable).put("capturedAt", "Local inventory");
    }
    public static JSONObject facts(JSONObject p) throws Exception {
        String mnemonic = language(p,"mnemonic","subject"), subject = language(p,"subject","mnemonic");
        String name = !mnemonic.isEmpty() && !mnemonic.matches("\\d+") ? mnemonic : !subject.isEmpty() ? subject : mnemonic;
        String normalized = name.toUpperCase(Locale.ROOT).replace('\u00d7','X').replace('*','X').trim()
                .replaceAll("(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(?:PERCENT\\b|%)", "$1c")
                .replaceAll("(?i)\\bM+L\\b", "").replaceAll("(?i)\\bCANS?\\b", "")
                .replaceAll("(?i)\\bAMERICAN\\s+LAGER\\s+BEER\\b", "").replaceAll("(?i)\\bLAGER\\s+BEER\\b", "")
                .replaceAll("\\s+", " ").trim()
                .replaceAll("(?i)^HEINEKEN\\s+5\\.0B\\b", "HEINEKEN 5b").replaceAll("(?i)^HEINEKEN\\s+5\\.0C\\b", "HEINEKEN 5c")
                .replaceAll("(?i)\\b0?(4|6|12|15|18|20|24)\\s*X\\s*(\\d{3,4})\\b", "$2x$1");
        if (normalized.matches("(?i)^BUDWEISER\\b.*") && !normalized.matches("(?i)^BUDWEISER\\s+ZERO\\b.*")
                && !normalized.matches("(?i).*\\b\\d+(?:\\.\\d+)?[BC]\\b.*"))
            normalized = normalized.replaceFirst("(?i)^BUDWEISER\\b", "BUDWEISER 5c");
        Matcher pack = suffix(normalized, "4|6|12|15|18|20|24");
        int quantity = 1;
        if (pack.find()) { quantity = suffixQuantity(pack); normalized = normalized.substring(0,pack.start()).trim(); }
        String group = normalize(normalized.replaceAll("(?i)\\b(\\d{3,4})\\s*M+L\\b", "$1"));
        boolean large = normalize(name).contains("710") || normalize(name).contains("740");
        int units = large ? 12 : quantity == 12 || quantity == 15 || quantity == 18 || quantity == 20 ? 1 : caseUnits(mnemonic,p.optInt("case_qty",0));
        int bottles = large ? 12 : quantity == 20 ? 20 : quantity == 12 || quantity == 15 || quantity == 18 ? quantity : Math.multiplyExact(quantity,units);
        int priority = large || quantity==12 ? 0 : quantity==15 ? 1 : quantity==18 ? 2 : quantity==20 ? 3 : quantity==24 ? 4 : quantity==6 ? 5 : quantity==4 ? 6 : Integer.MAX_VALUE;
        // Match Windows' existing MANQUE conversion, including its narrower suffix list.
        String shortageName = !mnemonic.matches("\\d+") ? mnemonic : subject;
        Matcher shortage = suffix(shortageName.toUpperCase(Locale.ROOT).replace('\u00d7','X').replace('*','X').trim(), "4|6|12|15|24");
        int shortageBottles = shortageName.contains("710") || shortageName.contains("740") ? 1 : shortage.find() ? suffixQuantity(shortage) : 1;
        String category = normalize(text(p,"category"));
        boolean eligible = false;
        for (String beer : new String[]{"BIERE","MOLSON","LABATT","SLEEMAN","POPPERS"}) eligible |= category.contains(beer);
        return new JSONObject().put("eligible",eligible).put("group",group).put("name",name)
                .put("priority",priority).put("unitsPerCase",units).put("caseBottles",bottles)
                .put("saleBottles",large ? 1 : quantity).put("shortageBottles",shortageBottles);
    }
    private static Matcher suffix(String name, String sizes) {
        return Pattern.compile("(?:X\\s*0?("+sizes+")|0?("+sizes+")\\s*X|(?<!\\d)0?("+sizes+"))\\s*$").matcher(name);
    }
    private static int suffixQuantity(Matcher m) { return Integer.parseInt(m.group(1)!=null ? m.group(1) : m.group(2)!=null ? m.group(2) : m.group(3)); }
    private static String normalize(String s) { return s.toUpperCase(Locale.ROOT).replace('\u3000',' ').replaceAll("[\\p{Punct}&&[^\\-]]+"," ").replaceAll("\\s+"," ").trim(); }
    private static String text(JSONObject p, String key) { return p.isNull(key) ? "" : p.optString(key,"").trim(); }
    private static String language(JSONObject p, String key, String alternate) {
        String value = text(p,key);
        if (!value.isEmpty() && !value.equalsIgnoreCase("null")) return value;
        String code = text(p,"code");
        return code.matches("\\d+") ? text(p,alternate) : code;
    }
    private static int caseUnits(String name, int explicit) {
        if (name.isEmpty()) return 24;
        int inferred = 24;
        Matcher m = Pattern.compile("(?i)(?:\\b(\\d{1,3})\\s*[xX](?!\\d)|[xX]\\s*(\\d{1,3})\\b)").matcher(name);
        Integer pack = null;
        while (m.find()) pack = Integer.valueOf(m.group(1)!=null ? m.group(1) : m.group(2));
        if (name.contains("710") || name.contains("740")) inferred=12;
        else if (pack!=null && pack>0) inferred=pack==12 || pack==15 || pack==18 ? 1 : Math.max(1,(int)Math.ceil(24.0/pack));
        else if (name.endsWith("x6") || name.endsWith("x06")) inferred=4;
        else if (name.endsWith("x4") || name.endsWith("x04")) inferred=6;
        else if (name.endsWith("x12")) inferred=2;
        else if (name.endsWith("25")) inferred=8;
        else if (name.endsWith("20")) inferred=10;
        else if (name.startsWith("Vuse Pod") || name.startsWith("Veev One Pod")) inferred=5;
        else if (name.startsWith("Logic Pod")) inferred=6;
        else if (name.contains("4x") || name.contains("x4") || name.contains("x04")) inferred=6;
        else if (name.contains("6x") || name.contains("x6") || name.contains("x06")) inferred=4;
        return explicit>0 && !(explicit==24 && inferred!=24) ? explicit : inferred;
    }
}
