package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.ui.panel.TablesPanel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.action.CreateNewOrderAction;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.TableButton;

import java.util.Date;

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
	public void onClick(View v) {
		TablesPanel tablesPanel = (TablesPanel) BarFrame.instance.panels[0];
		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		//save unsaved SALE_RECORD
    	billPanel.unsavedItemsCheck();
    	DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.SAVED, BarUtil.df.format(new Date()));
    	if(BarOption.isCounterMode()){
    		//create a new table(which is considered as bill in counter mode).
    		int billIdx = BarOption.popKitchenBillIdx();
    		CreateNewOrderAction.makeSureTableExistsAndOpened(String.valueOf(billIdx), BarUtil.df.format(new Date()));
    		TableButton tableToggleButton = new TableButton();
    		tableToggleButton.setText(String.valueOf(billIdx));
    		tableToggleButton.setTableName(String.valueOf(billIdx));
       	 	tablesPanel.onClick(tableToggleButton);
    	}else {
    		BarFrame.instance.addNewBillInCurTable();
    	}
	}
}
