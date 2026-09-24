import com.just.print.simclient.ProductSyncVerification;
import org.cas.client.platform.bar.net.inventory.StockProductStore;
import org.cas.client.platform.bar.net.inventory.InventoryCommandServer;
import org.json.*;
import java.sql.*;
public class ProductSyncCheck {
 public static void main(String[] args) throws Exception {
  try(Connection db=DriverManager.getConnection("jdbc:hsqldb:mem:sync_check","SA","")) {
   db.createStatement().execute("CREATE TABLE PRODUCT(ID INT PRIMARY KEY,CODE VARCHAR(40),MNEMONIC VARCHAR(100),SUBJECT VARCHAR(100),CATEGORY VARCHAR(40),DELETED INT,STOCK1 INT,STOCK2 INT,STOCK3 INT,STOCK1_SHORTAGE INT,COST INT,PRICE INT)");
   db.createStatement().execute("INSERT INTO PRODUCT VALUES(1,'a','TEST','TEST','BEER',0,5,6,7,-2,100,200)");
   JSONObject old=new JSONObject().put("stock1",5).put("stock2",6).put("stock3",7).put("monque",2);
   JSONObject values=new JSONObject(old.toString()).put("stock1",30);
   JSONObject req=new JSONObject().put("productId",1).put("expected",old).put("values",values);
   StockProductStore.update(db,req);
   try(Connection second=DriverManager.getConnection("jdbc:hsqldb:mem:sync_check","SA","")) {
    ProductSyncVerification.verify(req,StockProductStore.read(second,1));
   }
   try { StockProductStore.update(db,req); throw new AssertionError("Stale update accepted"); }
   catch(InventoryCommandServer.BusyException expected) {}
   JSONObject row=StockProductStore.read(db,1);
   for (String key : new String[]{"id","stock1","stock2","stock3","monque"}) {
    JSONObject wrong=new JSONObject(row.toString()).put(key,999);
    try { ProductSyncVerification.verify(req,wrong); throw new AssertionError("Mismatch accepted: "+key); }
    catch(java.io.IOException expected) {}
   }
   old.put("stock1",30).put("price",200); values.put("price",350);
   StockProductStore.update(db,req);
   ProductSyncVerification.verify(req,StockProductStore.read(db,1));
   System.out.println("PASS: persisted stock read on second connection, stale conflict, five mismatches, price update");
  }
 }
}
