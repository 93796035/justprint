package org.cas.client.platform.bar.action;

import android.view.View;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.dialog.CouponListDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;

public class Cmd_SettingCoupon implements SamActionListener {

	private static Cmd_SettingCoupon instance;
	private Cmd_SettingCoupon() {}
	public static Cmd_SettingCoupon getInstance() {
		if(instance == null)
			instance = new Cmd_SettingCoupon();
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
		new CouponListDlg(BarFrame.instance).setVisible(true);
	}
}
