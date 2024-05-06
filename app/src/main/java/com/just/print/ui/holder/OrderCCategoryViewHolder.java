package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Dish;
import com.stupid.method.adapter.expand.XExpadnViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

public class OrderCCategoryViewHolder extends XExpadnViewHolder<Dish> {
    @XViewByID(R.id.cname)
    TextView cname;

    @Override
    public int getLayoutId() {
        return R.layout.config_category_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        setOnClickListener(this);
        findViewById(R.id.delCategory).setOnClickListener(this);
    }

    @Override
    public void onResetView(Dish menu, int i) {
        cname.setText(menu.getName1());
    }

    @Override
    protected void onNodeIsParent(boolean b, int i) {

    }

    @Override
    protected void onNodeIsChild(boolean b, int i) {

    }
}
