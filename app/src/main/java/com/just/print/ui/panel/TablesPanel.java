package com.just.print.ui.panel;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.expand.DaoExpand;

import com.just.print.ui.fragment.LoginDlg;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.BackGroundTimerThread;
import org.cas.client.platform.bar.net.action.CreateNewOrderAction;
import org.cas.client.platform.casutil.L;
import com.stupid.method.reflect.StupidReflect;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.action.Cmd_CancelAll;
import org.cas.client.platform.bar.action.Cmd_ChangeTable;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.TableButton;
import org.cas.client.platform.cascustomize.CustOpts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class TablesPanel extends PicturePane implements View.OnClickListener, View.OnTouchListener{

    private static final String TAG = "TableFragment";
//    RelativeLayout  layout;
    public static ArrayList<TableButton> btnTables = new ArrayList<TableButton>();

    private FunctionButton btnSwitchToTableMode;
    private TableButton btnPressed;
    public TableButton btnLogo;
    private boolean isDragged;
    private int xGap, yGap;

    public static String startTime;

    //for print
    public static String SUCCESS = "0";
    public static String ERROR = "2";

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initContent();
        //this thread will start a request thread every 10 seconds to fetch new order from server..
        //also will trigger a refresh of tableivew for the waiting time.
        new BackGroundTimerThread().start();
        return mRoot;
    }
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tables_panel;
    }

    private int convertDpToPixel(int dp, Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
    private static TablesPanel instance = null;
    public static TablesPanel getInstance(){
        if(instance == null){
            instance = new TablesPanel();
        }
        return instance;
    }

    public void initContent() {
        if(BarOption.isCounterMode()) {
            btnSwitchToTableMode.setVisible(true);
        }
        //clean existing btns
        for (int i = btnTables.size() - 1; i >=0; i--) {
            TableButton tableToggleButton = btnTables.get(i);
            btnTables.remove(i);
            if(mRoot != null){
                ((RelativeLayout)mRoot).removeView(tableToggleButton);
            }
        }
        if(btnLogo != null) {
            if(mRoot != null){
                ((RelativeLayout)mRoot).removeView(btnLogo);
            }
        }

        try {
            // load all the tables ---------------------------
            //have to fetch the bill by outputs, because there's some table might have no output one it, it exist just in case user want to undo the split......
            StringBuilder sql = new StringBuilder("select t._id, t.openTime, t.STATUS as s1, e.nickName, b.createdTime, b.TOTAL, t.posX, t.posY, t.WIDTH, t.HEIGHT, t.displayIdx, t.TYPE, b.status as s2, t.name")
                    .append(" from dining_Table as t left join bill as b on t.name = b.tableName and t.opentime = b.opentime")
                    .append(" and (b.status is null or b.status < ").append(DBConsts.COMPLETED).append(")")
                    .append(" left join USER as e on b.userId = e._id");
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            int yPos = 0;
            HashSet<Integer> tableIds = new HashSet<Integer>();
            while (rs.moveToNext()) {
                TableButton tableToggleButton = new TableButton();

                int id = DBUtil.getCursorInt(rs, "_id");
                int type = DBUtil.getCursorInt(rs, "TYPE");

                //check if it's already created and added into tableAry.
                //because a table contains split bills, then there will be more than one records, and the total should counts the total on all the bills.
                if(tableIds.contains(id)) {
                    tableToggleButton = getTableButtonByName(DBUtil.getCursorString(rs, "name"));
                    tableToggleButton.setTotal(tableToggleButton.getTotal() + DBUtil.getCursorInt(rs, "total"));

                    tableToggleButton.setTypeStatusAndName(
                            DBUtil.getCursorString(rs, "openTime"),
                            DBUtil.getCursorString(rs, "createdTime"),
                            type,
                            DBUtil.getCursorInt(rs, "s2") > tableToggleButton.getStatus() ? DBUtil.getCursorInt(rs, "s2") : tableToggleButton.getStatus(), //make sure the status of table are updated to the biggest status of the bill.
                            DBUtil.getCursorString(rs, "Name"));
                    //THIS ROUND STOP HERE!!!!!!!!
                    continue;
                }else {
                    tableToggleButton.setTotal(DBUtil.getCursorInt(rs, "total"));
                }

                //check if it's the logo table.
                if(type == DBConsts.LOGO_TYPE) {
                    btnLogo = tableToggleButton;
// will show logo in something like screen saver in future.           		if(CustOpts.custOps.getValue("TablePanelBK") == null) { //if using default bg, there is our logo, so no need to show up our movable logo table.
                    continue;
//            		}
                }

                tableIds.add(id);
                tableToggleButton.setId(id);				//ID

                tableToggleButton.setStatus(DBUtil.getCursorInt(rs, "s1"));
                tableToggleButton.setWaiterName(DBUtil.getCursorString(rs, "nickName"));

                //for table mode, setTypeStatusAndName should go after setting bounds, because depends on the status, it might change the size.
                RelativeLayout.LayoutParams params = tableToggleButton.setTypeStatusAndName(
                        DBUtil.getCursorString(rs, "openTime"),
                        DBUtil.getCursorString(rs, "createTime"),
                        type,
                        DBUtil.getCursorInt(rs, "s2"),
                        DBUtil.getCursorString(rs, "Name"));

                //location
                int x = DBUtil.getCursorInt(rs, "posX");
                int y = DBUtil.getCursorInt(rs, "posY");
                int w = DBUtil.getCursorInt(rs, "width");
                int h = DBUtil.getCursorInt(rs, "height");
//                if(BarOption.isCounterMode()) {
//                    if(tableToggleButton.getStatus() == 0){
//                        x = -100;
//                        y = 0;
//                        w = 1;
//                        h = 0;
//                    }else {
//                        x = 0;
//                        y = yPos;
//                        //getWidth(), 60);
//                        yPos += 69;
//                    }
//                }else {
//                    //@Note:used the width of Barframe, because the width of tablesPane might be changed to thin style in counter mode.
//                    if(x > BarFrame.instance.getWidth() - (type == DBConsts.LOGO_TYPE ? 133 : 99)) {
//                        x = getWidth() - (type == DBConsts.LOGO_TYPE ? 133 : 99);
//                    }
//                    if(x < 0) {
//                        x = 0;
//                    }
//                    int cmdBtnHeight = (BarFrame.instance.getHeight() - 80) / 10;
//                    if(y > getHeight() - (type == DBConsts.LOGO_TYPE ? 200 + cmdBtnHeight : 160)) {
//                        y =  getHeight() - (type == DBConsts.LOGO_TYPE ? 200 + cmdBtnHeight : 160);
//                    }
//                    if(y < 0) {
//                        y = 0;
//                    }
//                    if(w <= 10) {	//doesn't matter, because will be set Rkagain in setTypeStatusAndName method.
//                        w = 69;
//                    }
//                    if(h <= 10) {
//                        h = 69;
//                    }
//                    tableToggleButton.setBounds(x, y, w, h);
//                }
                params.leftMargin = convertDpToPixel(x, BarFrame.instance);
                params.topMargin = convertDpToPixel(y, BarFrame.instance);

                btnTables.add(tableToggleButton);
                if(mRoot != null) {
                    ((RelativeLayout) mRoot).addView(tableToggleButton, params);
                }
                //displayIdx is used to check if it's a temporal added table button or the related client(employee) id if it's a table start with "D"(delivery type).
                if(DBUtil.getCursorInt(rs, "displayIdx") < 0) {
                    tableToggleButton.setOnTouchListener(this);
//                    tableToggleButton.addMouseMotionListener(this);
                }
                tableToggleButton.setOnClickListener(this);
            }

//            if(btnLogo == null) {
//            	sql = new StringBuilder("INSERT INTO DINING_TABLE (name, displayIdx, posX, posY, width, height, type) VALUES ('")
//    					.append("logo").append("', -1, ")
//    					.append(CustOpts.HOR_GAP).append(", ")
//    					.append(CustOpts.VER_GAP).append(", 133, 200, ")
//    					.append(DBConsts.LOGO_TYPE).append(")");
//            	PIMDBModel.getStatement().executeUpdate(sql.toString());
//            }
            //if cmbCurTable not updated, update the model of the table combobox on BarFrame.
            if(BarFrame.instance.getCurTable().length() == 0) {
                initCmbCurTable();
            }
        }catch(Exception e) {
            L.e("init tables", "Unexpected exception when init the tables from db.", e);
        }
    }

    private void initCmbCurTable() {
//        int p = 0;
//        String[] tableNames = new String[BarOption.isCounterMode() ? btnTables.size() : btnTables.size() + 1];
//        if(!BarOption.isCounterMode()) {
//            tableNames[0] = "";
//            p++;
//        }
//
//        for (int i = 0; i < btnTables.size(); i++) {
//            tableNames[p] = btnTables.get(i).getTableName();
//            p++;
//        }
//
//        BarFrame.instance.getCmbCurTable().setModel(new DefaultComboBoxModel<String>(tableNames));
//        if(BarOption.isCounterMode()) {
//            TableButton tableBtn = btnTables.get(BarFrame.instance.getCmbCurTable().getSelectedIndex());
//            if(tableBtn != null) {
//                BarFrame.instance.valOpenTime.setText(tableBtn.getOpenTime());
//            }
//        }

        BarFrame.instance.populateSpinnerWithTables(DaoExpand.queryAllTables(AikaPos.daoSession.getDiningTableDao()));
    }

    public TableButton getTableButtonByName(String newTable) {
        for (TableButton tableButton : btnTables) {
            if(tableButton.getTableName().equals(newTable)) {
                return tableButton;
            }
        }

        return null;
    }
    @Override
    public void onClick(View o) {
        // table buttons---------------------------------------------------------------------------------
        if (o instanceof TableButton) {
            if(isDragged) {
                return;	//do nothing.
            }
            if(Cmd_ChangeTable.getInstance().getSourceBtn().isSelected()) {
                if(Cmd_ChangeTable.getInstance().selectedTables.contains(o)) {
                    Cmd_ChangeTable.getInstance().selectedTables.remove(o);
                    o.setBackground(null);
                }else {
                    Cmd_ChangeTable.getInstance().selectedTables.add((TableButton)o);
                    o.setAlpha(0.0f);
                    o.setBackgroundColor(Color.parseColor(BarOption.getOnselectBC()));
                }
                return;
            }
            //if it's switching from other table, need to check if there's unsaved dishes. (this is for counter mode?)
            SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
            BillPanel billPanel = salesPanel.billPanel;
            if(billPanel.getNewDishes().size() > 0) {
                if(JOptionPane.showConfirmDialog(BarFrame.consts.COMFIRMLOSTACTION(), "Warning")) {
                    return;
                }else {
                    Cmd_CancelAll.getInstance().onClick(null);
                }
            }

            TableButton tableToggle = (TableButton) o;

            if(!BarOption.isSingleUser()) {	//if it's multi user, then login every time open table.
                LoginDlg.getInstance().show(getActivity().getSupportFragmentManager(), BarFrame.consts.Login());
                if (LoginDlg.PASSED == true) {
                    BarFrame.checkSignIn();
                    //@note: lowdown a little the level, to enable the admin do sales work.
                    if (LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS) {
                        LoginDlg.setUSERTYPE(LoginDlg.USER_STATUS);
                    }
                }else {
                    return;
                }
            }

            //this must be called after the login dialog, otherwise, it will be wiped off.
            //and also need to be reset after swithMode, because switchMode(0) will call tablesPane.initcontent, inside, will reinit cmbCurTable's model.
            BarFrame.instance.ignoreItemChange = true;
            BarFrame.instance.cmbCurTable.setSelection(btnTables.indexOf(tableToggle));
            //if before is not selected, then update the status
            if(tableToggle.getStatus() == 0){
                tableToggle.open();
            }
            //update ui's time field.
            BarFrame.instance.valOpenTime.setText(tableToggle.getOpenTime());

            try {
                StringBuilder sql = new StringBuilder("select * from bill where tableId = '").append(tableToggle.getTableName()).append("'")
                        .append(" and opentime = '").append(tableToggle.getOpenTime()).append("'")
                        .append(" and (status is null or status < ").append(DBConsts.COMPLETED)
                        .append(" and status >= ").append(DBConsts.ORIGINAL).append(")");
                Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
                int num = rs.getCount();

                //if it's table mode, will not be empty anymore, because whenever click a table, we will create a bill for it.
                if (num == 0) {// if it's empty, means it's fastfood mode, and the previous bill was deleted (should not happen though). switch to sales panel
                    L.w("Warning:", "when open table, found no related bill in a open status table", null);
                    tableToggle.open();
                    BarFrame.instance.valOpenTime.setText(tableToggle.getOpenTime());
                    BarFrame.instance.setCurBillIdx("");
                    BarFrame.instance.switchMode(2);
                } else { // if it's not empty
                    if(num == 1 && CustOpts.custOps.getValue("FrobiddenQuickEnter") == null) {	// display the only bill
                        BarFrame.instance.setCurBillIdx(DBUtil.getCursorString(rs, "customerNumber"));
                        BarFrame.instance.setCurBillID(DBUtil.getCursorInt(rs, "id"));
                        BarFrame.instance.hideBillIdx(true);
                        BarFrame.instance.switchMode(2);
                    }else { //or switch to the bill panel to show all the bills.
                        BarFrame.instance.hideBillIdx(false);
                        BarFrame.instance.switchMode(1);
                    }
                }
                tableToggle.setSelected(true);
                BarFrame.instance.ignoreItemChange = true;
                BarFrame.instance.cmbCurTable.setSelection(btnTables.indexOf(tableToggle));
            } catch (Exception exp) {
                L.e("TablesPanel", "Exception when table button clicked", exp);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 处理手指按下事件
                break;
            case MotionEvent.ACTION_MOVE:
                // 处理手指滑动事件
                break;
            case MotionEvent.ACTION_UP:
                // 处理手指抬起事件
                break;
        }
        return true;
    }

    public void switchToOtherTable() {
        if(BarOption.unclosedBillFirst()) {
            TableButton tblBtn = locateAnUnclosedTable();
            if(tblBtn != null) {
                onClick(tblBtn);
            }else {
                locateAEmptyTable();
            }
        }else {			//if no, create a new table wich next billIdx(will be table id)
            locateAEmptyTable();
        }
    }

    private TableButton locateAnUnclosedTable() {
        ArrayList<TableButton> btnTables = TablesPanel.btnTables;
        String curTable = (String)BarFrame.instance.getCmbCurTable().getSelectedItem();
        TableButton tblBtn = null;
        for (int i = 0; i < TablesPanel.btnTables.size(); i++) {
            if(btnTables.get(i).getStatus() > DBConsts.ORIGINAL && !btnTables.get(i).getText().equals(curTable)) {
                tblBtn = btnTables.get(i);
                break;
            }
        }
        return tblBtn;
    }

    private void locateAEmptyTable() {
        TableButton tblBtn = null;
        //check if there's an empty bill for each table.
        for (int i = 0; i < TablesPanel.btnTables.size(); i++) {
            if(btnTables.get(i).getStatus() == DBConsts.OPENNED ) {
                String opentime = btnTables.get(i).getOpenTime();
                String tableName = btnTables.get(i).getTableName();
                StringBuilder sql = new StringBuilder("select * from Bill where status = 0 and tableId = '")
                        .append(tableName).append("' and opentime = '").append(opentime).append("'");
                try {
                    Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
                    int tmpPos = rs.getCount();
                    if(tmpPos == 1) {	//if there's no this coupon number in database, then warning and return.
                        tblBtn = btnTables.get(i);
                        break;
                    }
                }catch(Exception exp) {
                    L.e("TablesPane", "exception happend when locateAEmptyTable: " + sql, exp);
                }
            }
        }

        if(tblBtn != null) {
            onClick(tblBtn);
        }else {
            createNew();
        }
    }

    private void createNew() {
        int billIdx = BarOption.popKitchenBillIdx();
        CreateNewOrderAction.makeSureTableExistsAndOpened(String.valueOf(billIdx), BarUtil.df.format(new Date()));
        TableButton tableToggleButton = new TableButton();
        tableToggleButton.setText(String.valueOf(billIdx));
        tableToggleButton.setTableName(String.valueOf(billIdx));

        TablesPanel tablesPanel = (TablesPanel)BarFrame.instance.panels[0];
        tablesPanel.onClick(tableToggleButton);
    }
}
