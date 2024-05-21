package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_AddUser implements SamActionListener {

	private static Cmd_AddUser instance;
	private Cmd_AddUser() {}
	public static Cmd_AddUser getInstance() {
		if(instance == null)
			instance = new Cmd_AddUser();
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
		BarFrame.instance.addNewBillInCurTable();
	}
}
