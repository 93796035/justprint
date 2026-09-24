import com.just.print.simclient.PhotoProductMatcher;
import java.util.Arrays;
import java.util.List;

public class PhotoProductMatcherCheck {
    private static int checks;
    private static PhotoProductMatcher.Item item(int id, String name, boolean preferred) {
        return new PhotoProductMatcher.Item(id, name, preferred ? "BEER" : "OTHER", "SKU" + id, preferred);
    }
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
    private static List<PhotoProductMatcher.Match> match(String text, PhotoProductMatcher.Item... items) {
        return PhotoProductMatcher.match(text, Arrays.asList(items));
    }
    public static void main(String[] args) {
        PhotoProductMatcher.Item colt = item(1, "COLT 45 710ML", true);
        List<PhotoProductMatcher.Match> matches = match("COLT\n45\nMALT LIQUOR\n710 mL", colt, item(2, "MOLSON EX 355ML", true));
        check(matches.size() == 1 && matches.get(0).item.id == 1 && matches.get(0).exact, "Colt 45 label");
        check(match("colt45", colt).get(0).exact, "Joined brand and digits");
        check(match("COLT 45", item(1, "COLT 45 6X355ML", true)).get(0).exact, "Ignore packaging");
        matches = match("COLT 45", colt, item(2, "COLT 45 355ML", true));
        check(matches.size() == 2, "Do not arbitrarily choose package size");
        matches = match("COLT 45", colt, item(2, "COLT 45 710ML", false));
        check(matches.size() == 1 && matches.get(0).item.id == 1, "Current category first");
        matches = match("COLT 45", item(2, "MOLSON EX 355ML", true), item(3, "COLT 45 710ML", false));
        check(matches.size() == 1 && matches.get(0).item.id == 3, "Fallback across catalog");
        check(match("BEER 710 ML 5% VOL", colt).isEmpty(), "Ignore generic packaging text");
        check(match("45 710", colt).isEmpty(), "Numbers alone are not a brand");
        check(match("LIGHT LAGER", item(1, "BUD LIGHT 355ML", true)).isEmpty(), "Generic type alone isn't enough");
        check(match("COLT", colt).size() == 1 && !match("COLT", colt).get(0).exact, "Missing brand number requires choice");
        check(!match("COLT 55", colt).get(0).exact, "Do not auto-select conflicting number");
        check(match("HEINEKEN", item(1, "HEINEKEN 500ML", true)).get(0).exact, "One-word brand");
        check(!match("HEINEKEM", item(1, "HEINEKEN 500ML", true)).get(0).exact, "Fuzzy OCR must be confirmed");
        check(match("BORÉALE ROUSSE", item(1, "BOREALE ROUSSE 473ML", true)).get(0).exact, "French accents");
        check(match("BEST BEFORE 2026-12-31", colt).isEmpty(), "Date isn't a product name");
        check(match("", colt).isEmpty(), "Empty OCR");
        check(match("COLT 45").isEmpty(), "Empty catalog");
        System.out.println("Photo product checks passed: " + checks);
    }
}
