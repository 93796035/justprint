package org.cas.client.platform.bar.dialog.statistics;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;
import com.just.print.util.StringUtils;

import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.casutil.L;

import java.util.Map;

// 添加或修改员工对话框示例
public class EmployeeDlg extends DialogFragment {

    private TextView edtId;
    private EditText edtName, edtFamilyName, edtNickName, edtPwd, edtJoinTime, edtAddress, edtNoTPS, edtNoTVQ;
    private RadioGroup radioLangGroup, radioRoleGroup;
    private RadioButton radioFr, radioEn, radioZh, radioEmployee, radioManager, radioAdmin;
    private Button btnSave;

    // 如果是编辑模式，保存当前记录的ID
    private Long currentEmployeeId;

    private EmployeeListDlg employeeListDlg;

    public static EmployeeDlg newInstance(Map<String, String> params, EmployeeListDlg employeeListDlg) {
        EmployeeDlg dialog = new EmployeeDlg();
        Bundle args = new Bundle();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            args.putString(entry.getKey(), entry.getValue());
        }

        dialog.setArguments(args);
        dialog.employeeListDlg = employeeListDlg;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_employee, container, false);

        edtId = view.findViewById(R.id.tvIdValue);
        edtName = view.findViewById(R.id.edtName);
        edtFamilyName = view.findViewById(R.id.edtFamilyName);
        edtNickName = view.findViewById(R.id.edtNName);
        edtAddress = view.findViewById(R.id.edtAddress);

        edtPwd = view.findViewById(R.id.edtPwd);
        edtJoinTime = view.findViewById(R.id.edtJoinTime);
        radioLangGroup = view.findViewById(R.id.radioLangGroup);
        radioRoleGroup = view.findViewById(R.id.radioRoleGroup);
        radioFr = view.findViewById(R.id.radioFr);
        radioEn = view.findViewById(R.id.radioEn);
        radioZh = view.findViewById(R.id.radioZh);
        radioEmployee = view.findViewById(R.id.radioEmployee);
        radioManager = view.findViewById(R.id.radioManager);
        radioAdmin = view.findViewById(R.id.radioAdmin);
        edtNoTPS = view.findViewById(R.id.edtNoTPS);
        edtNoTVQ = view.findViewById(R.id.edtNoTVQ);
        btnSave = view.findViewById(R.id.btnSave);

        // 获取传递的参数
        if (!getArguments().isEmpty()) {
            currentEmployeeId = Long.valueOf(getArguments().getString("ID"));
        }

        // 如果是编辑模式，填充现有记录的值
        if (currentEmployeeId != null) {
            fillEmployeeDetails(); // 填充现有记录的值
        }

        edtJoinTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(BarFrame.instance,
                    (DatePicker datePickerView, int yearSelected, int monthSelected, int dayOfMonthSelected) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", yearSelected, monthSelected + 1, dayOfMonthSelected);
                        edtJoinTime.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });


        btnSave.setOnClickListener(v -> {
            String pwd = edtPwd.getText().toString();
            if(pwd.length() < 4){
                JOptionPane.showMessageDialog("Password length less than 4 is not allowed", new JOptionPane.ConfirmDialogCallback() {
                    @Override
                    public void onResult(boolean confirmed) {
                        edtPwd.requestFocus();
                    }
                });
                return;
            }
            String id = edtId.getText().toString();
            String sql = "Select * from User where pwd = " + pwd + (StringUtils.isBlank(id) ? "": " and _id != " + id);
            Cursor rs= AikaPos.db.rawQuery(sql, null);
            if(rs.getCount() > 0){
                JOptionPane.showMessageDialog("Invalid Password!", new JOptionPane.ConfirmDialogCallback() {
                    @Override
                    public void onResult(boolean confirmed) {
                        edtPwd.requestFocus();
                    }
                });
                return;
            }
            String noTPS = this.edtNoTPS.getText().toString();
            if(noTPS.length() > 0 && noTPS.length() != 15){
                JOptionPane.showMessageDialog("TPS not valid!", new JOptionPane.ConfirmDialogCallback() {
                    @Override
                    public void onResult(boolean confirmed) {
                        edtNoTPS.requestFocus();
                    }
                });
                return;
            }
            String noTVQ = this.edtNoTVQ.getText().toString();
            if(noTVQ.length() > 0 && noTVQ.length() != 16){
                JOptionPane.showMessageDialog("TVQ not valid!", new JOptionPane.ConfirmDialogCallback() {
                    @Override
                    public void onResult(boolean confirmed) {
                        edtNoTVQ.requestFocus();
                    }
                });
                return;
            }
            String name = edtName.getText().toString();
            String familyName = edtFamilyName.getText().toString();
            String nickName = edtNickName.getText().toString();
            String address = edtAddress.getText().toString();
            String joinTime = edtJoinTime.getText().toString();
            int lang = getSelectedLang();
            int role = getSelectedRole();
            // 将员工信息添加到数据库中
            boolean success = addOrUpdateEmployee(currentEmployeeId, name, familyName, nickName, address, pwd, lang, role, joinTime, noTPS, noTVQ);
            if (success) {
                if (employeeListDlg != null) {
                    employeeListDlg.refreshEmployeeList();
                }
                dismiss();  // 关闭对话框
            } else {
                Toast.makeText(getContext(), currentEmployeeId != null ? "Failed to update employee" : "Failed to add employee", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fillEmployeeDetails() {
        // 根据当前的员工ID从数据库或其他存储中获取员工详细信息，并填充到界面中
        // 示例中省略从数据库获取数据的具体逻辑，直接假设填充示例数据
        edtId.setText(String.valueOf(currentEmployeeId)); // 填充ID
        edtName.setText(getArguments().get("name").toString()); // 示例：填充姓名
        edtFamilyName.setText(getArguments().get("familyName").toString());
        edtNickName.setText(getArguments().get("nickName").toString());
        edtAddress.setText(getArguments().get("address").toString());
        edtPwd.setText(getArguments().get("PWD").toString());
        edtNoTVQ.setText(getArguments().get("noTVQ").toString());
        edtNoTPS.setText(getArguments().get("noTPS").toString());
        edtJoinTime.setText(getArguments().get("joinTime").toString()); // 示例：填充加入时间
        // 假设要填充语言和角色的值，可以根据具体情况设置选中状态
        radioFr.setChecked(getArguments().get("LANG").equals("0"));
        radioEn.setChecked(getArguments().get("LANG").equals("1"));
        radioZh.setChecked(getArguments().get("LANG").equals("2"));
        radioEmployee.setChecked(getArguments().get("ROLE").equals("0")); // 示例：管理员选中
        radioManager.setChecked(getArguments().get("ROLE").equals("1")); // 示例：管理员选中g
        radioAdmin.setChecked(getArguments().get("ROLE").equals("2")); // 示例：管理员选中
        edtNoTVQ.setEnabled(edtNoTVQ.getText().length() == 0);
        edtNoTPS.setEnabled(edtNoTPS.getText().length() == 0);
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

    private boolean addOrUpdateEmployee(Long id, String name, String familyName, String nickName, String address, String pwd, int lang, int role, String joinTime, String noTPS, String noTVQ) {
        String sql = id != null ? "update USER set name = ?, familyName = ?, nickName = ?, address = ?, PWD=?, LANG=?, ROLE=?, joinTime=?, noTPS=?, noTVQ=? where _id = ?"
                : "INSERT INTO USER (name, familyName, nickName, address, PWD, LANG, ROLE, joinTime, noTPS, noTVQ, STATUS) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try {
            AikaPos.db.execSQL(sql,
                    id != null ? new Object[]{name,familyName, nickName, address, pwd, lang, role, joinTime, noTPS, noTVQ, id}
                                :new Object[]{name, familyName, nickName, address, pwd, lang, role, joinTime, noTPS, noTVQ, 0});
            if(id == null){
                if(noTPS.length() > 0 && noTVQ.length() > 0) {	//the format of gst/qst is like: "Michel Untel"/"567891234RT0001"/"5678912340TQ0001";
                    new MevUtils.AskWebMevToRegisterUserTask().execute(name, noTPS, noTVQ);
                }
            }
            return true; // 如果没有异常抛出，认为更新成功
        } catch (Exception e) {
            L.e("EmployeeDlg", "exception when calling update.", e);
            return false; // 更新失败
        }
    }
}
