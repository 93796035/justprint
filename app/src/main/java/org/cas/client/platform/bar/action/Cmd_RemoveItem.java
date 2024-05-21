package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_RemoveItem implements SamActionListener {

	private static Cmd_RemoveItem instance;
	private Cmd_RemoveItem() {}
	public static Cmd_RemoveItem getInstance() {
		if(instance == null)
			instance = new Cmd_RemoveItem();
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
		if(!billPanel.notTooLateToChangeStatus()) {
			return;
		}
		billPanel.adjustBothUIandDB();
	}
}
