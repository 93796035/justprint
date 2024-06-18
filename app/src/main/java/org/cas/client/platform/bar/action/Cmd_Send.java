package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.ErrorUtil;

import java.util.Date;

public class Cmd_Send implements SamActionListener {

	private static Cmd_Send instance;
	private Cmd_Send() {}
	public static Cmd_Send getInstance() {
		if(instance == null)
			instance = new Cmd_Send();
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
		
		billPanel.unsavedItemsCheck();
		DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.SAVED, BarUtil.df.format(new Date()));
		
    	if(BarOption.isCounterMode()) {
    		if(!BarOption.stayAfterSave()) {
//    			((TablesPanel)BarFrame.instance.panels[0]).switchToOtherTable();
    		}
	    }else {
	    	if(salesPanel.partialPaid && billPanel.getStatus() == DBConsts.ORIGINAL) {
    			JOptionPane.showConfirmDialog(
	    			BarFrame.consts.COMFIRMCLEARMONEYRECEIVED(),
					org.cas.client.resources.international.DlgConst.DlgTitle,
					new JOptionPane.ConfirmDialogCallback() {
						@Override
						public void onResult(boolean confirmed) {
							if (confirmed) {
								//clean partial input
								StringBuilder sql = new StringBuilder("update bill set cashReceived = 0, debitReceived = 0, visaReceived = 0, masterReceived = 0");
								sql.append(" where _id = ").append(billPanel.getBillID());
								try {
									AikaPos.db.execSQL(sql.toString());
									salesPanel.partialPaid = false;
								} catch (Exception exp) {
									ErrorUtil.write(exp);
								}
							}
						}
					});
    		}
			BarFrame.instance.switchMode(0);
	    }
	}
}
