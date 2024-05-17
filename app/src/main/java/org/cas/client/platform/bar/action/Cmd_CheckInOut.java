package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_CheckInOut implements SamActionListener {

	private static Cmd_CheckInOut instance;
	private Cmd_CheckInOut() {}
	public static Cmd_CheckInOut getInstance() {
		if(instance == null)
			instance = new Cmd_CheckInOut();
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
		BarFrame.instance.userCheckOut();
	}
}
