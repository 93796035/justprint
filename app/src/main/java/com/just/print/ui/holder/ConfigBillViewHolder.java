package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Bill;
import com.just.print.ui.UC;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

public class ConfigBillViewHolder extends XViewHolder<Bill> {
    @XViewByID(R.id.billButton)
    TextView billButton;

    @Override
    public int getLayoutId() {
        return R.layout.config_bill_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.billButton).setOnClickListener(this);
    }

    @Override
    public void onResetView(Bill bill, int i) {
        String billId = bill.getId().toString();
        String tableName = bill.getBillIdx();
//        String[] parts = billId.split("\n");

        if(billId.length() > 3){
            billId = billId.substring(billId.length() - 3);
        }
        if(tableName.length() > 1) {
            if (tableName.length() > 3) {
                tableName = tableName.substring(tableName.length() - 3);
            }
        }
        //comment out the price part to save space for more bill to display  billButton.setText( "#" + bill.getBillIdx() + " $" + bill.getPrice());
        billButton.setText(tableName == null ? billId + UC.ENTER : billId + "\n" + tableName);
    }
}
