package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.model.Dish;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.pimmodel.PIMDBModel;
import org.cas.client.resource.international.DlgConst;

public class Cmd_SplitBill implements  SamActionListener {

	private static Cmd_SplitBill instance;
	private Cmd_SplitBill() {}
	public static Cmd_SplitBill getInstance() {
		if(instance == null)
			instance = new Cmd_SplitBill();
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
		if(billPanel.getOrderedDishAry().size() == 0) {
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.ActionNotAllowed());
			return;
		}
		List<Dish> newDishes = billPanel.getNewDishes();
		if(newDishes.size() > 0) {
			for (Dish dish : newDishes) {
				if(dish.getPrinter() != null && dish.getPrinter().length() > 1) {		//if has been set a printer, then means the new added dish need to be print first.
        			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.UnSendRecordFound());
        			return;
				}
			}
		}
		
		billPanel.unsavedItemsCheck();	//print
		DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.SAVED, null);		//save ??shall we update the time (the third parameter)?
		
		if(salesPanel.partialPaid && billPanel.getStatus() == DBConsts.ORIGINAL) {
			if(JOptionPane.showConfirmDialog(BarFrame.instance, 
    				BarFrame.consts.COMFIRMCLEARMONEYRECEIVED(), DlgConst.DlgTitle, JOptionPane.YES_NO_OPTION) == 0) {
            	//clean partial input
            	StringBuilder sql = new StringBuilder("update bill set cashReceived = 0, debitReceived = 0, visaReceived = 0, masterReceived = 0");
            	sql.append(" where id = ").append(billPanel.getBillID());
            	try {
        			PIMDBModel.getStatement().executeUpdate(sql.toString());
        			salesPanel.partialPaid = false;
        		}catch(Exception exp) {
        			ErrorUtil.write(exp);
        		}
            }else {
            	return;
            }
		}
		BarFrame.instance.switchMode(1);
		
	}

}
