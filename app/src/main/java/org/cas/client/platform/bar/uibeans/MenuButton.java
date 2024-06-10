package org.cas.client.platform.bar.uibeans;

import android.graphics.Color;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;

import org.cas.client.platform.bar.BarOption;

public class MenuButton extends Button {
    int dspIndex = 0;
    private Dish dish;

    public MenuButton(int dspIndex) {
		super(BarFrame.instance);
		String bg = BarOption.getBK("Dish");
    	if(bg == null) {
    		bg = "#ccff66";
    	}
    	setBackgroundColor(Color.parseColor(bg));
        this.dspIndex = dspIndex;
    }

    public int getDspIndex() {
        return dspIndex;
    }

	public Dish getDish() {
		return dish;
	}

	public void setDish(Dish dish) {
		this.dish = dish;
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
