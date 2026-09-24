package com.just.print.simclient;

import java.text.Normalizer;
import java.util.Locale;

/** Conservative two-way routing: uncertain photos are treated as IDs. */
public final class PhotoKindClassifier {
    private PhotoKindClassifier() {}

    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    public static boolean hasIdentityEvidence(String text, boolean labelledBirthDate) {
        String value = normalize(text);
        return labelledBirthDate || value.matches("(?s).*(PERMIS|DRIVER.?S LICEN[CS]E|ASSURANCE MALADIE|"
                + "HEALTH INSURANCE|RAMQ|PERMANENT RESIDENT|RESIDENT PERMANENT|DATE DE NAISSANCE|"
                + "DATE OF BIRTH|\\bDOB\\b).*" );
    }

    public static boolean hasBeerEvidence(String text, boolean catalogNameMatch, boolean barcodeDigits) {
        if (catalogNameMatch || barcodeDigits) return true;
        String value = normalize(text);
        return value.matches("(?s).*(\\bBEER\\b|\\bBIERE(?:S)?\\b|\\bLAGER\\b|\\bALE\\b|"
                + "\\bIPA\\b|\\bSTOUT\\b|MALT LIQUOR|\\bBRASSERIE\\b|\\bBREWERY\\b|"
                + "\\bBREWED\\b|ALC(?:OOL)?[ .:/]*[0-9]|[0-9]+(?:[.,][0-9]+)?[ ]*%[ ]*(?:ALC|VOL)|"
                + "[0-9]+[ ]*(?:ML|CL)[ ]*(?:CAN|CANETTE|BOTTLE|BOUTEILLE)?).*" );
    }
}
