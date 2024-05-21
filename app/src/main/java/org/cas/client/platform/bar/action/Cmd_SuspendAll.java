package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.BillListPanel;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.ErrorUtil;

public class Cmd_SuspendAll implements  SamActionListener {

	private static Cmd_SuspendAll instance;
	private Cmd_SuspendAll() {}
	public static Cmd_SuspendAll getInstance() {
		if(instance == null)
			instance = new Cmd_SuspendAll();
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

		BillListPanel billListPanel = (BillListPanel) BarFrame.instance.panels[1];
		
		if(!billListPanel.checkColosedBill()) {
			return;
		}
		
        try {
        	String tableID = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
        	//update outputs
			StringBuilder sql = new StringBuilder("update output set deleted = ").append(DBConsts.SUSPENDED)
	                .append(" where SUBJECT = '").append(tableID)
	                .append("' and time = '").append(BarFrame.instance.valOpenTime.getText())
	                .append("' and (deleted is null or deleted = ").append(DBConsts.ORIGINAL).append(")");
			AikaPos.db.execSQL(sql.toString());
			
			//update bills
			sql = new StringBuilder("update bill set status = ").append(DBConsts.SUSPENDED)
					.append(" where openTime = '").append(BarFrame.instance.valOpenTime.getText())
					.append("' and (status is null or status = ").append(DBConsts.ORIGINAL).append(")");
			AikaPos.db.execSQL(sql.toString());
			
        	//update the tabel status
			BarFrame.instance.closeTableAndBillInDB(tableID, null);
        }catch(Exception exp) {
        	ErrorUtil.write(exp);
        }
        
		BarFrame.instance.setCurBillIdx("");
		BarFrame.instance.switchMode(0);
		
	}
}
