package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public class UpdateItemDiscountAction implements SamActionListener{

	ISButton btn;
	
	public UpdateItemDiscountAction(ISButton button) {
		this.btn = button;
	}
	
	@Override
	public void onClick(View v) {
//		if(btn.isSelected()) {
//
//			SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
//			BillPanel billPanel = salesPanel.billPanel;
//
// 			try {
// 				String curContent = BarFrame.discountDlg.tfdQTY.getText();
//            	if(curContent.endsWith("%")) {
//            		BarFrame.discountDlg.isPercentage = true;	//NOTE：maybe ok's action not called yet, so the percentage flag not set yet.
//                	String tContent = curContent.substring(0, curContent.length() - 1);
//            		curContent = String.valueOf(Float.valueOf(tContent)/100f);
//            	}else {
//            		BarFrame.discountDlg.isPercentage = false;
//            		Float.valueOf(curContent);
//            	}
//
//             	int row = billPanel.table.getSelectedRow();
//             	//NOTE: current total price + existing discount = original total price.
//             	String strDisCount = (String)billPanel.table.getValueAt(row, 2);
//
//             	strDisCount = (strDisCount == null || strDisCount.length() < 2) ? "0" : BarUtil.getMoneyStrOut(strDisCount);//find and remove "-$"
//             	float oldDiscount = Float.valueOf(strDisCount);
//
//             	String strTotalPrice = (String)billPanel.table.getValueAt(row, 3);
//				strTotalPrice = strTotalPrice.trim().substring(1);
//             	float totalPrice = Float.valueOf(strTotalPrice);
//
//             	float newDiscount = 0;
// 				if(BarFrame.discountDlg.isPercentage) {
// 					newDiscount = (totalPrice + oldDiscount) * Float.valueOf(curContent);
// 				}else {
// 					newDiscount = Float.valueOf(curContent);
// 				}
//
// 				String qtStr = (String)billPanel.table.getValueAt(row, 0);
// 				qtStr = qtStr.toLowerCase().endsWith("x") ? qtStr.substring(0, qtStr.length() - 1) : qtStr;
// 				float qt = 1;
//				try {
//					int i = qtStr.indexOf("/");
//	 				if(i > 0) {
//						qt = Float.valueOf(qtStr.substring(0, i)) / Float.valueOf(qtStr.substring(i + 1));
//					}else {
//		 				qt = Integer.valueOf(qtStr);
//					}
//				}catch(Exception exp) {
// 					if(qtStr.length() > 0) {	//"" is OK, just leave qt = 1.
// 						JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.ActionNotAllowed());
// 						return;
// 					}
// 				}
// 				totalPrice = totalPrice/qt + oldDiscount - newDiscount;
// 				if(totalPrice < 0) {
// 					JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
// 					return;
// 				}
// 				String oldContent = (String)billPanel.table.getValueAt(row, 2);
// 				String newContent = "-"+ BarOption.getMoneySign() + BarUtil.formatMoney(newDiscount);
// 				int idx = oldContent.indexOf("-" + BarOption.getMoneySign());
// 				if(idx >= 0) {
// 					newContent = oldContent.substring(0, idx) + newContent;
// 				}else {
// 					newContent = oldContent + newContent;
// 				}
//             	billPanel.table.setValueAt(newContent, row, 2);
//
//             	billPanel.table.setValueAt(BarOption.getMoneySign() + BarUtil.formatMoney(totalPrice * qt), row, 3);
//             	billPanel.getOrderedDishAry().get(row).setDiscount(Math.round(newDiscount * 100));
//             	billPanel.getOrderedDishAry().get(row).setTotalPrice(Math.round((totalPrice * qt) * 100));
//
//             	billPanel.updateTotalArea(false);
//             	int outputID = billPanel.getOrderedDishAry().get(row).getOutputID();
//             	if(outputID >= 0) {
//             		String sql = "update output set discount = " + Math.round(newDiscount * 100) + ", toltalprice = " + Math.round(totalPrice * qt * 100) + " where _id = " + outputID;
//             		AikaPos.db.execSQL(sql);
//             	}
//         	}catch(Exception exp) {
//             	JOptionPane.showMessageDialog(BarFrame.numberPanelDlg, org.cas.client.resources.international.DlgConst.FORMATERROR);
//         		return;
//         	}
////        	v.setOnClickListener(this);
 		}

	@Override
	public void setSourceBtn(ISButton sourceBtn) {

	}
}
