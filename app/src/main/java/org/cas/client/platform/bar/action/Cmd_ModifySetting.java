package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.modifyDish.AddModificationDlg;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_ModifySetting implements SamActionListener {

	private static Cmd_ModifySetting instance;
	private Cmd_ModifySetting() {}
	public static Cmd_ModifySetting getInstance() {
		if(instance == null)
			instance = new Cmd_ModifySetting();
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
	public void actionPerformed(ActionEvent e) {
		AddModificationDlg.getInstance().isSettingMode = true;;
		AddModificationDlg.getInstance().initContent("", 0);
		AddModificationDlg.getInstance().setVisible(true);
	}
}
