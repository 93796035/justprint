package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

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
	public void onClick(View o) {
//		new PayMethodDlg((BillListPanel) BarFrame.instance.panels[1]).show((FunctionButton)o);
	}
}
