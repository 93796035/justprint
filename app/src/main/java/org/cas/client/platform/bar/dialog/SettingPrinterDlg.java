package org.cas.client.platform.bar.dialog;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.casutil.ErrorUtil;

import java.sql.ResultSet;

public class SettingPrinterDlg extends DialogFragment {
    private long[] ids = new long[6];
    private EditText[] tfdName = new EditText[6];
    private EditText[] tfdIP = new EditText[6];
    private Spinner[] cmbStyle = new Spinner[6];
    private Spinner[] cmbLanguage = new Spinner[6];
    private EditText[] tfdWidth = new EditText[6];
    private CheckBox cbxIsDisplayBillInKitchen;
    private CheckBox cbxIsDisplayWaiterInKitchen;
    private CheckBox cbxSavePrintInvoiceWhenBilled;
    private CheckBox cbxPrintDuplicatedBill;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_printer, container, false);

        // 初始化视图
        for (int i = 0; i < 6; i++) {
            int nameId = getResources().getIdentifier("tfdName" + (i + 1+1), "id", requireActivity().getPackageName());
            int ipId = getResources().getIdentifier("tfdIP" + (i + 1), "id", requireActivity().getPackageName());
            int styleId = getResources().getIdentifier("cmbStyle" + (i + 1), "id", requireActivity().getPackageName());
            int languageId = getResources().getIdentifier("cmbLanguage" + (i + 1), "id", requireActivity().getPackageName());
            int widthId = getResources().getIdentifier("tfdWidth" + (i + 1), "id", requireActivity().getPackageName());

            tfdName[i] = dialogView.findViewById(nameId);
            tfdIP[i] = dialogView.findViewById(ipId);
            cmbStyle[i] = dialogView.findViewById(styleId);
            cmbLanguage[i] = dialogView.findViewById(languageId);
            tfdWidth[i] = dialogView.findViewById(widthId);

            ArrayAdapter<CharSequence> styleAdapter = ArrayAdapter.createFromResource(BarFrame.instance, R.array.style_array, android.R.layout.simple_spinner_item);
            styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbStyle[i].setAdapter(styleAdapter);

            ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(BarFrame.instance, R.array.language_array, android.R.layout.simple_spinner_item);
            languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbLanguage[i].setAdapter(languageAdapter);
        }

        cbxIsDisplayBillInKitchen = dialogView.findViewById(R.id.cbxIsDisplayBillInKitchen);
        cbxIsDisplayWaiterInKitchen = dialogView.findViewById(R.id.cbxIsDisplayWaiterInKitchen);
        cbxSavePrintInvoiceWhenBilled = dialogView.findViewById(R.id.cbxSavePrintInvoiceWhenBilled);
        cbxPrintDuplicatedBill = dialogView.findViewById(R.id.cbxPrintDuplicatedBill);

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // 设置初始值
        initContent();

        btnOk.setOnClickListener(v -> {
            // 保存设置的逻辑
            saveSettings();
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return dialogView;
    }

    private void initContent() {
        StringBuilder sql = new StringBuilder("select * from PRINTER where category = 0 order by _id");
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            int tRowCount = rs.getCount();
            if (tRowCount > 0) {
                int tIdx = 0;
                while (rs.moveToNext() && tIdx < 6) {
                    ids[tIdx] = DBUtil.getCursorLong(rs,"_id");
                    tfdName[tIdx].setText(DBUtil.getCursorString(rs, "NAME"));
                    tfdIP[tIdx].setText(DBUtil.getCursorString(rs, "IP"));
                    cmbStyle[tIdx].setSelection(DBUtil.getCursorInt(rs, "STYLE"));
                    cmbLanguage[tIdx].setSelection(DBUtil.getCursorInt(rs,"langType"));
                    tfdWidth[tIdx].setText(String.valueOf(DBUtil.getCursorInt(rs,"STATUS")));
                    tIdx++;
                }
            }
        }catch(Exception exp) {
            ErrorUtil.write(exp);
        }

        cbxIsDisplayBillInKitchen.setChecked(BarOption.isDisplayBillInKitchen());
        cbxIsDisplayWaiterInKitchen.setChecked(BarOption.isDisplayWaiterInKitchen());
        cbxSavePrintInvoiceWhenBilled.setChecked(BarOption.isSavePrintInvoiceWhenBilled());
        cbxPrintDuplicatedBill.setChecked(BarOption.isPrintDuplicatedBill());
    }

    private void saveSettings() {
        for (int i = 0; i < 6; i++) {
            long id = ids[i];
            String name = tfdName[i].getText().toString();
            String ip = tfdIP[i].getText().toString();
            int style = cmbStyle[i].getSelectedItemPosition();
            int language = cmbLanguage[i].getSelectedItemPosition();
            int width = Integer.parseInt(tfdWidth[i].getText().toString());

            String sql = "UPDATE PRINTER SET NAME = ?, IP = ?, STYLE = ?, langType = ?, STATUS = ? WHERE _id = ?";
            Object[] bindArgs = new Object[]{name, ip, style, language, width, id};
            AikaPos.db.execSQL(sql, bindArgs);
        }

        BarOption.setIsDisplayBillInKitchen(cbxIsDisplayBillInKitchen.isChecked());
        BarOption.setIsDisplayWaiterInKitchen(cbxIsDisplayWaiterInKitchen.isChecked());
        BarOption.setSavePrintInvoiceWhenBilled(cbxSavePrintInvoiceWhenBilled.isChecked());
        BarOption.setPrintDuplicatedBill(cbxPrintDuplicatedBill.isChecked());
    }
}
