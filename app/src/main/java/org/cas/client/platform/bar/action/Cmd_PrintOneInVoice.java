package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.PayMethodDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillListPanel;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_PrintOneInVoice implements SamActionListener {

	private static Cmd_PrintOneInVoice instance;
	private Cmd_PrintOneInVoice() {}
	public static Cmd_PrintOneInVoice getInstance() {
		if(instance == null)
			instance = new Cmd_PrintOneInVoice();
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
		Object o = e.getSource();
		new PayMethodDlg((BillListPanel)BarFrame.instance.panels[1]).show((FunctionButton)o);
	}
}
