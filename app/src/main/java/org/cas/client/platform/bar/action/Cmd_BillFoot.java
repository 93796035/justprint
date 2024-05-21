package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

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
//		new BillFootDlg(BarFrame.instance, BarOption.getBillFootInfo()).setVisible(true);
	}
}
