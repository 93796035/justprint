package org.cas.client.platform.bar.action;

import android.view.View;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.dialog.BillFootDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_BillFoot implements  SamActionListener {

	private static Cmd_BillFoot instance;
	private Cmd_BillFoot() {}
	public static Cmd_BillFoot getInstance() {
		if(instance == null)
			instance = new Cmd_BillFoot();
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
		new BillFootDlg(BarFrame.instance, BarOption.getBillFootInfo()).setVisible(true);
	}
}
