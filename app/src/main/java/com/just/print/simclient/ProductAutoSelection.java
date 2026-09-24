package com.just.print.simclient;

/** Policy for selecting a unique search result without trapping edits on the current product. */
public final class ProductAutoSelection {
    private ProductAutoSelection() {}
    public static boolean shouldSelect(int matchCount, int candidateId, Integer editedFromProductId) {
        return matchCount == 1 && (editedFromProductId == null || editedFromProductId != candidateId);
    }
}
