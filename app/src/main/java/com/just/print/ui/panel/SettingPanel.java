package com.just.print.ui.panel;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.just.print.R;
import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.CmdBtnsManagerDlg;

public class SettingPanel extends PicturePane {

    private EditText tfdGST, tfdQST, tfdBillPageRow, tfdBillPageCol, tfdStartTimeOfDay,
            tfdPrinterMinReachTime, tfdServerHost, tfdDBHost, tfdMevWaitTime, tfdLogoutTime;
    private CheckBox cbxIsTrainingMode, cbxIsSingleUserMode, cbxIsBuffetMode, cbxIsFastFoodMode,
            cbxIsDiscoutAffectTax, cbxIsServiceFeeAffectTax, cbxIsHideRecordFromOtherWaiter,
            cbxIsWaiterAllowedToDiscount, cbxIsWaiterAllowedToChangePrice, cbxShowCustomerFrame;
    private Button moreOptions, bkMenu, restoreMenu;
    private TextView lblGST, lblQST, lblBillPageRow, lblBillPageCol, lblStartTimeOfDay,
            lblPrinterMinReachTime, lblServerHost, lblDBHost, lblMevWaitTime, lblLogoutTime;

    public SettingPanel() {
        super(BarOption.getSettingPanelBK());
        MAX_CMDBTN_QT = 10;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRoot = inflater.inflate(getLayoutId(), container, false);
        initView(mRoot);
        initListeners();
        BarUtil.layoutCommandButtons(mRoot.findViewById(R.id.buttonBar), CmdBtnsManagerDlg.groupedButtons[3], MAX_CMDBTN_QT);
        reLayout();
        if (savedInstanceState == null) {
            if (BarFrame.menuPanel.isAdded()) {
                // Fragment 已添加到容器，可以安全地移除
                BarFrame.instance.panels[2].getChildFragmentManager().beginTransaction()
                        .remove(BarFrame.menuPanel)
                        .commitNow();
            }
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.menuPanelContainer, BarFrame.menuPanel)
                    .commit();
        }
        return mRoot;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting_panel;
    }

    private void initView(View view) {
        lblGST = view.findViewById(R.id.lblGST);
        tfdGST = view.findViewById(R.id.tfdGST);
        lblQST = view.findViewById(R.id.lblQST);
        tfdQST = view.findViewById(R.id.tfdQST);
        lblBillPageRow = view.findViewById(R.id.lblBillPageRow);
        tfdBillPageRow = view.findViewById(R.id.tfdBillPageRow);
        lblBillPageCol = view.findViewById(R.id.lblBillPageCol);
        tfdBillPageCol = view.findViewById(R.id.tfdBillPageCol);
        lblStartTimeOfDay = view.findViewById(R.id.lblStartTimeOfDay);
        tfdStartTimeOfDay = view.findViewById(R.id.tfdStartTimeOfDay);
        lblPrinterMinReachTime = view.findViewById(R.id.lblPrinterMinReachTime);
        tfdPrinterMinReachTime = view.findViewById(R.id.tfdPrinterMinReachTime);
        lblServerHost = view.findViewById(R.id.lblServerHost);
        tfdServerHost = view.findViewById(R.id.tfdServerHost);
        lblDBHost = view.findViewById(R.id.lblDBHost);
        tfdDBHost = view.findViewById(R.id.tfdDBHost);
        lblMevWaitTime = view.findViewById(R.id.lblMevWaitTime);
        tfdMevWaitTime = view.findViewById(R.id.tfdMevWaitTime);
        lblLogoutTime = view.findViewById(R.id.lblLogoutTime);
        tfdLogoutTime = view.findViewById(R.id.tfdLogoutTime);

        cbxIsTrainingMode = view.findViewById(R.id.cbxIsTrainingMode);
        cbxIsSingleUserMode = view.findViewById(R.id.cbxIsSingleUserMode);
        cbxIsBuffetMode = view.findViewById(R.id.cbxIsBuffetMode);
        cbxIsFastFoodMode = view.findViewById(R.id.cbxIsFastFoodMode);
        cbxIsDiscoutAffectTax = view.findViewById(R.id.cbxIsDiscoutAffectTax);
        cbxIsServiceFeeAffectTax = view.findViewById(R.id.cbxIsServiceFeeAffectTax);
        cbxIsHideRecordFromOtherWaiter = view.findViewById(R.id.cbxIsHideRecordFromOtherWaiter);
        cbxIsWaiterAllowedToDiscount = view.findViewById(R.id.cbxIsWaiterAllowedToDiscount);
        cbxIsWaiterAllowedToChangePrice = view.findViewById(R.id.cbxIsWaiterAllowedToChangePrice);
        cbxShowCustomerFrame = view.findViewById(R.id.cbxShowCustomerFrame);

        moreOptions = view.findViewById(R.id.moreOptions);
        bkMenu = view.findViewById(R.id.bkMenu);
        restoreMenu = view.findViewById(R.id.restoreMenu);
    }

    private void initListeners() {
        // Add your event listeners here
        moreOptions.setOnClickListener(v -> {
            // Handle more options
        });

        bkMenu.setOnClickListener(v -> {
            dumpDB();
            Toast.makeText(getContext(), "Backup menu data", Toast.LENGTH_SHORT).show();
        });

        restoreMenu.setOnClickListener(v -> {
            restoreMenu();
            Toast.makeText(getContext(), "Restore menu data", Toast.LENGTH_SHORT).show();
        });

        tfdGST.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String gstText = tfdGST.getText().toString();
                if (isValidPercentage(gstText)) {
                    // Handle setting GST
                } else {
                    showToast("Invalid GST input");
                }
            }
        });

        // Add similar listeners for other fields
    }

    public void reLayout() {
        if (mRoot == null) {
            return;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        BarFrame.instance.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int leftWidth = (int) (screenWidth * BarOption.getMenuAreaPortion());
        int rightWidth = screenWidth - leftWidth; // 或使用 (int) (screenWidth * 0.6)

        RelativeLayout leftPanel = mRoot.findViewById(R.id.leftTopSection);
        RelativeLayout rightPanel = mRoot.findViewById(R.id.rightTopSection);

        RelativeLayout.LayoutParams leftParams = (RelativeLayout.LayoutParams) leftPanel.getLayoutParams();
        leftParams.width = leftWidth;
        leftPanel.setLayoutParams(leftParams);

        RelativeLayout.LayoutParams rightParams = (RelativeLayout.LayoutParams) rightPanel.getLayoutParams();
        rightParams.width = rightWidth;
        rightParams.addRule(RelativeLayout.RIGHT_OF, R.id.leftTopSection);
        rightPanel.setLayoutParams(rightParams);

        if (BarFrame.menuPanel.isAdded()) { // 加载 BillPanel 和 MenuPanel
            FragmentManager fragmentManager = BarFrame.menuPanel.getFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction()
                        .remove(BarFrame.menuPanel)
                        .commitNow();
            }
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.menuPanelContainer, BarFrame.menuPanel);
        transaction.commit();
    }
    private boolean isValidPercentage(String text) {
        try {
            float num = Float.parseFloat(text);
            return num >= 0 && num <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void dumpDB() {
        // Handle dumping database logic here
    }

    private void restoreMenu() {
        // Handle restoring menu logic here
    }
}
