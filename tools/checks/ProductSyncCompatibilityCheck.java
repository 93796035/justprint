import com.just.print.simclient.ProductSyncCompatibility;
import org.json.JSONObject;

public class ProductSyncCompatibilityCheck {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        JSONObject stock = new JSONObject().put("stock1", 30).put("stock2", 2)
                .put("stock3", 3).put("monque", 4);
        JSONObject expected = new JSONObject().put("stock1", 1).put("stock2", 2)
                .put("stock3", 3).put("monque", 4);
        JSONObject request = new JSONObject().put("productId", 9)
                .put("values", new JSONObject(stock.toString()).put("price", 350))
                .put("expected", new JSONObject(expected.toString()).put("price", 200));
        check(ProductSyncCompatibility.needsStockOnlyRetry(400, "HTTP 400: 需要四个库存字段", request), "Chinese legacy response");
        check(ProductSyncCompatibility.needsStockOnlyRetry(400, "Four inventory fields required", request), "English legacy response");
        check(!ProductSyncCompatibility.needsStockOnlyRetry(409, "需要四个库存字段", request), "Do not retry conflicts");
        JSONObject fallback = ProductSyncCompatibility.stockOnly(request);
        check(fallback.getInt("productId") == 9, "Product ID retained");
        check(fallback.getJSONObject("values").length() == 4 && !fallback.getJSONObject("values").has("price"), "Only four values");
        check(fallback.getJSONObject("expected").length() == 4 && fallback.getJSONObject("expected").getInt("stock1") == 1, "Four original values");
        check(!ProductSyncCompatibility.needsStockOnlyRetry(400, "需要四个库存字段", fallback), "Do not repeat identical request");
        System.out.println("PASS: " + checks + " legacy sync compatibility checks");
    }
}
