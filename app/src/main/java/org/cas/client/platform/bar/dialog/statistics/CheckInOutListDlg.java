package org.cas.client.platform.bar.dialog.statistics;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

import java.util.Date;

public class CheckInOutListDlg extends DialogFragment implements View.OnClickListener {

	private EditText tfdMoneyCurrent;
	private EditText tfdMoneyLeft;
	private Button btnClose;
	private TextView lblMoneyCurrent;
	private TextView lblMoneyLeft;
	private TextView lblUnit;
	private TextView lblUnit2;

	public CheckInOutListDlg(BarFrame instance) {
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_check_in_out_list, container, false);

		initViews(view);
		initContent();
		return view;
	}

	private void initViews(View view) {
		tfdMoneyCurrent = view.findViewById(R.id.tfdMoneyCurrent);
		tfdMoneyLeft = view.findViewById(R.id.tfdMoneyLeft);
		btnClose = view.findViewById(R.id.btnClose);
		lblMoneyCurrent = view.findViewById(R.id.lblMoneyCurrent);
		lblMoneyLeft = view.findViewById(R.id.lblMoneyLeft);
		lblUnit = view.findViewById(R.id.lblUnit);
		lblUnit2 = view.findViewById(R.id.lblUnit2);

		btnClose.setOnClickListener(this);
	}

	private void initContent() {
		int tShoestring = 0;
		try {
			tShoestring = Integer.parseInt((String) CustOpts.custOps.getValue(BarFrame.consts.Shoestring()));
		} catch (Exception exp) {
		}
		tfdMoneyCurrent.setText(BarUtil.formatMoney(tShoestring / 100.0));
		tfdMoneyLeft.setText("");
		tfdMoneyLeft.setEnabled(false);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnClose) {
			float tMoneyChange;
			try {
				tMoneyChange = Float.parseFloat(tfdMoneyCurrent.getText().toString());
			} catch (Exception exp) {
				Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
				tfdMoneyCurrent.requestFocus();
				return;
			}
			CustOpts.custOps.setKeyAndValue(BarFrame.consts.Shoestring(), String.valueOf(tMoneyChange));
			dismiss();
		}
	}

	public static void updateCheckInRecord() {
		String time = BarUtil.df.format(new Date());
		int p = time.indexOf(" ");
		time = time.substring(0, p + 1) + BarOption.getStartTime();

		String sql = "Select * from evaluation where userId = " + LoginDlg.USERID + " and startTime > '" + time
				+ "' and endTime is null";

		try {
			Cursor rs = AikaPos.db.rawQuery(sql, null);
			if (rs.moveToNext()) {
				int id = DBUtil.getCursorInt(rs, "_id");
				sql = "update evaluation set endTime = '" + BarUtil.df.format(new Date()) + "' where _id = " + id;
				AikaPos.db.execSQL(sql);
			} else {
				sql = "INSERT INTO evaluation(startTime, userId, tableName) VALUES ('" + BarUtil.df.format(new Date())
						+ "', " + LoginDlg.USERID + ", '" + LoginDlg.USERNAME + "')";
				AikaPos.db.execSQL(sql);
			}
		} catch (Exception exp) {
			L.e("CheckInOutListDlg", "Exception when updating check-in record.", exp);
		}
	}
}
