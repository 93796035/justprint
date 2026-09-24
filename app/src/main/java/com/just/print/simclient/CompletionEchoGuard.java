package com.just.print.simclient;

/** Consumes characters already inserted by completion during uninterrupted typing. */
final class CompletionEchoGuard {
    static final long PAUSE_MS = 500;
    private String completed = "", remaining = "";
    private long lastInputAt;

    void arm(String typed, String result, long now) {
        reset();
        if (result.startsWith(typed) && result.length() > typed.length()) {
            completed = result;
            remaining = result.substring(typed.length());
            lastInputAt = now;
        }
    }

    void reset() { completed = ""; remaining = ""; }

    int consume(String current, int start, int end, String inserted, long now) {
        if (remaining.isEmpty()) return 0;
        if (!completed.equals(current) || start != current.length() || end != start
                || inserted.isEmpty() || now - lastInputAt >= PAUSE_MS) {
            reset();
            return 0;
        }
        int matched = 0;
        while (matched < inserted.length() && matched < remaining.length()) {
            int a = inserted.codePointAt(matched), b = remaining.codePointAt(matched);
            if (Character.toLowerCase(a) != Character.toLowerCase(b)) break;
            matched += Character.charCount(a);
        }
        if (matched < inserted.length()) reset();
        else {
            remaining = remaining.substring(matched);
            lastInputAt = now;
        }
        return matched;
    }
}
