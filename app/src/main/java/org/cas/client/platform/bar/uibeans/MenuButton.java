package org.cas.client.platform.bar.uibeans;

import android.graphics.Color;
import android.widget.Button;

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

//		setBorder(BorderFactory.createRaisedBevelBorder());
//		setFont(BarOption.getMenuCustFont());
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
}
