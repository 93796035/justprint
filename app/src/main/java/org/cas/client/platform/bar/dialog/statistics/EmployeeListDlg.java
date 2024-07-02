package org.cas.client.platform.bar.dialog.statistics;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.casutil.L;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeListDlg extends DialogFragment {

    private ListView listView;
    private Button btnAdd, btnDelete, btnModify, btnPrintDoc, btnClose;
    private List<HashMap<String, String>> employeeList;
    private SimpleAdapter adapter;
    private HashMap<String, String> selectedItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_employee_list, container, false);

        listView = view.findViewById(R.id.listView);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnModify = view.findViewById(R.id.btnModify);
        btnPrintDoc = view.findViewById(R.id.btnPrintDoc);
        btnClose = view.findViewById(R.id.btnClose);

        initList();
        setupListeners();

        return view;
    }

    private void initList() {
        employeeList = new ArrayList<>();

        adapter = new SimpleAdapter(getContext(), employeeList, R.layout.employee_list_item,
                new String[]{"ID","NNAME", "LANG", "ROLE", "createdTime"},
                new int[]{R.id.txtId, R.id.txtNName, R.id.txtLang, R.id.txtRole, R.id.txtCreatedTime});
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = (HashMap<String, String>)listView.getItemAtPosition(position);

                for (int i = 0; i < parent.getChildCount(); i++) {
                    View childView = parent.getChildAt(i);
                    // 设置未选中项的背景色
                    childView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
                // 设置当前选中项的背景色
                view.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                // 处理选中项的逻辑，例如更新UI或者处理数据
            }
        });

        refreshEmployeeList();
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            // 创建一个空的参数 Map
            Map<String, String> params = new HashMap<>();
            // 调用 newInstance 方法来显示 EmployeeDlg
            EmployeeDlg.newInstance(params, this).show(BarFrame.instance.getSupportFragmentManager(), "Employee");
        });
        btnDelete.setOnClickListener(v -> {
            String employeeId = selectedItem.get("ID");
            deleteEmployeeFromDatabase(employeeId);
        });

        btnModify.setOnClickListener(v -> {
            // Handle modify employee logic
            EmployeeDlg.newInstance(selectedItem, this).show(BarFrame.instance.getSupportFragmentManager(), "Employee");
        });

        btnPrintDoc.setOnClickListener(v -> {
            // Handle print document logic
            Toast.makeText(getContext(), "Print Document", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteEmployeeFromDatabase(String employeeId) {
        JOptionPane.showConfirmDialog(BarFrame.consts.COMFIRMDELETEACTION2(), BarFrame.consts.Operator(), new JOptionPane.ConfirmDialogCallback() {
            @Override
            public void onResult(boolean confirmed) {
                if(BarFrame.instance.menuPanel.getPrinters()[0].getIp().startsWith("mev")) {
                    String sql = "select * from USER where _id = " + employeeId;
                    try {
                        Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
                        //get out every field of first matching record.
                        rs.moveToNext();
                        String name = DBUtil.getCursorString(rs, "nickName");
                        String taxAccount1 = DBUtil.getCursorString(rs, "noTPS");
                        String taxAccount2 = DBUtil.getCursorString(rs, "noTVQ");
                        if(taxAccount1.length() == 15 && taxAccount2.length() == 16) {
                            MevUtils.deleteUser(name, taxAccount1, taxAccount2);
                        }
                    }catch(Exception exp) {
                        L.e("Redeem Coupon", "exception happend when redeem coupon: " + sql, exp);
                    }
                }

                String sql = "update USER set STATUS = " + DBConsts.DELETED + " where _id = " + employeeId;
                AikaPos.db.execSQL(sql.toString());
                refreshEmployeeList();  // 更新列表显示
            }
        });
    }

    public void refreshEmployeeList() {
        // 清空列表数据
        employeeList.clear();

        // 重新加载员工数据
        String sql = "SELECT * FROM USER WHERE STATUS < " + DBConsts.DELETED;
        try {
            Cursor rs = AikaPos.db.rawQuery(sql, null);
            while (rs.moveToNext()) {
                HashMap<String, String> employee = new HashMap<>();
                employee.put("ID",  DBUtil.getCursorString(rs, "_id"));
                employee.put("NNAME", DBUtil.getCursorString(rs, "nickName"));
                employee.put("PWD", DBUtil.getCursorString(rs, "PWD"));
                employee.put("LANG", DBUtil.getCursorString(rs, "LANG"));
                employee.put("ROLE", DBUtil.getCursorString(rs, "ROLE"));
                employee.put("createdTime", DBUtil.getCursorString(rs, "createdTime"));
                employee.put("noTVQ", DBUtil.getCursorString(rs, "noTVQ"));
                employee.put("noTPS", DBUtil.getCursorString(rs, "noTPS"));
                employeeList.add(employee);
            }
            rs.close();
        } catch (Exception e) {
            L.e("EmployeeListDialogFragment", "Exception when retrieving employee data", e);
        }

        // 更新适配器
        adapter.notifyDataSetChanged();
    }

}
