package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.BillListPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_MoveItem implements SamActionListener {

	private static Cmd_MoveItem instance;
	private Cmd_MoveItem() {}
	public static Cmd_MoveItem getInstance() {
		if(instance == null)
			instance = new Cmd_MoveItem();
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
	public void onClick(View sourceBtn) {
		BillListPanel billListPanel = (BillListPanel)BarFrame.instance.panels[1];
		
		if(BarFrame.instance.curDish == null) {
			JOptionPane.showMessageDialog(BarFrame.consts.OnlyOneShouldBeSelected());
			sourceBtn.setSelected(false);
			return;
		}
		BillPanel panel = billListPanel.getCurBillPanel();
		if(panel == null) {
			JOptionPane.showMessageDialog(BarFrame.consts.OnlyOneShouldBeSelected());
			sourceBtn.setSelected(false);
			return;
		}
		if(!panel.notTooLateToChangeStatus())	//create a new bill for original bill
			return;
		moveItemAction((ISButton)sourceBtn);
	}
	
	private void moveItemAction(ISButton button) {
		//for popup a number dialog for user to input target bill#.
//		BarFrame.numberPanelDlg.setTitle(BarFrame.consts.BILL());
//		BarFrame.numberPanelDlg.setBtnSource(button);
//		BarFrame.numberPanelDlg.setFloatSupport(false);
//		BarFrame.numberPanelDlg.setPercentSupport(false);
//		BarFrame.numberPanelDlg.setModal(true);
//		BarFrame.numberPanelDlg.reLayout();
//		BarFrame.numberPanelDlg.setNotice(BarFrame.consts.QTYNOTICE());
//		BarFrame.numberPanelDlg.setVisible(button.isSelected());
//		//when number input dialog confirmed and input are valid, then do the real work.
//		if(NumberPanelDlg.confirmed) {
//			BillListPanel billListPanel = (BillListPanel)BarFrame.instance.panels[1];
//			int targetBillIdx = Integer.valueOf(NumberPanelDlg.curContent);
//			if(targetBillIdx < 1) {
//				JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
//			}else {
//				//check if the original panel need to be regenerate
//	        	BillPanel originalBillPanel = billListPanel.getBillPanelByBillNumber(Integer.valueOf(BarFrame.instance.curDish.getCustomerIndex()));
//	        	int origianlBillstatus = originalBillPanel.getStatus();	//save the status before it's changed when regenerate the bill.
//	        	if(origianlBillstatus >= DBConsts.BILL_PRINTED || origianlBillstatus < DBConsts.ORIGINAL) {
//	        		originalBillPanel.expireAndReGenerateBill(billListPanel.getBillPanelByBillNumber(targetBillIdx).billButton.getText());  //TODO:????why if original is printed, then regenerate a bill with target bill #?
//	        	}	//TODO:????should be same as originalBillPanel.reGenerate(targetBillIdx); no?
//
//				int billId = 0;
//				//check if the bill exist
//				StringBuilder sql = new StringBuilder("Select _id, status from Bill where billIndex = '").append(targetBillIdx)
//				.append("' and tableName = '").append(BarFrame.instance.getCmbCurTable().getSelectedItem().toString()).append("'")
//				.append(" and openTime = '").append(BarFrame.instance.valOpenTime.getText()).append("'")
//				.append(" and (status is null or status < ").append(DBConsts.EXPIRED).append(")");
//				try {
//					Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//		        	if(rs.moveToNext()) {
//		        		billId = DBUtil.getCursorInt(rs,"_id");
//		        		int status = DBUtil.getCursorInt(rs,"status");
//		        		if(status >= DBConsts.BILL_PRINTED || status < DBConsts.ORIGINAL) {
////	        				if (JOptionPane.showConfirmDialog(BarFrame.instance, BarFrame.consts.ConvertClosedBillBack(), BarFrame.consts.Operator(),
////	        			            JOptionPane.YES_NO_OPTION) != 0) {// are you sure to convert the voided bill back？
////	        			        return;
////	        				}else {
//	        					billListPanel.getBillPanelByBillNumber(targetBillIdx).expireAndReGenerateBill(billListPanel.getBillPanelByBillNumber(targetBillIdx).billButton.getText());
////	        				}
//		        		}
//		        	}else {
//		        		billId = BarFrame.instance.createAnEmptyBill(BarFrame.instance.getCmbCurTable().getSelectedItem().toString(),
//								BarFrame.instance.valOpenTime.getText().toString(), targetBillIdx, 1);
//		        	}
//
//		        	if(origianlBillstatus >= DBConsts.BILL_PRINTED || origianlBillstatus < DBConsts.ORIGINAL) {	//@NOTE: use the old one, new one might be changed when regenerate.
//		        		//find the current bill's status and add comment with the id and subtotal of original bill.
//		        		sql = new StringBuilder("update bill set comment = comment + '").append(PrintUtil.REF_TO).append(originalBillPanel.getBillID());
//		        		if(originalBillPanel.getStatus() == DBConsts.COMPLETED || originalBillPanel.getStatus() < DBConsts.ORIGINAL) {
//		        			sql.append("F");
//		        		}
//		        		sql.append(PrintUtil.DATE_REF).append(originalBillPanel.getZippedCreateTime());
//		        		sql.append(PrintUtil.OLD_SUBTOTAL).append(BarUtil.formatMoney(originalBillPanel.subTotal / 100.0))
//		        		.append(" where _id = ").append(billId);
//
//		        		try {
//				        	PIMDBModel.getStatement().executeUpdate(sql.toString());
//				        }catch(Exception exp) {
//				        	ErrorUtil.write(exp);
//				        }
//		        	}
//		        }catch(Exception exp) {
//		        	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
//		        	return;
//		        }
//
//				// the selected SALE_RECORD to this bill.
//				sql = new StringBuilder("update SALE_RECORD set userId = ").append(targetBillIdx)
//						.append(", billId = ").append(billId)
//						.append(" where _id = ").append(BarFrame.instance.curDish.getOutputID());
//		        try {
//		        	PIMDBModel.getStatement().executeUpdate(sql.toString());
//		        }catch(Exception exp) {
//		        	ErrorUtil.write(exp);
//		        }
//		        BarFrame.instance.curDish = null;
//		        billListPanel.initContent();
//		        //find the two bill, and update the total price in db.
////TODO: this is not the right way to find out the two relevant panel, since it's a little complex, let's leave to next version
////		        for (BillPanel billPanel : billPanels) {
////			        BarUtil.updateBillRecordPrices(billPanel);
////				}
//			}
//		}
	}
}
