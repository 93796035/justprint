package org.cas.client.platform.bar.dialog;

import android.database.Cursor;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import com.just.print.app.AikaPos;
import com.just.print.util.JOptionPane;

//import java.awt.Color;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentEvent;
//import java.awt.event.ComponentListener;
//import java.awt.event.WindowEvent;
//import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

//import javax.swing.JButton;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JTextField;
//import javax.swing.JToggleButton;
//import javax.swing.SwingConstants;
//import javax.swing.SwingUtilities;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.action.Cmd_OpenDrawer;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.panel.BillPanel;
//import org.cas.client.platform.bar.pannel.BarFrame;
//import org.cas.client.platform.bar.pannel.BillPanel;
//import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.print.PrintService;
//import org.cas.client.platform.bar.uibeans.NumButton;
//import org.cas.client.platform.cascontrol.dialog.logindlg.LoginDlg;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;
//import org.cas.client.platform.pimmodel.PIMDBModel;
//import org.cas.client.resource.international.DlgConst;

public class PayDlg extends DialogFragment
//		implements ActionListener, ComponentListener, WindowListener
{

//	public ChangeDlg changeDlg;
//	private JToggleButton btnSource;
	//flag
	boolean isAllContentSelected;
	
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

//	private ArrayList<JLabel> labels = new ArrayList<JLabel>();
//	private ArrayList<JButton> values = new ArrayList<JButton>();
	
    public PayDlg() {
        initComponent();
    }
    
    public void updateBill(int billId) {
//    	StringBuilder sb = new StringBuilder();
//
//		String curTitle = getTitle();
//		//calculate the received of different kind. new on-screen received value = old on-screen value + value new inputted.
//		//old~means the value in db,
//		//onsrc value~ the value was first "value in db - cashback/tip - status, and do not change after that is use added money with this dialog several times.
//		//new~ means the latest onsrc + the new inputted content.
//		//@Note: we do not use old + new input directly, because maybe the on-screen one is changed many times---when using this dlg to add a little (but not enough) money.
//		//@NOTe: we do not use onSrc + new input, because onsrc value do not change after first time calculated.
//		if(curTitle.startsWith(BarFrame.consts.EnterCashPayment())){
//			int newCashReceived = reflectNewInput(valCashReceived.getText()) + (int)(Float.valueOf(valLeft.getText()) * 100);
//			sb.append("update bill set cashReceived = ").append(oldCashReceived + newCashReceived - onSrcCashReceived).append(" where id = ").append(billId);
//		} else if(curTitle.startsWith(BarFrame.consts.EnterDebitPayment())) {
//			int newDebitReceived = reflectNewInput(valDebitReceived.getText());
//			sb.append("update bill set debitReceived = ").append(oldDebitReceived + newDebitReceived - onSrcDebitReceived).append(" where id = ").append(billId);
//		} else if(curTitle.startsWith(BarFrame.consts.EnterVisaPayment())) {
//			int newVisaReceived = reflectNewInput(valVisaReceived.getText());
//			sb.append("update bill set visaReceived = ").append(oldVisaReceived + newVisaReceived - onSrcVisaReceived).append(" where id = ").append(billId);
//		} else if(curTitle.startsWith(BarFrame.consts.EnterMasterPayment())) {
//			int newMasterReceived = reflectNewInput(valMasterReceived.getText());
//			sb.append("update bill set masterReceived = ").append(oldMasterReceived + newMasterReceived - onSrcMasterReceived).append(" where id = ").append(billId);
//		}
//
//		try {
//			PIMDBModel.getStatement().executeUpdate(sb.toString());
//		}catch(Exception e) {
//			ErrorUtil.write(e);
//		}
    }

	private int reflectNewInput(String existingAmount) {
		Float existingMoney = 0f;
		Float newAddedMoney = 0f;
		
		try {
			existingMoney = Float.valueOf(existingAmount);
		}catch(Exception e) {
			//do nothing.
		}
		
//		try {
//			newAddedMoney = Float.valueOf(tfdNewReceived.getText());
//		}catch(Exception e) {
//			//do nothing.
//		}
		
		return Math.round(existingMoney * 100 + newAddedMoney * 100);
	}
    
	//it's public because there's a menu on salesPane is calling this method. ---didn't find who called it, I changed to private.
	//the difference between this and close currentBill is that this method close bill with an update of received money
    private void exactMoney(int billId, String pay) {
//   		try {
//   			String paidTime = BarUtil.df.format(new Date());
//   			//NOTE: should consider might been paid with card, so the new received should be the left not the total..
//   	    	StringBuilder sql = new StringBuilder("update bill set ").append(pay)
//   	    			.append("Received = ").append(Math.round(Float.valueOf(inputedContent) * 100))
//	   	    	//.append(", DebitReceived = 0, VisaReceived = 0, MasterReceived = 0")
//   	    		// why do we need to update create time? because this field no need to indicate how many minutes to display on table anymore,
//   	    		//we use this field to indicate when the bill was really finished, it will determine which batch this bill will display on.)
//   	    		.append(", createTime = '").append(paidTime).append("'")
//	   	    	.append(", status = ").append(DBConsts.COMPLETED)
//	   	    	.append(", userId = ").append(LoginDlg.USERID)
//	   	    	.append(" where id = ").append(billId);
//			PIMDBModel.getStatement().executeUpdate(sql.toString());
//			//update the status of relevant outputs.
//			BarFrame.instance.updateOutputStatus(DBConsts.COMPLETED);
//   		}catch(Exception e) {
//			ErrorUtil.write(e);
//		}
//
    }
    
	public void initContent(BillPanel billPanel) {
//		int billId = billPanel.getBillID();
//    	initMoneyDisplay(billId);
//    	btnExact.setText(BarFrame.consts.EXACT());
    }
	
	private void initMoneyDisplay(int billId) {
		StringBuilder sb = new StringBuilder("select * from bill where id = " + billId);
    	try {
    		Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
            rs.moveToNext();
        	
        	//save old values
            oldTotal = DBUtil.getCursorInt(rs,"total");
        	oldCashReceived =DBUtil.getCursorInt(rs,"cashReceived");
        	oldDebitReceived = DBUtil.getCursorInt(rs,"debitReceived");
        	oldVisaReceived = DBUtil.getCursorInt(rs,"visaReceived");
        	oldMasterReceived = DBUtil.getCursorInt(rs,"masterReceived");
        	oldOtherReceived = DBUtil.getCursorString(rs,"otherReceived");
        	
        	oldCashback = DBUtil.getCursorInt(rs,"cashback"); //cash back is negative in db. so use "+" instead of "-"
        	oldTip = DBUtil.getCursorInt(rs,"tip");
        	oldStatus = DBUtil.getCursorInt(rs,"status");
        	//cashReceived
        	onSrcCashReceived = oldCashReceived + oldCashback;
        	if(oldStatus < DBConsts.ORIGINAL) {
	        	if(onSrcCashReceived + oldStatus > 0) {
	        		onSrcCashReceived += oldStatus;
	        		oldStatus = 0;
	        	}else {
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
        	if(oldTip > 0) {
        		//debitReceived
	        	if(oldDebitReceived > oldTip) {
	        		onSrcDebitReceived -= oldTip;
	        	}
	        	
	        	//visaReceived
	            if(onSrcVisaReceived > oldTip) {
	            	onSrcVisaReceived -= oldTip;
	        	}
	            
	            //masterReceived
	            if(onSrcMasterReceived > oldTip) {
	            	onSrcMasterReceived -= oldTip;
	        	}
        	}
            //total
            int total = oldTotal;
            //left
            oldLeft = onSrcOtherReceived != null && onSrcOtherReceived.length() > 0 && !"null".equals(onSrcOtherReceived.toLowerCase()) ?
            		0 : total - onSrcCashReceived - onSrcDebitReceived - onSrcVisaReceived - onSrcMasterReceived;
            
            //set the interface value.
//            valCashReceived.setText(BarUtil.formatMoney(onSrcCashReceived / 100.0));
//            valDebitReceived.setText(BarUtil.formatMoney(onSrcDebitReceived / 100.0));
//            valVisaReceived.setText(BarUtil.formatMoney(onSrcVisaReceived / 100.0));
//            valMasterReceived.setText(BarUtil.formatMoney(onSrcMasterReceived / 100.0));
//
//            String totalStr = BarUtil.formatMoney(total / 100.0);
//            if(getTitle().equals(BarFrame.consts.EnterCashPayment())){
//            	totalStr = BarUtil.canadianPennyRound(totalStr);
//            }
//        	valTotal.setText(totalStr);
//
//        	String leftStr = BarUtil.formatMoney(oldLeft / 100.0);
//            if(getTitle().equals(BarFrame.consts.EnterCashPayment())){
//            	leftStr = BarUtil.canadianPennyRound(leftStr);
//            }
//            valLeft.setText(leftStr);
//
            reLayout();
            
    	}catch(Exception e) {
    		ErrorUtil.write(e);
    	}
	}
	
//	public void initContent(List<BillPanel> unclosedBillPanels) {
//		BillPanel billPanel = unclosedBillPanels.get(0);
//		
//		Float total = 0.0f;
//		for (BillPanel bP : unclosedBillPanels) {
//			total += Float.valueOf(bP.valTotlePrice.getText());
//		}
//		
//		int billId = billPanel.getBillId();
//    	initMoneyDisplay(billId);
//    }
	
    /*
     * 对话盒的布局独立出来，为了在对话盒尺寸发生改变后，界面各元素能够重新布局， 使整体保持美观。尤其在Linux系列的操作系统上，所有的对话盒都必须准备好应对用户的拖拉改变尺寸。
     * @NOTE:因为setBounds方法本身不会触发事件导致重新布局，所以本方法中设置Bounds之后调用了reLayout。
     */
    public void reLayout() {
//    	for (JLabel jLabel : labels) {
//    		getContentPane().remove(jLabel);
//		}
//    	for (JButton jLabel : values) {
//    		getContentPane().remove(jLabel);
//		}
//    	labels.clear();
//    	values.clear();
//
//    	if(!valCashReceived.getText().equals("0.00")) {
//    		labels.add(lblCashReceived);
//    		values.add(valCashReceived);
//    	}
//    	if(!valDebitReceived.getText().equals("0.00")) {
//    		labels.add(lblDebitReceived);
//    		values.add(valDebitReceived);
//    	}
//    	if(!valVisaReceived.getText().equals("0.00")) {
//    		labels.add(lblVisaReceived);
//    		values.add(valVisaReceived);
//    	}
//    	if(!valMasterReceived.getText().equals("0.00")) {
//    		labels.add(lblMasterReceived);
//    		values.add(valMasterReceived);
//    	}
//
//    	int WIDTH = 60;
//    	if(labels.size() > 0) {
//        	JLabel lbl1 = labels.get(0);
//        	JButton val1 = values.get(0);
//        	getContentPane().add(lbl1);
//        	getContentPane().add(val1);
//	    	lbl1.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP,
//	    			lbl1.getPreferredSize().width, lblTotal.getPreferredSize().height);
//	        val1.setBounds(lbl1.getX() + lbl1.getWidth(), lbl1.getY(),
//	        		WIDTH, lblCashReceived.getHeight());
//	        if(labels.size() > 1) {
//	        	JLabel lbl2 = labels.get(1);
//	        	JButton val2 = values.get(1);
//	        	getContentPane().add(lbl2);
//	        	getContentPane().add(val2);
//	        	lbl2.setBounds(CustOpts.HOR_GAP, lbl1.getY() + lbl1.getHeight() + CustOpts.VER_GAP,
//	        			lbl2.getPreferredSize().width, lbl2.getPreferredSize().height);
//		        val2.setBounds(lbl2.getX() + lbl2.getWidth(), lbl2.getY(),
//		        		WIDTH, lbl2.getHeight());
//		        if(labels.size() > 2) {
//		        	JLabel lbl3 = labels.get(2);
//		        	JButton val3 = values.get(2);
//		        	getContentPane().add(lbl3);
//		        	getContentPane().add(val3);
//		        	lbl3.setBounds(lbl1.getX() + lbl1.getWidth() + WIDTH + CustOpts.HOR_GAP, CustOpts.VER_GAP,
//		        			lbl3.getPreferredSize().width, lbl3.getPreferredSize().height);
//		            val3.setBounds(lbl3.getX() + lbl3.getWidth(), lbl3.getY(),
//		            		WIDTH, lbl3.getHeight());
//		            if(labels.size() > 3) {
//		            	JLabel lbl4 = labels.get(3);
//		            	JButton val4 = values.get(3);
//		            	getContentPane().add(lbl4);
//		            	getContentPane().add(val4);
//		            	lbl4.setBounds(lbl3.getX(), lbl2.getY(),
//		            			lbl4.getPreferredSize().width, lbl4.getPreferredSize().height);
//		            	val4.setBounds(lbl4.getX() + lbl4.getWidth(), lbl4.getY(),
//		            			WIDTH, lbl4.getHeight());
//		            }
//		        }
//	        }
//    	}
//
//        tfdNewReceived.setBounds(CustOpts.BTN_WIDTH_NUM * 2 + CustOpts.HOR_GAP * 4, CustOpts.VER_GAP, 180, 40);
//
////    	lblTotal.setBounds(tfdNewReceived.getX() + tfdNewReceived.getWidth() + CustOpts.HOR_GAP, CustOpts.VER_GAP,
////    			lblTotal.getPreferredSize().width, lblTotal.getPreferredSize().height);
////        valTotal.setBounds(lblTotal.getX() + lblTotal.getWidth(), lblTotal.getY(), 100 - lblTotal.getWidth(), lblTotal.getHeight());
//
//        lblLeft.setBounds(tfdNewReceived.getX() + tfdNewReceived.getWidth() + CustOpts.HOR_GAP, tfdNewReceived.getY() + 10,
//        		lblLeft.getPreferredSize().width, lblLeft.getPreferredSize().height);
//        valLeft.setBounds(lblLeft.getX() + lblLeft.getWidth() + CustOpts.HOR_GAP, tfdNewReceived.getY(),
//        		getWidth() - lblLeft.getX() - lblLeft.getWidth() - CustOpts.HOR_GAP * 2, tfdNewReceived.getHeight());
//
//        btn10.setBounds(CustOpts.HOR_GAP,  tfdNewReceived.getY() + tfdNewReceived.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn20.setBounds(btn10.getX() + btn10.getWidth() + CustOpts.HOR_GAP, btn10.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn30.setBounds(btn10.getX(), btn10.getY() + btn10.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn40.setBounds(btn20.getX(), btn30.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn50.setBounds(btn30.getX(), btn30.getY() + btn30.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn100.setBounds(btn40.getX(), btn50.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btnExact.setBounds(btn50.getX(), btn50.getY() + btn50.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM * 2 + CustOpts.HOR_GAP, CustOpts.BTN_WIDTH_NUM);
//
//        num1.setBounds(btn20.getX() + btn20.getWidth() + CustOpts.HOR_GAP * 2,  tfdNewReceived.getY() + tfdNewReceived.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num2.setBounds(num1.getX() + num1.getWidth() + CustOpts.HOR_GAP, num1.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num3.setBounds(num2.getX() + num2.getWidth() + CustOpts.HOR_GAP, num1.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num4.setBounds(num1.getX(), num1.getY() + num1.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num5.setBounds(num4.getX() + num4.getWidth() + CustOpts.HOR_GAP, num4.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num6.setBounds(num5.getX() + num5.getWidth() + CustOpts.HOR_GAP, num5.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num7.setBounds(num4.getX(), num4.getY() + num1.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num8.setBounds(num7.getX() + num7.getWidth() + CustOpts.HOR_GAP, num7.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num9.setBounds(num8.getX() + num8.getWidth() + CustOpts.HOR_GAP, num8.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num0.setBounds(num7.getX(), num7.getY() + num7.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        point.setBounds(num0.getX() + num0.getWidth() + CustOpts.HOR_GAP, num0.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        if(point.isVisible())
//        	back.setBounds(point.getX() + point.getWidth() + CustOpts.HOR_GAP, point.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        else
//        	back.setBounds(num0.getX() + num0.getWidth() + CustOpts.HOR_GAP, num0.getY(), CustOpts.BTN_WIDTH_NUM * 2 + CustOpts.HOR_GAP, CustOpts.BTN_WIDTH_NUM);
//    	ok.setBounds(num3.getX() + num3.getWidth() + CustOpts.HOR_GAP, num3.getY(),
//    			CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM * 4 + CustOpts.VER_GAP * 3);
//
//        validate();
    }

//   OO
//    @Override
//	public void onClick(View v) {
    	
//        inputedContent = tfdNewReceived.getText();
//        SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
//		BillPanel billPanel = salesPanel.billPanel;
//
//        Object o = e.getSource();
//        if (o == ok) {
//        	//check content format
//        	try {
//        		if(Float.valueOf(inputedContent) == 0) {
//        			return;
//        		}
//        	}catch(Exception exp) {
//                JOptionPane.showMessageDialog(this, DlgConst.FORMATERROR);
//        		return;
//        	}
//
//        	int billOldStatus = getBillStatus(billPanel.getBillID());
//        	//check if left moeny is 0.
//        	int left = Math.round(Float.valueOf(valLeft.getText()) * 100);
//
//        	//no matter it's closed or not, we need to update the pay info of the bill. ---why?
//    		updateBill(billPanel.getBillID()); //updated the previouse bill, while it doesn't hurt. because no transaction to web-mev happends.
//
//        	if(left > 0) {
//        		//if still smaller than total, then set the partialPaid flag. otherwise reset flat.
//        		((SalesPanel)BarFrame.instance.panels[2]).partialPaid = left < (Float.valueOf(valTotal.getText()) * 100);
//
////	        	if(JOptionPane.showConfirmDialog(this, BarFrame.consts.reCeivedMoneyNotEnough(), DlgConst.DlgTitle, JOptionPane.YES_NO_OPTION) == 0) {
////	        		//user selected to close the bill. update the bill to closed
////	        		billClosed = closeCurrentBill();
////	        		//if selected yes, then update the table status.
////	        		if(SalesPanel.isLastBillOfCurTable()) {
////	        			SalesPanel.resetCurTableDBStatus();
////	        		}
////	        	}
//	        	JOptionPane.showMessageDialog(this, BarFrame.consts.Due() + " : "  + BarOption.getMoneySign()
//        				+ BarUtil.formatMoney(left/100f));
//	        	//new ChangeDlg(BarFrame.instance, BarFrame.consts.Due() + BarOption.getMoneySign()
//        		//		+ BarUtil.format(left/100f)).setVisible(true); //it's a non-modal dialog.
//        	}else if(left <= 0) {
//        		((SalesPanel)BarFrame.instance.panels[2]).partialPaid = false;
//
//            	this.setVisible(false);
//            	//change to always display the change dialog.
//            	//if(left <= 0) {	//if it's equal to 0, then do not display the change dialog.
//            	showChangeDlg(billPanel, left);
//            	//}
//				//because except the first bill all bill was created when needed. so we need to check if the receipt is the first bill?
//				if(billPanel.getStatus() >= DBConsts.BILL_PRINTED) {
//					billPanel.expireAndReGenerateBill(null);
//				}
//	        	addLatestPayIntoComment(billPanel);	//have to be done after the "expireAndReGenerate" method, because this method will change comment.
//            	//let's qa decide if we should go back to table interface.
//        		boolean needToBePrinted = billOldStatus != DBConsts.BILL_PRINTED || !BarOption.isSavePrintInvoiceWhenBilled();
//	        	PrintService.printReceipt(billPanel, getTitle().equals(BarFrame.consts.EnterCashPayment()), true, needToBePrinted);
//
//	        	BarFrame.instance.completeCurrentBill();
//	        	BarFrame.instance.actionAfterBillClosed();
//
//	        	//put opendrawer at the end to avoid block the printing. because for those don't have this device, this might took a while.
//            	if(BarOption.hasDrawer() && getTitle().equals(BarFrame.consts.EnterCashPayment())){
//            		Cmd_OpenDrawer.getInstance().actionPerformed(null);
//            	}
//        	}
//
//
//        	resetContent();
//        	this.setVisible(false);
//
//        } else if(o == btnExact) {//update bill and display change 0.00;
//
//        	String strPay = "other";
//    		String curTitle = getTitle();
//    		//calculate the received of diffent kind
//    		if(curTitle.startsWith(BarFrame.consts.EnterCashPayment())){
//    			strPay = "cash";
//    		} else if(curTitle.startsWith(BarFrame.consts.EnterDebitPayment())) {
//    			strPay = "debit";
//    		} else if(curTitle.startsWith(BarFrame.consts.EnterVisaPayment())) {
//    			strPay = "visa";
//    		} else if(curTitle.startsWith(BarFrame.consts.EnterMasterPayment())) {
//    			strPay = "master";
//    		}
//    		int billOldStatus = getBillStatus(billPanel.getBillID());
//
//        	inputedContent = BarUtil.formatMoney(oldLeft / 100.0);	//if it's paying with gift card, will determine how much to update the account left base on this property.
//
//        	//because except the first bill all bill was created when needed. so we need to check if the receipt is the first bill?
//			if(billPanel.getStatus() > DBConsts.ORIGINAL) {
//				billPanel.expireAndReGenerateBill(null);
//			}
//        	addLatestPayIntoComment(billPanel);
//        	exactMoney(billPanel.getBillID(), strPay);
//
//        	if(getTitle().equals(BarFrame.consts.EnterCashPayment())){
//        		Cmd_OpenDrawer.getInstance().actionPerformed(null);
//        	}
//        	//let's qa decide if we should go back to table interface.
//        	boolean needToBePrinted = billOldStatus != DBConsts.BILL_PRINTED || !BarOption.isSavePrintInvoiceWhenBilled();
//          	PrintService.printReceipt(billPanel, getTitle().equals(BarFrame.consts.EnterCashPayment()), true, needToBePrinted);
//
//        	BarFrame.instance.actionAfterBillClosed();
//
//        	resetContent();
//        	this.setVisible(false);
//        } else if( o == valCashReceived || o == valDebitReceived || o == valVisaReceived || o == valMasterReceived) {
//        	StringBuilder sb = new StringBuilder("update bill set ");
//        	if(o == valCashReceived ) {
//        		sb.append("cashReceived = 0 where id = ");
//        	} else if(o == valDebitReceived) {
//        		sb.append("debitReceived = 0 where id = ");
//        	} else if(o == valVisaReceived) {
//        		sb.append("visaReceived = 0 where id = ");
//        	} else if(o == valMasterReceived) {
//        		sb.append("masterReceived = 0 where id = ");
//        	}
//        	sb.append(billPanel.getBillID());
//        	try {
//    			PIMDBModel.getStatement().executeUpdate(sb.toString());
//    		}catch(Exception exp) {
//    			ErrorUtil.write(exp);
//    		}
//        	initMoneyDisplay(billPanel.getBillID());
//        	revalidate();
//        	repaint();
//        } else {
//	        if(isAllContentSelected)
//	        	inputedContent = "";
//
//	        if (o == back) {
//	            if (inputedContent != null && inputedContent.length() > 0) {
//	                tfdNewReceived.setText(inputedContent.substring(0, inputedContent.length() - 1));
//	                if(!BarOption.isPointerInputStyle()) {
//	                	tfdNewReceived.setText("");
//	                }
//	                updateLeft();
//	            }
//	        } else if(o == btn10) {
//	        	increaseReceived(10);
//	        } else if(o == btn20) {
//	        	increaseReceived(20);
//	        } else if(o == btn30) {
//	        	increaseReceived(30);
//	        } else if(o == btn40) {
//	        	increaseReceived(40);
//	        } else if(o == btn50) {
//	        	increaseReceived(50);
//	        } else if(o == btn100) {
//	        	increaseReceived(100);
//	        } else if (o == num1) {
//	        	concatReceived("1");
//	        } else if (o == num2) {
//	        	concatReceived("2");
//	        } else if (o == num3) {
//	        	concatReceived("3");
//	        } else if (o == num4) {
//	        	concatReceived("4");
//	        } else if (o == num5) {
//	        	concatReceived("5");
//	        } else if (o == num6) {
//	        	concatReceived("6");
//	        } else if (o == num7) {
//	        	concatReceived("7");
//	        } else if (o == num8) {
//	        	concatReceived("8");
//	        } else if (o == num9) {
//	        	concatReceived("9");
//	        } else if (o == num0) {
//	        	concatReceived("0");
//	        } else if(o == point) {
//	        	concatReceived(".");
//	        }
//        }
//        isAllContentSelected = false;
//    }

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
//		changeDlg = new ChangeDlg(BarFrame.instance, BarOption.getMoneySign()
//				+ BarUtil.formatMoney((0 - left)/100f)); //it's a non-modal dialog.
//		//if the last pay was with cash, then might need cash back (no change, paid with a "50" bill)
//		if(getTitle().equals(BarFrame.consts.EnterCashPayment())){
//			changeDlg.setTitle(BarFrame.consts.Change());
//			DBUtil.updateBill(billPanel.getBillID(), "cashback", oldCashback + left);
//		}else {
//			changeDlg.setTitle(BarFrame.consts.Tip());
//			DBUtil.updateBill(billPanel.getBillID(), "TIP", oldTip - left);	//otherwise, treated as tip. the tip in DB are positive, because it means we earned money.
//		}
//
//		changeDlg.setVisible(true);		//note: is't a non-modal dialog.
	}

	public static int getBillStatus(int billId) {
		StringBuilder sb = new StringBuilder("select * from bill where id = ").append(billId);
		try {
			Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
			if(rs.moveToNext()) {
				return DBUtil.getCursorInt(rs,"status");
			}
		}catch(Exception e) {
			ErrorUtil.write(e);
		}
		return -1;
	}
    
    private void increaseReceived(int amount) {
//    	Float received = 0.0f;
//    	try{
//    		if(tfdNewReceived.getText().length() > 0) {
//    			received = Float.valueOf(tfdNewReceived.getText());
//    		}
//    	}catch(Exception e) {
//    		L.e("PayDlg-increaseReceived ", "exception when parsing content in tfdNewReceived into Float.", e);
//    	}
//    	received += amount;
//    	tfdNewReceived.setText(String.valueOf(received));
//        updateLeft();
    }
    
    private void concatReceived(String num){
//    	String newStr = tfdNewReceived.getText().concat(num);
//
//    	if(!BarOption.isPointerInputStyle()) {
//    		if(num.equals(".")) {
//    			int p = newStr.indexOf(".");
//        		if(p != newStr.lastIndexOf(".")) {
//        			newStr = newStr.substring(0, p) + newStr.substring(p + 1);
//        		}
//        		newStr = newStr + "00";
//        	}else if(newStr.length() == 1) {
//    			if(!newStr.equals(".")){
//    				newStr = ".0" + newStr;
//    			}
//    		}else if(newStr.length() == 2) {
//    			if(newStr.indexOf(".") < 0) {
//    				newStr = "." + newStr;
//    			}
//    		}else {
//    			int i = newStr.indexOf(".");
//    			if(i >= 0) {
//    				newStr = newStr.substring(0, i) + newStr.substring(i + 1, newStr.length());	//remove the point
//    			}
//    			newStr = newStr.substring(0, newStr.length() - 2) + "." + newStr.substring(newStr.length() - 2, newStr.length()); //put the point right place.
//			}
//    		if(newStr.startsWith("0")) {
//    			newStr = newStr.substring(1);
//    		}
//    	}
//    	if(maxInput > 0 && Float.valueOf(newStr) > maxInput) {
//    		JOptionPane.showMessageDialog(this, BarFrame.consts.InvalidInput());
//    	}else {
//	        tfdNewReceived.setText(newStr);
//	        updateLeft();
//    	}
    }
    
//	@Override
//	public void componentResized(ComponentEvent e) {}
//
//	@Override
//	public void componentMoved(ComponentEvent e) {}
//
//	@Override
//	public void componentShown(ComponentEvent e) {}
//
//	@Override
//	public void componentHidden(ComponentEvent e) {
//		if(btnSource != null)
//			btnSource.setSelected(false);
//	}

	public void updateLeft() {
//		Float total = Float.valueOf(valTotal.getText());
//		Float received = 0f;
//
//		try{
//			received += Float.valueOf(valCashReceived.getText()) + Float.valueOf(valDebitReceived.getText());
//			received += Float.valueOf(valVisaReceived.getText()) + Float.valueOf(valMasterReceived.getText());
//			received += Float.valueOf(tfdNewReceived.getText());
//		}catch(Exception e) {
//			L.e("PayDlg ", "Exception when adding every kind of received into received. ", e);
//		}
//
//		String leftStr = String.valueOf(Math.round(total * 100 - received * 100)/100f);
//        if(getTitle().equals(BarFrame.consts.EnterCashPayment())){
//        	leftStr = BarUtil.canadianPennyRound(leftStr);
//        }
//		valLeft.setText(leftStr);
//		if(BarFrame.secondScreen != null) {
//			String changeStr = leftStr.startsWith("-") ? leftStr.substring(1) : "-" + leftStr;
//			BarFrame.customerFrame.updateChange(String.valueOf(Math.round(received * 100)/100f), changeStr, getTitle().equals(BarFrame.consts.EnterCashPayment()));
//		}
	}
	
    void initComponent() {
//    	getContentPane().removeAll();
//
//        setResizable(false);
//        setModal(true);
//        //setAlwaysOnTop(true);
//
//        // 初始化－－－－－－－－－－－－－－－－
//
//        lblCashReceived = new JLabel(BarFrame.consts.CASH().substring(0,2) + ":");
//        valCashReceived = new JButton("");
//        lblDebitReceived = new JLabel(BarFrame.consts.DEBIT().substring(0,2) + ":");
//        valDebitReceived = new JButton("");
//        lblVisaReceived = new JLabel(BarFrame.consts.VISA().substring(0,2) + ":");
//        valVisaReceived = new JButton("");
//        lblMasterReceived = new JLabel(BarFrame.consts.MASTER().substring(0,2) + ":");
//        valMasterReceived = new JButton("");
//
//        tfdNewReceived = new JTextField();
//
//        lblTotal = new JLabel(BarFrame.consts.Total() + " : " + BarOption.getMoneySign());
//        valTotal = new JLabel("");
//        lblLeft = new JLabel(BarFrame.consts.Due() + ":");
//        valLeft = new JLabel("");
//
//        ok = new NumButton("<html><h1 style='text-align: center; padding-bottom: 5px; color:#18F507;'>✔</h1></html>");
//        back = new NumButton("←");
//
//        btn10 = new NumButton(BarOption.getMoneySign() + "10");
//        btn20 = new NumButton(BarOption.getMoneySign() + "20");
//        btn30 = new NumButton(BarOption.getMoneySign() + "30");
//        btn40 = new NumButton(BarOption.getMoneySign() + "40");
//        btn50 = new NumButton(BarOption.getMoneySign() + "50");
//        btn100 = new NumButton(BarOption.getMoneySign() + "100");
//        btnExact = new NumButton(BarFrame.consts.EXACT());
//
//        num1 = new NumButton("1");
//        num2 = new NumButton("2");
//        num3 = new NumButton("3");
//        num4 = new NumButton("4");
//        num5 = new NumButton("5");
//        num6 = new NumButton("6");
//        num7 = new NumButton("7");
//        num8 = new NumButton("8");
//        num9 = new NumButton("9");
//        num0 = new NumButton("0");
//        point = new NumButton(BarOption.isPointerInputStyle() ? "." : "00");
//
//        // 属性设置－－－－－－－－－－－－－－
//        // ok.setFont(CustOpts.custOps.getFontOfDefault());
//        back.setMargin(new Insets(0, 0, 0, 0));
//        ok.setMargin(new Insets(0, 0, 0, 0));
//
//        valCashReceived.setMargin(back.getMargin());
//        valDebitReceived.setMargin(back.getMargin());
//        valVisaReceived.setMargin(back.getMargin());
//        valMasterReceived.setMargin(back.getMargin());
//
//        btn10.setMargin(back.getMargin());
//        btn20.setMargin(back.getMargin());
//        btn30.setMargin(back.getMargin());
//        btn40.setMargin(back.getMargin());
//        btn50.setMargin(back.getMargin());
//        btn100.setMargin(back.getMargin());
//        btnExact.setMargin(back.getMargin());
//
//        num1.setMargin(back.getMargin());
//        num2.setMargin(back.getMargin());
//        num3.setMargin(back.getMargin());
//        num4.setMargin(back.getMargin());
//        num5.setMargin(back.getMargin());
//        num6.setMargin(back.getMargin());
//        num7.setMargin(back.getMargin());
//        num8.setMargin(back.getMargin());
//        num9.setMargin(back.getMargin());
//        num0.setMargin(back.getMargin());
//        point.setMargin(back.getMargin());
//
//        tfdNewReceived.setHorizontalAlignment(SwingConstants.RIGHT);
//        tfdNewReceived.setFont(BarOption.bigFont);
//        tfdNewReceived.setBackground(Color.WHITE);
//        lblLeft.setFont(BarOption.moderateBigFont);
//        valLeft.setFont(BarOption.lessBigFont);
//
//        // 布局---------------
//        int tHight = CustOpts.BTN_HEIGHT + CustOpts.BTN_WIDTH_NUM * 4 + 5 * CustOpts.VER_GAP
//                        + CustOpts.SIZE_EDGE + CustOpts.SIZE_TITLE + 20;
//        int tWidth = (CustOpts.BTN_WIDTH_NUM + CustOpts.HOR_GAP) * 6 + CustOpts.SIZE_EDGE * 2 + CustOpts.HOR_GAP + 5;
//        setBounds((CustOpts.SCRWIDTH - tWidth) / 2, (CustOpts.SCRHEIGHT - tHight) / 2, tWidth, tHight); // 对话框的默认尺寸。
//        getContentPane().setLayout(null);
//        reLayout();
//
//        // 搭建－－－－－－－－－－－－－
//        getContentPane().add(lblCashReceived);
//        getContentPane().add(valCashReceived);
//        getContentPane().add(lblDebitReceived);
//        getContentPane().add(valDebitReceived);
//        getContentPane().add(lblVisaReceived);
//        getContentPane().add(valVisaReceived);
//        getContentPane().add(lblMasterReceived);
//        getContentPane().add(valMasterReceived);
//
//        getContentPane().add(tfdNewReceived);
//
////        getContentPane().add(lblTotal);
////        getContentPane().add(valTotal);
//        getContentPane().add(lblLeft);
//        getContentPane().add(valLeft);
//
//        getContentPane().add(ok);
//        getContentPane().add(back);
//
//        getContentPane().add(btn10);
//        getContentPane().add(btn20);
//        getContentPane().add(btn30);
//        getContentPane().add(btn40);
//        getContentPane().add(btn50);
//        getContentPane().add(btn100);
//        getContentPane().add(btnExact);
//
//        getContentPane().add(num1);
//        getContentPane().add(num2);
//        getContentPane().add(num3);
//        getContentPane().add(num4);
//        getContentPane().add(num5);
//        getContentPane().add(num6);
//        getContentPane().add(num7);
//        getContentPane().add(num8);
//        getContentPane().add(num9);
//        getContentPane().add(num0);
//        getContentPane().add(point);
//
//        // 加监听器－－－－－－－－
//        ok.addActionListener(this);
//        back.addActionListener(this);
//        valCashReceived.addActionListener(this);
//        valDebitReceived.addActionListener(this);
//        valVisaReceived.addActionListener(this);
//        valMasterReceived.addActionListener(this);
//        num1.addActionListener(this);
//        num2.addActionListener(this);
//        num3.addActionListener(this);
//        num4.addActionListener(this);
//        num5.addActionListener(this);
//        num6.addActionListener(this);
//        num7.addActionListener(this);
//        num8.addActionListener(this);
//        num9.addActionListener(this);
//        num0.addActionListener(this);
//        point.addActionListener(this);
//
//        btn10.addActionListener(this);
//        btn20.addActionListener(this);
//        btn30.addActionListener(this);
//        btn40.addActionListener(this);
//        btn50.addActionListener(this);
//        btn100.addActionListener(this);
//        btnExact.addActionListener(this);
//        addComponentListener(this);
//        addWindowListener(this);
//
//        // init Contents
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                tfdNewReceived.grabFocus();
//            }
//        });
        
        reLayout();
    }

//	public void setFloatSupport(boolean setFloatSupport) {
//		point.setVisible(setFloatSupport);
//	}
//
//	private NumButton num1;
//    private NumButton num2;
//    private NumButton num3;
//    private NumButton num4;
//    private NumButton num5;
//    private NumButton num6;
//    private NumButton num7;
//    private NumButton num8;
//    private NumButton num9;
//    private NumButton num0;
//    private NumButton point;
//    private NumButton back;
//    private NumButton ok;
//
//    private NumButton btn10;
//    private NumButton btn20;
//    private NumButton btn30;
//    private NumButton btn40;
//    private NumButton btn50;
//    private NumButton btn100;
//    private NumButton btnExact;
//
//
//    private JLabel lblCashReceived;
//    public JButton valCashReceived;
//    private JLabel lblDebitReceived;
//    public JButton valDebitReceived;
//    private JLabel lblVisaReceived;
//    public JButton valVisaReceived;
//    private JLabel lblMasterReceived;
//    public JButton valMasterReceived;
//
//    public JTextField tfdNewReceived;
//
//    private JLabel lblTotal;
//    public static JLabel valTotal;
//    private JLabel lblLeft;
//    public static JLabel valLeft;
//
//	@Override
//	public void windowOpened(WindowEvent e) {}

//	@Override
//	public void windowClosing(WindowEvent e) {
//		if(tfdNewReceived.getText().length() > 0)
//			JOptionPane.showMessageDialog(this, BarFrame.consts.UnSavedContentWillBeLost());
//
//		resetContent();
//	}
//
//	private void resetContent() {
//		valCashReceived.setText("0.0");
//		valDebitReceived.setText("0.0");
//		valVisaReceived.setText("0.0");
//		valMasterReceived.setText("0.0");
//		valTotal.setText("0.0");
//		valLeft.setText("0.0");
//
//		tfdNewReceived.setText("");
//		maxInput = 0f;
//	}
//
//	@Override
//	public void windowClosed(WindowEvent e) {}
//
//	@Override
//	public void windowIconified(WindowEvent e) {}
//
//	@Override
//	public void windowDeiconified(WindowEvent e) {}
//
//	@Override
//	public void windowActivated(WindowEvent e) {}
//
//	@Override
//	public void windowDeactivated(WindowEvent e) {}
}
