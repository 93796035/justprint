package org.cas.client.platform.bar.action;

import android.database.Cursor;
import android.view.View;
import android.widget.Toast;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.web_srm.ValuePossible;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

public class Cmd_Return implements SamActionListener {

	private static Cmd_Return instance;
	private Cmd_Return() {}
	public static Cmd_Return getInstance() {
		if(instance == null)
			instance = new Cmd_Return();
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
    		JOptionPane.showConfirmDialog(null, BarFrame.consts.COMFIRMLOSTACTION(), org.cas.client.resources.international.DlgConst.DlgTitle, new JOptionPane.ConfirmDialogCallback() {
				@Override
				public void onResult(boolean confirmed) {
					if (confirmed) {
						Cmd_CancelAll.getInstance().onClick(v);
						if(BarOption.isCounterMode()) {
							BarFrame.instance.setVisible(false);
							BarFrame.singleUserLoginProcess();
							BarFrame.instance.ignoreLogin = true;
							BarFrame.instance.switchMode(2);
							BarFrame.instance.ignoreLogin = false;
						}else {	//for table mode
							//when bill saved(dish send to bach end), will do a check and as if waiter want to cancel the paid item.
							if(salesPanel.partialPaid && billPanel.getStatus() == DBConsts.ORIGINAL) {
								if(JOptionPane.showConfirmDialog(
										BarFrame.consts.COMFIRMCLEARMONEYRECEIVED(), org.cas.client.resources.international.DlgConst.DlgTitle)) {
									//check if the otehr receivement is not null, add back to gift card.
									//clean partial input
									StringBuilder sql = new StringBuilder("update bill set cashReceived = 0, debitReceived = 0, visaReceived = 0, masterReceived = 0");
									sql.append(" where _id = ").append(billPanel.getBillID());
									try {
										AikaPos.db.execSQL(sql.toString());
										salesPanel.partialPaid = false;
									}catch(Exception exp) {
										ErrorUtil.write(exp);
									}
								}
							}
							BarFrame.instance.switchMode(0);
						}
					}
				}
			});
		}
	}


	private void cleanTheLastUnsavedRecord() {
		if(BarFrame.instance.curPanelIdx == 3) { //if current is setting panel, no need to clean empty bill.
			return;
		}
		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		
		int billID = billPanel.getBillID();
    	String curBill = BarFrame.instance.getCurBillIndex();
    	
		try {
			//check if it's a "mistake-opening-table-action" or "adding bill action" by check if there's any output on it already. 
			//will be considered as non-empty as long as there's output connecting to the id, even the output is not currently displaying on this bill.
			StringBuilder sql = new StringBuilder("select * from output where category = '").append(billID)
					.append("' or (subject = '").append(BarFrame.instance.getCmbCurTable().getSelectedItem()).append("'")
					.append(" and time = '").append(BarFrame.instance.valOpenTime.getText()).append("'")
					.append(" and contactID = ").append(curBill).append(")"); 	//in future version, might need to check the deleted property.
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
		    if(rs.getCount() == 0) {			//if still empty
		    	//no related output, then don't record this bill in db at all.
		    	sql = new StringBuilder("delete from bill where _id = ").append(billID);
		    	AikaPos.db.execSQL(sql.toString());
		    }
		}catch(Exception exp) {
			L.e("counter mode returning... ", "error happend when deleting an empty bill with ID:"+ billID, exp);
		}		
	}
}
