package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_QTY implements SamActionListener {

	private static Cmd_QTY instance;
	private Cmd_QTY() {}
	public static Cmd_QTY getInstance() {
		if(instance == null)
			instance = new Cmd_QTY();
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
		
// 		BarFrame.numberPanelDlg.setTitle(BarFrame.consts.QTY());
// 		BarFrame.numberPanelDlg.setNotice(BarFrame.consts.QTYNOTICE());
//		BarFrame.instance.numberPanelDlg.setBtnSource(sourceBtn);//pomp up a numberPanelDlg
// 		BarFrame.numberPanelDlg.setFloatSupport(false);
// 		BarFrame.numberPanelDlg.setPercentSupport(false);
// 		BarFrame.numberPanelDlg.setModal(true);
// 		BarFrame.instance.numberPanelDlg.reLayout();
//		//should no record selected, select the last one.
//		BarFrame.instance.numberPanelDlg.setVisible(sourceBtn.isSelected());	//@NOTE: it's not model mode.
		
		if(sourceBtn.isSelected()) {
			try {
				String curContent = BarFrame.instance.numberPanelDlg.curContent;
        		int tQTY = Integer.valueOf(curContent);
            	int row = billPanel.table.getSelectedRow();
            	billPanel.table.setValueAt(curContent + "x", row, 0);
            	billPanel.getOrderedDishAry().get(row).setNum(tQTY);
            	billPanel.updateTotalArea(true);
        	}catch(Exception exp) {
            	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.FORMATERROR());
        		return;
        	}
		}
		if(billPanel.table.getSelectedRow() < 0) {
			billPanel.table.setSelectedRow(billPanel.table.getRowCount()-1);
		}
		//present the value in number dialog.
		Object obj = billPanel.table.getValueAt(billPanel.table.getSelectedRow(), 3);
		BarFrame.instance.numberPanelDlg.setContents(obj.toString());
	}
}
