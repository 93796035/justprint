package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.action.Cmd_CancelAll;
import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.bar.web_srm.ValuePossible;
import org.cas.client.platform.cascustomize.CustOpts;

public class MoreOptionsDlg extends DialogFragment {

    private EditText batchReportAddress, billNumberStartStr, moneySign, expiredWarningDays, serverIp;
    private CheckBox counterMode, depanneurMode, closeBatchWithUnclosedTable, displayVisaAsCredit, showProcessingBill,
            serviceFeeAffectTax, trainingMode, multiLineCellRender, noticeWhenOvertime, sameNameForbidden;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_more_options, container, false);

        // Initialize your buttons and other controls here
        Button btnCuscmd = view.findViewById(R.id.btnCuscmd);
        Button btnConfigFile = view.findViewById(R.id.btnConfigFile);
        Button btnClearAllDemoRecords = view.findViewById(R.id.btnClearAllDemoRecords);
        Button replaceCertificate = view.findViewById(R.id.replaceCertificate);
        Button deleteCertificate = view.findViewById(R.id.deleteCertificate);
        Button btnShowOffLineLogs = view.findViewById(R.id.btnShowOffLineLogs);

        batchReportAddress = view.findViewById(R.id.BatchReportAddress);
        billNumberStartStr = view.findViewById(R.id.BillNumberStartStr);
        moneySign = view.findViewById(R.id.moneySign);
        expiredWarningDays = view.findViewById(R.id.ExpiredWarningDays);
        serverIp = view.findViewById(R.id.serverIp);

        counterMode = view.findViewById(R.id.CounterMode);
        depanneurMode = view.findViewById(R.id.DepanneurMode);
        closeBatchWithUnclosedTable = view.findViewById(R.id.CloseBatchWithUnclosedTable);
        displayVisaAsCredit = view.findViewById(R.id.DisplayVisaAsCredit);
        showProcessingBill = view.findViewById(R.id.ShowProcessingBill);
        serviceFeeAffectTax = view.findViewById(R.id.serviceFeeAffectTax);
        trainingMode = view.findViewById(R.id.trainingMode);
        multiLineCellRender = view.findViewById(R.id.multiLineCellRender);
        noticeWhenOvertime = view.findViewById(R.id.noticeWhenOvertime);
        sameNameForbidden = view.findViewById(R.id.sameNameForbidden);

        // Set up listeners or bindings
        btnCuscmd.setOnClickListener(v -> {
//            setVisible(false);
//            new CmdBtnsManagerDlg().setVisible(true);
        });

        btnConfigFile.setOnClickListener(v -> {
            new ConfigEditorDlg().show(BarFrame.instance.getSupportFragmentManager(), "MoreOptionsDialog");
        });

        btnClearAllDemoRecords.setOnClickListener(v -> {
            JOptionPane.showConfirmDialog(BarFrame.consts.ConfirmDeleteAllRecords(), org.cas.client.resources.international.DlgConst.DlgTitle,
                    confirmed -> {
                        if (confirmed) {
                            org.cas.client.platform.bar.BarUtil.cleanDB();
                        }
                    });
        });

        replaceCertificate.setOnClickListener(v -> {
            new MevUtils.AskWebMevToSignTask().execute(ValuePossible.Modif.REM);
        });

        deleteCertificate.setOnClickListener(v -> {
            new MevUtils.AskWebMevToSignTask().execute(ValuePossible.Modif.SUP);
        });

        btnShowOffLineLogs.setOnClickListener(v -> {
//            OffLineLogsDlg offLineLogsDlg = new OffLineLogsDlg();
//            offLineLogsDlg.setVisible(true);
        });

        initComponent();

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("More com.just.print.app.Options (internal use only)");
        return dialog;
    }

    private void initComponent() {
        batchReportAddress.setText(BarOption.getBatchReportEmail());
        billNumberStartStr.setText(BarOption.getBillNumberStartStr());
        moneySign.setText(BarOption.getMoneySign());
        expiredWarningDays.setText(String.valueOf(CustOpts.custOps.getValue("ExpiredWarningDays")));
        serverIp.setText(BarOption.getServerIp());

        counterMode.setChecked(BarOption.isCounterMode());
        depanneurMode.setChecked(BarOption.isDepanneur());
        closeBatchWithUnclosedTable.setChecked(BarOption.CloseBatchWithUnclosedTable());
        displayVisaAsCredit.setChecked(BarOption.isCredit());
        showProcessingBill.setChecked(BarOption.isShowProcessingBill());
        serviceFeeAffectTax.setChecked(BarOption.isServiceFeeAffectTax());
        trainingMode.setChecked(BarOption.isTrainingMode());
        multiLineCellRender.setChecked(BarOption.isMultiLineCellRender());
        noticeWhenOvertime.setChecked(BarOption.needNoticeWhenOvertime());
        sameNameForbidden.setChecked(BarOption.isSameNameForbidden());

        addTextChangeListeners();
        addCheckboxChangeListeners();
    }

    private void addTextChangeListeners() {
        batchReportAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                CustOpts.custOps.setKeyAndValue("MUER", batchReportAddress.getText().toString());
            }
        });

        billNumberStartStr.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                BarOption.setBillNumberStartStr(billNumberStartStr.getText().toString());
            }
        });

        moneySign.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                BarOption.setMoneySign(moneySign.getText().toString());
            }
        });

        expiredWarningDays.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                CustOpts.custOps.setKeyAndValue("ExpiredWarningDays", expiredWarningDays.getText().toString());
            }
        });

        serverIp.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                CustOpts.custOps.setKeyAndValue("serverIp", serverIp.getText().toString());
            }
        });
    }

    private void addCheckboxChangeListeners() {
        counterMode.setOnCheckedChangeListener((buttonView, isChecked) -> BarOption.setCounterMode(isChecked));
        depanneurMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BarOption.setDepanneur(isChecked);
            if (isChecked) {
                counterMode.setChecked(true);
            }
        });
        closeBatchWithUnclosedTable.setOnCheckedChangeListener((buttonView, isChecked) -> CustOpts.custOps.setKeyAndValue("CloseBatchWithUnclosedTable", String.valueOf(isChecked)));
        displayVisaAsCredit.setOnCheckedChangeListener((buttonView, isChecked) ->
                CustOpts.custOps.setKeyAndValue("DisplayVisaAsCredit", String.valueOf(isChecked)));
        showProcessingBill.setOnCheckedChangeListener((buttonView, isChecked) ->
                CustOpts.custOps.setKeyAndValue("ShowProcessingBill", String.valueOf(isChecked)));
        serviceFeeAffectTax.setOnCheckedChangeListener((buttonView, isChecked) ->
                CustOpts.custOps.setKeyAndValue("ServiceFeeAffectTax", String.valueOf(isChecked)));
        trainingMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                CustOpts.custOps.setKeyAndValue("TrainingMode", String.valueOf(isChecked)));
        multiLineCellRender.setOnCheckedChangeListener((buttonView, isChecked) ->
                CustOpts.custOps.setKeyAndValue("multiLineCellRender", String.valueOf(isChecked)));
        noticeWhenOvertime.setOnCheckedChangeListener((buttonView, isChecked) ->
                CustOpts.custOps.setKeyAndValue("NoticeWhenOvertime", String.valueOf(isChecked)));
        sameNameForbidden.setOnCheckedChangeListener((buttonView, isChecked) ->
                CustOpts.custOps.setKeyAndValue("sameNameForbidden", String.valueOf(isChecked)));
    }
}
