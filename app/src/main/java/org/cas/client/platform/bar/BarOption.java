package org.cas.client.platform.bar;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.net.BackGroundTimerThread;
import org.cas.client.platform.bar.print.Command;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.cascustomize.CustOptsConsts;
import org.cas.client.platform.casutil.TaoEncrypt;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BarOption {

	public static float bigFont = 48;
	public static float lessBigFont = 36;
	public static float moderateBigFont = 24;
	public static float normalFont = 14;
	public static final String DELETE_CMD = "~delete~";
	
	private static final String SUMMER_UTC = "-05:00A";
	private static final String WINTER_UTC = "-05:00N";
	public static final String APPAREIL_ID_FORMAT = "0000-0000-0000";
	public static final int MaxQTY = 10000;
	
	private static String qstAccount;
	private static String gstAccount;
	private static String billHeadInfo;

	//activation related-------------------------------------------------------------------
    public static String getLicense(){
    	String license = (String)CustOpts.custOps.getValue("alan");
    	if(license != null) {
    		try {
    			license = TaoEncrypt.decrypt(license, 1);
    		}catch(Exception e) {
    		}
    	}
    	return license;
    }
    public static void setLicense(String licence){
    	CustOpts.custOps.setKeyAndValue("alan", TaoEncrypt.encryptPassword(licence));
    }
    
    public static String getLimitation(){
    	String limitation = (String)CustOpts.custOps.getValue("limitation");
    	if(limitation != null) {
    		try {
    			limitation = TaoEncrypt.decrypt(limitation, 1);
    		}catch(Exception e) {
    		}
    	}
    	return limitation;
    }

    public static long getActivateTimeLeft(){
    	String activateTimeLeft = (String)CustOpts.custOps.getValue("cola");
    	if(activateTimeLeft != null) {
    		try {
    			activateTimeLeft = TaoEncrypt.decrypt(activateTimeLeft, 1);
    		}catch(Exception e) {
    		}
    	}
    	return activateTimeLeft == null ? -1 : Long.valueOf(activateTimeLeft);
    }
    public static void setActivateTimeLeft(String activateTimeLeft){
    	CustOpts.custOps.setKeyAndValue("cola", TaoEncrypt.encryptPassword(activateTimeLeft));
    }

    public static String getLastSuccessStr() {
    	String lastsuccessStr = (String)CustOpts.custOps.getValue("leeyo");
    	if(lastsuccessStr != null) {
    		try {
    			lastsuccessStr = TaoEncrypt.decrypt(lastsuccessStr, 1);
    		}catch(Exception e) {
    		}
    	}
    	return lastsuccessStr;
    }
    public static void setLastSuccessStr(String lastsuccessStr) {
    	CustOpts.custOps.setKeyAndValue("leeyo", TaoEncrypt.encryptPassword(lastsuccessStr));
    }

    //for debug mode------------------------------------------------------
    public static boolean isDebugMode() {
    	if(CustOpts.custOps == null) {
    		return false;
    	}
		return CustOpts.custOps.getValue("debug") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("debug"));
	}
    
    //for conter mode---------------------------------------
    public static void setCounterMode(boolean isCounterMode) {
    	CustOpts.custOps.setKeyAndValue("CounterMode", String.valueOf(isCounterMode));
    	CustOpts.custOps.setKeyAndValue("DeleteTableWhenClose", String.valueOf(isCounterMode));
    }
    public static boolean isCounterMode() {
    	return CustOpts.custOps.getValue("CounterMode") == null ? 
    			false : "true".equalsIgnoreCase((String)CustOpts.custOps.getValue("CounterMode"));
    }

    //why do we need a kitchenBillIdx? we want to make it clear that kitchen bill is not lost?
    public static int getCurKitchenBillIdx() {
    	String curBillIdx = String.valueOf(CustOpts.custOps.getValue("KitchenBillIdx")); 
    	if(curBillIdx == null || curBillIdx.trim().length() == 0){
        	CustOpts.custOps.setKeyAndValue("KitchenBillIdx", "1");
        	return 1;
        }else {
        	try {
        		int i = Integer.valueOf(curBillIdx);
        		return i;
        	}catch(Exception e) {
        		L.e("Custome value getting:", "wrong format found in customize value: KitchenBillIdx = " + curBillIdx, e);
            	CustOpts.custOps.setKeyAndValue("KitchenBillIdx", "1");
        		return 1;
        	}
        }
    }
    
    //pop means return next value( and set next value as current value at the same time.
    public static int popKitchenBillIdx() {
    	
        String prmKey = "KitchenBillIdx";
		String curBillIdx = (String)CustOpts.custOps.getValue(prmKey);  //and it can also be used to know which order come in first.
        if(curBillIdx == null || curBillIdx.trim().length() == 0){
        	CustOpts.custOps.setKeyAndValue(prmKey, "1");
        	return 1;
        } else if(isCounterMode() && isFirstBillOfTheDay()) {
        	CustOpts.custOps.setKeyAndValue(prmKey, "1");
        	return 1;
        } else {
        	try {
        		int i = Integer.valueOf(curBillIdx);
            	CustOpts.custOps.setKeyAndValue(prmKey, String.valueOf(i + 1));
            	return i + 1;
        	}catch(Exception e) {
        		L.e("Custome value getting:", "wrong format found in customize value: KitchenBillIdx = " + curBillIdx, e);
            	CustOpts.custOps.setKeyAndValue(prmKey, "1");
            	return 1;
        	}
        }
    }
    
    private static boolean isFirstBillOfTheDay() {
    	String date = BarUtil.df.format(new Date());
    	String dateString = date.substring(0, 11);
    	String timeString = date.substring(11);
    	String barStartTime = BarOption.getStartTime();
    	StringBuilder sql = new StringBuilder("select * from Bill where openTime > '")
				.append(dateString + barStartTime).append("'");
		try {
			Cursor cursor = AikaPos.db.rawQuery(sql.toString(), null);
			return (cursor.moveToFirst() == false && BarUtil.df.parse(dateString + timeString).getTime() > BarUtil.df.parse(dateString + barStartTime).getTime());
            //no bill with openTime of today, and currently is not the morning before the store open....
		}catch(Exception exp) {
			L.e("TablesPane", "exception happend when locateAEmptyTable: " + sql, exp);
		}
		return false;
	}
	//print related----------------------------------------------------------
	public static int getPrinterMinWaiTime() {
		return CustOpts.custOps.getValue("PrinterMinWaiTime") == null ? 5000 : 
			Integer.valueOf((String)CustOpts.custOps.getValue("PrinterMinWaiTime"));
	}

	public static String getMainFrameBK() {
		String colorStr = getBK("mainFrameBK");
		if(colorStr == null) {
			colorStr = "eeeeee";
		}
		return colorStr.startsWith("#") ? colorStr : "#" + colorStr;
	}
	public static String getAdminMainFramelBK() {
		String colorStr = getBK("adminMainFrameBK");
		if(colorStr == null) {
			colorStr = "F47733";
		}
		return colorStr.startsWith("#") ? colorStr : "#" + colorStr;
	}
	public static void setBK(String color, String key) {
		CustOpts.custOps.setKeyAndValue(key, color);
	}
	public static String getBK(String key) {
		String colorStr = (String)CustOpts.custOps.getValue(key);
		if(colorStr == null){
			return null;
		}else{
			return colorStr.startsWith("#") ? colorStr : "#" + colorStr;
		}
	}

    public static String getBillHeadInfo() {
     	if(billHeadInfo == null || billHeadInfo.length() == 0) {
	    	billHeadInfo = (String)CustOpts.custOps.getValue("jingqi");
	    	if(billHeadInfo != null && billHeadInfo.length() > 0) {
	    		try {
	    			billHeadInfo = TaoEncrypt.decrypt(billHeadInfo, 1);
	    		}catch(Exception e) {
	    			L.e("When getting billHeadInfo", "exception happend when decrypting:" + billHeadInfo, e);
	    		}
	    	}
    	}
    	return billHeadInfo;
    }
    
    public static void setBillHeadInfo(String billHeadInfo) {
    	if(billHeadInfo == null) {
    		CustOpts.custOps.setKeyAndValue("jingqi", "");
    		return;
    	}
    	billHeadInfo.replace("：", ":");
    	if(!billHeadInfo.endsWith("\n"))
    		billHeadInfo += "\n";
    	CustOpts.custOps.setKeyAndValue("jingqi", TaoEncrypt.encryptPassword(billHeadInfo));
    }
    
    public static String getBillFootInfo(){
    	String footMsg = (String)CustOpts.custOps.getValue("BillFootInfo");
       	return footMsg == null ? "" : footMsg;
    }
    public static void setBillFootInfo(String billFootInfo) {
    	if(billFootInfo == null) {
    		L.e("setting BillFoot", "trying to set null to bill foot info.", null);
    		return;
    	}
    	
    	if(!billFootInfo.endsWith("\n"))
    		billFootInfo += "\n";
    	CustOpts.custOps.setKeyAndValue("BillFootInfo", billFootInfo);
    }

    // contents in bill info=================================
	public static String getShopName() {
		String headInfo = getBillHeadInfo();;
		if (headInfo != null) {
			int p = headInfo.indexOf(':');
			if(p > 0) {
				return headInfo.substring(0, p).trim();
			}
		}
		return "no name";
	}
	public static String getAddress() {
		String headInfo = getBillHeadInfo();
		if (headInfo != null) {
			String[] elements = headInfo.split(":");
			if(elements.length > 1) {
				return elements[1];
			}
		}
		return "no address";
    }
	public static String getPostCode() {
		String headInfo = getBillHeadInfo();
		if (headInfo != null) {
			String[] elements = headInfo.split(":");
			if(elements.length > 2) {
				return elements[2];
			}
		}
		return "no postcode";
	}
	public static String getTel() {
		String headInfo = getBillHeadInfo();
		if (headInfo != null) {
			String[] elements = headInfo.split(":");
			if(elements.length > 3) {
				return elements[3];
			}
		}
		return "no phone";
	}

//	public static void setMaxRooBackWorkHour(int workhour) {
//		CustOpts.custOps.setKeyAndValue("MaxRooBackWorkHour", workhour);
//	}
//	public static int getMaxRollBackWorkHour() {
//		return CustOpts.custOps.getValue("MaxRooBackWorkHour") == null ? 
//				17 * 60 * 60 * 1000 : Integer.valueOf((String)CustOpts.custOps.getValue("MaxRooBackWorkHour"));
//	}
	
//	public static boolean isPrintBillWhenPay() {
//    	return CustOpts.custOps.getValue("PrintBillWhenPay") == null ? 
//    			false : Boolean.valueOf((String)CustOpts.custOps.getValue("PrintBillWhenPay"));
//    }
//    public static void setIsPrintBillWhenPay(Boolean isPrintBillWhenPay) {
//    	CustOpts.custOps.setKeyAndValue("PrintBillWhenPay", String.valueOf(isPrintBillWhenPay));
//    }
	
	public static boolean isDisDishIDInKitchen(){
		return CustOpts.custOps.getValue("DisDishIDInKitchen") == null ? 
    			false : Boolean.valueOf((String)CustOpts.custOps.getValue("DisDishIDInKitchen"));
	}
	
	public static void setIsDisDishIDInKitchen(boolean isDisDishIDInKitchen){
		CustOpts.custOps.setKeyAndValue("DisDishIDInKitchen", String.valueOf(isDisDishIDInKitchen));
	}
	public static boolean isDisplayBillInKitchen(){
		return CustOpts.custOps.getValue("DisplayBillInKitchen") == null ? 
    			true : Boolean.valueOf((String)CustOpts.custOps.getValue("DisplayBillInKitchen"));
	}

	public static void setIsDisplayBillInKitchen(boolean isDisplayBillInKitchen){
		CustOpts.custOps.setKeyAndValue("DisplayBillInKitchen", String.valueOf(isDisplayBillInKitchen));
	}
	
	public static boolean isDisplayWaiterInKitchen(){
		return CustOpts.custOps.getValue("displayWaiterInKitchen") == null ? 
    			false : Boolean.valueOf((String)CustOpts.custOps.getValue("displayWaiterInKitchen"));
	}
	public static void setIsDisplayWaiterInKitchen(boolean displayWaiterInKitchen){
		CustOpts.custOps.setKeyAndValue("displayWaiterInKitchen", String.valueOf(displayWaiterInKitchen));
	}
	
    public static String getMoneySign() {
    	return CustOpts.custOps.getValue("moneySign") == null ? 
    			"$" : String.valueOf(CustOpts.custOps.getValue("moneySign"));
    }
    public static void setMoneySign(String moneySign) {
    	CustOpts.custOps.setKeyAndValue("moneySign", moneySign);
    }
    
    public static boolean isDiscountAffectTax() {
    	return CustOpts.custOps.getValue("DisCountBeforeTax") == null ? 
    			false : "true".equalsIgnoreCase((String)CustOpts.custOps.getValue("DisCountBeforeTax"));
    }
    public static void setIsDisCountBeforeTax(Boolean isDisCountBeforeTax) {
    	CustOpts.custOps.setKeyAndValue("DisCountBeforeTax", String.valueOf(isDisCountBeforeTax));
    }
    
    public static boolean isServiceFeeAffectTax() {
    	return CustOpts.custOps.getValue("ServiceFeeAffectTax") == null ? 
    			false : "true".equalsIgnoreCase((String)CustOpts.custOps.getValue("ServiceFeeAffectTax"));
    }
    public static void setIsServiceFeeAffectTax(Boolean isServiceFeeAffectTax) {
    	CustOpts.custOps.setKeyAndValue("ServiceFeeAffectTax", String.valueOf(isServiceFeeAffectTax));
    }
    
    public static boolean isBuffetMode() {
    	return CustOpts.custOps.getValue("BuffetMode") == null ? 
    			false : Boolean.valueOf((String)CustOpts.custOps.getValue("BuffetMode"));
    }
    public static void setIsBuffetMode(Boolean isBuffetMode) {
    	CustOpts.custOps.setKeyAndValue("BuffetMode", String.valueOf(isBuffetMode));
    }
    
    public static int getBillPageRow() {
    	return CustOpts.custOps.getValue("BillPanel_Row") == null ? 
    			1 : Integer.valueOf((String)CustOpts.custOps.getValue("BillPanel_Row"));
    }
    public static void setBillPageRow(String row) {
    	CustOpts.custOps.setKeyAndValue("BillPanel_Row", row);
    }
    
    public static int getBillPageCol() {
    	return CustOpts.custOps.getValue("BillPanel_Col") == null ? 
    			4 : Integer.valueOf((String)CustOpts.custOps.getValue("BillPanel_Col"));
    }
    public static void setBillPageCol(String col) {
    	CustOpts.custOps.setKeyAndValue("BillPanel_Col", col);
    }
    
    public static int getCategoryRow() {
    	return CustOpts.custOps.getValue("Category_Row") == null ? 
    			3 : Integer.valueOf((String)CustOpts.custOps.getValue("Category_Row"));
    }
    public static void setCategoryRow(String row) {
    	CustOpts.custOps.setKeyAndValue("Category_Row", row);
    }
    
    public static int getCategoryCol() {
    	return CustOpts.custOps.getValue("Category_Col") == null ? 
    			5 : Integer.valueOf((String)CustOpts.custOps.getValue("Category_Col"));
    }
    public static void setCategoryCol(String col) {
    	CustOpts.custOps.setKeyAndValue("Category_Col", col);
    }
    
    public static int getDishRow() {
    	return CustOpts.custOps.getValue("Dish_Row") == null ? 
    			4 : Integer.valueOf((String)CustOpts.custOps.getValue("Dish_Row"));
    }
    public static void setDishRow(String row) {
    	CustOpts.custOps.setKeyAndValue("Dish_Row", row);
    }
    
    public static int getDishCol() {
    	return CustOpts.custOps.getValue("Dish_Col") == null ? 
    			4 : Integer.valueOf((String)CustOpts.custOps.getValue("Dish_Col"));
    }
    public static void setDishCol(String col) {
    	CustOpts.custOps.setKeyAndValue("Dish_Col", col);
    }

    public static float getCategoryAreaPortion() {
    	return CustOpts.custOps.getValue("CategoryAreaPortion") == null ? 
    			0.4f : Float.valueOf((String)CustOpts.custOps.getValue("CategoryAreaPortion"));
    }
    public static void setCategoryAreaPortion(String col) {
    	CustOpts.custOps.setKeyAndValue("CategoryAreaPortion", col);
    }
    
    public static float getMenuAreaPortion() {
    	return CustOpts.custOps.getValue("MenuAreaPortion") == null ? 
    			0.41f : Float.valueOf((String)CustOpts.custOps.getValue("MenuAreaPortion"));
    }
    public static void setMenuAreaPortion(String col) {
    	CustOpts.custOps.setKeyAndValue("MenuAreaPortion", col);
    }
    
    public static boolean isSingleUser() {
    	return !"false".equals(CustOpts.custOps.getValue("SingleUserMode")) || isCounterMode();	//if it's counter mode, then it must be single user mode.
    }
    public static void setSingleUser(String is) {
    	CustOpts.custOps.setKeyAndValue("SingleUserMode", is);
    }
    
    public static void showRecordFromOtherWaiter(boolean showRecordFromOtherWaiter) {
    	CustOpts.custOps.setKeyAndValue("showRecordFromOtherWaiter", String.valueOf(showRecordFromOtherWaiter));
    }
    public static boolean showRecordFromOtherWaiter() {
    	return CustOpts.custOps.getValue("showRecordFromOtherWaiter") == null ? 
    			false : "true".equalsIgnoreCase(String.valueOf(CustOpts.custOps.getValue("showRecordFromOtherWaiter")));
    }
    
    public static void setStartTime(String startTimeOfDay) {
    	CustOpts.custOps.setKeyAndValue("StartTimeOfDay", startTimeOfDay);
    }
    public static String getStartTime() {
    	return CustOpts.custOps.getValue("StartTimeOfDay") == null ? 
    			"00:00:00" : String.valueOf(CustOpts.custOps.getValue("StartTimeOfDay"));
    }
    
    public static void setPrinterMinReachTime(String printerMinReachTime) {
    	CustOpts.custOps.setKeyAndValue("PrinterMinReachTime", printerMinReachTime);
    }
    public static String getPrinterMinReachTime() {
    	return CustOpts.custOps.getValue("PrinterMinReachTime") == null ? 
    			"2468" : String.valueOf(CustOpts.custOps.getValue("PrinterMinReachTime"));
    }

    public static String getServerIp() {
    	String serverIp = (String)CustOpts.custOps.getValue("serverIp");
    	return serverIp == null ? "" : serverIp;
    }

    public static void setServerHost(String serverHost) {
    	CustOpts.custOps.setKeyAndValue("serverHost", serverHost);
    	BackGroundTimerThread.reInitURLString();	//a loop in thread is using a modified copy of this value.
    }
    public static String getServerHost() {
    	return CustOpts.custOps.getValue("serverHost") == null ? 
    		"" : String.valueOf(CustOpts.custOps.getValue("serverHost"));
    }
    
    public static void setShowCustomerFrame(boolean isShowCustomerFrame) {
    	CustOpts.custOps.setKeyAndValue("ShowCustomerFrame", String.valueOf(isShowCustomerFrame));
    }
	public static boolean isShowCustomerFrame() {
		return CustOpts.custOps.getValue("ShowCustomerFrame") == null ? 
    			false : "true".equalsIgnoreCase((String)CustOpts.custOps.getValue("ShowCustomerFrame"));
	}
	
	public static float getGST() {
	    	return CustOpts.custOps.getValue(BarFrame.consts.GST()) == null ?
	    			5f : Float.valueOf((String)CustOpts.custOps.getValue(BarFrame.consts.GST()));
    }
    public static void setGST(String gst) {
    	CustOpts.custOps.setKeyAndValue(BarFrame.consts.GST(), gst);
    }
    
	public static float getQST() {
    	return CustOpts.custOps.getValue(BarFrame.consts.QST()) == null ?
    			9.975f : Float.valueOf((String)CustOpts.custOps.getValue(BarFrame.consts.QST()));
	}
	public static void setQST(String qst) {
		CustOpts.custOps.setKeyAndValue(BarFrame.consts.QST(), qst);
	}

	public static String getGSTAccount() {
    	if(gstAccount == null) {
    		gstAccount = (String)CustOpts.custOps.getValue("ningmeng");
	    	if(gstAccount != null) {
	    		try {
	    			gstAccount = TaoEncrypt.decrypt(gstAccount, 1);
	    		}catch(Exception e) {
	    		}
	    	}
    	}
    	return gstAccount;
	}
	public static void setGSTAccount(String gst) {
		CustOpts.custOps.setKeyAndValue("ningmeng", TaoEncrypt.encryptPassword(gst));
	}
	public static String getQSTAccount() {
    	if(qstAccount == null) {
    		qstAccount = (String)CustOpts.custOps.getValue("aika");
	    	if(qstAccount != null) {
	    		try {
	    			qstAccount = TaoEncrypt.decrypt(qstAccount, 1);
	    		}catch(Exception e) {
	    		}
	    	}
    	}
    	return qstAccount;
	}
	public static void setQSTAccount(String qst) {
		CustOpts.custOps.setKeyAndValue("aika", TaoEncrypt.encryptPassword(qst));
	}

	public static boolean isTreatPricePromtAsTaxInclude() {
		return CustOpts.custOps.getValue("TreatPricePromtAsTaxInClude") == null ? 
    			false : "true".equalsIgnoreCase((String)CustOpts.custOps.getValue("TreatPricePromtAsTaxInClude"));
	}
	public static void setTreatPricePromtAsTaxInclude(boolean treatPricePromtAsTaxInClude) {
    	CustOpts.custOps.setKeyAndValue("TreatPricePromtAsTaxInClude", String.valueOf(treatPricePromtAsTaxInClude));
	}

	public static boolean isSavePrintInvoiceWhenBilled() {
		return CustOpts.custOps.getValue("SavePrintInvoiceWhenBilled") == null ? 
    			false : "true".equalsIgnoreCase(String.valueOf(CustOpts.custOps.getValue("SavePrintInvoiceWhenBilled")));
	}
	public static void setSavePrintInvoiceWhenBilled(boolean isSavePrintInvoiceWhenBilled) {
		CustOpts.custOps.setKeyAndValue("SavePrintInvoiceWhenBilled", String.valueOf(isSavePrintInvoiceWhenBilled));
	}
	
	public static boolean isPrintDuplicatedBill() {
		return CustOpts.custOps.getValue("PrintDuplicatedBill") == null ? 
    			false : "true".equalsIgnoreCase(String.valueOf(CustOpts.custOps.getValue("PrintDuplicatedBill")));
	}
	public static void setPrintDuplicatedBill(boolean isPrintDuplicatedBill) {
		CustOpts.custOps.setKeyAndValue("PrintDuplicatedBill", String.valueOf(isPrintDuplicatedBill));
	}
	
	public static boolean isWaiterAllowedToDiscount() {
		return CustOpts.custOps.getValue("WaiterAllowedToDiscount") == null ? 
    			false : "true".equalsIgnoreCase((String)CustOpts.custOps.getValue("WaiterAllowedToDiscount"));
	}
	public static void setIsWaiterAllowedToDiscount(boolean isWaiterAllowedToDiscount) {
		CustOpts.custOps.setKeyAndValue("WaiterAllowedToDiscount", String.valueOf(isWaiterAllowedToDiscount));
	}

	public static boolean isWaiterAllowedToChangePrice() {
		return CustOpts.custOps.getValue("WaiterAllowedToChangePrice") == null ? 
    			false : "true".equalsIgnoreCase(String.valueOf(CustOpts.custOps.getValue("WaiterAllowedToChangePrice")));
	}
	public static void setIsWaiterAllowedToChangePrice(boolean isWaiterAllowedToChangePrice) {
		CustOpts.custOps.setKeyAndValue("WaiterAllowedToChangePrice", String.valueOf(isWaiterAllowedToChangePrice));
	}
	
	public static String getBatchReportEmail() {
    	return  (String)CustOpts.custOps.getValue("MUER");
	}
	public static String getBillNumberStartStr() {
    	return CustOpts.custOps.getValue("BillNumberStartStr") == null ? 
    			"#" : (String)CustOpts.custOps.getValue("BillNumberStartStr");
	}
	public static void setBillNumberStartStr(String BillNumberStartStr) {
    	CustOpts.custOps.setKeyAndValue("BillNumberStartStr", BillNumberStartStr);
	}
	
	public static void setIsTrainingMode(String isTrainingMode) {
		CustOpts.custOps.setKeyAndValue("TrainingMode", isTrainingMode);
		
	}
	public static boolean isTrainingMode() {
		return CustOpts.custOps.getValue("TrainingMode") == null ? 
				false : "true".equalsIgnoreCase(String.valueOf(CustOpts.custOps.getValue("TrainingMode")));
	}

	public static int getListViewTableRowHeight() {
		try{
			return Integer.valueOf(String.valueOf(CustOpts.custOps.getValue("ListViewTableRowHeight")));
		}catch(Exception exp) {
			return 22;
		}
	}
	public static void setListViewTableRowHeight(String height) {
		try {
	    	CustOpts.custOps.setKeyAndValue("ListViewTableRowHeight", Integer.valueOf(height));
		}catch(Exception exp) {
			CustOpts.custOps.setKeyAndValue("ListViewTableRowHeight", null);
		}
	}
	public static int getTableRowHeight() {
		try{
			return Integer.valueOf(String.valueOf(CustOpts.custOps.getValue("TableRowHeight")));
		}catch(Exception exp) {
			return 50;
		}
	}
	public static void setTableRowHeight(String height) {
		try {
	    	CustOpts.custOps.setKeyAndValue("TableRowHeight", Integer.valueOf(height));
		}catch(Exception exp) {
			CustOpts.custOps.setKeyAndValue("TableRowHeight", null);
		}
	}
	
	public static int getHistoryDays() {
		try{
			return Integer.valueOf(String.valueOf(CustOpts.custOps.getValue("HistoryDays")));
		}catch(Exception exp) {
			return 3650;
		}
	}
//	public static void setHistoryDays(String historyDays) {
//		try {
//	    	CustOpts.custOps.setKeyAndValue("HistoryDays", Integer.valueOf(historyDays));
//		}catch(Exception exp) {
//			CustOpts.custOps.setKeyAndValue("HistoryDays", null);
//		}
//	}
	
	public static int getBigFontWidth() {
		try{
			return Integer.valueOf(String.valueOf(CustOpts.custOps.getValue("BigFontWidth")));
		}catch(Exception exp) {
			return 0;
		}
	}
//	public static void setBigFontWidth(String BigFontWidth) {
//		try {
//	    	CustOpts.custOps.setKeyAndValue("BigFontWidth", Integer.valueOf(BigFontWidth));
//		}catch(Exception exp) {
//			CustOpts.custOps.setKeyAndValue("BigFontWidth", null);
//		}
//	}
	
    public static String getSuperPassword(){
    	String superPassword = (String)CustOpts.custOps.getValue("superPassword");
    	if(superPassword != null) {
    		try {
    			superPassword = TaoEncrypt.decrypt(superPassword, 1);
    		}catch(Exception e) {
    		}
    	}
    	return superPassword == null ? "4389210090" : superPassword;
    }
    public static void setSuperPassword(String superPassword){
    	CustOpts.custOps.setKeyAndValue("superPassword", TaoEncrypt.encryptPassword(superPassword));
    }
    
	public static int[] getOpenDrawerCommand() {
		String command = (String)CustOpts.custOps.getValue("OpenDrawerCommand");
		if(command == null || command.trim().length() < 7) {
			return Command.DLE_DC4;
		}else {
			String[] strs = command.split(",");
			int[] intAry = new int[strs.length];
			for(int i = 0; i < strs.length; i++) {
				intAry[i] = Integer.valueOf(strs[i]);
			}
			return intAry;
		}
	}
	
	//this is actually the order#. not the bill#.
	//if the system crushed, we might cleaned the db from under .storm07111 to make the app restart.
	//To avoid the order# restart from 0 again, which will cause the owner lost the real number of how many dish USER has sold in current batch.
	//we have to put back the right orderIdx into the customization table, so we make a copy of current order# in config file..
	//who's using it? who "don't want to use it?"
	public static String getNextOrderId() {
		//get the value out from DB.
		StringBuilder sql = new StringBuilder("select value from customizations where key = 'NextOrderId'");
		int valueInDB = 1; //user don't want to see start from 0.
		Cursor cursor = AikaPos.db.rawQuery(sql.toString(), null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndex("VALUE");
				if (columnIndex != -1) { // 确保索引值是有效的
					valueInDB = cursor.getInt(columnIndex);
				} else {
					Log.e("DatabaseError", "Column 'value' not found in the result set.");
				}
			}
		} catch (Exception e1) {
			L.e("BarOption", "Unexpected exception when getting next order id from db with:" + sql, e1);
			sql = new StringBuilder("INSERT INTO customizations (key, value) VALUES ('NextOrderId',0)");
			try {
				AikaPos.db.execSQL(sql.toString());
			} catch (Exception e) {
				L.e("BarOption", "Unexpected exception when initialize a NextOrderId with :" + sql, e);;
			}
		} finally {
			if (cursor != null) {
				cursor.close(); // 确保释放资源
			}
		}//no need to update the "search end date", keep it the date of current bill.

		int valueInConf = 1;//user don't want to see start from 0.
		//get the value out from config file.
		Object o = CustOpts.custOps.getValue("NextIdx");
		if(o == null) {	//give a default value when first use. while if it's "", means user don't want to use nextIdx.
			o = "1";
		}
		valueInConf = Integer.valueOf(o.toString());
		
		if(valueInDB != valueInConf) {
			L.e("BarOption", "unexpected unEqual next order id found! valueInDB:" + valueInDB + "; valueInConf:" + valueInConf, null);
		}
		//use the bigger value,
		int value = valueInDB >= valueInConf ? valueInDB : valueInConf;
		
		//update db and config with the big one.
		//update db.
		sql = new StringBuilder("update customizations set value = '").append(value + 1)
 				.append("' where key = 'NextOrderId'");
		try {
     		AikaPos.db.execSQL(sql.toString());
		} catch (Exception e) {
			L.e("BarFrame ", "unexpected exception when updating bill id with:" + sql, e);
		}
		//update config
		CustOpts.custOps.setKeyAndValue("NextIdx", String.valueOf(value + 1));
		CustOpts.custOps.saveData();
		
		String strFR = String.valueOf(value);
		for(int i = 6 - strFR.length(); i > 0; i--) {
			strFR = "0" + strFR;
		}
		return strFR;
	}

	public static String getActivatedTimes() {
		try{
			Object o = CustOpts.custOps.getValue("activatedTimes");
			if(o == null) {	//give a default value when first use. while if it's "", means user don't want to use nextIdx.
				o = new SimpleDateFormat("yyMMdd").format(new Date());
				CustOpts.custOps.setKeyAndValue("activatedTimes", String.valueOf(o));
				CustOpts.custOps.saveData();
			}
			return o.toString();
		}catch(Exception e) {
			return "";
		}
	}

    public static void setActivatedTimes(String activatedTimes){
    	CustOpts.custOps.setKeyAndValue("activatedTimes", activatedTimes);
    }
    
	public static String getLastResetDate() {
		try{
			Object o = CustOpts.custOps.getValue("LastResetDateDate");
			if(o == null) {	//give a default value when first use. while if it's "", means user don't want to use nextIdx.
				o = new SimpleDateFormat("yyMMdd").format(new Date());
				CustOpts.custOps.setKeyAndValue("LastResetDate", String.valueOf(o));
				CustOpts.custOps.saveData();
			}
			return o.toString();
		}catch(Exception e) {
			return "";
		}
	}
	
	public static String getCustomerFrameBK() {
		Object o = CustOpts.custOps.getValue("CustomerFrameBK");
		if(o != null && new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), o.toString()).exists()) {	//if path setted, then get the image by the path setted.
			return o.toString();
		}else{
			return "CustomerFrameBG.jpg";
		}
	}
	public static String getTablePanelBK() {
		Object o = CustOpts.custOps.getValue("TablePanelBK");
		if(o != null && new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), o.toString()).exists()) {
			return o.toString();
		}else{
			return CustOpts.custOps.getMainPaneBGImg();
		}
	}
	public static String getBillPanelBK() {
		Object o = CustOpts.custOps.getValue("BillPanelBK");
		if(o != null && new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), o.toString()).exists()) {
			return o.toString();
		}else{
			return "Cover.jpg";
		}
	}
	public static String getSalePanelBK() {
		Object o = CustOpts.custOps.getValue("SalePanelBK");
		if(o != null && new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), o.toString()).exists()) {
			return o.toString();
		}else{
			return "Cover.jpg";
		}
	}
	public static String getSettingPanelBK() {
		Object o = CustOpts.custOps.getValue("SettingPanelBK");
		if(o != null && new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), o.toString()).exists()) {
			return o.toString();
		}else{
			return "Cover.jpg";
		}
	}

	public static void setTablePanelBK(String filePath) {
		CustOpts.custOps.setKeyAndValue("TablePanelBK", filePath);
	}
	public static void setBillPanelBK(String filePath) {
		CustOpts.custOps.setKeyAndValue("BillPanelBK", filePath);
	}
	public static void setSalePanelBK(String filePath) {
		CustOpts.custOps.setKeyAndValue("SalePanelBK", filePath);
	}
	public static void setSettingPanelBK(String filePath) {
		CustOpts.custOps.setKeyAndValue("SettingPanelBK", filePath);
	}
	
	public static int getModifyDlgWidth() {
		int width = 0;
		Object o = CustOpts.custOps.getValue("ModifyDlgWidth");
		if(o != null) {
			try {
				width = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid ModifyDlgWidth set" + o, e);
			}
		}
		return width;
	}
	public static int getModifyButtonWidth() {
		int width = 200;
		Object o = CustOpts.custOps.getValue("ModifyButtonWidth");
		if(o != null) {
			try {
				width = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid ModifyButtonWidth set" + o, e);
			}
		}
		return width;
	}
	public static int getModifyButtonHeight() {
		int width = CustOpts.BTN_HEIGHT * 3;
		Object o = CustOpts.custOps.getValue("ModifyButtonHeight");
		if(o != null) {
			try {
				width = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid ModifyButtonHeight set" + o, e);
			}
		}
		return width;
	}
	
	public static boolean isDeleteTableWhenClose() {
		return CustOpts.custOps.getValue("DeleteTableWhenClose") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("DeleteTableWhenClose"));
	}
	
	public static Object getDefaultTableName() {
		return "D";
	}

//	private static Font cmdFont = null;
//    public static Font getCmdCustFont(){
//		if(cmdFont == null) {
//	    	String fontName = (String)CustOpts.custOps.getValue("CommandBtnFontName");
//	    	String fontSize = (String)CustOpts.custOps.getValue("CommandBtnFontSize");
//	    	if(fontName == null || fontSize == null || fontName.length() == 0 || fontSize.length() == 0) {
//	    		return CustOpts.custOps.getFontOfDefault();
//	    	}
//
//	    	cmdFont = new Font(fontName, Font.BOLD, Integer.valueOf(fontSize));
//		}
//    	return cmdFont;
//    }
//
//    private static Font menuFont = null;
//    public static Font getMenuCustFont(){
//		if(menuFont == null) {
//	    	String fontName = (String)CustOpts.custOps.getValue("MenuBtnFontName");
//	    	String fontSize = (String)CustOpts.custOps.getValue("MenuBtnFontSize");
//	    	if(fontName == null || fontSize == null || fontName.length() == 0 || fontSize.length() == 0) {
//	    		return CustOpts.custOps.getFontOfDefault();
//	    	}
//
//	    	menuFont = new Font(fontName, Font.BOLD, Integer.valueOf(fontSize));
//		}
//    	return menuFont;
//    }

//    private static Font selectionFont = null;
//	public static Font getSelectionFont() {
//		if(selectionFont == null) {
//	    	String fontName = (String)CustOpts.custOps.getValue("SelectionFontName");
//	    	String fontSize = (String)CustOpts.custOps.getValue("SelectionFontSize");
//	    	if(fontName == null || fontSize == null || fontName.length() == 0 || fontSize.length() == 0) {
//	    		return CustOpts.custOps.getFontOfDefault();
//	    	}
//
//			selectionFont = new Font(fontName, Font.PLAIN, Integer.valueOf(fontSize));
//		}
//    	return selectionFont;
//	}
	
	public static boolean isCredit() {
		return CustOpts.custOps.getValue("DisplayVisaAsCredit") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("DisplayVisaAsCredit"));
	}
	
	public static int getNameColWidth(int defaultWidth) {
		Object o = CustOpts.custOps.getValue("NameColWidth");
		if(o != null) {
			try {
				defaultWidth = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid ModifyDlgWidth set" + o, e);
			}
		}
		return defaultWidth;
	}
	
	//there's a case that user don't want to see the bill list. because all the user wait in one line, and be served always one by one. 
	public static int getCounterModeTablePanelWidth() {
//		TablesPanel panel = (TablesPanel)BarFrame.instance.panels[0];
//		int opennedTableNum = 0;
//		for (TableButton tableButton : panel.btnTables) {
//			if(tableButton.getStatus() > 0) {
//				opennedTableNum ++;
//			}
//			if(opennedTableNum > 1) {
//				break;
//			}
//		}
//		if(opennedTableNum <= 1) {
//			return 0;
//		}else {
			int width = 70;
			Object o = CustOpts.custOps.getValue("CounterModeTablePanelWidth");
			if(o != null) {
				try {
					width = Integer.valueOf(o.toString());
				}catch(Exception e) {
					L.e("barOption", "non valid CounterModeTablePanelWidth set" + o, e);
				}
			}
			return width;
//		}
	}
	public static boolean stayAfterSave() {
		return CustOpts.custOps.getValue("stayAfterSave") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("stayAfterSave"));
	}
	public static String getOnselectBC() {
		String colorStr = (String)CustOpts.custOps.getValue("OnselectBC");
		if(colorStr != null) {
			return colorStr.startsWith("#") ? colorStr : "#" + colorStr;
		}
		return "#ff0000";
	}
	
	public static String getOnselectFC() {
		String colorStr = (String)CustOpts.custOps.getValue("OnselectFC");
		if(colorStr != null) {
			return colorStr.startsWith("#") ? colorStr : "#" + colorStr;
		}
		return "#ffffff";
	}

	public static int getCustomerFrameLang() {
		int lang = 1;
		Object o = CustOpts.custOps.getValue("CustomerFrameLang");
		if(o != null) {
			try {
				lang = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid ModifyDlgWidth set" + o, e);
			}
		}
		return lang > 0 ? lang - 1 : 0;
	}
	public static boolean isMultiLineCellRender() {
		return CustOpts.custOps.getValue("multiLineCellRender") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("multiLineCellRender"));
	}
	public static boolean isNeedLoginBeforeReport() {
		return CustOpts.custOps.getValue("NeedLoginBeforeReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("NeedLoginBeforeReport"));
	}

	public static boolean isPointerInputStyle() {
		return CustOpts.custOps.getValue("PointerInputStyle") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("PointerInputStyle"));
	}
	public static void setIsPointerInputStyle(Boolean isPointerInputStyle) {
    	CustOpts.custOps.setKeyAndValue("PointerInputStyle", String.valueOf(isPointerInputStyle));
	}
	public static String getAreaCodes() {
    	return (String)CustOpts.custOps.getValue("areacodes");
	}
	public static boolean needNoticeWhenOvertime() {
		return "true".equals(CustOpts.custOps.getValue("NoticeWhenOvertime"));
	}
	public static boolean showFrameDecoration() {
		return CustOpts.custOps.getValue("showFrameDecoration") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("showFrameDecoration"));
	}
	public static boolean showTaxNumber() {
		return CustOpts.custOps.getValue("showTaxNumber") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("showTaxNumber"));
	}

	public static String getBKForModify(int i) {
		String colorStr = (String)CustOpts.custOps.getValue("BKForModify" + i);
		if(colorStr != null) {
			return colorStr.startsWith("#") ? colorStr : "#" + colorStr;
		}
		
		switch (i) {
		case 1:
			return "#eba434";
		case 2:
			return "#d5eb34";
		case 3:
			return "#a4eb34";
		case 4:
			return "#5eeb34";
		case 5:
			return "#34eb5e";
		case 6:
			return "#34eb5e";
		case 7:
			return "#348ceb";
		case 8:
			return "#348ceb";
		case 9:
			return "#d834eb";
		case 10:
			return "#eb3489";
		}
		return "#eb3440";
	}
	
	public static boolean showWaiterOnOpennedTable() {
		return CustOpts.custOps.getValue("showWaiterOnOpennedTable") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("showWaiterOnOpennedTable"));
	}
	
	public static boolean showTimeOnOpennedTable() {
		return CustOpts.custOps.getValue("showTimeOnOpennedTable") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("showTimeOnOpennedTable"));
	}
	
	public static boolean showMoneyOnOpennedTable() {
		return CustOpts.custOps.getValue("showMoneyOnOpennedTable") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("showMoneyOnOpennedTable"));
	}
	
	public static int getDefaultTableWidth() {
		int defaultWidth = 69;
		Object o = CustOpts.custOps.getValue("DefaultTableWidth");
		if(o != null) {
			try {
				defaultWidth = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid DefaultTableWidth set" + o, e);
			}
		}
		return defaultWidth;
	}
	public static int getTableNameFontSize() {
		int defaultWidth = 6;
		Object o = CustOpts.custOps.getValue("TableNameFontSize");
		if(o != null) {
			try {
				defaultWidth = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid TableNameFontSize set" + o, e);
			}
		}
		return defaultWidth;
	}
	
	public static int getWaiterOnTableFontSize() {
		int defaultWidth = 6;
		Object o = CustOpts.custOps.getValue("WaiterNameFontSize");
		if(o != null) {
			try {
				defaultWidth = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid WaiterNameFontSize set" + o, e);
			}
		}
		return defaultWidth;
	}

	public static int getTimeOnTableFontSize() {
		int defaultWidth = 3;
		Object o = CustOpts.custOps.getValue("TimeOnTableFontSize");
		if(o != null) {
			try {
				defaultWidth = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid TimeOnTableFontSize set" + o, e);
			}
		}
		return defaultWidth;
	}
	
	public static int getMoneyOnTableFontSize() {
		int defaultWidth = 3;
		Object o = CustOpts.custOps.getValue("MoneyOnTableFontSize");
		if(o != null) {
			try {
				defaultWidth = Integer.valueOf(o.toString());
			}catch(Exception e) {
				L.e("barOption", "non valid MoneyOnTableFontSize set" + o, e);
			}
		}
		return defaultWidth;
	}
	
	public static String getTableFontColor() {
		String colorStr = (String)CustOpts.custOps.getValue("TableFontColor");
		if(colorStr != null) {
			return colorStr.startsWith("#") ? "#" +colorStr : colorStr;
		}
		return "#000000";
	}
	public static String getOpenTableFontColor() {
		String colorStr = (String)CustOpts.custOps.getValue("OpenTableFontColor");
		if(colorStr != null) {
			return colorStr.startsWith("#") ? "#" +colorStr : colorStr;
		}
		return "#000000";
	}
	
	public static boolean isShowDishSumaryInReport() {
		return CustOpts.custOps.getValue("showDishSumaryInReport") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("showDishSumaryInReport"));
	}
	public static boolean isShowCategorySumaryInReport() {
		return CustOpts.custOps.getValue("showCategorySumaryInReport") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("showCategorySumaryInReport"));
	}
	public static boolean isShowInvoiceSumaryInReport() {
		return CustOpts.custOps.getValue("ShowInvoiceSumaryInReport") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("ShowInvoiceSumaryInReport"));
	}
	
	public static boolean isShowWaiterAndTimeInReport() {
		return CustOpts.custOps.getValue("ShowWaiterAndTimeInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowWaiterAndTimeInReport"));
	}
	public static boolean isShowSalesInReport() {
		return CustOpts.custOps.getValue("ShowSalesInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowSalesInReport"));
	}
	public static boolean isShowPaymentSummaryInReport() {
		return CustOpts.custOps.getValue("ShowPaymentAndWaiterSummaryInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowPaymentAndWaiterSummaryInReport"));
	}
	public static boolean isShowdWaiterSummaryInReport() {
		return CustOpts.custOps.getValue("ShowPaymentAndWaiterSummaryInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowPaymentAndWaiterSummaryInReport"));
	}
	public static boolean isShowServiceTypeSummaryInReport() {
		return CustOpts.custOps.getValue("ShowServiceTypeSummaryInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowServiceTypeSummaryInReport"));
	}
	
	public static boolean isShowVoidBillSummaryInReport() {
		return CustOpts.custOps.getValue("ShowVoidBillSummaryInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowVoidBillSummaryInReport"));
	}
	public static boolean isShowRemovedItemSummaryInReport() {
		return CustOpts.custOps.getValue("ShowVoidItemSummaryInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowVoidItemSummaryInReport"));
	}
	public static boolean isShowUnclosedBillInReport() {
		return CustOpts.custOps.getValue("ShowUnclosedBillInReport") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("ShowUnclosedBillInReport"));
	}
	public static boolean isShowProcessingBill() {
		return CustOpts.custOps.getValue("ShowProcessingBill") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("ShowProcessingBill"));
	}
	public static boolean showExpiredRecordOnlyToAdmin() {
		return CustOpts.custOps.getValue("showExpiredRecordOnlyToAdmin") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("showExpiredRecordOnlyToAdmin"));
	}
	public static boolean unclosedBillFirst() {
		return CustOpts.custOps.getValue("unclosedBillFirst") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("unclosedBillFirst"));
	}
	public static void setUnclosedBillFirst(boolean selected) {
		CustOpts.custOps.setKeyAndValue("unclosedBillFirst", String.valueOf(selected));
	}
	public static boolean showTableOnBill() {
		return CustOpts.custOps.getValue("showTableOnBill") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("showTableOnBill"));
	}
	public static void setShowTableOnBill(boolean selected) {
		CustOpts.custOps.setKeyAndValue("showTableOnBill", String.valueOf(selected));
	}
	public static boolean showPriceOnMenu() {
		return CustOpts.custOps.getValue("showPriceOnMenu") == null ? true :
			"true".equals(CustOpts.custOps.getValue("showPriceOnMenu"));
	}
	public static String getReportHeaderOfSuperAdmin() {
    	return CustOpts.custOps.getValue("ReportHeaderOfSuperAdmin") == null ? 
    			"DAILY REPORT #" : (String)CustOpts.custOps.getValue("ReportHeaderOfSuperAdmin");
	}
	public static String getReportHeaderOfManager() {
    	return CustOpts.custOps.getValue("ReportHeaderOfManager") == null ? 
    			"BATCH REPORT #" : (String)CustOpts.custOps.getValue("ReportHeaderOfManager");
	}
	public static boolean CloseBatchWithUnclosedTable() {
		return CustOpts.custOps.getValue("CloseBatchWithUnclosedTable") == null ? true : 
			"true".equals(CustOpts.custOps.getValue("CloseBatchWithUnclosedTable"));
	}
	public static boolean showOrderNoOnBill() {
		return CustOpts.custOps.getValue("showOrderNoOnBill") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("showOrderNoOnBill"));
	}
	public static boolean isSameNameForbidden() {
		return CustOpts.custOps.getValue("sameNameForbidden") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("sameNameForbidden"));
	}

	public static boolean getAutomaticCloseBatch() {
		return CustOpts.custOps.getValue("automaticCloseBatch") == null ? false : 
			"true".equals(CustOpts.custOps.getValue("automaticCloseBatch"));
	}
	public static void setAutomaticCloseBatch(boolean automaticCloseBatch) {
		CustOpts.custOps.setKeyAndValue("automaticCloseBatch", automaticCloseBatch);
	}
	
	//when was the menu,category,label, printer changed last time. this flag will be used to determine if the client need to be updated.
	public static String getLastModifiedTime() {
		return CustOpts.custOps.getValue("lastModifiedTime") == null ?  "0" :
			CustOpts.custOps.getValue("lastModifiedTime").toString();
	}
	public static void updateLastModifiyTime() {
		CustOpts.custOps.setKeyAndValue("lastModifiedTime",String.valueOf(new Date().getTime()));
	}
	
	public static boolean isTemparalTableModeAllowed() {
		return CustOpts.custOps.getValue("temporalTableModeAllowed") == null ?  true :
			Boolean.parseBoolean(CustOpts.custOps.getValue("temporalTableModeAllowed").toString());
	}
	public static void setTemparalTableModeAllowed(boolean allow) {
		CustOpts.custOps.setKeyAndValue("temporalTableModeAllowed",String.valueOf(allow));
	}
	public static boolean showNumberOnTable() {
		Object showNumberOnTable = CustOpts.custOps.getValue("showNumberOnTable");
		if(showNumberOnTable == null) {
			return !BarOption.isCounterMode();
		} else {
			return Boolean.parseBoolean(showNumberOnTable.toString());
		}
	}
	public static boolean showPaymentInReport() {
		return CustOpts.custOps.getValue("showPaymentInReport") == null ?  true :
			Boolean.parseBoolean(CustOpts.custOps.getValue("showPaymentInReport").toString());
	}
	public static boolean showServerDataInReport() {
		return CustOpts.custOps.getValue("showServerDataInReport") == null ?  true :
			Boolean.parseBoolean(CustOpts.custOps.getValue("showServerDataInReport").toString());
	}
	
	public static String getCertifiedDeviceId() {
		Object idAPPRL = CustOpts.custOps.getValue("deviceid");
		return idAPPRL == null ? APPAREIL_ID_FORMAT : idAPPRL.toString();
	}
	public static void setCertifiedDeviceId(String idApprl) {
		if(idApprl == null || idApprl.length() != APPAREIL_ID_FORMAT.length()) {
			CustOpts.custOps.setKeyAndValue("deviceid", APPAREIL_ID_FORMAT);
		}else {
			CustOpts.custOps.setKeyAndValue("deviceid", idApprl);
		}
	}

	//following two codes is what mandatory must get from Revenue quebec to connect to websrm.
	public static String getMandatoryAuthorizationCode() {
		Object mandatoryAuthorizationCode = CustOpts.custOps.getValue("mandatoryAuthorizationCode");
		return mandatoryAuthorizationCode == null ? "D8T8-W8W8" : mandatoryAuthorizationCode.toString();
	}
	public static void setMandatoryAuthorizationCode(String mandatoryAuthorizationCode) {
		if(mandatoryAuthorizationCode != null && mandatoryAuthorizationCode.length() == "D8T8-W8W8".length()) {
			CustOpts.custOps.setKeyAndValue("mandatoryAuthorizationCode", mandatoryAuthorizationCode);
		}
	}
	public static String getBillingNumber() {
		Object billingNumber = CustOpts.custOps.getValue("billingNumber");
		return billingNumber == null ? "" : billingNumber.toString();
	}
	public static void setBillingNumber(String billingNumber) {
		if(billingNumber != null && billingNumber.length() > 0 && billingNumber.length() <= 32) {
			CustOpts.custOps.setKeyAndValue("billingNumber", billingNumber);
		}
	}
	
	public static String getCertificateSerialNumberSRS() {
		Object certificateSerialNumberSRS = CustOpts.custOps.getValue("certificateSerialNumberSRS");
		return certificateSerialNumberSRS == null ? null : certificateSerialNumberSRS.toString();
	}
	public static void setCertificateSerialNumberSRS( String certificateSerialNumberSRS) {
		CustOpts.custOps.setKeyAndValue("certificateSerialNumberSRS",
				certificateSerialNumberSRS == null ? "" : certificateSerialNumberSRS);
	}

	public static String getCertificateSerialNumberWebSRM() {
		Object certificateSerialNumberWebSRM = CustOpts.custOps.getValue("certificateSerialNumberWebSRM");
		return certificateSerialNumberWebSRM == null ? null : certificateSerialNumberWebSRM.toString();
	}
	public static void setCertificateSerialNumberWebSRM( String certificateSerialNumberWebSRM) {
		CustOpts.custOps.setKeyAndValue("certificateSerialNumberWebSRM",
				certificateSerialNumberWebSRM == null ? "" : certificateSerialNumberWebSRM);
	}
	
	public static void setIdSev(String idSev ) {
		if(idSev != null && idSev.length() > 0) {
			CustOpts.custOps.setKeyAndValue("idSev", idSev);
		}
	}
	public static String getIdSignedToAikaPos() {
		Object idSev = CustOpts.custOps.getValue("idSev");
		return idSev == null ? "0000000000003372" : idSev.toString();
	}
	

	public static void setIdVersi(String idVers ) {
		if(idVers != null && idVers.length() > 0) {
			CustOpts.custOps.setKeyAndValue("idVers", idVers);
		}
	}
	//for new version other than the one got certificated, here should use all "0", like "0000000000000000"
	public static String getIdVersi() {
		Object idVers = CustOpts.custOps.getValue("idVers");
		return idVers == null ? "0000000000003D96" : idVers.toString();
	}
	public static String getCertificationPassedCode() {
		Object codCertif = CustOpts.custOps.getValue("codCertif");
		return codCertif == null ? "FOB201999999" : codCertif.toString();
	}
	public static String getPartnerId() {
		Object idPartn = CustOpts.custOps.getValue("idPartn");
		return idPartn == null ? "00000000000012C6" : idPartn.toString();
	}
	public static String getCertifiedVersion() {
		Object versi = CustOpts.custOps.getValue("versi");
		return versi == null ? "003.030.0616" : versi.toString();
	}
	public static String getParentVersion() {
		Object versiParn = CustOpts.custOps.getValue("versiParn");
		return versiParn == null ? "0" : versiParn.toString();
	}
	//it must be unique for each srs.
	public static String getCertificateName() {
		String shopName = getShopName();
		//for certification only-----------
		if("Michel Untel enr.".equals(shopName)) {
			return "Certificat A";
		}
		if(shopName.length() < 8) {
			return "Certificat" + shopName;
		}
		if(shopName.length() > 32) {
			return shopName.substring(0, 32);
		}
		return shopName;
	}

	public static String getIdentificationNumber() {
		String qstAccount = getQSTAccount();
		return qstAccount.substring(0, qstAccount.toUpperCase().indexOf("TQ")).trim();
	}
	
	public static void setDBHost(String dbHost) {
		CustOpts.custOps.setKeyAndValue(CustOptsConsts.DBHOST, dbHost);
	}
	
	//the expire time of the certificate.
	public static int getExpiredWarningDays() {
		try {
			return Integer.valueOf(String.valueOf(CustOpts.custOps.getValue("expiredWarningDays")));
		}catch(Exception e) {
			return 30;
		}
	}
	public static void setExpiredWarningDays(int expiredWarningDays) {
		CustOpts.custOps.setKeyAndValue("expiredWarningDays", expiredWarningDays);
	}

	//wait for webmev to response for so many seconds, if passed, throw exception and go into offline mode.
	public static int getWebMevWaitTime() {
		try {
			return Integer.valueOf(String.valueOf(CustOpts.custOps.getValue("WebMevWaitTime")));
		}catch(Exception e) {
			return 10;
		}
	}
	public static void setWebMevWaitTime(String secs) {
		int sec = Integer.valueOf(secs);
		if(sec >= 5) {
			CustOpts.custOps.setKeyAndValue("WebMevWaitTime", secs);
		}
	}

	public static boolean isWebMevOfflineMode() {
		try {
			return Boolean.valueOf(String.valueOf(CustOpts.custOps.getValue("OfflineMode")));
		}catch(Exception e) {
			return false;
		}
	}
	public static void setWebMevOfflineMode(boolean b) {
		if(isWebMevOfflineMode() == b) {
			L.w("BarOption.setOfflineMode", "current status is already " + b, null);
			return;
		}
		CustOpts.custOps.setKeyAndValue("OfflineMode", b);
		String sql = "INSERT INTO customizations (key, value, status) VALUES ('OfflineMode', '" + BarUtil.sdf.format(new Date()) + "'," + (b ? "1)" : "0)");
		try {
			AikaPos.db.execSQL(sql.toString());
		} catch (Exception e) {
			L.e("BarOption", "Unexpected exception when saving a log of offline flag switching with :" + sql, e);;
		}
	}
	
	public static String getCharset() {
		return CustOpts.custOps.getValue("charset") == null ? "GB18030" : CustOpts.custOps.getValue("charset").toString() ;
	}
	public static void setCharset(String charset) {
		CustOpts.custOps.setKeyAndValue("charset", charset);
	}

	public static boolean hasDrawer() {
		try {
			return Boolean.valueOf(String.valueOf(CustOpts.custOps.getValue("hasdrawer")));
		}catch(Exception e) {
			return false;
		}
	}
	public static void setHasDrawer(boolean b) {
		CustOpts.custOps.setKeyAndValue("hasdrawer", b);
	}
	
//	private static Font billFont;
//	public static Font getBillFont() {
//		if(billFont == null) {
//			Object fontName = CustOpts.custOps.getValue("billFont");
//			String billFontName = fontName == null ? "Songti" : fontName.toString();
//			Object fontSize = CustOpts.custOps.getValue("billFontSize");
//			int billFontSize = fontSize == null ? 12 : Integer.valueOf(fontSize.toString());
//			billFont = new Font(billFontName, Font.PLAIN, billFontSize);
//		}
//		return billFont;
//	}
//	public static void setBillFont(String fontName) {
//		CustOpts.custOps.setKeyAndValue("billFont", fontName);
//		Object fontSize = CustOpts.custOps.getValue("billFontSize");
//		int billFontSize = fontSize == null ? 12 : Integer.valueOf(fontSize.toString());
//		billFont = new Font(fontName, Font.PLAIN, billFontSize);
//	}
//	public static void setBillFontSize(String fontSize) {
//		CustOpts.custOps.setKeyAndValue("billFont", fontSize);
//		Object fontName = CustOpts.custOps.getValue("billFont");
//		String billFontName = fontName == null ? "Songti" : fontName.toString();
//		billFont = new Font(billFontName, Font.PLAIN, Integer.valueOf(fontSize));
//	}
	
	//normally we don't set this value when app is running in QC, while it's not cecessary to calculate every time. so I put a utc as cache.
	private static String utc = null;
	public static String getUTC() {  
		if(utc != null) {
			return utc;
		}
		Object cv = CustOpts.custOps.getValue("utc");
		if(cv == null || cv.equals("")) {
			cv = BarUtil.isBetweenSummerTime() ? SUMMER_UTC : WINTER_UTC;	
		}
		utc = cv.toString();
		return utc;
	}
	
	public static String getTestCaseNumber() {
		Object testcase = CustOpts.custOps.getValue("testcase");
		return testcase == null? "000.000" :testcase.toString();
	}
	
	public static int getAutoLogoutTime() {
		Object logoutTime = CustOpts.custOps.getValue("logouttime");
		try {
			int time =  Integer.valueOf(String.valueOf(logoutTime));
			if(time < 1 || time > 100) {
				return 5;
			}
			return time;
		}catch(Exception e) {
			return 5;
		}
	}
	public static void setAutoLogoutTime(String min) {
		try {
			CustOpts.custOps.setKeyAndValue("logouttime", Integer.valueOf(min));
		}catch(Exception e) {
			CustOpts.custOps.setKeyAndValue("logouttime", 5);
		}
	}

	public static String getPlatformCharacters() {
		Object testcase = CustOpts.custOps.getValue("platformCharacters");
		return testcase == null? "FUSD" :testcase.toString();
	}
	public static void setPlatformCharacters(String platformCharacters) {
		CustOpts.custOps.setKeyAndValue("platformCharacters", platformCharacters);
	}

	public static boolean getBillingHistoryDlgModal() {
		return CustOpts.custOps.getValue("billingHistoryDlgModal") == null ?  true :
				"true".equals(CustOpts.custOps.getValue("billingHistoryDlgModal"));
	}
}
