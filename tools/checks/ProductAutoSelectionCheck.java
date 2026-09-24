import com.just.print.simclient.ProductAutoSelection;

public class ProductAutoSelectionCheck {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(ProductAutoSelection.shouldSelect(1, 20, null), "Fresh unique result selects");
        check(ProductAutoSelection.shouldSelect(1, 20, 10), "Different unique result selects");
        check(!ProductAutoSelection.shouldSelect(1, 20, 20), "Current product remains editable");
        check(!ProductAutoSelection.shouldSelect(0, -1, null), "No result");
        check(!ProductAutoSelection.shouldSelect(2, 20, null), "Multiple results require choice");
        System.out.println("PASS: " + checks + " product auto-selection checks");
    }
}
