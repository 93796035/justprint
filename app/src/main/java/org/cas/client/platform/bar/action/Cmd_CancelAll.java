package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;

import java.util.ArrayList;

public class Cmd_CancelAll implements SamActionListener {

	private static Cmd_CancelAll instance;
	private Cmd_CancelAll() {}
	public static Cmd_CancelAll getInstance() {
		if(instance == null)
			instance = new Cmd_CancelAll();
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
		// cancel all---- if bill is empty, then check if table is empty, if yes, close current table. yes or not, all back to table view.
		SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		
		ArrayList<Dish> orderedDishAry = billPanel.getOrderedDishAry();
		if (orderedDishAry.size() > 0) { // if not empty, remove all new added items.
			int notBilledDishQT = billPanel.getNotBilledDishes().size();

			if(notBilledDishQT == 0) {
				JOptionPane.showMessageDialog(BarFrame.consts.ActionNotAllowed());
				return;
			}
			
			int lastBilledRow = orderedDishAry.size() - notBilledDishQT - 1;

			// update array first.
			ArrayList<Dish> sendedDishes = new ArrayList<>();
			for (int i = orderedDishAry.size() - 1; i > lastBilledRow; i--) {
				Dish dish = orderedDishAry.get(i);
				if(dish.getOutputID() > 0) {
					DBUtil.changeOutputStatusInDB(dish, DBConsts.DELETED);
					sendedDishes.add(dish);
				}
				orderedDishAry.remove(i);
			}
			if(sendedDishes.size() > 0) {
				//not printed while saved dishes, notice kitchen stop to prepare.
				billPanel.sendDishesToKitchen(sendedDishes, true);
				//may be still need to print internal used bill, because boss might wonder why dish prepared?
		        //PrintService.printTemporaryBill(billPanel, sendedDishes, false);
			}
			// update the table view
//			int tColCount = billPanel.table.getColumnCount();
//			int tValidRowCount = orderedDishAry.size(); // get the used RowCount
//			Object[][] tValues = new Object[tValidRowCount][tColCount];
//			for (int r = 0; r < tValidRowCount; r++) {
//				for (int c = 0; c < tColCount; c++)
//					tValues[r][c] = billPanel.table.getValueAt(r, c);
//			}
//			billPanel.table.setDataVector(tValues, billPanel.header);
//			billPanel.resetColWidth();
//
//			if (BarFrame.secondScreen != null) {
//				BarFrame.customerFrame.billPanel.resetColWidth();
//			}
//			billPanel.table.setSelectedRow(tValues.length - 1);
			billPanel.discount = 0;
			billPanel.updateTotalArea(true);
		}else {
			//if there's no record in current bill, then call actionAfterBillClosed to close the bill, and also close the table if not other bill left.
			BarFrame.instance.actionAfterBillClosed();
		}
	}
}
