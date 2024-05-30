package org.cas.client.platform.bar.uibeans;

import static org.cas.client.platform.bar.BarOption.getBK;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.widget.ToggleButton;

import com.just.print.app.BarFrame;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.action.SamActionListener;

public class FunctionToggleButton extends ToggleButton implements ISButton{
	
	private String title;
	private SamActionListener actionListener;
	private TextPaint textPaint;
	private Paint backgroundPaint;
	
	public FunctionToggleButton(String title) {
		super(BarFrame.instance);
		this.title = title;
		String bgColor = getBK(title);
		setBackgroundColor(bgColor == null ? Color.rgb(175, 198, 255) : Color.parseColor(bgColor));

		textPaint = new TextPaint();
		textPaint.setColor(Color.WHITE); // 设置文本颜色
		textPaint.setTextSize(30); // 设置文本大小
		textPaint.setTextAlign(Paint.Align.CENTER);
		backgroundPaint = new Paint();
		backgroundPaint.setStyle(Paint.Style.FILL);
	}
	
	public void addActionListener(SamActionListener actionListener) {
		if(actionListener == null) {
			L.e("FunctionToggleButton", "receieved a null as actionListener", null);
		}
		this.actionListener = (SamActionListener)actionListener;
		this.actionListener.setSourceBtn(this);
	}

	@Override
	public void setText(String text) {
		this.title = text;
	}

	@Override
	public void setBounds(int i, int i1, int btnWidth, int btnHeight) {

	}

	@Override
	public String getText(){
		return title;
	}

	public ISButton clone() {
		FunctionToggleButton btn = new FunctionToggleButton(title);
		btn.addActionListener(actionListener);
		if(actionListener == null) {
			L.e("FunctionToggleButton", "actionListener is null!", null);
		}
		actionListener.setSourceBtn(btn);
		return btn;
	}

	@Override
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
}
