package org.cas.client.platform.bar.action;

import android.database.Cursor;
import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.BillListPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.TableButton;

import java.util.ArrayList;
import java.util.List;

public class Cmd_ChangeTable implements SamActionListener {

	private static Cmd_ChangeTable instance;
	private Cmd_ChangeTable() {}
	public static Cmd_ChangeTable getInstance() {
		if(instance == null)
			instance = new Cmd_ChangeTable();
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
	
	public static List<TableButton> selectedTables = new ArrayList<TableButton>();
	@Override
	public void onClick(View v) {

		if(selectedTables.size() == 1){
			JOptionPane.showMessageDialog(BarFrame.consts.AtLeastTwoTableShouldBeSelected());
			selectedTables = new ArrayList<TableButton>();
			BarFrame.instance.switchMode(0);
		}else if(selectedTables.size() == 2) {
			if(selectedTables.get(0).getBackground() == null) {
				JOptionPane.showMessageDialog(BarFrame.consts.nothingToMove());
				selectedTables = new ArrayList<TableButton>();
				return;
			}
			try {
				StringBuilder sql = new StringBuilder("select id from bill where tableId = '").append(selectedTables.get(0).getTableName()).append("'")
						.append(" and opentime = '").append(selectedTables.get(0).getOpenTime()).append("'")
						.append(" and (status is null or status < ").append(DBConsts.COMPLETED)
						.append(" and status >= ").append(DBConsts.ORIGINAL).append(")");
				Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);

				ArrayList<Integer> billIDs = new ArrayList<Integer>();

				while(rs.moveToNext()) {
					billIDs.add(DBUtil.getCursorInt(rs, "_id"));
				}
				if(billIDs.size() == 0) {
					JOptionPane.showMessageDialog(BarFrame.consts.nothingToMove());
					selectedTables = new ArrayList<TableButton>();
					return;
				}

	            //if new table's startus is 0, then set start time with the old table's open time. otherwise, it's a join to not switching to table,
				//in this case, we should update the bill and output's time to the opentime of the target table.
	            //be noticed that when table closed, we make sure the opentime is removed. it's not because it will be used for check status, it's for order site in future.
				int targetBillIdx;
				String openTime;
				TableButton targetTableBtn = selectedTables.get(1);
				if(targetTableBtn.getStatus() == DBConsts.ORIGINAL) {	//if the target is not openned.
					targetBillIdx = 1;
					openTime = selectedTables.get(0).getOpenTime(); //the startTime is opentime here, because only table mode has change table.
					DBUtil.updateTableStatusInDB(targetTableBtn.getTableName(), openTime);
					targetTableBtn.setOpenTime(openTime);
				}else {											//if the target table is already opened
					//then should get a new billIdx number in target bill (even the existing bill on target table is an empty bill, we still create new one.)
					targetBillIdx = BillListPanel.getANewBillIdx(targetTableBtn.getTableName(), targetTableBtn.getOpenTime());
					openTime = targetTableBtn.getOpenTime();
				}
				
	            for(int i = 0; i < billIDs.size(); i++) {
					DBUtil.moveBillToAnotherTable(targetTableBtn.getTableName(), targetBillIdx, billIDs.get(i), openTime);
	            }
			}catch(Exception exp) {
				L.e("Cmd_changeTAble", "exception happend when fetching billID by table name" + selectedTables.get(0).getTableName(), exp);
			}
			
           	DBUtil.closeTableAndBillInDB(selectedTables.get(0).getTableName(), selectedTables.get(0).getOpenTime());
			selectedTables = new ArrayList<TableButton>();

           	BarFrame.instance.switchMode(0);//maybe need this to refresh the display of the tables
			
		}else if(selectedTables.size() > 2) {
			JOptionPane.showMessageDialog(BarFrame.consts.AtLeastTwoTableShouldBeSelected());
			for (TableButton tableButton : selectedTables) {
				tableButton.setAlpha(1.0f);
				tableButton.setBackground(null);
			}
			selectedTables = new ArrayList<TableButton>();
		}
	}
}
