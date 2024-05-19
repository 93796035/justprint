package org.cas.client.platform.bar.action;

import android.view.View;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil.getCursorString(rs,;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.pimmodel.PIMDBModel;
import org.cas.client.resource.international.DlgConst;

public class Cmd_Suspend implements SamActionListener {

	private static Cmd_Suspend instance;
	private Cmd_Suspend() {}
	public static Cmd_Suspend getInstance() {
		if(instance == null)
			instance = new Cmd_Suspend();
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

		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;

    	if(salesPanel.billPanel.getStatus() > DBConsts.SUSPENDED || salesPanel.billPanel.getStatus() < DBConsts.ORIGINAL) {
			return;
		}
    	if(JOptionPane.showConfirmDialog(BarFrame.instance, BarFrame.consts.confirmSuspend(), DlgConst.DlgTitle,
	                JOptionPane.YES_NO_OPTION) != 0) {
			return;
		}
    	
    	billPanel.unsavedItemsCheck();		//will send new added(not printed yet) dishes to kitchen. also save output in db
    	
		if(billPanel.getStatus() >= DBConsts.BILL_PRINTED) {
			billPanel.expireAndReGenerateBill(null);
		}
		//update bill 
    	DBUtil.getCursorString(rs,.updatePriceAndCreateTimeInBillRecord(salesPanel.billPanel, DBConsts.SUSPENDED, BarUtil.df.format(new Date()));
    	//update outputs
        try {
        	String tableID = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
        	//update outputs
			StringBuilder sql = new StringBuilder("update output set deleted = ").append(DBConsts.SUSPENDED)
	                .append(" where SUBJECT = '").append(tableID)
	                .append("' and time = '").append(BarFrame.instance.valOpenTime.getText())
	                .append("' and (deleted is null or deleted = ").append(DBConsts.ORIGINAL).append(")");
			PIMDBModel.getStatement().executeUpdate(sql.toString());
        }catch(Exception exp) {
        	ErrorUtil.write(exp);
        }
        //send to mev
        PrintService.printReceipt(billPanel, false, false, false);
        if(!BarOption.isCounterMode()) {
        	BarFrame.instance.actionAfterBillClosed();
        }
	}
}
