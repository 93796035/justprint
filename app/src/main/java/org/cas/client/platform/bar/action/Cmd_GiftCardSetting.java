package org.cas.client.platform.bar.action;

import android.view.View;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.GiftCardListDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_GiftCardSetting implements SamActionListener {

	private static Cmd_GiftCardSetting instance;
	private Cmd_GiftCardSetting() {}
	public static Cmd_GiftCardSetting getInstance() {
		if(instance == null)
			instance = new Cmd_GiftCardSetting();
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
		new GiftCardListDlg(BarFrame.instance).setVisible(true);
	}
}
