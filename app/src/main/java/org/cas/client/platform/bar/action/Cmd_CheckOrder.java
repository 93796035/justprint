package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.statistics.BillingHistoryDlg;
import org.cas.client.platform.bar.uibeans.ISButton;

import java.util.Date;

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
		BillingHistoryDlg dlg = new BillingHistoryDlg();
		dlg.show(BarFrame.instance.getSupportFragmentManager(), "Order History");
	}
}
