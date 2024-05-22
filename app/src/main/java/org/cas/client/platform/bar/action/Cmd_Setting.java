package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_Setting implements SamActionListener {

	private static Cmd_Setting instance;
	private Cmd_Setting() {}
	public static Cmd_Setting getInstance() {
		if(instance == null)
			instance = new Cmd_Setting();
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
		
		if(billPanel.getNewDishes().size() > 0) {
    		if(JOptionPane.showConfirmDialog(
    				BarFrame.consts.COMFIRMLOSTACTION(), org.cas.client.resources.international.DlgConst.DlgTitle)) {
                return;	
            }else {
    			//call cancel all
//    			Cmd_CancelAll.getInstance().actionPerformed(null);
            }
    	}
		BarFrame.instance.switchMode(3);
	}
}
