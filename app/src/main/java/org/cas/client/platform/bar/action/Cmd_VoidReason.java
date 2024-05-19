package org.cas.client.platform.bar.action;

import android.view.View;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.ReasonDlg;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_VoidReason implements SamActionListener {

	private static Cmd_VoidReason instance;
	private Cmd_VoidReason() {}
	public static Cmd_VoidReason getInstance() {
		if(instance == null)
			instance = new Cmd_VoidReason();
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
		new ReasonDlg(true).setVisible(true);
	}
}
