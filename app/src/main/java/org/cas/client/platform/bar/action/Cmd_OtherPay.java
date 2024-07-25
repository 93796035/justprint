package org.cas.client.platform.bar.action;

import android.database.Cursor;
import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.PayDlg;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.ErrorUtil;

import java.util.Date;

public class Cmd_OtherPay implements SamActionListener {

	private static Cmd_OtherPay instance;
	private Cmd_OtherPay() {}
	public static Cmd_OtherPay getInstance() {
		if(instance == null)
			instance = new Cmd_OtherPay();
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

		if(!BarUtil.routineCheckBeforePrintingReceipt()) {
			return;
		}

    	BillPanel billPanel = ((SalesPanel) BarFrame.instance.panels[2]).billPanel;
    	
		
		if(billPanel.getStatus() >= DBConsts.BILL_PRINTED) { // if already printed, means there's record in web-mev alredy, make sure refs are right.
			billPanel.expireAndReGenerateBill(null);
		}
		//if it's processing a closed bill, then
    	if(billPanel.getStatus() >= DBConsts.COMPLETED) {
     		//send a refund to mev secretly.
    		PrintService.printRefund(billPanel, - Math.round(billPanel.totalOnBill), BarFrame.instance.getCurBillIndex(), true);
    	}
    	
		int billId = billPanel.getBillID();
		int oldTotal = 0;
		StringBuilder sb = new StringBuilder("select * from bill where _id = " + billId);
    	try {
    		Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
            rs.moveToNext();
            oldTotal = DBUtil.getCursorInt(rs,"total");
    	}catch(Exception exp) {
    		ErrorUtil.write(exp);
    	}
    	
		int billOldStatus = PayDlg.getBillStatus(billId);
    	boolean needToBePrinted = billOldStatus != DBConsts.BILL_PRINTED || !BarOption.isSavePrintInvoiceWhenBilled();
    	
		//do the exact button same style updating, but set the cash column with the total and set the other column with the pay mothod(Uber ski).
		try {
   			//NOTE: should consider might been paid with card, so the new received should be the left not the total..
			//we need to update the createdTime with current time, because the openTime could be a time before last batch, this bill was not calculatd into that batch, because it
			//not finished yet, might add more dish...so the bill must modify the createdTime to be calculated into next batch.
			String paidTime = BarUtil.df.format(new Date());
   	    	StringBuilder sql = new StringBuilder("update bill set cashReceived = ").append(oldTotal)
   		   	    	.append(", DEBITRECEIVED = 0, VISARECEIVED = 0, MASTERRECEIVED = 0")
   	    		.append(", createdTime = '").append(paidTime)
	   	    	.append("', otherReceived = '").append(((FunctionButton)v).getText())
	   	    	.append("', STATUS = ").append(DBConsts.COMPLETED)
	   	    	.append(", userId = ").append(LoginDlg.USERID)
	   	    	.append(" where _id = ").append(billId);
			AikaPos.db.execSQL(sql.toString());
			//update the status of relevant outputs.
			sql = new StringBuilder("update SALE_RECORD set STATUS = ").append(DBConsts.COMPLETED)
					.append(" where openTime = '").append(BarFrame.instance.valOpenTime.getText()).append("'")
					.append(" and tableName = '").append(BarFrame.instance.getCmbCurTable().getSelectedItem()).append("'")
					.append(" and userId = ").append(BarFrame.instance.getCurBillIndex());
			if(billPanel.getStatus() >= DBConsts.COMPLETED) {
				sql.append(" and STATUS = ").append(DBConsts.COMPLETED);
			}else {
				sql.append(" and ( STATUS is null or STATUS = ").append(DBConsts.ORIGINAL).append(")");
			}
			AikaPos.db.execSQL(sql.toString());
   		}catch(Exception exp) {
			ErrorUtil.write(exp);
			BarFrame.setStatusMes("Not payed succesfully, please choose other pay for now and notice the technician to check log.");
			return;
		}
		
    	PrintService.printReceipt(billPanel, false, true, needToBePrinted);
    	
    	BarFrame.instance.actionAfterBillClosed();
	}

}
