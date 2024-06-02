package com.just.print.ui.panel;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.expand.DaoExpand;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.util.JOptionPane;
import com.stupid.method.reflect.StupidReflect;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.action.Cmd_CancelAll;
import org.cas.client.platform.bar.action.Cmd_ChangeTable;
import org.cas.client.platform.bar.dialog.NumberPanelDlg;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.action.CreateNewOrderAction;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.TableButton;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class SettingPanel extends PicturePane implements View.OnClickListener, View.OnTouchListener {
    public static String startTime;

    // flags
    NumberPanelDlg numberPanelDlg;

    // for print
    public static String SUCCESS = "0";
    public static String ERROR = "2";

    private EditText tfdGST, tfdQST, tfdBillPageRow, tfdBillPageCol, tfdStartTimeOfDay, tfdPrinterMinReachTime, tfdServerHost, tfdDBHost, tfdMevWaitTime, tfdLogoutTime;
    private CheckBox cbxIsTrainingMode, cbxIsSingleUserMode, cbxIsDiscoutAffectTax, cbxIsServiceFeeAffectTax, cbxIsBuffetMode, cbxIsFastFoodMode, cbxIsHideRecordFromOtherWaiter, cbxIsWaiterAllowedToDiscount, cbxIsWaiterAllowedToChangePrice, cbxShowCustomerFrame;
    private Button bkMenu, restoreMenu, bkConfig, restoreConfig, moreOptions;

    public SettingPanel() {
        super(BarOption.getSettingPanelBK());
        MAX_CMDBTN_QT = 10;
//        initComponent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_panel, container, false);

        tfdGST = view.findViewById(R.id.tfdGST);
        tfdQST = view.findViewById(R.id.tfdQST);
        tfdBillPageRow = view.findViewById(R.id.tfdBillPageRow);
        tfdBillPageCol = view.findViewById(R.id.tfdBillPageCol);
        tfdStartTimeOfDay = view.findViewById(R.id.tfdStartTimeOfDay);
        tfdPrinterMinReachTime = view.findViewById(R.id.tfdPrinterMinReachTime);
        tfdServerHost = view.findViewById(R.id.tfdServerHost);
        tfdDBHost = view.findViewById(R.id.tfdDBHost);
        tfdMevWaitTime = view.findViewById(R.id.tfdMevWaitTime);
        tfdLogoutTime = view.findViewById(R.id.tfdLogoutTime);

        cbxIsTrainingMode = view.findViewById(R.id.cbxIsTrainingMode);
        cbxIsSingleUserMode = view.findViewById(R.id.cbxIsSingleUserMode);
        cbxIsDiscoutAffectTax = view.findViewById(R.id.cbxIsDiscoutAffectTax);
        cbxIsServiceFeeAffectTax = view.findViewById(R.id.cbxIsServiceFeeAffectTax);
        cbxIsBuffetMode = view.findViewById(R.id.cbxIsBuffetMode);
        cbxIsFastFoodMode = view.findViewById(R.id.cbxIsFastFoodMode);
        cbxIsHideRecordFromOtherWaiter = view.findViewById(R.id.cbxIsHideRecordFromOtherWaiter);
        cbxIsWaiterAllowedToDiscount = view.findViewById(R.id.cbxIsWaiterAllowedToDiscount);
        cbxIsWaiterAllowedToChangePrice = view.findViewById(R.id.cbxIsWaiterAllowedToChangePrice);
        cbxShowCustomerFrame = view.findViewById(R.id.cbxShowCustomerFrame);

        bkMenu = view.findViewById(R.id.bkMenu);
        restoreMenu = view.findViewById(R.id.restoreMenu);
        bkConfig = view.findViewById(R.id.bkConfig);
        restoreConfig = view.findViewById(R.id.restoreConfig);
        moreOptions = view.findViewById(R.id.moreOptions);

        initComponent();

        return view;
    }

    private void initComponent() {
        // 设置初始值
        tfdGST.setText(String.valueOf(BarOption.getGST()));
        tfdQST.setText(String.valueOf(BarOption.getQST()));
        tfdBillPageRow.setText(String.valueOf(BarOption.getBillPageRow()));
        tfdBillPageCol.setText(String.valueOf(BarOption.getBillPageCol()));
        tfdStartTimeOfDay.setText(String.valueOf(BarOption.getStartTime()));
        tfdPrinterMinReachTime.setText(String.valueOf(BarOption.getPrinterMinReachTime()));
        tfdServerHost.setText(String.valueOf(BarOption.getServerHost()));
        tfdDBHost.setText(String.valueOf(CustOpts.custOps.getDBHost()));
        tfdMevWaitTime.setText(String.valueOf(BarOption.getWebMevWaitTime()));
        tfdLogoutTime.setText(String.valueOf(BarOption.getAutoLogoutTime()));

        cbxIsTrainingMode.setChecked(BarOption.isTrainingMode());
        cbxIsSingleUserMode.setChecked(BarOption.isSingleUser());
        cbxIsDiscoutAffectTax.setChecked(BarOption.isDiscountAffectTax());
        cbxIsServiceFeeAffectTax.setChecked(BarOption.isServiceFeeAffectTax());
        cbxIsBuffetMode.setChecked(BarOption.isBuffetMode());
        cbxIsFastFoodMode.setChecked(BarOption.isCounterMode());
        cbxIsHideRecordFromOtherWaiter.setChecked(BarOption.showRecordFromOtherWaiter());
        cbxIsWaiterAllowedToDiscount.setChecked(BarOption.isWaiterAllowedToDiscount());
        cbxIsWaiterAllowedToChangePrice.setChecked(BarOption.isWaiterAllowedToChangePrice());
        cbxShowCustomerFrame.setChecked(BarOption.isShowCustomerFrame());

        // 设置监听器
        tfdGST.setOnFocusChangeListener(this::onFocusChange);
        tfdQST.setOnFocusChangeListener(this::onFocusChange);
        tfdBillPageRow.setOnFocusChangeListener(this::onFocusChange);
        tfdBillPageCol.setOnFocusChangeListener(this::onFocusChange);
        tfdStartTimeOfDay.setOnFocusChangeListener(this::onFocusChange);
        tfdPrinterMinReachTime.setOnFocusChangeListener(this::onFocusChange);
        tfdServerHost.setOnFocusChangeListener(this::onFocusChange);
        tfdDBHost.setOnFocusChangeListener(this::onFocusChange);
        tfdMevWaitTime.setOnFocusChangeListener(this::onFocusChange);
        tfdLogoutTime.setOnFocusChangeListener(this::onFocusChange);

        cbxIsTrainingMode.setOnClickListener(this);
        cbxIsSingleUserMode.setOnClickListener(this);
        cbxIsDiscoutAffectTax.setOnClickListener(this);
        cbxIsServiceFeeAffectTax.setOnClickListener(this);
        cbxIsBuffetMode.setOnClickListener(this);
        cbxIsFastFoodMode.setOnClickListener(this);
        cbxIsHideRecordFromOtherWaiter.setOnClickListener(this);
        cbxIsWaiterAllowedToDiscount.setOnClickListener(this);
        cbxIsWaiterAllowedToChangePrice.setOnClickListener(this);
        cbxShowCustomerFrame.setOnClickListener(this);

        bkMenu.setOnClickListener(this);
        restoreMenu.setOnClickListener(this);
        bkConfig.setOnClickListener(this);
        restoreConfig.setOnClickListener(this);
        moreOptions.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbxIsTrainingMode:
                BarOption.setIsTrainingMode(String.valueOf(cbxIsTrainingMode.isChecked()));
                break;
            case R.id.cbxIsSingleUserMode:
                BarOption.setSingleUser(String.valueOf(cbxIsSingleUserMode.isChecked()));
                break;
            case R.id.cbxIsDiscoutAffectTax:
                BarOption.setIsDisCountBeforeTax(cbxIsDiscoutAffectTax.isChecked());
                break;
            case R.id.cbxIsServiceFeeAffectTax:
                BarOption.setIsServiceFeeAffectTax(cbxIsServiceFeeAffectTax.isChecked());
                break;
            case R.id.cbxIsBuffetMode:
                BarOption.setIsBuffetMode(cbxIsBuffetMode.isChecked());
                break;
            case R.id.cbxIsFastFoodMode:
                BarOption.setCounterMode(cbxIsFastFoodMode.isChecked());
                break;
            case R.id.cbxIsHideRecordFromOtherWaiter:
                BarOption.showRecordFromOtherWaiter(cbxIsHideRecordFromOtherWaiter.isChecked());
                break;
            case R.id.cbxIsWaiterAllowedToDiscount:
                BarOption.setIsWaiterAllowedToDiscount(cbxIsWaiterAllowedToDiscount.isChecked());
                break;
            case R.id.cbxIsWaiterAllowedToChangePrice:
                BarOption.setIsWaiterAllowedToChangePrice(cbxIsWaiterAllowedToChangePrice.isChecked());
                break;
            case R.id.cbxShowCustomerFrame:
                BarOption.setShowCustomerFrame(cbxShowCustomerFrame.isChecked());
                break;
            case R.id.bkMenu:
                dumpDB();
                break;
            case R.id.restoreMenu:
                restoreMenu();
                break;
            case R.id.moreOptions:
                showMoreOptions();
                break;
        }
    }

    private void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            switch (v.getId()) {
                case R.id.tfdGST:
                    BarOption.setGST(tfdGST.getText().toString());
                    break;
                case R.id.tfdQST:
                    BarOption.setQST(tfdQST.getText().toString());
                    break;
                case R.id.tfdBillPageRow:
                    BarOption.setBillPageRow(tfdBillPageRow.getText().toString());
                    ((BillListPanel) BarFrame.instance.panels[1]).initComponent();
                    break;
                case R.id.tfdBillPageCol:
                    BarOption.setBillPageCol(tfdBillPageCol.getText().toString());
                    ((BillListPanel) BarFrame.instance.panels[1]).initComponent();
                    break;
                case R.id.tfdStartTimeOfDay:
                    BarOption.setStartTime(tfdStartTimeOfDay.getText().toString());
                    break;
                case R.id.tfdPrinterMinReachTime:
                    BarOption.setPrinterMinReachTime(tfdPrinterMinReachTime.getText().toString());
                    break;
                case R.id.tfdServerHost:
                    BarOption.setServerHost(tfdServerHost.getText().toString());
                    break;
                case R.id.tfdDBHost:
                    BarOption.setDBHost(tfdDBHost.getText().toString());
                    break;
                case R.id.tfdMevWaitTime:
                    BarOption.setWebMevWaitTime(tfdMevWaitTime.getText().toString());
                    break;
                case R.id.tfdLogoutTime:
                    BarOption.setAutoLogoutTime(tfdLogoutTime.getText().toString());
                    break;
            }
        }
    }

    private void dumpDB() {
        // 实现数据库导出逻辑
    }

    private void restoreMenu() {
        // 实现数据库恢复逻辑
    }

    private void showMoreOptions() {
        // 实现更多选项显示逻辑
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 实现触摸事件处理逻辑
        return false;
    }
}
