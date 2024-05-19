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
import org.cas.client.platform.bar.pannel.TablesPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.pimmodel.PIMDBModel;
import org.cas.client.resource.international.DlgConst;

public class Cmd_Send implements SamActionListener {

	private static Cmd_Send instance;
	private Cmd_Send() {}
	public static Cmd_Send getInstance() {
		if(instance == null)
			instance = new Cmd_Send();
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
		
		billPanel.unsavedItemsCheck();
		DBUtil.getCursorString(rs,.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.SAVED, BarUtil.df.format(new Date()));
		
    	if(BarOption.isCounterMode()) {
    		if(!BarOption.stayAfterSave()) {
    			((TablesPanel)BarFrame.instance.panels[0]).switchToOtherTable();
    		}
	    }else {
	    	if(salesPanel.partialPaid && billPanel.getStatus() == DBConsts.ORIGINAL) {
    			if(JOptionPane.showConfirmDialog(BarFrame.instance, 
	    				BarFrame.consts.COMFIRMCLEARMONEYRECEIVED(), DlgConst.DlgTitle, JOptionPane.YES_NO_OPTION) == 0) {
	            	//clean partial input
	            	StringBuilder sql = new StringBuilder("update bill set cashReceived = 0, debitReceived = 0, visaReceived = 0, masterReceived = 0");
	            	sql.append(" where id = ").append(billPanel.getBillID());
	            	try {
	        			PIMDBModel.getStatement().executeUpdate(sql.toString());
	        			salesPanel.partialPaid = false;
	        		}catch(Exception exp) {
	        			ErrorUtil.write(exp);
	        		}
	            }
    		}
	    	BarFrame.instance.switchMode(0);
	    }
	}
}
