package org.cas.client.platform.bar.dialog.statistics;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Bill;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.TablesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportDlg extends DialogFragment implements View.OnClickListener {

	private List<String> formattedString;
	private String startTime;
	private String endTime;
	private String printerIP;

	private Integer year;
	private Integer month;
	private Integer day;
	private Integer hrs;
	private Integer min;
	private Integer sec;
	private String curBatchIdx = "";

	private boolean stopTriggerEventflag;

	private EditText valBatchID;
	private EditText tfdYearFrom;
	private EditText tfdMonthFrom;
	private EditText tfdDayFrom;
	private EditText tfdYearTo;
	private EditText tfdMonthTo;
	private EditText tfdDayTo;
	private EditText tfdHourFrom;
	private EditText tfdMinuteFrom;
	private EditText tfdSecondFrom;
	private EditText tfdHourTo;
	private EditText tfdMinuteTo;
	private EditText tfdSecondTo;
	private TextView txpPreview;
	private CheckBox cbxShowOnlyMine;
	private CheckBox cbxIsSilendMode;
	private Button btnSendDocument;
	private Button btnPrint;
	private Button btnLeftDay;
	private Button btnToday;
	private Button btnRightDay;
	private Button btnLeftMonth;
	private Button btnThisMonth;
	private Button btnRightMonth;

	public ReportDlg() {
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_report, container, false);

		initViews(view);
		initContent();
		return view;
	}

	private void initViews(View view) {
		valBatchID = view.findViewById(R.id.valBatchID);
		tfdYearFrom = view.findViewById(R.id.tfdYearFrom);
		tfdMonthFrom = view.findViewById(R.id.tfdMonthFrom);
		tfdDayFrom = view.findViewById(R.id.tfdDayFrom);
		tfdYearTo = view.findViewById(R.id.tfdYearTo);
		tfdMonthTo = view.findViewById(R.id.tfdMonthTo);
		tfdDayTo = view.findViewById(R.id.tfdDayTo);
		tfdHourFrom = view.findViewById(R.id.tfdHourFrom);
		tfdMinuteFrom = view.findViewById(R.id.tfdMinuteFrom);
		tfdSecondFrom = view.findViewById(R.id.tfdSecondFrom);
		tfdHourTo = view.findViewById(R.id.tfdHourTo);
		tfdMinuteTo = view.findViewById(R.id.tfdMinuteTo);
		tfdSecondTo = view.findViewById(R.id.tfdSecondTo);
		txpPreview = view.findViewById(R.id.txpPreview);
		cbxShowOnlyMine = view.findViewById(R.id.cbxShowOnlyMine);
		cbxIsSilendMode = view.findViewById(R.id.cbxIsSilendMode);
		btnSendDocument = view.findViewById(R.id.btnSendDocument);
		btnPrint = view.findViewById(R.id.btnPrint);
		btnLeftDay = view.findViewById(R.id.btnLeftDay);
		btnToday = view.findViewById(R.id.btnToday);
		btnRightDay = view.findViewById(R.id.btnRightDay);
		btnLeftMonth = view.findViewById(R.id.btnLeftMonth);
		btnThisMonth = view.findViewById(R.id.btnThisMonth);
		btnRightMonth = view.findViewById(R.id.btnRightMonth);

		btnLeftDay.setOnClickListener(this);
		btnToday.setOnClickListener(this);
		btnRightDay.setOnClickListener(this);
		btnLeftMonth.setOnClickListener(this);
		btnThisMonth.setOnClickListener(this);
		btnRightMonth.setOnClickListener(this);
		btnSendDocument.setOnClickListener(this);
		btnPrint.setOnClickListener(this);
		cbxShowOnlyMine.setOnClickListener(this);
	}

	private void initContent() {
		String endTime = BarUtil.df.format(new Date());
		String startTime = endTime.substring(0, 11) + BarOption.getStartTime();
		StringBuilder sql = new StringBuilder("select ANNIVERSARY from USER where _id = ").append(LoginDlg.USERID);
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
			if (rs.moveToNext()) {
				String lastPrintTime = DBUtil.getCursorString(rs,"ANNIVERSARY");
				if (lastPrintTime != null && lastPrintTime.length() > 10) {
					startTime = lastPrintTime;
					if (startTime.indexOf(".") > 0) {
						startTime = startTime.substring(0, startTime.indexOf("."));
					}
				}
			}
		} catch (Exception exp) {
			L.e("ReportDlg ", "Exception when fetching ANNIVERSARY." + sql, exp);
		}

		if (LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS) {
			sql = new StringBuilder("select * from systeminfo where version >= '").append(startTime).append("' order by _id desc");
			try {
				Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
				curBatchIdx = "1";
				while (rs.moveToNext()) {
					if (LoginDlg.getUSERTYPE() == LoginDlg.ADMIN_STATUS) {
						curBatchIdx = String.valueOf(DBUtil.getCursorInt(rs, "_id") + 1);
						break;
					} else {
						curBatchIdx = curBatchIdx.concat(String.valueOf(DBUtil.getCursorInt(rs, "_id"))).concat(",");
					}
				}
			} catch (Exception exp) {
				L.e("ReportDlg ", "Exception when fetching last record." + sql, exp);
			}
			if (curBatchIdx.endsWith(",")) {
				curBatchIdx = curBatchIdx.substring(0, curBatchIdx.length() - 1);
			}
			valBatchID.setText(curBatchIdx);
		}

		updateTimeFields(startTime, endTime);
		updatePreviewContent();
	}

	private void applyNewBatchID() {
		int max = Integer.parseInt(curBatchIdx.split(",")[0]);
		try {
			int inputted = Integer.parseInt(valBatchID.getText().toString());
			if (inputted > max || inputted < 1) {
				Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
				valBatchID.requestFocus();
				return;
			} else {
				if (inputted == max) {
					StringBuilder sql = new StringBuilder("select * from systeminfo where _id = ").append(max - 1);
					try {
						Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
						rs.moveToNext();
						startTime = DBUtil.getCursorString(rs,"version");
						endTime = BarUtil.df.format(new Date());
						updateTimeFields(startTime, endTime);
						updatePreviewContent();
					} catch (Exception exp) {
						L.e("ReportDlg ", "Exception when fetching latest record." + sql, exp);
					}
				} else {
					StringBuilder sql = new StringBuilder("select * from systeminfo where _id <= ").append(inputted).append(" order by _id desc limit 2");
					try {
						Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
						rs.moveToNext();
						endTime = DBUtil.getCursorString(rs,"version");
						rs.moveToNext();
						startTime = DBUtil.getCursorString(rs,"version");
						updateTimeFields(startTime, endTime);
						updatePreviewContent();
					} catch (Exception exp) {
						L.e("ReportDlg ", "Exception when fetching last record." + sql, exp);
					}
				}
			}
		} catch (Exception exp) {
			Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
			valBatchID.requestFocus();
			return;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnLeftDay:
				adjustDate(-1);
				break;
			case R.id.btnToday:
				setToday();
				break;
			case R.id.btnRightDay:
				adjustDate(1);
				break;
			case R.id.btnLeftMonth:
				adjustMonth(-1);
				break;
			case R.id.btnThisMonth:
				setThisMonth();
				break;
			case R.id.btnRightMonth:
				adjustMonth(1);
				break;
			case R.id.btnSendDocument:
				PrintService.sendDocument(cbxIsSilendMode.isChecked());
				break;
			case R.id.btnPrint:
				printReport();
				break;
			case R.id.cbxShowOnlyMine:
				updatePreviewContent();
				break;
		}
	}

	private void adjustDate(int days) {
		valBatchID.setText("");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.parseInt(tfdYearFrom.getText().toString()), Integer.parseInt(tfdMonthFrom.getText().toString()) - 1, Integer.parseInt(tfdDayFrom.getText().toString()), Integer.parseInt(tfdHourFrom.getText().toString()), Integer.parseInt(tfdMinuteFrom.getText().toString()), Integer.parseInt(tfdSecondFrom.getText().toString()));
		Date endDate = calendar.getTime();
		calendar.add(Calendar.DATE, days);
		Date startDate = calendar.getTime();
		startTime = BarUtil.df.format(startDate);
		endTime = BarUtil.df.format(endDate);
		updateTimeFields(startTime, endTime);
		updatePreviewContent();
	}

	private void setToday() {
		valBatchID.setText("");
		endTime = BarUtil.df.format(new Date());
		startTime = endTime.substring(0, 11) + BarOption.getStartTime();
		updateTimeFields(startTime, endTime);
		updatePreviewContent();
	}

	private void adjustMonth(int months) {
		valBatchID.setText("");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.parseInt(tfdYearFrom.getText().toString()), Integer.parseInt(tfdMonthFrom.getText().toString()) - 1, Integer.parseInt(tfdDayFrom.getText().toString()), Integer.parseInt(tfdHourFrom.getText().toString()), Integer.parseInt(tfdMinuteFrom.getText().toString()), Integer.parseInt(tfdSecondFrom.getText().toString()));
		Date endDate = calendar.getTime();
		calendar.add(Calendar.MONTH, months);
		Date startDate = calendar.getTime();
		startTime = BarUtil.df.format(startDate);
		endTime = BarUtil.df.format(endDate);
		updateTimeFields(startTime, endTime);
		updatePreviewContent();
	}

	private void setThisMonth() {
		valBatchID.setText("");
		endTime = BarUtil.df.format(new Date());
		startTime = endTime.substring(0, 8) + "01 " + BarOption.getStartTime();
		updateTimeFields(startTime, endTime);
		updatePreviewContent();
	}

	private void updatePreviewContent() {
		List<Bill> salesAndRefunds = queryRelevantCompleteAndRefundBillList(startTime, endTime, cbxShowOnlyMine.isChecked());
		printerIP = BarFrame.menuPanel.getPrinters()[0].getIp();
		formattedString = PrintService.formatContentForReport(salesAndRefunds, printerIP, startTime, endTime, valBatchID.getText().toString());

		StringBuilder wholeContent = new StringBuilder();
		for (String content : formattedString) {
			wholeContent.append(content);
		}
		txpPreview.setText(wholeContent.substring(0, wholeContent.length() - 9));
	}

	private void updateTimeFields(String startTime, String endNow) {
		stopTriggerEventflag = true;
		int p;
		p = startTime.indexOf(" ");
		if (p < 0) {
			startTime = endNow;
			p = startTime.indexOf(" ");
		}
		String startDate = startTime.substring(0, p);
		startTime = startTime.substring(p + 1);

		p = startDate.indexOf("-");
		tfdYearFrom.setText(startDate.substring(0, p));
		startDate = startDate.substring(p + 1);
		p = startDate.indexOf("-");
		tfdMonthFrom.setText(startDate.substring(0, p));
		tfdDayFrom.setText(startDate.substring(p + 1));

		p = startTime.indexOf(":");
		tfdHourFrom.setText(startTime.substring(0, p));
		startTime = startTime.substring(p + 1);
		p = startTime.indexOf(":");
		tfdMinuteFrom.setText(startTime.substring(0, p));
		tfdSecondFrom.setText(startTime.substring(p + 1));

		p = endNow.indexOf(" ");
		String endDate = endNow.substring(0, p);
		endNow = endNow.substring(p + 1);

		p = endDate.indexOf("-");
		tfdYearTo.setText(endDate.substring(0, p));
		endDate = endDate.substring(p + 1);
		p = endDate.indexOf("-");
		tfdMonthTo.setText(endDate.substring(0, p));
		tfdDayTo.setText(endDate.substring(p + 1));

		p = endNow.indexOf(":");
		tfdHourTo.setText(endNow.substring(0, p));
		endNow = endNow.substring(p + 1);
		p = endNow.indexOf(":");
		tfdMinuteTo.setText(endNow.substring(0, p));
		tfdSecondTo.setText(endNow.substring(p + 1));

		stopTriggerEventflag = false;
	}

	public static List<Bill> queryRelevantCompleteAndRefundBillList(String startTime, String endTimeStr, boolean showOnlyMine) {
		StringBuilder sql = new StringBuilder("select * from bill, USER where createdTime >= '").append(startTime)
				.append("' and createdTime <= '").append(endTimeStr)
				.append("' and bill.userId = employee._id ")
				.append(" and (bill.status < ").append(DBConsts.ORIGINAL)
				.append(" or bill.status = ").append(DBConsts.COMPLETED).append(")");
		if ((LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS && !BarOption.showRecordFromOtherWaiter())
				|| showOnlyMine) {
			sql.append(" and employee._id = ").append(LoginDlg.USERID);
		}

		List<Bill> salesAndRefunds = new ArrayList<>();
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
			while (rs.moveToNext()) {
				Bill bill = new Bill();
				bill.setId(DBUtil.getCursorLong(rs, "_id"));
				bill.setCreatedTime(DBUtil.getCursorString(rs, "createTime"));
				bill.setTableName(DBUtil.getCursorString(rs, "tableName"));
				bill.setBillIndex(DBUtil.getCursorString(rs, "billIndex"));
				bill.setTotal(DBUtil.getCursorInt(rs, "total"));
				bill.setDiscount(DBUtil.getCursorInt(rs, "discount"));
				bill.setCashReceived(DBUtil.getCursorInt(rs, "cashReceived"));
				bill.setDebitReceived(DBUtil.getCursorInt(rs, "debitReceived"));
				bill.setVisaReceived(DBUtil.getCursorInt(rs, "visaReceived"));
				bill.setMasterReceived(DBUtil.getCursorInt(rs, "masterReceived"));
				bill.setOtherReceived(DBUtil.getCursorString(rs, "otherReceived"));
				bill.setTip(DBUtil.getCursorInt(rs, "tip"));
				bill.setCashback(DBUtil.getCursorInt(rs, "cashback"));
				bill.setStatus(DBUtil.getCursorInt(rs, "status"));
				bill.setUserId(DBUtil.getCursorInt(rs, "employee.nName"));
				bill.setComment(DBUtil.getCursorString(rs, "Comment"));
				bill.setOpenTime(DBUtil.getCursorString(rs, "OpenTime"));
				salesAndRefunds.add(bill);
			}
			rs.close();
		} catch (Exception e) {
			L.e("ReportDlg","exception when queryRelevantCompleteAndRefundBillList: " + sql, e);
		}

		return salesAndRefunds;
	}

	private void printReport() {
		if (valBatchID.getVisibility() == View.VISIBLE && valBatchID.getText().toString().equals(curBatchIdx)) {
			if (!BarOption.CloseBatchWithUnclosedTable()) {
				if (isUnclosedTable()) {
					Toast.makeText(getContext(), BarFrame.consts.foundUnclosedTable(), Toast.LENGTH_SHORT).show();
					return;
				}
			}
			if (confirmBatchClose()) {
				closeBatchAndPrintReport();
			} else {
				return;
			}
		} else {
			sendReportToWeb();
			PrintService.printReport(printerIP, formattedString);
			updateLastPrintTime(endTime);
		}
	}

	private boolean isUnclosedTable() {
		TablesPanel tablesPanel = (TablesPanel) BarFrame.instance.panels[0];
		for (int i = 0; i < tablesPanel.btnTables.size(); i++) {
			if (tablesPanel.btnTables.get(i).getStatus() > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean confirmBatchClose() {
		return JOptionPane.showConfirmDialog(BarFrame.consts.cleanTheBatch(), org.cas.client.resources.international.DlgConst.DlgTitle);
	}

	private void closeBatchAndPrintReport() {
		StringBuilder sql = new StringBuilder("insert into systeminfo (version) values ('").append(endTime).append("')");
		try {
			AikaPos.db.execSQL(sql.toString());
		} catch (Exception exp) {
			L.e("Report", "Exception when trying to add batch report time" + sql, exp);
		}
		curBatchIdx = String.valueOf(Integer.parseInt(curBatchIdx) + 1);
		CustOpts.custOps.setKeyAndValue("NextIdx", 0);
		CustOpts.custOps.saveData();
		updateLastPrintTime(endTime);
		Toast.makeText(getContext(), BarFrame.consts.BatchClosedSuccefully(), Toast.LENGTH_SHORT).show();
	}

	private void sendReportToWeb() {
		if (BarFrame.instance.menuPanel.getPrinters()[0].getIp().toLowerCase().startsWith("mev")) {
			StringBuilder sql = new StringBuilder("select * from USER where _id = ").append(LoginDlg.USERID);
			try {
				Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
				if (rs.moveToNext()) {
					String name = DBUtil.getCursorString(rs,"NAME") + " " + DBUtil.getCursorString(rs,"FName");
					String taxAccount1 = DBUtil.getCursorString(rs,"BANKNUMBER");
					String taxAccount2 = DBUtil.getCursorString(rs,"SSCNumber");
					String address = DBUtil.getCursorString(rs,"ADDRESS");
					if (taxAccount1.length() == 15 && taxAccount2.length() == 16) {
						int yearFrom = Integer.parseInt(tfdYearFrom.getText().toString());
						int yearTo = Integer.parseInt(tfdYearTo.getText().toString());
						LocalDate date = LocalDate.now();
						int currentYear = date.getYear();
						if (yearFrom == yearTo && Integer.parseInt(tfdMonthFrom.getText().toString()) == 1 && Integer.parseInt(tfdDayFrom.getText().toString()) == 1) {
							int currentMonth = date.getMonthValue();
							int currentDate = date.getDayOfMonth();
							int monthTo = Integer.parseInt(tfdMonthTo.getText().toString());
							int dayTo = Integer.parseInt(tfdDayTo.getText().toString());
							if ((yearTo == currentYear && monthTo == currentMonth && dayTo == currentDate) || (yearTo + 1 == currentYear && monthTo == 12 && dayTo == 31)) {
								printDocument(LoginDlg.USERID, name, address, yearTo, monthTo, dayTo, taxAccount1, taxAccount2);
							}
						}
					}
				}
			} catch (Exception exp) {
				L.e("ReportDlg ", "Exception when fetching ANNIVERSARY." + sql, exp);
			}
		}
	}

	public static void printDocument(int userId, String name, String address, int year, int monthTo, int dayTo, String gst, String qst) throws ParseException {
		String recentLoginTime = null;
		String sql = "select * from evaluation where userId = " + userId + " order by starttime desc";
		try {
			Cursor rs = AikaPos.db.rawQuery(sql, null);
			if (rs.moveToNext()) {
				recentLoginTime = DBUtil.getCursorString(rs,"starttime");
			}
			rs.close();
		} catch (Exception e) {
			L.e("ReportDlg", "Exception when print Document:" + sql, e);
		}

		List<Bill> salesAndRefunds = queryRelevantCompleteAndRefundBillList(
				BarUtil.df.format(Date.from(LocalDateTime.of(year, 1, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())),
				BarUtil.df.format(Date.from(LocalDateTime.of(year, monthTo, dayTo, 23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())),
				true);
		List<String> strAryFR = new ArrayList<>();
		int tWidth = BarUtil.getPreferedWidth();
//		PrintService.pushSalesSummary(strAryFR, salesAndRefunds, tWidth);
//		PrintService.pushPaymentAndWaiterSummaryForReport(strAryFR, salesAndRefunds, tWidth);
//
//		String line = strAryFR.get(0);
//		int pNumber = line.indexOf("Number");
//		int pTotalAvaTaxe = line.indexOf("Total avant taxes");
//		int pGST = line.indexOf("GST");
//		int pQst = line.indexOf("QST");
//		int PSeparater = line.indexOf("====");
//
//		DocumentRUT docRUT = new DocumentRUT();
//		docRUT.setRT(gst);
//		docRUT.setTQ(qst);
//		docRUT.setUT(name);
//
//		StringBuilder sql = new StringBuilder("SELECT * FROM CUSTOMIZATIONS WHERE KEY = '").append(userId).append("' ORDER BY value desc");
//		try {
//			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//			rs.moveToNext();
//			docRUT.setDF(DBUtil.getCursorString(rs,"value"));
//			docRUT.setNO(DBUtil.getCursorString(rs,"status"));
//			docRUT.setMT(UtilesFormatterDonnees.formatterMontant(DBUtil.getCursorInt(rs, "type") / 100.00f));
//		} catch (SQLException e1) {
//			L.e("BarOption", "Unexpected exception when getting next order id from db with:" + sql, e1);
//		}
//
//		docRUT.setAN(String.valueOf(year));
//		docRUT.setSN(line.substring(pNumber + "Number".length(), pTotalAvaTaxe).trim());
//		docRUT.setSV(getPaymentTransactionNumber(strAryFR.get(1)));
//		String beforeTax = line.substring(pTotalAvaTaxe + "Total avant taxes".length(), pGST).trim();
//		docRUT.setSS(UtilesFormatterDonnees.formatterMontant(beforeTax));
//		String gstTotal = line.substring(pGST + "GST".length(), pQst).trim();
//		docRUT.setSF(UtilesFormatterDonnees.formatterMontant(gstTotal));
//		String qstTotall = line.substring(pQst + "QST".length(), PSeparater).trim();
//		docRUT.setSP(UtilesFormatterDonnees.formatterMontant(qstTotall));
//		float toltal = Float.valueOf(beforeTax) + Float.valueOf(gstTotal) + Float.valueOf(qstTotall);
//		docRUT.setST(UtilesFormatterDonnees.formatterMontant(toltal));
//		float adjustedAmount = getRefoundTotal(line);
//		docRUT.setSA(UtilesFormatterDonnees.formatterMontant(adjustedAmount));
//		float afterTaxAmount = toltal + adjustedAmount;
//		docRUT.setSD(UtilesFormatterDonnees.formatterMontant(afterTaxAmount));
//
//		docRUT.setIA(BarOption.getCertifiedDeviceId());
//		docRUT.setIS(BarOption.getIdSignedToAikaPos());
//		docRUT.setVR(BarOption.getCertifiedVersion());
//
//		docRUT.setDC(UtilesFormatterDonnees.GetDateHeureFormatté(BarUtil.df.parse(recentLoginTime)));
//		String date = UtilesFormatterDonnees.GetDateHeureFormatté(new Date());
//		docRUT.setDR(date);
//		docRUT.setEM(KeyStoreUtils.GetCertificateThumbprint(BarOption.getCertificateSerialNumberSRS()));
//		docRUT.setAD(address);
//
//		if (MevUtils.sendUserDocumentRequest(docRUT, userId, date, afterTaxAmount)) {
//			BarFrame.setStatusMes(name + "'s document was send to Revenue Quebec successfully!");
//		}
	}

	private static String getPaymentTransactionNumber(String summary) {
		String[] sections = summary.split(BarUtil.getSeperatorLine(1, BarUtil.getPreferedWidth()));
		String lastSection = sections[sections.length - 1];
		String firstLine = lastSection.trim();
		String[] parts = firstLine.split("\\s+");
		return parts[0];
	}

	private static float getRefoundTotal(String content) {
		String[] lines = content.split("\n");
		boolean refundSection = false;
		float total = 0;

		for (String line : lines) {
			if (line.contains("Transaction (Refund)")) {
				refundSection = true;
			} else if (line.contains("==================")) {
				refundSection = false;
			}
			if (refundSection) {
				if (line.contains("Total avant taxes") || line.contains("GST") || line.contains("QST")) {
					String[] parts = line.split("\\s+");
					try {
						float value = Float.parseFloat(parts[parts.length - 1]);
						total += value;
					} catch (NumberFormatException e) {
						System.err.println("Number format error: " + e.getMessage());
					}
				}
			}
		}

		return total;
	}

	public static void updateLastPrintTime(String endTime) {
		StringBuilder sql = new StringBuilder("update USER set anniversary = '").append(endTime).append("' where _id = ").append(LoginDlg.USERID);
		try {
			AikaPos.db.execSQL(sql.toString());
		} catch (Exception exp) {
			L.e("Report", "Exception when updating the last print time" + sql, exp);
		}
	}
}
