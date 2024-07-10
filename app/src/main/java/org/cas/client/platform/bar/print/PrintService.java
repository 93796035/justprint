package org.cas.client.platform.bar.print;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Bill;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.Printer;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;
import com.just.print.util.StringUtils;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.i18n.BarDlgConst;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.NetUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.bar.web_srm.Transaction;
import org.cas.client.platform.bar.web_srm.ValuePossible;
import org.cas.client.platform.bar.web_srm.utiles.UtilesECDSA;
import org.cas.client.platform.bar.web_srm.utiles.UtilesQRCode;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.CASUtility;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

//If the ip of a printer is "LPT1", then will actually user com interface to drive the printer.
public class PrintService{

	private static final String REASON_MARK = "#REASON:";
	private static final String PROBLEME_COMMUNICATION = "PROBLÈME DE COMMUNICATION";
	public static final String TRANSACTION_ONLY = "#TRANSACTION_ONLY@";
	public static final String INVOICE = "TRANSACTION#";
	public static final String FACTURE_ORIGINAL = "\n                          *FACTURE ORIGINAL*";
	public static final String FACTURE_REVISEE =  "\n                *FACTURE RÉVISÉE*(Remplace 1 facture)";
	private static final String PAYMENT_REÇU =   "\n                                *PAIEMENT REÇU*";
	public static final String PAYMENT_FAIL =   "\n                                *PAIEMENT FAILED*";
	private static final String FILE = " #file:";
	private static final int KITCHEN_SIZE = 7;
	private static final int V_RFER_SIZE = 8;
	private static final int ADDI_SIZE = 9;
	private static final int RFER_SIZE = 10;
	private static final int R_RFER_MIN_SIZE = 11;
	private static final int SILENT_R_RFER_MIN_SIZE = 12;
	private static final int REPORT_MIN_SIZE = 15;
    public static Category[] allCategory;
    
    public static HashMap<String,Printer> ipPrinterMap = new HashMap<>();
    private static HashMap<String,List<Dish>> ipSelectionsMap;
    private static HashMap<String,List<String>> ipContentMap;

    static String curPrintIp = "";

    public static String SOUSTOTAL = "SOUS-TOTAL";
    public static String TPS = "TPS : ";
    public static String TVQ = "TVQ : ";

    public static String SERVICE_FEE = "Service Fee";
    public static String DISCOUNT = "Discount";
    public static String ANNULATION = "Annulation";
    public static String LATEST_PAY = "Latest Pay";
    
    private static final String KITCHEN = "KITCHEN";
	private static final String REPORT = "REPORT";
    
	public static void flushNotPrintedContentAndInitEmptyMap() {
    	flushIpContent();
        reInitIpSelectionMap();
	}

	//=======================================printing=======================================================
	private static String replaceMoney(String comment, String subTotalStr) {
		//clean the subTotal string. incase it't like "subtotal:0.00"
		int p = subTotalStr.indexOf(":");
		if(p > 0) {
			subTotalStr = subTotalStr.substring(p + 1).trim();
		}
		p = subTotalStr.indexOf(BarOption.getMoneySign());	//in case there's dollar sign like "subtotal:$0.00"
		if(p >= 0) {
			subTotalStr = subTotalStr.substring(p + BarOption.getMoneySign().length());
		}
		
		//change the comment
		p = comment.lastIndexOf(PrintUtil.OLD_SUBTOTAL);
		if(p > 0) {
			p += PrintUtil.OLD_SUBTOTAL.length();
			String firstPart = comment.substring(0, p);
			String secondPart = comment.substring(p);
			p = secondPart.indexOf("*");
			secondPart = p >0 ? subTotalStr + "\n" + secondPart.substring(p) : subTotalStr;
			comment = firstPart + secondPart;
		}
		return comment;
	}

	//The start time and end time are long format, need to be translate for print.
    public static String printTemporaryBill(BillPanel billPanel, ArrayList<Dish> saleRecords, boolean isToCustomer){

		String printerIP = routineCheckBeforePrint(true);
		ArrayList<String> formatedContent = formatContentForBill(printerIP, billPanel);
		//shouldn't change the db anymore.
		//we need to revise the formatted comment, to let the mev formatter know not need to send request to webmev.
		if(!isToCustomer) {
			//if it's internal use (it happens when user click print bill, but no item added), then
	    	formatedContent.set(formatedContent.size() - 3, ValuePossible.RE_PRINTED_INTERNAL_USE + billPanel.getComment());
		}
        loadAndFire(printerIP, formatedContent);
        return "OK";
    }
 
    //The start time and end time are long format, need to be translate for print.
    //@NOTE: not openned for now.
    public static void printTemporaryBills(List<BillPanel> unclosedBillPanels){
		flushNotPrintedContentAndInitEmptyMap();

		if(BarFrame.menuPanel.getPrinters().length == 0) {
			JOptionPane.showMessageDialog(BarFrame.consts.AddPrinterFirst());
			return;
		}
		
        String printerIP = BarFrame.menuPanel.getPrinters()[0].getIp();
		if("".equals(printerIP)) {
        	JOptionPane.showMessageDialog(BarFrame.consts.PrinterSettingError());
        	return;
        }
        if(ipContentMap.get(printerIP) == null)
        	ipContentMap.put(printerIP,new ArrayList<String>());
        List<String> contentList = ipContentMap.get(printerIP);
        
    	pushAllInOneBillContent(unclosedBillPanels, contentList, printerIP);
    	
        pushEndMessage(contentList, "");	//couldn't be an expired bill. so warry no about comment.
        contentList.add("\n\n\n\n\n");
        contentList.add(PrintUtil.CUT);
        
        printContents();
    }

    //The start time and end time are long format, need to be translate for print.
//    private static void printConbinedInvoice(List<BillPanel> unclosedBillPanels, boolean isCashBack){
//		flushIpContent();
//        reInitPrintRelatedMaps();
//        
//        String printerIP = BarFrame.menuPanel.getPrinters()[0].getIp();
//		if("".equals(printerIP)) {
//			JOptionPane.showMessageDialog(null, BarFrame.consts.PrinterSettingError());
//      	return;
//        }
//        if(ipContentMap.get(printerIP) == null)
//        	ipContentMap.put(printerIP,new ArrayList<String>());
//        List<String> contentList = ipContentMap.get(printerIP);
//        
//    	pushAllInOneBillContent(unclosedBillPanels, contentList, printerIP);
//    	
//    	//payInfo
//        pushPayInfo(unclosedBillPanels.get(0), contentList,  BarUtil.getPreferedWidth(), isCashBack);
//        
//        //end message
//        pushEndMessage(contentList);
//        contentList.add("\n\n\n\n\n");
//        contentList.add(PrintUtil.CUT);
//        
//        printContents();
//    }
    
	public static void pushAllInOneBillContent(List<BillPanel> unclosedBillPanels, List<String> contentList, String printerIP) {
        
        int tWidth = BarUtil.getPreferedWidth();
        
	    String tableIdx = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
	    String billId = String.valueOf(unclosedBillPanels.get(0).getBillID());
    	String orderNo = getOrderNoOfBill(billId);
    	if(orderNo == null) {
    		orderNo = BarOption.getNextOrderId();
    		updateOrderNoOnBillId(orderNo, billId);		//I should have check if it's exist or not first, this is a little lazy way.
    	}
        pushBillHeadInfo(contentList, tWidth, fomatBillId(billId), tableIdx, orderNo, null);
        
	    pushWaiterAndTime(contentList, tWidth, BarFrame.instance.valOpenTime.getText().toString(), LoginDlg.USERNAME);
        
        float totalSubTotal = 0.0f;
        float totalTPS = 0.0f;
        float totalTVQ = 0.0f;
        float totalPrice = 0.0f;
        StringBuilder content = new StringBuilder();
        for (BillPanel billPanel : unclosedBillPanels) {
        	content.append(getServiceDetailContent(billPanel.getOrderedDishAry(), curPrintIp, billPanel, tWidth));
        	//fetch out total price in content.
        	String tContent = content.substring(content.lastIndexOf("-") + 2); //there's a "\n".
			String[] a = tContent.split("\n");
			if(a.length >= 3) {
				String strAvT = a[0].substring(a[0].indexOf(":") + 1);
				totalSubTotal += Float.valueOf(strAvT.trim());
				String strTPS = a[1].substring(a[1].indexOf(":") + 1);
				totalTPS += Float.valueOf(strTPS.trim());
				String strTVQ = a[2].substring(a[2].indexOf(":") + 1);
				totalTVQ += Float.valueOf(strTVQ.trim());
				
	    		String strTotal = a[3].substring(a[3].indexOf(":") + 1);
	    		totalPrice += Float.valueOf(strTotal.trim());
			}
		}

        //seperator
        String sep_str2 = (String)CustOpts.custOps.getValue("sep_str2");
        if(sep_str2 == null || sep_str2.length() == 0){
            sep_str2 = BarUtil.SEP_STR2;
        }
        content.append(BarUtil.generateString(tWidth, sep_str2)).append("\n");
        //subtotal, tps, tvq
        StringBuilder subTotal = new StringBuilder(SOUSTOTAL).append(":").append(BarUtil.formatMoney(totalSubTotal));
        int lengthOfSpaceBeforeTime = tWidth - subTotal.length();
        if(lengthOfSpaceBeforeTime > 0) 
        	content.append(BarUtil.generateString(lengthOfSpaceBeforeTime, " "));        	
        content.append(subTotal).append("\n");
        
        StringBuilder tps = new StringBuilder(TPS).append(BarUtil.formatMoney(totalTPS));
        lengthOfSpaceBeforeTime = tWidth - tps.length();
        if(lengthOfSpaceBeforeTime > 0) 
        	content.append(BarUtil.generateString(lengthOfSpaceBeforeTime, " "));
        content.append(tps).append("\n");
        
        StringBuilder tvq = new StringBuilder(TVQ).append(BarUtil.formatMoney(totalTVQ));
        lengthOfSpaceBeforeTime = tWidth - tvq.length();
        if(lengthOfSpaceBeforeTime > 0) 
        	content.append(BarUtil.generateString(lengthOfSpaceBeforeTime, " "));
        content.append(tvq).append("\n");
        contentList.add(content.toString());
        //total
        contentList.add("BigFont");
        contentList.add("\n\n               Total:" + totalPrice);
        contentList.add("NormalFont");
		
	}
    
	//The start time and end time are long format, need to be translate for print.
	//needPrint: sometimes we dont need to print out content, while we always need to send mev a query.
    public static void printReceipt(BillPanel billPanel, boolean isCashBack, boolean isToCustomer, boolean needPrint){
        String printerIP = routineCheckBeforePrint(needPrint);
        //start to format and print.
        ArrayList<String> formatedContent = formatContentForReceipt(printerIP, billPanel, isCashBack, isToCustomer, !needPrint);
        loadAndFire(printerIP, formatedContent);
    }

	public static void printVoid(BillPanel billPanel, String customerIndex){
		String printerIP = routineCheckBeforePrint(true);
		ArrayList<String> formatedContent = formatContentForVoid(printerIP, billPanel, customerIndex);
        loadAndFire(printerIP, formatedContent);
    }
    
    public static void printRefund(BillPanel billPanel, int refundAmount, String customerIndex, boolean sendToMevOnly){
    	String printerIP = routineCheckBeforePrint(true);
		ArrayList<String> formatedContent = formatContentForRefund(printerIP, billPanel, refundAmount, customerIndex, sendToMevOnly);
        loadAndFire(printerIP, formatedContent);
    }
    
    public static void printReport(String printerIP, List<String> formatedContent){
    	routineCheckBeforePrint(true);
        loadAndFire(printerIP, formatedContent);
    }

    public static void loadAndFire(String printerIP, List<String> formatedContent) {
    	//load into ipContentMap
    	if(ipContentMap.get(printerIP) == null)
        	ipContentMap.put(printerIP,new ArrayList<String>());
         ipContentMap.get(printerIP).addAll(formatedContent);
         
        //flush ipContentMap!
        printContents();
	}
    
 	//use 8 as the maximum length, in case we need to add F at the end or add some charactor on top...we still not exceed 10 (the limitation of MEV)
    public static String fomatBillId(String billId) {
     	String activatedTimes = BarOption.getActivatedTimes();
     	for(int i = 8 - activatedTimes.length() - billId.length(); i > 0; i--) {
     		billId = "0" + billId;
     	}
     	return activatedTimes + billId;
 	}

 	private static String routineCheckBeforePrint(boolean needPrint) {
 		//clean last print content first, this line is put even before the checks.
     	flushNotPrintedContentAndInitEmptyMap();
     	//the checks------
 		if(BarFrame.menuPanel.getPrinters().length == 0) {
 			JOptionPane.showMessageDialog(BarFrame.consts.AddPrinterFirst());
 			throw new RuntimeException(BarFrame.consts.AddPrinterFirst());
 		}
         //there's a case that we don't print invoice: when printer is not mev print, and bill printed, and the flag of saving paper is on.
         String printerIP = BarFrame.menuPanel.getPrinters()[0].getIp();
         if("".equals(printerIP)) {
         	JOptionPane.showMessageDialog(BarFrame.consts.PrinterSettingError());
         	throw new RuntimeException(BarFrame.consts.PrinterSettingError());
         }
         
         if(!needPrint && !printerIP.toLowerCase().contains("mev")) {	//needPrint == false means bill printed and saving paper flag is set.
         	throw new RuntimeException(BarFrame.consts.PrinterSettingError());												//when called from Print button on checkListDlg, the needPrint flag should be set true.
         }
         
 		return printerIP;
 	}

 	private static String getIPofNonEmptyIpContentMapEntry(){
         for(Entry entry : ipContentMap.entrySet()){
             List<String> listTypeValue = (List<String>)entry.getValue();
             if(listTypeValue.size() > 0){
                 if(!PrintUtil.errorPrinterList.contains(entry.getKey())) {
                     return entry.getKey().toString();
                 }
             }
         }
         return null;
     }
     

    public static void printOrderList(List<Dish> selectdDishAry, String curTable, String curBill, String waiterName, boolean isCancelled){

		Printer[] printers = BarFrame.menuPanel.getPrinters();

		//ErrorUtil.(TAG,"start to translate selection into ipContent for printing.");
        if(!isIpContentMapEmpty()){
        	printContents();
        	// when there's content left from last print, we will try to print it out, and success or not, we will add new content to queue;
        	// return;
        }
        
        reInitIpSelectionMap();
        
        //1、遍历每个选中的菜，并分别遍历加在其上的打印机。并在ipSelectionsMap上对应IP后面增加菜品
        for(Dish dish : selectdDishAry){
            String printerStr = dish.getPrinterIds();
            if(StringUtils.isBlank(printerStr)) {
            	continue;
            }
            String[] printerIdx = printerStr.split(",");
            String[] ips = new String[printerIdx.length];
            for(int i = 0; i < printerIdx.length; i++) {
            	if(printerIdx[i].length() > 0) {	//some dish might has no printer set.
            		if(printerIdx[i].lastIndexOf(".") > printerIdx[i].indexOf(".")) {
            			ips[i] = printerIdx[i];
            		}else {
            			ips[i] = mapToIP(printers, Integer.valueOf(printerIdx[i]));
            		}
            	}
            }
            for(String ip: ips) {
                Printer printer = ipPrinterMap.get(ip);
                if(printer == null) {                   //should never happen, just in case someone changed db.
                    L.d("Printing to Kitchen", "Selected dish not connected with any printer yet.");
                    continue;
                }
                ipSelectionsMap.get(ip).add(dish);
            }
        }

        //2、遍历ipSelectionsMap，如对应打印机type为0, 则对其后的value(dishes)按照类别进行排序
        for(Entry entry: ipSelectionsMap.entrySet()){
            String key = (String)entry.getKey();
            List<Dish> dishList = (List<Dish>) entry.getValue();

            if(ipPrinterMap.get(key).getCategory() == 0 && dishList.size() > 0){
                //订单排序
                Collections.sort(ipSelectionsMap.get(key), new Comparator<Dish>() {
                    @Override
                    public int compare(Dish dish1, Dish dish2) {
                        String c1 = dish1.getCategory();
						String c2 = dish2.getCategory();
                        int dspIdx = 0;
                        for(int i = 0; i < allCategory.length; i++) {
                        	if(c1.equals(allCategory[i])){
                        		dspIdx = i;
                        		break;
                        	}
                        }
                        for(int i = 0; i < allCategory.length; i++) {
                        	if(c2.equals(allCategory[i])){
                        		return dspIdx - i;
                        	}
                        }
                        return (int)(c1.compareTo(c2));
                    }
                });
            }
        }

        //3、再次遍历ipSelectionsMap, 封装打印信息
        for(Entry entry: ipSelectionsMap.entrySet()){
            String printerIP = (String)entry.getKey();
            List<Dish> dishList = (List<Dish>) entry.getValue();

            if(dishList.size() > 0){
                if(ipSelectionsMap.get(printerIP) != dishList){
                    ErrorUtil.write("the dishList are different from ipSelectionsMap.get(printerIP)!");
                }

        		if (!"silent".equals(CustOpts.custOps.getValue("mode"))) {
        			ipContentMap.get(printerIP).add("Command.BEEP");
        		}
        		ipContentMap.get(printerIP).add("BigFont");
                if(ipPrinterMap.get(printerIP).getStyle() == 1){  //全单封装
                    ipContentMap.get(printerIP).add(
                    		formatContentForOrder(dishList, printerIP, curTable, curBill, waiterName, isCancelled)
                    		+ "\n\n\n\n\n");
                    ipContentMap.get(printerIP).add(PrintUtil.CUT);
                }else{   												//分单封装
                    for(Dish dish : dishList){
                        List<Dish> tlist = new ArrayList<Dish>();
                        tlist.add(dish);
                        ipContentMap.get(printerIP).add(
                        		formatContentForOrder(tlist, printerIP, curTable, curBill, waiterName, isCancelled) + "\n\n");
                        ipContentMap.get(printerIP).add(PrintUtil.CUT);
                    }
                }
        		ipContentMap.get(printerIP).add("NormalFont");
            }
            //clear the ipSelectionsMap immediately
            ipSelectionsMap.get(printerIP).clear();
        }

        //L.d(TAG, "Order is translated into ipContentMap map and ready for print.");
        printContents();
    }
    
    private static String mapToIP(Printer[] printers, int id){
    	String ip = "";
    	for (Printer printer : printers) {
			if(printer.getId() == id) {
				return printer.getIp();
			}
		}
    	return ip;
    }
  
    private static int printContents() {
    	
    	BarFrame.setStatusMes(BarFrame.consts.printing());
    	
    	int errorsAmount = 0;
        for(Entry<String,List<String>> entry : ipContentMap.entrySet()) {
        	List<String> sndMsg = entry.getValue();
        	if(sndMsg.size() > 0) {
        		String fileNameKey = null;
        		String printerName = entry.getKey();
        		//if printer is in "not working list", then don't work.
        		if(PrintUtil.errorPrinterList.contains(printerName)){
        			BarFrame.setStatusMes("Didn't print " + sndMsg.toString() + ", Please fix issue on printer: " + printerName);
        			continue;
        		}
        		
        		//the printer might be connected by serial port, or by usb, or by websocket.
        		if(printerName.startsWith("mev")) {	// if it's for mev. before print, should change to unique "mev-format", no command inside.
             		fileNameKey = doMevPrint(printerName, sndMsg);
             	} else if("serial".equalsIgnoreCase(printerName)) { 
            		fileNameKey = doSerialFilePrint(sndMsg);
            	} else if("serial2".equalsIgnoreCase(printerName)) { 
            		fileNameKey = PrintUtil.doSerialPrint(sndMsg);
            	} else {//if it's not connected to the serial port.
					CompletableFuture<String> result = PrintUtil.doWebSocketPrint(printerName, sndMsg);
					try {
						fileNameKey = result.get();
					} catch (Exception e) {
						L.e("PrintService", " printContents", e);
					}
				}
        		
            	//add the fileNameKey link into comment, or add printer into errorlist.
        		//TODO: if it's a kitchen print not a bill print, shouldn't modify the bill't info other than status to saved?
        		//while doesn't hurt.....
            	if(fileNameKey != null) {
            		//link it to bill.
            		//when app first start every day, it will need to print a empty report to activate the printer. 
            		//At that time, the sndMsg is empty.(seems not necessary now, I have stopped the print Report action.....)
                	if(sndMsg.size() > 1 && sndMsg.get(0).contains(INVOICE)) {
	            		String billNumber = sndMsg.get(0).substring(sndMsg.get(0).lastIndexOf(INVOICE) + INVOICE.length());
	            		billNumber = getBillIdFromeBillNumber(billNumber.substring(1));
	            		String oldComment = getCommentFromSndMsg(sndMsg);
	            		DBUtil.updateBill(Integer.valueOf(billNumber.trim()), "COMMENT", oldComment + FILE + fileNameKey);
                	}
            		sndMsg.clear();//clean ip content; no need to do ipContentMap.remove(entry.getKey()), because will do ipContentMap.clear later.
             	}else {
             		
             		//if it's added into error printer list, then ignore it. if not, then ask if user want to add the printer into ignore list?
//         			if(JOptionPane.showConfirmDialog(BarFrame.instance, entry.getKey() + BarFrame.consts.IgnorePrinterError(), DlgConst.DlgTitle,
//    	                    JOptionPane.YES_NO_OPTION) != 0) {
//         				//if choosed no, do nothing.
//        			}else {
//        				PrintUtil.errorPrinterList.add(entry.getKey());
//        			}
             		
         			//or if didn't print well, add to error list, because USER never want to fix the printer...they always call boss.
					JOptionPane.showMessageDialog(entry.getKey() + BarFrame.consts.PrinterSettingError(), new JOptionPane.ConfirmDialogCallback() {
						@Override
						public void onResult(boolean confirmed) {
						}
					});

					PrintUtil.errorPrinterList.add(entry.getKey());
					BarFrame.instance.btnRetryPrinter.setVisibility(View.VISIBLE);
					errorsAmount ++;
             	}
        	}
        }
        
        BarFrame.setStatusMes("");
        return errorsAmount;
    }
	
    /**
     * when called from buildTransaction, the sndMsg is cleaned, in this case the comment is the last element.
     * while in the other case, the sndMsg is not cleaned, the last eletment will be a PrintUtil.CUT, in this case, should use
     * (sndMsg.size() - 3) to locate the comment.
     */
    public static String getCommentFromSndMsg(List<String> sndMsg) {
    	String comment = "";
    	for(String string : sndMsg) {
    		if(string.contains(PrintUtil.REF_TO)) {
    			comment = string;
    		}else if(string.contains(LATEST_PAY)) {
    			comment = string;
    		}else if(string.indexOf(FILE) > 0) {
        		comment += string.substring(comment.indexOf(FILE));
        	}
    	}
		return comment;
	}

	private static String getBillIdFromeBillNumber(String billNumber) {
		billNumber = billNumber.trim();
		for(int i = 0; i < billNumber.length(); i++) {
			if(!billNumber.substring(i, i + 1).equals("0")) {
				return billNumber.substring(i);
			}
		}
		return "0";
	}

	//=====================================simple serial port print========================================
	static String doSerialFilePrint(List<String> pSndMsg) {
		//check if mev device and printer has been installed?
		PrintUtil.checkPrinter();

    	//check the msg type and do a clean to remove unnecessary lines like empty line and cut..(e.g. if it's a bill/check(6 item inside)  or a receipt/invoice (7 item inside)
		String transType = PrintService.getOutTransType(pSndMsg);	//we've added special content when prparing the content, in method:
		List<String> sndMsg = PrintService.cleanSndMsgBaseOnType(pSndMsg, transType);
		
		//build the content, 
		byte[] contentToWirteToFile = PrintUtil.buildSerialLabelContent(sndMsg);
		
		//save contents into a file
		String filePath = CASUtility.getPIMDirPath().concat(new Date().getTime() + "transaction.xml");
		return PrintUtil.printSerialFormattedContent(Paths.get(filePath), contentToWirteToFile);
	}
	
    //==============================================mev print part===========================================
	//this method is currently works for creating bill. 
    private static String doMevPrint(String printerName, List<String> pSndMsg) {
    	//check if mev device and printer has been installed?
    	PrintUtil.checkPrinter();

    	//check the msg type and do a clean to remove unnecessary lines like empty line and cut..(e.g. if it's a bill/check(6 item inside)  or a receipt/invoice (7 item inside)
    	//@NOTE: we know the message type(bill, receipt, rp, db base on the sndMsg's length.
    	String transType = getOutTransType(pSndMsg);	//we've added special content when preparing the content.----????bad comment! should mention what's the special content.
    	List<String> sndMsg = cleanSndMsgBaseOnType(pSndMsg, transType);
    	
    	//build the content, 
    	String mainContent;
    	if(transType.equals(REPORT)) {
    		mainContent =  buildMevReportContent(sndMsg);
        	//save contents into a file and send it to mev.
    		return printMevFormattedContent(mainContent, null, "");
    	}else if(transType.equals(KITCHEN)) {	//just a backup of kitchen bill, no need to send to web mev.
    		mainContent = buildKitchenContent(sndMsg);
        	//save contents into a file and send it to mev.
    		return printMevFormattedContent(mainContent, null, "");
    	}else {//other types has same print format (ADDI, RFER, R(eturn)_RFER, V(oid)_RFER)
			//if not for internal use, must send request.
    		boolean isForInternalUse = sndMsg.get(sndMsg.size() - 1).contains(ValuePossible.RE_PRINTED_INTERNAL_USE);
    		List<Object> responseStrs = isForInternalUse ? null : sendWebMevBillRequest(sndMsg, transType);  //send request to webMev and generate the QRCode Image in temporal folder.
    		//if only for send to webmev, then no need to print.
    		if(ValuePossible.S_RFER.equals(transType) 
    				|| sndMsg.get(sndMsg.size() - 1).contains(TRANSACTION_ONLY)
    				|| sndMsg.get(sndMsg.size() - 1).endsWith(PAYMENT_FAIL)) {
    			return "NONeedPrint";
    		}
    		mainContent = buildMevBillFormat(sndMsg, transType); //@note must do this after the sendWebMevBillRequest() method, because the transction id and time might be changed if request failed.
    		//send to printer
    		String strFR = null;
    		int p = printerName.indexOf("192.168.");  //local printer
    		p = p < 0 ? printerName.indexOf("10.") : p;
    		if(p > -1) {
    			printerName = printerName.substring(p);
    			p = printerName.indexOf(')');
    			if(p > 0) {
    				printerName = printerName.substring(0, p);
    			}
    			strFR = PrintUtil.doWebSocketPrint(
						printerName.trim(),
						mainContent,
    					responseStrs == null ? null : (Bitmap) responseStrs.get(0),
    					responseStrs == null ? null : responseStrs.get(1).toString());
    			//in case user want to always print duplicated temporal bill automatically
    			if(BarOption.isPrintDuplicatedBill() && transType.equals(ValuePossible.TypTrans.ADDI)) {
    				PrintUtil.doWebSocketPrint(printerName.trim(), mainContent, (Bitmap)responseStrs.get(0), responseStrs.get(1).toString());
    			}
			} else {									//cloud printer
				strFR = printMevFormattedContent(mainContent,
						responseStrs == null ? null : (Bitmap)responseStrs.get(0),
						responseStrs == null ? null : responseStrs.get(1).toString());
				//in case user want to always print duplicated temporal bill automatically
				if(BarOption.isPrintDuplicatedBill() && transType.equals(ValuePossible.TypTrans.ADDI)) {
					printMevFormattedContent(mainContent, (Bitmap)responseStrs.get(0), responseStrs.get(1).toString());
				}
			}
    		return strFR;
    	}
	}
    
	private static List<Object> sendWebMevBillRequest(final List<String> sndMsg, String transType) {
    	Transaction transActu = new Transaction( );
		try {
			MevUtils.buildTransaction(sndMsg, transType, transActu);
		} catch (Exception e) {
			L.e("PrintService", "Exception when generatingTransaction for web mev.", e);
			return null;
		}

		// Transaction's JSON document
		String jsonTransaction = MevUtils.getCompleteJSONStr(transActu);
		// Digital signature of the request header
        String signatransm = 
        		Base64.getEncoder().encodeToString(
        				UtilesECDSA.getSignedText(BarOption.getMandatoryAuthorizationCode() + BarOption.getCertifiedDeviceId() + transActu.signa.Actu)
        		);

		// Creation of the URL for the QR Code, with the WEB-SRM certificate to use its
		// public key to encrypt the information
		String strUrlQRCode = UtilesQRCode.createUrlQRCode(transActu,
				BarOption.getCertificateSerialNumberWebSRM());

		List<Object> responses = new ArrayList<Object>();
		// Creation of the image containing the QR code
		try {
			responses.add(UtilesQRCode.getQRCodeImage(strUrlQRCode, 188,  188, 0));//why 250, 70 seems doesn't change anything?
		} catch (Exception e) {
			L.e("MevUtils", "exception when generating codeQR for transaction", e);
		}
		
		// now that we've got everything ready, Send the transaction to the WEB-SRM (yeah need to also check if there's records in batch.
		String response = MevUtils.flushBatchWithCurTransaction(jsonTransaction, signatransm, transActu.EmprCertifSEV, strUrlQRCode, transActu.NoTrans);
		if(response == null) {
			responses.add("\nPROBLÈME DE COMMUNICATION\n" + BarOption.getCertifiedDeviceId());
		}else {
			responses.add("\nConsulter la transaction en lingne\n" + response);
		}
		return responses;
	}

	private static byte[] buildWebMevReportContent(List<String> sndMsg) {
		// TODO Auto-generated method stub
		return null;
	}

	//base on different type cleaned useless part, clean the sndMsg to left only useful elements.
	static List<String> cleanSndMsgBaseOnType(List<String> pSndMsg, String transType) {
		List<String> sndMsg = new ArrayList<String>(pSndMsg);
    	switch (transType) {
			case ValuePossible.V_RFER:		//voided receipt 
				sndMsg.remove(7);
				sndMsg.remove(5);
	    		sndMsg.remove(3);
				break;
			
	    	case ValuePossible.TypTrans.ADDI:	//check
				sndMsg.remove(8);
	    		sndMsg.remove(7);
	    		sndMsg.remove(5);
				sndMsg.remove(3);
				String total = sndMsg.get(3);
				int p = total.indexOf(":");
				//NOTE:there's a \n at the end of total, use PreferedWidth() - (total.length() - 1)
				total = total.substring(0, p + 1) + BarUtil.generateString(BarUtil.getPreferedWidth() - (total.length() - 1), " ") + total.substring(p+1);
				sndMsg.remove(3);
				sndMsg.add(3, total);
				
				break;
				
			case ValuePossible.RFER:		//receipt
				sndMsg.remove(9);
	    		sndMsg.remove(8);
	    		sndMsg.remove(5);
	    		sndMsg.remove(3);
				break;
				
			case ValuePossible.R_RFER:		//refund receipt //TODO: when we reprint receipt, we should make the msg added with a new Item like "re-printed invoice".
				sndMsg.remove(10);
	    		sndMsg.remove(9);
	    		sndMsg.remove(8);
	    		sndMsg.remove(6);
	    		sndMsg.remove(5);
	    		sndMsg.remove(4);
	    		sndMsg.set(sndMsg.size() - 1, sndMsg.get(sndMsg.size() - 1) + ValuePossible.RE_FUND);
				break;

			case ValuePossible.S_RFER:		//refund receipt //TODO: when we reprint receipt, we should make the msg added with a new Item like "re-printed invoice".
				sndMsg.remove(11);
				sndMsg.remove(10);
	    		sndMsg.remove(9);
	    		sndMsg.remove(8);
	    		sndMsg.remove(6);
	    		sndMsg.remove(4);
				break;
				
			case REPORT:		//report //TODO: when we reprint receipt, we should make the msg added with a new Item like "re-printed invoice.".
	    		sndMsg.remove(sndMsg.size() - 1);
	    		sndMsg.remove(sndMsg.size() - 1);
	    		for (int i = sndMsg.size() - 1; i > 0; i--) {
	    			if(sndMsg.get(i) == null || sndMsg.get(i).trim().length() == 0) {
	    				sndMsg.remove(i);
	    			}else {
	    				break;
	    			}
	    		}
				break;
				
			case KITCHEN:
				if(sndMsg.get(0).contains("Command.BEEP")) {
					sndMsg.remove(0);
				}
				sndMsg.remove(sndMsg.size() - 1);	//remove the last Normal font
				sndMsg.remove(sndMsg.size() - 1);	//remove the last one (now the last one is cut)
				sndMsg.remove(0);
				
				for (int i = sndMsg.size() - 1; i >= 0; i--) {
					if(sndMsg.get(i).equals(PrintUtil.CUT)) {
						sndMsg.remove(i);
					}
				}
				break;
				
			default:
				break;
		}
		return sndMsg;
	}

	//if qrcodeImg is null, means it's internal used reprint or there's exception when sending request to server.
	private static String printMevFormattedContent(String contentToWriteToFile, Bitmap qrcodeImg, String contentToWirteToFile2) {

		//print the text part content and qrcode content out.
		String key = PrintUtil.combinedPrint(contentToWriteToFile, qrcodeImg, contentToWirteToFile2);
				//PrintUtil.printThroughOSdriver(PrintUtil.getMevCommandFilePath(path1, null), false) && PrintUtil.combinedPrint(null, qrcodeImg, path2);
				//PrintUtil.combinedPrintToPDF(path1, qrcodeImg, path2);
				//PrintUtil.printThroughOSdriver(path1, qrcodeImg, path2, CustOpts.custOps.getDeleteOriginalTransaction());

		if(key == null) {
			JOptionPane.showMessageDialog(BarFrame.consts.PrinterError());
		}

		return key; //note:returning only the key part of the the whole file path.
	}
	
	private static String getOutTransType(List<String> sndMsg) {
		if(sndMsg.size() <= KITCHEN_SIZE) {	//if it's KITCHEN BILL, allow it to come out from mev printer, because it's better than only thrown out an exception. 
			return KITCHEN;
		}else if(sndMsg.size() == ADDI_SIZE) {			//currently if it's bill/check, sndMsg has 9 element. 
			return ValuePossible.TypTrans.ADDI;
		}else if (sndMsg.size() == RFER_SIZE) {			//currently if it's original receipt/invoice, sndMsg has 10 element. 
			return ValuePossible.RFER;
		}else if(sndMsg.size() == V_RFER_SIZE) {		//currently if it's VOID receipt/invoice, sndMsg has 8 element. 
			return ValuePossible.V_RFER;
		}else if (sndMsg.size() == R_RFER_MIN_SIZE) {	//currently if it's Refund/invoice, sndMsg has 12 element. 
			return ValuePossible.R_RFER;
		}else if (sndMsg.size() == SILENT_R_RFER_MIN_SIZE) {	//currently if it's Refund/invoice, sndMsg has 12 element. 
			return ValuePossible.S_RFER;
		}else if (sndMsg.size() >= REPORT_MIN_SIZE) {	//currently if it's Refund/invoice, sndMsg has 12 element. 
			return REPORT;
		}
		return "";
	}

	private static String buildMevReportContent(List<String> sndMsg) {
		//the contents could be composed by :1/Command.BEEP 2/BigFont 3/NormalFont 4/cut 5/content.
		//while when sending to mev device, the sndMsg could only be content. and length will always be 1.
		StringBuilder printContent = new StringBuilder();
		for (int i = 0; i < sndMsg.size(); i++) {					
			printContent.append(sndMsg.get(i));
		}
		
		return printContent.toString();
	}
	
	private static String buildKitchenContent(List<String> sndMsg) {
		//the contents could be composed by :1/Command.BEEP 2/BigFont 3/NormalFont 4/cut 5/content.
		//while when sending to mev device, the sndMsg could only be content. and length will always be 1.
		StringBuilder printContent = new StringBuilder();
		for (int i = 0; i < sndMsg.size(); i++) {					
			printContent.append(sndMsg.get(i));
		}
		
		return printContent.toString();
	}
	//the contents for printers could be composed by :1/Command.BEEP 2/BigFont 3/NormalFont 4/cut 5/content.
	//while when sending to mev device, the sndMsg could only be content. and length will always be 1.
	public static byte[] mergeByteArrays(byte[] array1, byte[] array2) {
	    int length1 = array1.length;
	    int length2 = array2.length;
	    
	    byte[] result = new byte[length1 + length2];
	    
	    System.arraycopy(array1, 0, result, 0, length1);
	    System.arraycopy(array2, 0, result, length1, length2);
	    
	    return result;
	}

	public static byte[] convertImageToByteArray(Bitmap image, Bitmap.CompressFormat format) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		image.compress(format, 100, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}
    
	//the contents for printers could be composed by :1/Command.BEEP 2/BigFont 3/NormalFont 4/cut 5/content.
	//while when sending to mev device, the sndMsg could only be content. and length will always be 1.
    private static String buildMevBillFormat(List<String> sndMsg, String transType) {
		
		StringBuilder printContent = new StringBuilder();	//add every thing, except the comment part
		for (int i = 0; i < sndMsg.size() - 1; i++) {
			printContent.append(sndMsg.get(i));
		}
		//insert the foot message, position is above the equal line. and at the end of details part (according to the picture example of printed bill)
		int pos = printContent.indexOf(BarUtil.getSeperatorLine(1));
		String string = BarUtil.splitStringToThreshold(BarOption.getBillFootInfo(), 52);
		if(ValuePossible.TypServ.LVT.equals(MevUtils.getServiceType(sndMsg))) {
			string = string + "   *COMMANDEPAR UNE PLATEFORME NUMÉRIQUE*";
		}
		printContent = new StringBuilder(printContent.substring(0,  pos) + "\n" + string + "\n" +  printContent.substring(pos));
		//append the bill Type
		printContent.append("\n" + getInvoiceType(sndMsg));
		return printContent.toString();
//		//=====================convert to ASCII and return========================
//		try {
//			byte[] textPart = printContent.toString().getBytes(BarOption.getCharset());//"857");
//			return textPart;
//		} catch (UnsupportedEncodingException e) {
//			ErrorUtil.write(e);
//			return null;
//		}
	}
	
	//decide what to display before the qrcode.FACTURE_ORIGINAL or PAYMENT_RECU or REPRINT......
	private static String getInvoiceType(List<String> sndMsg) {
		// prepare the message which will be inserted above "===========" 
		String endMessage = sndMsg.get(sndMsg.size() - 1);
		
		if(endMessage.contains(ValuePossible.VOID)){
			return ValuePossible.VOID;
		}else if(endMessage.contains(ValuePossible.RE_FUND)){
			return ValuePossible.RE_FUND;
		}else if(endMessage.contains(ValuePossible.RE_PRINTED)) {
			return ValuePossible.RE_PRINTED;
		}else if(endMessage.contains(ValuePossible.RE_PRINTED_INTERNAL_USE)) {
			return ValuePossible.RE_PRINTED_INTERNAL_USE;
		}else if(endMessage.contains(LATEST_PAY)) {
			return PAYMENT_REÇU;
		}else if (!endMessage.contains(PrintUtil.REF_TO)) { // for only original invoice case. the last one is pure welcome message
			return FACTURE_ORIGINAL;
		}else {
			return FACTURE_REVISEE;
		}
	}

    private static boolean isComposedByTwoNumber(String substring) {
    	String[] nums = substring.trim().split(",");
    	if(nums.length != 2) {
    		return false;
    	}
    	try {
    		Long.parseLong(nums[0]);
    		Long.parseLong(nums[1]);
    	}catch(Exception e) {
    		return false;
    	}
		return true;
	}

	private static List<List<String>> prepareEmptyListForOldMoneys() {
    	ArrayList<List<String>> oldMoneysAry = new ArrayList<List<String>>();
    	List<String> oldMoneys = new ArrayList<String>();
    	oldMoneys.add(null);
    	oldMoneys.add(null);
    	oldMoneys.add(null);
    	oldMoneys.add(null);
    	oldMoneysAry.add(oldMoneys);
		return oldMoneysAry; 
	}
    
    //Figure out the bill Number
    private static String buildBillID(String content) {
		String tBillNumberStartStr = BarOption.getBillNumberStartStr();
		
		int tStartPos = content.lastIndexOf(tBillNumberStartStr);
		if(tStartPos >= 0) {
			String tBillID = content.substring(tStartPos + tBillNumberStartStr.length(), content.indexOf("\n", tStartPos)); //billID
			return tBillID;
		} else {
			L.e("Printer Serice", "unexpected content found when building BillID:"+content, null);
			return null;
		}
    }
    
    //build the numeroTrans
    private static String buildTransNumber(String tBillID, String transType) {
		String numeroTrans = tBillID;
		if(transType.endsWith(ValuePossible.RFER))
			numeroTrans += "F";

		String strPre = BarOption.getBillNumberStartStr();
		if(numeroTrans.lastIndexOf(strPre) > -1) {
			numeroTrans = numeroTrans.substring(numeroTrans.lastIndexOf(strPre));
		}
		if(numeroTrans.length() > 10) {	//make sure the number not too long. because the mev accept max 10 character for this field.
			numeroTrans = strPre + numeroTrans.substring(strPre.length() + numeroTrans.length() - 10); 
		}
		
		return numeroTrans;
	}
    
	private static String formatMoneyForMev(String newValue, String oldValue, boolean isRefund) {
		float finalValue = Float.valueOf(newValue) - (oldValue == null ? 0.0f : Float.valueOf(oldValue));
		newValue = BarUtil.formatMoney(finalValue);
		
		StringBuilder stringFR = new StringBuilder();
    	String cleanText = newValue.trim();
    	
    	if(newValue.startsWith("-")) {
    		cleanText = cleanText.substring(1);
    		stringFR.append("-");
    	} else if(isRefund) {
    		stringFR.append("-");
    	} else {
    		stringFR.append("+");
    	}
    	
    	for(int i = 1; i < 10 - cleanText.length(); i++) {
    		stringFR.append("0");
    	}
		return stringFR.append(cleanText).toString();
	}

	private static ArrayList<String> formatContentForBill(String printerIP, BillPanel billPanel) {
		ArrayList<String> strAryFR = new ArrayList<String>();
		int tWidth = BarUtil.getPreferedWidth();
        
        //push head info
	    String tableIdx = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
        String billId = String.valueOf(billPanel.getBillID());
    	String orderNo = getOrderNoOfBill(billId);  //if exsits order number then return order number, otherwise call getNextOrderNumber to get one.
    	if(orderNo == null) {
    		orderNo = BarOption.getNextOrderId();
    		updateOrderNoOnBillId(orderNo, billId);
    	}
	    pushBillHeadInfo(strAryFR, tWidth, fomatBillId(billId), tableIdx, orderNo, billPanel.customerIndex);
	    
	    //push bill waiter and time
	    StringBuilder startTimeStr = new StringBuilder(BarUtil.df.format(new Date()));	//I decide to use current time, because there's a case of revising 
	    	                         //a bill, when we need to specify the bill time in ref part. while for the first bill, we don't need to set the ref part.
	    pushWaiterAndTime(strAryFR, tWidth, startTimeStr.toString(), LoginDlg.USERNAME);
	    
	    //push content and total
	    String content = getServiceDetailContent(billPanel.getOrderedDishAry(), printerIP, billPanel, tWidth)
	    		.append(getContentOfTotalPart(billPanel)).toString();
	    int pEnd = content.lastIndexOf("\n");
	    int pStart = content.substring(0, pEnd).lastIndexOf("\n");//ignore the "\n" at the end
	    String lastRow = content.substring(pStart, pEnd);
	    
	    strAryFR.add(content.substring(0, pStart + 1));
	    strAryFR.add("BigFont");	    //push bigger font. while it will not work for mev.
	    strAryFR.add(lastRow);
	    strAryFR.add("NormalFont");     //push normal font
        
        //push end message, if already printed temporary bill, should put reprint here, while in printTemporalBill Mehtod will check if the reprint is for internal use, will replace it with other string.
        pushEndMessage(strAryFR, FACTURE_ORIGINAL);
        
        //push cut
        strAryFR.add("\n\n\n\n\n");
        strAryFR.add(PrintUtil.CUT);
        
        return strAryFR;
	}
	
    private static ArrayList<String> formatContentForReceipt(String curPrintIp, BillPanel billPanel, boolean isCashBack,
    		boolean isToCustomer, boolean sendToMevOnly){
    	ArrayList<String> strAryFR = new ArrayList<String>();
    	int tWidth = BarUtil.getPreferedWidth();

    	//push head info
	    String tableIdx = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
	    String billId = String.valueOf(billPanel.getBillID());
    	String orderNo = getOrderNoOfBill(billId);
    	if(orderNo == null) {
    		orderNo = BarOption.getNextOrderId();
    		updateOrderNoOnBillId(orderNo, billId);		//I should have check if it's exist or not first, this is a little lazy way.
    	}
	    pushBillHeadInfo(strAryFR, tWidth, fomatBillId(billId), tableIdx, orderNo, billPanel.customerIndex);
	    
	    //push waiter and time
	    StringBuilder startTimeStr = new StringBuilder(BarFrame.instance.valOpenTime.getText());
	    if(startTimeStr.length() == 0) {
	    	startTimeStr.append(BarUtil.df.format(new Date()));
	    }
	    pushWaiterAndTime(strAryFR, tWidth, startTimeStr.toString(), LoginDlg.USERNAME);
	    
	    //service details
	    String content = getServiceDetailContent(billPanel.getOrderedDishAry(), curPrintIp, billPanel, tWidth).append(getContentOfTotalPart(billPanel)).toString();
	    int pEnd = content.lastIndexOf("\n");
	    int pStart = content.substring(0, pEnd).lastIndexOf("\n");//ignore the "\n" at the end
	    String lastRow = content.substring(pStart, pEnd);
	    strAryFR.add(content.substring(0, pStart + 1));
        strAryFR.add("BigFont");	    //push bigger font. while it will not work for mev.
        strAryFR.add(lastRow);
        strAryFR.add("NormalFont");     //push normal font
        
        //payInfo
        strAryFR.add(getOutPayInfo(billPanel, tWidth, isCashBack));
        
        //comment
        //if there's revisee inside, should be removed, because for a completed bill, should mark with revisee, should be some thing like credit note?
        String comment = billPanel.getComment();
        if(comment.contains(FACTURE_REVISEE)) {
        	billPanel.setComment(comment.substring(0, comment.indexOf(FACTURE_REVISEE)));
        }
        // when formatting receipt for different status of bill(original, printted, completed) will set different foot message..
    	// so when the message was fetched out from map, if it's to be print in mev format, we'll know how to process it.
        // and even if it's not printed through mev, it's still in good format.

    	if(billPanel.getStatus() >= DBConsts.COMPLETED || billPanel.getStatus() < DBConsts.ORIGINAL) {	//if it's re-print, then add the info into comment.
    		strAryFR.add(billPanel.getComment()
    				+ (isToCustomer ? ValuePossible.RE_PRINTED : ValuePossible.RE_PRINTED_INTERNAL_USE));
    	}else {
    		if(billPanel.getComment().indexOf(LATEST_PAY) < 0) {
    			//if there's no latest_pay in comment, means paydlg's exact button not clicked, and ok button not clicked with left < 0. in this case,
    			//no paid but want to close the bill (and not use "void" to close), is called suspend. we will send a special transaction to mev, and will not print bill.
    			strAryFR.add(comment + PAYMENT_FAIL);
    		}else {
    			pushEndMessage(strAryFR, PAYMENT_REÇU);	//only original bill print will display foot message.
    		}
    	}
    	
        strAryFR.add("\n\n\n\n\n");
        strAryFR.add(PrintUtil.CUT);
        return strAryFR;
    }
    
    private static ArrayList<String> formatContentForVoid(String curPrintIp, BillPanel billPanel, String customerIndex){
    	ArrayList<String> strAryFR = new ArrayList<String>();
    	int tWidth = BarUtil.getPreferedWidth();
    	
	    String tableIdx = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
	    String billId = String.valueOf(billPanel.getBillID());
    	String orderNo = getOrderNoOfBill(billId);
    	if(orderNo == null) {
    		orderNo = BarOption.getNextOrderId();
    		updateOrderNoOnBillId(orderNo, billId);		//I should have check if it's exist or not first, this is a little lazy way.
    	}
	    pushBillHeadInfo(strAryFR, tWidth, fomatBillId(billId), tableIdx, orderNo, customerIndex);
	    
	    pushWaiterAndTime(strAryFR, tWidth, BarFrame.instance.valOpenTime.getText().toString(), LoginDlg.USERNAME);
	    
	    billPanel.lblTPS.setText(":0.00");
	    billPanel.lblTVQ.setText(":0.00");
	    strAryFR.add(getServiceDetailContent(billPanel.getOrderedDishAry(), curPrintIp, billPanel, tWidth).toString());
	    
        pushVoidedTotal(billPanel, strAryFR, tWidth);
        
        pushEndMessage(strAryFR, ValuePossible.VOID);
        
        strAryFR.add("\n\n\n\n\ncut");
        return strAryFR;
    }
    
    private static ArrayList<String> formatContentForRefund(String curPrintIp, BillPanel billPanel, int refundAmount,
    		String customerIndex, boolean sendToMevOnly){
    	ArrayList<String> strAryFR = new ArrayList<String>();
    	int tWidth = BarUtil.getPreferedWidth();

	    String tableIdx = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
	    String billId = String.valueOf(billPanel.getBillID());
    	String orderNo = getOrderNoOfBill(billId);
    	if(orderNo == null) {
    		orderNo = BarOption.getNextOrderId();
    		updateOrderNoOnBillId(orderNo, billId);		//I should have check if it's exist or not first, this is a little lazy way.
    	}
	    pushBillHeadInfo(strAryFR, tWidth, fomatBillId(billId), tableIdx, orderNo, customerIndex);
	    
	    pushWaiterAndTime(strAryFR, tWidth, BarFrame.instance.valOpenTime.getText().toString(), LoginDlg.USERNAME);
	    
	    strAryFR.add(getServiceDetailContent(billPanel.getRemovedDishAry(), curPrintIp, billPanel, tWidth).toString());
	    String payMethodInfo = getOutPayInfo(billPanel, tWidth, false);
	    String payMethod = ValuePossible.getMatchedPaytrans(payMethodInfo.trim().split("\n"));
		
        pushRefundAndNewTotal(billPanel, strAryFR, refundAmount, payMethod, tWidth);
        
        strAryFR.add(billPanel.getComment());
    	strAryFR.add("\n##REFUND##\n\n");//put this line just to make the size of content increase 1 to match the length of rerund case.
        if(sendToMevOnly) {
        	strAryFR.add("\n##sendToMevOnly##\n\n");
        }
        strAryFR.add("\n\n\n\n\n");
        strAryFR.add(PrintUtil.CUT);
        return strAryFR;
    }
    
    public static ArrayList<String> formatContentForReport(List<Bill> salesAndRefunds, String curPrintIp, String startTime, String endTime, String batchId){
    	ArrayList<String> strAryFR = new ArrayList<String>();
        
        //initContent
  		int tWidth = BarUtil.getPreferedWidth();
  		//@NOTE times goes first, to make the number bigger, then do divide. to avoid lost number after ".".
	    // don't open it otherwise the size of the list will become big, which will cause the wrong type judgement....pushBillHeadInfo(strAryFR, tWidth, null);
        if(BarOption.isShowWaiterAndTimeInReport()) {
	  		pushWaiterAndTimeForReport(strAryFR, tWidth, null, startTime, endTime, batchId);
	    }
        if(BarOption.isShowSalesInReport()) {
	        pushSalesSummary(strAryFR, salesAndRefunds, tWidth);
	    }
        //will check the flag inside:
        pushPaymentAndWaiterSummaryForReport(strAryFR, salesAndRefunds, tWidth);
	    
        if(BarOption.isShowServiceTypeSummaryInReport()) {
	        pushSummaryByServiceType(strAryFR, salesAndRefunds, tWidth);
	    }
        if(BarOption.isShowVoidBillSummaryInReport()) {
	        pushVoidBillSummary(strAryFR, tWidth, startTime, endTime);
	    }
        if(BarOption.isShowRemovedItemSummaryInReport()) {
	        pushRemovedItemSummary(strAryFR, tWidth, startTime, endTime);
	    }
        if(BarOption.isShowUnclosedBillInReport()) {
        	pushUnclosedBillsSummary(strAryFR, tWidth, startTime, endTime);
        }
        if(BarOption.isShowCategorySumaryInReport() || BarOption.isShowDishSumaryInReport()) {
        	pushCategoryAndDishSaleSummary(strAryFR, salesAndRefunds, tWidth);
        }
        if(BarOption.isShowInvoiceSumaryInReport()) {
        	pushInvoiceSumary(strAryFR, salesAndRefunds, tWidth, startTime, endTime);
        }
        //just to make the size reach 15 to be recognized as a report.
        for(int i = REPORT_MIN_SIZE - strAryFR.size() - 2; i > 0; i--) {
        	strAryFR.add(" ");
        }
        strAryFR.add("\n\n\n\n\n");
        strAryFR.add(PrintUtil.CUT);
        return strAryFR;
    }
    
	private static void pushCategoryAndDishSaleSummary(ArrayList<String> strAryFR, List<Bill> list, int width) {

  		HashMap<String, Integer> dishQTMap = new HashMap<String, Integer>();
  		HashMap<String, Integer> categoryQTMap = new HashMap<String, Integer>();
  		HashMap<String, Integer> dishTotalMap = new HashMap<String, Integer>();
  		HashMap<String, Integer> categoryTotalMap = new HashMap<String, Integer>();
  		  		
  		for (Bill bill : list) {
  			BillPanel bp = new BillPanel(null);
    		bp.initContent(null, bill.getBillIndex(), bill.getTableName(), bill.getOpenTime(), true);
    		for (Dish dish : bp.getOrderedDishAry()) {
    			String dishName = NetUtil.trimContentFromHTML(dish.getLanguage(LoginDlg.USERLANG));
    			int num = dish.getNum();
    			if(bill.getStatus() < 0 && num > 0) {
    				num = 0 - num;
    			}
				if(dishQTMap.containsKey(dishName)) {
					dishQTMap.put(dishName, dishQTMap.get(dishName) + num);
					dishTotalMap.put(dishName, dishTotalMap.get(dishName) + (int)(num * dish.getPrice() * 100));
				}else {
					dishQTMap.put(dishName, num);
					dishTotalMap.put(dishName, (int)(num * dish.getPrice() * 100));
				}
				
				StringBuilder sql = new StringBuilder("select category from Dish where STATUS != true and CODE = 0");

		        try {
		            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
		            if(rs.moveToNext()) {
		            	String category = NetUtil.trimContentFromHTML(DBUtil.getCursorString(rs,"category"));
		            	if(categoryQTMap.containsKey(category)) {
		            		categoryQTMap.put(category, categoryQTMap.get(category) + num);
		            		categoryTotalMap.put(category, (int)(categoryTotalMap.get(category) + num * dish.getPrice()) * 100);
						}else {
							categoryQTMap.put(category, num);
							categoryTotalMap.put(category, (int)(num * dish.getPrice() * 100));
						}
		            }
		        }catch(Exception e) {
		        	L.e("PrintServer_pushCategoryAndDishSaleSummary", "exception when runing sql to fetch category" + sql, e);
		        }
			}
  		}
  		
		StringBuilder content = new StringBuilder();
		//dish-------------------------------
		if(BarOption.isShowDishSumaryInReport()) {
			String dishSummary = "Dish Summary";
			String emptySpaceStr = BarUtil.generateString((width - dishSummary.length())/2, " ");
			content.append("\n").append(emptySpaceStr).append(dishSummary).append("\n");
			content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
			content.append("Dish").append(BarUtil.generateString(30 - 6, " ")).append("QT")
			.append(BarUtil.generateString(width- 30 - 5, " ")).append("Total").append("\n");
			
			ArrayList<String> keys = new ArrayList<String>(dishQTMap.keySet());
	  		Collections.sort(keys, new Comparator<String>() {
	            @Override
	            public int compare(String c1, String c2) {
	                return c1.compareTo(c2);
	            }
	        });
	  		for (String key : keys) {
				String value = String.valueOf(dishQTMap.get(key));
				emptySpaceStr = BarUtil.generateString(30 - key.length() - value.length(), " ");
				String total = BarUtil.formatMoney(dishTotalMap.get(key)/100.0);
				content.append(key.length() >= 30? key.substring(0, 27) + "..." : key).append(emptySpaceStr).append(value)
				.append(BarUtil.generateString(width - 30 - total.length(), " ")).append(total)
				.append("\n");
			}
		}
		//category------------------------------
		if(BarOption.isShowCategorySumaryInReport()) {
			String categorySummary = "Category Summary";
			String emptySpaceStr = BarUtil.generateString((width - categorySummary.length())/2, " ");
			content.append("\n").append(emptySpaceStr).append(categorySummary).append("\n");
			content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
			content.append("Category").append(BarUtil.generateString(11, " ")).append("QT")
			.append(BarUtil.generateString(width - 30 -5, " ")).append("Total").append("\n");
			
			ArrayList<String> keys = new ArrayList<String>(categoryQTMap.keySet());
	  		Collections.sort(keys, new Comparator<String>() {
	            @Override
	            public int compare(String c1, String c2) {
	                return c1.compareTo(c2);
	            }
	        });
	  		for (String key : keys) {
				String value = String.valueOf(categoryQTMap.get(key));
				emptySpaceStr = BarUtil.generateString(width/2 - key.length() - value.length(), " ");
				String total = BarUtil.formatMoney(categoryTotalMap.get(key)/100.0);
				
				content.append(key.length() >= 30 ? key.substring(0, 27) + "..." : key).append(emptySpaceStr).append(value)
				.append(BarUtil.generateString(width - 30 - total.length(), " ")).append(total)
				.append("\n");
			}
		}
		
		strAryFR.add(content.toString());
	}

	private static void pushInvoiceSumary(ArrayList<String> strAryFR, List<Bill> list, int width, String startTime, String endTime) {
		StringBuilder content = new StringBuilder();
		//title
		String Invoices = "Completed Invoices";
		String emptySpaceStr = BarUtil.generateString((width - Invoices.length())/2, " ");
		content.append("\n");
		content.append(emptySpaceStr).append(Invoices).append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");

		content.append("Inv#   Table#      Total").append("\n");
		
        StringBuilder sql = new StringBuilder("select * from BILL, USER where BILL.userId = USER._id ")
        		.append(" and createdTime >= '").append(startTime)
        		.append("' and createdTime <= '").append(endTime)
        		.append("' and BILL.STATUS = ").append(DBConsts.COMPLETED);
        if(LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS) {	//if is not admin, then get out only user related records.
        	sql.append(" and USER._id = ").append(LoginDlg.USERID);
        }
        sql.append(" order by _id");
        
        float totalSales = 0.0f;
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            while (rs.moveToNext()) {
            	String tableName = DBUtil.getCursorString(rs,"tableName");
            	String orderId = String.valueOf(DBUtil.getCursorString(rs, "_id"));
            	content.append(orderId).append(BarUtil.generateString(12 - orderId.length() - (tableName == null ? 4 : tableName.length()), " ")).append(tableName);
            	float total = Float.valueOf((float) (DBUtil.getCursorInt(rs,"TOTAL") / 100.0));
            	totalSales += total;
            	String totalStr = BarUtil.formatMoney(total);
            	content.append(BarUtil.generateString(12 - totalStr.length(), " ")).append(totalStr).append("    ");
//            	content.append(DBUtil.getCursorString(rs,"OpenTime"));
            	content.append("\n");
            }
            rs.close();// 关闭
        } catch (Exception e) {
            ErrorUtil.write(e);
        }
        
        content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
		String toltal = BarUtil.formatMoney(totalSales);
		content.append(BarUtil.generateString(24 - toltal.length(), " ")).append(toltal).append("\n");
		
		strAryFR.add(content.toString());
	}
	
	private static void pushBillHeadInfo(List<String> strAryFR, int tWidth, String billID, String tableIdx, String orderNo, String customerIndex) {
		StringBuilder content = new StringBuilder();
		if(billID != null) {
			content.append(getFormattedBillHeader(tWidth, billID, tableIdx, orderNo, customerIndex));
		}else {
			ErrorUtil.write("Unexpected parameter in method [pushBillHeadInfo()], billId not supposed to be null! ");
		}
        content.append("\n");
        //push bill header
        strAryFR.add(content.toString());
	}
	
	private static void pushWaiterAndTime(List<String> strAryFR, int tWidth, String startTimeStr, String waiterName) {
		StringBuilder sb = new StringBuilder();
		
        //waiter
        if(waiterName.length() > 18) {
        	waiterName = waiterName.substring(0,16) + "..";
        }
        sb.append(waiterName).append(" ");
        
        //space and times
        int lengthOfSpaceBeforeTime = tWidth - sb.length() - startTimeStr.length() - 3;
        if(lengthOfSpaceBeforeTime > 0) {
        	String spaceStr = BarUtil.generateString(lengthOfSpaceBeforeTime, " ");
        	sb.append(spaceStr).append(startTimeStr);
        }else {
        	String spaceStr = BarUtil.generateString(tWidth - startTimeStr.length() - 3, " ");
        	sb.append("\n").append(spaceStr).append(startTimeStr);
        }
        sb.append("\n");
        strAryFR.add(sb.toString());
	}
	
	private static void pushWaiterAndTimeForReport(List<String> strAryFR, int tWidth, String tableIdx, String startTimeStr, String endTimeStr, String batchId) {
		
		StringBuilder content = new StringBuilder();
		content.append("\n");
		String firstLineStr = batchId.length() > 0 ? 
				(LoginDlg.getUSERTYPE() == LoginDlg.SUPER_ADMIN ? BarOption.getReportHeaderOfSuperAdmin() : BarOption.getReportHeaderOfManager()) + batchId
				: REPORT;
		int length = (tWidth - firstLineStr.length())/2;
		content.append(BarUtil.generateString(length, " "));
		content.append(firstLineStr).append("\n");
	    
        //waiter
        String waiterName = LoginDlg.USERNAME;
		content.append(BarUtil.generateString((tWidth - waiterName.length())/2, " "));
        content.append(waiterName).append("\n");
        
        //store name and address
        String s = BarOption.getBillHeadInfo();
    	if(s != null && s.trim().length() > 0) {
    		String[] infos = s.split(":");
    		content.append(BarUtil.generateString((tWidth - infos[0].length())/2, " "));
			content.append(infos[0]);
			content.append("\n");
    		content.append(BarUtil.generateString((tWidth - infos[1].length())/2, " "));
			content.append(infos[1]);
			content.append("\n");
    	}
        //space and times
        String spaceStr = BarUtil.generateString((tWidth - startTimeStr.length() - endTimeStr.length() - 3)/2, " ");
        content.append(spaceStr).append(startTimeStr).append("   ").append(endTimeStr);
        content.append("\n");
        content.append("\n");
        
        strAryFR.add(content.toString());
	}
	
	private static StringBuilder getServiceDetailContent(List<Dish> dishList, String curPrintIp, BillPanel billPanel, int tWidth) {//, String refTo) {
        //bills---classify the dishes into different bill. in case the content of the bill is combined.
        HashMap<Integer, ArrayList<Dish>> billDishesMap = new HashMap<Integer, ArrayList<Dish>>();
        Float subtotal = 0.0f;
        for(Dish dish:dishList){
        	ArrayList<Dish> dishes = billDishesMap.get(dish.getBillID());
        	if(dishes == null) {
        		dishes = new ArrayList<Dish>();
        		dishes.add(dish);
        		billDishesMap.put(dish.getBillID(), dishes);
        	}else {
        		dishes.add(dish);
        	}
        	subtotal += dish.getTotalPrice();
        }
        
        //get the value of current bill ready (current bill might be a combined bill.
    	Float fSubTotal = Float.valueOf(subtotal / 100f);
    	String strSubTotal = BarUtil.formatMoney(fSubTotal);
        if(fSubTotal > 0) {	//if it's not refund case, then should use the text on billPane directly, because the number on billPane included the service fee and discount of bill.
        	String[] strs = billPanel.lblSubTotle.getText().toString().split(":");
            strSubTotal = strs[1].trim();
            fSubTotal = Float.valueOf(strSubTotal);
        }

        String strTPS = "";
        String[] strs = null;
        if(fSubTotal > 0) { // not refund mode
	        strs = billPanel.lblTPS.getText().toString().split(":");
	        strTPS = strs[1].trim();
		} else {	//for refund mode, calculate only the tax for ruturn items (no calculating discount and service fees for now) 
			strTPS = BarUtil.formatMoney(fSubTotal * BarOption.getGST() / 100f);
		}
        
        String strTVQ = "";
        if(fSubTotal > 0) { // not refund mode
	        strs = billPanel.lblTVQ.getText().toString().split(":");
	        strTVQ = strs[1].trim();
        } else {
        	strTVQ = BarUtil.formatMoney(fSubTotal * BarOption.getQST() / 100f);
        }
        
        Float fServiceFee = 0.0f;
        
        String strServiceFee = billPanel.lblServiceFee.getText().toString();
		if(strServiceFee.length() > 0) {
        	strs = strServiceFee.split(":");
        	strServiceFee = strs[1].trim();
        	fServiceFee = Float.valueOf(strServiceFee);
        }

        Float fDiscount = 0.0f;
        String strDiscount = billPanel.lblDiscount.getText().toString();
		if(strDiscount.length() > 0) {
        	strs = strDiscount.split(":");
        	strDiscount = strs[1].trim().substring(1);
        	fDiscount = Float.valueOf(strDiscount);  //@NOTE:an "-" is displayed before the number of discount on totalArea.so used "-" to remove it.
        }

		StringBuilder content = new StringBuilder();
		//normal bill vs combined bill
        if(billDishesMap.size() == 1) {
            formatDishListContent(dishList, curPrintIp, tWidth, content, null, null, null);
        }else {//it's a combined bill
        	//@NOTE:in case the original bill was used again after combination, so the values of the bill record could changed.
        	//so have to use the dish list to calculate every thing again,  while the service fee and discount will be problem.
        	//to avoid the mismatch in bill, will average the service fee and discount.
	        for (Entry<Integer, ArrayList<Dish>> entry : billDishesMap.entrySet()) {
	        	//apply list into a new panel, and calculate the subtotal and taxes.
        		BillPanel bp = new BillPanel(null);
        		bp.setOrderedDishAry(entry.getValue());
        		bp.updateTotalArea(true);
        		
        		formatDishListContent(entry.getValue(), curPrintIp, tWidth, content,
	            		BarUtil.formatMoney(bp.subTotal / 100.0),
	            		BarUtil.formatMoney(fServiceFee/billDishesMap.size()),
	            		BarUtil.formatMoney(fDiscount/billDishesMap.size()));
			}
        	//as for a combined bill, we need to add an extra total part.	
	        content.append(BarUtil.getSeperatorLine(0)).append("\n\n");
        }
        
        if(fServiceFee > 0) {
            String serviceFee = String.valueOf(fServiceFee);
            content.append(SERVICE_FEE).append(" : ")
				.append(BarUtil.generateString(tWidth - serviceFee.length() - SERVICE_FEE.length() - 3, " "))
            	.append(serviceFee).append("\n");
        }
        if(fDiscount > 0) {
            content.append(ANNULATION).append(" : ")
				.append(BarUtil.generateString(tWidth - strDiscount.length() - ANNULATION.length() - 4, " "))
				.append("-").append(strDiscount).append("\n");
        }
        //add the total.
        content.append(SOUSTOTAL).append(" : ")
        	.append(BarUtil.generateString(tWidth - strSubTotal.length() - SOUSTOTAL.length() - 3, " "))
        	.append(strSubTotal).append("\n");

        content.append("\n").append(TPS).append(BarOption.getGSTAccount());
        content.append("\n").append(TVQ).append(BarOption.getQSTAccount()).append("\n");
        
//        //push end message
//	    if(refTo.length() > 0) {
//	    	if(!refTo.toLowerCase().contains(PrintUtil.REF_TO)) {
//	    		refTo = PrintUtil.REF_TO + refTo;
//	    	}
//	    	if(!refTo.contains(PrintUtil.DATE_REF)) {
//	    		int pComma = refTo.indexOf(",");
//	    		refTo = refTo.substring(0, pComma).trim() + PrintUtil.DATE_REF + refTo.substring(pComma + 1).trim();
//	    	}
//	    	int pComma = refTo.indexOf(",");	//if there's still comma, means there's the third part, which is the mtAvTaxRef(it's not mandatory).
//	    	if(pComma > 0) {
//	    		refTo = refTo.substring(0, pComma).trim() + PrintUtil.OLD_SUBTOTAL + refTo.substring(pComma).trim();
//	    	}
//	    	content.append("\n" + refTo);	//has comment means bill printed(will be co-considered when saving paper flag is set) or invoice was reopened,
//        }else {
//            pushEndMessage(content);
//        }
	    
        content.append("\n").append(BarUtil.getSeperatorLine(1)).append("\n");
        content.append(TPS)
    		.append(BarUtil.generateString(tWidth - strTPS.length() - TPS.length() - 3, " "))
        	.append(strTPS).append("\n");
        
        content.append(TVQ)
			.append(BarUtil.generateString(tWidth - strTVQ.length() - TVQ.length() - 3, " "))
        	.append(strTVQ).append("\n");
        return content;
	}

	//generate an ===== items ----- subTotal taxes area 
	private static void formatDishListContent(List<Dish> dishList, String curPrintIp, int tWidth,
			StringBuilder content, String subTotal, String serviceFee, String discount) {
        
		content.append(BarUtil.getSeperatorLine(0)).append("\n\n");
        Integer langIndex = ipPrinterMap.get(curPrintIp).getLangType();
		langIndex = langIndex == null ? 0 : langIndex;
		for(Dish d:dishList){
            StringBuilder sb = new StringBuilder();
            
            if(BarOption.isDisDishIDInKitchen()) {
                sb.append(d.getId());
                sb.append(BarUtil.generateString(5 - String.valueOf(d.getId()).length(), " "));
            }
            
            String dishName = BarUtil.getPlainTextOut(d.getLanguage(langIndex)).trim(); 
            String[] subStrs = dishName.split("\n");
            StringBuilder modifiedStr = new StringBuilder();
            int i = 0;
            for(i = 0; i < subStrs.length; i++) {
            	if(subStrs[i].length() > 30) {
            		subStrs[i] = subStrs[i].substring(0, 27) + "...";
                }
            	modifiedStr.append(subStrs[i].trim()).append("\n");
            }
            sb.append(modifiedStr.substring(0, modifiedStr.length() - 1));
            
            //NOTE: the number could be bigger than 10000.
            //number and price:
            StringBuilder numberAndPrice = new StringBuilder(" ").append(DBUtil.getDisplayableNumWithX(d.getNum()))
            		.append(BarUtil.formatMoney(d.getTotalPrice()/d.getNum()/100f));
            
            sb.append(numberAndPrice);
            subStrs[i - 1] = subStrs[i - 1] + numberAndPrice;
            
            String price = BarUtil.formatMoney((d.getTotalPrice())/100f);
            int occupiedLength = BarUtil.getLengthOfString(subStrs[i - 1].toString());
            sb.append(BarUtil.generateString(tWidth - occupiedLength - (price.length()), " "));
            sb.append(price).append(" ");
            if(d.getGst() == 1) {
            	sb.append("F");
            }
            if(d.getQst() == 1) {
            	sb.append("P");
            }
            content.append(sb);
            content.append("\n");

            
            if(d.getModification() != null && !"null".equals(d.getModification().toLowerCase()) && d.getModification().length() > 0) {
            	int pOfDisCount = d.getModification().indexOf("-" + BarOption.getMoneySign());
            	String modify = pOfDisCount < 0 ? d.getModification() : d.getModification().substring(0, pOfDisCount);
            	if(modify.endsWith("\n")) {
            		modify = modify.substring(0, modify.length() - 1);
            	}
            	content.append(BarUtil.changeToLangForModify(modify, langIndex));
            	content.append("\n");
            }else {
            	content.append("\n");
            }
            
            if(d.getDiscount() > 0) {
            	sb = new StringBuilder(DISCOUNT);
            	String itmeDisc = "-" + BarUtil.formatMoney(d.getDiscount()/100f);
                occupiedLength = BarUtil.getLengthOfString(sb.toString());
            	sb.append(BarUtil.generateString(tWidth - occupiedLength - (itmeDisc.length()), " "));
                sb.append(itmeDisc);
                content.append(sb);
                content.append("\n");
            }
        }
        
        //add total part.
        if(serviceFee != null && serviceFee.length() > 0 && !"0.00".equals(serviceFee)) {
            content.append(SERVICE_FEE).append(" : ")
				.append(BarUtil.generateString(tWidth - serviceFee.length() - SERVICE_FEE.length() - 3, " "))
            	.append(serviceFee).append("\n");
        }
        if(discount != null && discount.length() > 0 && !"0.00".equals(discount)) {
            content.append(DISCOUNT).append(" : ")
				.append(BarUtil.generateString(tWidth - discount.length() - DISCOUNT.length() - 4, " "))
				.append("-").append(discount).append("\n");
        }
        if(subTotal != null && subTotal.length() > 0 && !"0.00".equals(subTotal)) {
	        content.append(SOUSTOTAL).append(" : ")
	        	.append(BarUtil.generateString(tWidth - subTotal.length() - SOUSTOTAL.length() - 3, " "))
	        	.append(subTotal).append("\n");
        }
	}

	private static StringBuilder getContentOfTotalPart(BillPanel billPanel) {
        
        String strTotal = billPanel.valTotlePrice.getText().toString();
        //push total
		StringBuilder content = new StringBuilder("Total").append(" : ")//.append(BarUtil.generateString(tWidth - strTotal.length() - Total.length() - 3 - 1, " "))
			.append(strTotal).append("\n");
        
        return content;
	}

//	private static void pushRound(BillPanel billPanel, ArrayList<String> strAryFR, int tWidth) {
//		StringBuilder content = new StringBuilder();
//        //push total
//        String strTotal = BarUtil.canadianPennyRound(billPanel.valTotlePrice.getText());
//        content.append("Round").append(" : ").append(BarUtil.generateString(tWidth - 9 - strTotal.length(), " "))
//			.append(strTotal).append("\n");
//        strAryFR.add(content.toString());
//	}
	
	private static void pushVoidedTotal(BillPanel billPanel, List<String> strAryFR, int tWidth) {
	    //push new totall
		//push bigger font. while it will not work for mev.
        strAryFR.add("BigFont");
        //push NEW total
		//push refund into ary.
        StringBuilder content = new StringBuilder("             ").append(ValuePossible.VOID).append("\n").append("Total : 0.00").append("\n");
        strAryFR.add(content.toString());
        //push normal font
        strAryFR.add("NormalFont");
	}
	
	private static void pushRefundAndNewTotal(BillPanel billPanel, List<String> strAryFR, int refund, String method, int tWidth) {
		StringBuilder content = new StringBuilder();
		//push refund into ary.
		String refundStr = BarUtil.formatMoney(refund/100f);
	    content.append(ValuePossible.REFUND).append(BarUtil.generateString(tWidth - 9 - refundStr.length(), " ")).append(refundStr).append("\n");
	    content.append(ValuePossible.METHOD).append(BarUtil.generateString(tWidth - ValuePossible.METHOD.length() - method.length(), " ")).append(method).append("\n");
	    strAryFR.add(content.toString());    
	    
	    //push new totall
		//push bigger font. while it will not work for mev.
        strAryFR.add("BigFont");
        //push NEW total
        String strTotal = billPanel.valTotlePrice.getText().toString();
        String newTotal = BarUtil.formatMoney(Float.valueOf(strTotal) + Float.valueOf(refundStr));
        content = new StringBuilder("Total : ").append(newTotal).append("\n");
        strAryFR.add(content.toString());
        //push normal font
        strAryFR.add("NormalFont");
	}
	
	private static String getOutPayInfo(BillPanel billPanel, int width, boolean isCashBack) {

		StringBuilder content = new StringBuilder("\n");
		int billId = billPanel.getBillID();
		
    	StringBuilder sb = new StringBuilder("select * from bill where _id = " + billId);
    	try {
    		Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
            rs.moveToNext();
            float total = (float) (DBUtil.getCursorInt(rs,"total") / 100.0);

            String otherReceived = DBUtil.getCursorString(rs,"otherReceived");
            int cashReceived = DBUtil.getCursorInt(rs,"cashReceived");
            String str = BarUtil.formatMoney(cashReceived/100f);
            
            boolean isPaidFromOther = otherReceived != null && !otherReceived.toLowerCase().equals("null") && otherReceived.length() > 0;
        	if(isPaidFromOther) {
                if(cashReceived > 0) {	//currently if it's paid from skip/uber/doordash.... the cash must was put into the price already.
	    			content.append(otherReceived).append(" : ")
	    			.append(BarUtil.generateString(width - otherReceived.length() - 3 - str.length(), " "))
	    			.append(str).append("\n");
                }
    		}
            if(cashReceived > 0 && !isPaidFromOther) {
    			content.append(ValuePossible.CASH).append(" : ")
    			.append(BarUtil.generateString(width - 9 - str.length(), " "))
    			.append(str).append("\n");
    		}
            int debitReceived = DBUtil.getCursorInt(rs,"debitReceived");
            if(debitReceived > 0) {
                str = BarUtil.formatMoney(debitReceived/100f);
    			content.append("DEBIT").append(" : ")
    			.append(BarUtil.generateString(width - 8 - str.length(), " "))
    			.append(str).append("\n");
    		}
            int visaReceived = DBUtil.getCursorInt(rs,"visaReceived");
            if(visaReceived > 0) {
                str = BarUtil.formatMoney(visaReceived/100f);
    			content.append("VISA").append(" : ")
    			.append(BarUtil.generateString(width - 7 - str.length(), " "))
    			.append(str).append("\n");
    		}
            int masterReceived = DBUtil.getCursorInt(rs,"masterReceived");
        	if(masterReceived > 0) {
                str = BarUtil.formatMoney(masterReceived/100f);
    			content.append("MASTER").append(" : ")
    			.append(BarUtil.generateString(width - 9 - str.length(), " "))
    			.append(str).append("\n");
    		}

            float left = isPaidFromOther ? 0 :(Math.round((total * 100 - cashReceived - debitReceived - visaReceived - masterReceived)));
            str = BarUtil.formatMoney( -1 * left/100f); 
            String lblText;
            if(left < 0 && billPanel.getStatus() < DBConsts.COMPLETED) {					//normally left should be negative, it means the cash back or tip.
	            if(isCashBack) {
	            	lblText = "MONNAIE : ";
	            	content.append(lblText)
	 				.append(BarUtil.generateString(width - lblText.length() - str.length(), " "))
	 				.append(str).append("\n");
	            	
	            	String roundedStr = BarUtil.canadianPennyRound(str);
	            	if(!roundedStr.equals(str)) {
	            		lblText = "ARRONDI DE MONTANT : ";	//rounded price
	            		content.append(lblText)
	     				.append(BarUtil.generateString(width - lblText.length() - roundedStr.length(), " "))
	     				.append(roundedStr).append("\n");
	            	}
	            }else{//if there's extra money, consider it as tip.
	            	lblText = "Frais de service : ";
	            	content.append(lblText)
	 				.append(BarUtil.generateString(width - lblText.length() - str.length(), " "))
	 				.append(str).append("\n");
	            }
            } else {	//if the bill not completed paid yet, or it's processing a refund
            	//do nothing for now.
            }

            //for the use of outside QC province, they don't have mev print, but they may also need to printout GST account...then should open showTaxNumber flag.
            if(BarOption.showTaxNumber()) {
	            if(BarOption.getGSTAccount() != null && BarOption.getGSTAccount().length() > 5
	            		&& BarOption.getQSTAccount() != null && BarOption.getQSTAccount().length() > 5) {
		            content.append("\n\n" + BarFrame.consts.GST() + ":").append(BarOption.getGSTAccount());
		            content.append("\n" + BarFrame.consts.QST() + ":").append(BarOption.getQSTAccount()).append("\n");
	            }
            }
            
    	}catch(Exception e) {
    		ErrorUtil.write(e);
    	}
    	
		return content.toString();
	}

	//end message is comment + foot info
	private static void pushEndMessage(List<String> strAryFR, String invoiceType) {
		BillPanel billPanel = ((SalesPanel)BarFrame.instance.panels[2]).billPanel; 
		StringBuilder content = new StringBuilder();
		if(billPanel.getComment().trim().length() > 0) {	//has comment means bill is already printed(will be co-considered when saving paper flag is set)
			 content.append("\n").append(billPanel.getComment());	//or invoice was reopened, maybe also when bill is revised.
		}
		//push end message.
        String endMes = BarOption.getBillFootInfo();
        if(endMes.length() > 0) {
        	//content.append(BarUtil.generateString(tWidth, sep_str2));
        	content.append("\n").append(endMes);
        }
        //for now I think it's safe to say if comment is empty, means it could be only original fature..
        //because receipt must have refTo, at least to it self, otherwise web-mev will complain.
        //@Note: the invoiceType like FACTURE_ORIGINAL/PAITMENT RESU/VOID will not appear in db, it's temporally added before printed.
        content.append(invoiceType == null ? "" : invoiceType);
        strAryFR.add(content.toString());
	}
    
	public static void pushSalesSummary(ArrayList<String> strAryFR, List<Bill> list, int width) {
  		int refundCount = 0;
//  		int refoundAmount = 0;	//real money refund.
  		
  		int salesGrossCount = 0;
//  		int salesGrossAmount = 0;	//real money.
  		
  		float subTotal = 0;	//salesGrossAmount * 100 /(100 + BarOption.getGST() + BarOption.getQST());
  		float GST = 0;	//net * BarOption.getGST()/100;
  		float QST = 0;	//net * BarOption.getQST()/100;
  		float refundTotal = 0;
  		float refundGST = 0;
  		float refundQST = 0;
  		
  		for (Bill bill : list) {
  			BillPanel bp = new BillPanel(null);
    		bp.initContent(null, bill.getBillIndex(), bill.getTableName(), bill.getOpenTime(), true);
    		
    		subTotal += bp.subTotal;		//the sub total included the refund ones. why when it's refund, the subtotal here is 0,is it correct?
    		GST += bp.totalGst;
    		QST += bp.totalQst;
            				
  			int status = bill.getStatus();
  			if(status < DBConsts.ORIGINAL) {//means has refund) so if there's a refund, there will be no new bill, just the old bill's status was updated
  				
  				status = -1 * status;
//  				refoundAmount += status;
//  				salesGrossAmount -= status;
  				//
  				refundCount++;
  				float refundAvaTax = 100 * status/(BarOption.getGST() + BarOption.getQST() + 100);
  				subTotal += refundAvaTax;
  				GST += refundAvaTax * BarOption.getGST() / 100;
  	    		QST += refundAvaTax * BarOption.getQST() / 100;
  				refundTotal += refundAvaTax;	//because when it's refund the bp.subTotal will be 0, and originall bill was not fetched out because it's expired?
  				refundGST += BarOption.getGST()/100 * refundAvaTax;
  				refundQST += BarOption.getQST()/100 * refundAvaTax;
  			}
			salesGrossCount++;
//			salesGrossAmount += bill.getCashReceived() + bill.getDebitReceived()
//			+ bill.getVisaReceived() + bill.getMasterReceived() + bill.getCashback() - bill.getTip();
  		}
  		
		StringBuilder content = new StringBuilder();
		//title
		String saleSummary = "Sales Summary";
		String emptySpaceStr = BarUtil.generateString((width - saleSummary.length())/2, " ");
		content.append(emptySpaceStr).append(saleSummary).append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
		//countSaleGross + amountSaleGross
		String totalSaleCount = String.valueOf(Integer.valueOf(refundCount) + Integer.valueOf(salesGrossCount));

//		String amountSaleGross = BarUtil.formatMoney(salesGrossAmount / 100.0);
//		String refoundMoney = BarUtil.formatMoney(refoundAmount/100.0);
				
		//SubTotal
		
		//Transaction CT
		content.append("Transaction (Sales)").append("\n");
		String strSaleNum = String.valueOf(salesGrossCount);
		content.append("   Number").append(BarUtil.generateString(width - 9 - strSaleNum.length(), " ")).append(strSaleNum).append("\n");
		String subTotalIncome = BarUtil.formatMoney(subTotal / 100.0);
		content.append("   Total avant taxes").append(BarUtil.generateString(width - 20 - subTotalIncome.length(), " ")).append(subTotalIncome).append("\n");
		String strGST = BarUtil.formatMoney(GST / 100.0);
		content.append("   GST").append(BarUtil.generateString(width - 6 - strGST.length(), " ")).append(strGST).append("\n");
		String strQST = BarUtil.formatMoney(QST / 100.0);
		content.append("   QST").append(BarUtil.generateString(width - 6 - strQST.length(), " ")).append(strQST).append("\n");
		content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
		
		//Transaction DT
		content.append("Transaction (Refund)").append("\n");
		
		String strRefundQT = String.valueOf(refundCount);
		content.append("   Number").append(BarUtil.generateString(width - 9 - strRefundQT.length(), " ")).append(strRefundQT).append("\n");
		String strRefundTotal = BarUtil.formatMoney(refundTotal / 100.0);
		content.append("   Total avant taxes").append(BarUtil.generateString(width - 21 - strRefundTotal.length(), " ")).append("-").append(strRefundTotal).append("\n");
		strGST = BarUtil.formatMoney(refundGST / 100.0);
		content.append("   GST").append(BarUtil.generateString(width - 7 - strGST.length(), " ")).append("-").append(strGST).append("\n");
		strQST = BarUtil.formatMoney(refundQST / 100.0);
		content.append("   QST").append(BarUtil.generateString(width - 7 - strQST.length(), " ")).append("-").append(strQST).append("\n");
		content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
		
		//Total
		content.append("Total").append("\n");
		content.append("   Number").append(BarUtil.generateString(width - 9 - totalSaleCount.length(), " ")).append(totalSaleCount).append("\n");
		String netTotal = BarUtil.formatMoney((subTotal - refundTotal) / 100.0);
		content.append("   Total avant taxes").append(BarUtil.generateString(width - 20 - netTotal.length(), " ")).append(netTotal).append("\n");
		strGST = BarUtil.formatMoney((GST - refundGST) / 100.0);
		content.append("   GST").append(BarUtil.generateString(width - 6 - strGST.length(), " ")).append(strGST).append("\n");
		strQST = BarUtil.formatMoney((QST - refundQST) / 100.0);
		content.append("   QST").append(BarUtil.generateString(width - 6 - strQST.length(), " ")).append(strQST).append("\n");
		content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
		String strTotal = BarUtil.formatMoney((subTotal - refundTotal + GST - refundGST + QST - refundQST) / 100.0);
		content.append(BarUtil.generateString(width - strTotal.length(), " ")).append(strTotal);
		
		strAryFR.add(content.toString());
	}
	
	public static void pushPaymentAndWaiterSummaryForReport(ArrayList<String> strAryFR, List<Bill> list, int width) {
		StringBuilder content = new StringBuilder();
		//title
		String paymentSummary = "Payment Summary";
		String emptySpaceStr = BarUtil.generateString((width - paymentSummary.length())/2, " ");
		content.append("\n");
		content.append(emptySpaceStr).append(paymentSummary).append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
		content.append("PaidBy  Qt    Sales      Tip ").append(BarUtil.generateString(width - 34, " ")).append("TOTAL").append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
		
		//NOTE: the refundToTal should be kept negative.
		int cashQt = 0, cashTotal = 0, cashRefundTotal = 0, cashRefundQT = 0;
		int debitQt = 0, debitSales = 0, debitTip = 0, debitTotal = 0, debitRefundTotal = 0, debitRefundQT = 0;
		int visaQt = 0, visaSales = 0, visaTip = 0, visaTotal = 0, visaRefundTotal = 0, visaRefundQT = 0;
		int masterQt = 0, masterSales = 0, masterTip = 0, masterTotal = 0, masterRefundTotal = 0, masterRefundQT = 0;
		int otherQt = 0, otherSales = 0, otherTip = 0, otherTotal = 0, otherRefundTotal = 0, otherRefundQT = 0;
		
		HashMap<String, int[]> waiterSalesData = new LinkedHashMap<String, int[]>();
		HashMap<String, ReportDto> otherPayMethodMap = new HashMap<String, ReportDto>();
		for (Bill bill : list) {

			String waiterName = bill.getUserId().toString();
			int[] achivement = waiterSalesData.get(waiterName);
			if(achivement == null) {
				achivement = new int[6];	//why 6?--[0]count[1]Sale[2]Tip[3][4][5]
			}
			achivement[0] = achivement[0] + 1; 
			
			//process the refund number.
			//Refund rule: refund cash first, then debit card then visa card, then master card, then other.
  			int status = bill.getStatus();
  			if(status < -1) {//means has refund)
  				achivement[4] += 1;
  				achivement[5] += status;
  				boolean isPaidFromOther = bill.getOtherReceived() != null && bill.getOtherReceived().length() == 0; //if paied from other, then couldn't be mix pay.
				if(isPaidFromOther) {
					otherRefundTotal += status;
					otherRefundQT ++;
				}else if(bill.getCashReceived() + bill.getCashback() >= 0 - status) {		//and plenty of cash to refund
					cashRefundTotal += status;												//then must be fund with cash.
					cashRefundQT ++;
				}else {
					int cashPartRefund = bill.getCashReceived() + bill.getCashback();
					cashRefundTotal -= cashPartRefund;
					cashRefundQT += cashPartRefund > 0 ? 1 : 0;
					status += cashPartRefund;
					if(bill.getDebitReceived() >= 0 - status) {
						debitRefundTotal += status;
						debitRefundQT ++;
					}else {
						debitRefundTotal -= bill.getDebitReceived();
						debitRefundQT += bill.getDebitReceived() > 0 ? 1 : 0;
  						status += bill.getDebitReceived();
  						
  						if(bill.getVisaReceived() >= 0 - status) {
  							visaRefundTotal += status;
  							visaRefundQT ++;
  						}else {
  							visaRefundTotal -= bill.getVisaReceived();
  							visaRefundQT += bill.getVisaReceived() > 0 ? 1 : 0;
  							status += bill.getVisaReceived();
  							
  							if(bill.getMasterReceived() >= 0 - status) {
  								masterRefundTotal += status;
  								masterRefundQT ++;
  							}else {
  								masterRefundTotal -= bill.getMasterReceived();
  								masterRefundQT += bill.getMasterReceived() > 0 ? 1 : 0;
  								status += bill.getMasterReceived();
  							}
  						}
					}
				}
  			}
  			
			boolean tipCounted = false;	//the fist pay method that full filled tip condition will set this flag to true. 
			//getOtherReceived field used to save the type of "otherReceived" like Uber/SKIP/Fantuan....
			//if this field is set, then the cashReceived means the value of "other received.
			if(bill.getCashReceived() > 0) {
				String otherReceivedType = bill.getOtherReceived();
				if(otherReceivedType != null && otherReceivedType.length() > 0) {
					ReportDto reportDto = otherPayMethodMap.get(otherReceivedType);
					if(reportDto == null) {
						reportDto = new ReportDto();
						otherPayMethodMap.put(otherReceivedType, reportDto);
					}
					reportDto.setQt(reportDto.getQt() + 1);
					reportDto.setMoneyRecieved(reportDto.getMoneyRecieved() + bill.getCashReceived() - bill.getTip());
					reportDto.setTip(reportDto.getTip() + bill.getTip());
		  	  		reportDto.setTotal(reportDto.getTotal() + bill.getTotal());	//@NOTE: cashback is negtive value.
				}else {
	  				cashQt++;
		  	  		cashTotal += bill.getCashReceived() + bill.getCashback();	//@NOTE: cashback is negtive value.
		  	  		achivement[1] = achivement[1] + bill.getCashReceived() + bill.getCashback(); 
		  	  		achivement[3] = achivement[3] + bill.getCashReceived() + bill.getCashback();
				}
			}
			if(bill.getDebitReceived() > 0) {
				debitQt++;
				debitTotal += bill.getDebitReceived();
				debitSales += bill.getDebitReceived();
				if(bill.getTip() > 0 && !tipCounted) {
					debitTip += bill.getTip();
					debitSales -= bill.getTip();
					tipCounted = true;
		  	  		achivement[2] += bill.getTip();
		  	  		achivement[1] -= bill.getTip();
				}
	  	  		achivement[1] = achivement[1] + bill.getDebitReceived(); 
			}
			if(bill.getVisaReceived() > 0) {
  				visaQt++;
	  	  		visaTotal += bill.getVisaReceived();
	  	  		visaSales += bill.getVisaReceived();
				if(bill.getTip() > 0 && !tipCounted) {
					visaTip += bill.getTip();
					visaSales -= bill.getTip();
					tipCounted = true;
		  	  		achivement[2] += bill.getTip();
		  	  		achivement[1] -= bill.getTip();
				}
	  	  		achivement[1] = achivement[1] + bill.getVisaReceived(); 
			}
			if(bill.getMasterReceived() > 0) {
  				masterQt++;
  				masterTotal += bill.getMasterReceived();
  				masterSales += bill.getMasterReceived();
				if(bill.getTip() > 0 && !tipCounted) {
					masterTip += bill.getTip();
					masterSales -= bill.getTip();
					tipCounted = true;
		  	  		achivement[2] += bill.getTip();
		  	  		achivement[1] -= bill.getTip();
				}
	  	  		achivement[1] = achivement[1] + bill.getMasterReceived(); 
			}
			
			waiterSalesData.put(waiterName, achivement);
  		}
		
		String strcashTotal = BarUtil.formatMoney(cashTotal / 100.0);
		String strdebitSales = BarUtil.formatMoney(debitSales / 100.0), strdebitTip = BarUtil.formatMoney(debitTip / 100.0), strdebitTotal = BarUtil.formatMoney(debitTotal / 100.0);
		String strvisaSales = BarUtil.formatMoney(visaSales / 100.0), strvisaTip = BarUtil.formatMoney(visaTip / 100.0), strvisaTotal = BarUtil.formatMoney(visaTotal / 100.0);
		String strmasterSales = BarUtil.formatMoney(masterSales / 100.0), strmasterTip = BarUtil.formatMoney(masterTip / 100.0), strmasterTotal = BarUtil.formatMoney(masterTotal / 100.0);
		String strotherSales = BarUtil.formatMoney(otherSales / 100.0), strotherTip = BarUtil.formatMoney(otherTip / 100.0), strotherTotal = BarUtil.formatMoney(otherTotal / 100.0);
		//content.append("PaidBy  Qt     Sales      Tip   
		content.append("Cash")
		.append(BarUtil.generateString(10 - 4 - String.valueOf(cashQt).length(), " ")).append(cashQt)
		.append(BarUtil.generateString(10 - String.valueOf(strcashTotal).length(), " ")).append(strcashTotal)
		.append(BarUtil.generateString(9 - 4, " ")).append("0.00")	//TIP always use 0, because if waiter received tip in case, he will never report ;)
		.append(BarUtil.generateString(width - 29 - strcashTotal.length(), " ")).append(strcashTotal).append("\n");
		if(cashRefundTotal < 0) {
			String strCashRefundTotal = BarUtil.formatMoney(cashRefundTotal / 100.0);
			content.append("RF-Cash")
			.append(BarUtil.generateString(10 - 7 - String.valueOf(cashRefundQT).length(), " ")).append(cashRefundQT)
			.append(BarUtil.generateString(10 - strCashRefundTotal.length(), " ")).append(strCashRefundTotal)
			.append(BarUtil.generateString(22 - strCashRefundTotal.length(), " ")).append(strCashRefundTotal).append("\n");
		}
		content.append("Debit")
		.append(BarUtil.generateString(10 - 5 - String.valueOf(debitQt).length(), " ")).append(debitQt)
		.append(BarUtil.generateString(10 - String.valueOf(strdebitSales).length(), " ")).append(strdebitSales)
		.append(BarUtil.generateString(9 - strdebitTip.length(), " ")).append(strdebitTip)
		.append(BarUtil.generateString(width - 29 - strdebitTotal.length(), " ")).append(strdebitTotal).append("\n");
		if(debitRefundTotal < 0) {
			String strDebitRefundTotal = BarUtil.formatMoney(debitRefundTotal / 100.0);
			content.append("RF-Debit")
			.append(BarUtil.generateString(10 - 8 - String.valueOf(debitRefundQT).length(), " ")).append(debitRefundQT)
			.append(BarUtil.generateString(10 - strDebitRefundTotal.length(), " ")).append(strDebitRefundTotal)
			.append(BarUtil.generateString(22 - strDebitRefundTotal.length(), " ")).append(strDebitRefundTotal).append("\n");
		}
		content.append("Visa")
		.append(BarUtil.generateString(10 - 4 - String.valueOf(visaQt).length(), " ")).append(visaQt)
		.append(BarUtil.generateString(10 - String.valueOf(strvisaSales).length(), " ")).append(strvisaSales)
		.append(BarUtil.generateString(9 - strvisaTip.length(), " ")).append(strvisaTip)
		.append(BarUtil.generateString(width - 29 - strvisaTotal.length(), " ")).append(strvisaTotal).append("\n");
		if(visaRefundTotal < 0) {
			String strVisahRefundTotal = BarUtil.formatMoney(visaRefundTotal / 100.0);
			content.append("RF-Visa")
			.append(BarUtil.generateString(10 - 7 - String.valueOf(visaRefundQT).length(), " ")).append(visaRefundQT)
			.append(BarUtil.generateString(10 - strVisahRefundTotal.length(), " ")).append(strVisahRefundTotal)
			.append(BarUtil.generateString(22 - strVisahRefundTotal.length(), " ")).append(strVisahRefundTotal).append("\n");
		}
		content.append("Master")
		.append(BarUtil.generateString(10 - 6 - String.valueOf(masterQt).length(), " ")).append(masterQt)
		.append(BarUtil.generateString(10 - String.valueOf(strmasterSales).length(), " ")).append(strmasterSales)
		.append(BarUtil.generateString(9 - strmasterTip.length(), " ")).append(strmasterTip)
		.append(BarUtil.generateString(width - 29 - strmasterTotal.length(), " ")).append(strmasterTotal).append("\n");
		if(masterRefundTotal < 0) {
			String strMasterRefundTotal = BarUtil.formatMoney(masterRefundTotal / 100.0);
			content.append("RF-Mast")
			.append(BarUtil.generateString(10 - 7 - String.valueOf(masterRefundQT).length(), " ")).append(masterRefundQT)
			.append(BarUtil.generateString(10 - strMasterRefundTotal.length(), " ")).append(strMasterRefundTotal)
			.append(BarUtil.generateString(22 - strMasterRefundTotal.length(), " ")).append(strMasterRefundTotal).append("\n");
		}
		
		for(Entry<String, ReportDto> entry : otherPayMethodMap.entrySet()){
			ReportDto dto = entry.getValue();
			String key = entry.getKey().toString();
			String qt = String.valueOf(dto.getQt());
			String received = BarUtil.formatMoney(dto.getMoneyRecieved() / 100.0);
			String tip = BarUtil.formatMoney(dto.getTip());
			String total = BarUtil.formatMoney(dto.getTotal());
			
			content.append(key)
			.append(BarUtil.generateString(10 - key.length() - qt.length(), " ")).append(qt)
			.append(BarUtil.generateString(10 - received.length(), " ")).append(received)
			.append(BarUtil.generateString(9 - tip.length(), " ")).append(tip)
			.append(BarUtil.generateString(width - 29 - total.length(), " ")).append(total).append("\n");
			if(otherRefundTotal < 0) {
				String strOtherRefundTotal = BarUtil.formatMoney(otherRefundTotal / 100.0);
				content.append("RF-" + key)
				.append(BarUtil.generateString(10 - key.length() - 3 - String.valueOf(otherRefundQT).length(), " ")).append(otherRefundQT)
				.append(BarUtil.generateString(10 - strOtherRefundTotal.length(), " ")).append(strOtherRefundTotal)
				.append(BarUtil.generateString(22 - strOtherRefundTotal.length(), " ")).append(strOtherRefundTotal).append("\n");
			}
			otherQt += dto.getQt();
			otherSales += dto.getMoneyRecieved();
			otherTip += dto.getTip();
			otherTotal += dto.getTotal();
        }
		//seperator--------------
		content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
		//total values-------------
		int totalQt = cashQt + debitQt + visaQt + masterQt + otherQt + cashRefundQT + debitRefundQT + visaRefundQT + masterRefundQT + otherRefundQT;
		int totalSales = cashTotal + debitSales + visaSales + masterSales + otherSales;
		int totalTips = 0 + debitTip + visaTip + masterTip + otherTip;
		int totalRefund = cashRefundTotal + debitRefundTotal + visaRefundTotal + masterRefundTotal + otherRefundTotal;
		String strTotalSales = BarUtil.formatMoney((totalSales + totalRefund) / 100.0);
		String strTotalTip = BarUtil.formatMoney((0 + debitTip + visaTip + masterTip + otherTip) / 100.0);
		String strTotalTotal = BarUtil.formatMoney((totalSales + totalTips + totalRefund) / 100.0);
		content.append(BarUtil.generateString(10 - String.valueOf(totalQt).length(), " ")).append(totalQt)
		.append(BarUtil.generateString(10 - strTotalSales.length(), " ")).append(strTotalSales)
		.append(BarUtil.generateString(9 - strTotalTip.length(), " ")).append(strTotalTip)
		.append(BarUtil.generateString(width - 29 - strTotalTotal.length(), " ")).append(strTotalTotal).append("\n");
		if(BarOption.showPaymentInReport()) {
			strAryFR.add(content.toString());
		}
		if(BarOption.showServerDataInReport()) {
			content = new StringBuilder();
			//server datas-------
			String serverSales = "Server Summary";
			emptySpaceStr = BarUtil.generateString((width - serverSales.length())/2, " ");
			content.append("\n");
			content.append(emptySpaceStr).append(serverSales).append("\n");
			content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
			content.append("Name     Qt   Sales     Tip   ").append(BarUtil.generateString(width - 34, " ")).append("Cash").append("\n");
			content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
			
			for (Entry<String,int[]> entry: waiterSalesData.entrySet()) {
				String waiter = (entry.getKey().length() < 8 ? entry.getKey() : entry.getKey().subSequence(0, 7)) + ":";
				String waiterSaleStr = BarUtil.formatMoney(entry.getValue()[1] / 100.0);
				String waiterTipStr = BarUtil.formatMoney(entry.getValue()[2] / 100.0);
				String waiterCashStr = BarUtil.formatMoney(entry.getValue()[3] / 100.0);
				String waiterRFTotal = BarUtil.formatMoney(entry.getValue()[5] / 100.0);
				String waiterRFCash = (0 - entry.getValue()[5]) > entry.getValue()[3] ? ("-" + waiterCashStr) : waiterRFTotal;
				
	 			content.append(waiter).append(BarUtil.generateString(11 - waiter.length() - (String.valueOf(entry.getValue()[0])).length(), " ")).append(entry.getValue()[0]);
				content.append(BarUtil.generateString(8 - waiterSaleStr.length(), " ")).append(waiterSaleStr);
				content.append(BarUtil.generateString(9 - waiterTipStr.length(), " ")).append(waiterTipStr);
				content.append(BarUtil.generateString(14 - waiterCashStr.length(), " ")).append(waiterCashStr).append("\n");
	
				content.append("RF")
				.append(BarUtil.generateString(11 - 2 - String.valueOf(entry.getValue()[4]).length(), " ")).append(entry.getValue()[4]);
				content.append(BarUtil.generateString(8 - waiterRFTotal.length(), " ")).append(waiterRFTotal);
				content.append(BarUtil.generateString(23 - waiterRFCash.length(), " ")).append(waiterRFCash);
				content.append("\n");
			}
			strAryFR.add(content.toString());
		}
	}

	private static void pushSummaryByServiceType(ArrayList<String> strAryFR, List<Bill> list, int width) {
		StringBuilder content = new StringBuilder();
		//title
		String paymentSummary = "Service Type Summary";
		String emptySpaceStr = BarUtil.generateString((width - paymentSummary.length())/2, " ");
		content.append("\n");
		content.append(emptySpaceStr).append(paymentSummary).append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
		content.append("Type      Qt   SubTotal     TOTAL").append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");

		HashMap<Character, Float> serviceTypeMap = new HashMap<Character, Float>();
		HashMap<Character, int[]> serviceTypeQTMap = new HashMap<Character, int[]>();
		int totalQT = 0;
		int totalSale = 0;
		for (Bill bill : list) {
  			String tableName = bill.getTableName();
  			if(tableName == null) {
  				L.e("Found bill with no tableName", "found a bill with no tableName", null);
  				continue;
  			}
  			Character firstCharacter = tableName.trim().substring(0, 1).toUpperCase().toCharArray()[0];
  			if('1' <= firstCharacter && firstCharacter <= '9' || firstCharacter == 'T') { //normal table
	  			Float totalPrice = serviceTypeMap.get('I');
	  			if(totalPrice == null) {
	  				serviceTypeMap.put((Character)'I', bill.getTotal() / 100.0f);
	  			}else {
	  				serviceTypeMap.put((Character)'I', totalPrice + bill.getTotal());
	  			}
	  			int[] nums = serviceTypeQTMap.get('I');
	  			if(nums == null) {
	  				nums = new int[3];
	  				nums[0] = 1;
	  				serviceTypeQTMap.put('I', nums);
	  			}else {
	  				nums[0] += 1;
	  			}

	  			totalQT ++;
	  			totalSale += bill.getTotal() * 100;
	  			
	  			if(bill.getStatus() < 0) { //@NOTE, only process when status < 0, when status > 0 means other stuff, shouldn't be calculated.
	  				nums[1] ++;
	  				nums[2] += bill.getStatus();
	  				totalQT ++;
	  				totalSale += bill.getStatus();
	  			}
  			}else {
	  			Float totalPrice = serviceTypeMap.get(firstCharacter);
	  			if(totalPrice == null) {
	  				serviceTypeMap.put(firstCharacter, bill.getTotal()/100.0f);
	  			}else {
	  				serviceTypeMap.put(firstCharacter, totalPrice + bill.getTotal());
	  			}
	  			
	  			int[] nums = serviceTypeQTMap.get(firstCharacter);
	  			if(nums == null) {
	  				nums = new int[3];
	  				nums[0] += 1;
	  				serviceTypeQTMap.put(firstCharacter, nums);
	  			}else {
	  				nums[0] += 1;
	  			}

	  			totalQT ++;
	  			totalSale += bill.getTotal() * 100;
	  			
	  			if(bill.getStatus() < 0) { //@NOTE, only process when status < 0, when status > 0 means other stuff, shouldn't be calculated.
	  				nums[1] += 1;
	  				nums[2] += bill.getStatus();
	  				totalQT ++;
	  				totalSale += bill.getStatus();
	  			}
  			}
  		}
		List<Character> keys = new ArrayList<Character>();
		keys.add('I');
		keys.add('0');
		keys.add('G');
		keys.add('P');
		keys.add('U');
		keys.add('H');
		keys.add('D');
		keys.add('F');
		keys.add('X');
		for (Character key : keys) {
			Float value = serviceTypeMap.get(key);
			if(value == null) {
				continue;
			}
			String total = BarUtil.formatMoney(value);
			String subTotal = BarUtil.formatMoney(Math.round(value * 100 / ((100 + BarOption.getGST() + BarOption.getQST()) / 100.0)) / 100.0);
			int[] nums = serviceTypeQTMap.get(key);
			content.append(PrintUtil.getTitle(key))
			.append(BarUtil.generateString(12 - PrintUtil.getTitle(key).length() - String.valueOf(nums[0]).length(), " ")).append(nums[0])
			.append(BarUtil.generateString(11 - subTotal.length(), " ")).append(subTotal)
			.append(BarUtil.generateString(10 - String.valueOf(total).length(), " ")).append(total).append("\n");
			
			if(nums[1] != 0) {
				String str = BarUtil.formatMoney(nums[2] / 100.0);

				subTotal = BarUtil.formatMoney(Math.round(nums[2] / ((100 + BarOption.getGST() + BarOption.getQST()) / 100.0)) / 100.0);
				content.append("RF").append(BarUtil.generateString(12 - 2 - String.valueOf(nums[1]).length(), " ")).append(nums[1])
				.append(BarUtil.generateString(11 - subTotal.length(), " ")).append(subTotal)
				.append(BarUtil.generateString(10 - str.length(), " ")).append(str).append("\n");
			}
		}
		//for other unknow types.
		for (Entry<Character, Float> entry : serviceTypeMap.entrySet()) {
			Character key = entry.getKey();
			if(keys.contains(key)) {
				continue;
			}
			
			Float value = entry.getValue();
			if(value == null) {
				continue;
			}
			
			String total = BarUtil.formatMoney(value);
			String subTotal = BarUtil.formatMoney(Math.round(value * 100 / ((100 + BarOption.getGST() + BarOption.getQST()) / 100.0)) / 100.0);
			int[] nums = serviceTypeQTMap.get(key);
			content.append(PrintUtil.getTitle(key))
			.append(BarUtil.generateString(12 - PrintUtil.getTitle(key).length() - String.valueOf(nums[0]).length(), " ")).append(nums[0])
			.append(BarUtil.generateString(10 - subTotal.length(), " ")).append(subTotal)
			.append(BarUtil.generateString(10 - total.length(), " ")).append(total).append("\n");
			
			if(nums[1] != 0) {
				String str = BarUtil.formatMoney(nums[2] / 100.0);
				subTotal = BarUtil.formatMoney(Math.round(nums[2] / ((100 + BarOption.getGST() + BarOption.getQST()) / 100.0)) / 100.0);
				content.append("RF").append(BarUtil.generateString(12 - 2 - String.valueOf(nums[1]).length(), " ")).append(nums[1])
				.append(BarUtil.generateString(11 - subTotal.length(), " ")).append(subTotal)
				.append(BarUtil.generateString(10 - str.length(), " ")).append(str).append("\n");
			}
		}
		
		content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
		
		String str = BarUtil.formatMoney(totalSale / 100.0);
		String subTotal = BarUtil.formatMoney(Math.round(totalSale / ((100 + BarOption.getGST() + BarOption.getQST()) / 100.0)) / 100.00);

		content.append(BarUtil.generateString(12 - String.valueOf(totalQT).length(), " ")).append(totalQT)
		.append(BarUtil.generateString(11 - subTotal.length(), " ")).append(subTotal)
		.append(BarUtil.generateString(10 - str.length(), " ")).append(str).append("\n");
				
		strAryFR.add(content.toString());
	};
	
	private static void pushVoidBillSummary(ArrayList<String> strAryFR, int width, String startDateStr, String endDateStr) {
		StringBuilder content = new StringBuilder();
		//title
		String voidItemSummary = "Void Bill Summary";
		String emptySpaceStr = BarUtil.generateString((width - voidItemSummary.length())/2, " ");
		content.append("\n");
		content.append(emptySpaceStr).append(voidItemSummary).append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
		content.append("Bill#  Time    Table Cust# Total Waiter Reason").append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");

		StringBuilder sql = new StringBuilder("Select * from BILL where BILL.STATUS = ").append(DBConsts.VOIDED)
				.append(" and createdTime >= '").append(startDateStr).append("' and createdTime <= '").append(endDateStr).append("'");
        if(LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS) {
        	sql.append(" and userId = ").append(LoginDlg.USERID);
        }else {
        	sql.append(" order by userId");
        }
        
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            double totalSales = 0f;
            while (rs.moveToNext()) {
                Integer billId = DBUtil.getCursorInt(rs,"_id");
                content.append(billId);
                
                String time = DBUtil.getCursorString(rs,"openTime");
                content.append(BarUtil.generateString(6 - billId.toString().length(), " ")).append(time.substring(5, 16));	//remove the year to make it shorter.
                
                String table = DBUtil.getCursorString(rs,"tableName");
                content.append(BarUtil.generateString(4 - table.length(), " ")).append(table);
        		
                Integer seatIdx = DBUtil.getCursorInt(rs,"billIndex");
                content.append(BarUtil.generateString(3 - seatIdx.toString().length(), " ")).append(seatIdx);
                
                double f = DBUtil.getCursorInt(rs,"total") / 100.0;
                String money = BarUtil.formatMoney(f);
                content.append(BarUtil.generateString(8 - money.length(), " ")).append(money);
                totalSales += f;

                Integer employeeId = DBUtil.getCursorInt(rs,"userId");
                content.append("  ").append(employeeId);
                
                String comment = DBUtil.getCursorString(rs,"comment");
                int p = comment.indexOf(REASON_MARK);
                if(p >= 0) {
                	comment = comment.substring(p + REASON_MARK.length()).trim();
                	if(comment.length() > 10) {
                		comment = comment.substring(0, 8) + "..";
                	}
                    content.append(" ").append(comment);
                }
                content.append("\n");
            }	
            
    		content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
    		String toltal = BarUtil.formatMoney(totalSales);
    		content.append(BarUtil.generateString(32 - toltal.length(), " "))
    		.append(toltal).append("\n");
    		
    		strAryFR.add(content.toString());

            rs.close();// 关闭
        }catch(Exception e) {
        	L.e("Pos", "Printing Report", e);
        }
	}
	
	private static void pushRemovedItemSummary(ArrayList<String> strAryFR, int width, String startDateStr, String endDateStr) {
		StringBuilder content = new StringBuilder();
		//title
		String voidItemSummary = "Removed Items Summary";
		String emptySpaceStr = BarUtil.generateString((width - voidItemSummary.length())/2, " ");
		content.append("\n");
		content.append(emptySpaceStr).append(voidItemSummary).append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");
		content.append("Bill#     Time   Table  Dish       Qt   Price").append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");

		StringBuilder sql = new StringBuilder("Select * from SALE_RECORD, DISH, BILL where BILL.STATUS != ").append(DBConsts.VOIDED)
				.append(" and SALE_RECORD.STATUS = ").append(DBConsts.VOIDED)
				.append(" and createdTime >= '").append(startDateStr).append("' and createdTime <= '").append(endDateStr)
				.append("' and SALE_RECORD.productId = DISH._id and SALE_RECORD.billId = bill._id");
        if(LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS) {
        	sql.append(" and userId = ").append(LoginDlg.USERID);
        }
        
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            float totalSales = 0f;
            while (rs.moveToNext()) {
            	String billId = DBUtil.getCursorString(rs,"SALE_RECORD._id");
                content.append(billId);	//remove the year to make it shorter.
                
                String time = DBUtil.getCursorString(rs,"SALE_RECORD.createdTime");
                content.append(BarUtil.generateString(6 - billId.toString().length(), " ")).append(time.substring(5, 16));	//remove the year to make it shorter.
                
                String table = DBUtil.getCursorString(rs,"SALE_RECORD.tableName");
                content.append(BarUtil.generateString(4 - table.length(), " ")).append(table);
        		
                String product = DBUtil.getCursorString(rs,"DISH.NAME1");
                if(product.length() > 12) {
                	product = product.substring(0, 10) + "..";
                }else {
                	product= product + BarUtil.generateString(12 - product.length(), " ");
                }
                content.append("  ").append(product);
                
                int i = DBUtil.getCursorInt(rs,"SALE_RECORD.AMOUNT");
                String displayableNum = DBUtil.getDisplayableNum(i).toString();
                content.append(BarUtil.generateString(3 - displayableNum.length(), " ")).append(displayableNum);
                
                float f = Float.valueOf((float) (DBUtil.getCursorInt(rs,"SALE_RECORD.toltalPrice") / 100.0));
                String money = BarUtil.formatMoney(f);
                content.append(BarUtil.generateString(width - 38 - money.length(), " ")).append(money).append("\n");
                totalSales += f;
            }
            
    		content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
    		String toltal = BarUtil.formatMoney(totalSales);
    		content.append(BarUtil.generateString(width - toltal.length(), " "))
    		.append(toltal).append("\n");
    		
    		strAryFR.add(content.toString());

            rs.close();// 关闭
        }catch(Exception e) {
        	L.e("Pos", "Printing Report", e);
        }
	};
	
	private static void pushUnclosedBillsSummary(ArrayList<String> strAryFR, int width, String startTime, String endTime) {
		StringBuilder content = new StringBuilder();
		//title
		String unclosedTables = "Unclosed Bills";
		String emptySpaceStr = BarUtil.generateString((width - unclosedTables.length())/2, " ");
		content.append("\n");
		content.append(emptySpaceStr).append(unclosedTables).append("\n");
		content.append(BarUtil.getSeperatorLine(0, width)).append("\n");

		content.append("Inv#   Table#      Total").append("\n");
		
        StringBuilder sql = new StringBuilder("select * from BILL, USER where BILL.userId = USER._id ")
        		//.append(" and createdTime >= '").append(startTime)
        		//.append("' and createdTime <= '").append(endTime)
        		.append(" and (BILL.STATUS is null or (BILL.STATUS >= ").append(DBConsts.ORIGINAL)
        		.append(" and BILL.STATUS < ").append(DBConsts.COMPLETED).append("))");
        if(LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS) {	//if is not admin, then get out only user related records.
        	sql.append(" and USER._id = ").append(LoginDlg.USERID);
        }

        float totalSales = 0.0f;
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            while (rs.moveToNext()) {
            	String id = String.valueOf(DBUtil.getCursorInt(rs,"_id"));
            	content.append(id);
            	String tableName = DBUtil.getCursorString(rs,"tableName");
            	content.append(BarUtil.generateString(12 - id.length() - tableName.length(), " ")).append(tableName);
            	float total = Float.valueOf((float) (DBUtil.getCursorInt(rs,"TOTAL") / 100.0));
            	totalSales += total;
            	String formattedTotal = BarUtil.formatMoney(total);
            	content.append(BarUtil.generateString(12 - formattedTotal.length(), " ")).append(formattedTotal);
//            	content.append("    ").append(DBUtil.getCursorString(rs,"OpenTime"));
            	content.append("\n");
            }
            rs.close();// 关闭
        } catch (Exception e) {
            ErrorUtil.write(e);
        }
        
        content.append(BarUtil.getSeperatorLine(1, width)).append("\n");
		String toltal = BarUtil.formatMoney(totalSales);
		content.append(BarUtil.generateString(24 - toltal.length(), " ")).append(toltal).append("\n");
		
		strAryFR.add(content.toString());
		strAryFR.add(" ");
		strAryFR.add(" ");
		strAryFR.add(" ");
		strAryFR.add(" ");
		strAryFR.add(" ");
		strAryFR.add(" ");
		strAryFR.add(" ");//just to make the size reach 15 to be recognized as a report.
	};
	
    private static StringBuilder getFormattedBillHeader(int tWidth, String billId, String tableName, String orderNo, String customerIndex) {
    	StringBuilder sb = new StringBuilder();
    	String s = BarOption.getBillHeadInfo();
    	if(s != null && s.trim().length() > 0) {
    		String[] infos = s.split(":");
    		for(String info : infos) {
    			int length = (tWidth - info.length())/2;
    			sb.append(BarUtil.generateString(length, " "));
    			sb.append(info);
    			sb.append("\n");
    		}
    	}
    		
    	//order ID
		if(BarOption.showOrderNoOnBill()) {
	    	orderNo = "order#" + orderNo;
			sb.append(BarUtil.generateString((tWidth - orderNo.length())/2, " "));
	    	sb.append(orderNo);
	    	sb.append("\n");
		}
		//address on bill
    	if(tableName.toUpperCase().startsWith("D")) {
    		sb.append(PrintUtil.findClientInfoByTableName(tableName));
    		sb.append("\n");
    	}
		//table and invoice number
		String idx = "";
		if(BarOption.showTableOnBill()) {
			idx = "#" + tableName;
			if(customerIndex != null && !"".equals(customerIndex) && !"0".equals(customerIndex)) {
				idx += ("-" + customerIndex);
			}
		}
		idx += "  " + INVOICE + billId;
		sb.append(BarUtil.generateString((tWidth - idx.length())/2, " "));
		sb.append(idx);
		sb.append("\n");
		
    	return sb;
    }

    //get the number which start to count since batch start.
	private static String getOrderNoOfBill(String billId) {
		StringBuilder sql = new StringBuilder("select * from bill where _id = ").append(billId);
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            rs.moveToNext();
            int invoiceid = DBUtil.getCursorInt(rs,"invoiceid");
            if(invoiceid > 0) {
            	return String.valueOf(invoiceid);
            }
		}catch(Exception exp) {
			L.e("PrintService ", "unexpected error when fetching the invoiceId of bill.", exp);
		}
		return null;
	}

	private static void updateOrderNoOnBillId(String nextIdx, String billId) {
		StringBuilder sql = new StringBuilder("update bill set invoiceId = ").append(nextIdx).append(" where _id = ").append(billId);
		try {
			AikaPos.db.execSQL(sql.toString());
		}catch(Exception exp) {
			L.e("PrintService ", "unexpected error when updating the invoiceId of bill. billId = " + billId + "!" , exp);
		}
	}

	private static String formatContentForOrder(List<Dish> list, String curPrintIp,
    		String curTable, String curBill, String waiterName, boolean isCancelled){
        //L.d(TAG,"formatContentForPrint");
        String font = (String)CustOpts.custOps.getValue(curPrintIp + "font");
        if(font ==  null || font.length() < 1) {
            font = (String)CustOpts.custOps.getValue("font");
        }
        
        int tWidth = BarUtil.getPreferedWidth();
        Integer w = ipPrinterMap.get(curPrintIp).getStatus();
        if(w != null) {
        	tWidth = w.intValue();
        }
        StringBuilder content = new StringBuilder("\n\n");
        if(isCancelled) {
        	content.append(BarUtil.generateString((tWidth - 4)/2, " ")).append("STOP");
        }
        if(tWidth < 20) {
            content.append("\n\n");
        }
        content.append("(").append(curTable).append(")");

        int lengthOfStrToDisplay = 3 + curTable.length();
        if(BarOption.isDisplayBillInKitchen()) {
        	content.append(curBill);
        	lengthOfStrToDisplay += curBill.length();
        }
        
        content.append("   ");	//the first 3 spaces of the spaceStr is consumed first.
        if(BarOption.isDisplayWaiterInKitchen()) {
        	content.append(waiterName);
        	lengthOfStrToDisplay += waiterName.length();
        }

        DateFormat df = new SimpleDateFormat("HH:mm");
        String dateStr = df.format(new Date());
        lengthOfStrToDisplay += dateStr.length();
        String spaceStr = BarUtil.generateString(tWidth - lengthOfStrToDisplay - 3, " ");
        
        content.append(spaceStr).append(dateStr).append("\n");

        String sep_str1 = isCancelled ? "!" : (String)CustOpts.custOps.getValue("sep_str1");
        if(sep_str1 == null || sep_str1.length() == 0){
            sep_str1 = BarUtil.SEP_STR1;
        }
        String sep_str2 = isCancelled ? "X" : (String)CustOpts.custOps.getValue("sep_str2");
        if(sep_str2 == null || sep_str2.length() == 0){
            sep_str2 = BarUtil.SEP_STR2;
        }

        content.append(BarUtil.generateString(tWidth, sep_str1)).append("\n\n");
        int langIndex = ipPrinterMap.get(curPrintIp).getLangType();
        for(Dish d:list){
            StringBuilder sb = new StringBuilder();
            if(BarOption.isDisDishIDInKitchen()) {
                sb.append(d.getId());
                sb.append(BarUtil.generateString(5 - String.valueOf(d.getId()).length(), " "));
            }
            sb.append(BarUtil.getPlainTextOut(d.getLanguage(langIndex)));
            if(d.getNum() > 1){//Note：when sending to kitchen, the number couldnot be bigger than 10000.
                String space = " ";
                int occupiedLength = BarUtil.getLengthOfString(sb.toString());
                sb.append(BarUtil.generateString(tWidth - occupiedLength - (d.getNum() < 10 ? 2 : 3), " "));
                sb.append("x").append(Integer.toString(d.getNum()));
            }
            content.append(sb);
            content.append("\n");
            String modification = d.getModification();
            if(modification != null && !"null".equalsIgnoreCase(modification)) {
            	String modifyStr = d.getModification();
            	String[] notes = modifyStr.split(BarDlgConst.delimiter); 
                for (String str : notes) {
                	//@NOTE： currently it's not possit to have multi element, because we save only one lang of the modification in dish.
                	//because there's "modify X num" in the modification string, so it's complex if we add multi lang into it.
                	//while it's sure better if we set all lang into the dish's modifyString property. because if it's openned by other waiter, it will show with right lang...
//                	String[] langs = str.split(BarDlgConst.semicolon);
//                	String lang = langs.length > langIndex ? langs[langIndex] : langs[0];
//                	if(lang.length() == 0)
//                		lang = langs[0];
                	if(str.trim().length() < 1 || str.trim().startsWith("-" + BarOption.getMoneySign())) {
                		continue;
                	}
                	int numP = str.indexOf("x");
                	String numStr = "";
                	if(numP > 0) { //if the string folowing "x" is number, then update the numStr.
                		try {
                			numStr = String.valueOf(Integer.valueOf(str.substring(numP + 1)));
                			str = str.substring(0, numP);
                		}catch(Exception e) {}
                	}
                	
                    content.append(BarUtil.generateString(2, " ")).append("* ").append(findRightLang(str, langIndex));
                    if(numStr.length() > 0) {
                    	content.append("x").append(numStr);
                    }
                    content.append(" *\n");
                }
            }
            Object sep = CustOpts.custOps.getValue("customizedSeperator");
            if(sep != null && !"null".equalsIgnoreCase(sep.toString())) {
            	if("".equals(sep)) {
            		sep = " ";
            	}
            	content.append(BarUtil.generateString(tWidth, (String)sep)).append("\n");
            }
        }
        
        return content.substring(0, content.length());//spec change: do not to show separator!  - (tWidth + 1));
    }

    private static Object findRightLang(String str, int langIdx) {
    	StringBuilder sql = new StringBuilder("select lang");
    	sql.append(langIdx + 1).append(", lang1 from modification where (lang1 = ? or lang2 = ? or lang3 = ? or lang4 = ?)");
    	sql.append(" and status = ").append(DBConsts.ORIGINAL);
        try {
        	str = str.trim();
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), new String[]{str, str, str, str});
            
            if (rs.moveToNext()) {
            	String val = rs.getString(1);
            	return  val== null || val.length() == 0 ? str : val;
            }
		}catch(Exception exp) {
			L.e("AddModificationDlg", "exception when querying full langs with sql " + sql, exp);
		}
        L.e("AddModificationDlg", "find no modification record!" + sql, null);
        return null;
	}

	private static void flushIpContent() {
    	if(!isIpContentMapEmpty()){
        	printContents();
        	L.w("PrintService","found non-empty ipContentMap when printing. has tried to flush them, now isIpContentMapEmpty = " + String.valueOf(isIpContentMapEmpty()), null);
        }
    }

    private static boolean isIpContentMapEmpty(){
    	if(ipContentMap == null) {
        	ipContentMap = new HashMap<String,List<String>>();
        }
    	if(ipContentMap.isEmpty()) {
        	for(Entry<String,Printer> entry: ipPrinterMap.entrySet()){
                ipContentMap.put(entry.getKey(),new ArrayList<String>());
            }
    	}
		for (Entry entry : ipContentMap.entrySet()) {
			List<String> listTypeValue = (List<String>) entry.getValue();
			if (listTypeValue.size() > 0) {
				L.w("PrintService", "ipContent is not empty, key is : " + entry.getKey() + ", and value is:" + listTypeValue, null);
				return false;
			}
		}
		return true;
    }    

    private static void reInitIpSelectionMap(){
        ipSelectionsMap = new HashMap<String,List<Dish>>();

        for(Entry<String,Printer> entry: ipPrinterMap.entrySet()){
            ipSelectionsMap.put(entry.getKey(),new ArrayList<Dish>());
        }
    }

    public static String[] getCurrentPays(List<String> sndMsg, String transType) {
    	if(ValuePossible.V_RFER.equals(transType) || ValuePossible.R_RFER.equals(transType)
    			|| ValuePossible.TypTrans.ADDI.equals(transType)
    			|| sndMsg.get(sndMsg.size() - 1).contains(PAYMENT_FAIL.trim())) {
    		return new String[0];
    	}
        String currentPays = sndMsg.get(4);
        switch (transType) {
            case ValuePossible.TypTrans.RFER:
                currentPays = sndMsg.get(4);
                break;
            case ValuePossible.TypTrans.ADDI:
                currentPays = sndMsg.get(4);
                break;
        }
        return currentPays == null? new String[0] : currentPays.split("\n");
    }
	public static void sendDocument(boolean selected) {
		//TODO abc
	}


}
