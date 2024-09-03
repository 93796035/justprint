package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_Pay implements SamActionListener {

	private static Cmd_Pay instance;

	private Cmd_Pay() {
	}

	public static Cmd_Pay getInstance() {
		if (instance == null)
			instance = new Cmd_Pay();
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

		BillPanel billPanel = ((SalesPanel) BarFrame.instance.panels[2]).billPanel;

		//if it's processing a closed bill, then
		if (billPanel.getStatus() >= DBConsts.COMPLETED) {
			Cmd_OtherPay.getInstance().onClick(v);
			return;
		}

		if (!BarUtil.routineCheckBeforePrintingReceipt()) {
			return;
		}

		String title = "";
		String txtOnButton = ((FunctionButton) v).getText();
		if (txtOnButton.equals(BarFrame.consts.CASH())) {
			title = BarFrame.consts.EnterCashPayment();
		} else if (txtOnButton.equals(BarFrame.consts.DEBIT())) {
			title = BarFrame.consts.EnterDebitPayment();
		} else if (txtOnButton.equals(BarFrame.consts.VISA()) || txtOnButton.equals(BarFrame.consts.CREDIT())) {
			title = BarFrame.consts.EnterVisaPayment();
		} else if (txtOnButton.equals(BarFrame.consts.MASTER())) {
			title = BarFrame.consts.EnterMasterPayment();
		} else {
			return;
		}

		BarFrame.payDlg.initContent(title, billPanel.getBillID());
		BarFrame.payDlg.show(BarFrame.instance.getSupportFragmentManager(), BarFrame.payDlg.getTitle());
	}
}
