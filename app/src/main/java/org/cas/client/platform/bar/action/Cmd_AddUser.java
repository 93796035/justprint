package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

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
	public void actionPerformed(ActionEvent e) {
		BarFrame.instance.addNewBillInCurTable();
	}
}
