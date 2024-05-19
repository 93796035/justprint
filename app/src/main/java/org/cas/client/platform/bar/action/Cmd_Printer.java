package org.cas.client.platform.bar.action;

import android.view.View;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.SettingPrinterDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_Printer implements SamActionListener {

	private static Cmd_Printer instance;
	private Cmd_Printer() {}
	public static Cmd_Printer getInstance() {
		if(instance == null)
			instance = new Cmd_Printer();
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
		new SettingPrinterDlg(BarFrame.instance).setVisible(true);
	}
}
