package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_CombineAll implements SamActionListener {

	private static Cmd_CombineAll instance;
	private Cmd_CombineAll() {}
	public static Cmd_CombineAll getInstance() {
		if(instance == null)
			instance = new Cmd_CombineAll();
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
//		this.sourceBtn = (FunctionButton)e.getSource();
//		//@note should consider the time, incase there's some bill not paid before, while was calculated into current client.
//
//		BillListPanel billListPanel = (BillListPanel)BarFrame.instance.panels[1];
//
//		if(sourceBtn.getText().equals(BarFrame.consts.CombineAll())) {
//			if(!billListPanel.checkColosedBill()) {
//				return;
//			}
//
//			//check if all bills are not closed
//			ArrayList<BillPanel> unclosedBillPanels = billListPanel.gatherAllUnclosedBillPanels();
//			billListPanel.combineBills(unclosedBillPanels, unclosedBillPanels.size() == billListPanel.billPanels.size());
//		}
//
//		//for uncombine action.
//		else {
//			HashMap idxMap = new HashMap();
//			StringBuilder sql = new StringBuilder("select * from bill where tableName = '").append(BarFrame.instance.getCmbCurTable().getSelectedItem().toString()).append("'")
//					.append(" and openTime = '").append(BarFrame.instance.valOpenTime.getText()).append("'")
//					.append(" and (status is null or status < ").append(DBConsts.COMPLETED).append(")");
//			try {
//				Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//	            while (rs.moveToNext()) {
//	                idxMap.put(DBUtil.getCursorInt(rs,"_id"), DBUtil.getCursorInt(rs,"billIndex"));
//	            }
//			}catch(Exception exp) {
//				L.e("BillListPanel", "exception when change SALE_RECORD back to original bill" + sql, exp);
//			}
//
//			//get the unclosed billPane.
//			ArrayList<Dish> orderedDishes = new ArrayList<Dish>();
//			for (BillPanel billPanel : billListPanel.billPanels) {
//				if(billPanel.getStatus() < DBConsts.COMPLETED) {
//					orderedDishes = billPanel.getOrderedDishAry();
//					break;
//				}
//			}
//
//			for (Dish dish : orderedDishes) {
//				Object idx = idxMap.get(dish.getBillID());
//				if(idx != null) {
//					sql = new StringBuilder("update SALE_RECORD set userId = ").append(idx)
//							.append(" where _id = ").append(dish.getOutputID());
//					try {
//						PIMDBModel.getStatement().execute(sql.toString());
//					}catch(Exception exp) {
//						L.e("BillListPanel", "exception when change SALE_RECORD back to original bill" + sql, exp);
//					}
//				}
//			}
//			billListPanel.initContent();
//		}
	}
}
