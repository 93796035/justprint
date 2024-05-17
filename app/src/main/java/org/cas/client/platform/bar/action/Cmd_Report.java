package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.dialog.statistics.ReportDlg;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.cascontrol.dialog.logindlg.LoginDlg;
import org.cas.client.platform.pimmodel.PIMDBModel;

public class Cmd_Report implements SamActionListener {

	private static Cmd_Report instance;
	private Cmd_Report() {}
	public static Cmd_Report getInstance() {
		if(instance == null)
			instance = new Cmd_Report();
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
		if(!BarOption.isSingleUser()) {
			LoginDlg loginDlg = new LoginDlg(BarFrame.instance);
			loginDlg.noRefresh = true;
			loginDlg.setVisible(true);
	        if (LoginDlg.PASSED == true) {
	    		ReportDlg dlg = new ReportDlg(BarFrame.instance);
	    		dlg.setVisible(true);
	        }
		}else {
			if(BarOption.isNeedLoginBeforeReport()) {
				LoginDlg loginDlg = new LoginDlg(BarFrame.instance);
				loginDlg.noRefresh = true;
				loginDlg.setVisible(true);
	            if (LoginDlg.PASSED != true) {
	            	return;
	            }

    			if(LoginDlg.getUSERTYPE() == LoginDlg.SUPER_ADMIN) {
    				StringBuilder sql = new StringBuilder("select * from systeminfo order by id desc limit 1");
    				try {//get the relevant print Time base on batchID and the previous batchID
    					ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
    		            rs.next();
    		            String endTime = rs.getString("version");
    		            sql = new StringBuilder("select * from Bill where opentime > '").append(endTime).append("'");
    		            rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
    		            rs.afterLast();
    		            rs.relative(-1);
    		            int tmpPos = rs.getRow();
    		            if(tmpPos > 0) {
    		            	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.CloseBatchFirst());
    		            	return;
    		            }
    				}catch (Exception exp) {
						return;
					}
    			}
    			
            	BarFrame.checkSignIn();
	    	}
	            
			ReportDlg dlg = new ReportDlg(BarFrame.instance);
    		dlg.setVisible(true);
		}
	}
}
