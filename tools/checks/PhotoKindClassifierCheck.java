import com.just.print.simclient.PhotoKindClassifier;

public class PhotoKindClassifierCheck {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(PhotoKindClassifier.hasIdentityEvidence("PERMIS DE CONDUIRE", false), "Quebec licence");
        check(PhotoKindClassifier.hasIdentityEvidence("CARTE D'ASSURANCE MALADIE", false), "Quebec health card");
        check(PhotoKindClassifier.hasIdentityEvidence("DOB 2000-01-02", true), "Birth label");
        check(PhotoKindClassifier.hasBeerEvidence("COLT 45 MALT LIQUOR", false, false), "Malt liquor");
        check(PhotoKindClassifier.hasBeerEvidence("5% ALC./VOL. 710 ML", false, false), "Alcohol label");
        check(PhotoKindClassifier.hasBeerEvidence("LAGER BEER", false, false), "Beer words");
        check(PhotoKindClassifier.hasBeerEvidence("unreadable", true, false), "Catalog name match");
        check(PhotoKindClassifier.hasBeerEvidence("unreadable", false, true), "Barcode digits");
        check(!PhotoKindClassifier.hasIdentityEvidence("unknown text", false), "Unknown is not ID evidence");
        check(!PhotoKindClassifier.hasBeerEvidence("unknown text", false, false), "Unknown defaults away from beer");
        check(!PhotoKindClassifier.hasBeerEvidence("123 MAIN STREET", false, false), "ID address is not beer");
        System.out.println("PASS: " + checks + " photo routing checks");
    }
}
