package org.cas.client.platform.bar.model;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.CustomizedRule;
import com.just.print.db.bean.Dish;
import com.just.print.ui.fragment.LoginDlg;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;
import java.util.Date;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.panel.BillPanel;
//import org.cas.client.platform.bar.pannel.BillPanel;
//import org.cas.client.platform.bar.print.PrintService;
//import org.cas.client.platform.casutil.ErrorUtil;
//import org.cas.client.platform.casutil.L;
//import org.cas.client.platform.pimmodel.PIMDBModel;

public class DBUtil {

	public static void updateTableStatusInDB(String tableName, String openTime) {
		StringBuilder sql = new StringBuilder("update dining_Table set status = ").append(DBConsts.OPENNED)
				.append(", opentime = '").append(openTime)
				.append("' WHERE name = '").append(tableName).append("'");
		try {
			AikaPos.db.execSQL(sql.toString());
		}catch(Exception exp) {
			L.e("openATable", "exception when openning a table: " + sql, exp);
		}
	}
	//update all current output and bill with target table name, opentime and with the new billIdx?
	//@Note: why people merge table??? maybe they are friends met in restaurant, they shouldn't share same bill number for sure,
	//because they might AA when pay the bill.
	public static void moveBillToAnotherTable(String newTableName, int newBillIdx, int billID, String openTime) {
		moveOutputToTable(billID, newTableName, newBillIdx, openTime);
		moveBillToTable(billID, newTableName, newBillIdx, openTime);
	}
	//@NOTE didn't use current table as filter condition. because it have been changed.
	public static void moveOutputToTable(int billID, String newTable, int newBillIdx, String openTime) {
		StringBuilder sql = new StringBuilder("update output set SUBJECT = '").append(newTable)
				.append("', CONTACTID = ").append(newBillIdx)
				.append(", time = '").append(openTime)
				.append("' where category = ").append(billID);
		try {
			AikaPos.db.execSQL(sql.toString());
		}catch(Exception exp) {
			L.e("change table", "exception when changing table:" + sql, exp);
		}
	}
	private static void moveBillToTable(int billID, String newTable, int billIdx, String openTime) {
		StringBuilder sql;
		sql = new StringBuilder("update bill set tableName = '").append(newTable)
				.append("', BillIndex = '").append(billIdx)
				.append("' , opentime = '").append(openTime)
				.append("' where id = ").append(billID);
		try {
			AikaPos.db.execSQL(sql.toString());
		}catch(Exception exp) {
			L.e("change table", "exception when changing table for bill list:" + sql, exp);
		}
	}
	public static boolean isThereItemAddOrRemoved(int billId, boolean isPreviousBill) {
		//fetch out the "3 elements". and two format of created time

		String tableId = "", openTime = "", strCreateTime = "";
		int clientIdx = 0;
		long createTime = 0;

		String sql = "select * from bill where id = " + billId;  // 直接使用字符串连接
		Cursor cursor = AikaPos.db.rawQuery(sql, null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				tableId = getCursorString(cursor, "tableId");
				openTime = getCursorString(cursor, "opentime");
				clientIdx = getCursorInt(cursor, "billIndex");
				strCreateTime = getCursorString(cursor, "createTime");

				if (strCreateTime != null) {
					createTime = BarUtil.df.parse(strCreateTime).getTime() / 1000;
				} else {
					Log.e("DatabaseError", "Column 'createTime' not found in the result set.");
				}
			}
		} catch (Exception exp) {
			L.e("", "", exp); // 假设 L 是日志类
			return false;
		} finally {
			if (cursor != null) {
				cursor.close(); // 确保释放资源
			}
		}

    	//prepare the two format of createdTime of last bill, also two format of current bill.
		String strCreateTimeOfLastBill = "1970-01-011 00:00:00";
    	long createTimeOfLastbill = 0;
    	if(isPreviousBill) {  //the parameter already tell that the bill is "previouse bill", or call it "last bill"
    		//update the "search start date" to not so early(not "1970-01-011..."), but the time of this bill.
	    	strCreateTimeOfLastBill = strCreateTime;//(if the bill is the original one. these two time should be equals.)
	    	createTimeOfLastbill = createTime;
    		//update the "search end date", so the time range will be "since the time to now"....
    		strCreateTime = BarUtil.df.format(new Date());
    		createTime = System.currentTimeMillis() / 1000;
    	} else {				//otherwise, need to fetch out the previous bill to find the "start time" for the searching in next step.
			sql = new StringBuilder("SELECT createdTime FROM bill WHERE tableId = '").append(tableId)
			.append("' AND openTime = '").append(openTime)
			.append("' AND billIndex = ").append(clientIdx)
			.append(" AND id < ").append(billId)
			.append(" ORDER BY id DESC LIMIT 1;").toString();
			cursor = AikaPos.db.rawQuery(sql.toString(), null);
			try {
				if (cursor != null && cursor.moveToFirst()) {
					int columnIndex = cursor.getColumnIndex("createTime");
					if (columnIndex != -1) { // 确保索引值是有效的
						strCreateTimeOfLastBill = cursor.getString(columnIndex);
						// 假设您已经有正确的日期格式化方式
						Date dateTime = BarUtil.df.parse(strCreateTimeOfLastBill);
						createTimeOfLastbill = dateTime.getTime() / 1000;
					} else {
						Log.e("DatabaseError", "Column 'createTime' not found in the result set.");
					}
				}
			} catch (Exception exp) {
				//if not found, keep createTimeOfLastbill as 0;
			} finally {
				if (cursor != null) {
					cursor.close(); // 确保释放资源
				}
			}//no need to update the "search end date", keep it the date of current bill.
    	}
    	
    	//fetch out all the output created or removed during the period, 
		//also included the price changed items.
		sql = new StringBuilder("SELECT * FROM output WHERE subject = '").append(tableId)
				.append("' AND time = '").append(openTime)
				.append("' AND CONTACTID = ").append(clientIdx)
				.append(" AND ((arrearage > ").append(createTimeOfLastbill)
				.append(" AND arrearage <= ").append(createTime)
				//when there's an item was changed price, the deleted is DBConsts.itemPriceChanged, so as long as the arrearage has value of this period, means there's some thing happened and affected the money amount.
				//.append(" AND deleted = ").append(DBConsts.voided) 
				.append(")")	//maybe not new added, but new deleted dishes.
				.append(" OR (category > '").append(strCreateTimeOfLastBill)
				.append("' AND category <= '").append(strCreateTime)	//after the setting the created time, no need to check the status could be void.(if this kind item is deleted, it will be set to "deleted" not "void".
				.append("' AND (deleted is null OR deleted < ").append(DBConsts.COMPLETED)//output has 3 status, null, void, deleted. it
				.append(")))").toString();	//new created not report dishes
		//should include the price changed item.
		try {
			cursor = AikaPos.db.rawQuery(sql.toString(), null);
			return (cursor != null && cursor.moveToFirst());
		}catch (Exception exp) {
			return false;
		}
	}

	public static void updateOutput(int outputId, String fieldName, int value) {
		StringBuilder sb = new StringBuilder("update output set ").append(fieldName).append(" = ").append(value).append(" where id = ").append(outputId);
		try {
			AikaPos.db.execSQL(sb.toString());
		}catch(Exception e) {
			L.e("DBUtil", "exception when running sql:" + sb.toString(), e);
		}
	}

	public static void updateBill(int billId, String fieldName, int value) {
		StringBuilder sb = new StringBuilder("update bill set ").append(fieldName).append(" = ").append(value).append(" where id = ").append(billId);
		try {
			AikaPos.db.execSQL(sb.toString());
		}catch(Exception e) {
			L.e("DBUtil", "exception when running sql:" + sb.toString(), e);
		}
	}
	
	public static void updateBill(int billId, String fieldName, String value) {
		StringBuilder sb = new StringBuilder("update bill set ").append(fieldName)
				.append(" = '").append(value).append("' where id = ").append(billId);
		try {
			AikaPos.db.execSQL(sb.toString());
		}catch(Exception e) {
			L.e("DBUtil", "exception when running sql:" + sb.toString(), e);
		}
	}
	public static void closeTableAndBillInDB(String tableName, String openTime) {
		//all the bill should already completed when table closed except those was generated while has no output on it.
		//this is the designed time to clean those bills.
		StringBuilder sql = new StringBuilder("update bill set status = ").append(DBConsts.DELETED)
				.append(" WHERE tableName = '").append(tableName)
				.append("' and OPENTIME = '").append(openTime)
				.append("' and (status IS NULL OR status <= ").append(DBConsts.SAVED).append(")");	//there are bills which was generated during split, which status is "saved".
		try {
			AikaPos.db.execSQL(sql.toString());
			if(BarOption.isDeleteTableWhenClose()) {
				sql = new StringBuilder("delete from dining_Table WHERE name = '").append(tableName).append("'");
			}else {
				sql = new StringBuilder("update dining_Table set status = ").append(DBConsts.ORIGINAL)
						.append(", opentime = '' WHERE name = '").append(tableName).append("'");
			}
			AikaPos.db.execSQL(sql.toString());
		}catch(Exception exp) {
			L.e("change table", "exception when recover table:" + sql, exp);
		}
	}

	public static void updatePriceAndCreateTimeInBillRecord(BillPanel billPanel, int status, String createTime) {
		StringBuilder sql = new StringBuilder("update bill set total = ").append(Math.round(Float.valueOf(billPanel.valTotlePrice.getText().toString()) * 100))
				.append(", discount = ").append(Math.round(billPanel.discount))
				.append(", serviceFee = ").append(billPanel.serviceFee)
				.append(", comment = ' ").append(billPanel.getComment()).append("'");
		if(status != 0) {
			sql.append(", status = ").append(status);
		}
		if(createTime != null) {
			sql.append(", createTime = '").append(createTime).append("'");
		}
		sql.append(" where id = ").append(billPanel.getBillID());

		try {
			AikaPos.db.execSQL(sql.toString());
		} catch (Exception e) {
			L.e("DBUtil", "Excepioint in updateing bill info:" + sql, e);
		}

	}

//	public static void upateBillInfoIntoDB(BillPanel billPanel) {
//		//update the total price of the target bill,
//		//---because when add dish into the billPane, adding discount, splitting bill....all will cause the total price of the bill change.
//		StringBuilder sql = new StringBuilder("update bill set total = ").append(Math.round(Float.valueOf(billPanel.valTotlePrice.getText()) * 100))
//				.append(", discount = ").append(billPanel.discount)
//				.append(", serviceFee = ").append(billPanel.serviceFee)
//				.append(", comment = ' ").append(billPanel.getComment()).append("'")
//				.append(" where id = ").append(billPanel.getBillID());
//		try {
//			AikaStyle.db.execSQL(sql.toString());
//		}catch(Exception e) {
//			L.e("BillPane", "Excepioint in updateing bill info:" + sql, e);
//		}
//	}

	//clean the temporal info from comment.
	public static void cleanTemporalInfoInComment(BillPanel billPanel, String comment) {
		int pServiceFee = comment.lastIndexOf(BillPanel.SERVICEFEE);
		if(pServiceFee > -1) {
			int end = comment.indexOf("@", pServiceFee);
			comment = comment.substring(0, pServiceFee) + comment.substring(end + 1);
		}

		int pDiscBill = comment.lastIndexOf(BillPanel.DISCBILL); //pDB might changed because of the removing of SERVICEFEE.
		if(pDiscBill > -1) {
			int end = comment.indexOf("@", pDiscBill);
			comment = comment.substring(0, pDiscBill) + comment.substring(end + 1);
		}

		int pTransacOnly = comment.lastIndexOf(PrintService.TRANSACTION_ONLY); //pDB might changed because of the removing of SERVICEFEE.
		if(pTransacOnly > -1) {
			int end = comment.indexOf("@", pTransacOnly);
			comment = comment.substring(0, pTransacOnly) + comment.substring(end + 1);
		}

		DBUtil.updateBill(billPanel.getBillID(), "comment", comment);

		//update the bill with new createTime and new comment
	    //(it pointing to the previous record by the specified billId and createTime and subtotal, which is not in our db, but in mev system),
		DBUtil.updateBill(billPanel.getBillID(), "status", DBConsts.BILL_PRINTED);

		//NOTE: if some thing goes wrong for original bill printing, then we will have trouble (btw, it's hard to know if there's problem when printing
		//because action will prepare the content and send to the q
		billPanel.setStatus(DBConsts.BILL_PRINTED);
	}

	public static String getCursorString(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex != -1) {
			return cursor.getString(columnIndex);
		} else {
			Log.e("DatabaseError", "Column '" + columnName + "' not found in the result set.");
			return null;
		}
	}

	public static int getCursorInt(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex != -1) {
			return cursor.getInt(columnIndex);
		} else {
			Log.e("DatabaseError", "Column '" + columnName + "' not found in the result set.");
			return -1;  // 返回一个明显的无效值，调用者可以检查这个值
		}
	}

	public static Long getCursorLong(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex != -1) {
			return cursor.getLong(columnIndex);
		} else {
			Log.e("DatabaseError", "Column '" + columnName + "' not found in the result set.");
			return -1l;  // 返回一个明显的无效值，调用者可以检查这个值
		}
	}

	//For Dish=======================
	public static void changeOutputStatusInDB(Dish dish, int status) {
		if(dish.getOutputID() < 0) {
			return;
		}
		dish.setStatus(status);

		StringBuilder sql = new StringBuilder("update output set deleted = ").append(status)
				.append(", arrearage = ").append((int)(System.currentTimeMillis() / 1000))
				.append(" where id = ").append(dish.getOutputID());
		try {
			AikaPos.db.execSQL(sql.toString());
		} catch (Exception exp) {
			L.e("Dish", "exception when exec sql："+sql, exp);
		}
	}
	public static int createOutput(Dish dish, String billIndex, String createTime) {
		return createSplitedOutput(dish, billIndex, 1, createTime);
	}

	public static int createSplitedOutput(Dish dish, String billIndex, float splitRate, String createdTime) {
		// 使用PreparedStatement替代Statement以提高性能和安全性

		StringBuilder sql = new StringBuilder("INSERT INTO output(SUBJECT, CONTACTID, PRODUCTID, AMOUNT, TOLTALPRICE, DISCOUNT, CONTENT, userId, TIME, CATEGORY, FOLDERID) VALUES (")
				.append("'").append(BarFrame.instance.getCmbCurTable().getSelectedItem().toString()).append("',") // SUBJECT
				.append("'").append(billIndex).append("',") // CONTACTID
				.append(dish.getId()).append(",") // PRODUCTID
				.append(dish.getNum()).append(",") // AMOUNT
				.append(dish.getPrice() * splitRate).append(",") // TOLTALPRICE
				.append(dish.getDiscount() * splitRate).append("',") // DISCOUNT
				.append("'").append(dish.getModification()).append("',") // CONTENT
				.append(LoginDlg.USERID).append(",") // userId
				.append("'").append(BarFrame.instance.valOpenTime.getText()).append("',") // TIME
				.append("'").append(createdTime != null ? createdTime : dish.getOrderedTime()).append("',")// CATEGORY
				.append(dish.getBillID()); // FOLDERID
		AikaPos.db.execSQL(sql.toString()); // 执行插入操作
		return (int) DatabaseUtils.longForQuery(AikaPos.db, "SELECT last_insert_rowid()", null);
	}

	public static String getDisplayableNumWithX(int num) {
		//first pick out the number on 100,0000 and 10000 position
		return getDisplayableNum(num).append("x").toString();
	}
	public static StringBuilder getDisplayableNum(int num) {
		//first pick out the number on 100,0000 and 10000 position
		int pK = num /(BarOption.MaxQTY * 100);
		if(num > BarOption.MaxQTY * 100) {
			num = num %(BarOption.MaxQTY * 100);
		}
		int pS = num /BarOption.MaxQTY;
		if(num > BarOption.MaxQTY) {
			num = num % BarOption.MaxQTY;
		}
		StringBuilder strNum = new StringBuilder(String.valueOf(num));
		if(pS > 0)
			strNum.append("/").append(pS);
		if(pK > 0)
			strNum.append("/").append(pK);
		return strNum;
	}

	public static void splitOutputList(ArrayList<Dish> selectdDishAry, int splitAmount, String billIndex, int billID) {
		for(int i = 0; i < selectdDishAry.size(); i++) {
			Dish dish = selectdDishAry.get(i);
			if(billID > 0)
				dish.setBillID(billID);
			splitOutput(dish, splitAmount, billIndex, dish.getBillID());
		}
	}

	public static void splitOutput(Dish dish, int splitAmount, String billIndex) {
		splitOutput(dish, splitAmount, billIndex, 0);
	}

	public static void splitOutput(Dish dish, int splitAmount, String billIndex, int billId) {
		int num = dish.getNum();			//current amount
		//first pick out the number on 100,0000 and 10000 position
		int pK = num /(BarOption.MaxQTY * 100);
		if(num > BarOption.MaxQTY * 100) {
			num = num %(BarOption.MaxQTY * 100);
		}
		int pS = num /BarOption.MaxQTY;
		if(num > BarOption.MaxQTY) {
			num = num % BarOption.MaxQTY;
		}
		//calculate the new rate of dividing price, and the new num to update into the db.
		float splitRate = (float) 1.0 / splitAmount;	//default division rate.
		int pX = splitAmount * BarOption.MaxQTY;

		//check the new split number should be added onto pS or pK.
		if(pS > 0) {	//while if it's already a float, then reset the division rate.
			//dont' need ot do it as the total price is not price anymore, now we save final pirce in total price. splitRate = splitRate / pS;
			num += pS * BarOption.MaxQTY ;		//if more than one time division, put the number on higher position.
			pX *= 100;
		}
		if(pK > 0) {
			//splitRate = splitRate / pK;
			num += pK * BarOption.MaxQTY * 100;
			pX *= 100;
		}
		num += pX;
		if(billIndex == null) {		//updating the original output.
			StringBuilder sql = new StringBuilder("update output set amount = ").append(num)
					.append(", category = ").append(dish.getBillID())
					.append(", TOLTALPRICE = ").append(dish.getTotalPrice() * splitRate)
					.append(", discount = ").append(dish.getDiscount() * splitRate)
					.append(" where id = ").append(dish.getOutputID());
			try {
				AikaPos.db.execSQL(sql.toString());
			} catch (Exception exp) {
				L.e("Dish ", "splitOutput" + sql, exp);
			}
		}else {						//creating a splited one.
			dish.setNum(num);
			createSplitedOutput(dish, billIndex, splitRate, null);
		}
	}

	public static void splitOutputList(ArrayList<Dish> selectdDishAry, int splitAmount, String billIndex) {
		splitOutputList(selectdDishAry, splitAmount, billIndex, 0);
	}

	//For CustomizedRule
	public static float getActionPrice(CustomizedRule customizedRule) {
		if(customizedRule.getAction() >= 60) {
			return customizedRule.getAction();
		}else {
			//get the price of the dishes
			String[] ids = customizedRule.getContent().split(",");
			int totalPrice = 0;
			for(String id : ids) {
				for(Dish dish : BarFrame.menuPanel.getDishAry()) {
					if(id.equals(String.valueOf(dish.getId()))) {
						totalPrice += dish.getPrice();
						break;
					}
				}
			}
			//then calculate the discount price.
			return Math.round(Float.valueOf(totalPrice * customizedRule.getAction()) / 100f);
		}
	}
}
