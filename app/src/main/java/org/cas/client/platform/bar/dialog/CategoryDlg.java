package org.cas.client.platform.bar.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.panel.MenuPanel;
import org.cas.client.platform.bar.uibeans.CategoryToggleButton;
import org.cas.client.platform.casutil.L;

public class CategoryDlg extends DialogFragment {

    private EditText tfdDspIndex, tfdCategoryName1, tfdCategoryName2, tfdCategoryName3;
    private Button btnOk, btnCancel, btnDelete;

    private int dspIndex;
    private String lang1;
    private CategoryToggleButton sourceButton;
    private MenuPanel menuPanel;

    public CategoryDlg(MenuPanel menuPanel) {
        this.menuPanel = menuPanel;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_category_dialog, null);

        tfdDspIndex = view.findViewById(R.id.tfdDspIndexCategory);
        tfdCategoryName1 = view.findViewById(R.id.tfdCategoryName1);
        tfdCategoryName2 = view.findViewById(R.id.tfdCategoryName2);
        tfdCategoryName3 = view.findViewById(R.id.tfdCategoryName3);

        btnOk = view.findViewById(R.id.btnOk);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);

        btnOk.setOnClickListener(v -> onOkClick());
        btnCancel.setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> onDeleteClick());

        tfdDspIndex.setText(String.valueOf(dspIndex));
        if (dspIndex <= menuPanel.categoryNameMetrix[0].length) {
            tfdCategoryName1.setText(BarUtil.removeHTMLTAG(menuPanel.categoryNameMetrix[0][dspIndex - 1]));
            lang1 = menuPanel.categoryNameMetrix[0][dspIndex - 1];
            tfdCategoryName2.setText(BarUtil.removeHTMLTAG(menuPanel.categoryNameMetrix[1][dspIndex - 1]));
            tfdCategoryName3.setText(BarUtil.removeHTMLTAG(menuPanel.categoryNameMetrix[2][dspIndex - 1]));
        }
        builder.setView(view);
        builder.setTitle("Category Editor");

        return builder.create();
    }

    public void setCategoryBtn(CategoryToggleButton btn) {
        this.sourceButton = btn;
        this.dspIndex = btn.getIndex();
    }

    private void onOkClick() {

        String text = BarUtil.addHTMLTAG(tfdCategoryName1.getText().toString());

        if (text == null || text.length() < 1) {
            Toast.makeText(getActivity(), "Invalid Input", Toast.LENGTH_SHORT).show();
            tfdCategoryName1.requestFocus();
            return;
        }

        if (isCategoryNameModified(0)) {
            for (int i = 0; i < menuPanel.categoryNameMetrix[0].length; i++) {
                if (i != dspIndex - 1 && text.equalsIgnoreCase(menuPanel.categoryNameMetrix[0][i])) {
                    Toast.makeText(getActivity(), "Duplicated Input", Toast.LENGTH_SHORT).show();
                    tfdCategoryName1.requestFocus();
                    return;
                }
            }
        }

        // similar checks for other category names...

        try {
            int newIndex = Integer.valueOf(tfdDspIndex.getText().toString());

            // adjust affected category's Index
            if (newIndex > dspIndex) {
                for (int i = dspIndex + 1; i <= newIndex; i++) {
                    String sql = "UPDATE Category SET displayIdx = " + (i - 1) + " where displayIdx = " + i;
                    AikaPos.db.execSQL(sql);
                }
            } else if (newIndex < dspIndex) {
                for (int i = dspIndex - 1; i >= newIndex; i--) {
                    String sql = "UPDATE Category SET displayIdx = " + (i + 1) + " where displayIdx = " + i;
                    AikaPos.db.execSQL(sql);
                }
            }

            // Save to DB
            if (dspIndex <= menuPanel.categoryNameMetrix[0].length && menuPanel.categoryNameMetrix[0][dspIndex - 1] != null) {
                String sql = "UPDATE Category SET NAME1 = ?, NAME2 = ?, NAME3 = ?, displayIdx = ? where NAME1 = ?";
                AikaPos.db.execSQL(sql, new Object[]{
                        BarUtil.addHTMLTAG(tfdCategoryName1.getText().toString()),
                        BarUtil.addHTMLTAG(tfdCategoryName2.getText().toString()),
                        BarUtil.addHTMLTAG(tfdCategoryName3.getText().toString()),
                        tfdDspIndex.getText().toString(),
                        menuPanel.categoryNameMetrix[0][dspIndex - 1]});

                if (!tfdCategoryName1.getText().toString().equals(lang1)) {
                    sql = "update Product set category = ? where category = '" + lang1 + "'";
                    AikaPos.db.execSQL(sql, new Object[]{tfdCategoryName1.getText().toString()});
                }
            } else {
                String sql = "INSERT INTO Category(NAME1, NAME2, NAME3, displayIdx) VALUES(?,?,?,?)";
                AikaPos.db.execSQL(sql, new Object[]{BarUtil.addHTMLTAG(tfdCategoryName1.getText().toString()),
                        BarUtil.addHTMLTAG(tfdCategoryName2.getText().toString()),
                        BarUtil.addHTMLTAG(tfdCategoryName3.getText().toString()),
                        tfdDspIndex.getText().toString()});
            }

            sourceButton.setText(tfdCategoryName1.getText().toString());
            menuPanel.initCategoryAndDishes();
            dismiss();
        } catch (Exception e) {
            L.e("CategoryDlg", "unexpected exception when saving category to db." + e.getMessage(), e);
        }
    }

    private void onDeleteClick() {
        String category = menuPanel.categoryNameMetrix[0][dspIndex - 1];
        if (category == null) return;

        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    try {
                        String sql = "delete from product where CATEGORY = ?";
                        AikaPos.db.execSQL(sql, new Object[]{category});

                        sql = "delete from Category where NAME1 = ?";
                        AikaPos.db.execSQL(sql, new Object[]{category});

                        menuPanel.initCategoryAndDishes();
                        dismiss();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private boolean isCategoryNameModified(int lang) {
        boolean isNotInitYet = dspIndex - 1 >= menuPanel.categoryNameMetrix[lang].length;
        String oldText = isNotInitYet ? null : menuPanel.categoryNameMetrix[lang][dspIndex - 1];
        boolean isEmptyBefore = oldText == null || oldText.length() == 0;
        String newText = tfdCategoryName1.getText().toString();
        boolean isEmptyNow = newText == null || newText.length() == 0;

        if ((isNotInitYet || isEmptyBefore) && isEmptyNow) {
            return false;
        } else if ((isNotInitYet || isEmptyBefore) && !isEmptyNow) {
            return true;
        } else if (isEmptyNow) {
            return true;
        } else {
            return !newText.equals(oldText);
        }
    }
}