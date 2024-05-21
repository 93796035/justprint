package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public class UpdateItemPriceAction implements SamActionListener{

	ISButton btn;
	
	public UpdateItemPriceAction(ISButton button) {
		this.btn = button;
	}
	
	@Override
	public void onClick(View v) {
//		if(btn.isSelected()) {
//
//			SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
//			BillPanel billPanel = salesPanel.billPanel;
//
// 			try {
//             	int row = billPanel.table.getSelectedRow();
//         		Dish dish = billPanel.getOrderedDishAry().get(row);
//         		float price = 0;
// 				String curContent = BarFrame.numberPanelDlg.tfdQTY.getText().toString();
//            	if(curContent.endsWith("%")) {
//                	String tContent = curContent.substring(0, curContent.length() - 1);
//            		Float f = Float.valueOf(tContent);
//            		curContent = String.valueOf(f/100f);
//         			price = dish.getPrice() * Float.valueOf(curContent)/100f;
//            	}else {
//            		Float.valueOf(curContent);
//         			price = Float.valueOf(curContent);
//            	}
//
//             	dish.setPrice(Math.round(price * 100));
//             	dish.setTotalPrice((dish.getPrice() - dish.getDiscount()) * dish.getNum());
//
// 				int num = dish.getNum();
// 				int pK = num /(BarOption.MaxQTY * 100);
// 	    		if(num > BarOption.MaxQTY * 100) {
// 	    			num = num %(BarOption.MaxQTY * 100);
// 	    		}
// 	    		int pS = num /BarOption.MaxQTY;
// 	    		if(num > BarOption.MaxQTY) {
// 	    			num = num % BarOption.MaxQTY;
// 	    		}
//
// 	    		int priceDSP = dish.getPrice() * num;
// 	    		if(pS > 0)
// 	    			priceDSP /= pS;
// 	    		if(pK > 0)
// 	    			priceDSP /= pK;
//
//             	billPanel.table.setValueAt(BarOption.getMoneySign() + priceDSP/100f, row, 3);
//             	billPanel.updateTotalArea(false);
//             	int outputID = billPanel.getOrderedDishAry().get(row).getOutputID();
//             	if(outputID >= 0) {
//             		String sql = "update output set TOLTALPRICE = "
//             				+ (dish.getPrice() - dish.getDiscount()) + " where id = " + outputID;
//             		AikaPos.db.execSQL(sql);
//             	}
//
//         	}catch(Exception exp) {
//             	JOptionPane.showMessageDialog(BarFrame.numberPanelDlg, org.cas.client.resources.international.DlgConst.FORMATERROR);
//         		return;
//         	}
//// 	    	v.removeActionListener(this);
// 		}
	}

	@Override
	public void setSourceBtn(ISButton sourceBtn) {

	}
}
