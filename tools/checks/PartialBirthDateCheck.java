import com.just.print.simclient.PartialBirthDate;
import java.time.LocalDate;

public class PartialBirthDateCheck {
    private static int checks;
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 16);
    private static void expect(int year, Integer month, Integer day, LocalDate today, int min, int max) {
        int[] ages = PartialBirthDate.ages(year, month, day, today);
        if (ages[0] != min || ages[1] != max) throw new AssertionError(year + "/" + month + "/" + day);
        checks++;
    }
    private static void invalid(int year, Integer month, Integer day) {
        try { PartialBirthDate.ages(year, month, day, TODAY); }
        catch (RuntimeException expected) { checks++; return; }
        throw new AssertionError("Accepted invalid date");
    }
    public static void main(String[] args) {
        expect(2009, null, null, TODAY, 16, 17);
        expect(2009, 8, null, TODAY, 17, 17);
        expect(2009, 10, null, TODAY, 16, 16);
        expect(2009, 9, null, TODAY, 16, 17);
        expect(2009, 9, 15, TODAY, 17, 17);
        expect(2009, 9, 16, TODAY, 17, 17);
        expect(2009, 9, 17, TODAY, 16, 16);
        expect(2009, 9, null, LocalDate.of(2026, 9, 30), 17, 17);
        expect(2009, null, null, LocalDate.of(2026, 12, 31), 17, 17);
        expect(2004, 2, 29, LocalDate.of(2023, 2, 28), 18, 18);
        expect(2004, 2, 29, LocalDate.of(2023, 3, 1), 19, 19);
        expect(2026, null, null, TODAY, 0, 0);
        invalid(2027, null, null);
        invalid(1800, null, null);
        invalid(2009, 13, null);
        invalid(2009, 0, null);
        invalid(2009, 2, 29);
        invalid(2009, 4, 31);
        invalid(2009, 9, 0);
        invalid(2009, null, 10);
        invalid(2026, 9, 17);
        System.out.println("Partial birth date checks passed: " + checks);
    }
}
