 package org.cas.client.platform.bar.action;

 import android.view.View;

 import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_VoidOrder implements SamActionListener {

	private static Cmd_VoidOrder instance;
	private Cmd_VoidOrder() {}
	public static Cmd_VoidOrder getInstance() {
		if(instance == null)
			instance = new Cmd_VoidOrder();
		return instance;
	}
	
	private ISButton sourceBtn;
	
	public ISButton getSourceBtn() {
		return sourceBtn;
	}
	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}
	
	@Override
	// void order include saved ones
	public void onClick(View v) {
    	
//		//show up ReasonDialog, for user to select a reason for void.
//		ReasonDlg reasonDlg = new ReasonDlg(false);
//		reasonDlg.setVisible(true);
//		if(ReasonDlg.comment.length() == 0) {
//			return;
//		}
//
//		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
//		BillPanel billPanel = salesPanel.billPanel;
//		int billID = billPanel.getBillID();
//    	String clientIdx = BarFrame.instance.getCurBillIndex();
//		try {
//			//find out the total count of all saved relevant outputs.
//			//check if it's a "mistake-opening-table-action" or "adding bill action" by check if there's any output on it already.
//			//will be considered as non-empty as long as there's output connecting to the id, even the output is not currently displaying on this bill.
//			StringBuilder sql = new StringBuilder("select * from output where folderId = '").append(billID)
//					.append("' or (subject = '").append(BarFrame.instance.getCmbCurTable().getSelectedItem()).append("'")
//					.append(" and time = '").append(BarFrame.instance.valOpenTime.getText()).append("'")
//					.append(" and contactID = ").append(clientIdx).append(")"); 	//in future version, might need to check the deleted property.
//			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//			rs.last();
//
//		    if(rs.getRow() == 0) {			//if didn't find any saved output, means either empty, or all new.
//		    	sql = new StringBuilder("delete from bill where _id = ").append(billID);
//			    PIMDBModel.getStatement().executeUpdate(sql.toString());
//		    } else { 						//if found some saved outputs......saved outputs also means send to kitchen already.
//		    	ArrayList<Dish> orderedDishes = billPanel.getOrderedDishAry();
//		    	//clean the unsaved item from UI first.
//				for(int i = orderedDishes.size() - 1; i >= 0; i--) {//clean the new(unsent) items from billPanel first.
//		    		if(orderedDishes.get(i).getOutputID() <= 0) {
//		    			orderedDishes.remove(i);
//		        	}
//				}
//				billPanel.updateTotalArea(true);//to make sure the subtotal updated, so I can calculated.
//
//				//"not printed, but saved" dished send cancel print to ketchen.
//				ArrayList<Dish> notPrintedDishes = new ArrayList<>();
//				for(Dish dish: orderedDishes) {
//					if(dish.getStatus() < DBConsts.BILL_PRINTED) {
//						notPrintedDishes.add(dish);
//					}
//				}
//				billPanel.sendDishesToKitchen(notPrintedDishes, true);
//
////				//if bill never printed, print an internal bill. //now it's not possible, if not printed,the void button should be disabled. TODO: add this logic to cancel all.
////		        if(billPanel.getStatus() < DBConsts.billPrinted) {//in this case should print a internal used bill, no need to send request.
////		        	billPanel.updateTotalArea(false);			//still need to print internal used bill, because boss might wonder why dish prepared.
////		        	PrintService.printTemporaryBill(billPanel, orderedDishes, false);
////		        	return;
////		        }
//				//as asked by revenue quebec, we need to display a line of "cancelation:-money" before the subtotal, so the subtotal will display 0.00
//				//and there shouldn't be a discbill item......so I modify the possible discount bill info here.
//				float originalSubtotal = billPanel.subTotal + billPanel.discount - billPanel.serviceFee;
//				billPanel.serviceFee = 0;
//				billPanel.discount = originalSubtotal;
//				billPanel.updateTotalArea(true);
//		    	billPanel.expireAndReGenerateBill(null);	//create a new bill which status will be void.
//
//				//for the left dishes, all are alredy send to kitchen, so tell kitchen to stop preparing.
//
//			    //@NOTE: we need to process cur bill, give it a special status, so we can see the voided bills in check order dialog.
//		    	//and have to process it to be not null, better will not be considered as there's still non closed bill, when checking in isLastBill()
//		    	//update bill
//		    	billPanel.setComment( billPanel.getComment() + ValuePossible.VOID + " #REASON:" + ReasonDlg.comment);
//				sql = new StringBuilder("update bill set status = ").append(DBConsts.VOIDED)
//						.append(", comment = '").append(billPanel.getComment())
//						.append("' where _id = ").append(billPanel.getBillID());
//		    	PIMDBModel.getStatement().executeQuery(sql.toString());
//
//		    	//update output
//		    	sql = new StringBuilder("update output set deleted = ").append(DBConsts.VOIDED)
//		    			.append(" where folderId = ").append(billPanel.getBillID());
//		        PIMDBModel.getStatement().executeQuery(sql.toString());
//
//		    	//print a final receipt or notice kitchen to stop preparing. @NOTE: have to be done after the comment has been added.
//		    	PrintService.printVoid(billPanel, BarFrame.instance.getCurBillIndex());
//
//		    	if(BarOption.isCounterMode()) {
//		    		if(!BarOption.stayAfterSave()) {	//if not set to stay (in current table), move to next table....
//		    			((TablesPanel)BarFrame.instance.panels[0]).switchToOtherTable();
//		    		}
//			    } else {
//		        	BarFrame.instance.actionAfterBillClosed();;
//			    }
//		    }
//		}catch(Exception exp) {
//			L.e("void order", "error happend when voiding a bill with ID:"+ billID, exp);
//		}
	}
}
