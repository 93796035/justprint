package org.cas.client.platform.bar.dialog;

import static org.cas.client.platform.bar.action.Cmd_Refund.checkRefundAmount;
import static org.cas.client.platform.bar.action.Cmd_Refund.refundRoutineSteps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.NumButton;
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
		reLayout();
		return dialog;
	}

	public void initContent(String title, String message){
		this.title = title;
		this.message = message;
	}

	private void initComponent() {
		tfdQTY = new EditText(getActivity());
		lblQTY = new TextView(getActivity());
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

		lblQTY.setText("Enter Quantity:");

		if(BarFrame.consts.Refund().equals(title)){
			SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
			BillPanel billPanel = salesPanel.billPanel;

			setBtnSource(null);
			setFloatSupport(true);
			setPercentSupport(false);
			setContents(billPanel.valTotlePrice.getText().toString());

			if(curContent == null || curContent.isEmpty()){	//if numberPanelDlg is not closed by clicking the ok button, but the X button, the curContent will be set to "";
				return;
			}

			float toRefundThisTime = isPercentage ?
				Float.valueOf(billPanel.valTotlePrice.getText().toString()) * Float.valueOf(curContent)
				: Float.valueOf(curContent);

			if(toRefundThisTime > Float.valueOf(billPanel.valTotlePrice.getText().toString())) {
				JOptionPane.showMessageDialog(BarFrame.consts.InvalidInput());
				return;
			}

			int refundedLifeTime = billPanel.getStatus();

			//user modified item/items, will be treated as partial refund, which will need to expire also the outputs records.
			refundedLifeTime = checkRefundAmount(toRefundThisTime, refundedLifeTime);
			if(Integer.MAX_VALUE == refundedLifeTime) {
				return;
			}

			refundRoutineSteps(billPanel, toRefundThisTime, refundedLifeTime);
		}
		ok.setOnClickListener(this);
		back.setOnClickListener(this);
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
		percent.setOnClickListener(this);
	}

	public void reLayout() {
		FrameLayout rootLayout = new FrameLayout(getActivity());
		int padding = 16;
		int buttonSize = 200;

		rootLayout.setPadding(padding, padding, padding, padding);

		FrameLayout.LayoutParams params;

		params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.topMargin = padding;
		lblQTY.setLayoutParams(params);

		params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.topMargin = padding * 2;
		tfdQTY.setLayoutParams(params);

		int yPosition = padding * 5;
		int xPosition = padding;

		for (Button btn : new Button[]{num1, num2, num3, num4, num5, num6, num7, num8, num9, num0, point, percent, back, ok}) {
			params = new FrameLayout.LayoutParams(buttonSize, buttonSize);
			params.topMargin = yPosition;
			params.leftMargin = xPosition;
			btn.setLayoutParams(params);
			xPosition += buttonSize + padding;
			if (xPosition >= rootLayout.getWidth() - padding) {
				xPosition = padding;
				yPosition += buttonSize + padding;
			}
		}

		rootLayout.addView(tfdQTY);
		rootLayout.addView(lblQTY);
		rootLayout.addView(ok);
		rootLayout.addView(back);
		rootLayout.addView(num1);
		rootLayout.addView(num2);
		rootLayout.addView(num3);
		rootLayout.addView(num4);
		rootLayout.addView(num5);
		rootLayout.addView(num6);
		rootLayout.addView(num7);
		rootLayout.addView(num8);
		rootLayout.addView(num9);
		rootLayout.addView(num0);
		rootLayout.addView(point);
		rootLayout.addView(percent);
		dialog.setContentView(rootLayout);
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
		reLayout();
	}

	public void setPercentSupport(boolean setPercentSupport) {
		percent.setVisibility(setPercentSupport ? View.VISIBLE : View.GONE);
	}

	public void setNotice(String notice) {
		lblQTY.setText(notice);
	}
}
