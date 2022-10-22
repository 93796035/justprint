package com.just.print.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.NingMengStyle;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Bill;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.BillDao;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.UC;
import com.just.print.ui.activity.ConfigFragmentActivity;
import com.just.print.ui.holder.ConfigBillViewHolder;
import com.just.print.ui.holder.ConfigOrderViewHolder;
import com.just.print.util.L;
import com.just.print.util.StringUtils;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class ConfigMainFragment extends BaseFragment implements OnClickItemListener{

    private String reportContent;

    @XViewByID(R.id.password)
    EditText password;

    @XViewByID(R.id.billList)
    FlowListView billListView;

    @XViewByID(R.id.billIdx)
    TextView billIdx;
    @XViewByID(R.id.table)
    TextView table;
    @XViewByID(R.id.time)
    TextView time;

    @XViewByID(R.id.subtotal)
    TextView subtoal;
    @XViewByID(R.id.total)
    TextView total;

    @XViewByID(R.id.orderList)
    ListView ordersView;

    private SaleRecordDao saleRecordDao;
    private BillDao billDao;

    XAdapter2<Bill> billsXAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.config_main_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        if("false".equalsIgnoreCase(AppData.getCustomData(AppData.allowNoTableSelected))) {
            findViewById(R.id.configTableManager).setVisibility(View.GONE);
        }

        //init the existing bills area.
        billDao = NingMengStyle.app.getDaoMaster().newSession().getBillDao();
        List<Bill> orders = new ArrayList<>();
        try{
            String recentNum = AppData.getCustomData(AppData.recentBillAmount);
            orders = DaoExpand.queryRecentBill(billDao, StringUtils.isBlank(recentNum) ? 20 : Integer.valueOf(recentNum));
        }catch(Exception e){
            L.e("orders", "exception when fetching sale orders", e);
            //means the bill table not existing. for old version there are no bill table yet, so when update them into new version, and downloaded the old database, then will have this issue.
        }
        billsXAdapter = new XAdapter2<Bill>(getContext(), ConfigBillViewHolder.class);
        billsXAdapter.setClickItemListener(this);
        billListView.setAdapter(billsXAdapter);
        billsXAdapter.setData(orders);
    }

    @XClick({R.id.verifyPassword})
    private void onConfirmPassword(View view) {
        String inputContent = this.password.getText().toString().trim();
        if (StringUtils.isEmpty(inputContent) || inputContent.length() < 2) {
            showToast("Please input the right password");
        } else {
            if(!AppData.getUserPassword().equals(inputContent)){
                if(AppData.getAdminPassword().equals(inputContent)){
                    //if there's unclosed table, move relevant bill out of the report and show notice .
                    HashSet billsNotCount = new HashSet();
                    for(Mark table : OrderIdentifierFragment.tables){
                        if(table.getVersion() != -1){
                            billsNotCount.add(String.valueOf(table.getVersion() * -1));
                            showToast("Unclosed table will not be calculated into report!");
                        }
                    }
                    List<SaleRecord> orders = NingMengStyle.app.getDaoMaster().newSession().getSaleRecordDao().loadAll();
                    for(int i = orders.size() - 1; i >= 0; i--){
                        if(billsNotCount.contains(orders.get(i).getbId())){
                            orders.remove(i);
                        }
                    }
                    reportContent = WifiPrintService.getInstance().exePrintReportCommand(billsNotCount, orders, String.valueOf(new Date().getTime()));
                    findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
                    findViewById(R.id.alertReportResetDlg).setVisibility(View.VISIBLE);
                }else if(AppData.getOrderPassword().equals(inputContent)){
                    findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);

                    saleRecordDao = NingMengStyle.app.getDaoMaster().newSession().getSaleRecordDao();
                    List<SaleRecord> saleRecords = saleRecordDao.loadAll();
                    if(saleRecords == null || saleRecords.size() == 0){
                        getActivity().finish();
                        showToast("No report to print! The sales record has been cleaned!");
                        return;
                    }else{
                        findViewById(R.id.recentOrders).setVisibility(View.VISIBLE);
                    }
                }else{
                    showToast("Please input the right password");
                }
                return;
            }

            findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.configList).setVisibility(View.VISIBLE);
        }
    }

    @XClick({R.id.configMenuManager, R.id.configPrintManager, R.id.configTagManager, R.id.configTableManager,
            R.id.configUserManager, R.id.configCusManager, R.id.configPrintReport,R.id.uploadDB, R.id.downloadDB, R.id.uploadConfig, R.id.downloadConfig, R.id.reactivate})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.configMenuManager:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, ConfigMenuFragment.class);
                break;
            case R.id.configUserManager:
                // getEventBus().post(ConfigActivity.CHANGE_PAGE, Config.class);
                break;
            case R.id.configTagManager:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, ConfigMarkFragment.class);
                break;
            case R.id.configTableManager:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, ConfigTableFragment.class);
                break;
            case R.id.configPrintManager:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, ConfigPrinterFragment.class);
                break;
            case R.id.configCusManager:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, ConfigCustFragment.class);
                break;
            case R.id.configPrintReport:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, ConfigPrintReportFragment.class);
                break;
            case R.id.uploadDB:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, UploadingConfirmFragment.class);
                break;
            case R.id.downloadDB:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, DownloadingConfirmFragment.class);
                break;
            case R.id.uploadConfig:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, UploadingConfigConfirmFragment.class);
                break;
            case R.id.downloadConfig:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, DownloadingConfigConfirmFragment.class);
                break;
            case R.id.reactivate:
                getEventBus().post(ConfigFragmentActivity.CHANGE_PAGE, ReActivateFragment.class);
                break;
        }
    }

    @XClick({R.id.buttonCancel})
    private void notResetReport(){
        findViewById(R.id.alertReportResetDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertReportResetDlg).setMinimumHeight(0);
        //check if need to send email
        try{
            int reportIdx = 1;
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
            AppData.notifyCheck(reportIdx, reportContent, false);
        }catch(Exception e){
            //It's OK if reportIdx is null
        }
        startActivity(new Intent(getContext(), ConfigFragmentActivity.class));
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void resetReport(){
        int reportIdx = 1;
        //check if need to send email
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
        }catch(Exception e){
            //It's OK if reportIdx is null
        }
        if(!AppData.notifyCheck(reportIdx, reportContent, true)){
            return;
        }

        //when printed succcesfully, clean all records, and update now as the next reportStartDate
        //if the bill and sales belongs to an unclosed table, don't clean it.
        HashSet billIds = new HashSet();
        for(Mark table : OrderIdentifierFragment.tables){
            if(table.getVersion() != -1){
                billIds.add(String.valueOf(table.getVersion() * -1));
                showToast("Unclosed table will not be calculated into report!");
            }
        }
        SaleRecordDao saleRecord = NingMengStyle.app.getDaoMaster().newSession().getSaleRecordDao();
        List<SaleRecord> orders = saleRecord.loadAll();
        for(int i = orders.size() - 1; i >= 0; i--){
            if(billIds.contains(orders.get(i).getbId())){
                orders.remove(i);
            }
        }
        saleRecord.deleteInTx(orders);
        BillDao billDao = NingMengStyle.app.getDaoMaster().newSession().getBillDao();
        List<Bill> bills = billDao.loadAll();
        for(int i = bills.size() - 1; i >= 0; i--){
            if(billIds.contains(bills.get(i).getId().toString())){
                bills.remove(i);
            }
        }
        NingMengStyle.app.getDaoMaster().newSession().getBillDao().deleteInTx(bills);

        AppData.putCustomData(AppData.reportStartDate, String.valueOf(new Date().getTime()));
        AppData.putCustomData(AppData.billIdx, "1");        //reset kitchenbillIndex

        AppData.putCustomData(AppData.reportIdx, String.valueOf(reportIdx + 1));

        findViewById(R.id.alertReportResetDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertReportResetDlg).setMinimumHeight(0);

        startActivity(new Intent(getContext(), ConfigFragmentActivity.class));
    }

    @Override
    public void onClickItem(View view, int idx) {
        Bill order = billsXAdapter.getData().get(idx);
        billIdx.setText(UC.ORDER_NUMBER + order.getId());
        table.setText(UC.TABLE + order.getBillIdx());
        time.setText(UC.OPEN_TIME + order.getTime().substring(0, order.getTime().length() - 3));
        subtoal.setText(UC.SUB_TOTAL + String.format("%.2f", Float.valueOf(order.getPrice())));
        total.setText(UC.TOTAL + String.format("%.2f", Float.valueOf(order.getPrice()) * AppData.getTaxRate()));
//        OrderIdentifierFragment.getInstance().dishesXAdapter.setData(order.getSelectedDishes());
        final List<SaleRecord> data = DaoExpand.querySalesByBill(order.getId().toString(), NingMengStyle.app.getDaoMaster().newSession().getSaleRecordDao());

        XAdapter2<SaleRecord> adapter = new XAdapter2<SaleRecord>(getContext(), ConfigOrderViewHolder.class);
        adapter.setData(data);
        ordersView.setAdapter(adapter);

    }
}
