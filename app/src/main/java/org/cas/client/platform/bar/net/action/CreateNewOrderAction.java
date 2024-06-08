package org.cas.client.platform.bar.net.action;

import android.database.Cursor;
import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.Printer;
import com.just.print.sys.server.beans.MainOrder;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.TablesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.action.SamActionListener;
import org.cas.client.platform.bar.dialog.modifyDish.AddModificationDlg;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.NetUtil;
import org.cas.client.platform.bar.net.bean.Material;
import org.cas.client.platform.bar.net.bean.TextContent;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CreateNewOrderAction implements SamActionListener {

	List<MainOrder> mainOrders;
	List<Material> materials;
	HashMap<String, Integer> qtMap;
	List<TextContent> menus;
	List<TextContent> serviceTexts;
	
	private static CreateNewOrderAction instance = null;
	public static CreateNewOrderAction getInstance() {
		if(instance == null) {
			instance = new CreateNewOrderAction();
		}
		return instance;
	}
	
	public String processAddingOrderRequest(String json) throws JSONException {
		JSONObject rjson = new JSONObject(json);
		String lastModifiedTime = rjson.getString("lastModifiedTime");
		
		//prepare tableName and billIndex
		if(rjson.has("waiter") && rjson.has("table") && rjson.has("billIndex") && rjson.has("totalPrice") && rjson.has("orderContent")) {
			String waiter = rjson.getString("waiter");
			String tableName = rjson.getString("table");
			String billIndex = rjson.getString("billIndex");
			Double totalPrice =  rjson.getDouble("totalPrice");
			String orderContent = rjson.getString("orderContent");
			try {
				orderContent = URLDecoder.decode(orderContent, "UTF-8");
			}catch(Exception e) {
				L.e("CreateNewOrderAction", "exception when convert the orderContent into UTF-8", e);
			}
			String[] disheStrs = orderContent.split("DishStart:");
			
			//start to generate the order.===============================
			String openTime = rjson.has("opentime") ? rjson.getString("opentime") : "";
	        if(openTime != null && openTime.trim().length() > 0) {
	        	openTime = BarUtil.df.format(new Date());
	        }
	        //table
	        if(tableName != null && tableName.length() > 0) {
	        	makeSureTableExistsAndOpened(tableName, openTime);
	        }
	        //bills
	        int billId = makeSureBillExists(waiter, openTime, tableName, billIndex, totalPrice.floatValue());
	
	        if(billId < 0) {
	        	L.e("Request new orders", "Failed to create a bill with createtime:" + openTime + " tableName:" + tableName + " billIndex:" + " price:" + totalPrice, null);
	        	return lastModifiedTime;
	        }
	        
			for(String dishStr: disheStrs) {
				//TODO: calculate the totle price.
				if(dishStr == null || dishStr.length() == 0) {
					continue;
				}
				int marksLocation = dishStr.indexOf("MarkStart:");
				String markStr = marksLocation > 0 ? dishStr.substring(marksLocation + 10) : "";
				dishStr = marksLocation > 0 ? dishStr.substring(0, marksLocation) : dishStr;
				String[] dishProp = dishStr.split("\n");	//get name, price and qt of dish
				String[] markProp = markStr.split("\n");	//get name, qt and status of marks
				//price total;
				String category = dishProp[0];
				String menuName = dishProp[1];
				int price = Math.round(Float.valueOf(dishProp[2]) * 100);
				int num = Integer.valueOf(dishProp[3]);
				makeSureCategoryExist(category);
				Dish dish = makeSureDishExist(category, menuName, price);
				dish.setNum(num);
				int markPrice = makeSureMarkExist(markProp);
				markStr = refineMarkStr(markProp);
		    	dish.setModification(markStr);
		    	price = price * num + markPrice;
		    	//if totalPrice is negative, means the request is a cancel request.
				createOutputRecord(dish.getId().intValue(), openTime, tableName, billIndex, billId, markStr, num, totalPrice < 0 ? -1 * price : price);
			}
			//automatically set the ShowProcessingBill flag.
			CustOpts.custOps.setKeyAndValue("ShowProcessingBill", "true");
		}
		return lastModifiedTime;
	}
	
	private String refineMarkStr(String[] markProp) {
		int size = markProp.length / 4;
		StringBuilder markStr = new StringBuilder();
		for(int i = 0; i < size; i++) {
			if(i > 0) {
				markStr.append(",");
			}
			markStr.append(markProp[0 + i * 4]).append(" x").append(markProp[1 + i * 4]);
		}
		return markStr.toString();
	}

	private int makeSureMarkExist(String[] markProp) {
		AddModificationDlg dlg = AddModificationDlg.getInstance();
		ArrayList<String> existingList = dlg.getListInCurrentListComponent();
		int size = markProp.length / 4;
		int totalPrice = 0;
		for(int i = 0; i < size; i++) {
			String strInput = markProp[0 + i * 4];
			int num = 1;
			try {
				num = Integer.valueOf(markProp[1 + i * 4]);
			}catch(Exception e) {
				//do nothing.
			}
			
			int price = 0;
			try {
				price = Integer.valueOf(markProp[3 + i * 4]);
			}catch(Exception e) {
				//do nothing.
			}
			
			int index = dlg.findIndexByLang1(existingList, strInput);
			if(index < 0) {
				dlg.insertModification(strInput, BarUtil.formatMoney(price/100.0));
			}else {
				dlg.updateToModification(strInput, BarUtil.formatMoney(price/100.0));
			}
			totalPrice += num * price;
		}
		return totalPrice; 
	}
	

	@Override
	public synchronized void onClick(View e) {
//		List<String> tList = new JSONDeserializer<List<String>>()
//				.use(null, ArrayList.class).use("values", String.class).deserialize(e.getActionCommand());
//		//reinit the four instance properties with new received json string.
//	    mainOrders = new JSONDeserializer<List<MainOrder>>()
//	    		.use(null, ArrayList.class).use("values", MainOrder.class).deserialize(tList.get(0));
//	    if(mainOrders.isEmpty())
//	    	return;
//
//	    materials = new JSONDeserializer<List<Material>>().use(null, ArrayList.class)
//	            .use("values", Material.class).deserialize(tList.get(1));
//
//
//        //because the qt (4X) will stands for 4 material, so need to merge it before creating output.
//        qtMap = conbineMainOrders(materials);
//
//	    menus = new JSONDeserializer<List<TextContent>>().use(null, ArrayList.class)
//	            .use("values", TextContent.class).deserialize(tList.get(2));
//
//	    serviceTexts = new JSONDeserializer<List<TextContent>>().use(null, ArrayList.class)
//	            .use("values", TextContent.class).deserialize(tList.get(3));
//
//        String openTime = BarUtil.df.format(new Date());
//
//	    for (int i = 0; i < mainOrders.size(); i++) {
//	        MainOrder mainOrder = mainOrders.get(i);
//	        //prepare tableName and billIndex
//	        String tableName = mainOrder.sizeTable;
//	        //@NOTE: do we really should use the billIdx from client? what if there's multible client?
//	        String billIndex = "1";			//@default value, will change if tableName contains "_".
//	        int p = tableName.indexOf("_");
//	        if(p > 0) {
//	        	billIndex = tableName.substring(p+1);
//	        	tableName = tableName.substring(0, p);
//	        }
//	        //table
//	        makeSureTableExistsAndOpened(tableName, openTime);
//
//	        //bills
//	        int billId = generateBill(findEmployeeId(mainOrder.contactPerson.name), openTime, tableName, billIndex, Float.valueOf(mainOrder.payCondition));
//            if(billId < 0) {
//            	L.e("Request new orders", "Failed to create a bill with createtime:" + openTime + " tableName:" + tableName + " billIndex:" + billIndex + " price:" + mainOrder.payCondition, null);
//            	return;
//            }
//
//            //outputs
//            List<Dish> dishes = generateOutputs(mainOrder, openTime, tableName, billIndex, billId, materials);
//
//            //send these output to kitchen.
//            //PrintService.printTemporaryOrderList(dishes, tableName, billIndex, "customer", false);
//
//            //send request to update the status to be 50.
//        	new HttpRequestClient(
//        			prepareUpdateOrderStatusURL(BarOption.getServerHost(), mainOrder.id, 50),
//        			"POST",
//        			BarFrame.prepareLicenceJSONString(),
//        			null,
//        			null).start();
//	    }
	}
	
	public int makeSureBillExists(String waiter, String createtime, String tableName, String billIndex, Float totalPrice) {
		//considered the case that, client added dish from slave machine in an existing bill.
		//should the bill exist, then should update the total price, and return back the id.
		StringBuilder sql = new StringBuilder("select _id, status from bill where tableId = '").append(tableName).append("'")
				.append(" and opentime = '").append(createtime).append("'")
				.append(" and (status is null or status < ").append(DBConsts.COMPLETED)
				.append(" and status >= ").append(DBConsts.ORIGINAL).append(")");
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
        	if(rs.moveToNext()) {
        		
        		int billId = DBUtil.getCursorInt(rs, "_id");
        		int status = DBUtil.getCursorInt(rs, "status");
        		int price = DBUtil.getCursorInt(rs, "total");
        		if(status >= DBConsts.BILL_PRINTED || status < DBConsts.ORIGINAL) {
//    				if (JOptionPane.showConfirmDialog(BarFrame.instance, BarFrame.consts.ConvertClosedBillBack(), BarFrame.consts.Operator(),
//    			            JOptionPane.YES_NO_OPTION) != 0) {// are you sure to convert the voided bill back？
//    			        return -1;
//    				}else {
    					//update the totalprice of bill;
    					//@This means waiter can not update discount and service fee on the slave machine.
    					DBUtil.updateBill(billId, "total", Math.round(price + totalPrice * 100));
    					return billId;
//    				}
        		}
        	}
        	       	
        }catch(Exception exp) {
        	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
        	return -1;
        }
		
		int employeeId = findEmployeeId(waiter);
		return generateBill(employeeId, createtime, tableName, billIndex, totalPrice);
	}

	private int findEmployeeId(String waiter) {
		StringBuilder sql;
		sql = new StringBuilder("select id from USER where nickName = '").append(waiter).append("'");
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
        	if(rs.moveToNext()) {
        		return DBUtil.getCursorInt(rs, "_id");
        	}
        }catch(Exception exp) {
        	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
        	return -1;
        }
		return 0;
	}
	
	public int generateBill(int employeeId, String createtime, String tableName, String billIndex, Float total) {
		//create a bill
		StringBuilder sql = new StringBuilder(
		        "INSERT INTO bill(createtime, tableName, BillIndex, total, discount, tip, serviceFee, cashback, userId, Comment, opentime) VALUES ('")
				.append(createtime).append("', '")			//createtime
		        .append(tableName).append("', '")				//tableID
		        .append(billIndex).append("', ")			//BillIndex
		        .append(Math.round(total * 100)).append(", ")	 //remove the $ ahead.
		        .append(0).append(", ")	//discount
		        .append(0).append(", ")	//tip
		        .append(0).append(", ")	//serviceFee
		        .append(0).append(", ")	//cashback
		        .append(employeeId).append(", '")		//emoployid--0 is the id of automactically generated DEFAULT USER when first time use.
		        .append("").append("', '")					//Comment
		        .append(createtime).append("')");				//opentime
		try {
			AikaPos.db.execSQL(sql.toString());
		   	sql = new StringBuilder("Select id from bill where createtime = '").append(createtime).append("' and billIndex = '").append(billIndex).append("'");
		    Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
		    rs.moveToNext();
            return DBUtil.getCursorInt(rs, "_id");
		 }catch(Exception exp) {
			ErrorUtil.write(exp);
			return -1;
		 }
	}

	public List<Dish> generateOutputs(MainOrder mainOrder, String createtime, String tableName, String billIndex,
			int billId, List<Material> materials) {
		
		List<Dish> dishes = new ArrayList<Dish>();
		//create output record for each materials of this mainOrder. @materials has been cleaned.
		for (int j = 0; j < materials.size(); j++) {
			Material material = materials.get(j);
			if(material.mainOrder.id == mainOrder.id) {	//I prefer this way rather than generate a clean materials list as parameter, because faster and more stable.
		    	String location = material.location;
		    	String portionName = material.portionName;	//dish name
		    	Integer num = qtMap.get(material.portionName + (material.remark == null ? "" : material.remark));	//@NOTE we supposed the dish name should not duplicate.
		    	int price = material.dencity == null ? 0 : Math.round(Float.valueOf(material.dencity.substring(1).trim()) * 100);
		    	
		    	//make sure product and marexist.
		    	Dish dish = synchronizeToLocalDB(location, portionName, price, material.menFu);
		    	
		    	dish.setNum(num);
		    	dish.setModification(material.remark);
		    	dishes.add(dish);
		    	
				createOutputRecord(dish.getId().intValue(), createtime, tableName, billIndex, billId, material.remark, num, price);
			}
		}
		return dishes;
	}

	private void createOutputRecord(int productID, String openTime, String tableName, String billIndex, int billId,
			 String remark, int num, int price) {
		StringBuilder sql;
		try {
			sql = new StringBuilder(
		    	"INSERT INTO output(SUBJECT, CONTACTID, PRODUCTID, AMOUNT, TOLTALPRICE, DISCOUNT, CONTENT, userId, TIME, CATEGORY, FOLDERID) VALUES ('")
					.append(tableName).append("', ")	//subject ->table id
		            .append(billIndex).append(", ")			//contactID ->bill id
		            .append(productID).append(", ")	//product id
		            .append(num).append(", ")	//amount
		            .append(price).append(", ")	//totalprice int
		            .append(0).append(", '")	//discount
		            .append(remark == null ? "" : remark).append("', ")				//content
		            .append(LoginDlg.USERID).append(", '")		//emoployid
		            .append(openTime).append("', '")	//opentime
		            .append(BarUtil.df.format(new Date())).append("', ")	//created Time
		            .append(billId).append(")");	//category ->billId
			AikaPos.db.execSQL(sql.toString());
		} catch (Exception exp) {
			ErrorUtil.write(exp);
		}
	}
	
	private String prepareUpdateOrderStatusURL(String url, long mainOrderId, int targetStatus) {
		url = NetUtil.validateURL(url);
		StringBuilder sb = new StringBuilder(url);
		if(!url.endsWith("/")) {
			sb.append("/");
		}
		sb.append("updateMainOrderStatus/");
		sb.append(mainOrderId).append("/").append(targetStatus);
		return sb.toString();
	}
	
	//combine the material list. remove the duplicated ones, and add the qt into the map value (use name+comment as key).
	private HashMap<String, Integer> conbineMainOrders(List<Material> materials) {
		HashMap<String, Integer> qtMap = new HashMap<String, Integer>();
		for(int i = materials.size() - 1; i >= 0; i--) {
			Material material = materials.get(i);
			String key = material.portionName + (material.remark == null ? "" : material.remark);
			if(qtMap.containsKey(key)) {
				qtMap.put(key, qtMap.get(key) + 1);//increase the qt.
				materials.remove(i);			//remove the none-first one from list.
			}else {
				qtMap.put(key, 1);	//put into it and set the start qt 1.
			}
		}
		return qtMap;
	}

	//check the category and dish, make sure they exist.
	private Dish synchronizeToLocalDB(String location, String portionName, int price, String menFu) {
		String[] strAry = location.split("_");
		
		String category = makeSureCategoryExist(location, Integer.parseInt(strAry[0]), Integer.parseInt(strAry[1]));
		return makeSureDishExist(category, location, Integer.parseInt(strAry[3]), portionName, price, menFu);
	}

	//creata a table record if it's not exist yet. and make it opened. @NOTE: is the status is already opened, then report error.
	public static void makeSureTableExistsAndOpened(String table, String openTime) {
		StringBuilder sql = new StringBuilder("select status from dining_Table where name = '")
				.append(table).append("'");
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
	        int tmpPos = rs.getCount();
	        if(tmpPos == 0) {
	        	createNewOpenedTableInDB(table, openTime);
	        }else {
				rs.moveToNext();
	        	if(DBUtil.getCursorInt(rs,"status") != DBConsts.OPENNED) {//if already exist, make the status to be 1;
	        		DBUtil.updateTableStatusInDB(table, openTime);
	        	}
	        }
	        ((TablesPanel)BarFrame.instance.panels[0]).initContent();
		}catch(Exception exp) {
			L.e("downloading new orders", "makeSureTableExistsAndOpened", exp);
		}
	}

	//create a new opened table
	private static void createNewOpenedTableInDB(String table, String openTime) {
		StringBuilder sql = null;
		int idx = 0;
		try {
			idx = Integer.valueOf(table);
		}catch(Exception e) {
			sql = null;new StringBuilder("select count(*) from dining_Table");
			try {
				Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
				int size = !rs.moveToNext() ? 0 : rs.getInt( 1);
				idx = size + 1;
			}catch(Exception exp) {
				L.e("CreateNewOrder", "exception when run sql:" + sql, exp);
			}
		}
		int row = idx / 10 + 1;
		int col = idx % 10;
		
		sql = new StringBuilder("INSERT INTO DINING_TABLE (name, posX, posY, width, height, type, status, openTime) VALUES ('")
			.append(table).append("', ")	//name
			.append(col * (40 + 20)).append(", ")	//posX
			.append(row * (40 + 20)).append(", ")	//posY
			.append(40).append(", ")	//width
			.append(40).append(", ")	//height
			.append(0).append(", ")		//type
			.append(DBConsts.OPENNED).append(", '")
			.append(openTime == null ? BarUtil.df.format(new Date()) : openTime).append("')");		//status
		
		try {
			AikaPos.db.execSQL(sql.toString());
		}catch(Exception exp) {
			ErrorUtil.write(exp);
		}
	}
	
	//processing request from pad.
	public void makeSureCategoryExist(String categoryName) {
		StringBuilder sql = new StringBuilder("select _id, name1, name2, name3, displayIdx from CATEGORY where LANG1 = ? or LANG2 = ? or LANG3 = ?");
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), new String[]{categoryName,categoryName,categoryName});
            if(rs.moveToNext()) {
            	// we don't need to synchronize the category with pad for now. synchronizeLocalCategory(menuIdx, idx, rs);
            }else {
            	sql = new StringBuilder("select count(*) from CATEGORY where displayIdx >= 0");
            	rs = AikaPos.db.rawQuery(sql.toString(), null);
            	int dspIndex = !rs.moveToNext() ? 1 : rs.getInt(1) + 1;
            	createNewCategory(categoryName, categoryName, categoryName, dspIndex);
            }
        } catch (Exception e) {
            ErrorUtil.write(e);
        }
	}
	
	//make it exist, and return the English version of category.
	private String makeSureCategoryExist(String location, int menuIdx, int idx) {
		//if less than category exist, then add category to 3.
        String sql = "select _id, name1, name2, name3, displayIdx from CATEGORY where displayIdx = " + idx;
        try {
            Cursor rs = AikaPos.db.rawQuery(sql, null);
            int tmpPos = rs.getCount();
            if(tmpPos == 0) {
            	createNewCategory(idx);
            	return rs.getString(1);
            }else {
            	return synchronizeLocalCategory(menuIdx, idx, rs);
            }
        } catch (SQLException e) {
            ErrorUtil.write(e);
        }
        return "";
	}

	//location's format is supposed to be like "3_1_0_1"
	private String synchronizeLocalCategory(int menuIdx, int subMenuIdx, Cursor rs) throws SQLException {
		String location = menuIdx + "_" + subMenuIdx;
		HashMap<String, String> contentMap = new HashMap<>(); 
		for(TextContent menu : menus) {
			String posInPage = menu.posInPage;
			if(posInPage.endsWith("_menu_" + location)) {
				contentMap.put(posInPage, menu.content);
			}
		}
		//values in web
		String name1 = contentMap.get("en_menu_" + location);
		String name2 = contentMap.get("fr_menu_" + location);
		String name3 = contentMap.get("zh_menu_" + location);
		name1 = name1 == null ? "" : name1;
		name2 = name2 == null ? "" : name2;
		name3 = name3 == null ? "" : name3;
		//values in local
		String lName1 = DBUtil.getCursorString(rs, "NAME1");
		String lName2 = DBUtil.getCursorString(rs, "NAME2");
		String lName3 = DBUtil.getCursorString(rs, "NAME3");
		
		if(!name1.equals(lName1) || !name2.equals(lName2) || !name3.equals(lName3)) {
			StringBuilder sql = new StringBuilder("Update category set LANG1 = ?, LANG2 = ?, LANG3 = ? where displayIdx = ").append(subMenuIdx);
			AikaPos.db.execSQL(sql.toString(), new String[]{name1, name2, name3});
			BarFrame.menuPanel.initComponent();
		}
		return name1;
	}

	//create the No. idx category
	private void createNewCategory(int dspIdx) {
		HashMap<String, String> titleMap = new HashMap<>(); 
		for(TextContent textContent : menus) {
			if(textContent.getCategoryIdx() == dspIdx) {
				textContent.putTitle(titleMap);
			}
		}
		createNewCategory(titleMap.get("en"), titleMap.get("fr"), titleMap.get("zh"), dspIdx);
	}	
	
	//add a category
	private void createNewCategory(String lang1, String lang2, String lang3, int dspIdx) {
        StringBuilder sql = new StringBuilder("INSERT INTO Category(LANG1, LANG2, LANG3, displayIdx) VALUES(?, ?, ?, ")
            		.append(dspIdx).append(")");
        try {
	        AikaPos.db.execSQL(sql.toString(), new String[]{lang1,lang2,lang3});
	        BarFrame.menuPanel.initComponent();
        }catch(Exception exp) {
        	L.e("CreateNewOrder", "exception when insert a new category:" +sql , exp);
        }
	}

	//the dencity must be a null or $1
	private Dish makeSureDishExist(String category, String location, int menuIdx, String menuName, int price, String seletedPrinterIdStr) {
		//ID, CODE, MNEMONIC, SUBJECT, PRICE, FOLDERID, STORE,  COST, BRAND, CATEGORY, CONTENT, UNIT, ModifyPomp, INDEX
		StringBuilder sql = new StringBuilder("select * from Dish where deleted != true and index = ").append(menuIdx).append(" and category = ?");
        
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), new String[]{category});
			if(rs.moveToNext()) {
            	//check if the properties are all same, if not, update local to make sure local are same with server.
        		String localPrinterIdx = synchronizeLocalProduct(location, price, seletedPrinterIdStr, rs);
            	return new Dish(DBUtil.getCursorInt(rs, "_id"),
						category,
						menuName,
						DBUtil.getCursorString(rs, "MNEMONIC"),
						DBUtil.getCursorString(rs,"SUBJECT"),
						localPrinterIdx);
			}else {
            	return createNewDish(location, price, seletedPrinterIdStr, category);
            }
        } catch (SQLException e) {
            ErrorUtil.write(e);
        }
        return null;
	}
	
	//process reqeust from pad.
	public Dish makeSureDishExist(String category, String menuName, int price) {
		//ID, CODE, MNEMONIC, SUBJECT, PRICE, FOLDERID, STORE,  COST, BRAND, CATEGORY, CONTENT, UNIT, ModifyPomp, INDEX
		StringBuilder sql = new StringBuilder("select * from Dish where status = 0 and category = ? and (name1 = ? or name2 = ? or name3 = ?)");

        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), new String[]{category, menuName, menuName, menuName});
            if(rs.moveToNext()) {//check if the properties are all same, if not, update local to make sure local are same with server.
        		//TODO: consider later  String localPrinterIdx = synchronizeLocalProduct(location, price, seletedPrinterIdStr, rs);
            	return new Dish(DBUtil.getCursorInt(rs,"_id"),
						category,
						menuName,
						DBUtil.getCursorString(rs,"MNEMONIC"),
						DBUtil.getCursorString(rs,"SUBJECT"),
						"");
            }else {
            	return createNewDish(menuName, price, category);
            }
        } catch (Exception e) {
            ErrorUtil.write(e);
        }
        return null;
	}
		
	private String synchronizeLocalProduct(String location, int price, String menFu, Cursor rs) throws SQLException {
		HashMap<String, String> contentMap = new HashMap<>(); 
		for(TextContent textContent : serviceTexts) {
			String posInPage = textContent.posInPage;
			if(posInPage.contains(location)) {
				if(posInPage.endsWith("_description")) {	//if it's name of dish.
					contentMap.put(posInPage, NetUtil.fetchProductName(textContent.content, BarOption.getMoneySign()));
				}
			}
		}
		//values in web
		String name1 = contentMap.get("en_service_" + location + "_description");
		String name2 = contentMap.get("fr_service_" + location + "_description");
		String name3 = contentMap.get("zh_service_" + location + "_description");
		name1 = name1 == null ? "" : name1;
		name2 = name2 == null ? "" : name2;
		name3 = name3 == null ? "" : name3;
		
		//values in local
		String lName1 = DBUtil.getCursorString(rs, "CODE");
		String lName2 = DBUtil.getCursorString(rs, "MNEMONIC");
		String lName3 = DBUtil.getCursorString(rs, "SUBJECT");
		int lPrice = DBUtil.getCursorInt(rs, "PRICE");
		String lPrinter = DBUtil.getCursorString(rs, "BRAND");
		
		if(!name1.equals(lName1) || !name2.equals(lName2) || !name3.equals(lName3) 
				|| price != lPrice || !menFu.equals(lPrinter)) {
			String categorySQL = DBUtil.getCursorString(rs, "CATEGORY");
			StringBuilder sql = new StringBuilder("Update Product set CODE = ?, MNEMONIC = ?, SUBJECT = ?, PRICE = ").append(price)
					.append(", BRAND = '").append(menFu)
					.append("' where deleted != true and index = ").append(DBUtil.getCursorInt(rs,"INDEX")).append(" and category = ?");
            AikaPos.db.execSQL(sql.toString(), new String[]{"1", name1, name2, name3, categorySQL});
    		BarOption.updateLastModifiyTime();
			BarFrame.menuPanel.initComponent();
		}
		return menFu;
	}

	//create the new dish in the given category.
	private Dish createNewDish(String location, int price, String seletedPrinterIdStr, String category) {
		//we need to find out the 3 name of the product and the first category string of the product.
		HashMap<String, String> contentMap = new HashMap<>(); 
		for(TextContent textContent : serviceTexts) {
			String posInPage = textContent.posInPage;
			if(posInPage.contains(location)) {
				if(posInPage.endsWith("_description")) {	//if it's name of dish.
					contentMap.put(posInPage, NetUtil.fetchProductName(textContent.content, BarOption.getMoneySign()));
				}
			}
		}

		String name1 = contentMap.get("en_service_" + location + "_description");
		String name2 = contentMap.get("fr_service_" + location + "_description");
		String name3 = contentMap.get("zh_service_" + location + "_description");
		seletedPrinterIdStr = convertPrinterIPtoIdx(seletedPrinterIdStr);
		StringBuilder sql = new StringBuilder(
                 "INSERT INTO Product(name1, name2, name3, PRICE, gst, qst, size, printerIds, CATEGORY, INDEX, CONTENT, Unit, PRODUCAREA) VALUES (?,?,?,")//CODE//MNEMONIC//SUBJECT
                 .append(price).append(", ")	//PRICE
                 .append(1).append(", ")		//FOLDERID---if need gst?
                 .append(1).append(", ")		//store---if need qst?
                 .append(0).append(", '")		//Cost--size
                 .append(seletedPrinterIdStr).append("', ")	//BRAND --printer string
                 .append("?, ")		//category
                 .append(Integer.valueOf(location.substring(location.lastIndexOf("_") + 1))).append(", '")//INDEX
                 .append("false").append("', '")//CONTENT---cbxPricePomp
                 .append("false").append("', '")//Unit---cbxMenuPomp
                 .append("false").append("')");//PRODUCAREA---cbxModifyPomp
        try {
            AikaPos.db.execSQL(sql.toString(), new String[]{name1, name2, name3, category});
            
	        BarFrame.menuPanel.initComponent();
	        
	        sql = new StringBuilder("Select id from Dish where CODE = ? and category = ?");
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), new String[]{name1, category});
            
            rs.moveToNext();
            return new Dish(DBUtil.getCursorInt(rs,"_id"), category, name1, name2, name3, seletedPrinterIdStr);
        }catch(Exception exp) {
        	L.e("RequestNewOrderAction", "exception when creating new Dish.", exp);
        }
        return null;
	}
	
	//create the new dish in the given category.
	private Dish createNewDish(String menuName, int price, String category) {
		//we need to find out the 3 name of the product and the first category string of the product.
		StringBuilder sql = new StringBuilder(
                 "INSERT INTO Product(CODE, MNEMONIC, SUBJECT, PRICE, FOLDERID, store, Cost,  BRAND, CATEGORY, INDEX, CONTENT, Unit, PRODUCAREA) VALUES (?,?,?,")//CODE//MNEMONIC//SUBJECT
                 .append(price).append(", ")	//PRICE
                 .append(1).append(", ")		//FOLDERID---if need gst?
                 .append(1).append(", ")		//store---if need qst?
                 .append(0).append(", '")		//Cost--size
                 .append("").append("',")	//BRAND --printer string
                 .append("?, ")		//category
                 .append(0).append(", '")//INDEX
                 .append("false").append("', '")//CONTENT---cbxPricePomp
                 .append("false").append("', '")//Unit---cbxMenuPomp
                 .append("false").append("')");//PRODUCAREA---cbxModifyPomp
        try {
            AikaPos.db.execSQL(sql.toString(), new String[]{menuName,menuName,menuName,category});
	        BarFrame.menuPanel.initComponent();
	        
	        sql = new StringBuilder("Select id from Dish where CODE = ? and category = ?");
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), new String[]{menuName, category});
            rs.moveToNext();
            return new Dish(DBUtil.getCursorInt(rs,"_id"), category, menuName, menuName, menuName, "");
        }catch(Exception exp) {
        	L.e("RequestNewOrderAction", "exception when creating new Dish.", exp);
        }
        return null;
	}

	private String convertPrinterIPtoIdx(String seletedPrinterIdStr) {
		
		String[] ipAryFromWeb = seletedPrinterIdStr.split(",");
		if(ipAryFromWeb.length < 1) {
			return seletedPrinterIdStr;
		}
		int nonEmptyQt = BarFrame.menuPanel.getPrinters().length;
		int idxOfFirstEmpty = 0;
		for (int i = BarFrame.menuPanel.getPrinters().length - 1; i >= 0; i--) {
			Printer printer = BarFrame.menuPanel.getPrinters()[i];
			if(printer.getIp() == null || printer.getIp().length() == 0) {
				nonEmptyQt--;
				idxOfFirstEmpty = i;
			}
		}
		
		for(int i = 0; i < ipAryFromWeb.length; i++) {
			if(ipAryFromWeb[i].length() > 0) {	// some are just "", ignore this kind of element.
				boolean findedMatching = false;
				for (int idxOfLocalPrinter = 0; idxOfLocalPrinter < 6; idxOfLocalPrinter++) {	
					Printer localPrinter  = BarFrame.menuPanel.getPrinters()[idxOfLocalPrinter];
					if(ipAryFromWeb[i].equals(localPrinter.getIp())) {
						ipAryFromWeb[i] = String.valueOf(idxOfLocalPrinter);
						findedMatching = true;
						break;
					}
				}
				if(!findedMatching) {
					if(nonEmptyQt < 6) {
						updateLocalPrinter("P"+(idxOfFirstEmpty + 1), 0, 0, ipAryFromWeb[i], 0, 0, idxOfFirstEmpty);
						BarFrame.menuPanel.initPrinters();
						ipAryFromWeb[i] = String.valueOf(idxOfFirstEmpty + 1);
					}else {
						ipAryFromWeb[i] = String.valueOf(1);
						BarFrame.setStatusMes("Error! Web printer ip do not match any of local 6 printer ip! Printed on main printer temperally.");
						L.e("RequestNewOrderAction", "web printer ip do not match any of local 6 printer IP.", null);
					}
				}
			}
		}
		
		StringBuilder sb = new StringBuilder(ipAryFromWeb[0]);
		for (int i = 1; i < ipAryFromWeb.length; i++) {
			if(ipAryFromWeb[i].length() > 0) {
				sb.append(",").append(ipAryFromWeb[i]);
			}
		}
		return sb.toString();
	}

	private void updateLocalPrinter(String name, int category, int langType, String ip, int style, int status, int id) {
		String[] numStrs = ip.split("\\.");
		if(numStrs.length != 4) {
			L.e("updating localprinters", "non-ip parameter found", null);
		}
		StringBuilder sql = new StringBuilder("Update Hardware set name = '").append(name)
				.append("', category = ").append(category).append(", langType = ").append(langType)
				.append(", ip = '").append(ip).append("', style = ").append(style).append(", status = ").append(status)
				.append(" where id = ").append(id);
		try {
			AikaPos.db.execSQL(sql.toString());
    		BarOption.updateLastModifiyTime();
		}catch(Exception e) {
			L.e("RequestNewOrderAction", "exception when creating an Printer record", e);
		}
	}

	@Override
	public void setSourceBtn(ISButton sourceBtn) {

	}
}
