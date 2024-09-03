package org.cas.client.platform.bar.uibeans;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import org.cas.client.platform.bar.BarOption;

public class NumButton extends Button {

	public NumButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NumButton(Context context) {
		super(context);
		init();
	}

	public NumButton(Context context, String title) {
		super(context);
		setText(title);
		init();
	}

	private void init() {
		String bgColor = BarOption.getBK("NumBtn");
		if (bgColor == null) { // Assuming 0 is returned when the color is not found
			bgColor = "#32618d";
		}
		setBackgroundColor(Color.parseColor(bgColor));

		setTextSize(BarOption.lessBigFont); // Assuming lessBigFont is in SP (scaled pixels)
		setPadding(8, 8, 8, 8); // Equivalent to BorderFactory.createRaisedBevelBorder()
		setElevation(8); // Provides a raised effect similar to the bevel border in Swing
	}

	public boolean isVisible() {
		return getVisibility() == View.VISIBLE;
	}
}
