package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_Pay implements SamActionListener {

	private static Cmd_Pay instance;
	private Cmd_Pay() {}
	public static Cmd_Pay getInstance() {
		if(instance == null)
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
	public void actionPerformed(ActionEvent e) {

		BillPanel billPanel = ((SalesPanel)BarFrame.instance.panels[2]).billPanel;
    	
		//if it's processing a closed bill, then
    	if(billPanel.getStatus() >= DBConsts.COMPLETED) {
    		Cmd_OtherPay.getInstance().actionPerformed(e);
    		return;
    	}
    	
		if(!BarUtil.routineCheckBeforePrintingReceipt()) {
			return;
		}
		//check if the pay dialog is already visible, if yes, then update bill received values.
		if(BarFrame.payDlg.isVisible()) {
			BarFrame.payDlg.updateBill(billPanel.getBillID());
		}
		//show dialog-------------------------------------
 		BarFrame.payDlg.setFloatSupport(true);

		String txtOnButton = ((FunctionButton)e.getSource()).getText();
 		if(txtOnButton.equals(BarFrame.consts.CASH())) {
 			BarFrame.payDlg.setTitle(BarFrame.consts.EnterCashPayment());
 		}else if(txtOnButton.equals(BarFrame.consts.DEBIT())) {
 			BarFrame.payDlg.setTitle(BarFrame.consts.EnterDebitPayment());
 		}else if(txtOnButton.equals(BarFrame.consts.VISA()) || txtOnButton.equals(BarFrame.consts.CREDIT())) {
 			BarFrame.payDlg.setTitle(BarFrame.consts.EnterVisaPayment());
 		}else if(txtOnButton.equals(BarFrame.consts.MASTER())) {
 			BarFrame.payDlg.setTitle(BarFrame.consts.EnterMasterPayment());
 		}else {// if(txtOnButton.equals(BarFrame.consts.GIFTCARD()) || txtOnButton.equals(BarFrame.consts.OTHER())) {	//might be cheque, gift cards.....
 			return;
 		}
 		//init payDialog content base on bill.
 		BarFrame.payDlg.initContent(billPanel);
 		BarFrame.payDlg.setVisible(true);
	}
}
