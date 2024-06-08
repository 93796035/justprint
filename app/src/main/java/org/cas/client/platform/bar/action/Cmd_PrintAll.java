package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.BillListPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.web_srm.MevUtils;

import java.util.ArrayList;
import java.util.Date;

public class Cmd_PrintAll implements SamActionListener {

	private static Cmd_PrintAll instance;
	private Cmd_PrintAll() {}
	public static Cmd_PrintAll getInstance() {
		if(instance == null)
			instance = new Cmd_PrintAll();
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
		
		BillListPanel billListPanel = (BillListPanel) BarFrame.instance.panels[1];
		
		ArrayList<BillPanel> unclosedBillPanels = billListPanel.gatherAllUnclosedBillPanels();
		for (BillPanel billPanel : unclosedBillPanels) {
			//check status if it's closed(voided, completed, refunded), do not allow to print.
			//add the check here because event the button is disabled, client can also call this methods from single instance.
			//didn't check bill has no item in it case, because when it's empty, it might because of all printed item was removed.
			if(billPanel.getStatus() >= DBConsts.VOIDED || billPanel.getStatus() < DBConsts.ORIGINAL) {
				JOptionPane.showMessageDialog(BarFrame.consts.ActionNotAllowed());
				return;
			}
			
			billPanel.unsavedItemsCheck();		//will send new added(not printed yet) dishes to kitchen. also save output in db
			
			if(billPanel.getStatus() >= DBConsts.BILL_PRINTED) { // if already printed, means there's record in web-mev alredy, make sure refs are right.
				billPanel.expireAndReGenerateBill(null);
			}
			// if the bill was the one that was generated when table opened, it has save createTime as openTime. now we take
			//the chance to update it to the real printed time. then, if there's an other revisee bill printed, system will
			//not confuse about the outputs（ which was actually generated before first time print) should belongs to which bill.
			DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.BILL_PRINTED, BarUtil.df.format(new Date())); //already saved billRecord need to update price to reflect the new ordered dishes.
			
			boolean hasItemAddedOrRemoved = DBUtil.isThereItemAddOrRemoved(billPanel.getBillID(), false);
			if(!hasItemAddedOrRemoved) {//no item change, means should print a internal bill.
				PrintService.printTemporaryBill(billPanel, billPanel.getOrderedDishAry(), false);
				if(MevUtils.isBillIdAlreayInMevBatch(billPanel.getBillID())) { //check if there's batch to clean.
					MevUtils.flushBatchWithCurTransaction();
					return;
				}
				return;
			}
			
			PrintService.printTemporaryBill(billPanel, billPanel.getOrderedDishAry(), true);
			//NOTE: if some thing goes wrong for original bill printing, then we will have trouble (btw, it's hard to know if there's problem when printing
			//because action will prepare the content and send to the q
			
			//update the bill with new createTime and new comment
	        //(it pointing to the previous record by the specified billId and createTime and subtotal, which is not in our db, but in mev system), 
			DBUtil.updateBill(billPanel.getBillID(), "status", DBConsts.BILL_PRINTED);
			
			if(BarOption.isCounterMode()) {
	    		if(BarOption.stayAfterSave()) {
	    			billPanel.setStatus(DBConsts.BILL_PRINTED);
	    			billPanel.initContent();
	    		} else {//if not set to stay (in current table), move to next table.... 
//	    			((TablesPanel)BarFrame.instance.panels[0]).switchToOtherTable();
	    		}
		    } else {
		    	BarFrame.instance.switchMode(0); //switch interface to mode 0(table selection interface)
		    }
		}
	}
}
