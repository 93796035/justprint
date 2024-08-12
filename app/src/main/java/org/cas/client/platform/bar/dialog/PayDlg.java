package org.cas.client.platform.bar.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.util.Date;

public class PayDlg extends SamDialog {

	private Button btn10, btn20, btn30, btn40, btn50, btn100, btnExact;
	private Button num1, num2, num3, num4, num5, num6, num7, num8, num9, num0, point, ok, back;
	private TextView valCashReceived, valDebitReceived, valVisaReceived, valMasterReceived;
	private TextView lblCashReceived, lblDebitReceived, lblVisaReceived, lblMasterReceived, lblTotal, lblLeft, valTotal, valLeft, titleView;
	private EditText tfdNewReceived;
	public float maxInput;
	// values got from db record
	private int oldTotal = 0;
	private int oldCashReceived = 0;
	private int oldDebitReceived = 0;
	private int oldVisaReceived = 0;
	private int oldMasterReceived = 0;
	private String oldOtherReceived = "";

	// calculated by minus cashback/tip and refund. (cash back is negative)
	private int onSrcCashReceived = 0;
	private int onSrcDebitReceived = 0;
	private int onSrcVisaReceived = 0;
	private int onSrcMasterReceived = 0;
	private String onSrcOtherReceived = ""; // why this one is defined as string instead of int?

	private int oldCashback = 0;
	private int oldTip = 0;
	private int oldStatus = 0;
	private int oldLeft = 0;

	public String inputedContent;
	private String cashReceived = "0.00";
	private String debitReceived = "0.00";
	private String visaReceived = "0.00";
	private String masterReceived = "0.00";
	private String total = "0.00";
	private String left = "0.00";
	private String title = "";

	boolean isAllContentSelected;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initComponents();
		relayout(dialog);
		initContent();
		return dialog;
	}

	private void initComponents() {
		titleView = createLabel(title, 22, Gravity.LEFT, Color.WHITE);

		lblCashReceived = createLabel(BarFrame.consts.CASH() + ":", Gravity.END);
		lblDebitReceived = createLabel(BarFrame.consts.DEBIT() + ":", Gravity.END);
		lblVisaReceived = createLabel(BarFrame.consts.VISA() + ":", Gravity.END);
		lblMasterReceived = createLabel(BarFrame.consts.MASTER() + ":", Gravity.END);
		lblTotal = createLabel(BarFrame.consts.Total() + " : " + BarOption.getMoneySign(), Gravity.END);
		lblLeft = createLabel(BarFrame.consts.Due() + ":", Gravity.END);

		valCashReceived = createLabel();
		valDebitReceived = createLabel();
		valVisaReceived = createLabel();
		valMasterReceived = createLabel();
		valTotal = createLabel();
		valLeft = createLabel();

		tfdNewReceived = new EditText(BarFrame.instance);

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

		addListeners();
	}

	private void relayout(Dialog dialog) {
		FrameLayout rootLayout = new FrameLayout(BarFrame.instance);
		dialog.setContentView(rootLayout);
		// Title
		rootLayout.addView(titleView, getLayout(titleView, 16, 50, FrameLayout.LayoutParams.WRAP_CONTENT, 100));

		// Payment Details
		rootLayout.addView(lblCashReceived, getLayout(lblCashReceived, 100, 50, LABEL_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(valCashReceived, getLayout(valCashReceived, lblCashReceived.getTop(), lblCashReceived.getLeft() + lblCashReceived.getMinimumWidth(), VALUE_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(lblDebitReceived, getLayout(lblDebitReceived, lblCashReceived.getTop(), valCashReceived.getLeft() + valCashReceived.getMinimumWidth() + CustOpts.HOR_GAP, LABEL_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(valDebitReceived, getLayout(valDebitReceived, lblCashReceived.getTop(), lblDebitReceived.getLeft() + lblDebitReceived.getMinimumWidth(), VALUE_WIDTH, VALUE_HEIGHT));

		rootLayout.addView(lblVisaReceived, getLayout(lblVisaReceived, lblCashReceived.getTop() + valCashReceived.getMinimumHeight() - CustOpts.VER_GAP * 3, lblCashReceived.getLeft(), LABEL_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(valVisaReceived, getLayout(valVisaReceived, lblVisaReceived.getTop(), valCashReceived.getLeft(), VALUE_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(lblMasterReceived, getLayout(lblMasterReceived, lblVisaReceived.getTop(), lblDebitReceived.getLeft(), LABEL_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(valMasterReceived, getLayout(valMasterReceived, lblVisaReceived.getTop(), valDebitReceived.getLeft(), VALUE_WIDTH, VALUE_HEIGHT));

		rootLayout.addView(tfdNewReceived, getLayout(tfdNewReceived, lblCashReceived.getTop(), valMasterReceived.getLeft() + valMasterReceived.getMinimumWidth() + CustOpts.HOR_GAP, 300));

		rootLayout.addView(lblTotal, getLayout(lblTotal, lblCashReceived.getTop(), tfdNewReceived.getLeft() + tfdNewReceived.getMinimumWidth() + CustOpts.HOR_GAP, LABEL_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(valTotal, getLayout(valTotal, lblTotal.getTop(), lblTotal.getLeft() + lblTotal.getMinimumWidth(), VALUE_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(lblLeft, getLayout(lblLeft, lblMasterReceived.getTop(), lblTotal.getLeft(), LABEL_WIDTH, VALUE_HEIGHT));
		rootLayout.addView(valLeft, getLayout(valLeft, lblLeft.getTop(), valTotal.getLeft(), VALUE_WIDTH, VALUE_HEIGHT));

		// Buttons
		constructPaymentButtons(rootLayout);
	}

	private void constructPaymentButtons(FrameLayout rootLayout) {

		int BUTTON_WIDTH = 110;

		rootLayout.addView(btn10, getLayout(btn10, valVisaReceived.getTop() + valVisaReceived.getMinimumHeight() + CustOpts.VER_GAP * 2, lblVisaReceived.getLeft(),
				BUTTON_WIDTH * 2, BUTTON_WIDTH));
		rootLayout.addView(btn20, getLayout(btn20, btn10.getTop(), btn10.getLeft() + btn10.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH * 2, BUTTON_WIDTH));
		rootLayout.addView(btn30, getLayout(btn30, btn10.getTop() + btn10.getMinimumHeight() + CustOpts.VER_GAP, btn10.getLeft(), BUTTON_WIDTH * 2, BUTTON_WIDTH));
		rootLayout.addView(btn40, getLayout(btn40, btn30.getTop(), btn20.getLeft(), BUTTON_WIDTH * 2, BUTTON_WIDTH));
		rootLayout.addView(btn50, getLayout(btn50, btn30.getTop() + btn30.getMinimumHeight() + CustOpts.VER_GAP, btn30.getLeft(), BUTTON_WIDTH * 2, BUTTON_WIDTH));
		rootLayout.addView(btn100, getLayout(btn100, btn50.getTop(), btn40.getLeft(), BUTTON_WIDTH * 2, BUTTON_WIDTH));
		rootLayout.addView(btnExact, getLayout(btnExact, btn50.getTop() + btn50.getMinimumHeight() + CustOpts.VER_GAP, btn50.getLeft(),
				btn100.getMinimumWidth() * 2 + CustOpts.HOR_GAP, BUTTON_WIDTH));

		// Number buttons
		rootLayout.addView(num1, getLayout(num1, btn10.getTop(), btn20.getLeft() + btn20.getMinimumWidth() + CustOpts.HOR_GAP * 2, BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(num2, getLayout(num2, btn10.getTop(), num1.getLeft() + num1.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(num3, getLayout(num3, btn10.getTop(), num2.getLeft() + num2.getMinimumWidth() + CustOpts.HOR_GAP, BUTTON_WIDTH + 20, BUTTON_WIDTH));

		rootLayout.addView(num4, getLayout(num4, btn30.getTop(), num1.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(num5, getLayout(num5, num4.getTop(), num2.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(num6, getLayout(num6, num4.getTop(), num3.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));

		rootLayout.addView(num7, getLayout(num7, btn50.getTop(), num4.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(num8, getLayout(num8, num7.getTop(), num5.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(num9, getLayout(num9, num7.getTop(), num6.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));

		rootLayout.addView(num0, getLayout(num0, btnExact.getTop(), num7.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(point, getLayout(point, num0.getTop(), num8.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));
		rootLayout.addView(back, getLayout(back, num0.getTop(), num9.getLeft(), BUTTON_WIDTH + 20, BUTTON_WIDTH));

		rootLayout.addView(ok, getLayout(ok, num1.getTop(), num3.getLeft() + num3.getMinimumWidth() + CustOpts.HOR_GAP,
				BUTTON_WIDTH + 20, back.getTop() - num1.getTop() + BUTTON_WIDTH));
		FrameLayout.LayoutParams params =  (FrameLayout.LayoutParams) ok.getLayoutParams();
		params.setMargins(params.leftMargin, params.topMargin, 50, 50);
	}

	private void addListeners() {
		valCashReceived.setOnClickListener(this);
		valDebitReceived.setOnClickListener(this);
		valVisaReceived.setOnClickListener(this);
		valMasterReceived.setOnClickListener(this);

		btn10.setOnClickListener(this);
		btn20.setOnClickListener(this);
		btn30.setOnClickListener(this);
		btn40.setOnClickListener(this);
		btn50.setOnClickListener(this);
		btn100.setOnClickListener(this);
		btnExact.setOnClickListener(this);
		ok.setOnClickListener(this);

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
		back.setOnClickListener(this);
		point.setOnClickListener(this);
	}

	private void initContent() {
		valCashReceived.setText(cashReceived);
		valDebitReceived.setText(debitReceived);
		valVisaReceived.setText(visaReceived);
		valMasterReceived.setText(masterReceived);
		valTotal.setText(total);
		valLeft.setText(left);
	}

	public void initContent(String title, int billId) {
		this.title = title;
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
			//left
			int oldLeft = onSrcOtherReceived != null && onSrcOtherReceived.length() > 0 && !"null".equals(onSrcOtherReceived.toLowerCase()) ?
					0 : oldTotal - onSrcCashReceived - onSrcDebitReceived - onSrcVisaReceived - onSrcMasterReceived;

			//set the interface value.
			cashReceived = BarUtil.formatMoney(onSrcCashReceived / 100.0);
			debitReceived = BarUtil.formatMoney(onSrcDebitReceived / 100.0);
			visaReceived = BarUtil.formatMoney(onSrcVisaReceived / 100.0);
			masterReceived = BarUtil.formatMoney(onSrcMasterReceived / 100.0);
			total = BarUtil.formatMoney(oldTotal / 100.0);
			left = BarUtil.formatMoney(oldLeft / 100.0);
			if (getTitle().equals(BarFrame.consts.EnterCashPayment())) {
				total = BarUtil.canadianPennyRound(total);
				left = BarUtil.canadianPennyRound(left);
			}
		} catch (Exception e) {
			ErrorUtil.write(e);
		}
	}

	@Override
	public void onClick(View v) {
		inputedContent = tfdNewReceived.getText().toString();
		SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		if (v == ok) {
			try {
				if (Float.valueOf(inputedContent) == 0) {
					return;
				}
			} catch (Exception exp) {
				Toast.makeText(getActivity(), org.cas.client.resources.international.DlgConst.FORMATERROR, Toast.LENGTH_SHORT).show();
				return;
			}

			int billOldStatus = getBillStatus(billPanel.getBillID());
			int left = Math.round(Float.valueOf(valLeft.getText().toString()) * 100);

			updateBill(billPanel.getBillID());

			if (left > 0) {
				((SalesPanel) BarFrame.instance.panels[2]).partialPaid = left < (Float.valueOf(valTotal.getText().toString()) * 100);
				Toast.makeText(getActivity(), BarFrame.consts.Due() + " : " + BarOption.getMoneySign() + BarUtil.formatMoney(left / 100f), Toast.LENGTH_SHORT).show();
			} else if (left <= 0) {
				((SalesPanel) BarFrame.instance.panels[2]).partialPaid = false;
				showChangeDlg(billPanel, left);
				if (billPanel.getStatus() >= DBConsts.BILL_PRINTED) {
					billPanel.expireAndReGenerateBill(null);
				}
				addLatestPayIntoComment(billPanel);
				boolean needToBePrinted = billOldStatus != DBConsts.BILL_PRINTED || !BarOption.isSavePrintInvoiceWhenBilled();
				PrintService.printReceipt(billPanel, getTitle().equals(BarFrame.consts.EnterCashPayment()), true, needToBePrinted);
				BarFrame.instance.completeCurrentBill();
				BarFrame.instance.actionAfterBillClosed();
			}

			dismiss();

		} else if (v == btnExact) {
			String strPay = "other";
			String curTitle = getTitle();
			if (curTitle.startsWith(BarFrame.consts.EnterCashPayment())) {
				strPay = "cash";
			} else if (curTitle.startsWith(BarFrame.consts.EnterDebitPayment())) {
				strPay = "debit";
			} else if (curTitle.startsWith(BarFrame.consts.EnterVisaPayment())) {
				strPay = "visa";
			} else if (curTitle.startsWith(BarFrame.consts.EnterMasterPayment())) {
				strPay = "master";
			}
			int billOldStatus = getBillStatus(billPanel.getBillID());

			inputedContent = BarUtil.formatMoney(oldLeft / 100.0);
			if (billPanel.getStatus() > DBConsts.ORIGINAL) {
				billPanel.expireAndReGenerateBill(null);
			}
			addLatestPayIntoComment(billPanel);
			exactMoney(billPanel.getBillID(), strPay);
			boolean needToBePrinted = billOldStatus != DBConsts.BILL_PRINTED || !BarOption.isSavePrintInvoiceWhenBilled();
			PrintService.printReceipt(billPanel, getTitle().equals(BarFrame.consts.EnterCashPayment()), true, needToBePrinted);
			BarFrame.instance.actionAfterBillClosed();
			dismiss();

		} else if (v == valCashReceived || v == valDebitReceived || v == valVisaReceived || v == valMasterReceived) {
			StringBuilder sb = new StringBuilder("update bill set ");
			if (v == valCashReceived) {
				sb.append("cashReceived = 0 where _id = ");
			} else if (v == valDebitReceived) {
				sb.append("debitReceived = 0 where _id = ");
			} else if (v == valVisaReceived) {
				sb.append("visaReceived = 0 where _id = ");
			} else if (v == valMasterReceived) {
				sb.append("masterReceived = 0 where _id = ");
			}
			sb.append(billPanel.getBillID());
			try {
				AikaPos.db.execSQL(sb.toString());
			} catch (Exception exp) {
				ErrorUtil.write(exp);
			}
			initMoneyDisplay(billPanel.getBillID());
			initContent();
		} else {
			if (isAllContentSelected)
				inputedContent = "";

			if (v == back) {
				if (inputedContent != null && inputedContent.length() > 0) {
					tfdNewReceived.setText(inputedContent.substring(0, inputedContent.length() - 1));
					if (!BarOption.isPointerInputStyle()) {
						tfdNewReceived.setText("");
					}
					updateLeft();
				}
			} else if (v == btn10) {
				increaseReceived(10);
			} else if (v == btn20) {
				increaseReceived(20);
			} else if (v == btn30) {
				increaseReceived(30);
			} else if (v == btn40) {
				increaseReceived(40);
			} else if (v == btn50) {
				increaseReceived(50);
			} else if (v == btn100) {
				increaseReceived(100);
			} else if (v == num1) {
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
			}
		}
		isAllContentSelected = false;
	}

	//it's public because there's a menu on salesPane is calling this method. ---didn't find who called it, I changed to private.
	//the difference between this and close currentBill is that this method close bill with an update of received money
	private void exactMoney(int billId, String pay) {
		try {
			String paidTime = BarUtil.df.format(new Date());
			//NOTE: should consider might been paid with card, so the new received should be the left not the total..
			StringBuilder sql = new StringBuilder("update bill set ").append(pay)
					.append("Received = ").append(Math.round(Float.valueOf(inputedContent) * 100))
					//.append(", DebitReceived = 0, VisaReceived = 0, MasterReceived = 0")
					// why do we need to update create time? because this field no need to indicate how many minutes to display on table anymore,
					//we use this field to indicate when the bill was really finished, it will determine which batch this bill will display on.)
					.append(", createdTime = '").append(paidTime).append("'")
					.append(", STATUS = ").append(DBConsts.COMPLETED)
					.append(", userId = ").append(LoginDlg.USERID)
					.append(" where _id = ").append(billId);
			AikaPos.db.execSQL(sql.toString());
			//update the status of relevant outputs.
			BarFrame.instance.updateOutputStatus(DBConsts.COMPLETED);
		}catch(Exception e) {
			ErrorUtil.write(e);
		}
	}

	private void addLatestPayIntoComment(BillPanel billPanel) {
		//update latest-pay value into db record. because web-mev need to report latest pay, previous pay and due for each time of payment (installment)
		String comment = billPanel.getComment();
		int p = comment.indexOf(PrintService.LATEST_PAY);
		if(p > -1) { //if there's already this field
			billPanel.setComment(comment.substring(0, p + PrintService.LATEST_PAY.length() + 2)
					+ " " + inputedContent
					+ comment.substring(comment.indexOf("\n", p)));
		}else {
			billPanel.setComment(comment + PrintService.LATEST_PAY + " : " + inputedContent + "\n");
		}
		DBUtil.updateBill(billPanel.getBillID(), "comment", billPanel.getComment());
	}

	private void showChangeDlg(BillPanel billPanel, int left) {
		String message = BarOption.getMoneySign() + BarUtil.formatMoney((0 - left) / 100f);
		String title;

		// Determine title and update database accordingly
		if (getTitle().equals(BarFrame.consts.EnterCashPayment())) {
			title = BarFrame.consts.Change();
			DBUtil.updateBill(billPanel.getBillID(), "cashback", oldCashback + left);
		} else {
			title = BarFrame.consts.Tip();
			DBUtil.updateBill(billPanel.getBillID(), "TIP", oldTip - left); // Treated as tip. The tip in DB is positive because it means we earned money.
		}

		// Create and show the dialog
		new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, null)
				.show();
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

		dismiss();
	}

	public void updateBill(int billId) {
		StringBuilder sb = new StringBuilder();
		String curTitle = getTitle().toString();
		int newReceivedAmount = 0;

		// Calculate the new received amount based on the current payment method
		if (curTitle.startsWith("Enter Cash Payment")) {
			newReceivedAmount = reflectNewInput(valCashReceived.getText().toString());
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

	private void increaseReceived(int amount) {
		Float received = 0.0f;
		try{
			if(tfdNewReceived.getText().length() > 0) {
				received = Float.valueOf(tfdNewReceived.getText().toString());
			}
		}catch(Exception e) {
			L.e("PayDlg-increaseReceived ", "exception when parsing content in tfdNewReceived into Float.", e);
		}
		received += amount;
		tfdNewReceived.setText(String.valueOf(received));
		updateLeft();
	}

	private void concatReceived(String num){
		String newStr = tfdNewReceived.getText().toString() + num;

		if(!BarOption.isPointerInputStyle()) {
			if(num.equals(".")) {
				int p = newStr.indexOf(".");
				if(p != newStr.lastIndexOf(".")) {
					newStr = newStr.substring(0, p) + newStr.substring(p + 1);
				}
				newStr = newStr + "00";
			}else if(newStr.length() == 1) {
				if(!newStr.equals(".")){
					newStr = ".0" + newStr;
				}
			}else if(newStr.length() == 2) {
				if(newStr.indexOf(".") < 0) {
					newStr = "." + newStr;
				}
			}else {
				int i = newStr.indexOf(".");
				if(i >= 0) {
					newStr = newStr.substring(0, i) + newStr.substring(i + 1, newStr.length());	//remove the point
				}
				newStr = newStr.substring(0, newStr.length() - 2) + "." + newStr.substring(newStr.length() - 2, newStr.length()); //put the point right place.
			}
			if(newStr.startsWith("0")) {
				newStr = newStr.substring(1);
			}
		}
		if(maxInput > 0 && Float.valueOf(newStr) > maxInput) {
			JOptionPane.showMessageDialog(BarFrame.consts.InvalidInput());
		}else {
			tfdNewReceived.setText(newStr);
			updateLeft();
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
