package org.cas.client.platform.bar.dialog.statistics;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.SamDialog;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.casutil.L;

public class OperationLogDlg extends SamDialog {

    private ScrollView scrollView;
    private LinearLayout scrollableLayout;
    private TextView selectedLogView;

    private Button btnClose;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initComponents();
        relayout();
        initContent();
        return dialog;
    }

    private void initComponents() {
        // 初始化组件
        btnClose = createButton("Close");
        btnClose.setOnClickListener(this);

        scrollView = new ScrollView(getContext());
        scrollableLayout = new LinearLayout(getContext());
        scrollableLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(scrollableLayout);
    }

    private void relayout() {
        LinearLayout rootLayout = new LinearLayout(getContext());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        dialog.setContentView(rootLayout);

        rootLayout.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f)); // Weight 1 for scrollable content

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.gravity = Gravity.END;
        rootLayout.addView(btnClose, btnParams);
    }

    private void initContent() {
        SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
        String sql = "SELECT * FROM operationlog, user WHERE operationlog.userId = user._id AND opentime = '"
                + BarFrame.instance.valOpenTime.getText() + "' AND BILLIDX = '"
                + BarFrame.instance.getCurBillIndex() + "' AND tableName = '"
                + BarFrame.instance.getCurTable() + "'";

        fillTableAreaWithResultSet(sql);
    }

    private void fillTableAreaWithResultSet(String sql) {
        scrollableLayout.removeAllViews();
        selectedLogView = null; // 重置选中的行

        Cursor cursor = AikaPos.db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            String employeeName = DBUtil.getCursorString(cursor, "employee.nName");
            String operationType = DBUtil.getCursorInt(cursor, "OperationType") == 1 ? "add" : "remove";
            String dishName = DBUtil.getCursorString(cursor, "dishName");
            String operationTime = DBUtil.getCursorString(cursor, "OPERATIONTIME");
            double dishPrice = DBUtil.getCursorInt(cursor, "dishprice") / 100.0;

            // 处理status
            int status = DBUtil.getCursorInt(cursor, "billstatus");
            String statusText;
            switch (status) {
                case DBConsts.ORIGINAL:
                    statusText = "Original";
                    break;
                case DBConsts.SAVED:
                    statusText = "Saved";
                    break;
                case DBConsts.BILL_PRINTED:
                    statusText = "Billed";
                    break;
                case DBConsts.SUSPENDED:
                    statusText = "Suspended";
                    break;
                case DBConsts.COMPLETED:
                    statusText = "Paid";
                    break;
                case DBConsts.VOIDED:
                    statusText = "Void";
                    break;
                case DBConsts.EXPIRED:
                    statusText = "Expired";
                    break;
                case DBConsts.DELETED:
                    statusText = "Deleted";
                    break;
                default:
                    if (status < 0 && status + DBUtil.getCursorInt(cursor, "total") == 0) {
                        statusText = "refund";
                    } else {
                        statusText = BarUtil.formatMoney(status / 100.0);
                    }
                    break;
            }

            // 创建 OperationLogData 对象
            OperationLogData logData = new OperationLogData(employeeName, operationType, dishName, statusText, operationTime, dishPrice);

            // 构建显示字符串
            String logInfo = String.format(" %s  %s  %s  %s  %s  %s",
                    employeeName, operationType, dishName, statusText, operationTime, BarUtil.formatMoney(dishPrice));

            // 创建 TextView 并设置数据
            TextView logView = createLabel(logInfo, Gravity.START);
            logView.setTextColor(Color.BLACK);
            logView.setTag(logData); // 绑定 OperationLogData 数据

            // 设置点击事件
            logView.setOnClickListener(v -> onLogSelected((TextView) v));

            // 将 TextView 添加到布局中
            scrollableLayout.addView(logView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // 添加分隔线
            View divider = new View(getContext());
            divider.setBackgroundColor(Color.GRAY);
            LinearLayout.LayoutParams dividerLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            dividerLayoutParams.setMargins(0, 20, 0, 20);
            scrollableLayout.addView(divider, dividerLayoutParams);
        }

        cursor.close();
    }

    private void onLogSelected(TextView logView) {
        if (selectedLogView != null) {
            selectedLogView.setBackgroundColor(Color.TRANSPARENT);
        }
        logView.setBackgroundColor(Color.LTGRAY);
        selectedLogView = logView;

        OperationLogData selectedLogData = (OperationLogData) logView.getTag();
        showLogDetails(selectedLogData);
    }

    private void showLogDetails(OperationLogData logData) {
        // 这里你可以实现显示日志详细信息的逻辑
        // 比如可以在 SalesPanel 中展示或者弹出新窗口
        L.i("OperationLogDlg", "Selected Log: " + logData.toString());
    }

    class OperationLogData {
        String employeeName;
        String operationType;
        String dishName;
        String statusText;
        String operationTime;
        double dishPrice;

        OperationLogData(String employeeName, String operationType, String dishName, String statusText, String operationTime, double dishPrice) {
            this.employeeName = employeeName;
            this.operationType = operationType;
            this.dishName = dishName;
            this.statusText = statusText;
            this.operationTime = operationTime;
            this.dishPrice = dishPrice;
        }

        @Override
        public String toString() {
            return String.format("Employee: %s, Operation: %s, Dish: %s, Status: %s, Time: %s, Price: %.2f",
                    employeeName, operationType, dishName, statusText, operationTime, dishPrice);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnClose) {
            dialog.dismiss();
        }
    }
}
