package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;
import com.just.print.ui.panel.BillListPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;

import java.util.List;

public class Cmd_SplitItem implements SamActionListener {

	private static Cmd_SplitItem instance;
	private Cmd_SplitItem() {}
	public static Cmd_SplitItem getInstance() {
		if(instance == null)
			instance = new Cmd_SplitItem();
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
	public void onClick(View sourceBtn) {
		BillListPanel billListPanel = (BillListPanel)BarFrame.instance.panels[1];
		
		if(sourceBtn.isSelected()) {//select
			//check if there's one item selected.
			if(BarFrame.instance.curDish == null) {
				JOptionPane.showMessageDialog(BarFrame.consts.OnlyOneShouldBeSelected());
				sourceBtn.setSelected(false);
			}
		}else {
		
			// unselect: if here reached, there must be curDish.
			// remove the bill where the curDish is. @because sometimes that bill might be
			// unselected.
			List<BillPanel> selectedPanels = billListPanel.getSelectedBillPannels();
			for (BillPanel billPanel : selectedPanels) { // remove the original panel from the list.
				if (billPanel.billButton.getText().equals(BarFrame.instance.curDish.getCustomerIndex())) {
					selectedPanels.remove(billPanel);
					break;
				}
			}
			if(selectedPanels.size() == 0) {
				JOptionPane.showMessageDialog(BarFrame.consts.NoBillSeleted());
				return;
			}
			DBUtil.splitOutput(BarFrame.instance.curDish, selectedPanels.size() + 1, null); // update the num and totalprice of curDish
			for (BillPanel billPanel : selectedPanels) { // insert new SALE_RECORD with other billID
				int billIndex = Integer.parseInt(billPanel.billButton.getText());
				//generate a bill for each new occupied panel, incase there's discount info need to set into it.
				//@Note, when the initContent of the panel called, the bill ID will be set into the dish instance in memory.
				//and eventually, if the bill id is not 0, will calculate the service fee and discount into Total.
				if(billPanel.getBillID() == 0) {
					int id = BarFrame.instance.createAnEmptyBill(
							BarFrame.instance.getCmbCurTable().getSelectedItem().toString(),
							BarFrame.instance.valOpenTime.getText().toString(),
							billIndex, 1);
					billPanel.setBillID(id);
				}
				Dish dish = BarFrame.instance.curDish.clone();
				dish.setBillID(billPanel.getBillID());
				DBUtil.splitOutput(dish, selectedPanels.size() + 1, billPanel.billButton.getText());
			}

			BarFrame.instance.curDish = null;
			billListPanel.initContent();
		}
	}
}
