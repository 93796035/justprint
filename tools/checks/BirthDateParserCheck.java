import com.just.print.simclient.BirthDateParser;
import java.time.LocalDate;
import java.util.List;

public class BirthDateParserCheck {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 16);
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
    private static void date(String text, String expected, boolean labeled) {
        List<BirthDateParser.Candidate> dates = BirthDateParser.extract(text, TODAY);
        check(dates.size() == 1, "Expected one date: " + text);
        check(dates.get(0).date.toString().equals(expected), "Wrong date: " + text);
        check(dates.get(0).birthLabel == labeled, "Wrong label: " + text);
    }
    public static void main(String[] args) {
        date("3 Date de naissance (A-M-J) : 2000-09-17\n4a Valide le : 2023-01-01\n4b Expire le : 2028-01-01", "2000-09-17", true);
        date("Date de naissance\n1999 12 31\nExpiration 2025 12", "1999-12-31", true);
        date("DATE OF BIRTH / DATE DE NAISSANCE\n1995-MAR-21", "1995-03-21", true);
        date("DOB: 21 FÉV 1995", "1995-02-21", true);
        date("DOB 1995 AUG/AOÛT 21", "1995-08-21", true);
        date("Naissance: 1995-AL-21", "1995-04-21", true);
        date("1990.10.29", "1990-10-29", false);
        date("DOB: 29/10/1990", "1990-10-29", true);
        date("DOB: 10/29/1990", "1990-10-29", true);
        date("DOB 1990-10-29\n1990-10-29", "1990-10-29", true);
        List<BirthDateParser.Candidate> ambiguous = BirthDateParser.extract("DOB 03/04/2000", TODAY);
        check(ambiguous.size() == 2, "Ambiguous month/day must not be guessed");
        check(BirthDateParser.extract("Expires 2020-01-01\n4a 2021-02-03", TODAY).isEmpty(), "Expiry/issue must be excluded");
        check(BirthDateParser.extract("DOB 2001-02-29\nDOB 2027-01-01\nDOB 1800-01-01\nDOB 2000-13-01", TODAY).isEmpty(), "Impossible/future dates rejected");
        check(BirthDateParser.extract("ABCD 95032112\n3O/O1/2OOO\n950321", TODAY).isEmpty(), "No guessing digits, century, or RAMQ number");
        check(BirthDateParser.extract("DOB\nExpires 2020-01-01", TODAY).isEmpty(), "Don't carry DOB label into expiry");
        check(BirthDateParser.extract("Date of issue\n2021-01-01\nExpiration\n2025-02-01", TODAY).isEmpty(), "Exclude date following issue/expiry label");
        check(!BirthDateParser.extract("DOB 2000-01-01 EXPIRY 2025-01-01", TODAY).get(0).birthLabel, "Mixed columns need manual review");
        check(BirthDateParser.age(LocalDate.of(2008, 9, 17), TODAY) == 17, "Birthday tomorrow");
        check(BirthDateParser.age(LocalDate.of(2008, 9, 16), TODAY) == 18, "Birthday today");
        check(BirthDateParser.age(LocalDate.of(2008, 9, 15), TODAY) == 18, "Birthday yesterday");
        check(BirthDateParser.age(LocalDate.of(2004, 2, 29), LocalDate.of(2023, 2, 28)) == 18, "Leap birthday before March 1");
        check(BirthDateParser.age(LocalDate.of(2004, 2, 29), LocalDate.of(2023, 3, 1)) == 19, "Leap birthday March 1");
        check(BirthDateParser.age(TODAY, TODAY) == 0, "Newborn");
        System.out.println("Birth date checks passed: " + checks);
    }
}
