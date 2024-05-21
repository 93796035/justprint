package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.dialog.NumberPanelDlg;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_ChangePrice implements SamActionListener {
	
	private static Cmd_ChangePrice instance = null;
	private Cmd_ChangePrice() {}
	public static Cmd_ChangePrice getInstance() {
		if(instance == null) {
			instance = new Cmd_ChangePrice();
		}
		return instance;
	}

	private ISButton sourceBtn;
	public ISButton getSourceBtn() {
		return sourceBtn;
	}
	
	@Override
	public void onClick(View sourceBtn) {

		if(BarFrame.instance.curDish == null) {//check if there's an item selected.
  			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.OnlyOneShouldBeSelected());
  			return;
  		}
    	if(!BarOption.isWaiterAllowedToChangePrice()) {
    		if (!BarFrame.instance.adminAuthentication()) {
    			JOptionPane.showMessageDialog(BarFrame.instance, org.cas.client.resources.international.DlgConst.InvalidPassword);
    			Cmd_ChangePrice.getInstance().getSourceBtn().setSelected(false);
				return;
    		}
    	}
    	
    	//to allow store owner to apply 50% more convienient, we use discount dialog instead of the numberDialog.  //showPriceChangeDlg();
// 		BarFrame.discountDlg.setTitle(BarFrame.consts.CHANGEPRICE());
// 		BarFrame.discountDlg.setNotice(BarFrame.consts.ChangePriceNotice());
// 		BarFrame.discountDlg.setBtnSource(null);
// 		BarFrame.discountDlg.setFloatSupport(true);
// 		BarFrame.discountDlg.setPercentSupport(true);
// 		BarFrame.discountDlg.setComboBtnVisible(false);
// 		BarFrame.discountDlg.setModal(true);
// 		BarFrame.discountDlg.setVisible(true);
 		
 		sourceBtn.setSelected(false);
 		
 		try {
			String curContent = BarFrame.discountDlg.curContent;
			if(curContent == null || curContent.length() == 0)
				return;
			
			changePrice(curContent, BarFrame.discountDlg.isPercentage);
     		
     	}catch(Exception exp) {
         	JOptionPane.showMessageDialog(BarFrame.discountDlg, org.cas.client.resources.international.DlgConst.FORMATERROR);
     		return;
     	}
	}
	
	public void showPriceChangeDlg() {
		NumberPanelDlg numberPanelDlg = BarFrame.numberPanelDlg;
//		numberPanelDlg.setTitle(BarFrame.consts.CHANGEPRICE());
//		numberPanelDlg.setNotice(BarFrame.consts.ChangePriceNotice());
		numberPanelDlg.setBtnSource(sourceBtn);//pomp up a numberPanelDlg
		numberPanelDlg.setFloatSupport(true);
//		numberPanelDlg.setPercentSupport(false);
//		numberPanelDlg.setModal(false);
		
		if(sourceBtn == null) {		//when added a "change price dish" onto the billPanel frome menu panel, will show dlg with null as sourcebtn.
//			numberPanelDlg.setVisible(true);
		}else {
			sourceBtn.setSelected(true);
//			numberPanelDlg.setVisible(sourceBtn.isSelected());	//@NOTE: it's not model mode.
		}
//		numberPanelDlg.setAction(new UpdateItemPriceAction(sourceBtn));
	}

	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}
	
	public void changePrice(String curContent, boolean isPercentage) {

//		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
//		BillPanel billPanel = salesPanel.billPanel;
//
//		try {
//         	int row = billPanel.table.getSelectedRow();
//     		Dish dish = billPanel.getOrderedDishAry().get(row);
//     		float price = 0;
//        	if(isPercentage) {
//     			price = dish.getPrice() * Float.valueOf(curContent)/100f;
//        	}else {
//        		Float.valueOf(curContent);
//     			price = Float.valueOf(curContent);
//        	}
//
//         	dish.setPrice(Math.round(price * 100));
//         	dish.setTotalPrice((dish.getPrice() - dish.getDiscount()) * dish.getNum());
//
//			int num = dish.getNum();
//			int pK = num /(BarOption.MaxQTY * 100);
//    		if(num > BarOption.MaxQTY * 100) {
//    			num = num %(BarOption.MaxQTY * 100);
//    		}
//    		int pS = num /BarOption.MaxQTY;
//    		if(num > BarOption.MaxQTY) {
//    			num = num % BarOption.MaxQTY;
//    		}
//
//    		int priceDSP = dish.getPrice() * num;
//    		if(pS > 0)
//    			priceDSP /= pS;
//    		if(pK > 0)
//    			priceDSP /= pK;
//
//         	billPanel.table.setValueAt(BarOption.getMoneySign() + priceDSP/100f, row, 3);
//         	billPanel.updateTotalArea(false);
//         	int outputID = billPanel.getOrderedDishAry().get(row).getOutputID();
//         	if(outputID >= 0) {
//         		StringBuilder sql = new StringBuilder("update output set TOLTALPRICE = ").append(dish.getPrice() - dish.getDiscount())
//         				.append(", deleted = ").append(DBConsts.ITEM_PRICE_CHANGED)
//         				.append(", arrearage = ").append((int)(System.currentTimeMillis() / 1000)) //this will cause the bill considered as revised when printing bill.
//         				.append(" where id = ").append(outputID);
//
//         		PIMDBModel.getStatement().executeUpdate(sql.toString());
//         	}
//
//     	}catch(Exception exp) {
//         	JOptionPane.showMessageDialog(BarFrame.discountDlg, DlgConst.FORMATERROR);
//     		return;
//     	}
	}

}
