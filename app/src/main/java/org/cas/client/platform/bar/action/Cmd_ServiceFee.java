package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.dialog.NumberPanelDlg;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_ServiceFee implements SamActionListener {

	private static Cmd_ServiceFee instance;
	private Cmd_ServiceFee() {}
	public static Cmd_ServiceFee getInstance() {
		if(instance == null)
			instance = new Cmd_ServiceFee();
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
//		BarFrame.numberPanelDlg.setTitle(BarFrame.consts.ServiceFee());
// 		BarFrame.numberPanelDlg.setNotice(BarFrame.consts.ServiceFeeNotice());
		BarFrame.numberPanelDlg.setBtnSource(null);
 		BarFrame.numberPanelDlg.setFloatSupport(true);
// 		BarFrame.numberPanelDlg.setPercentSupport(true);
 		
// 		BarFrame.numberPanelDlg.setModal(true);
		BarFrame.numberPanelDlg.reLayout();
// 		BarFrame.numberPanelDlg.setVisible(true);

		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
 		try {
				String curContent = NumberPanelDlg.curContent;
				if(curContent == null || curContent.length() == 0) {
					return;
				}
				if(!billPanel.notTooLateToChangeStatus()) {
					return;
				}
				billPanel.serviceFee  = BarFrame.numberPanelDlg.isPercentage ? 
     				Math.round(Float.valueOf((billPanel.subTotal - billPanel.serviceFee + billPanel.discount) * Float.valueOf(curContent)))
     				: Math.round(Float.valueOf(curContent) * 100);

			String newComment = billPanel.getComment() + BillPanel.SERVICEFEE + billPanel.serviceFee + "@";
	     	DBUtil.updateBill(billPanel.getBillID(), "comment", newComment);
	     	billPanel.setComment(newComment);
     		billPanel.updateTotalArea(true);
     		
     	}catch(Exception exp) {
         	JOptionPane.showMessageDialog(org.cas.client.resources.international.DlgConst.FORMATERROR);
     		return;
     	}
		
	}
}
