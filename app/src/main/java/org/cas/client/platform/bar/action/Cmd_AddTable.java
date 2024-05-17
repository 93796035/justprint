package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.dialog.TableDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.TablesPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.cascontrol.dialog.logindlg.LoginDlg;

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
	public void actionPerformed(ActionEvent e) {
		
		TablesPanel tablesPanel = (TablesPanel)BarFrame.instance.panels[0];
		
		if(!BarOption.isSingleUser()) {
			LoginDlg loginDlg = new LoginDlg(BarFrame.instance);
			loginDlg.noRefresh = true;
			loginDlg.setVisible(true);
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
		new TableDlg(null, null).setVisible(true);
		tablesPanel.initContent();
	}
}
