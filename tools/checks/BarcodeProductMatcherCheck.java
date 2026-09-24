import com.just.print.simclient.*;
import java.util.*;
public class BarcodeProductMatcherCheck {
 static void check(boolean ok) { if(!ok) throw new AssertionError(); }
 static PhotoProductMatcher.Item item(int id,String code,boolean preferred) { return new PhotoProductMatcher.Item(id,"Beer "+id,"BEER",code,preferred); }
 public static void main(String[] args) {
  List<PhotoProductMatcher.Item> products=Arrays.asList(item(1,"012345678905",false),item(2,"112345678906",true));
  List<BarcodeProductMatcher.Match> m=BarcodeProductMatcher.match(Arrays.asList("012345678905"),products);
  check(m.size()==1 && m.get(0).item.id==1 && m.get(0).exact);
  m=BarcodeProductMatcher.match(Arrays.asList("0012345678905"),products);
  check(m.size()==1 && m.get(0).exact);
  m=BarcodeProductMatcher.match(Arrays.asList("1234567890"),products);
  check(m.size()==2 && m.get(0).item.id==2 && !m.get(0).exact);
  check(BarcodeProductMatcher.match(Arrays.asList("12345"),products).isEmpty());
  check(BarcodeProductMatcher.match(Arrays.asList("345678"),products).isEmpty());
  check(BarcodeProductMatcher.match(Arrays.asList("012345678805"),products).isEmpty());
  check(BarcodeProductMatcher.printedCodes("BEER\n0 12345 67890 5\n355 ml").equals(Arrays.asList("012345678905")));
  check(BarcodeProductMatcher.printedCodes("2026-09-18").isEmpty());
  check(BarcodeProductMatcher.printedCodes("12345678901234567890").isEmpty());
  check(BarcodeProductMatcher.match(Arrays.asList("012345678905"),Arrays.asList(item(1,"012345678905",true),item(2,"012345678905",false))).size()==2);
  check(BarcodeProductMatcher.match(Arrays.asList("012345678905","112345678906"),products).size()==2);
  System.out.println("PASS: 11 barcode matching checks");
 }
}
