package com.just.print.simclient;

public class CompletionEchoGuardCheck {
    public static void main(String[] args) {
        CompletionEchoGuard g = new CompletionEchoGuard();
        g.arm("Bo", "Boris", 0);
        eq(1, g.consume("Boris", 5, 5, "r", 100));
        eq(1, g.consume("Boris", 5, 5, "I", 450));
        eq(1, g.consume("Boris", 5, 5, "s", 800));
        eq(0, g.consume("Boris", 5, 5, "s", 850));
        g.arm("Bo", "Boris", 0);
        eq(3, g.consume("Boris", 5, 5, "ris Bold", 100));
        g.arm("Bo", "Boris", 0);
        eq(1, g.consume("Boris", 5, 5, "rx", 100));
        eq(0, g.consume("Boris", 5, 5, "i", 120));
        g.arm("Bo", "Boris", 0);
        eq(0, g.consume("Boris", 5, 5, "r", 500));
        g.arm("Bo", "Boris", 0);
        eq(0, g.consume("Boris", 4, 5, "", 100));
        eq(0, g.consume("Boris", 5, 5, "r", 120));
        g.arm("Bo", "Boris", 0);
        eq(0, g.consume("Boris", 2, 2, "r", 100));
        g.arm("Bo", "Boris", 0);
        eq(0, g.consume("Other", 5, 5, "r", 100));
        g.arm("Bo", "Boris", 0);
        g.reset();
        eq(0, g.consume("Boris", 5, 5, "r", 100));
        g.arm("A", "A\uD83D\uDE00", 0);
        eq(2, g.consume("A\uD83D\uDE00", 3, 3, "\uD83D\uDE00!", 100));
        System.out.println("PASS: continuous typing, batches, mismatch, pause, edits, reset and Unicode");
    }
    private static void eq(int expected, int actual) {
        if (expected != actual) throw new AssertionError(expected + " != " + actual);
    }
}
