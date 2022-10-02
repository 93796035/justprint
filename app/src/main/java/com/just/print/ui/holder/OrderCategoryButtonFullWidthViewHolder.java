package com.just.print.ui.holder;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

import com.just.print.R;
import com.stupid.method.adapter.XViewHolder;

/**
 * Created by qiqi on 2016/11/23.
 */

public class OrderCategoryButtonFullWidthViewHolder extends XViewHolder<String>{
    Button textView;
    @Override
    public void onCreate(Context context) {
        textView = (Button)findViewById(R.id.buttonholder);
        textView.setTextColor(Color.parseColor("#3F51B5"));
        textView.setOnClickListener(this);
        textView.setTextSize(24);
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_holder_button_full_width;
    }

    @Override
    public void onResetView(String s, int i) {
        textView.setText(s);
//        try {
//            Integer.valueOf(s);
//        }catch(Exception e){
//            textView.setBackgroundColor(AppDate.getThemColor());
//
//            //textView.setShadowLayer(5f, 3f, 3f, 0xFFFF00FF);
//        }
    }
}
