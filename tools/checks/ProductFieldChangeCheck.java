import com.just.print.simclient.ProductFieldChange;
import org.json.JSONObject;

public class ProductFieldChangeCheck {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        JSONObject row = new JSONObject().put("stock1", 3).put("monque", -2)
                .put("cost", 1458).put("price", JSONObject.NULL);
        check(!ProductFieldChange.changed(row, "stock1", "3", false), "Same stock");
        check(!ProductFieldChange.changed(row, "stock1", "003", false), "Equivalent stock");
        check(ProductFieldChange.changed(row, "stock1", "4", false), "Changed stock");
        check(ProductFieldChange.changed(row, "stock1", "bad", false), "Invalid stock needs validation");
        check(!ProductFieldChange.changed(row, "cost", "14.58", true), "Same cost");
        check(!ProductFieldChange.changed(row, "cost", "14,58", true), "Equivalent decimal separator");
        check(ProductFieldChange.changed(row, "cost", "14.59", true), "Changed cost");
        check(!ProductFieldChange.changed(row, "price", "", true), "Null price remains empty");
        check(ProductFieldChange.changed(row, "price", "17.99", true), "Set null price");
        check(!ProductFieldChange.changed(row, "missing", "1", false), "Unavailable field ignored");
        System.out.println("PASS: " + checks + " automatic field-save checks");
    }
}
