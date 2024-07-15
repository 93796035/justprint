package org.cas.client.platform.bar.dialog;

import static com.just.print.app.BarFrame.menuPanel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;

import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.MenuPanel;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

public class DishDlg extends DialogFragment implements View.OnClickListener {

    private EditText tfdLanguage1, tfdLanguage2, tfdLanguage3, tfdPrice, tfdDspIndex;
    private CheckBox cbxGST, cbxQST, cbxPricePomp, cbxMenuPomp, cbxModifyPomp;
    private CheckBox[] cbxPrinters = new CheckBox[6];
    private RadioGroup rgSizes;
    private Spinner cmbCategory;
    private Button btnOk, btnCancel, btnDelete;

    private int dspIndex;
    private int prodID = -1;
    private Dish dish;
    // all the name of the menu, used to validate the new input of the name.
    // @NOTE: if the category is changed, please rember to update the nameMetrix according to the new category.
    // @NOTE: this is the model Metrix, not the onscreen metrix, it's using metrix because each dish has 3 names.

    // for initializing the status of category combobox.
    private String activeCategory;

    //private int[] categoryIdAry;
    private String[] categorySubjectAry;

    public DishDlg(int dspIndex) {
        this.dspIndex = dspIndex;
    }

    public DishDlg(Dish dish) {
        this.dish = dish;
        this.prodID = dish.getId().intValue();
        this.dspIndex = dish.getDisplayIdx();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_dish, null);

        tfdLanguage1 = view.findViewById(R.id.tfdLanguage1);
        tfdLanguage2 = view.findViewById(R.id.tfdLanguage2);
        tfdLanguage3 = view.findViewById(R.id.tfdLanguage3);
        tfdPrice = view.findViewById(R.id.tfdPrice);
        tfdDspIndex = view.findViewById(R.id.tfdDspIndex);

        cbxGST = view.findViewById(R.id.cbxGST);
        cbxQST = view.findViewById(R.id.cbxQST);
        cmbCategory = view.findViewById(R.id.cmbCategory);

        rgSizes = view.findViewById(R.id.rgSizes);
        cbxPrinters[0] = view.findViewById(R.id.cbxPrinter1);
        cbxPrinters[1] = view.findViewById(R.id.cbxPrinter2);
        cbxPrinters[2] = view.findViewById(R.id.cbxPrinter3);
        cbxPrinters[3] = view.findViewById(R.id.cbxPrinter4);
        cbxPrinters[4] = view.findViewById(R.id.cbxPrinter5);
        cbxPrinters[5] = view.findViewById(R.id.cbxPrinter6);

        cbxPricePomp = view.findViewById(R.id.cbxPricePomp);
        cbxMenuPomp = view.findViewById(R.id.cbxMenuPomp);
        cbxModifyPomp = view.findViewById(R.id.cbxModifyPomp);

        btnOk = view.findViewById(R.id.btnOk);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);

        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        builder.setView(view);
        builder.setTitle("Dish Editor");

        if (dish != null) {
            tfdLanguage1.setText(dish.getLanguage(0));
            tfdLanguage2.setText(dish.getLanguage(1));
            tfdLanguage3.setText(dish.getLanguage(2));
            tfdPrice.setText(String.valueOf(dish.getPrice() / 100.0));
            cbxGST.setChecked(dish.getGst() == 1);
            cbxQST.setChecked(dish.getQst() == 1);
            ((RadioButton) rgSizes.getChildAt(dish.getSize() < 0 ? 0 : dish.getSize())).setChecked(true);

            String[] printers = dish.getPrinterIds().split(",");
            try {
                for (String printer : printers) {
                    int printerId = Integer.parseInt(printer);
                    if (printerId > 0 && printerId <= 6) {
                        cbxPrinters[printerId - 1].setChecked(true);
                    }
                }
            }catch(Exception e){
                //in case no need to print.
            }

            cbxPricePomp.setChecked("true".equals(dish.getPricePomp()));
            cbxMenuPomp.setChecked("true".equals(dish.getMenuPomp()));
            cbxModifyPomp.setChecked("true".equals(dish.getModifyPomp()));
        }
        activeCategory = menuPanel.tgbActiveCategory.getText().toString();
        tfdDspIndex.setText(String.valueOf(dspIndex));

        initCategory();
        return builder.create();
    }

    public void initCategory() {
        try {
            // load all the categorys---------------------------
            Cursor categoryRS = AikaPos.db.rawQuery("select _id, NAME1, NAME2, NAME3 from CATEGORY  where displayIdx >= 0 order by displayIdx", null);
            int tmpPos = categoryRS.getColumnCount();
            //categoryIdAry = new int[tmpPos];
            categorySubjectAry = new String[tmpPos];

            tmpPos = 0;
            while (categoryRS.moveToNext()) {
                //categoryIdAry[tmpPos] = categoryRS.getInt("_id");
                categorySubjectAry[tmpPos] = DBUtil.getCursorString(categoryRS, "NAME1");
                tmpPos++;
            }
            categoryRS.close();// 关闭

        } catch (Exception e) {
            L.e("DishDlg", "unexpected exception when initCategory catagories:", e);
        }

        if (categorySubjectAry.length > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(BarFrame.instance, android.R.layout.simple_spinner_item, categorySubjectAry);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbCategory.setAdapter(adapter);

            // Set the active category
            if (activeCategory != null) {
                int spinnerPosition = adapter.getPosition(activeCategory);
                cmbCategory.setSelection(spinnerPosition);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOk:
                saveDish();
                break;
            case R.id.btnCancel:
                dismiss();
                break;
            case R.id.btnDelete:
                deleteDish();
                break;
        }
    }

    private void saveDish() {
        // Implement save logic using SQLiteDatabase
        // Similar to the logic in your Java Swing version
        SQLiteDatabase db = AikaPos.db;
        db.beginTransaction();
        try {
            String lang1 = tfdLanguage1.getText().toString();
            String lang2 = tfdLanguage2.getText().toString();
            String lang3 = tfdLanguage3.getText().toString();
            float price = Float.parseFloat(tfdPrice.getText().toString()) * 100;
            int gst = cbxGST.isChecked() ? 1 : 0;
            int qst = cbxQST.isChecked() ? 1 : 0;
            int size = getSelectedSize();
            String printers = getSelectedPrinters();
            int dspIndex = Integer.parseInt(tfdDspIndex.getText().toString());

            if (prodID == -1) {
                String sql = "INSERT INTO Dish (name1, name2, name3, PRICE, gst, qst,  size, printerIds, CATEGORY, displayIdx, pricePomp, menuPomp, ModifyPomp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                db.execSQL(sql, new Object[]{lang1, lang2, lang3, price, gst, qst, size, printers,
                        cmbCategory.getSelectedItem().toString(), dspIndex, cbxPricePomp.isChecked(), cbxMenuPomp.isChecked(), cbxModifyPomp.isChecked()});
            } else {
                String sql = "UPDATE dish SET name1 = ?, name2 = ?, name3 = ?, PRICE = ?, gst = ?, qst = ?, size = ?, printerIds = ?, CATEGORY = ?, displayIdx = ?, pricePomp = ?, menuPomp = ?, ModifyPomp = ? WHERE _id = ?";
                db.execSQL(sql, new Object[]{lang1, lang2, lang3, price, gst, qst, size, printers,
                        cmbCategory.getSelectedItem().toString(), dspIndex, cbxPricePomp.isChecked(), cbxMenuPomp.isChecked(), cbxModifyPomp.isChecked(), prodID});
            }

            db.setTransactionSuccessful();
            menuPanel.initCategoryAndDishes();
            dismiss();
            Toast.makeText(getActivity(), "Dish saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    private void deleteDish() {
        if (prodID == -1) return;

        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this dish?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SQLiteDatabase db = AikaPos.db;
                    db.beginTransaction();
                    try {
                        String sql = "DELETE FROM Dish WHERE _id = ?";
                        db.execSQL(sql, new Object[]{prodID});
                        db.setTransactionSuccessful();
                        Toast.makeText(getActivity(), "Dish deleted", Toast.LENGTH_SHORT).show();
                        menuPanel.initCategoryAndDishes();
                        dismiss();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        db.endTransaction();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private int getSelectedSize() {
        int selectedId = rgSizes.getCheckedRadioButtonId();
        View radioButton = rgSizes.findViewById(selectedId);
        return rgSizes.indexOfChild(radioButton);
    }

    private String getSelectedPrinters() {
        StringBuilder selectedPrinters = new StringBuilder();
        for (int i = 0; i < cbxPrinters.length; i++) {
            if (cbxPrinters[i].isChecked()) {
                selectedPrinters.append(i + 1).append(",");
            }
        }
        if (selectedPrinters.length() > 0) {
            selectedPrinters.deleteCharAt(selectedPrinters.length() - 1);
        }
        return selectedPrinters.toString();
    }
}
