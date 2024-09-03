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
    		JOptionPane.showMessageDialog(BarFrame.consts.NotPayYet());
    		return;
    	}
    	
 		//To make it easier to user, we decided to disable partial refund for now.     		BarFrame.numberPanelDlg.setVisible(true);
	    String[] options = {BarFrame.consts.WholeBillRefund(), BarFrame.consts.itemBasedRefund()};
		JOptionPane.showOptionDialog(org.cas.client.resources.international.DlgConst.DlgTitle, BarFrame.consts.confirmRefund(),
				options, new JOptionPane.OptionDialogListener() {
					@Override
					public void onOptionSelected(int selection) {
						if(selection == -1) {
							return;
						}else if(selection == 0) {
							BarFrame.numberPanelDlg.initContent(BarFrame.consts.Refund(), BarFrame.consts.RefundNotice());
							BarFrame.numberPanelDlg.show(BarFrame.instance.getSupportFragmentManager(), BarFrame.consts.Refund());
						}else if(selection == 1) {
//							RefundItemDlg(bitzp).setVisible(true);
						}
					}
				});
	}
	
	public static int checkRefundAmount(float refund, int refundAmount) {
		if(refundAmount < -1) {	//if already refund, then add into existing amount. //currently not possible, because we have checked the status to make sure status must be completed.
			if (JOptionPane.showConfirmDialog(BarFrame.consts.AlreadyRefund() + BarOption.getMoneySign() + (0-refundAmount) / 100.0, org.cas.client.resources.international.DlgConst.DlgTitle
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
		DBUtil.updateBill(billPanel.getBillID(), "STATUS", refundedLifeTime);
		
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
 		StringBuilder sb = new StringBuilder("select * from bill where _id = " + newBillID);
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
