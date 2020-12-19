package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Bill;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.BillDao;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.holder.ConfigBillViewHolder;
import com.just.print.ui.holder.ConfigOrderViewHolder;
import com.just.print.ui.holder.OrderMenuViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.just.print.util.ToastUtil.showToast;

public class ConfigPrintReportFragment extends BaseFragment {// implements OnClickItemListener {

    private HashMap<String,List<String>> contentForPrintMap;

    @XViewByID(R.id.billList)
    FlowListView billListView;
    XAdapter2<SaleRecord> orderListXAdapter;
    XAdapter2<Bill> billsXAdapter;

    @XViewByID(R.id.orderList)
    ListView ordersView;

    private SaleRecordDao saleRecordDao;
    private BillDao billDao;
    private long now;
    private String reportContent;

    @Override
    protected int getLayoutId() {
        return R.layout.config_printreport_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }

    @XClick({R.id.verifyPassword})
    private void verifyPassword(@XGetValueByView(fromId = R.id.etMark) String mark) {
        //maybe we should hide the functions into a "more..." button.
        String adminPassword = AppData.getCustomData(AppData.adminPassword);
        if(adminPassword != null && !adminPassword.equals(mark)){
            showToast("Please input correct password!");
            return;
        }
        findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);

        saleRecordDao = Applic.app.getDaoMaster().newSession().getSaleRecordDao();
        List<SaleRecord> saleRecord = saleRecordDao.loadAll();
        if(saleRecord == null || saleRecord.size() == 0){
            getActivity().finish();
            showToast("No report to print! The sales record has been cleaned!");
            return;
        }

// comment out in case some boss don't like to see it.       //init the existing bills area.
//        billDao = Applic.app.getDaoMaster().newSession().getBillDao();
//        List<Bill> orders = new ArrayList<>();
//        try{
//            String recentNum = AppData.getCustomData(AppData.recentNum);
//            orders = DaoExpand.queryRecentBill(billDao, recentNum == null || recentNum.length() < 1 ? 9 : Integer.valueOf(recentNum));
//        }catch(Exception e){
//            //means the bill table not existing. for old version there are no bill table yet, so when update them into new version, and downloaded the old database, then will have this issue.
//        }
//        billsXAdapter = new XAdapter2<Bill>(getContext(), ConfigBillViewHolder.class);
//        billsXAdapter.setData(orders);
//        billsXAdapter.setClickItemListener(this);
//        billListView.setAdapter(billsXAdapter);

//        List<SaleRecord> dishes = saleRecordDao.loadAll();
//        orderListXAdapter.setData(dishes);

        findViewById(R.id.alertResetDlg).setVisibility(View.VISIBLE);

    }

    @XClick({R.id.buttonCancel})
    private void justPrint(){
        printReport();
        int reportIdx = 1;
        //check if need to send email
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
        }catch(Exception e){
            //It's OK if reportIdx is null
        }

        if(AppData.notifyCheck(reportIdx, reportContent, false)) {
            getActivity().finish();
        }
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void printAndReset(){
        printReport();
        int reportIdx = 1;
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
        }catch(Exception e){
            //report error.
        }

        if(AppData.notifyCheck(reportIdx, reportContent, true)){
            //when printed succcesfully, clean all records, and update now as the next reportStartDate
            saleRecordDao.deleteAll();
            billDao.deleteAll();
            AppData.putCustomData(AppData.reportStartDate, String.valueOf(now));
            AppData.putCustomData(AppData.kitchenBillIdx, "1");        //reset kitchenbillIndex

            AppData.putCustomData(AppData.reportIdx, String.valueOf(reportIdx + 1));
            getActivity().finish();
        }
    }

    private void printReport(){
        //print code:
        now = new Date().getTime(); //remem the now for later use.
        reportContent = WifiPrintService.getInstance().exePrintReportCommand(saleRecordDao.loadAll(), String.valueOf(now));
    }

//    @Override
//    public void onClickItem(View view, int idx) {
//        Bill order = billsXAdapter.getData().get(idx);
//        //show the content of this order in a pop up view
//
////        OrderIdentifierFragment.getInstance().dishesXAdapter.setData(order.getSelectedDishes());
//        final List<SaleRecord> data = DaoExpand.querySalesByBill(order.getBillIdx(), Applic.app.getDaoMaster().newSession().getSaleRecordDao());
//
//        XAdapter2<SaleRecord> adapter = new XAdapter2<SaleRecord>(getContext(), ConfigOrderViewHolder.class);
//        adapter.setData(data);
//        ordersView.setAdapter(adapter);
//
//    }
}
