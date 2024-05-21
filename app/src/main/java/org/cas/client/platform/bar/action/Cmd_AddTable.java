package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.TablesPanel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_AddTable implements SamActionListener {

	private static Cmd_AddTable instance;
	private Cmd_AddTable() {}
	public static Cmd_AddTable getInstance() {
		if(instance == null)
			instance = new Cmd_AddTable();
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
		
		TablesPanel tablesPanel = (TablesPanel) BarFrame.instance.panels[0];
		
		if(!BarOption.isSingleUser()) {
			LoginDlg.getInstance().noRefresh = true;
			LoginDlg.getInstance().show(BarFrame.instance.getSupportFragmentManager(), "login_dialog");
            if (LoginDlg.PASSED == true) {
            	BarFrame.checkSignIn();
            	//@note: lowdown a little the level, to enable the admin do sales work.
            	if (LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS) {
            		 LoginDlg.setUSERTYPE(LoginDlg.USER_STATUS);
            	}
            }else {
            	return;
            }
		}
//		new TableDlg(null, null).show(BarFrame.instance.getSupportFragmentManager(), "table_dialog");
		tablesPanel.initContent();
	}
}
