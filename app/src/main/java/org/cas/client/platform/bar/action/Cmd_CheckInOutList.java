package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_CheckInOutList implements SamActionListener {

	private static Cmd_CheckInOutList instance;
	private Cmd_CheckInOutList() {}
	public static Cmd_CheckInOutList getInstance() {
		if(instance == null)
			instance = new Cmd_CheckInOutList();
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
//		new CheckInOutListDlg(BarFrame.instance).setVisible(true);
	}
}
