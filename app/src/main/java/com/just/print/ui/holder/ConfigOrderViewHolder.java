package com.just.print.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.SaleRecord;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by qiqi on 2016/11/9.
 */

public class ConfigOrderViewHolder extends XViewHolder<SaleRecord> {
    private static final String TAG = "ConfigOrderViewHolder";
    @XViewByID(R.id.odmId)
    TextView odMId;
    @XViewByID(R.id.odmname)
    TextView odMName;
    @XViewByID(R.id.odmnnum)
    TextView odMNNum;
    @XViewByID(R.id.odprice)
    TextView odPrice;

    @Override
    public int getLayoutId() {

        return R.layout.config_order_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
//        findViewById(R.id.showDetailBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    SaleRecord saleRecord;

    @Override
    public void onResetView(SaleRecord saleRecord, int i) {
        this.saleRecord = saleRecord;

        odMName.setText(saleRecord.getMname());
        odMNNum.setText("x" + saleRecord.getNumber().intValue());
        odPrice.setText("$" + saleRecord.getPrice());
    }
}
