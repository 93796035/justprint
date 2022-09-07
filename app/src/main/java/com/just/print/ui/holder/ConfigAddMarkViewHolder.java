package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

public class ConfigAddMarkViewHolder extends XViewHolder<Mark> {
    @XViewByID(R.id.ip)
    TextView ip;
    @XViewByID(R.id.name)
    TextView name;

    @Override
    public int getLayoutId() {
        return R.layout.add_mark_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.imageButton).setOnClickListener(this);
    }

    @Override
    public void onResetView(Mark mark, int i) {
        if(mark == null){ //if the mark is deleted while the M2M menu is not cleaned well.
            return;
        }
        ip.setText(String.valueOf(mark.getId()));
        name.setText(mark.getName());
    }
}
