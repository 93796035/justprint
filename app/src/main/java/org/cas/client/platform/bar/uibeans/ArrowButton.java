package org.cas.client.platform.bar.uibeans;

import android.graphics.Color;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;

public class ArrowButton extends Button {
	public ArrowButton(String title) {
		super(BarFrame.instance);
		setText(title);
		String bg = BarOption.getBK("Arrow");
    	if(bg == null) {
    		bg = "#08cc08";
    	}
		setBackgroundColor(Color.parseColor(bg));

//		setBorder(BorderFactory.createEtchedBorder());
	}

	public void setMargin(int left, int top, int right, int bottom) {
		RelativeLayout.LayoutParams categoryParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		categoryParams.setMargins(left, top, right, bottom);  // 设置边距
		setLayoutParams(categoryParams);
	}
}
