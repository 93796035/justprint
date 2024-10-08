package org.cas.client.platform.bar.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.NumButton;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;

public class NumberPanelDlg extends SamDialog implements View.OnClickListener {

	public static String curContent = "";
	public boolean isPercentage = false;
	public static boolean confirmed = false;
	private ISButton btnSource;
	private boolean isAllContentSelected = false;
	private int maxInput; // 可以设置输入数字的最大值
	private EditText tfdQTY;
	private TextView lblQTY;
	private NumButton num1, num2, num3, num4, num5, num6, num7, num8, num9, num0, point, back, ok, percent;
	private String message;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initComponent();
		if (lblQTY.getMinimumHeight() != BUTTON_WIDTH * 2) {
			reLayout();
		}
		return dialog;
	}

	public void initContent(String title, String message){
		this.title = title;
		this.message = message;
	}

	private void initComponent() {
		tfdQTY = new EditText(BarFrame.instance);

		ok = createNumButton("✔");
		back = createNumButton("←");
		num1 = createNumButton("1");
		num2 = createNumButton("2");
		num3 = createNumButton("3");
		num4 = createNumButton("4");
		num5 = createNumButton("5");
		num6 = createNumButton("6");
		num7 = createNumButton("7");
		num8 = createNumButton("8");
		num9 = createNumButton("9");
		num0 = createNumButton("0");
		point = createNumButton(".");
		percent = createNumButton("%");

		if(BarFrame.consts.Refund().equals(title)){
			lblQTY = createLabel(message);
			SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
			BillPanel billPanel = salesPanel.billPanel;

			setBtnSource(null);
			setPercentSupport(false);
			setFloatSupport(true);
			setContents(billPanel.valTotlePrice.getText().toString());
		}else{
			lblQTY = createLabel("Enter Quantity:");
		}
		num1.setOnClickListener(this);
		num2.setOnClickListener(this);
		num3.setOnClickListener(this);
		num4.setOnClickListener(this);
		num5.setOnClickListener(this);
		num6.setOnClickListener(this);
		num7.setOnClickListener(this);
		num8.setOnClickListener(this);
		num9.setOnClickListener(this);
		num0.setOnClickListener(this);
		point.setOnClickListener(this);
		back.setOnClickListener(this);
		percent.setOnClickListener(this);
		ok.setOnClickListener(this);
	}

	public void reLayout() {
		FrameLayout rootLayout = new FrameLayout(BarFrame.instance);
		dialog.setContentView(rootLayout);

		rootLayout.addView(lblQTY, getLayoutParams(lblQTY, CustOpts.VER_GAP, CustOpts.VER_GAP, BUTTON_WIDTH * 2, BUTTON_WIDTH));
		rootLayout.addView(tfdQTY, getLayoutParams(tfdQTY, lblQTY.getTop(), lblQTY.getLeft() + lblQTY.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH * 2, BUTTON_WIDTH));

		rootLayout.addView(num1, getLayoutParams(num1, lblQTY.getTop() + lblQTY.getMinimumHeight() + CustOpts.VER_GAP,  lblQTY.getLeft(), BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num2, getLayoutParams(num2, num1.getTop(), num1.getLeft() + num1.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num3, getLayoutParams(num3, num2.getTop(), num2.getLeft() + num2.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num4, getLayoutParams(num4, num1.getTop() + num1.getMinimumHeight() + CustOpts.VER_GAP, num1.getLeft(), BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num5, getLayoutParams(num5, num4.getTop(), num4.getLeft() + num4.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num6, getLayoutParams(num6, num5.getTop(), num5.getLeft() + num5.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num7, getLayoutParams(num7, num4.getTop() + num4.getMinimumHeight() + CustOpts.VER_GAP, num4.getLeft(), BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num8, getLayoutParams(num8, num7.getTop(), num7.getLeft() + num7.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num9, getLayoutParams(num9, num8.getTop(), num8.getLeft() + num8.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(num0, getLayoutParams(num0, num7.getTop() + num7.getMinimumHeight() + CustOpts.VER_GAP, num4.getLeft(), BUTTON_WIDTH, BUTTON_WIDTH));
		rootLayout.addView(point, getLayoutParams(point, num0.getTop(), num0.getLeft() + num0.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		if(point.isVisible())
			rootLayout.addView(back, getLayoutParams(back, point.getTop(), point.getLeft() + point.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		else
			rootLayout.addView(back, getLayoutParams(back, point.getTop(), point.getLeft(), BUTTON_WIDTH * 2 + CustOpts.HOR_GAP, BUTTON_WIDTH));
		rootLayout.addView(percent, getLayoutParams(percent, num3.getTop(), num3.getLeft() + num3.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH, BUTTON_WIDTH));
		if(percent.isVisible())
			rootLayout.addView(ok, getLayoutParams(ok, percent.getTop() + percent.getMinimumHeight() + CustOpts.VER_GAP, percent.getLeft(), BUTTON_WIDTH, BUTTON_WIDTH * 3 + CustOpts.VER_GAP * 2));
		else
			rootLayout.addView(ok, getLayoutParams(ok, percent.getTop(), percent.getLeft(), BUTTON_WIDTH, BUTTON_WIDTH * 4 + CustOpts.VER_GAP * 3));

		rootLayout.setPadding(20, 20, 20, 20);
	}

	@Override
	public void onClick(View v) {
		curContent = tfdQTY.getText().toString();

		if (v == ok) {
			try {
				if (curContent.endsWith("%")) {
					String tContent = curContent.substring(0, curContent.length() - 1);
					Float f = Float.valueOf(tContent);
					curContent = String.valueOf(f / 100f);
					isPercentage = true;
				} else {
					Float.valueOf(curContent);
					isPercentage = false;
				}
			} catch (Exception exp) {
				new AlertDialog.Builder(getActivity())
						.setTitle("Error")
						.setMessage("Invalid Format!")
						.setPositiveButton(android.R.string.ok, null)
						.show();
				return;
			}

			confirmed = true;
			dismiss();
			return;
		}

		if (isAllContentSelected) {
			tfdQTY.setText("");
			curContent = "";
		}

		if (v == back) {
			if (curContent.length() > 0) {
				tfdQTY.setText(curContent.substring(0, curContent.length() - 1));
			}
		} else {
			if (v == num1) {
				concatReceived("1");
			} else if (v == num2) {
				concatReceived("2");
			} else if (v == num3) {
				concatReceived("3");
			} else if (v == num4) {
				concatReceived("4");
			} else if (v == num5) {
				concatReceived("5");
			} else if (v == num6) {
				concatReceived("6");
			} else if (v == num7) {
				concatReceived("7");
			} else if (v == num8) {
				concatReceived("8");
			} else if (v == num9) {
				concatReceived("9");
			} else if (v == num0) {
				concatReceived("0");
			} else if (v == point) {
				concatReceived(".");
			} else if (v == percent) {
				concatReceived("%");
			}
		}

		isAllContentSelected = false;
	}

	private void concatReceived(String num) {
		String newStr = tfdQTY.getText().toString().concat(num);

		if (!BarOption.isPointerInputStyle() && point.isVisible()) {
			if (num.equals("%")) {
				if (newStr.startsWith(".")) {
					newStr = newStr.substring(1);
				}
			} else if (num.equals(".")) {
				int p = newStr.indexOf(".");
				if (p != newStr.lastIndexOf(".")) {
					newStr = newStr.substring(0, p) + newStr.substring(p + 1);
				}
				newStr = newStr + "00";
			} else if (newStr.length() == 1) {
				if (!newStr.equals(".")) {
					newStr = ".0" + newStr;
				}
			} else if (newStr.length() == 2) {
				if (newStr.indexOf(".") < 0) {
					newStr = "." + newStr;
				}
			} else {
				int i = newStr.indexOf(".");
				if (i >= 0) {
					newStr = newStr.substring(0, i) + newStr.substring(i + 1, newStr.length());
				}
				newStr = newStr.substring(0, newStr.length() - 2) + "." + newStr.substring(newStr.length() - 2, newStr.length());
			}
			if (newStr.startsWith("0")) {
				newStr = newStr.substring(1);
			}
		}

		if (maxInput > 0 && Float.valueOf(newStr) > maxInput) {
			new AlertDialog.Builder(getActivity())
					.setTitle("Error")
					.setMessage("Invalid Input!")
					.setPositiveButton(android.R.string.ok, null)
					.show();
		} else {
			tfdQTY.setText(newStr);
		}
	}

	public void setContents(String qty) {
		if (qty != null) {
			if (qty.endsWith("x")) {
				qty = qty.substring(0, qty.length() - 1).trim();
				try {
					Integer.valueOf(qty);
				} catch (Exception e) {
					ErrorUtil.write("non-integer string found! when setContents() called in NumberPanelDlg class.");
				}
			}
		} else {
			ErrorUtil.write("unexpected null String when setContents() called in NumberPanelDlg class.");
		}

		curContent = qty;
		tfdQTY.setText(curContent);
		tfdQTY.requestFocus();
		tfdQTY.selectAll();
		isAllContentSelected = true;
	}

	public void setAction(View.OnClickListener action) {
		this.ok.setOnClickListener(action);
		reLayout();
	}

	public void setBtnSource(ISButton btnSource) {
		this.btnSource = btnSource;
		confirmed = false;
		curContent = "";
		tfdQTY.setText("");
	}


	public void setFloatSupport(boolean setFloatSupport) {
		point.setVisibility(setFloatSupport ? View.VISIBLE : View.GONE);
	}

	public void setPercentSupport(boolean setPercentSupport) {
		percent.setVisibility(setPercentSupport ? View.VISIBLE : View.GONE);
	}

	public void setNotice(String notice) {
		lblQTY.setText(notice);
	}
}
