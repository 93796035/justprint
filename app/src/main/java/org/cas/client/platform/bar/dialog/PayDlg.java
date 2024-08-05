package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;

public class PayDlg extends DialogFragment implements View.OnClickListener {

	private Context context;
	private Button btnExact, ok, back, btn10, btn20, btn30, btn40, btn50, btn100;
	private Button num1, num2, num3, num4, num5, num6, num7, num8, num9, num0, point;
	private TextView valCashReceived, valDebitReceived, valVisaReceived, valMasterReceived;
	private TextView valTotal, valLeft, titleView;

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
	private String cashReceived = "0.0";
	private String debitReceived = "0.0";
	private String visaReceived = "0.0";
	private String masterReceived = "0.0";
	private String total = "0.0";
	private String left = "0.0";
	private String title = "";

	private ArrayList<TextView> labels = new ArrayList<TextView>();
	private ArrayList<Button> values = new ArrayList<Button>();

	public PayDlg(){
		onCreateDialog(null);
	}
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = new Dialog(BarFrame.instance);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_pay);
		dialog.setCancelable(true);

		initComponent(dialog);
		setInitialValues();

		return dialog;
	}

	private void initComponent(Dialog dialog) {
		titleView = dialog.findViewById(R.id.title);
		valCashReceived = dialog.findViewById(R.id.valCashReceived);
		valDebitReceived = dialog.findViewById(R.id.valDebitReceived);
		valVisaReceived = dialog.findViewById(R.id.valVisaReceived);
		valMasterReceived = dialog.findViewById(R.id.valMasterReceived);
		valTotal = dialog.findViewById(R.id.valTotal);
		valLeft = dialog.findViewById(R.id.valLeft);
		tfdNewReceived = dialog.findViewById(R.id.tfdNewReceived);

		btn10 = dialog.findViewById(R.id.btn10);
		btn20 = dialog.findViewById(R.id.btn20);
		btn30 = dialog.findViewById(R.id.btn30);
		btn40 = dialog.findViewById(R.id.btn40);
		btn50 = dialog.findViewById(R.id.btn50);
		btn100 = dialog.findViewById(R.id.btn100);
		btnExact = dialog.findViewById(R.id.btnExact);
		ok = dialog.findViewById(R.id.ok);
		back = dialog.findViewById(R.id.back);

		num1 = dialog.findViewById(R.id.num1);
		num2 = dialog.findViewById(R.id.num2);
		num3 = dialog.findViewById(R.id.num3);
		num4 = dialog.findViewById(R.id.num4);
		num5 = dialog.findViewById(R.id.num5);
		num6 = dialog.findViewById(R.id.num6);
		num7 = dialog.findViewById(R.id.num7);
		num8 = dialog.findViewById(R.id.num8);
		num9 = dialog.findViewById(R.id.num9);
		num0 = dialog.findViewById(R.id.num0);
		point = dialog.findViewById(R.id.num00);//we don't user "." any more. "00" might be more convenient.

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
		valCashReceived.setText(cashReceived);
		valDebitReceived.setText(debitReceived);
		valVisaReceived.setText(visaReceived);
		valMasterReceived.setText(masterReceived);
		valTotal.setText(total);
		valLeft.setText(left);
	}

	public void setInitialValues(String title, String cashReceived, String debitReceived, String visaReceived, String masterReceived, String total, String left) {
		this.title = title;
		this.cashReceived = cashReceived;
		this.debitReceived = debitReceived;
		this.visaReceived = visaReceived;
		this.masterReceived = masterReceived;
		this.total = total;
		this.left = left;

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
		valCashReceived.setText("0.0");
		valDebitReceived.setText("0.0");
		valVisaReceived.setText("0.0");
		valMasterReceived.setText("0.0");
		valTotal.setText("0.0");
		valLeft.setText("0.0");
		tfdNewReceived.setText("");
		maxInput = 0f;
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
			oldTotal = DBUtil.getCursorInt(rs, "TOTAL");
			oldCashReceived = DBUtil.getCursorInt(rs, "cashReceived");
			oldDebitReceived = DBUtil.getCursorInt(rs, "debitReceived");
			oldVisaReceived = DBUtil.getCursorInt(rs, "visaReceived");
			oldMasterReceived = DBUtil.getCursorInt(rs, "masterReceived");
			oldOtherReceived = DBUtil.getCursorString(rs, "otherReceived");

			oldCashback = DBUtil.getCursorInt(rs, "CASHBACK"); //cash back is negative in db. so use "+" instead of "-"
			oldTip = DBUtil.getCursorInt(rs, "TIP");
			oldStatus = DBUtil.getCursorInt(rs, "STATUS");
			//cashReceived
			onSrcCashReceived = oldCashReceived + oldCashback;
			if (oldStatus < DBConsts.ORIGINAL) {
				if (onSrcCashReceived + oldStatus > 0) {
					onSrcCashReceived += oldStatus;
					oldStatus = 0;
				} else {
					oldStatus += onSrcCashReceived;
					onSrcCashReceived = 0;
				}
			}

			//if the tip is not 0, there must be one receive(debit, visa or master) which is bigger than tip.
			//@No need to consider the case that status is negative, because if it's refund, when reopenit, a new bill will be generated.
			//and the payDlg will display of payDlg is base on the new bill.
			onSrcDebitReceived = oldDebitReceived;
			onSrcVisaReceived = oldVisaReceived;
			onSrcMasterReceived = oldMasterReceived;
			onSrcOtherReceived = oldOtherReceived;
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
			oldLeft = onSrcOtherReceived != null && onSrcOtherReceived.length() > 0 && !"null".equals(onSrcOtherReceived.toLowerCase()) ?
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

//          reLayout();

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
