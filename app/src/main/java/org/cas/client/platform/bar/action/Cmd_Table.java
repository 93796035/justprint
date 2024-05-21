package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_Table implements SamActionListener {

	private static Cmd_Table instance;
	private Cmd_Table() {}
	public static Cmd_Table getInstance() {
		if(instance == null)
			instance = new Cmd_Table();
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
//		new TabbleSettingDlg().setVisible(true);
	}
}
