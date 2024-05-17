package org.cas.client.platform.bar.dialog.statistics;

import android.database.Cursor;

import com.just.print.app.AikaPos;
import com.just.print.db.bean.Bill;
import com.just.print.ui.fragment.LoginDlg;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.DBUtil;
import org.cas.client.platform.bar.model.Bill;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.TablesPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.web_srm.DocumentRUT;
import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.bar.web_srm.utiles.KeyStoreUtils;
import org.cas.client.platform.bar.web_srm.utiles.UtilesFormatterDonnees;
import org.cas.client.platform.casbeans.textpane.PIMTextPane;
import org.cas.client.platform.cascontrol.dialog.ICASDialog;
import org.cas.client.platform.cascontrol.dialog.logindlg.LoginDlg;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;
import org.cas.client.platform.casutil.TaoEmail;
import org.cas.client.platform.pimmodel.PIMDBModel;
import org.cas.client.platform.pimmodel.PIMRecord;
import org.cas.client.resource.international.DlgConst;

public class ReportDlg extends JDialog implements ICASDialog, ActionListener, ComponentListener, ListSelectionListener, ChangeListener {
	
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
	
	/**
     * Creates a new instance of ContactDialog
     * 
     * @called by PasteAction 为Copy邮件到联系人应用。
     */
    public ReportDlg(JFrame pParent) {
        super(pParent, true);
        initDialog();
    }

	/*
     * 对话盒的布局独立出来，为了在对话盒尺寸发生改变后，界面各元素能够重新布局， 使整体保持美观。尤其在Linux系列的操作系统上，所有的对话盒都必须准备好应对用户的拖拉改变尺寸。
     * @NOTE:因为setBounds方法本身不会触发事件导致重新布局，所以本方法中设置Bounds之后调用了reLayout。
     */
    @Override
    public void reLayout() {
        //main-----------------------------
        scrPane.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP,
        		390,
        		getHeight() - CustOpts.VER_GAP - CustOpts.SIZE_TITLE);
        //control elements--------------------
        lblCloseBatch.setBounds(scrPane.getX() + scrPane.getWidth() + CustOpts.HOR_GAP, CustOpts.VER_GAP * 2, lblCloseBatch.getPreferredSize().width, lblCloseBatch.getPreferredSize().height);
        valBatchID.setBounds(lblCloseBatch.getX() + lblCloseBatch.getWidth() + CustOpts.HOR_GAP, lblCloseBatch.getY(), 60, valBatchID.getPreferredSize().height);
        
        //time----------------------------------------
        lblFrom.setBounds(lblCloseBatch.getX(),  lblCloseBatch.getY() + lblCloseBatch.getHeight() + CustOpts.VER_GAP * 4,
        		lblFrom.getPreferredSize().width, lblFrom.getPreferredSize().height);
        
        lblYearFrom.setBounds(lblFrom.getX() + CustOpts.HOR_GAP, lblFrom.getY() + lblFrom.getHeight() + CustOpts.VER_GAP,
        		55, lblYearFrom.getPreferredSize().height);
        tfdYearFrom.setBounds(lblYearFrom.getX(), lblYearFrom.getY() + lblYearFrom.getHeight() + CustOpts.VER_GAP, 
        		lblYearFrom.getWidth(), 30);
        
        lblMonthFrom.setBounds(lblYearFrom.getX() + lblYearFrom.getWidth() + CustOpts.HOR_GAP, lblYearFrom.getY(), 
        		35, lblMonthFrom.getPreferredSize().height);
        tfdMonthFrom.setBounds(lblMonthFrom.getX(), tfdYearFrom.getY(), lblMonthFrom.getWidth(), tfdYearFrom.getHeight());
        
        lblDayFrom.setBounds(lblMonthFrom.getX() + lblMonthFrom.getWidth() + CustOpts.HOR_GAP, lblMonthFrom.getY(), 
        		lblMonthFrom.getWidth(), lblDayFrom.getPreferredSize().height);
        tfdDayFrom.setBounds(lblDayFrom.getX(), tfdYearFrom.getY(), lblDayFrom.getWidth(), tfdYearFrom.getHeight());
        
        lblHourFrom.setBounds(lblYearFrom.getX(), tfdYearFrom.getY() + tfdYearFrom.getHeight() + CustOpts.VER_GAP,
        		lblMonthFrom.getWidth(), lblHourFrom.getPreferredSize().height);
        tfdHourFrom.setBounds(lblHourFrom.getX(), lblHourFrom.getY() + lblHourFrom.getHeight() + CustOpts.VER_GAP, 
        		lblHourFrom.getWidth(), 30);
        
        lblMinuteFrom.setBounds(lblHourFrom.getX() + lblHourFrom.getWidth() + CustOpts.HOR_GAP, lblHourFrom.getY(), 
        		lblHourFrom.getWidth(), lblMinuteFrom.getPreferredSize().height);
        tfdMinuteFrom.setBounds(lblMinuteFrom.getX(), tfdHourFrom.getY(), lblMinuteFrom.getWidth(), tfdHourFrom.getHeight());
        
        lblSecondFrom.setBounds(lblMinuteFrom.getX() + lblMinuteFrom.getWidth() + CustOpts.HOR_GAP, lblMinuteFrom.getY(), 
        		lblMinuteFrom.getWidth(), lblSecondFrom.getPreferredSize().height);
        tfdSecondFrom.setBounds(lblSecondFrom.getX(), tfdMinuteFrom.getY(), lblSecondFrom.getWidth(), tfdMinuteFrom.getHeight());
       
        lblTo.setBounds(lblFrom.getX(), tfdSecondFrom.getY() + tfdSecondFrom.getHeight() + CustOpts.VER_GAP,
        		lblTo.getPreferredSize().width, lblFrom.getHeight());
        
        lblYearTo.setBounds(lblYearFrom.getX(), lblTo.getY() + lblTo.getHeight() + CustOpts.VER_GAP, 
        		lblYearFrom.getWidth(), lblYearFrom.getHeight());
        tfdYearTo.setBounds(lblYearTo.getX(), lblYearTo.getY() + lblYearTo.getHeight() + CustOpts.VER_GAP, 
        		lblYearTo.getWidth(), tfdYearFrom.getHeight());
        
        lblMonthTo.setBounds(lblYearTo.getX() + lblYearTo.getWidth() + CustOpts.HOR_GAP, lblYearTo.getY(), 
        		lblMonthFrom.getWidth(), lblMonthFrom.getHeight());
        tfdMonthTo.setBounds(lblMonthTo.getX(), tfdYearTo.getY(),
        		lblMonthTo.getWidth(), tfdYearFrom.getHeight());
        
        lblDayTo.setBounds(lblMonthTo.getX() + lblMonthTo.getWidth() + CustOpts.HOR_GAP, lblMonthTo.getY(), 
        		lblDayFrom.getWidth(), lblDayFrom.getHeight());
        tfdDayTo.setBounds(lblDayTo.getX(), tfdMonthTo.getY(), lblDayTo.getWidth(), tfdYearFrom.getHeight());
        
        lblHourTo.setBounds(lblYearTo.getX(), tfdDayTo.getY() + tfdDayTo.getHeight() + CustOpts.VER_GAP, 
        		lblHourFrom.getWidth(), lblHourFrom.getHeight());
        tfdHourTo.setBounds(lblHourTo.getX(), lblHourTo.getY() + lblHourTo.getHeight() + CustOpts.VER_GAP,
        		lblHourTo.getWidth(), tfdHourFrom.getHeight());
        
        lblMinuteTo.setBounds(lblHourTo.getX() + lblHourTo.getWidth() + CustOpts.HOR_GAP, lblHourTo.getY(), 
        		lblMinuteFrom.getWidth(), lblMinuteFrom.getHeight());
        tfdMinuteTo.setBounds(lblMinuteTo.getX(), tfdHourTo.getY(), lblMinuteTo.getWidth(), tfdHourFrom.getHeight());
        
        lblSecondTo.setBounds(lblMinuteTo.getX() + lblMinuteTo.getWidth() + CustOpts.HOR_GAP, lblMinuteTo.getY(), 
        		lblSecondFrom.getWidth(), lblSecondFrom.getHeight());
        tfdSecondTo.setBounds(lblSecondTo.getX(), tfdMinuteTo.getY(), lblSecondTo.getWidth(), tfdHourFrom.getHeight());
        
        btnLeftDay.setBounds(lblTo.getX(), tfdSecondTo.getY() + tfdSecondTo.getHeight() + CustOpts.VER_GAP * 4,
        		btnLeftDay.getPreferredSize().width, CustOpts.BTN_HEIGHT * 2);
        btnToday.setBounds(btnLeftDay.getX() + btnLeftDay.getWidth() + CustOpts.VER_GAP, btnLeftDay.getY(),
        		btnToday.getPreferredSize().width, CustOpts.BTN_HEIGHT * 2);
        btnRightDay.setBounds(btnToday.getX() + btnToday.getWidth() + CustOpts.VER_GAP, btnToday.getY(),
        		btnRightDay.getPreferredSize().width, CustOpts.BTN_HEIGHT * 2);
        
        btnLeftMonth.setBounds(btnLeftDay.getX(), btnRightDay.getY() + btnRightDay.getHeight() + CustOpts.VER_GAP,
        		btnLeftMonth.getPreferredSize().width, CustOpts.BTN_HEIGHT * 2);
        btnThisMonth.setBounds(btnLeftMonth.getX() + btnLeftMonth.getWidth() + CustOpts.VER_GAP, btnLeftMonth.getY(),
        		btnThisMonth.getPreferredSize().width, CustOpts.BTN_HEIGHT * 2);
        btnRightMonth.setBounds(btnThisMonth.getX() + btnThisMonth.getWidth() + CustOpts.VER_GAP, btnThisMonth.getY(),
        		btnRightMonth.getPreferredSize().width, CustOpts.BTN_HEIGHT * 2);
        
        cbxShowOnlyMine.setBounds(lblTo.getX(), btnRightMonth.getY() + btnRightMonth.getHeight() + CustOpts.VER_GAP * 4,
          		 cbxShowOnlyMine.getPreferredSize().width, cbxShowOnlyMine.getPreferredSize().height);

        //button---------------------------
        int btnWidth = CustOpts.getBTN_WIDTH(btnPrint);
        cbxIsSilendMode.setBounds(lblFrom.getX(), cbxShowOnlyMine.getY() + cbxShowOnlyMine.getHeight() + CustOpts.VER_GAP * 5, 
        		cbxIsSilendMode.getPreferredSize().width, CustOpts.BTN_HEIGHT);
        btnSendDocument.setBounds(cbxIsSilendMode.getX(),
        		cbxIsSilendMode.getY() + cbxIsSilendMode.getHeight() + CustOpts.VER_GAP,
        		btnWidth * 2 + 30,
        		CustOpts.BTN_HEIGHT * 2 - CustOpts.VER_GAP);
        btnPrint.setBounds(getWidth() - btnWidth - 20, 
        		cbxIsSilendMode.getY(), btnWidth, CustOpts.BTN_HEIGHT * 3);

        validate();
    }

	private void applyNewBatchID() {
		int max = Integer.valueOf(curBatchIdx.split(",")[0]);
		try {
			int inputted = Integer.valueOf(valBatchID.getText());
			if(inputted > max || inputted < 1) {
				JOptionPane.showMessageDialog(this, BarFrame.consts.InvalidInput());
				valBatchID.grabFocus();
				return;
			}else {
				
				if(inputted == max){
					StringBuilder sql = new StringBuilder("select * from systeminfo where id = ").append(max - 1);
					try {
						ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
			            rs.next();
		            	startTime = rs.getString("version");
		            	endTime = BarUtil.df.format(new Date());
		            	updateTimeFields(startTime, endTime);
		            	updatePreviewContent();
					}catch(Exception exp) {
						L.e("ReportDlg ", "Exception when fetching lastest record." + sql, exp);
					}
				}else {	//there's must be 2 records fetched out, because the inputed value must be at least 1.
					StringBuilder sql = new StringBuilder("select * from systeminfo where id <= ").append(inputted).append(" order by id desc limit 2");
					try {//get the relevant print Time base on batchID and the previous batchID
						ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
			            rs.next();
			            endTime = rs.getString("version");
			            rs.next();
		            	startTime = rs.getString("version");
		            	updateTimeFields(startTime, endTime);
		            	updatePreviewContent();
					}catch(Exception exp) {
						L.e("ReportDlg ", "Exception when ertiving last record." + sql, exp);
					}
				}
			}
		}catch (Exception exp) {
			JOptionPane.showMessageDialog(this, BarFrame.consts.InvalidInput());
			valBatchID.grabFocus();
			return;
		}
	}

    @Override
    public PIMRecord getContents() {
        return null;
    }

    @Override
    public boolean setContents( PIMRecord prmRecord) {
        return true;
    }

    @Override
    public void makeBestUseOfTime() {}

    @Override
    public void addAttach(File[] file, Vector actualAttachFiles) {}

    @Override
    public PIMTextPane getTextPane() {
        return null;
    }

    @Override
    public void release() {
    	cbxIsSilendMode.removeActionListener(this);
    	btnSendDocument.removeActionListener(this);
        btnPrint.removeActionListener(this);
        dispose();// 对于对话盒，如果不加这句话，就很难释放掉。
        System.gc();// @TODO:不能允许私自运行gc，应该改为象收邮件线程那样低优先级地自动后台执行，可以从任意方法设置立即执行。
    }

    @Override
    public void componentResized(ComponentEvent e) {
        reLayout();
    };

    @Override
    public void componentMoved(ComponentEvent e) {};

    @Override
    public void componentShown(ComponentEvent e) {};

    @Override
    public void componentHidden(ComponentEvent e) {};

    @Override
    public void actionPerformed(ActionEvent e) {
    	if(e.getSource() == btnLeftDay) {
    		valBatchID.setText("");
    		Calendar calendar = Calendar.getInstance();
    		calendar.set(Integer.parseInt(tfdYearFrom.getText()), Integer.parseInt(tfdMonthFrom.getText()) - 1, Integer.parseInt(tfdDayFrom.getText())
    				, Integer.parseInt(tfdHourFrom.getText()), Integer.parseInt(tfdMinuteFrom.getText()), Integer.parseInt(tfdSecondFrom.getText()));
    		Date endDate = calendar.getTime();
    		calendar.add(Calendar.DATE, -1);
    		Date startDate = calendar.getTime();
    		startTime = BarUtil.df.format(startDate);
    		endTime = BarUtil.df.format(endDate);
    		updateTimeFields(startTime, endTime);
    	    updatePreviewContent();
    	}else if(e.getSource() == btnToday) {
    		valBatchID.setText("");
    		endTime = BarUtil.df.format(new Date());
    		startTime = endTime.substring(0, 11) + BarOption.getStartTime();
    		updateTimeFields(startTime, endTime);
    	    updatePreviewContent();
    	}else if(e.getSource() == btnRightDay) {
    		valBatchID.setText("");
    		Calendar calendar = Calendar.getInstance();
    		calendar.set(Integer.parseInt(tfdYearTo.getText()), Integer.parseInt(tfdMonthTo.getText()) - 1, Integer.parseInt(tfdDayTo.getText())
    				, Integer.parseInt(tfdHourTo.getText()), Integer.parseInt(tfdMinuteTo.getText()), Integer.parseInt(tfdSecondTo.getText()));
    		Date startDate = calendar.getTime();
    		calendar.add(Calendar.DATE, 1);
    		Date endDate = calendar.getTime();
    		startTime = BarUtil.df.format(startDate);
    		endTime = BarUtil.df.format(endDate);
    		updateTimeFields(startTime, endTime);
    	    updatePreviewContent();
    	}else if(e.getSource() == btnLeftMonth) {
    		valBatchID.setText("");
    		Calendar calendar = Calendar.getInstance();
    		calendar.set(Integer.parseInt(tfdYearFrom.getText()), Integer.parseInt(tfdMonthFrom.getText()) - 1, Integer.parseInt(tfdDayFrom.getText())
    				, Integer.parseInt(tfdHourFrom.getText()), Integer.parseInt(tfdMinuteFrom.getText()), Integer.parseInt(tfdSecondFrom.getText()));
    		Date endDate = calendar.getTime();
    		calendar.add(Calendar.MONTH, -1);
    		Date startDate = calendar.getTime();
    		startTime = BarUtil.df.format(startDate);
    		endTime = BarUtil.df.format(endDate);
    		updateTimeFields(startTime, endTime);
    	    updatePreviewContent();
    	}else if(e.getSource() == btnThisMonth) {
    		valBatchID.setText("");
    		endTime = BarUtil.df.format(new Date());
    		startTime = endTime.substring(0, 8) + "01 " + BarOption.getStartTime();
    		updateTimeFields(startTime, endTime);
    	    updatePreviewContent();
    	}else if(e.getSource() == btnRightMonth) {
    		valBatchID.setText("");
    		Calendar calendar = Calendar.getInstance();
    		calendar.set(Integer.parseInt(tfdYearTo.getText()), Integer.parseInt(tfdMonthTo.getText()) - 1, Integer.parseInt(tfdDayTo.getText())
    				, Integer.parseInt(tfdHourTo.getText()), Integer.parseInt(tfdMinuteTo.getText()), Integer.parseInt(tfdSecondTo.getText()));
    		Date startDate = calendar.getTime();
    		calendar.add(Calendar.MONTH, 1);
    		Date endDate = calendar.getTime();
    		startTime = BarUtil.df.format(startDate);
    		endTime = BarUtil.df.format(endDate);
    		updateTimeFields(startTime, endTime);
    	    updatePreviewContent();
    	}else if(e.getSource() == cbxShowOnlyMine) {
    		valBatchID.setText("");
    		updatePreviewContent();
    	}else if (e.getSource() == btnSendDocument) {
    		PrintService.sendDocument(cbxIsSilendMode.isSelected());
    	}else if(e.getSource() == btnPrint){
	    	//if it's manager(admin), update db batch report time and in memory curBatchIdx value.
    		//while every one will update the last print time except the manager(admin) has chance to select not to.
    		//(when manager select not to close, then will not update the last print time).
	    	if(valBatchID.isVisible() && valBatchID.getText().equals(curBatchIdx)) {
		    	
	    		if(!BarOption.CloseBatchWithUnclosedTable()) {
	    			//check if there are unclosed table? if yes, show message and return;
			    	TablesPanel tablesPanel = (TablesPanel)BarFrame.instance.panels[0];
	           	 	boolean foundUnclosedTable = false;
	           	 	int i = 0;
	           	 	for (; i < tablesPanel.btnTables.size(); i++) {
	           	 		if(tablesPanel.btnTables.get(i).getStatus() > 0) {
	           	 			foundUnclosedTable = true;
	           	 			break;
	           	 		}
	           	 	}
					if(foundUnclosedTable) {
						JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.foundUnclosedTable());
						return;
					}
	    		}
	    		//notice user to confirm that batch will be closed.
				if(JOptionPane.showConfirmDialog(BarFrame.instance, BarFrame.consts.cleanTheBatch(), DlgConst.DlgTitle,
	                    JOptionPane.YES_NO_OPTION) == 0) {
					//if user selected to close batch, then update the sytemInfo_version with the endtime of current report (the row id will the be batch #.
					//then reset the NextIdx system config file
			    	PrintService.printReport(printerIP, formattedString);
			    	
		    		StringBuilder sql = new StringBuilder("insert into systeminfo (version) values ('").append(endTime).append("')");
		    		try {
		    			PIMDBModel.getStatement().executeUpdate(sql.toString());
			    	}catch(Exception exp) {
			    		L.e("Report", " exception when trying to add batch report time" + sql, exp);
			    	}
		    		
		    		curBatchIdx =  String.valueOf(Integer.valueOf(curBatchIdx) + 1);
		    		CustOpts.custOps.setKeyAndValue("NextIdx", 0);//reset invoice number to 0.
		    		CustOpts.custOps.saveData();

			    	updateLastPrintTime(endTime);
		    		JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.BatchClosedSuccefully());
				}else {
					return;
					//changed to if don't want to close batch, then don't print.....
//					cbxCustTime.setSelected(true);
//					valBatchID.setEnabled(false);
//		            tfdYearFrom.setEnabled(true);
//		            tfdMonthFrom.setEnabled(true);
//		            tfdDayFrom.setEnabled(true);
//		            tfdYearTo.setEnabled(true);
//		            tfdMonthTo.setEnabled(true);
//		            tfdDayTo.setEnabled(true);
//		            
//		            tfdHourFrom.setEnabled(true);
//		            tfdMinuteFrom.setEnabled(true);
//		            tfdSecondFrom.setEnabled(true);
//		            tfdHourTo.setEnabled(true);
//		            tfdMinuteTo.setEnabled(true);
//		            tfdSecondTo.setEnabled(true);
//		            
//		            updatePreviewContent();
//		            
//			    	PrintService.printReport(printerIP, formattedString);
				}
    		}else {
    			//if it's for web mev
    			if(BarFrame.instance.menuPanel.getPrinters()[0].getIp().toLowerCase().startsWith("mev")) {
    				//and the login user has tax numbers
    				StringBuilder sql = new StringBuilder("select * from employee where id = ").append(LoginDlg.USERID);	
    				try {
    					ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
    		            if(rs.next()) {
    	                    String name = rs.getString("Name") + " " + rs.getString("FName");
    		            	String taxAccount1 = rs.getString("BANKNUMBER");
    		            	String taxAccount2 = rs.getString("SSCNumber");
    		            	String address = rs.getString("ADDRESS");
    		            	if(taxAccount1.length() == 15 && taxAccount2.length() == 16) {
    		            		//lastly,if it's for this year or last year, we consider it as user report, then send request to web srm.
    		            		int yearFrom = parseContentToInt(tfdYearFrom);
    		            		int yearTo = parseContentToInt(tfdYearTo);
		            			LocalDate date = LocalDate.now();
		            			int currentYear = date.getYear();
    		            		if(yearFrom == yearTo && parseContentToInt(tfdMonthFrom) == 1 && parseContentToInt(tfdDayFrom) == 1){
    		            			int currentMonth = date.getMonthValue();
    		            			int currentDate = date.getDayOfMonth();
    		            			int monthTo = parseContentToInt(tfdMonthTo);
    		            			int dayTo = parseContentToInt(tfdDayTo);
    		            			if((yearTo == currentYear && monthTo == currentMonth && dayTo == currentDate) 
    		            					|| (yearTo + 1 == currentYear && monthTo == 12 && dayTo == 31)) {
    		            				printDocument(LoginDlg.USERID, name, address, yearTo, monthTo, dayTo, taxAccount1, taxAccount2);
    		            			}
    		            		}
    		            	}
    		            }
    				}catch(Exception exp) {
    					L.e("ReportDlg ", "Exception when ertiving ANNIVERSARY." + sql, exp);
    				}
    			}
    			//so, by default, we set time to currentday(customized start time + current time), while if this user has printed before, then we set start time to last print time. 
    				
    			String email = BarOption.getBatchReportEmail();
    			if(TaoEmail.isValidEmail(email)) {
    				TaoEmail.sendMessage("Batch Report", email, txpPreview.getText()); 
    			}
		    	PrintService.printReport(printerIP, formattedString);
		    	updateLastPrintTime(endTime);
    		}
	    	
    		this.dispose();
    		
	    	//don't think this will make any sense
//	    	//delete relevant record. of 10 years old.
//	    	Date searchDate = new GregorianCalendar(year, month - 1 , day, hrs, min, sec).getTime();
//	    	int days = (int) ((new Date().getTime() - searchDate.getTime()) / (1000*3600*24));
//	    	if(days >= BarOption.getHistoryDays()) {
//	    		StringBuilder sql = new StringBuilder("update Bill set status = ").append(DBConsts.deleted)
//		    			.append(" where createTime >= '").append(startTime).append("'")
//		    			.append(" and createTime <= '").append(endTime).append("'");
//		    	try {
//		    		PIMDBModel.getStatement().executeUpdate(sql.toString());
//		    	}catch(Exception exp) {
//		    		L.e("Report", " exception when trying to delete records from db", exp);
//		    	}
//	    	}
    	}
    }

	public static void printDocument(int userId, String name, String address, int year, int monthTo, int dayTo, String gst, String qst) throws ParseException {
		String recentLoginTime = null;
		try {
			String sql = "select * from evaluation where employeeId = " + userId + " order by starttime desc";
		    ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql);
		    rs.beforeFirst();
		    if(rs.next()) {
		    	recentLoginTime = rs.getString("starttime");
		    }
		    rs.close();// 关闭
		} catch (SQLException e) {
		    ErrorUtil.write(e);
		}

		ArrayList<Bill> salesAndRefunds = queryRelevantCompleteAndRefundBillList(
			BarUtil.df.format(Date.from(LocalDateTime.of(year, 1, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())),
			BarUtil.df.format(Date.from(LocalDateTime.of(year,monthTo,dayTo,23,59,59).atZone(ZoneId.systemDefault()).toInstant())),
			true);
		ArrayList<String> strAryFR = new ArrayList<String>();
		int tWidth = BarUtil.getPreferedWidth();
		PrintService.pushSalesSummary(strAryFR, salesAndRefunds, tWidth);
		PrintService.pushPaymentAndWaiterSummaryForReport(strAryFR, salesAndRefunds, tWidth);
		
		String line = strAryFR.get(0);
		// Total number of transactions
		int pNumber = line.indexOf("Number");
		int pTotalAvaTaxe = line.indexOf("Total avant taxes");
		int pGST = line.indexOf("GST");
		int pQst = line.indexOf("QST");
		int PSeparater = line.indexOf("====");

		
		DocumentRUT docRUT = new DocumentRUT();
		docRUT.setRT(gst);//GST registration number noTPS 123456789RT0001
		docRUT.setTQ(qst);//QST registration number noTVQ 1234567890TQ0001
		docRUT.setUT(name);//Name of the mandatary or the first name and last name of the person linked
		//--------------------------Parameters related to the last document------------------------------
		//TODO：看来需要写一个custommized字段来保存每个mandetory的上一次document信息
		StringBuilder sql = new StringBuilder("SELECT * FROM CUSTOMIZATIONS WHERE KEY = '")
				.append(userId)
				.append("' ORDER BY value desc");
		try {
			ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
	        rs.beforeFirst();
	        rs.next();
	        docRUT.setDF(rs.getString("value"));//Date and time of the transaction of the last document produced (datTrans) 20200101125939
			docRUT.setNO(rs.getString("status"));	//Transaction number of the last document produced (noTrans) 1234567890
			docRUT.setMT(UtilesFormatterDonnees.formatterMontant(DBUtil.getCursorInt(rs,"type") / 100.00f));	//After-tax amount of the last document produced (apresTax) +000000015.00
	        
		} catch (SQLException e1) {
			L.e("BarOption", "Unexpected exception when getting next order id from db with:" + sql, e1);
		}
		//--------------------------Parameters related to the sales summary
		docRUT.setAN(String.valueOf(year));//Year (current or preceding) the transactions in the sales summary were recorded (no field) 2020
		docRUT.setSN(line.substring(pNumber + "Number".length(), pTotalAvaTaxe).trim());	//Total number of transactions (no field) 16
		docRUT.setSV(getPaymentTransactionNumber(strAryFR.get(1)));		//Number of payment transactions (no field) 3
		String beforeTax = line.substring(pTotalAvaTaxe + "Total avant taxes".length(), pGST).trim();
		docRUT.setSS(UtilesFormatterDonnees.formatterMontant(beforeTax));//Before-tax amount from the sales summary (no field) +000000020.00
		String gstTotal = line.substring(pGST + "GST".length(), pQst).trim();
		docRUT.setSF(UtilesFormatterDonnees.formatterMontant(gstTotal));	//GST amount from the sales summary (no field) +000000001.00
		String qstTotall = line.substring(pQst + "QST".length(), PSeparater).trim();
		docRUT.setSP(UtilesFormatterDonnees.formatterMontant(qstTotall));	//QST amount from the sales summary (no field) +000000002.00
		Float toltal = Float.valueOf(beforeTax) + Float.valueOf(gstTotal) + Float.valueOf(qstTotall);
		docRUT.setST(UtilesFormatterDonnees.formatterMontant(toltal));//After-tax amount from the sales summary (no field) +000000023.00
		Float adjustedAmount = getRefoundTotal(line);
		docRUT.setSA(UtilesFormatterDonnees.formatterMontant(adjustedAmount));	//Adjusted amount from the sales summary (no field) -000000004.00
		Float afterTaxAmount = toltal + adjustedAmount;
		docRUT.setSD(UtilesFormatterDonnees.formatterMontant(afterTaxAmount));//Amount due from the sales summary (no field) +000000019.00   looks like this is the money to pay revainu
		
		/**-------not sure following is needed or not, currently, without this part, also work----
		docRUT.setTS("A");	//Source used to calculate the sales summary		NOTE: If the sales summary is calculated for a single SRS, enter "A". If the sales summary is calculated for all SRS of the establishment, enter "E".
		docRUT.setSR("0001-2345-6789");
		//Identifier of the device for which the report is requested, if the sales summary is calculated for a single SRS. OR Mandatory billing file number relating to mandatory invoicing, if the sales
		//summary is calculated for all of the establishment's SRS (for example, ER0001 or TR0001).	(no field) 0001-2345-6789
		docRUT.setCM("Unique");		//Indication that the sales correspond to a single user account or to all user accounts of the establishment.
		//NOTE:	If the sales correspond to a single user, enter "Unique". If the sales correspond to all users of the establishment, for example for the madatary and his employees, enter “Tous” (All). (no field) Unique
		**/
		//-------------------Parameters relating to the device used to produce the report----------------
		docRUT.setIA(BarOption.getCertifiedDeviceId());	// Unique device identifier IDAPPRL 0001-2345-67890001-2345-6789
		docRUT.setIS(BarOption.getIdSignedToAikaPos());	//Unique SRS identifier IDSEV 0000000000003372
		docRUT.setVR(BarOption.getCertifiedVersion());	//SRS version identifier assigned by the developer, which corresponds to the update of the parent version VERSI 003.030.0616
		//-------------------Parameters relating to the dates required on the report----------------------
		docRUT.setDC(UtilesFormatterDonnees.GetDateHeureFormatté(BarUtil.df.parse(recentLoginTime)));// Date and time the user logged into their account (no field) 20200101130108
		String date = UtilesFormatterDonnees.GetDateHeureFormatté(new Date()); 
		docRUT.setDR(date); //Date and time the user report was produced (no field) 20200101132302
		//------------------Other parameters-------------------
		docRUT.setEM(KeyStoreUtils.GetCertificateThumbprint(BarOption.getCertificateSerialNumberSRS())); //Thumbprint of the digital certificate linked with the private key that generated the digital signature (no field) (40 characters)
		docRUT.setAD(address);	//Address of the establishment NOTE: The field must be present even when the address is not sent (e.g. AD=). (no field) 3800, rue de Marly, Québec, G1X4A5
		
		//======send out!=======
		if(MevUtils.sendUserDocumentRequest(docRUT, userId, date, afterTaxAmount)) {
         	BarFrame.setStatusMes(name + "'s document was send to Revenue Quebec successfully!");
		}
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
                refundSection = true; // 开始退款部分
            } else if (line.contains("==================")) {
                refundSection = false; // 退款部分结束
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
		//save the to time into anniversary (wow, we use last print time as anniversary)
		StringBuilder sql = new StringBuilder("update employee set anniversary = '").append(endTime).append("' where id = ").append(LoginDlg.USERID);
		try {
			PIMDBModel.getStatement().executeUpdate(sql.toString());
		}catch(Exception exp) {
			L.e("Report", " exception when updating the lastprint time" + sql, exp);
		}
	}
    
	@Override
	public void valueChanged(ListSelectionEvent e) {}

	@Override
	public void stateChanged(ChangeEvent e) {	//this is used to monitor the change or inside Textfield.
		if(e.getSource() == valBatchID) {
			if(valBatchID.getText().length() > 0) {
				stopTriggerEventflag = true;
				applyNewBatchID();
				SwingUtilities.invokeLater(() -> {stopTriggerEventflag = false;});
			}
		}else {
			if(stopTriggerEventflag) { //when the content of more than 1 textfields are changed by code, not by hand, should set this. 
				return;					//I didn't use remove/addback listener way, because it seems a little complex.
			}
			updateStartAndEndTime();
	    	updatePreviewContent();
	    	valBatchID.setText("");	//clear the content in valBatchID,
		}
	}

	private void updatePreviewContent() {
		ArrayList<Bill> salesAndRefunds = queryRelevantCompleteAndRefundBillList(startTime, endTime, cbxShowOnlyMine.isSelected());
    	//the filting work has been down in the query.
//    	HashMap<String, ArrayList<Bill>> map = divideIntoMap(bills); 
//    	if(!BarOption.showRecordFromOtherWaiter() && LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS) {
//    		bills = map.get(BarFrame.instance.valOperator.getText());
//    	}
		printerIP = BarFrame.menuPanel.getPrinters()[0].getIp();
		formattedString = PrintService.formatContentForReport(salesAndRefunds, printerIP, startTime, endTime, valBatchID.getText());
		
		StringBuilder wholeContent = new StringBuilder();
		for (String content : formattedString) {
			wholeContent.append(content);
		}
		txpPreview.setText(wholeContent.substring(0, wholeContent.length() - 9));
	}

	private Integer parseContentToInt(JTextField tfd) {
		try {
			return Integer.valueOf(tfd.getText());
		}catch(Exception e) {
			JOptionPane.showMessageDialog(this, BarFrame.consts.InvalidInput());
			tfd.selectAll();
		}
		return 0;
	}
	
	private void updateStartAndEndTime() {
		StringBuilder startTimeBuilder = new StringBuilder();
		//start date===========================
		year = parseContentToInt(tfdYearFrom);
		if(year < 100) {
			year += 100;
		}else if(year < 1000){
			year += 2000;
		}
		startTimeBuilder.append(year);
		startTimeBuilder.append("-");

    	month = parseContentToInt(tfdMonthFrom);
    	if(month < 10) {
    		startTimeBuilder.append("0");
		}
    	startTimeBuilder.append(month);
    	startTimeBuilder.append("-");

    	day = parseContentToInt(tfdDayFrom);
    	if(day < 10) {
    		startTimeBuilder.append("0");
    	}
    	startTimeBuilder.append(day);
    	startTimeBuilder.append(" ");
    	//start time--------------------------------
    	hrs = parseContentToInt(tfdHourFrom);
    	if(hrs < 10) {
    		startTimeBuilder.append("0");
		}
		startTimeBuilder.append(hrs);
    	startTimeBuilder.append(":");

    	min = parseContentToInt(tfdMinuteFrom);
    	if(min < 10) {
    		startTimeBuilder.append("0");
    	}
    	startTimeBuilder.append(min);
    	startTimeBuilder.append(":");

    	sec = parseContentToInt(tfdSecondFrom);
    	if(sec < 10) {
    		startTimeBuilder.append("0");
    	}
    	startTimeBuilder.append(sec);
    	startTime = startTimeBuilder.toString();
    	
    	//endDate======================================
    	StringBuilder endTimeBuilder = new StringBuilder();
    	year = parseContentToInt(tfdYearTo);
    	if(year < 100) {
			year += 100;
		}else if(year < 1000){
			year += 2000;
		}
    	endTimeBuilder.append(tfdYearTo.getText());
    	endTimeBuilder.append("-");

    	month = parseContentToInt(tfdMonthTo);
		if(month < 10) {
	    	endTimeBuilder.append("0");
		}
    	endTimeBuilder.append(month);
    	endTimeBuilder.append("-");

    	day = parseContentToInt(tfdDayTo);
		if(day < 10) {
			endTimeBuilder.append("0");
		}
    	endTimeBuilder.append(day);
    	endTimeBuilder.append(" ");
    	//end time----------------------------------------
    	hrs = parseContentToInt(tfdHourTo);
    	if(hrs < 10) {
    		endTimeBuilder.append("0");
		}
    	endTimeBuilder.append(hrs);
    	endTimeBuilder.append(":");

    	min = parseContentToInt(tfdMinuteTo);
    	if(min < 10) {
    		endTimeBuilder.append("0");
    	}
    	endTimeBuilder.append(min);
    	endTimeBuilder.append(":");

    	sec = parseContentToInt(tfdSecondTo);
    	if(sec < 10) {
    		endTimeBuilder.append("0");
    	}
    	endTimeBuilder.append(sec);
    	endTime = endTimeBuilder.toString();
	}
	
//    private HashMap<String, ArrayList<Bill>> divideIntoMap(ArrayList<Bill> bills) {
//    	HashMap<String, ArrayList<Bill>> map = new HashMap<String, ArrayList<Bill>>();
//    	for (Bill bill : bills) {
//			String waiterName = bill.getEmployeeName();
//			if(map.containsKey(waiterName)) {
//				ArrayList<Bill> list = map.get(waiterName);
//				list.add(bill);
//			}else {
//				ArrayList<Bill> list = new ArrayList<Bill>();
//				list.add(bill);
//				map.put(waiterName, list);
//			}
//		}
//		return map;
//	}

	@Override
    public Container getContainer() {
        return getContentPane();
    }

    
	private void initDialog() {
        setTitle(BarFrame.consts.SaleReport());
        setResizable(false);
        setModal(false);

        // 初始化－－－－－－－－－－－－－－－－
        lblCloseBatch = new JLabel(BarFrame.consts.barchIndex());
        valBatchID = new JTextField();
        cbxShowOnlyMine = new JCheckBox(BarFrame.consts.showOnlyMine());
        
        btnLeftDay = new JButton("<<<");
        btnToday = new JButton(BarFrame.consts.today());
        btnRightDay = new JButton(">>>");
        btnLeftMonth = new JButton("<<<");
        btnThisMonth = new JButton(BarFrame.consts.thisMonth());
        btnRightMonth = new JButton(">>>");
        
        lblFrom = new JLabel(BarFrame.consts.FROM());
        lblTo = new JLabel(BarFrame.consts.TO());
        lblYearFrom = new JLabel("YYYY");
        lblMonthFrom = new JLabel("MM");
        lblDayFrom = new JLabel("DD");
        lblYearTo = new JLabel("YYYY");
        lblMonthTo = new JLabel("MM");
        lblDayTo = new JLabel("DD");
        tfdYearFrom = new JTextField();
        tfdMonthFrom = new JTextField();
        tfdDayFrom = new JTextField();
        tfdYearTo = new JTextField();
        tfdMonthTo = new JTextField();
        tfdDayTo = new JTextField();
        
        lblHourFrom = new JLabel("HH");
        lblMinuteFrom = new JLabel("mm");
        lblSecondFrom = new JLabel("SS");
        lblHourTo = new JLabel("HH");
        lblMinuteTo = new JLabel("mm");
        lblSecondTo = new JLabel("SS");
        tfdHourFrom = new JTextField();
        tfdMinuteFrom = new JTextField();
        tfdSecondFrom = new JTextField();
        tfdHourTo = new JTextField();
        tfdMinuteTo = new JTextField();
        tfdSecondTo = new JTextField();
        
        cbxIsSilendMode = new JCheckBox(BarFrame.consts.inSilentMode());
        btnSendDocument = new JButton(BarFrame.consts.sendDocument());
        btnPrint = new JButton(BarFrame.consts.PRINT());
        txpPreview = new JTextArea();
        scrPane = new JScrollPane(txpPreview);
        // properties
        cbxIsSilendMode.setVisible(LoginDlg.getUSERTYPE() == LoginDlg.SUPER_ADMIN);
        btnSendDocument.setVisible(LoginDlg.getUSERTYPE() == LoginDlg.SUPER_ADMIN);
        btnPrint.setMnemonic('F');
        btnPrint.setMargin(new Insets(0, 0, 0, 0));
        
        lblTo.setHorizontalTextPosition(SwingConstants.CENTER);
        lblYearFrom.setHorizontalTextPosition(SwingConstants.CENTER);
        lblMonthFrom.setHorizontalTextPosition(SwingConstants.CENTER);
        lblDayFrom.setHorizontalTextPosition(SwingConstants.CENTER);
        lblYearTo.setHorizontalTextPosition(SwingConstants.CENTER);
        lblMonthTo.setHorizontalTextPosition(SwingConstants.CENTER);
        lblDayTo.setHorizontalTextPosition(SwingConstants.CENTER);
        
        lblHourFrom.setHorizontalTextPosition(SwingConstants.CENTER);
        lblMinuteFrom.setHorizontalTextPosition(SwingConstants.CENTER);
        lblSecondFrom.setHorizontalTextPosition(SwingConstants.CENTER);
        lblHourTo.setHorizontalTextPosition(SwingConstants.CENTER);
        lblMinuteTo.setHorizontalTextPosition(SwingConstants.CENTER);
        lblSecondTo.setHorizontalTextPosition(SwingConstants.CENTER);
        
        JLabel tLbl = new JLabel();
        tLbl.setOpaque(true);
        tLbl.setBackground(Color.GRAY);
        txpPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        txpPreview.setEditable(false);
        
        // 布局---------------
        setBounds((CustOpts.SCRWIDTH - 683) / 2, 20, 683, BarFrame.instance.getHeight() - 20); // 对话框的默认尺寸。
        getContentPane().setLayout(null);

        // 搭建－－－－－－－－－－－－－

        getContentPane().add(lblCloseBatch);
        getContentPane().add(valBatchID);
        getContentPane().add(cbxShowOnlyMine);

        getContentPane().add(btnLeftDay);
        getContentPane().add(btnToday);
        getContentPane().add(btnRightDay);
        getContentPane().add(btnLeftMonth);
        getContentPane().add(btnThisMonth);
        getContentPane().add(btnRightMonth);
        
        getContentPane().add(lblFrom);
        getContentPane().add(lblTo);
        
        getContentPane().add(lblYearFrom);
        getContentPane().add(lblMonthFrom);
        getContentPane().add(lblDayFrom);
        getContentPane().add(lblYearTo);
        getContentPane().add(lblMonthTo);
        getContentPane().add(lblDayTo);
        getContentPane().add(tfdYearFrom);
        getContentPane().add(tfdMonthFrom);
        getContentPane().add(tfdDayFrom);
        getContentPane().add(tfdYearTo);
        getContentPane().add(tfdMonthTo);
        getContentPane().add(tfdDayTo);

        getContentPane().add(lblHourFrom);
        getContentPane().add(lblMinuteFrom);
        getContentPane().add(lblSecondFrom);
        getContentPane().add(lblHourTo);
        getContentPane().add(lblMinuteTo);
        getContentPane().add(lblSecondTo);
        getContentPane().add(tfdHourFrom);
        getContentPane().add(tfdMinuteFrom);
        getContentPane().add(tfdSecondFrom);
        getContentPane().add(tfdHourTo);
        getContentPane().add(tfdMinuteTo);
        getContentPane().add(tfdSecondTo);
        
        getContentPane().add(cbxIsSilendMode);
        getContentPane().add(btnSendDocument);
        getContentPane().add(btnPrint);
        getContentPane().add(scrPane);
        

        //initContent-----------
		initContent();	//must be done before adding the changeListeners, other wise when content in those fields changes the 
						//content in valBatchId will be reset.
        //listeners－－－－－－－－
		btnLeftDay.addActionListener(this);
		btnToday.addActionListener(this);
		btnRightDay.addActionListener(this);
		
		btnLeftMonth.addActionListener(this);
		btnThisMonth.addActionListener(this);
		btnRightMonth.addActionListener(this);
		
		btnSendDocument.addActionListener(this);
        btnPrint.addActionListener(this);

        addChangeListener(valBatchID, this);
        cbxShowOnlyMine.addActionListener(this);
        
        addChangeListener();
        getContentPane().addComponentListener(this);
    }

	private void addChangeListener() {
		addChangeListener(tfdYearFrom, this);
        addChangeListener(tfdMonthFrom,this);
        addChangeListener(tfdDayFrom,this);
        addChangeListener(tfdYearTo,this);
        addChangeListener(tfdMonthTo,this);
        addChangeListener(tfdDayTo,this);
        
        addChangeListener(tfdHourFrom,this);
        addChangeListener(tfdMinuteFrom,this);
        addChangeListener(tfdSecondFrom,this);
        addChangeListener(tfdHourTo,this);
        addChangeListener(tfdMinuteTo,this);
        addChangeListener(tfdSecondTo,this);
	}
	
    //fill the dataselection area
	
	private void initContent() {
        //endtime
        String endTime = BarUtil.df.format(new Date());
        //temperal starttime, will be replaced by user specific start time (which is user last print time)
		String startTime = endTime.substring(0, 11) + BarOption.getStartTime();  //default value for start time, it's today's report. for those who has
																					// printted report, this start time will be replaced by last report time.
		//find the user's last print time.
		StringBuilder sql = new StringBuilder("select ANNIVERSARY from employee where id = ").append(LoginDlg.USERID);
		try {
			ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
            if(rs.next()) {
            	String lastPrintTime = rs.getString("ANNIVERSARY");
            	if(lastPrintTime != null && lastPrintTime.length() > 10) {
            		startTime = lastPrintTime;
            		if(startTime.indexOf(".") > 0) {
            			startTime = startTime.substring(0, startTime.indexOf(".") );
            		}
            	}
            }
		}catch(Exception exp) {
			L.e("ReportDlg ", "Exception when ertiving ANNIVERSARY." + sql, exp);
		}//so, by default, we set time to currentday(customized start time + current time), while if this user has printed before, then we set start time to last print time. 
		
		//if it's higher than manager, then also need to init the display of the batch id. for manager is next batch id, super_admin(boss) will be batchids since last print.
		if(LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS) { //above manager(admin and super_admin)
			//if it's above manager, find the batch number to display on dialog. for admin, will be one record, super admin might has more.
			//because only admin will update the systeminfo table with new record when it close a batch.
			sql = new StringBuilder("select * from systeminfo where version >= '").append(startTime).append("' order by id desc");
			try {
				ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
				curBatchIdx = "1"; //if didn't found any batch, means never print yet, set current batchIdx to 1
		    while(rs.next()) {
		    	if(LoginDlg.getUSERTYPE() == LoginDlg.ADMIN_STATUS) {
		    		curBatchIdx = String.valueOf(DBUtil.getCursorInt(rs,"id") + 1);
		    		break;
		    	}else {	// if it's super admin, then no need to show "next" batch number.
		    		curBatchIdx = curBatchIdx.concat(String.valueOf(DBUtil.getCursorInt(rs,"id"))).concat(",");
		    	}
		    }
			}catch(Exception exp) {
				L.e("ReportDlg ", "Exception when ertiving last record." + sql, exp);
			}
			if(curBatchIdx.endsWith(",")) {
				curBatchIdx = curBatchIdx.substring(0, curBatchIdx.length() - 1);
			}
        	valBatchID.setText(curBatchIdx);
		}

		lblCloseBatch.setVisible(LoginDlg.getUSERTYPE() == LoginDlg.ADMIN_STATUS);
    	valBatchID.setVisible(LoginDlg.getUSERTYPE() == LoginDlg.ADMIN_STATUS);
    	cbxShowOnlyMine.setVisible(LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS);
	    //start date and time-----------------
	    updateTimeFields(startTime, endTime);
	    
	    updateStartAndEndTime();
	    updatePreviewContent();
    }

	//@Make sure the stateChanged method is not triggered. 
	//@if there's only one record and inputted is equal to cur batchID, then use current time as end date.
	private void updateTimeFields(String startTime, String endNow) {
		stopTriggerEventflag = true;
		int p;
		p = startTime.indexOf(" ");
		if (p < 0) {
			startTime = endNow;
			p = startTime.indexOf(" ");
		}
	    String startDate = startTime.substring(0, p);
	    startTime = startTime.substring(p+1);
	      
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
	    
	    //end date and time-------------------
	    p = endNow.indexOf(" ");
	    String endDate = endNow.substring(0, p);
	    endNow = endNow.substring(p+1);
	    
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
	    
	    SwingUtilities.invokeLater(()->stopTriggerEventflag = false);
	}
    
    public static ArrayList<Bill> queryRelevantCompleteAndRefundBillList(String startTime, String endTimeStr, boolean showOnlyMine) {
        StringBuilder sql = new StringBuilder("select * from bill, employee where createTime >= '").append(startTime)
        		.append("' and createTime <= '").append(endTimeStr)
        		.append("' and bill.employeeId = employee.id ")
        		.append(" and (bill.status < ").append(DBConsts.ORIGINAL)
        		.append(" or bill.status = ").append(DBConsts.COMPLETED).append(")");
        if((LoginDlg.getUSERTYPE() < LoginDlg.ADMIN_STATUS && !BarOption.showRecordFromOtherWaiter())//if is not admin,
        		|| showOnlyMine) {	// or it's admin but it want to show only then get out only user related records.
        	sql.append(" and employee.id = ").append(LoginDlg.USERID);
        }

        ArrayList<Bill> salesAndRefunds = new ArrayList<Bill>();
        
        try {
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            rs.afterLast();
            rs.relative(-1);
            rs.beforeFirst();
            while (rs.next()) {
            	Bill bill = new Bill();
            	bill.setId(DBUtil.getCursorInt(rs,"id"));
            	bill.setCreateTime(DBUtil.getCursorString(rs,"createTime"));
            	bill.setTableID(DBUtil.getCursorString(rs,"tableID"));
            	bill.setBillIndex(DBUtil.getCursorString(rs,"billIndex"));
            	bill.setTotal(DBUtil.getCursorInt(rs,"total"));
            	bill.setDiscount(DBUtil.getCursorInt(rs,"discount"));
            	bill.setCaseReceived(DBUtil.getCursorInt(rs,"cashReceived"));
            	bill.setDebitReceived(DBUtil.getCursorInt(rs,"debitReceived"));
            	bill.setVisaReceived(DBUtil.getCursorInt(rs,"visaReceived"));
            	bill.setMasterReceived(DBUtil.getCursorInt(rs,"masterReceived"));
            	bill.setOtherReceived(DBUtil.getCursorString(rs,"otherReceived"));
            	bill.setTip(DBUtil.getCursorInt(rs,"tip"));
            	bill.setCashback(DBUtil.getCursorInt(rs,"cashback"));
            	bill.setStatus(DBUtil.getCursorInt(rs,"status"));
            	bill.setEmployeeName(DBUtil.getCursorString(rs,"employee.nName"));
            	bill.setComment(DBUtil.getCursorString(rs,"Comment"));
            	bill.setOpenTime(DBUtil.getCursorString(rs,"OpenTime"));
            	salesAndRefunds.add(bill);
            }
            rs.close();// 关闭
        } catch (SQLException e) {
            ErrorUtil.write(e);
        }

        return salesAndRefunds;
    }
    
    private JLabel lblCloseBatch;
    private JTextField valBatchID;

    private JCheckBox cbxShowOnlyMine;

    private JCheckBox cbxIsSilendMode;
    private JButton btnSendDocument;
    private JButton btnPrint;
    private JTextArea txpPreview;
    private JScrollPane scrPane;
     
    private JButton btnLeftDay;
    private JButton btnToday;
    private JButton btnRightDay;
    private JButton btnLeftMonth;
    private JButton btnThisMonth;
    private JButton btnRightMonth;
    
    private JLabel lblFrom;
    private JLabel lblTo;
     
    private JLabel lblYearFrom;
    private JLabel lblMonthFrom;
    private JLabel lblDayFrom;
    private JLabel lblYearTo;
    private JLabel lblMonthTo;
    private JLabel lblDayTo;
    
    private JTextField tfdYearFrom;
    private JTextField tfdMonthFrom;
    private JTextField tfdDayFrom;
    private JTextField tfdYearTo;
    private JTextField tfdMonthTo;
    private JTextField tfdDayTo;

    private JLabel lblHourFrom;
    private JLabel lblMinuteFrom;
    private JLabel lblSecondFrom;
    private JLabel lblHourTo;
    private JLabel lblMinuteTo;
    private JLabel lblSecondTo;
    
    private JTextField tfdHourFrom;
    private JTextField tfdMinuteFrom;
    private JTextField tfdSecondFrom;
    private JTextField tfdHourTo;
    private JTextField tfdMinuteTo;
    private JTextField tfdSecondTo;

	
	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 * 
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
	    Objects.requireNonNull(text);
	    Objects.requireNonNull(changeListener);
	    DocumentListener dl = new DocumentListener() {
	        private int lastChange = 0, lastNotifiedChange = 0;

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	            lastChange++;
	            SwingUtilities.invokeLater(() -> {
	                if (lastNotifiedChange != lastChange) {
	                    lastNotifiedChange = lastChange;
	                    changeListener.stateChanged(new ChangeEvent(text));
	                }
	            });
	        }
	    };
	    text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
	        Document d1 = (Document)e.getOldValue();
	        Document d2 = (Document)e.getNewValue();
	        if (d1 != null) d1.removeDocumentListener(dl);
	        if (d2 != null) d2.addDocumentListener(dl);
	        dl.changedUpdate(null);
	    });
	    Document d = text.getDocument();
	    if (d != null) d.addDocumentListener(dl);
	}
}
