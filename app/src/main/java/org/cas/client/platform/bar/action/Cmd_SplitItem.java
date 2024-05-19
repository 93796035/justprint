package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.model.Dish;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillListPanel;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.uibeans.FunctionToggleButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

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
	public void onClick(View v) {
		this.sourceBtn = (FunctionToggleButton)e.getSource();
		BillListPanel billListPanel = (BillListPanel)BarFrame.instance.panels[1];
		
		if(sourceBtn.isSelected()) {//select
			//check if there's one item selected.
			if(BarFrame.instance.curDish == null) {
				JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.OnlyOneShouldBeSelected());
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
				JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.NoBillSeleted());
				return;
			}
			Dish.splitOutput(BarFrame.instance.curDish, selectedPanels.size() + 1, null); // update the num and totalprice of curDish
			for (BillPanel billPanel : selectedPanels) { // insert new output with other billID
				int billIndex = Integer.valueOf(billPanel.billButton.getText());
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
				Dish.splitOutput(dish, selectedPanels.size() + 1, billPanel.billButton.getText());
			}

			BarFrame.instance.curDish = null;
			billListPanel.initContent();
		}
	}
}
