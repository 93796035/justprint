package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.just.print.R;

import org.cas.client.platform.bar.dialog.TabbleSettingDlg;
import org.cas.client.platform.bar.uibeans.TableButton;

import java.util.ArrayList;
import java.util.List;

public class TableDialogFragment extends DialogFragment {
    private TabbleSettingDlg settingTableDlg;
    private List<TableButton> btnTables = new ArrayList<TableButton>();	//@NOTE: don't let the logo button inside the list.
    public static Integer[] typeAry = new Integer[]{0,  11, 12, 13, 14, 21, 22, 31, 32, 33, 34, 41, 42, 43, 44, 61,  62, 63,  64, 81, 82, 83, 84 };

    public static int[] widthAry    = new int[] {   69, 42, 69, 42, 69, 96, 42, 96, 69, 96, 69, 96, 84, 96, 96, 96, 124, 138, 96, 96, 168,168,96};
    public static int[] heightAry   = new int[] {   69, 69, 42, 69, 42, 42, 96, 69, 96, 69, 96, 84, 96, 96, 96, 124, 96, 96, 138, 168,96, 96, 168};

    public TableDialogFragment(TabbleSettingDlg pFrame, TableButton button) {
//        if(pFrame != null) {
//            settingTableDlg = pFrame;
//        }
//        if(button == null) {
//            button =  new TableButton();
//            button.setTypeStatusAndName("", "", 0, 0, "");
//            button.setId(-1);
//            btnTables.add(button);
//        }else {
//            for (TableButton tableButton : TableSettingDlg.btnTables) {
//                if (!tableButton.getBackground().equals(settingTableDlg.bg) && !tableButton.getTableName().equals("logo")) {
//                    btnTables.add(tableButton);
//                }
//            }
//        }
//        initDialog(button);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_table, null);
        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // 处理确认按钮事件
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // 处理取消按钮事件
                    }
                });
        return builder.create();
    }

    public void showDialog(FragmentManager fragmentManager) {
        show(fragmentManager, "MyDialogFragment");
    }
}
