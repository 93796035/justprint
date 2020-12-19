package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Bill;
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
        billButton.setText( "#" + bill.getBillIdx() + " $" + bill.getPrice());
    }
}
