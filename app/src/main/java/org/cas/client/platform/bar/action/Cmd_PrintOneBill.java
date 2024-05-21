package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.BillListPanel;

import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.ISButton;

import java.util.ArrayList;

public class Cmd_PrintOneBill implements SamActionListener {

	private static Cmd_PrintOneBill instance;
	private Cmd_PrintOneBill() {}
	public static Cmd_PrintOneBill getInstance() {
		if(instance == null)
			instance = new Cmd_PrintOneBill();
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
		
		if(!billListPanel.checkColosedBill()) {
			return;
		}

		//check if all bills are not closed, and find the first panel
		ArrayList<BillPanel> unclosedBillPanels = billListPanel.gatherAllUnclosedBillPanels();
		
		PrintService.printTemporaryBills(unclosedBillPanels);
		
		//combine bills--------------------------------------------------------------------------------------------
		billListPanel.combineBills(unclosedBillPanels, false);

	}
}
