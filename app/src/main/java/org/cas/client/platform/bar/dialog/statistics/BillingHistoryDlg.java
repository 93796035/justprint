package org.cas.client.platform.bar.dialog.statistics;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.SamDialog;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.util.Date;

public class BillingHistoryDlg extends SamDialog {
    private TextView colBillId, colOpenTime, colCustomer, colTable, colStatus, colTotal;
    private TextView lblBillID, lblYearFrom, lblMonthFrom, lblDayFrom, lblYearTo, lblMonthTo, lblDayTo, lblTo;
    private EditText tfdBillID, tfdYearFrom, tfdMonthFrom, tfdDayFrom, tfdYearTo, tfdMonthTo, tfdDayTo;
    private Button btnChangeDate, btnPrintInvoice;
    private ScrollView scrollView;
    private LinearLayout scrollableLayout; // 用于放入 ScrollView 的布局

    private TextView selectedBillView; // 用于保存当前选中的行

    // 用于检测三次点击的变量
    private static final long TRIPLE_CLICK_TIME_INTERVAL = 500; // 500ms 内三次点击判定为三击
    private int clickCount = 0;
    private Handler handler = new Handler();
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initComponents();
        relayout();
        initContent();
        return dialog;
    }

    private void initComponents() {
        colBillId = createLabel("Bill", Gravity.CENTER);
        colOpenTime = createLabel("Open Time", Gravity.CENTER);
        colCustomer = createLabel("Customer", Gravity.CENTER);
        colTable = createLabel("Table", Gravity.CENTER);
        colStatus = createLabel("Status", Gravity.CENTER);
        colTotal = createLabel("Total", Gravity.CENTER);
        // 创建时间设置组件
        lblBillID = createLabel("Bill ID:", Gravity.CENTER);
        lblYearFrom = createLabel("YYYY", Gravity.CENTER);
        lblMonthFrom = createLabel("MM", Gravity.CENTER);
        lblDayFrom = createLabel("DD", Gravity.CENTER);
        lblYearTo = createLabel("YYYY", Gravity.CENTER);
        lblMonthTo = createLabel("MM", Gravity.CENTER);
        lblDayTo = createLabel("DD", Gravity.CENTER);
        lblTo = createLabel("To:", Gravity.CENTER);

        tfdBillID = createEditText();
        tfdYearFrom = createEditText();
        tfdMonthFrom = createEditText();
        tfdDayFrom = createEditText();
        tfdYearTo = createEditText();
        tfdMonthTo = createEditText();
        tfdDayTo = createEditText();

        btnChangeDate = createButton("Apply");
        btnPrintInvoice = createButton("Print");

        btnChangeDate.setOnClickListener(this);
        btnPrintInvoice.setOnClickListener(this);

        // 初始化 ScrollView 和固定布局
        scrollView = new ScrollView(getContext());
        scrollableLayout = new LinearLayout(getContext());
        scrollableLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.setBackgroundColor(Color.WHITE);
        scrollView.addView(scrollableLayout);
    }

    private void relayout() {
        FrameLayout rootLayout = new FrameLayout(getContext());
        dialog.setContentView(rootLayout);

        rootLayout.addView(colBillId, getLayout(colBillId, 50, 50,
                100, 50));
        rootLayout.addView(colOpenTime, getLayout(colOpenTime, colBillId.getTop(), colBillId.getLeft() + colBillId.getMinimumWidth(),
                350, colBillId.getMinimumHeight()));
        rootLayout.addView(colCustomer, getLayout(colCustomer, colBillId.getTop(), colOpenTime.getLeft() + colOpenTime.getMinimumWidth(),
                150, colBillId.getMinimumHeight()));
        rootLayout.addView(colTable, getLayout(colTable, colBillId.getTop(), colCustomer.getLeft() + colCustomer.getMinimumWidth(),
                100, colBillId.getMinimumHeight()));
        rootLayout.addView(colStatus, getLayout(colStatus, colBillId.getTop(), colTable.getLeft() + colTable.getMinimumWidth(),
                100, colBillId.getMinimumHeight()));
        rootLayout.addView(colTotal, getLayout(colTotal, colBillId.getTop(),colStatus.getLeft() + colStatus.getMinimumWidth(),
                150, colBillId.getMinimumHeight()));

        rootLayout.addView(scrollView, getLayout(scrollView, colBillId.getTop() + colBillId.getMinimumHeight(), colBillId.getLeft(),
                1020, 800));

        rootLayout.addView(lblBillID, getLayout(lblBillID, scrollView.getTop() + scrollView.getMinimumHeight() + CustOpts.VER_GAP, scrollView.getLeft(),
                LABEL_WIDTH + CustOpts.HOR_GAP * 2, VALUE_HEIGHT));
        rootLayout.addView(tfdBillID, getLayout(tfdBillID, lblBillID.getTop() + lblBillID.getMinimumHeight() - 20, lblBillID.getLeft(),
                lblBillID.getMinimumWidth(), VALUE_HEIGHT));

        View verticalLine = new View(BarFrame.instance);
        verticalLine.setBackgroundColor(Color.WHITE); // 设置竖线的颜色
        rootLayout.addView(verticalLine, getLayout(verticalLine, lblBillID.getTop() + 24 , lblBillID.getLeft() + lblBillID.getMinimumWidth() + CustOpts.HOR_GAP, 1, BUTTON_HEIGHT));

        rootLayout.addView(lblYearFrom, getLayout(lblYearFrom, lblBillID.getTop(), verticalLine.getLeft() + verticalLine.getMinimumWidth() + CustOpts.HOR_GAP,
                LABEL_WIDTH, VALUE_HEIGHT));
        rootLayout.addView(tfdYearFrom, getLayout(tfdYearFrom, tfdBillID.getTop(), lblYearFrom.getLeft(), lblYearFrom.getMinimumWidth(), VALUE_HEIGHT));

        rootLayout.addView(lblMonthFrom, getLayout(lblMonthFrom, lblBillID.getTop(), lblYearFrom.getLeft() + lblYearFrom.getMinimumWidth() + CustOpts.HOR_GAP,
                LABEL_WIDTH - CustOpts.HOR_GAP * 4, VALUE_HEIGHT));
        rootLayout.addView(tfdMonthFrom, getLayout(tfdMonthFrom, tfdBillID.getTop(), lblMonthFrom.getLeft(),
                lblMonthFrom.getMinimumWidth(), VALUE_HEIGHT));

        rootLayout.addView(lblDayFrom, getLayout(lblDayFrom, lblBillID.getTop(), lblMonthFrom.getLeft() + lblMonthFrom.getMinimumWidth() + CustOpts.HOR_GAP,
                lblMonthFrom.getMinimumWidth(), VALUE_HEIGHT));
        rootLayout.addView(tfdDayFrom, getLayout(tfdDayFrom, tfdBillID.getTop(), lblDayFrom.getLeft(),
                lblDayFrom.getMinimumWidth(), VALUE_HEIGHT));

        rootLayout.addView(lblTo, getLayout(lblTo, tfdDayFrom.getTop(), lblDayFrom.getLeft() + lblDayFrom.getMinimumWidth() + CustOpts.HOR_GAP,
                40, VALUE_HEIGHT ));

        rootLayout.addView(lblYearTo, getLayout(lblYearTo, lblBillID.getTop(), lblTo.getLeft() + lblTo.getMinimumWidth() + CustOpts.HOR_GAP,
                lblYearFrom.getMinimumWidth(), VALUE_HEIGHT));
        rootLayout.addView(tfdYearTo, getLayout(tfdYearTo, tfdBillID.getTop(), lblYearTo.getLeft(),
                lblYearTo.getMinimumWidth(), VALUE_HEIGHT));

        rootLayout.addView(lblMonthTo, getLayout(lblMonthTo, lblBillID.getTop(), lblYearTo.getLeft() + lblYearTo.getMinimumWidth() + CustOpts.HOR_GAP,
                LABEL_WIDTH, VALUE_HEIGHT));
        rootLayout.addView(tfdMonthTo, getLayout(tfdMonthTo, tfdBillID.getTop(), lblMonthTo.getLeft(),
                LABEL_WIDTH, VALUE_HEIGHT));

        rootLayout.addView(lblDayTo, getLayout(lblDayTo, lblBillID.getTop(), lblMonthTo.getLeft() + lblMonthTo.getMinimumWidth() + CustOpts.HOR_GAP,
                LABEL_WIDTH, VALUE_HEIGHT));
        rootLayout.addView(tfdDayTo, getLayout(tfdDayTo, tfdBillID.getTop(), lblDayTo.getLeft(),
                LABEL_WIDTH, VALUE_HEIGHT));

        View verticalLine2 = new View(BarFrame.instance);
        verticalLine2.setBackgroundColor(Color.WHITE); // 设置竖线的颜色
        rootLayout.addView(verticalLine2, getLayout(verticalLine2, lblBillID.getTop() + 24 , tfdDayTo.getLeft() + tfdDayTo.getMinimumWidth() + CustOpts.HOR_GAP, 1, BUTTON_HEIGHT));

        // 将按钮添加到滚动区域的底部
        rootLayout.addView(btnChangeDate, getLayout(btnChangeDate, verticalLine2.getTop(), verticalLine2.getLeft() + verticalLine2.getMinimumWidth() + CustOpts.HOR_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT));
        rootLayout.addView(btnPrintInvoice, getLayout(btnPrintInvoice,  btnChangeDate.getTop(), btnChangeDate.getLeft() + btnChangeDate.getMinimumWidth() + CustOpts.HOR_GAP,
                BUTTON_WIDTH, btnChangeDate.getMinimumHeight()));

        FrameLayout.LayoutParams params =  (FrameLayout.LayoutParams) btnPrintInvoice.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin, 50, 50);
    }

    @Override
    public void onClick(View v) {
        if (v == btnChangeDate) {
            onApply();
        } else if (v == btnPrintInvoice) {
            onPrint();
        }
    }

    private void onApply() {
        String billId = tfdBillID.getText().toString();
        if(billId.length() > 0){
            initContent(billId);
        }else {
            String startDate = tfdYearFrom.getText().toString() + "-" +
                    tfdMonthFrom.getText().toString() + "-" +
                    tfdDayFrom.getText().toString() + " 00:00:00";

            String endDate = tfdYearTo.getText().toString() + "-" +
                    tfdMonthTo.getText().toString() + "-" +
                    tfdDayTo.getText().toString() + " 23:59:59";

            // 调用数据库查询并更新界面
            initContent(startDate, endDate);
        }
    }

    private void onPrint() {
        if(selectedBillView != null) {
            BillData selectedBillData = (BillData) selectedBillView.getTag();
            try {
                int billId = selectedBillData.billId;
                // 示例打印逻辑
                BillPanel billPanel = ((SalesPanel)BarFrame.instance.panels[2]).billPanel;
                boolean isToCustomer = false;
                if(isToCustomer) {
                    billPanel.expireAndReGenerateBill(null);
                    //if ckxIsToCustomer not selected, the printReceipt function will know it's a internal bill. while if it's selected,
                    //then printReceipt will not sure if it's a original print or reprint. so need to give the information out here.
                    billPanel.setStatus(DBConsts.COMPLETED);
                }
                PrintService.printReceipt(billPanel, false, isToCustomer, true);

            } catch (NumberFormatException e) {
                L.e("BillingHistory", "Unexpected NumberFormatException! ", e);
            }
        }
    }

    private void initContent() {
        String endNow = BarUtil.df.format(new Date());
        int p = endNow.indexOf(" ");
        String startTime = endNow.substring(0, p + 1) + BarOption.getStartTime();
        initContent(startTime, endNow);
    }

    private void initContent(String billId){
        StringBuilder sql = new StringBuilder("SELECT bill._id AS bill_id, user._id AS user_id, bill.createdTime," +
                "bill.billIndex, bill.tableName, bill.openTime, bill.STATUS, bill.TOTAL, bill.DISCOUNT from bill, user where bill.userId = user._id and bill._id = ")
                .append(billId);

        // 如果配置了，则不显示其他服务员的记录
        if (!BarOption.showRecordFromOtherWaiter() && LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS) {
            sql.append(" and user._id = ").append(LoginDlg.USERID);
        }

        // 如果是培训模式，不显示相关记录
        if (!"F".equals(LoginDlg.MODETRANS)) {
            sql.append(" and bill.comment != 'for training'");
        }

        fillTableAreaWithResultSet(sql.toString());
    }

    private void initContent(String startTime, String endTime) {
        StringBuilder sql = new StringBuilder("SELECT bill._id AS bill_id, user._id AS user_id, bill.createdTime," +
                "bill.billIndex, bill.tableName, bill.openTime, bill.STATUS, bill.TOTAL, bill.DISCOUNT from bill, user where bill.userId = user._id and createdTime >= '")
                .append(startTime)
                .append("' and createdTime <= '")
                .append(endTime)
                .append("' and (bill.status < ")
                .append((BarOption.showExpiredRecordOnlyToAdmin() && LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS)
                        ? DBConsts.EXPIRED : DBConsts.DELETED)
                .append(" or bill.status is null)");

        // 如果没有设置显示处理中的账单，则仅显示已完成（已支付）的账单
        if (!BarOption.isShowProcessingBill()) {
            sql.append(" and bill.status is not null ")
                    .append(" and (bill.status <= -10 or bill.status > ")
                    .append(DBConsts.BILL_PRINTED)
                    .append(")");
        }

        // 如果配置了，则不显示其他服务员的记录
        if (!BarOption.showRecordFromOtherWaiter() && LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS) {
            sql.append(" and user._id = ").append(LoginDlg.USERID);
        }

        // 如果是培训模式，不显示相关记录
        if (!"F".equals(LoginDlg.MODETRANS)) {
            sql.append(" and bill.comment != 'for training'");
        }

        sql.append(" order by bill_id");
        fillTableAreaWithResultSet(sql.toString());

        // 填充日期选择区域
        int p = startTime.indexOf(" ");
        String dateOnlyStart = startTime.substring(0, p);
        setDateFields(dateOnlyStart, tfdYearFrom, tfdMonthFrom, tfdDayFrom);

        p = endTime.indexOf(" ");
        String dateOnlyEnd = endTime.substring(0, p);
        setDateFields(dateOnlyEnd, tfdYearTo, tfdMonthTo, tfdDayTo);
    }

    private void setDateFields(String date, EditText yearField, EditText monthField, EditText dayField) {
        int p = date.indexOf("-");
        yearField.setText(date.substring(0, p));
        date = date.substring(p + 1);
        p = date.indexOf("-");
        monthField.setText(date.substring(0, p));
        dayField.setText(date.substring(p + 1));
    }

    private void fillTableAreaWithResultSet(String sql) {
        // 清空现有内容
        scrollableLayout.removeAllViews();
        selectedBillView = null; // 清空之前选中的行

        // 执行SQL查询
        Cursor cursor = AikaPos.db.rawQuery(sql, null);

        // 动态加载查询结果
        while (cursor.moveToNext()) {
            StringBuilder billInfo = new StringBuilder();

            // 获取需要的字段值
            int status = DBUtil.getCursorInt(cursor, "STATUS");
            int total = DBUtil.getCursorInt(cursor, "TOTAL");
            int billId = DBUtil.getCursorInt(cursor, "bill_id");
            int billIdx = DBUtil.getCursorInt(cursor, "billIndex");
            String openTime = DBUtil.getCursorString(cursor, "openTime");
            String operator = DBUtil.getCursorString(cursor, "operator");
            int customerId = DBUtil.getCursorInt(cursor, "user_id"); // 示例，假设 user_id 即为 customerId
            String tableName = DBUtil.getCursorString(cursor, "tableName");

            String statusText;
            switch (status) {
                case DBConsts.ORIGINAL:
                    statusText = "to pay";
                    break;
                case DBConsts.BILL_PRINTED:
                    statusText = "billed";
                    break;
                case DBConsts.SUSPENDED:
                    statusText = "suspended";
                    break;
                case DBConsts.COMPLETED:
                    statusText = "paid";
                    break;
                case DBConsts.VOIDED:
                    statusText = "void";
                    break;
                case DBConsts.EXPIRED:
                    statusText = "expired";
                    break;
                case DBConsts.DELETED:
                    statusText = "deleted";
                    break;
                default:
                    if (status < 0 && status + total == 0) {
                        statusText = "refund";
                    } else {
                        statusText = BarUtil.formatMoney(status / 100.0);
                    }
                    break;
            }
            // 创建 BillData 对象
            BillData billData = new BillData(billId, billIdx, operator, openTime, statusText, customerId, tableName);

            // 构建 billInfo 字符串
            billInfo.append("    ").append(billId)
                    .append("              ").append(openTime)
                    .append("              ").append(billIdx)
                    .append("              ").append(tableName)
                    .append("            ").append(statusText)  // 使用 statusText 代替原始 STATUS
                    .append("            ").append(BarUtil.formatMoney(total / 100.0));

            // 为每个账单创建一个TextView，并添加到 scrollableLayout 中
            TextView billView = createLabel(billInfo.toString(), Gravity.START);
            billView.setTextColor(Color.BLACK);
            billView.setTag(billData); // 将 BillData 对象作为标签存储到 TextView 中

            // 设置点击事件处理程序
            billView.setOnClickListener(v -> onBillSelected((TextView) v));

            scrollableLayout.addView(billView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // 添加一个分隔线，增强视觉效果
            View divider = new View(getContext());
            divider.setBackgroundColor(Color.GRAY);
            LinearLayout.LayoutParams dividerLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            dividerLayoutParams.setMargins(0, 20, 0, 20);
            scrollableLayout.addView(divider, dividerLayoutParams);
        }

        cursor.close();
    }

    private void onBillSelected(TextView billView) {
        if (selectedBillView != null) {
            // 取消之前选中的行的高亮
            selectedBillView.setBackgroundColor(Color.TRANSPARENT);
        }

        // 高亮当前选中的行
        billView.setBackgroundColor(Color.LTGRAY);
        selectedBillView = billView; // 更新当前选中的行

        // 获取选中行的 BillData 对象
        BillData selectedBillData = (BillData) billView.getTag();

        // 检测三次点击
        clickCount++;
        handler.postDelayed(() -> {
            if (clickCount == 3) {
                new OperationLogDlg().show(getChildFragmentManager(), "operationLogDlg");
            }
            clickCount = 0; // 重置点击次数
        }, TRIPLE_CLICK_TIME_INTERVAL);

        // 触发相关事件或操作
        showInSalesPanel(selectedBillData);
    }

    private boolean showInSalesPanel(BillData billData) {
        // 使用 BillData 对象中的信息
        BarFrame.instance.setCurBillIdx(String.valueOf(billData.billIdx));
        BarFrame.instance.valOperator.setText(billData.operator);
        BarFrame.instance.valOpenTime.setText(billData.openTime);

        ((SalesPanel)BarFrame.instance.panels[2]).billPanel.setBillID(billData.billId);

        BarFrame.instance.setShowingAnExpiredBill("expired".equals(billData.status));
        BarFrame.instance.setCurBillID(billData.billId);
        BarFrame.instance.ignoreItemChange = true;
        Spinner cmbCurTable = BarFrame.instance.getCmbCurTable();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) BarFrame.instance.getCmbCurTable().getAdapter(); // 获取Spinner的适配器
        int position = adapter.getPosition(billData.tableName); // 找到对应tableName的position

        if (position >= 0) {
            cmbCurTable.setSelection(position); // 设置Spinner选中对应的项
        } else {
            // 如果没有找到对应的tableName，可以选择处理这种情况
            L.e("BillingHistoryDlg", "Table name " + billData.tableName + " not found in Spinner options.", null);
        }
        BarFrame.instance.switchMode(2);

        return true;
    }

    class BillData {
        int billId;
        int billIdx;
        String operator;
        String openTime;
        String status;
        int customerId;
        String tableName;

        BillData(int billId, int billIdx, String operator, String openTime, String status, int customerId, String tableName) {
            this.billId = billId;
            this.billIdx = billIdx;
            this.operator = operator;
            this.openTime = openTime;
            this.status = status;
            this.customerId = customerId;
            this.tableName = tableName;
        }
    }
}
