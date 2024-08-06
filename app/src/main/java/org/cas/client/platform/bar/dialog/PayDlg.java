package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;

public class PayDlg extends SamDialog{

	private Button btn10, btn20, btn30, btn40, btn50, btn100, btnExact;
	private Button num1, num2, num3, num4, num5, num6, num7, num8, num9, num0, point, ok, back;
	private Button valCashReceived, valDebitReceived, valVisaReceived, valMasterReceived;
	private TextView lblCashReceived, lblDebitReceived, lblVisaReceived, lblMasterReceived, lblTotal, lblLeft, valTotal, valLeft, titleView;
	private EditText tfdNewReceived;
	public float maxInput;
	//values got from db record
	private int oldTotal = 0;
	private int oldCashReceived = 0;
	private int oldDebitReceived = 0;
	private int oldVisaReceived = 0;
	private int oldMasterReceived = 0;
	private String oldOtherReceived = "";

	//calculated by minus cashback/tip and refund. (cash back is negative)
	private int onSrcCashReceived = 0;
	private int onSrcDebitReceived = 0;
	private int onSrcVisaReceived = 0;
	private int onSrcMasterReceived = 0;
	private String onSrcOtherReceived = "";	//why this one is defined as string instead of int?

	private int oldCashback = 0;
	private int oldTip = 0;
	private int oldStatus = 0;
	private int oldLeft = 0;

	public String inputedContent;
//	private String cashReceived = "0.0";
//	private String debitReceived = "0.0";
//	private String visaReceived = "0.0";
//	private String masterReceived = "0.0";
//	private String total = "0.0";
//	private String left = "0.0";
	private String title = "";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initComponents();
		setAttributes();

		constructLayout(dialog);
		addListeners();
		setInitialValues();
		return dialog;
	}

	private void initComponents() {
		titleView = new TextView(BarFrame.instance);
		lblCashReceived = createLable(BarFrame.consts.CASH().substring(0,2) + ":");
		lblDebitReceived = createLable(BarFrame.consts.DEBIT().substring(0,2) + ":");
		lblVisaReceived = createLable(BarFrame.consts.VISA().substring(0,2) + ":");
		lblMasterReceived = createLable(BarFrame.consts.MASTER().substring(0,2) + ":");
		lblTotal = createLable(BarFrame.consts.Total() + " : " + BarOption.getMoneySign());
		lblLeft = createLable(BarFrame.consts.Due() + ":");
		valCashReceived = createButton("10.00");
		valDebitReceived = createButton("20.00");
		valVisaReceived = createButton("30.00");
		valMasterReceived = createButton("40.00");
		tfdNewReceived = new EditText(BarFrame.instance);
		valTotal = new TextView(BarFrame.instance);
		valLeft = new TextView(BarFrame.instance);

		btn10 = createButton("$10");
		btn20 = createButton("$20");
		btn30 = createButton("$30");
		btn40 = createButton("$40");
		btn50 = createButton("$50");
		btn100 = createButton("$100");
		btnExact = createButton("Exact");

		num1 = createButton("1");
		num2 = createButton("2");
		num3 = createButton("3");
		ok = createButton("✔");
		num4 = createButton("4");
		num5 = createButton("5");
		num6 = createButton("6");
		num7 = createButton("7");
		num8 = createButton("8");
		num9 = createButton("9");
		num0 = createButton("0");
		point = createButton("00");
		back = createButton("←");
	}

	private void setAttributes() {
		titleView.setText("Enter Cash Payment");
		titleView.setTextSize(20);
		titleView.setTextColor(getResources().getColor(android.R.color.white));

		valCashReceived.setTextSize(20);
		valDebitReceived.setTextSize(20);
		valVisaReceived.setTextSize(20);
		valMasterReceived.setTextSize(20);
		valTotal.setTextSize(20);
		valLeft.setTextSize(20);

		tfdNewReceived.setText("0.0");
		tfdNewReceived.setTextSize(24);
		tfdNewReceived.setGravity(android.view.Gravity.CENTER);
	}

	private void constructLayout(Dialog dialog) {
		FrameLayout rootLayout = new FrameLayout(BarFrame.instance);
		dialog.setContentView(rootLayout);
		int LABEL_WIDTH = 120;
		int VALUE_WIDTH = 140;
		int VALUE_HEIGHT = 70;
		// Title
		rootLayout.addView(titleView, getLayout(titleView,16, 16));

		// Payment Details
		rootLayout.addView(lblCashReceived, getLayout(lblCashReceived,64, 16, LABEL_WIDTH));
		rootLayout.addView(valCashReceived, getLayout(valCashReceived, lblCashReceived.getTop(), lblCashReceived.getLeft() + lblCashReceived.getMinimumWidth(), VALUE_WIDTH, VALUE_HEIGHT));

		rootLayout.addView(lblDebitReceived, getLayout(lblDebitReceived, lblCashReceived.getTop(), valCashReceived.getLeft() + valCashReceived.getMinimumWidth() + CustOpts.HOR_GAP, LABEL_WIDTH));
		rootLayout.addView(valDebitReceived, getLayout(valDebitReceived, lblCashReceived.getTop(), lblDebitReceived.getLeft() + lblDebitReceived.getMinimumWidth(), VALUE_WIDTH, VALUE_HEIGHT));

		rootLayout.addView(lblVisaReceived, getLayout(lblVisaReceived, lblCashReceived.getTop() + valCashReceived.getMinimumHeight() + CustOpts.VER_GAP, lblCashReceived.getLeft(), LABEL_WIDTH));
		rootLayout.addView(valVisaReceived, getLayout(valVisaReceived, lblVisaReceived.getTop(), valCashReceived.getLeft(), VALUE_WIDTH, VALUE_HEIGHT));

		rootLayout.addView(lblMasterReceived, getLayout(lblMasterReceived, lblVisaReceived.getTop(), lblDebitReceived.getLeft(), LABEL_WIDTH));
		rootLayout.addView(valMasterReceived, getLayout(valMasterReceived, lblVisaReceived.getTop(), valDebitReceived.getLeft(), VALUE_WIDTH, VALUE_HEIGHT));

		rootLayout.addView(tfdNewReceived, getLayout(tfdNewReceived, lblCashReceived.getTop(), valMasterReceived.getLeft() + valMasterReceived.getMinimumWidth() + CustOpts.HOR_GAP, 200));

		rootLayout.addView(valTotal, getLayout(valTotal, lblCashReceived.getTop(), tfdNewReceived.getLeft() + tfdNewReceived.getMinimumWidth() + CustOpts.HOR_GAP, VALUE_HEIGHT));
		rootLayout.addView(valLeft, getLayout(valLeft, lblMasterReceived.getTop(), valTotal.getLeft(), VALUE_HEIGHT));

		// Buttons
		constructPaymentButtons(rootLayout);
	}

	private void constructPaymentButtons(FrameLayout rootLayout) {
		int topMargin = 320;
		int leftMargin = 16;
		rootLayout.addView(btn10, getLayout(btn10, topMargin, leftMargin));
		leftMargin += 100;
		rootLayout.addView(btn20, getLayout(btn20, topMargin, leftMargin));
		leftMargin += 100;
		rootLayout.addView(btn30, getLayout(btn30, topMargin, leftMargin));
		leftMargin += 100;
		rootLayout.addView(btn40, getLayout(btn40, topMargin, leftMargin));
		leftMargin += 100;
		rootLayout.addView(btn50, getLayout(btn50, topMargin, leftMargin));
		leftMargin += 100;
		rootLayout.addView(btn100, getLayout(btn100, topMargin, leftMargin));
		leftMargin += 100;
		rootLayout.addView(btnExact, getLayout(btnExact, topMargin, leftMargin));

		// Add more buttons similarly...

		// Number buttons
		topMargin += 100;
		leftMargin = 16;

		rootLayout.addView(num1, getLayout(num1, topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(num2, getLayout(num2,topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(num3, getLayout(num3, topMargin, leftMargin));

		// Number buttons
		topMargin += 100;
		leftMargin = 16;
		rootLayout.addView(num4, getLayout(num4,topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(num5, getLayout(num5, topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(num6, getLayout(num6,topMargin, leftMargin));

		topMargin += 100;
		leftMargin = 16;
		rootLayout.addView(num7, getLayout(num7, topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(num8, getLayout(num8,topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(num9, getLayout(num9,topMargin, leftMargin));

		topMargin += 100;
		leftMargin = 16;
		rootLayout.addView(num0, getLayout(num0, topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(point, getLayout(point,topMargin, leftMargin));

		leftMargin += 100;
		rootLayout.addView(back, getLayout(back, topMargin, leftMargin));

	}

	private void addListeners() {
		btn10.setOnClickListener(this);
		btn20.setOnClickListener(this);
		btn30.setOnClickListener(this);
		btn40.setOnClickListener(this);
		btn50.setOnClickListener(this);
		btn100.setOnClickListener(this);
		btnExact.setOnClickListener(this);
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
	}

	private void setInitialValues() {
		titleView.setText(title);
//		valCashReceived.setText(cashReceived);
//		valDebitReceived.setText(debitReceived);
//		valVisaReceived.setText(visaReceived);
//		valMasterReceived.setText(masterReceived);
//		valTotal.setText(total);
//		valLeft.setText(left);
	}

	public void setInitialValues(String title, String cashReceived, String debitReceived, String visaReceived, String masterReceived, String total, String left) {
		this.title = title;
//		this.cashReceived = cashReceived;
//		this.debitReceived = debitReceived;
//		this.visaReceived = visaReceived;
//		this.masterReceived = masterReceived;
//		this.total = total;
//		this.left = left;

		// 如果组件已经初始化，则立即设置初始值
		if (titleView != null) {
			setInitialValues();
		}
	}

	@Override
	public void onClick(View v) {
		inputedContent = tfdNewReceived.getText().toString();

		switch (v.getId()) {
			case R.id.ok:
				handleOkClick();
				break;
			case R.id.btnExact:
				handleExactClick();
				break;
			case R.id.back:
				handleBackClick();
				break;
			// Handle other button clicks similarly
		}
	}

	private void handleOkClick() {
		inputedContent = tfdNewReceived.getText().toString();
		SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		try {
			if (Float.valueOf(inputedContent) == 0) {
				return;
			}
		} catch (Exception exp) {
			// Show format error dialog
			return;
		}

		int billOldStatus = getBillStatus(billPanel.getBillID()); // Simplified for demonstration
		int left = Math.round(Float.valueOf(valLeft.getText().toString()) * 100);

		updateBill(billPanel.getBillID()); // Simplified for demonstration

		if (left > 0) {
			// Show due amount dialog
		} else if (left <= 0) {
			// Complete the payment process
		}

		resetContent();
		dismiss();
	}

	public void updateBill(int billId) {
		StringBuilder sb = new StringBuilder();
		String curTitle = getTitle().toString();
		int newReceivedAmount = 0;

		// Calculate the new received amount based on the current payment method
		if (curTitle.startsWith("Enter Cash Payment")) {
			newReceivedAmount = reflectNewInput(valCashReceived.getText().toString()) + (int) (Float.valueOf(valLeft.getText().toString()) * 100);
			sb.append("update bill set cashReceived = ").append(oldCashReceived + newReceivedAmount - onSrcCashReceived).append(" where _id = ").append(billId);
		} else if (curTitle.startsWith("Enter Debit Payment")) {
			newReceivedAmount = reflectNewInput(valDebitReceived.getText().toString());
			sb.append("update bill set debitReceived = ").append(oldDebitReceived + newReceivedAmount - onSrcDebitReceived).append(" where _id = ").append(billId);
		} else if (curTitle.startsWith("Enter Visa Payment")) {
			newReceivedAmount = reflectNewInput(valVisaReceived.getText().toString());
			sb.append("update bill set visaReceived = ").append(oldVisaReceived + newReceivedAmount - onSrcVisaReceived).append(" where _id = ").append(billId);
		} else if (curTitle.startsWith("Enter Master Payment")) {
			newReceivedAmount = reflectNewInput(valMasterReceived.getText().toString());
			sb.append("update bill set masterReceived = ").append(oldMasterReceived + newReceivedAmount - onSrcMasterReceived).append(" where _id = ").append(billId);
		}

		try {
			// Assuming PIMDBModel.getStatement() returns a valid SQLiteStatement or equivalent
			AikaPos.db.execSQL(sb.toString());
		} catch (Exception e) {
			// Handle exception, e.g., log the error
			e.printStackTrace();
		}
	}

	private int reflectNewInput(String existingAmount) {
		float existingMoney = 0f;
		float newAddedMoney = 0f;

		try {
			existingMoney = Float.valueOf(existingAmount);
		} catch (Exception e) {
			// Handle exception, e.g., log the error
			e.printStackTrace();
		}

		try {
			newAddedMoney = Float.valueOf(tfdNewReceived.getText().toString());
		} catch (Exception e) {
			// Handle exception, e.g., log the error
			e.printStackTrace();
		}

		return Math.round(existingMoney * 100 + newAddedMoney * 100);
	}

	private void handleExactClick() {
		// Similar to handleOkClick but with exact payment logic
	}

	private void handleBackClick() {
		if (inputedContent != null && inputedContent.length() > 0) {
			tfdNewReceived.setText(inputedContent.substring(0, inputedContent.length() - 1));
			updateLeft();
		}
	}

	private void resetContent() {
//		valCashReceived.setText("0.0");
//		valDebitReceived.setText("0.0");
//		valVisaReceived.setText("0.0");
//		valMasterReceived.setText("0.0");
//		valTotal.setText("0.0");
//		valLeft.setText("0.0");
//		tfdNewReceived.setText("");
//		maxInput = 0f;
	}

	private void updateLeft() {
		Float total = Float.valueOf(valTotal.getText().toString());
		Float received = 0f;

		try {
			received += Float.valueOf(valCashReceived.getText().toString()) + Float.valueOf(valDebitReceived.getText().toString());
			received += Float.valueOf(valVisaReceived.getText().toString()) + Float.valueOf(valMasterReceived.getText().toString());
			received += Float.valueOf(tfdNewReceived.getText().toString());
		} catch (Exception e) {
			L.e("PayDlg ", "Exception when adding every kind of received into received. ", e);
		}

		String leftStr = String.valueOf(Math.round(total * 100 - received * 100) / 100f);
		if (getTitle().equals(BarFrame.consts.EnterCashPayment())) {
			leftStr = BarUtil.canadianPennyRound(leftStr);
		}
		valLeft.setText(leftStr);
	}

	public void setTitle(String title) {
		this.title = title;
		if (titleView != null) {
			titleView.setText(title);
		}
	}

	public String getTitle() {
		return title;
	}

	public void initContent(int billId) {
		// 初始化收款信息显示
		initMoneyDisplay(billId);
	}

	private void initMoneyDisplay(int billId) {
		StringBuilder sb = new StringBuilder("select * from bill where _id = " + billId);
		try {
			Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
			rs.moveToNext();

			//save old values
			int oldTotal = DBUtil.getCursorInt(rs, "TOTAL");
			int oldCashReceived = DBUtil.getCursorInt(rs, "cashReceived");
			int oldDebitReceived = DBUtil.getCursorInt(rs, "debitReceived");
			int oldVisaReceived = DBUtil.getCursorInt(rs, "visaReceived");
			int oldMasterReceived = DBUtil.getCursorInt(rs, "masterReceived");
			String oldOtherReceived = DBUtil.getCursorString(rs, "otherReceived");

			int oldCashback = DBUtil.getCursorInt(rs, "CASHBACK"); //cash back is negative in db. so use "+" instead of "-"
			int oldTip = DBUtil.getCursorInt(rs, "TIP");
			int oldStatus = DBUtil.getCursorInt(rs, "STATUS");

			int onSrcCashReceived = oldCashReceived + oldCashback;
			if (oldStatus < DBConsts.ORIGINAL) {
				if (onSrcCashReceived + oldStatus > 0) {
					onSrcCashReceived += oldStatus;
					oldStatus = 0;
				} else {
					oldStatus += onSrcCashReceived;
					onSrcCashReceived = 0;
				}
			}

			int onSrcDebitReceived = oldDebitReceived;
			int onSrcVisaReceived = oldVisaReceived;
			int onSrcMasterReceived = oldMasterReceived;
			String onSrcOtherReceived = oldOtherReceived;
			if (oldTip > 0) {
				//debitReceived
				if (oldDebitReceived > oldTip) {
					onSrcDebitReceived -= oldTip;
				}

				//visaReceived
				if (onSrcVisaReceived > oldTip) {
					onSrcVisaReceived -= oldTip;
				}

				//masterReceived
				if (onSrcMasterReceived > oldTip) {
					onSrcMasterReceived -= oldTip;
				}
			}
			//total
			int total = oldTotal;
			//left
			int oldLeft = onSrcOtherReceived != null && onSrcOtherReceived.length() > 0 && !"null".equals(onSrcOtherReceived.toLowerCase()) ?
					0 : total - onSrcCashReceived - onSrcDebitReceived - onSrcVisaReceived - onSrcMasterReceived;

			//set the interface value.
			valCashReceived.setText(BarUtil.formatMoney(onSrcCashReceived / 100.0));
			valDebitReceived.setText(BarUtil.formatMoney(onSrcDebitReceived / 100.0));
			valVisaReceived.setText(BarUtil.formatMoney(onSrcVisaReceived / 100.0));
			valMasterReceived.setText(BarUtil.formatMoney(onSrcMasterReceived / 100.0));

			String totalStr = BarUtil.formatMoney(total / 100.0);
			if (getTitle().equals(BarFrame.consts.EnterCashPayment())) {
				totalStr = BarUtil.canadianPennyRound(totalStr);
			}
			valTotal.setText(totalStr);

			String leftStr = BarUtil.formatMoney(oldLeft / 100.0);
			if (getTitle().equals(BarFrame.consts.EnterCashPayment())) {
				leftStr = BarUtil.canadianPennyRound(leftStr);
			}
			valLeft.setText(leftStr);

		} catch (Exception e) {
			ErrorUtil.write(e);
		}
	}

	public static int getBillStatus(int billId) {
		StringBuilder sb = new StringBuilder("select * from bill where _id = ").append(billId);
		try {
			Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
			if (rs.moveToNext()) {
				return DBUtil.getCursorInt(rs, "STATUS");
			}
		} catch (Exception e) {
			ErrorUtil.write(e);
		}
		return -1;
	}

}
