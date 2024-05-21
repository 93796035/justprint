package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_CheckOrder implements SamActionListener {

	private static Cmd_CheckOrder instance;
	private Cmd_CheckOrder() {}
	public static Cmd_CheckOrder getInstance() {
		if(instance == null)
			instance = new Cmd_CheckOrder();
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
		
//    	String endNow = BarUtil.df.format(new Date());
//		int p = endNow.indexOf(" ");
//		String startTime = endNow.substring(0, p + 1) + BarOption.getStartTime();
//		BillingHistoryDlg dlg = new BillingHistoryDlg(BarFrame.instance);
//		dlg.initContent(startTime, endNow);
//		dlg.setVisible(true);
	}
}
