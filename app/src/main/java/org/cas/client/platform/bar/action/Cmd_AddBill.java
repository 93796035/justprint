package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;
import java.util.Date;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.action.CreateNewOrderAction;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.pannel.TablesPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.bar.uibeans.TableButton;

public class Cmd_AddBill implements  SamActionListener {

	private static Cmd_AddBill instance;
	private Cmd_AddBill() {}
	public static Cmd_AddBill getInstance() {
		if(instance == null)
			instance = new Cmd_AddBill();
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
	public void actionPerformed(ActionEvent e) {
		TablesPanel tablesPanel = (TablesPanel)BarFrame.instance.panels[0];
		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		//save unsaved output
    	billPanel.unsavedItemsCheck();
    	DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.SAVED, BarUtil.df.format(new Date()));
    	if(BarOption.isCounterMode()){
    		//create a new table(which is considered as bill in counter mode).
    		int billIdx = BarOption.popKitchenBillIdx();
    		CreateNewOrderAction.makeSureTableExistsAndOpened(String.valueOf(billIdx), BarUtil.df.format(new Date()));
    		TableButton tableToggleButton = new TableButton();
    		tableToggleButton.setText(String.valueOf(billIdx));
    		tableToggleButton.setTableName(String.valueOf(billIdx));
    		ActionEvent evt = new ActionEvent(tableToggleButton, 0, null);
       	 	tablesPanel.actionPerformed(evt);
    	}else {
    		BarFrame.instance.addNewBillInCurTable();
    	}
	}
}
