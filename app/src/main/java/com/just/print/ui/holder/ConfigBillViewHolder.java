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
        String billId = bill.getBillIdx();
        String[] parts = billId.split("\n");

        if(parts[0].length() > 3){
            parts[0] = parts[0].substring(billId.length() - 3);
        }
        parts[0] += "\n";
        String table = null;
        if(parts.length > 1) {
            if (parts[1].length() > 3) {
                table = parts[1].substring(parts[1].length() - 3);
            }else{
                table = parts[1];
            }
        }
        //comment out the price part to save space for more bill to display  billButton.setText( "#" + bill.getBillIdx() + " $" + bill.getPrice());
        billButton.setText(table == null ? parts[0] : parts[0] + table);
    }
}
