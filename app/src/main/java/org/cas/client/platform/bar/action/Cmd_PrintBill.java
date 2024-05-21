package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.ui.panel.TablesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.ISButton;

import java.util.Date;

public class Cmd_PrintBill implements SamActionListener {

	private static Cmd_PrintBill instance;
	private Cmd_PrintBill() {}
	public static Cmd_PrintBill getInstance() {
		if(instance == null)
			instance = new Cmd_PrintBill();
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
		
		//check status if it's closed(voided, completed, refunded), do not allow to print.
		//add the check here because event the button is disabled, client can also call this methods from single instance.
		//didn't check bill has no item in it case, because when it's empty, it might because of all printed item was removed.
		if(billPanel.getStatus() >= DBConsts.VOIDED || billPanel.getStatus() < DBConsts.ORIGINAL) {
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.ActionNotAllowed());
			return;
		}
		
		billPanel.unsavedItemsCheck();		//will send new added(not printed yet) dishes to kitchen. also save output in db
		
		if(billPanel.getStatus() >= DBConsts.BILL_PRINTED) { // if already printed, means there's record in web-mev alredy, make sure refs are right.
			billPanel.expireAndRegenerateModifiedOutputs(DBConsts.BILL_PRINTED);
			billPanel.expireAndReGenerateBill(null);
		}
		// if the bill was the one that was generated when table opened, it has save createTime as openTime. now we take
		//the chance to update it to the real printed time. then, if there's an other revisee bill printed, system will
		//not confuse about the outputs(which was actually generated before first time print) should belongs to which bill.
		DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.BILL_PRINTED, BarUtil.df.format(new Date())); //already saved billRecord need to update price to reflect the new ordered dishes.
		BarFrame.instance.updateOutputStatus(DBConsts.BILL_PRINTED);
		
		//no price and item change, means should print a internal bill.
		String comment = billPanel.getComment();
		boolean hasModificationInPrice = comment.contains(BillPanel.SERVICEFEE) || comment.contains(BillPanel.DISCBILL);
		boolean hasItemAddedOrRemoved = DBUtil.isThereItemAddOrRemoved(billPanel.getBillID(), false);
		PrintService.printTemporaryBill(billPanel, billPanel.getOrderedDishAry(), hasItemAddedOrRemoved || hasModificationInPrice);
        
		DBUtil.cleanTemporalInfoInComment(billPanel, comment);
		
		if(BarOption.isCounterMode()) {
    		if(BarOption.stayAfterSave()){
    			billPanel.initContent();
    		} else {	//if not set to stay (in current table), move to next table.... 
    			((TablesPanel)BarFrame.instance.panels[0]).switchToOtherTable();
    		}
	    } else {
	    	BarFrame.instance.switchMode(0); //switch interface to mode 0(table selection interface)
	    }
	}
}
