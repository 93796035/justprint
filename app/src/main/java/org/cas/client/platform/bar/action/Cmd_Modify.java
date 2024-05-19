package org.cas.client.platform.bar.action;

import android.view.View;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.dialog.modifyDish.AddModificationDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillListPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_Modify implements SamActionListener {

	private static Cmd_Modify instance;
	private Cmd_Modify() {}
	public static Cmd_Modify getInstance() {
		if(instance == null)
			instance = new Cmd_Modify();
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
		if(BarFrame.instance.curDish == null) {
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.AtLeastOneShouldBeSelected());
			return;
		}
		//if already send, then do not allow to modify the marks.
		if(BarFrame.instance.curDish.getOutputID() > 0) {
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.SendItemCanNotModify());
			return;
		}
		
		//if there's a curDish?
		if(BarFrame.instance.curDish == null) {//check if there's an item selected.
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.OnlyOneShouldBeSelected());
			return;
		}

		AddModificationDlg.getInstance().isSettingMode = false;
		AddModificationDlg.getInstance().initContent(BarFrame.instance.curDish.getModification(), 0);
		AddModificationDlg.getInstance().setVisible(true);
	}
}
