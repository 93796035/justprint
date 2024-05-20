package org.cas.client.platform.bar.action;

import android.database.Cursor;
import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.db.bean.Dish;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.model.Dish;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillListPanel;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.casutil.L;
import org.cas.client.platform.pimmodel.PIMDBModel;

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
		this.sourceBtn = (FunctionButton)e.getSource();
		//@note should consider the time, incase there's some bill not paid before, while was calculated into current client.

		BillListPanel billListPanel = (BillListPanel)BarFrame.instance.panels[1];
		
		if(sourceBtn.getText().equals(BarFrame.consts.CombineAll())) {
			if(!billListPanel.checkColosedBill()) {
				return;
			}
			
			//check if all bills are not closed
			ArrayList<BillPanel> unclosedBillPanels = billListPanel.gatherAllUnclosedBillPanels();
			billListPanel.combineBills(unclosedBillPanels, unclosedBillPanels.size() == billListPanel.billPanels.size());
		}
		
		//for uncombine action.
		else {
			HashMap idxMap = new HashMap();
			StringBuilder sql = new StringBuilder("select * from bill where tableId = '").append(BarFrame.instance.getCmbCurTable().getSelectedItem().toString()).append("'")
					.append(" and opentime = '").append(BarFrame.instance.valOpenTime.getText()).append("'")
					.append(" and (status is null or status < ").append(DBConsts.COMPLETED).append(")");
			try {
				Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
	            while (rs.moveToNext()) {
	                idxMap.put(DBUtil.getCursorInt(rs,"id"), DBUtil.getCursorInt(rs,"billIndex"));
	            }
			}catch(Exception exp) {
				L.e("BillListPanel", "exception when change output back to original bill" + sql, exp);
			}
			
			//get the unclosed billPane.
			ArrayList<Dish> orderedDishes = new ArrayList<Dish>();
			for (BillPanel billPanel : billListPanel.billPanels) {
				if(billPanel.getStatus() < DBConsts.COMPLETED) {
					orderedDishes = billPanel.getOrderedDishAry();
					break;
				}
			}
			
			for (Dish dish : orderedDishes) {
				Object idx = idxMap.get(dish.getBillID());
				if(idx != null) {
					sql = new StringBuilder("update output set contactID = ").append(idx)
							.append(" where id = ").append(dish.getOutputID());
					try {
						PIMDBModel.getStatement().execute(sql.toString());
					}catch(Exception exp) {
						L.e("BillListPanel", "exception when change output back to original bill" + sql, exp);
					}
				}
			}
			billListPanel.initContent();
		}
	}
}
