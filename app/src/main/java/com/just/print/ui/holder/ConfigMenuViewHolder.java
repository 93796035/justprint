package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Dish;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

public class ConfigMenuViewHolder extends XViewHolder<Dish> {
    @XViewByID(R.id.mname)
    TextView mname;
    @XViewByID(R.id.msg)
    TextView msg;

    @Override
    public int getLayoutId() {
        return R.layout.config_menu_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.modifyMenu).setOnClickListener(this);
        findViewById(R.id.delMenu).setOnClickListener(this);
    }

    @Override
    public void onResetView(Dish menu, int i) {
        mname.setText(menu.getName1());
        msg.setText(menu.getId().toString());
        if (menu.getM2M_MenuPrintList() != null)
            msg.append("   P" + menu.getM2M_MenuPrintList().size());

        msg.append("   $" + menu.getPrice());

    }
}
