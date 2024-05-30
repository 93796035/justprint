package org.cas.client.platform.bar.uibeans;

import static org.cas.client.platform.bar.BarOption.getBK;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.action.SamActionListener;

public class FunctionButton extends Button implements ISButton{
	
	String title;
	SamActionListener actionListener;
	private TextPaint textPaint;
	private Paint backgroundPaint;
	
	public FunctionButton(String title) {
		super(BarFrame.instance);
		this.title = title;
		String bgColor = getBK(title);
		setBackgroundColor(bgColor == null ? Color.rgb(175, 198, 255) : Color.parseColor(bgColor));
		setText(title);

		textPaint = new TextPaint();
		textPaint.setColor(Color.WHITE); // 设置文本颜色
		textPaint.setTextSize(30); // 设置文本大小
		textPaint.setTextAlign(Paint.Align.CENTER);
		backgroundPaint = new Paint();
		backgroundPaint.setStyle(Paint.Style.FILL);
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

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int w = getWidth();
		int h = getHeight();

		if (this.isSelected()) {
			backgroundPaint.setColor(Color.BLUE); // 选中状态的背景颜色
		} else {
			backgroundPaint.setColor(Color.GRAY); // 非选中状态的背景颜色
		}

		canvas.drawRect(0, 0, w, h, backgroundPaint);

		// 计算绘制文本的位置，使其水平和垂直居中
		float x = w / 2f;
		float y = (h / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);

		canvas.drawText(title, x, y, textPaint);
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
