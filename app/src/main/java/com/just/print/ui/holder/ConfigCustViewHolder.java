package com.just.print.ui.holder;

import android.content.Context;
import android.widget.ToggleButton;

import com.just.print.R;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

public class ConfigCustViewHolder extends XViewHolder<String> {
    @Override
    public int getLayoutId() {
        return R.layout.tag_view_holder;
    }

    @XViewByID(R.id.tag)
    ToggleButton btnCustomization;

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
         setOnClickListener(this);
        btnCustomization.setClickable(false);
        btnCustomization.setTextOn(null);
        btnCustomization.setTextOff(null);
        btnCustomization.setChecked(true);
        btnCustomization.setCompoundDrawablePadding(10);
        btnCustomization.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.change, 0);
    }

    @Override
    public void onResetView(String customization, int i) {
        btnCustomization.setText(customization);
     }
}
