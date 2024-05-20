package org.cas.client.platform.bar.uibeans;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.action.SamActionListener;

public class FunctionButton extends Button implements ISButton{
	
	String title;
	SamActionListener actionListener;
	
	public FunctionButton(String title) {
		super(BarFrame.instance);
		this.title = title;
		initButton();
	}

	private void initButton() {
		String bgColor = getBK(title);
		setBackgroundColor(bgColor == null ? Color.rgb(175, 198, 255) : Color.parseColor(bgColor));
		setText(title);
		//setTypeface(BarOption.getCmdCustFont()); // 设置默认字体，可以修改为自定义字体
		// Android没有直接的边框设置方法，通常通过背景Drawable或其他视图属性来实现
	}

	private String getBK(String title) {
		String color = BarOption.getBK("C-" + title);
		if(color == null) {
			color = getDefaultBKColor(title);
			if(color == null) {
				color = BarOption.getBK("Function");
			}
		}
		return color;
	}

	private String getDefaultBKColor(String title) {
		if(title.equals(BarFrame.consts.SPLIT_BILL()) || title.equals(BarFrame.consts.AddBill()) || title.equals(BarFrame.consts.VolumnDiscount())) {
			return "#ffeeaf";
		}else if(title.equals(BarFrame.consts.DISC_ITEM()) || title.equals(BarFrame.consts.CANCEL_ALL()) || title.equals(BarFrame.consts.VOID_ORDER())) {
			return "#b5c4e0";
		}else if(title.equals(BarFrame.consts.TEMPORARY_BILL()) || title.equals(BarFrame.consts.SEND())) {
			return "#0185ca";
		}
		return null;
	}

	public FunctionButton clone() {
		FunctionButton btn = new FunctionButton(title);
		btn.addActionListener(actionListener);
		actionListener.setSourceBtn(btn);
		return btn;
	}

	@Override
	public void addActionListener(SamActionListener actionListener) {
		super.setOnClickListener(actionListener);
		this.actionListener = actionListener;
		actionListener.setSourceBtn(this);
	}


	@Override
	public void setText(String text) {
		this.title = text;
	}

	@Override
	public String getText(){
		return title;
	}

    public void setVisible(boolean b) {
		setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setBounds(int i, int i1, int tBtnWidht, int tBtnHeight) {
    }
}
