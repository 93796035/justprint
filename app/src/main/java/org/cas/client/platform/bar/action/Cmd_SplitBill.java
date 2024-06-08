package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.ErrorUtil;

import java.util.List;

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
	public void onClick(View v) {
		SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		if(billPanel.getOrderedDishAry().size() == 0) {
			JOptionPane.showMessageDialog(BarFrame.consts.ActionNotAllowed());
			return;
		}
		List<Dish> newDishes = billPanel.getNewDishes();
		if(newDishes.size() > 0) {
			for (Dish dish : newDishes) {
				if(dish.getPrinterIds() != null && dish.getPrinterIds().length() > 1) {		//if has been set a printer, then means the new added dish need to be print first.
        			JOptionPane.showMessageDialog(BarFrame.consts.UnSendRecordFound());
        			return;
				}
			}
		}
		
		billPanel.unsavedItemsCheck();	//print
		DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.SAVED, null);		//save ??shall we update the time (the third parameter)?
		
		if(salesPanel.partialPaid && billPanel.getStatus() == DBConsts.ORIGINAL) {
			if(JOptionPane.showConfirmDialog(
    				BarFrame.consts.COMFIRMCLEARMONEYRECEIVED(), org.cas.client.resources.international.DlgConst.DlgTitle)) {
            	//clean partial input
            	StringBuilder sql = new StringBuilder("update bill set cashReceived = 0, debitReceived = 0, visaReceived = 0, masterReceived = 0");
            	sql.append(" where id = ").append(billPanel.getBillID());
            	try {
        			AikaPos.db.execSQL(sql.toString());
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
