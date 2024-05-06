package com.just.print.ui.holder;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

import com.just.print.R;
import com.just.print.db.bean.Mark;
import com.stupid.method.adapter.XViewHolder;

/**
 * Created by caspr on 6/12/2019.
 */

public class TableIdentifierViewHolder extends XViewHolder<Mark>{
    Button textView;
    @Override
    public void onCreate(Context context) {
        textView = (Button)findViewById(R.id.buttonholder);
        textView.setOnClickListener(this);
        textView.setTextSize(24);
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_holder_button_full_width;
    }

    @Override
    public void onResetView(Mark s, int i) {
        textView.setText(s.getName1());
        if(s.getVersion() < -1){
            textView.setTextColor(Color.BLUE);
        }else{
            textView.setTextColor(Color.BLACK);
        }
    }
}
