package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.resource.international.DlgConst;

public class Cmd_DiscBill implements SamActionListener {

	private static Cmd_DiscBill instance;
	private Cmd_DiscBill() {}
	public static Cmd_DiscBill getInstance() {
		if(instance == null)
			instance = new Cmd_DiscBill();
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
		
		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;

		if(!BarOption.isWaiterAllowedToDiscount()) {
    		if (!BarFrame.instance.adminAuthentication()) {
    			JOptionPane.showMessageDialog(BarFrame.instance, DlgConst.InvalidPassword);
    			Cmd_DiscBill.getInstance().getSourceBtn().setSelected(false);
				return;
    		}
    	}
    	
 		BarFrame.discountDlg.setTitle(BarFrame.consts.DISCOUNT_BILL());
 		BarFrame.discountDlg.setNotice(BarFrame.consts.VolumnDiscountNotice());
 		BarFrame.discountDlg.setBtnSource(null);
 		BarFrame.discountDlg.setFloatSupport(true);
 		BarFrame.discountDlg.setPercentSupport(true);
 		BarFrame.discountDlg.setComboBtnVisible(true);
 		BarFrame.discountDlg.setModal(true);
 		BarFrame.discountDlg.setVisible(true);
 		
 		try {
			String curContent = BarFrame.discountDlg.curContent;
			if(curContent == null || curContent.length() == 0)
				return;
			float originalSubtotal = billPanel.subTotal + billPanel.discount - billPanel.serviceFee;
			float discount = BarFrame.discountDlg.isPercentage ? originalSubtotal * (Float.valueOf(curContent) / 100f)
					: Float.valueOf(curContent);

			discount = Math.round(discount * 100);
			
			if(!billPanel.notTooLateToChangeStatus()) {
				return;
			}
			
			billPanel.discount = discount > originalSubtotal  ? originalSubtotal : discount;
			String newComment = billPanel.getComment() + BillPanel.DISCBILL + Math.round(billPanel.discount) + "@";
			DBUtil.updateBill(billPanel.getBillID(), "comment", newComment);
			billPanel.setComment(newComment);
			billPanel.updateTotalArea(true);
			
     	}catch(Exception exp) {
         	JOptionPane.showMessageDialog(BarFrame.discountDlg, DlgConst.FORMATERROR);
     		return;
     	}
	}
}
