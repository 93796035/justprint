package com.just.print.ui.panel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.just.print.R;

import org.cas.client.platform.bar.BarOption;

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
        super(BarOption.getSalePanelBK());
        MAX_CMDBTN_QT = 10;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_panel, container, false);

        initView(view);
        initListeners();

        return view;
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
