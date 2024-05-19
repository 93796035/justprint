package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.ui.panel.SalesPanel;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.statistics.CheckRuleDlg;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_SettingRule  implements SamActionListener {

	private static Cmd_SettingRule instance;
	private Cmd_SettingRule() {}
	public static Cmd_SettingRule getInstance() {
		if(instance == null)
			instance = new Cmd_SettingRule();
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
		
		CheckRuleDlg dlg = new CheckRuleDlg(BarFrame.instance);
		dlg.initContent();
		dlg.setVisible(true);
	}
}
