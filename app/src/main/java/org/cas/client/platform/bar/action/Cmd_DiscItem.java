package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_DiscItem implements SamActionListener {
	private static Cmd_DiscItem instance;
	private Cmd_DiscItem() {}
	public static Cmd_DiscItem getInstance() {
		if(instance == null)
			instance = new Cmd_DiscItem();
		return instance;
	}
	
	ISButton sourceBtn;
	
	public ISButton getSourceBtn() {
		return sourceBtn;
	}
	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}
	
	@Override
	public void onClick(View sourceBtn) {
		if(BarFrame.instance.curDish == null) {//check if there's an item selected.
 			JOptionPane.showMessageDialog(BarFrame.consts.OnlyOneShouldBeSelected());
 			return;
 		}
 		
 		if(!BarOption.isWaiterAllowedToDiscount()) {
    		if (!BarFrame.instance.adminAuthentication()) {
    			JOptionPane.showMessageDialog(org.cas.client.resources.international.DlgConst.InvalidPassword);
    			Cmd_DiscItem.getInstance().getSourceBtn().setSelected(false);
				return;
    		}
    	}
 		
// 		BarFrame.discountDlg.setTitle(BarFrame.consts.DISCITEM());
// 		BarFrame.discountDlg.setNotice(BarFrame.consts.DISC_ITEMNotice());
// 		BarFrame.discountDlg.setBtnSource(sourceBtn);//pomp up a discountDlg
// 		BarFrame.discountDlg.setFloatSupport(true);
// 		BarFrame.discountDlg.setPercentSupport(true);
// 		BarFrame.discountDlg.setComboBtnVisible(false);
// 		BarFrame.discountDlg.setModal(false);
// 		//should no record selected, select the last one.
// 		BarFrame.discountDlg.setVisible(sourceBtn.isSelected());	//@NOTE: it's not model mode.
// 		BarFrame.discountDlg.setAction(new UpdateItemDiscountAction(sourceBtn));
 		
	}
}
