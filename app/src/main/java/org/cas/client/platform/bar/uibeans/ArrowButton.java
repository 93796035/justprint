package org.cas.client.platform.bar.uibeans;

import android.graphics.Color;
import android.widget.Button;

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
}
