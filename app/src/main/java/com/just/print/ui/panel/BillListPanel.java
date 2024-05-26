package com.just.print.ui.panel;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.app.BaseFragment;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.action.Cmd_CombineAll;
import org.cas.client.platform.bar.dialog.CmdBtnsManagerDlg;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintUtil;
import org.cas.client.platform.bar.uibeans.ArrowButton;
import org.cas.client.platform.bar.uibeans.FunctionToggleButton;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

import com.just.print.db.bean.Dish;
import com.just.print.util.JOptionPane;
import com.stupid.method.reflect.StupidReflect;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.model.DBConsts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BillListPanel extends PicturePane implements View.OnClickListener{

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bill_list;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }

    int curPageNum;

    public BillListPanel() {
//        super(BarOption.getBillPanelBK());
//        MAX_CMDBTN_QT = 10;
        initComponent();
    }

    public void initComponent() {
//        removeAll();
        billPanels= new ArrayList<BillPanel>();
        onScrBillPanels= new ArrayList<BillPanel>();

        btnLeft = new ArrowButton("<<");
        btnRight = new ArrowButton(">>");

//        separator= new JSeparator();
//
//        btnLeft.setMargin(new Insets(0, 0, 0, 0));
//        btnRight.setMargin(new Insets(0, 0, 0, 0));
//
//        setLayout(null);
//
//        add(btnLeft);
//        add(btnRight);
//        add(separator);
//
//        BarUtil.addFunctionButtons(this, CmdBtnsManagerDlg.groupedButtons[1]);
//
//        addComponentListener(this);
//        btnLeft.addActionListener(this);
//        btnRight.addActionListener(this);

        btnLeft.setEnabled(curPageNum > 0);
        reLayout();
    }

    public void initContent() {
        BarFrame.instance.getCmbCurTable().setEnabled(true);	//if can reach to the view, mean there un completed bills for sure, so the change table combobox is ennabled.

        cleanInterface();

        //load all the unclosed outputs under this table with content inside.---------------------------
        //output will be set as deleted=true only when click a "-" button. when bill closed, the output will not be set as deleted = true!
        //so closed bill of this table will also be counted. but will displayed in different color.

        int latestID = reInitBillPanels();

        reInitOnscreenBills(latestID + 1);

        allowUnCombineCheck();

        reLayout();
    }

    public void allowUnCombineCheck() {
        int unclosedNum = 0;
        for (BillPanel billPanel : billPanels) {
            if(billPanel.getStatus() < DBConsts.COMPLETED) {
                unclosedNum ++;
            }
        }
        if(unclosedNum == 1) {
            String tableName = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
            String openTime = BarFrame.instance.valOpenTime.getText().toString();
            StringBuilder sql =  new StringBuilder("SELECT DISTINCT category from output where SUBJECT = '").append(tableName)
                    .append("' and (deleted is null or deleted < ").append(DBConsts.COMPLETED)
                    .append(") and time = '").append(openTime).append("'");
            try {
                Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
                if (rs.getCount() > 1) {
                    Cmd_CombineAll.getInstance().getSourceBtn().setText(BarFrame.consts.UnCombine());
                }
            } catch (Exception e) {
                L.e("BillListPanel","exception in allowUnCombineCheck" + sql, e);
            }
        }else {
            Cmd_CombineAll.getInstance().getSourceBtn().setText(BarFrame.consts.CombineAll());
        }
    }

    private int reInitBillPanels(){
        String tableName = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
        String openTime = BarFrame.instance.valOpenTime.getText().toString();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT contactID from output where SUBJECT = '").append(tableName)
                .append("' and (deleted is null or deleted < ").append(DBConsts.EXPIRED)
                .append(") and time = '").append(openTime).append("' order by contactID");

        int latestID = 0;
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            while (rs.moveToNext()) {
                FunctionToggleButton billButton = new FunctionToggleButton(String.valueOf(DBUtil.getCursorInt(rs, "contactID")));
//                billButton.setMargin(new Insets(0, 0, 0, 0));

                BillPanel billPanel = new BillPanel(this, billButton);

                billPanel.initContent();
                billPanels.add(billPanel);

                latestID = DBUtil.getCursorInt(rs, "contactID");
            }
        } catch (Exception e) {
            L.e("BillListPane", "Unexpected exception when init the bill panels." + sql, e);
        }
        return latestID;
    }

    private void reInitOnscreenBills(int billNum) {
        //do it outside the above loop, because there's another qb query inside.
        int col = BarOption.getBillPageCol();
        int row = BarOption.getBillPageRow();

        for(int i = 0; i < row * col; i++) {
            if(row * col * curPageNum + i < billPanels.size()) {	//some panel is using the panel in billPanels list.
                onScrBillPanels.add(billPanels.get(row * col * curPageNum + i));
                btnRight.setEnabled(true);
            }else {													//others are temperally newed BillPanel.
                BillPanel panel = new BillPanel(this, new FunctionToggleButton(String.valueOf(billNum)));	//have to give a number to construct valid sql.
                onScrBillPanels.add(panel);
                btnRight.setEnabled(false);
                billNum++;
            }
        }
    }

    public void cleanInterface() {
        for(int i = onScrBillPanels.size() - 1; i >= 0; i--) {
//            remove(onScrBillPanels.get(i));
        }
        billPanels.clear();
        onScrBillPanels.clear();
    }

    private void reLayout() {

//        int tBtnHeight = BarFrame.instance.getHeight() / 10;
//
//        int top = BarUtil.layoutCommandButtons(this, CmdBtnsManagerDlg.groupedButtons[1]);
//        if(top < 0) {
//            return;
//        }
//        separator.setBounds(CustOpts.HOR_GAP,
//                top - CustOpts.VER_GAP * 2,
//                getWidth() - CustOpts.HOR_GAP * 2, tBtnHeight);
//
//        btnLeft.setBounds(CustOpts.SIZE_EDGE,
//                separator.getY() - 40 - CustOpts.VER_GAP * 2,
//                40, 40);
//        btnRight.setBounds(getWidth() - 40 - CustOpts.SIZE_EDGE,
//                btnLeft.getY(),
//                40, 40);
//
//        if(onScrBillPanels.size() > 0) { //@NOTE: when barframe initialized, this will be called.
//            int col = CustOpts.custOps.getValue("BillPanel_Col") == null ? 4 : Integer.valueOf((String)CustOpts.custOps.getValue("BillPanel_Col"));
//            int row = CustOpts.custOps.getValue("BillPanel_Row") == null ? 1 : Integer.valueOf((String)CustOpts.custOps.getValue("BillPanel_Row"));
//            int table_H = (separator.getY() - CustOpts.VER_GAP * 2)/row;
//            int table_W = (getWidth() - btnLeft.getWidth() * 2 - CustOpts.SIZE_EDGE * 2 - CustOpts.HOR_GAP * 2) / col - CustOpts.HOR_GAP;
//            for(int r = 0, i = 0; r < row; r++) {
//                for (int c = 0; c < col; c++) {
//                    int x = btnLeft.getX() + btnLeft.getWidth() + CustOpts.HOR_GAP + (CustOpts.HOR_GAP + table_W) * c;
//                    int y = (table_H + CustOpts.VER_GAP) * r + CustOpts.VER_GAP;
//                    onScrBillPanels.get(i).setBounds(x, y, table_W, table_H);
//                    onScrBillPanels.get(i).reLayout();
//                    onScrBillPanels.get(i).resetColWidth();
//                    add(onScrBillPanels.get(i));
//
//                    i++;
//                }
//            }
//        }
    }

    //can only be triggered when clicking the billpanel's viewport or table area under "billListPanel mode".
    public void moveDishToBill(BillPanel targetBillPanel) {
        int originalBillId = BarFrame.instance.curDish.getBillID();
        int targetBillId = targetBillPanel.getOrderedDishAry() != null && targetBillPanel.getOrderedDishAry().size() > 0?
                targetBillPanel.getOrderedDishAry().get(0).getBillID() : 0;
        //generate the new bills and generate the ref in comment.
        if(getCurBillPanel().getStatus() >= DBConsts.BILL_PRINTED || getCurBillPanel().getStatus() < DBConsts.ORIGINAL
                || targetBillPanel.getStatus() >= DBConsts.BILL_PRINTED || targetBillPanel.getStatus() < DBConsts.ORIGINAL) {
//			if (JOptionPane.showConfirmDialog(this, BarFrame.consts.ConvertClosedBillBack(), BarFrame.consts.Operator(),
//		            JOptionPane.YES_NO_OPTION) != 0) {// are you sure to convert the voided bill back？
//		        return;
//			}else {
            if(getCurBillPanel().getStatus() >= DBConsts.BILL_PRINTED || getCurBillPanel().getStatus() < DBConsts.ORIGINAL) {
                getCurBillPanel().expireAndReGenerateBill(getCurBillPanel().billButton.getText());
            }

            if(targetBillPanel.getStatus() >= DBConsts.BILL_PRINTED || targetBillPanel.getStatus() < DBConsts.ORIGINAL) {
                targetBillPanel.expireAndReGenerateBill(targetBillPanel.billButton.getText());
                if(getCurBillPanel().getStatus() >= DBConsts.BILL_PRINTED || getCurBillPanel().getStatus() < DBConsts.ORIGINAL) {
                    StringBuilder sql = new StringBuilder("update bill set comment = comment + ' ")
                            .append(PrintUtil.REF_TO).append(getCurBillPanel().getBillID()).append("'")
                            .append(PrintUtil.DATE_REF).append(getCurBillPanel().getZippedCreateTime())
                            .append(PrintUtil.OLD_SUBTOTAL).append(BarUtil.formatMoney(getCurBillPanel().subTotal/100.0))
                            .append(" where id = ").append(targetBillPanel.getBillID());
                    try {
                        AikaPos.db.execSQL(sql.toString());
                    }catch(Exception e) {
                        L.e("BillPane", "Excepioint in print bill:" + sql, e);
                    }
                }
            }
//			}
        }

        try {
            //get the output price of the moving dish, it will be used to adjust the total of two bill.
            int outputTotalPrice = 0;
            StringBuilder sql = new StringBuilder("select toltalprice from output where id = ").append(BarFrame.instance.curDish.getOutputID());
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            while (rs.moveToNext()) {
                outputTotalPrice = DBUtil.getCursorInt(rs, "toltalprice");
                break;
            }
            rs.close();// 关闭

            //update existing bill.
            if(originalBillId > 0) {
                sql = new StringBuilder("update bill set total = total - ")
                        .append(outputTotalPrice)
                        .append(" where id = ").append(originalBillId);
                AikaPos.db.execSQL(sql.toString());
            }

            //generate/update bill for target bill
            if(targetBillId > 0) {
                //update the total price of the target bill
                sql = new StringBuilder("update bill set total = total + ")
                        .append(outputTotalPrice)
                        .append(" where id = ").append(targetBillId);
                AikaPos.db.execSQL(sql.toString());
            }else {
                targetBillId = BarFrame.instance.createAnEmptyBill(
                        BarFrame.instance.getCmbCurTable().getSelectedItem().toString(),
                        BarFrame.instance.valOpenTime.getText().toString(),
                        Integer.valueOf(targetBillPanel.billButton.getText()), 1);
                sql = new StringBuilder("update bill set total = ")
                        .append(outputTotalPrice)
                        .append(" where id = ").append(targetBillId);
                AikaPos.db.execSQL(sql.toString());
            }
            BarFrame.instance.curDish.setBillID(targetBillId);	//might not necessary, just in case the billPanel will not updated before it's used anywhere.

            // Update the output to belongs to the new ContactID (contactID is bill Index,not  bill id)
            sql = new StringBuilder("update output set CONTACTID = ").append(targetBillPanel.billButton.getText())
                    .append(", category = " + targetBillId)
                    .append(" where id = ").append(BarFrame.instance.curDish.getOutputID());
            AikaPos.db.execSQL(sql.toString());

        }catch(Exception exp) {
            L.e("SalesPanel", "unexpected error when updating the totalvalue of bill.", exp);
        }

        // update all the table content.
        BarFrame.instance.curDish = null;
        initContent();
    }

    @Override
    public void onClick(View o) {	//@NOTE: the bill button could trigger two times of event.
        if(o instanceof ArrowButton){
            if(o == btnLeft) {
                curPageNum--;
            }else if(o == btnRight) {
                curPageNum++;
            }
            btnLeft.setEnabled(curPageNum > 0);
            initContent();
        }else {
            //select all output of each bill wich curtable and status is not completed, and set the status to be cancelled.
            //set the table as unselected.
        }
    }

    public void combineBills(ArrayList<BillPanel> unclosedBillPanels, boolean needCombinOutputs) {
        String firstUnclosedBillIdx = unclosedBillPanels.get(0).billButton.getText();

        //update all related output to belongs to first Bill, deleted and completed output will not be modified.
        //the billId will not be modified, so when waiter want to do a undo, can use the billID to undo combine all.
        String tableName = BarFrame.instance.getCmbCurTable().getSelectedItem().toString();
        String openTime = BarFrame.instance.valOpenTime.getText().toString();
        StringBuilder sql = new StringBuilder("update output set contactID = ").append(firstUnclosedBillIdx)
                //@NOTE we don't change the billID when combinAll(to support undo) .append(", category = '").append(firstUnclosedBillId).append("'")
                .append(" where SUBJECT = '").append(tableName)
                .append("' and time = '").append(openTime)
                .append("' and (deleted is null or DELETED = ").append(DBConsts.ORIGINAL).append(")");
        try {
            AikaPos.db.execSQL(sql.toString());
        }catch(Exception exp) {
            L.e("Combine All", "exception when updating output with "+ sql, exp);
        }

//				一轮遍历过之后，可以确定消灭了二次分单的存在。但是一次分单的情况仍然存在。所以有必要再进行一遍，用来把pS也去掉。
//				两遍目前是足够的。但程序上应该写成说只要发现“pkOrPsFoundFlag”没有被打上，就表示合并结束（没分单过的是否合并是个问题，因为有的菜点了多次，但每个菜可能给了不同的折扣）
//there's a bug, when coplete one of the equual splitted bill, will cause issue when combineOutputs, so I added a flag, if any bill closed, don not do this operation anymore.
        if(needCombinOutputs) {
            combineOutputs(tableName, openTime, Integer.valueOf(firstUnclosedBillIdx));
        }

        //combine bills discount and services fees---------------------------------------------------------------------
        //combine the discount, service fee, of all the none-empty bills.
        //Note: we don't deleted combined bills now, because might be used again later.
        //when we reset the table. we check if it the last non-empty bill and clean all the empty bill .
        int combinedDiscount = 0;
        int combinedServiceFee = 0;
        StringBuilder combinedComment = new StringBuilder();
        for (BillPanel billPanel : unclosedBillPanels) {
            combinedDiscount += billPanel.discount;
            combinedServiceFee += billPanel.serviceFee;
            if(billPanel.getStatus() >= DBConsts.BILL_PRINTED || billPanel.getStatus() < DBConsts.ORIGINAL) {
                combinedComment.append(PrintUtil.REF_TO).append(billPanel.getBillID())
                        .append(PrintUtil.DATE_REF).append(billPanel.getZippedCreateTime())
                        .append(PrintUtil.OLD_SUBTOTAL).append(BarUtil.formatMoney(billPanel.subTotal/100.0));
            }
        }

        BillPanel billPanel = unclosedBillPanels.get(0);
        if(billPanel.getStatus() >= DBConsts.BILL_PRINTED || billPanel.getStatus() < DBConsts.ORIGINAL) {
            billPanel.expireAndReGenerateBill(billPanel.billButton.getText());
        }
        sql = new StringBuilder("update bill set discount = ").append(combinedDiscount)
                .append(", serviceFee = ").append(combinedServiceFee)
                .append(", comment = '").append(combinedComment).append("'")
                .append(" where id = ").append(billPanel.getBillID());	//billPanel's billID will be a new bill Id if it's regenerated.

        try {
            AikaPos.db.execSQL(sql.toString());
        }catch(Exception exp) {
            L.e("Combine All", "exception when updating bill with "+ sql, exp);
        }

        //reinit the display----------------------------------------------------------------------------------------------
        initContent();
    }

    public ArrayList<BillPanel> gatherAllUnclosedBillPanels() {
        ArrayList<BillPanel> billPanelsToCombine = new ArrayList<BillPanel>();	//add all panel to be combined, for calculating the service fee and dicount in it.
        for (BillPanel billPanel : billPanels) {
            if(billPanel.getStatus() < DBConsts.COMPLETED) {
                billPanelsToCombine.add(billPanel);
            }
        }
        return billPanelsToCombine;
    }

    public boolean checkColosedBill() {
        for (BillPanel billPanel : billPanels) {
            if(billPanel.getStatus() == DBConsts.COMPLETED) {
                if (JOptionPane.showConfirmDialog(BarFrame.consts.workOnOnlyUnclosedBills(),
                        org.cas.client.resources.international.DlgConst.DlgTitle)) {
                    return false;
                }
            }
        }
        return true;
    }

    //if in same table same openTime same billIndex, we should combine the outputs.
    //qty, total price, discount, serviceFee
    private void combineOutputs(String tableName, String startTime, int billIdx) {

        //target the items which can be combined.
        List<Dish> dishes = new ArrayList<Dish>();

        StringBuilder sql = new StringBuilder("select * from output where SUBJECT = '").append(tableName)
                .append("' and time = '").append(startTime)
                .append("' and contactID = ").append(billIdx)
                .append(" and (deleted is null or deleted = ").append(DBConsts.ORIGINAL).append(") order by id");
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            while (rs.moveToNext()) {
                int num = DBUtil.getCursorInt(rs,"OUTPUT.AMOUNT");
                if(num < BarOption.MaxQTY) {
                    continue;
                }
                Dish dish = new Dish();
                dish.setNum(num);
                dish.setDiscount(DBUtil.getCursorInt(rs,"discount"));//
                dish.setId((long)DBUtil.getCursorInt(rs,"PRODUCTID"));
                dish.setModification(DBUtil.getCursorString(rs, "OUTPUT.CONTENT"));//
                dish.setOutputID(DBUtil.getCursorInt(rs,"OUTPUT._ID"));//
                dish.setTotalPrice(DBUtil.getCursorInt(rs,"OUTPUT.TOLTALPRICE"));
                dishes.add(dish);
            }
        }catch(Exception exp) {
            L.e("BillList ","exception when combine outputs:" + sql, exp);
        }

        HashMap map = new HashMap();
        for (int i = dishes.size() - 1; i >= 0; i--) {	//here, all dish是带有PS或者PK的
            Dish dish = dishes.get(i);
            int num = dish.getNum();
            //记录下PK和PS.和Num。总单的discount，和totalPrice（total price应该是已经扣除了本菜的discount的）
            int pK = num /(BarOption.MaxQTY * 100);
            if(num > BarOption.MaxQTY * 100) {
                num = num %(BarOption.MaxQTY * 100);
            }
            int pS = num /BarOption.MaxQTY;
            if(num > BarOption.MaxQTY) {
                num = num % BarOption.MaxQTY;
            }

            String key = new StringBuilder("pk:").append(pK).append("ps:").append(pS).append("num:").append(num)
                    .append("dish:").append(dish.getId()).append(String.valueOf(dish.getModification())).toString();

            if(map.containsKey(key)) {//if map already contains this key, then delete this output

                sql = new StringBuilder("update output set deleted = ").append(DBConsts.DELETED)
                        .append(" where id = ").append(dish.getOutputID());
                try {
                    AikaPos.db.execSQL(sql.toString());
                }catch(Exception e) {
                    L.e("combineOutputs", " exception when change status of an ouput which already in map:" + sql, e);
                }
                dishes.remove(i);

                //并把map中的value减少1.如果减完1后value值为1，map中去掉这个key。同时更新原始的（那个key相同但是没有被删除的）的output记录到数据库，
                //把discount*value，totalPrice也*valuev，num中的pK位去掉，如果已经去掉了，那么把PS位去掉。（继续遍历时，如果发现还有这个key的，将会新建一个key存入map）
                Dish tD = (Dish)map.get(key);
                int value = tD.getDisplayIdx() - 1;

                tD.setDisplayIdx(value);
                tD.setDiscount(tD.getDiscount() + dish.getDiscount());
                tD.setTotalPrice(tD.getTotalPrice() + dish.getTotalPrice());

                if(value == 1) {	//@NOTE： it's not necessarily equals to 1,  because if some of the splitted bill already paid, then it will never reach 1.
                    map.remove(key);
                    int newNum = pK > 1 ? pS * BarOption.MaxQTY + num : num;
                    sql = new StringBuilder("update output set AMOUNT = ").append(newNum)
                            .append(", discount = ").append(tD.getDiscount())
                            .append(", TOLTALPRICE = ").append(tD.getTotalPrice())
                            .append(" where id = ").append(tD.getOutputID());
                    try {
                        AikaPos.db.execSQL(sql.toString());
                    }catch(Exception e) {
                        L.e("combineOutputs", "update output set amount = " + newNum + " where id = " + dish.getOutputID(), e);
                    }
                }
            }else {//如果发现map中没有这个key，那么创建一个key存入map。PK为value，如果PK不存在，或者为1，或者为0，那么用PS为value。
                dish.setDisplayIdx(pK > 1 ? pK : pS);	//use dspIndex to save the temparal new value, so we can know if it's merged ever
                map.put(key, dish);
            }
        }
    }

    public static int getANewBillIdx(String tableName, String openTime){
        if(BarUtil.empty(tableName)) {
            tableName = BarFrame.instance.cmbCurTable.getSelectedItem().toString();
        }
        if(BarUtil.empty(openTime)) {
            openTime = BarFrame.instance.valOpenTime.getText().toString();
        }

        int num = 0;
        try {
            StringBuilder sql = new StringBuilder("select billIndex from bill where tableId = '").append(tableName).append("'")
                    .append(" and opentime = '").append(openTime).append("'");
            //TBD:the status curently used "expired", means void and completed bills will all be included, to ensure the new customer number
            //number not duplicated with existing one.
            sql.append(" and (status is null or status < ").append(DBConsts.EXPIRED).append(" and status >= 0)");
            sql.append(" order by id");//if order by billIndex, then "9" will be the last one. so should order by id
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            if (rs != null && rs.moveToFirst()) {
                num = DBUtil.getCursorInt(rs, "billIndex");
            }
        } catch (Exception exp) {
            L.d("BillListPane.getANewBillNumber", "found no bill on an already opened table.");
            return 1;
        }
        return num + 1;
    }

    public BillPanel getCurBillPanel(){
        if(billPanels.size() == 1)
            return billPanels.get(0);
        else {
            List<BillPanel> panels = getSelectedBillPannels();
            return panels.size() == 1 ? panels.get(0) : null;
        }
    }

    public List<BillPanel> getSelectedBillPannels(){
        List<BillPanel> panels = new ArrayList<>();
        for (BillPanel billPanel : onScrBillPanels) {
            if(billPanel.billButton.isSelected())
                panels.add(billPanel);
        }

        return panels;
    }

    public BillPanel getBillPanelByBillNumber(int targetBillIdx) {
        for (BillPanel billPanel : billPanels) {
            if(billPanel.billButton.getText().equals(String.valueOf(targetBillIdx))) {
                return billPanel;
            }
        }
        return null;
    }

    public List<BillPanel> billPanels;
    List<BillPanel> onScrBillPanels;

    private ArrowButton btnLeft;
    private ArrowButton btnRight;
//    JSeparator separator;
}