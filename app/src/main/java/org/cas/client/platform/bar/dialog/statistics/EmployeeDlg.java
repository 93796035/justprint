package org.cas.client.platform.bar.dialog.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;

// 添加员工对话框示例
public class EmployeeDlg extends DialogFragment {

    private EditText edtId, edtName, edtJoinTime;
    private RadioGroup radioLangGroup, radioRoleGroup;
    private RadioButton radioFr, radioEn, radioZh, radioEmployee, radioManager, radioAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_employee, container, false);

        edtId = view.findViewById(R.id.edtId);
        edtName = view.findViewById(R.id.edtName);
        edtJoinTime = view.findViewById(R.id.edtJoinTime);
        radioLangGroup = view.findViewById(R.id.radioLangGroup);
        radioRoleGroup = view.findViewById(R.id.radioRoleGroup);
        radioFr = view.findViewById(R.id.radioFr);
        radioEn = view.findViewById(R.id.radioEn);
        radioZh = view.findViewById(R.id.radioZh);
        radioEmployee = view.findViewById(R.id.radioEmployee);
        radioManager = view.findViewById(R.id.radioManager);
        radioAdmin = view.findViewById(R.id.radioAdmin);

        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            // 获取输入的员工信息
            String id = ""; // 不从界面获取ID，因为新建时ID由系统分配
            String name = edtName.getText().toString();
            String joinTime = edtJoinTime.getText().toString();
            int lang = getSelectedLang();
            int role = getSelectedRole();

            // 将员工信息添加到数据库中
            boolean success = addEmployee(id, name, lang, role, joinTime);
            if (success) {
                // 刷新员工列表
                refreshEmployeeList();
                dismiss();  // 关闭对话框
            } else {
                Toast.makeText(getContext(), "Failed to add employee", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private int getSelectedLang() {
        int lang = -1;
        switch (radioLangGroup.getCheckedRadioButtonId()) {
            case R.id.radioFr:
                lang = 0;
                break;
            case R.id.radioEn:
                lang = 1;
                break;
            case R.id.radioZh:
                lang = 2;
                break;
        }
        return lang;
    }

    private int getSelectedRole() {
        int role = -1;
        switch (radioRoleGroup.getCheckedRadioButtonId()) {
            case R.id.radioEmployee:
                role = 0;
                break;
            case R.id.radioManager:
                role = 1;
                break;
            case R.id.radioAdmin:
                role = 2;
                break;
        }
        return role;
    }

    private boolean addEmployee(String id, String name, int lang, int role, String joinTime) {
        // 将员工信息添加到数据库中的逻辑
        // 返回添加是否成功的结果
        return true;  // 示例中默认返回 true
    }

    private void refreshEmployeeList() {
        // 刷新员工列表的逻辑
        // 更新 employeeList 和调用 adapter.notifyDataSetChanged()
    }
}
