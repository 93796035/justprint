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

import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.casutil.L;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EmployeeListDlg extends DialogFragment {

    private ListView listView;
    private Button btnAdd, btnDelete, btnModify, btnPrintDoc, btnClose;
    private List<HashMap<String, String>> employeeList;
    private SimpleAdapter adapter;
    private Object selectedItem;

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
        String sql = "SELECT * FROM USER WHERE STATUS < " + DBConsts.DELETED;

        try {
            Cursor rs = AikaPos.db.rawQuery(sql, null);
            while (rs.moveToNext()) {
                HashMap<String, String> employee = new HashMap<>();
                employee.put("ID",  DBUtil.getCursorString(rs, "_id"));
                employee.put("NNAME", DBUtil.getCursorString(rs, "nickName"));
                employee.put("SUBJECT", DBUtil.getCursorString(rs, "LANG"));
                employee.put("CATEGORY", DBUtil.getCursorString(rs, "ROLE"));
                employee.put("JOINTIME", DBUtil.getCursorString(rs, "createdTime"));
                employee.put("SSCNUMBER", DBUtil.getCursorString(rs, "noTVQ"));
                employee.put("BANKNUMBER", DBUtil.getCursorString(rs, "noTPS"));
                employeeList.add(employee);
            }
            rs.close();
        } catch (Exception e) {
            L.e("EmployeeListDialogFragment", "Exception when retrieving employee data", e);
        }

        adapter = new SimpleAdapter(getContext(), employeeList, R.layout.employee_list_item,
                new String[]{"ID","NNAME", "SUBJECT", "CATEGORY","JOINTIME"},
                new int[]{R.id.txtId, R.id.txtNName, R.id.txtLang, R.id.txtRole, R.id.txtCreatedTime});
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = listView.getItemAtPosition(position);

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
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            // Handle add employee logic
            Toast.makeText(getContext(), "Add Employee", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            // Handle delete employee logic
            Toast.makeText(getContext(), "Delete Employee", Toast.LENGTH_SHORT).show();
        });

        btnModify.setOnClickListener(v -> {
            // Handle modify employee logic
            Toast.makeText(getContext(), "Modify Employee", Toast.LENGTH_SHORT).show();
        });

        btnPrintDoc.setOnClickListener(v -> {
            // Handle print document logic
            Toast.makeText(getContext(), "Print Document", Toast.LENGTH_SHORT).show();
        });
    }
}
