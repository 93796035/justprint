import com.just.print.simclient.ProductCatalog;
import com.just.print.simclient.ShortageSync;
import org.cas.shared.inventory.MovingPlan;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShortageSyncCheck {
    public static void main(String[] args) throws Exception {
        check(ShortageSync.afterSale(-4, 1) == -5, "Negative shortage increases in magnitude");
        check(ShortageSync.afterSale(4, 1) == 5, "Positive shortage keeps its sign");
        check(ShortageSync.afterSale(0, 2) == -2, "Zero follows website convention");
        check(ShortageSync.afterSale(0, 2, false) == 2, "Zero follows local POS convention");
        check(ShortageSync.afterSale(-4, 3) == -7, "Sale quantity is applied");
        JSONObject local = new JSONObject().put("id", 1).put("code", "123")
                .put("stock1", 5).put("monque", 4);
        JSONObject website = ShortageSync.forWebsite(local);
        check(website.getInt("monque") == -4 && local.getInt("monque") == 4,
                "Website upload is negative without mutating phone data");
        check(ShortageSync.forWebsite(new JSONObject().put("monque", -4)).getInt("monque") == -4,
                "Negative website upload is unchanged");
        check(ShortageSync.forWebsite(new JSONObject().put("stock1_shortage", 3)).getInt("monque") == -3,
                "Legacy website column alias is normalized before upload");
        ProductCatalog catalog = new ProductCatalog(new JSONArray().put(new JSONObject()
                .put("id", 2).put("code", "456").put("stock1_shortage", -8)));
        check(catalog.all().get(0).row().getInt("monque") == -8,
                "Website response alias survives catalog normalization");
        JSONObject moving = new JSONObject().put("eligible", true).put("group", "case")
                .put("name", "Test case").put("priority", 1).put("unitsPerCase", 1)
                .put("caseBottles", 1).put("shortageBottles", 1).put("saleBottles", 1);
        JSONObject input = new JSONObject().put("products", new JSONArray().put(new JSONObject()
                .put("id", 3).put("stock1", 0).put("stock2", 0).put("stock3", 10)
                .put("monque", -4).put("moving", moving)));
        check(MovingPlan.generate(input, null).getJSONArray("rows").getJSONObject(0)
                .getLong("shortage") == 4, "Negative website shortage counts in moving plan");
        System.out.println("PASS: website sign, sale magnitude, and response shortage alias");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
