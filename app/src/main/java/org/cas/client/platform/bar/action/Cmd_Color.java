package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_Color implements SamActionListener {

	private static Cmd_Color instance;
	private Cmd_Color() {}
	public static Cmd_Color getInstance() {
		if(instance == null)
			instance = new Cmd_Color();
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
//		new SettingColorDlg(BarFrame.instance).setVisible(true);
	}
}
