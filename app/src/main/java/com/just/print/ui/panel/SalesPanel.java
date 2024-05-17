package com.just.print.ui.panel;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Dish;
import com.just.print.db.expand.DaoExpand;
import com.stupid.method.reflect.StupidReflect;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.DBUtil;
import org.cas.client.platform.bar.dialog.CmdBtnsManagerDlg;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.cascustomize.CustOpts;

import java.sql.SQLException;

public class SalesPanel extends PicturePane{

    private static final String TAG = "TableFragment";

    private boolean isDragged;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initComponent();
        return mRoot;
    }
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sales_panel;
    }

    private static SalesPanel instance = null;
    public static SalesPanel getInstance(){
        if(instance == null){
            instance = new SalesPanel();
        }
        return instance;
    }
    //for print
    public static String SUCCESS = "0";
    public static String ERROR = "2";

    //temporal flags
    public boolean partialPaid;	//for indicating that money is partial paid, when leaving sales panel, will give a notice!

    public SalesPanel() {
//        MAX_CMDBTN_QT = 20;
        initComponent();
    }

    //used by discCountCoupon, will be deleted to use only the above method with same name.
    public void discountBill(float discount) {
        if(!billPanel.notTooLateToChangeStatus()) {
            return;
        }
        billPanel.discount = discount > billPanel.subTotal ? billPanel.subTotal : discount;
        billPanel.updateTotalArea(false);

        billPanel.unsavedItemsCheck();
        DBUtil.updatePriceAndCreateTimeInBillRecord(billPanel, DBConsts.SAVED, null);
    }

    //Todo: Maybe it's safe to delete this method, because I think no need to touch ouputs.
//	private void reopenOutput() {
//		//convert the status of relevant output.
//		StringBuilder sql = new StringBuilder("update output set deleted = ").append(DBConsts.original)
//				.append(" where deleted = ").append(DBConsts.voided)
//				.append(" and category = ").append(billPanel.billID);
//		try {
//			PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
//		}catch(Exception exp) {
//			L.e("SalesPane", "Exception happenned when converting output's status to 0", exp);
//		}
//	}

    public void discountADish(int value, Dish mostExpensiveDish) throws SQLException {
        if(!billPanel.notTooLateToChangeStatus()) {
            return;
        }
        long outputID = mostExpensiveDish.getOutputID();
        if(outputID >= 0) {
            StringBuilder sql = new StringBuilder("update output set discount = ").append(value)
                    .append(", toltalprice = ").append(Math.round(mostExpensiveDish.getPrice() - value))
                    .append(" where id = ").append(outputID);
            AikaPos.db.execSQL(sql.toString());
        }

        billPanel.updateTotalArea(false);
        billPanel.unsavedItemsCheck();
        billPanel.billPricesUpdateToDB();
    }

//    public static void resetCurTable(){
//    	try {
//            //clean all empty bill (match table id and opentime, status is null, while doesn't exist in any output.).
//            //if there's an output was deleted from this bill, this bill is still considered as empty.
//            //if there's an output was completed 10
////            sql = new StringBuilder("update bill set status = ").append(DBConsts.deleted)
////            		.append(" WHERE bill.id IN ( SELECT id FROM bill WHERE tableID = ").append(BarFrame.instance.valCurTable.getText())
////    				.append(" and OPENTIME = '").append(BarFrame.instance.valStartTime.getText())
////    				.append("' and status IS NULL OR status = ").append(DBConsts.original)
////    				.append(") AND NOT EXISTS (SELECT category FROM OUTPUT WHERE (deleted IS null or deleted = ").append(DBConsts.completed)
////    				.append(" AND time = '").append(BarFrame.instance.valStartTime.getText())
////    				.append("' and SUBJECT = '").append(BarFrame.instance.valCurTable.getText()).append("')");
//
//            //no need to be complex, all ortiginal status bills of this table should be cleaned.
//            //close table
//            BarFrame.instance.closeATable(BarFrame.instance.cmbCurTable.getSelectedItem().toString(),
//            		BarFrame.instance.valStartTime.getText());
//    	}catch(Exception exp) {
//    		ErrorUtil.write(exp);
//    	}
//    }

    void reLayout() {
        // command buttons--------------
        int top = BarUtil.layoutCommandButtons(this, CmdBtnsManagerDlg.groupedButtons[2]);
        if(top < 0) {
            return;
        }
        int v_gat = CustOpts.VER_GAP / 2;
        int h_gap = CustOpts.HOR_GAP / 2;
        // TOP part============================
        int topAreaHeight = top - 3 * v_gat;

//        billPanel.setBounds(h_gap/2, v_gat,
//                (int) (getWidth() * (1 - BarOption.getMenuAreaPortion())), topAreaHeight);

        // menu area--------------
//        int xMenuArea = billPanel.getX() + billPanel.getWidth() + h_gap;
//        int widthMenuArea = getWidth() - billPanel.getWidth() - h_gap - CustOpts.SIZE_EDGE;

//        BarFrame.menuPanel.setBounds(xMenuArea, billPanel.getY(), widthMenuArea, topAreaHeight + 4);
        BarFrame.menuPanel.reLayout();

        billPanel.resetColWidth();
    }

    public void initComponent() {
//        removeAll();	//when it's called by setting panel(changed colors...), it will be called to refresh.

        billPanel = new BillPanel(this);
        // properties
//        Color bg = BarOption.getBK("Sales");
//        if(bg == null) {
//            bg = new Color(216,216,216);
//        }
//        setBackground(bg);
//        setLayout(null);

        // built
        BarUtil.addFunctionButtons(this, CmdBtnsManagerDlg.groupedButtons[2]);

//        add(billPanel);
        // add listener
//        addComponentListener(this);
//        addKeyListener(this);
        // 因为考虑到条码经常由扫描仪输入，不一定是靠键盘，所以专门为他加了DocumentListener，通过监视内容变化来自动识别输入完成，光标跳转。
        // tfdProdNumber.getDocument().addDocumentListener(this); // 而其它组件如实收金额框不这样做为了节约（一个KeyListener接口全搞定）
        reLayout();
    }

    public BillPanel billPanel;
    private String keyboardInput = "";

//    @Override
//    public void keyTyped(KeyEvent e) {
//        char key = e.getKeyChar();
//        if (key == 10) {		//when "enter" is hit, start to process(match a menu and add to selection.
//            int idx = keyboardInput.indexOf(".");//first get the inputted category and menu.
//            String category = idx > 0 ? keyboardInput.substring(0, idx) : BarFrame.menuPanel.categoryNameMetrix[0][0];
//            String menu = keyboardInput.substring(idx + 1);
//            //find out category index
//            int categoryIdx = 0;
//            int menuIdx = 0;
//            if(idx > 0) {	//if it contains the ".", and not in 0 position means user inputted category index of name
//                try {
//                    categoryIdx = Integer.valueOf(category) - 1;
//                    category = BarFrame.instance.menuPanel.categoryNameMetrix[0][categoryIdx];
//                }catch(Exception exp) {
//                    boolean matched = false;
//                    //not a number, then match to a String.
//                    for (int i = 0; i < BarFrame.menuPanel.categoryNameMetrix.length; i++) {
//                        for(int j = 0; j < BarFrame.menuPanel.categoryNameMetrix[i].length; j++) {
//                            if(category.equalsIgnoreCase(BarFrame.menuPanel.categoryNameMetrix[i][j])) {
//                                categoryIdx = j;
//                                category = BarFrame.menuPanel.categoryNameMetrix[0][categoryIdx];
//                                matched = true;
//                                break;
//                            }
//                        }
//                        if(matched) {
//                            break;
//                        }
//                    }
//                }
//            }
//
//            //find out menu index
//            try {
//                menuIdx = Integer.valueOf(menu) - 1;
//                Dish[] dishes = BarFrame.menuPanel.getDishAry();
//                int j = 0;
//                for(int i = 0; i < dishes.length; i++) {
//                    if(dishes[i].getCATEGORY().equalsIgnoreCase(category)) {
//                        if(menuIdx == j) {
//                            menu = dishes[i].getLanguage(0);
//                            break;
//                        }
//                        j++;
//                    }
//                }
//            }catch(Exception exp) {
//                boolean matched = false;
//                //not a number, then match to a String.
//                for (int i = 0; i < BarFrame.menuPanel.dishNameMetrix.length; i++) {
//                    for(int j = 0; j < BarFrame.menuPanel.dishNameMetrix[i].length; j++) {
//                        if(menu.equalsIgnoreCase(BarFrame.menuPanel.dishNameMetrix[i][j])) {
//                            menuIdx = j;
//                            menu = BarFrame.menuPanel.dishNameMetrix[0][menuIdx];
//                            matched = true;
//                            break;
//                        }
//                    }
//                    if(matched) {
//                        break;
//                    }
//                }
//            }
//            //trigger the menuSelect event
//            BillPanel billPanel = ((SalesPanel)BarFrame.instance.panels[2]).billPanel;
//            if(!billPanel.notTooLateToChangeStatus()) {
//                return;
//            }
//            // add into table. @note, here can not use menuButton, because the menu button might not exist.
//            // how to find the dish by menu name.
//            Dish[] dishes = BarFrame.menuPanel.getDishAry();
//            Dish dish = null;
//            for(int i = 0; i < dishes.length; i++) {
//                if(dishes[i].getLanguage(0).equals(menu)) {// && ) {
//                    dish = dishes[i];
//                    if(dishes[i].getCATEGORY().equals(category)) {
//                        dish = null;
//                        BarFrame.menuPanel.addIntoBillPane(dishes[i]);
//                        break;
//                    }
//                }
//            }
//            if(dish != null) {
//                BarFrame.menuPanel.addIntoBillPane(dish);
//            }
//            keyboardInput = "";
//        } else {
//            keyboardInput = keyboardInput + String.valueOf(key);
//        }
//        BarFrame.setStatusMes(keyboardInput);
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {}
//
//    @Override
//    public void keyReleased(KeyEvent e) {}
}
