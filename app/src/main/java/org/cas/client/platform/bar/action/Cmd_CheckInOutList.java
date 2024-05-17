package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.statistics.CheckInOutListDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

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
	public void actionPerformed(ActionEvent e) {
		new CheckInOutListDlg(BarFrame.instance).setVisible(true);
	}
}
