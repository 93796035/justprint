package com.just.print.ui.panel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.dialog.CmdBtnsManagerDlg;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.net.BackGroundTimerThread;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.cascustomize.CustOpts;

import java.sql.SQLException;

public class SalesPanel extends PicturePane{

    private static final String TAG = "TableFragment";

    private boolean isDragged;

    //temporal flags
    public boolean partialPaid;	//for indicating that money is partial paid, when leaving sales panel, will give a notice!

    public SalesPanel() {
        super(BarOption.getSalePanelBK());
        MAX_CMDBTN_QT = 20;
        initComponent();//have to do it earlier, because before it displayed, we already need it's property like billPanel
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRoot = inflater.inflate(getLayoutId(), container, false);
        reLayout();
        BarUtil.layoutCommandButtons(mRoot.findViewById(R.id.buttonBar), CmdBtnsManagerDlg.groupedButtons[2], MAX_CMDBTN_QT);
        return mRoot;
    }
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sales_panel;
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
//			AikaPos.db.rawQuery(sql.toString(), null);
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
                    .append(" where _id = ").append(outputID);
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
////            		.append(" WHERE bill._id IN ( SELECT _id FROM bill WHERE tableName = ").append(BarFrame.instance.valCurTable.getText())
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

    public void reLayout() {
        if(mRoot == null){
            return;
        }
        float billPanelWeight = BarOption.getMenuAreaPortion();
        float menuPanelWeight = 1.0f - billPanelWeight;

        FrameLayout billPanelContainer = mRoot.findViewById(R.id.billPanelContainer);
        FrameLayout menuPanelContainer = mRoot.findViewById(R.id.menuPanelContainer);

        // 设置 BillPanel 的布局权重
        LinearLayout.LayoutParams billPanelParams = (LinearLayout.LayoutParams) billPanelContainer.getLayoutParams();
        billPanelParams.weight = billPanelWeight;
        billPanelContainer.setLayoutParams(billPanelParams);

        // 设置 MenuPanel 的布局权重
        LinearLayout.LayoutParams menuPanelParams = (LinearLayout.LayoutParams) menuPanelContainer.getLayoutParams();
        menuPanelParams.weight = menuPanelWeight;
        menuPanelContainer.setLayoutParams(menuPanelParams);

        if (BarFrame.menuPanel.isAdded()) { // 加载 BillPanel 和 MenuPanel
            FragmentManager fragmentManager = BarFrame.menuPanel.getFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction()
                        .remove(BarFrame.menuPanel)
                        .commitNow();
            }
        }
        if (billPanel.isAdded()) { // 加载 BillPanel 和 MenuPanel
            FragmentManager fragmentManager = billPanel.getFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction()
                        .remove(billPanel)
                        .commitNow();
            }
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.billPanelContainer, billPanel);
        transaction.replace(R.id.menuPanelContainer, BarFrame.menuPanel);
        transaction.commit();
    }

    public void initComponent() {
        billPanel = new BillPanel(this);
        // properties
//        Color bg = BarOption.getBK("Sales");
//        if(bg == null) {
//            bg = new Color(216,216,216);
//        }
//        setBackground(bg);
//        setLayout(null);
    }
    @XViewByID(R.id.billPanelContainer)
    public BillPanel billPanel;
}
