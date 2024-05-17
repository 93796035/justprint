package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.dialog.NumberPanelDlg;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.Dish;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillListPanel;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.uibeans.FunctionToggleButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.casutil.L;
import org.cas.client.platform.pimmodel.PIMDBModel;

public class Cmd_EqualBill implements SamActionListener {

	private static Cmd_EqualBill instance;
	private Cmd_EqualBill() {}
	public static Cmd_EqualBill getInstance() {
		if(instance == null)
			instance = new Cmd_EqualBill();
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
		FunctionToggleButton o = (FunctionToggleButton)e.getSource();
		//splite bill eually
		BillListPanel billListPanel = (BillListPanel)BarFrame.instance.panels[1];
		
		BillPanel panel = billListPanel.getCurBillPanel();
		if(panel == null) {
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.OnlyOneShouldBeSelected());
			o.setSelected(false);
			return;
		}
		BarFrame.numberPanelDlg.setBtnSource(o);
		BarFrame.numberPanelDlg.setFloatSupport(false);
		BarFrame.numberPanelDlg.setPercentSupport(false);
		BarFrame.numberPanelDlg.setModal(true);
		BarFrame.numberPanelDlg.reLayout();
		BarFrame.numberPanelDlg.setNotice(BarFrame.consts.QTYNOTICE());
		BarFrame.numberPanelDlg.setVisible(o.isSelected());
		if(NumberPanelDlg.confirmed) {
			int num = Integer.valueOf(NumberPanelDlg.curContent);
			if(num < 2) {
				JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
				return;
			}
			
			//if current billPanel is printed, then create a new one and expire it.
			if(billListPanel.getCurBillPanel().getStatus() >= DBConsts.BILL_PRINTED || billListPanel.getCurBillPanel().getStatus() < DBConsts.ORIGINAL) {
				billListPanel.getCurBillPanel().expireAndReGenerateBill(null);
			}
			
			//split into {num} bills. each dish's number and price will be divided by {num}.
			Dish.splitOutputList(panel.getOrderedDishAry(), num, null);//the third parameter is null, means update existing outputs
			//update existing bill.
			int curBillId = billListPanel.getCurBillPanel().getBillID();
			if(curBillId > 0) {
				try {
					StringBuilder sql = new StringBuilder("update bill set total = ").append(Math.round(Float.valueOf(panel.valTotlePrice.getText()) * 100)/num)
							.append(", discount = discount/").append(num)
							.append(", serviceFee = serviceFee/").append(num)
					.append(" where id = ").append(curBillId);
					PIMDBModel.getStatement().executeUpdate(sql.toString());
					
					panel.discount /= num;
					panel.serviceFee /= num;
				}catch(Exception exp) {
					L.e("SalesPanel", "unexpected error when updating the totalvalue of bill.", exp);
				}
			}
									
			for (int i = 1; i < num; i++) {				//generate output for splited ones.
				int billIndex = BillListPanel.getANewBillIdx(null, null);
				
				//generate a bill for each new occupied panel, incase there's discount info need to set into it.
				//@Note, when the initContent of the panel called, the bill ID will be set into the dish instance in memory.
				//and eventually, if the bill id is not 0, will calculate the service fee and discount into Total.
				int id = panel.cloneCurrentBillRecord(null,
						String.valueOf(billIndex),
						Math.round(Float.valueOf(panel.valTotlePrice.getText()) * 100/num));

				ArrayList<Dish> tDishAry = new ArrayList<Dish>();
				for (Dish dish : panel.getOrderedDishAry()) {
					tDishAry.add(dish.clone());
				}
				Dish.splitOutputList(tDishAry, num, String.valueOf(billIndex), id);
			}
		}
		billListPanel.initContent();
	}
}
