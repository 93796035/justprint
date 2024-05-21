package org.cas.client.platform.bar.action;

import android.database.Cursor;
import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.L;

public class Cmd_Refund implements SamActionListener {

	private static Cmd_Refund instance;
	private Cmd_Refund() {}
	public static Cmd_Refund getInstance() {
		if(instance == null)
			instance = new Cmd_Refund();
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
	public void onClick(View v) {
		
		SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		
		//check if it's already paid.
    	if(billPanel.getStatus() != DBConsts.COMPLETED) {
    		JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.NotPayYet());
    		return;
    	}
    	
 		//To make it easier to user, we decided to disable partial refund for now.     		BarFrame.numberPanelDlg.setVisible(true);
	    Object[] options = {BarFrame.consts.WholeBillRefund(), BarFrame.consts.itemBasedRefund()};
//		int selection = JOptionPane.showOptionDialog(BarFrame.instance, BarFrame.consts.confirmRefund(), org.cas.client.resources.international.DlgConst.DlgTitle,
//	                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//		if(selection == JOptionPane.CLOSED_OPTION ) {
//			return;
//		}else if(selection == 1) {
//			new RefundItemDlg(BarFrame.instance).setVisible(true);
//		}else if(selection == 0) {
//			BarFrame.numberPanelDlg.setTitle(BarFrame.consts.Refund());
// 			BarFrame.numberPanelDlg.setNotice(BarFrame.consts.RefundNotice());
// 			BarFrame.numberPanelDlg.setBtnSource(null);
// 			BarFrame.numberPanelDlg.setFloatSupport(true);
// 			BarFrame.numberPanelDlg.setPercentSupport(false);
// 			BarFrame.numberPanelDlg.reLayout();
// 			BarFrame.numberPanelDlg.setModal(true);
// 			BarFrame.numberPanelDlg.setContents(billPanel.valTotlePrice.getText());
// 			BarFrame.numberPanelDlg.setVisible(true);
//
// 			String curContent = BarFrame.numberPanelDlg.curContent;
// 			if(StringUtil.isEmpty(curContent)){	//if numberPanelDlg is not closed by clicking the ok button, but the X button, the curContent will be set to "";
// 				return;
//			}
//
//			float toRefundThisTime = BarFrame.numberPanelDlg.isPercentage ?
// 				Float.valueOf(billPanel.valTotlePrice.getText()) * Float.valueOf(curContent)
// 				: Float.valueOf(curContent);
//
// 			if(toRefundThisTime > Float.valueOf(billPanel.valTotlePrice.getText())) {
// 				JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
// 			    return;
// 			}
//
// 			int refundedLifeTime = billPanel.getStatus();
//
// 			//user modified item/items, will be treated as partial refund, which will need to expire also the outputs records.
// 		    refundedLifeTime = checkRefundAmount(toRefundThisTime, refundedLifeTime);
// 			if(Integer.MAX_VALUE == refundedLifeTime) {
// 				return;
// 			}
//
// 			refundRoutineSteps(billPanel, toRefundThisTime, refundedLifeTime);
//		}
	}
	
	public static int checkRefundAmount(float refund, int refundAmount) {
		if(refundAmount < -1) {	//if already refund, then add into existing amount. //currently not possible, because we have checked the status to make sure status must be completed.
			if (JOptionPane.showConfirmDialog(BarFrame.instance, BarFrame.consts.AlreadyRefund() + BarOption.getMoneySign() + (0-refundAmount) / 100.0, org.cas.client.resources.international.DlgConst.DlgTitle
				)) {// already refunded, sure to refund again?
				return Integer.MAX_VALUE;
			}else {
				refundAmount -= Math.round(refund * 100);
			}
		}else {		//first time refund, then set the refund.  //this is the only possible case for now, since we added status check...
			refundAmount = 0 - Math.round(refund * 100);
		}
		return refundAmount;
	}
	
	/**
	 * @param billPanel
	 * @param toRefundThisTime how much this time will refund
	 * @param refundedLifeTime shoud be the value of current status(already refunded money) + how much this time will refund.
	 */
	public static void refundRoutineSteps(BillPanel billPanel, float toRefundThisTime, int refundedLifeTime) {
		//first, set old bill's status to expired and generate a new bill to reflect the refund result.
        billPanel.expireAndReGenerateBill(null);
    		    		
		//update the new bill's status in db to be the refund amount, (if we support multi-time refund, next time refund will base on this number).
		DBUtil.updateBill(billPanel.getBillID(), "status", refundedLifeTime);
		
		//prepare infomation for customer (bill)
		PrintService.printRefund(billPanel, - Math.round(toRefundThisTime * 100), BarFrame.instance.getCurBillIndex(), false);
		//update onscreen displays(changeDlg, MainFrame turning back to tableView, Customer View update)
 		popupMoneyBoxCheck(billPanel.getBillID(), refundedLifeTime);
		if(BarOption.isCounterMode()) {
//			((TablesPanel)BarFrame.instance.panels[0]).switchToOtherTable();
		}else {
			BarFrame.instance.switchMode(0);
		}
//		if(BarFrame.secondScreen != null) {		//customerFrame update.
//			BarFrame.customerFrame.initContent();
//			BarFrame.customerFrame.updateChange("", "", false);
//		}
	}
	
	// refundIntegerFormat should be negative
	private static void popupMoneyBoxCheck(int newBillID, int refundAmount) {
		//to check if we need to pop up money box.
 		StringBuilder sb = new StringBuilder("select * from bill where id = " + newBillID);
 		try {
 			Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
 		    rs.moveToNext();
 		 	int cashChange = DBUtil.getCursorInt(rs,"cashReceived");
 			if(refundAmount < -1 && cashChange > 0 ) {
 				Cmd_OpenDrawer.getInstance().onClick(null);
 			    if(cashChange > 0 - refundAmount) {
 			    	cashChange = 0 - refundAmount;
 			    }
 			 	            
// 			    new ChangeDlg(BarFrame.instance,
// 			    	BarOption.getMoneySign() + BarUtil.canadianPennyRound(BarUtil.formatMoney(cashChange / 100.0))).setVisible(true); //it's a non-modal dialog.
 		 	}
 		 }catch(Exception exp) {
 		 	L.e("Cmd_Refund ", " failed to fetch bill with Id:" + newBillID, exp);
 		 }
	}
}
