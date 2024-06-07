package org.cas.client.platform.bar.uibeans;

import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;

public class CategoryToggleButton extends ToggleButton {
	private String lang1Text;
    int index = 0;

    public CategoryToggleButton(int index) {
		super(BarFrame.instance);
		String bg = BarOption.getBK("Category");
    	if(bg == null) {
    		bg = "#a9d18d";
    	}
        setBackgroundColor(Color.parseColor(bg));
        
//        setBorder(BorderFactory.createRaisedBevelBorder());
////		setBorder(BorderFactory.createEtchedBorder());
//		setFont(BarOption.getMenuCustFont());
        this.index = index;
		setText("");//to avoid the not used button displaying an "OFF" or "关闭“ by default.
    }

	public void setMargin(int left, int top, int right, int bottom) {
		RelativeLayout.LayoutParams categoryParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		categoryParams.setMargins(left, top, right, bottom);  // 设置边距
		setLayoutParams(categoryParams);
	}

    public int getIndex() {
        return index;
    }

	public String getLang1Text() {
		return lang1Text;
	}

	public void setLang1Text(String lang1Text) {
		this.lang1Text = lang1Text;
	}
}
