package org.cas.client.platform.bar.dialog;

import android.annotation.SuppressLint;

import androidx.fragment.app.DialogFragment;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.PicturePane;

import org.cas.client.platform.bar.action.Cmd_CombineAll;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.action.Cmd_AddBill;
import org.cas.client.platform.bar.action.Cmd_AddTable;
import org.cas.client.platform.bar.action.Cmd_BillFoot;
import org.cas.client.platform.bar.action.Cmd_CancelAll;
import org.cas.client.platform.bar.action.Cmd_ChangePrice;
import org.cas.client.platform.bar.action.Cmd_ChangeTable;
import org.cas.client.platform.bar.action.Cmd_CheckInOut;
import org.cas.client.platform.bar.action.Cmd_CheckInOutList;
import org.cas.client.platform.bar.action.Cmd_CheckOrder;
import org.cas.client.platform.bar.action.Cmd_Color;
import org.cas.client.platform.bar.action.Cmd_DiscBill;
import org.cas.client.platform.bar.action.Cmd_DiscItem;
import org.cas.client.platform.bar.action.Cmd_DiscountCoupon;
import org.cas.client.platform.bar.action.Cmd_Employee;
import org.cas.client.platform.bar.action.Cmd_EqualBill;
import org.cas.client.platform.bar.action.Cmd_GiftCard;
import org.cas.client.platform.bar.action.Cmd_GiftCardSetting;
import org.cas.client.platform.bar.action.Cmd_Lang;
import org.cas.client.platform.bar.action.Cmd_Menu;
import org.cas.client.platform.bar.action.Cmd_Modify;
import org.cas.client.platform.bar.action.Cmd_ModifySetting;
import org.cas.client.platform.bar.action.Cmd_MoveItem;
import org.cas.client.platform.bar.action.Cmd_OpenDrawer;
import org.cas.client.platform.bar.action.Cmd_OtherPay;
import org.cas.client.platform.bar.action.Cmd_Pay;
import org.cas.client.platform.bar.action.Cmd_PrintAll;
import org.cas.client.platform.bar.action.Cmd_PrintBill;
import org.cas.client.platform.bar.action.Cmd_PrintOneBill;
import org.cas.client.platform.bar.action.Cmd_PrintOneInVoice;
import org.cas.client.platform.bar.action.Cmd_Printer;
import org.cas.client.platform.bar.action.Cmd_QTY;
import org.cas.client.platform.bar.action.Cmd_Refund;
import org.cas.client.platform.bar.action.Cmd_RemoveItem;
import org.cas.client.platform.bar.action.Cmd_Report;
import org.cas.client.platform.bar.action.Cmd_Return;
import org.cas.client.platform.bar.action.Cmd_Send;
import org.cas.client.platform.bar.action.Cmd_ServiceFee;
import org.cas.client.platform.bar.action.Cmd_Setting;
import org.cas.client.platform.bar.action.Cmd_SettingCoupon;
import org.cas.client.platform.bar.action.Cmd_SettingRule;
import org.cas.client.platform.bar.action.Cmd_SplitBill;
import org.cas.client.platform.bar.action.Cmd_SplitItem;
import org.cas.client.platform.bar.action.Cmd_Suspend;
import org.cas.client.platform.bar.action.Cmd_SuspendAll;
import org.cas.client.platform.bar.action.Cmd_Table;
import org.cas.client.platform.bar.action.Cmd_VoidOrder;
import org.cas.client.platform.bar.action.Cmd_VoidReason;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.uibeans.ArrowButton;
import org.cas.client.platform.bar.uibeans.FunctionButton;
import org.cas.client.platform.bar.uibeans.FunctionToggleButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.cascustomize.CustOpts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CmdBtnsManagerDlg extends DialogFragment
//		implements ComponentListener, ActionListener, MouseListener, MouseMotionListener
{
   
	private static final String KEY_TABLE_CMD = "TableCmd";
	private static final String KEY_BILL_CMD = "BillCmd";
	private static final String KEY_SALE_CMD = "SaleCmd";
	private static final String KEY_SETTING_CMD = "SettingCmd";
	private static final String[] LAYOUT_KEYS = new String[] {KEY_TABLE_CMD, KEY_BILL_CMD, KEY_SALE_CMD, KEY_SETTING_CMD};
	public static ArrayList<ISButton>[] groupedButtons;
	HashMap<String, List<ISButton>> btnListMap = new HashMap<String, List<ISButton>>();
	private static String[] defaultTableCmds = new String[4];
	
	public CmdBtnsManagerDlg() {
		super();
		initComponent();
	}

	public void initComponent() {
//		getContentPane().removeAll();
//		//title
////		setTitle(BarFrame.consts.OrgonizeCommands());
//
//		//list
//		originalList = new JList<String>(getBtnNames());
//		originalScrollBar = new JScrollPane(originalList);
//
//		//arrow buttons
//		toTable = new ArrowButton(">>");
//		toBill = new ArrowButton(">>");
//		toSale = new ArrowButton(">>");
//		toSetting = new ArrowButton(">>");
//
//		fromTable = new ArrowButton("<<");
//		fromBill = new ArrowButton("<<");
//		fromSale = new ArrowButton("<<");
//		fromSetting = new ArrowButton("<<");
//
//		//4 panels
//		tablePanle = new PicturePane(null);
//		billPanle = new PicturePane(null);
//		salePanle = new PicturePane(null);
//		settingPanle = new PicturePane(null);
//		panels = new PicturePane[]{tablePanle, billPanle, salePanle, settingPanle};
//
//		//reset button
//		reset = new FunctionButton(BarFrame.consts.RESET());
//		//close button
//		close = new FunctionButton(BarFrame.consts.Close());
//
//		//properties--------------------------------------------------------
//		Border etchedBorder = BorderFactory.createRaisedBevelBorder();
//		tablePanle.setBorder(BorderFactory.createTitledBorder(etchedBorder,"Table"));
//		tablePanle.MAX_CMDBTN_QT = BarFrame.instance.panels[0].MAX_CMDBTN_QT;
//		billPanle.setBorder(BorderFactory.createTitledBorder(etchedBorder,"Bill"));
//		billPanle.MAX_CMDBTN_QT = BarFrame.instance.panels[1].MAX_CMDBTN_QT;
//		salePanle.setBorder(BorderFactory.createTitledBorder(etchedBorder,"Sale"));
//		salePanle.MAX_CMDBTN_QT = BarFrame.instance.panels[2].MAX_CMDBTN_QT;
//		settingPanle.setBorder(BorderFactory.createTitledBorder(etchedBorder,"Setting"));
//		settingPanle.MAX_CMDBTN_QT = BarFrame.instance.panels[3].MAX_CMDBTN_QT;
//
//		//layout------------------------------------------------------------
//		tablePanle.setLayout(null);
//		billPanle.setLayout(null);
//		salePanle.setLayout(null);
//		settingPanle.setLayout(null);
//		getContentPane().setLayout(null);
//
//		//build
//		setBounds(BarFrame.instance.getX(), BarFrame.instance.getY(), BarFrame.instance.getWidth(), BarFrame.instance.getHeight());
//		getContentPane().add(originalScrollBar);
//		getContentPane().add(toTable);
//		getContentPane().add(toBill);
//		getContentPane().add(toSale);
//		getContentPane().add(toSetting);
//
//		getContentPane().add(fromTable);
//		getContentPane().add(fromBill);
//		getContentPane().add(fromSale);
//		getContentPane().add(fromSetting);
//
//		getContentPane().add(tablePanle);
//		getContentPane().add(billPanle);
//		getContentPane().add(salePanle);
//		getContentPane().add(settingPanle);
//
//		getContentPane().add(reset);
//		getContentPane().add(close);
//
//		//listener
//		toTable.addActionListener(this);
//		toBill.addActionListener(this);
//		toSale.addActionListener(this);
//		toSetting.addActionListener(this);
//
//		fromTable.addActionListener(this);
//		fromBill.addActionListener(this);
//		fromSale.addActionListener(this);
//		fromSetting.addActionListener(this);
//
//		tablePanle.addMouseListener(this);
//		billPanle.addMouseListener(this);
//		salePanle.addMouseListener(this);
//		settingPanle.addMouseListener(this);
//
//		reset.addActionListener(this);
//		close.addActionListener(this);
//
//		addComponentListener(this);
//
//		initPanel(KEY_TABLE_CMD, tablePanle, 0);
//		initPanel(KEY_BILL_CMD, billPanle, 1);
//		initPanel(KEY_SALE_CMD, salePanle, 2);
//		initPanel(KEY_SETTING_CMD, settingPanle, 3);
//
//		relayout();
	}
	
	private String[] getBtnNames() {
		String[] btnNames = new String[buttons.length];
		for(int i = 0; i < buttons.length; i++) {
			btnNames[i] = buttons[i].getText(); 
		}
		return btnNames;
	}

//	@Override
//	public void onClick(View v) {
//		Object o = e.getSource();
//		int[] ids = originalList.getSelectedIndices();
//
//		if(o == toTable) {
//			if(ids.length < 1) {
//				JOptionPane.showMessageDialog(this, BarFrame.consts.AtLeastOneShouldBeSelected());
//				return;
//			}
//			addCmd(KEY_TABLE_CMD, ids, tablePanle, 0);
//
//		}else if(o == toBill) {
//			if(ids.length < 1) {
//				JOptionPane.showMessageDialog(this, BarFrame.consts.AtLeastOneShouldBeSelected());
//				return;
//			}
//			addCmd(KEY_BILL_CMD, ids, billPanle, 1);
//
//		}else if(o == toSale) {
//			if(ids.length < 1) {
//				JOptionPane.showMessageDialog(this, BarFrame.consts.AtLeastOneShouldBeSelected());
//				return;
//			}
//			addCmd(KEY_SALE_CMD, ids, salePanle, 2);
//
//		}else if(o == toSetting) {
//			if(ids.length < 1) {
//				JOptionPane.showMessageDialog(this, BarFrame.consts.AtLeastOneShouldBeSelected());
//				return;
//			}
//			addCmd(KEY_SETTING_CMD, ids, settingPanle, 3);
//
//		}else if(o == fromTable) {
//			clearCmd(KEY_TABLE_CMD, tablePanle);
//
//		}else if(o == fromBill) {
//			clearCmd(KEY_BILL_CMD, billPanle);
//
//		}else if(o == fromSale) {
//			clearCmd(KEY_SALE_CMD, salePanle);
//
//		}else if(o == fromSetting) {
//			clearCmd(KEY_SETTING_CMD, settingPanle);
//
//		}else if(o == reset) {
//			if(JOptionPane.showConfirmDialog(BarFrame.instance, "Are you sure to delete all your configration about command buttons? (interface will be reset to default layout.", DlgConst.DlgTitle, JOptionPane.YES_NO_OPTION) != 0) {
//				return;
//			}
//
//			CustOpts.custOps.removeKeyAndValue(KEY_TABLE_CMD);
//			CustOpts.custOps.removeKeyAndValue(KEY_BILL_CMD);
//			CustOpts.custOps.removeKeyAndValue(KEY_SALE_CMD);
//			CustOpts.custOps.removeKeyAndValue(KEY_SETTING_CMD);
//			CustOpts.custOps.saveData();
//			//update ui
//			tablePanle.removeAll();
//			billPanle.removeAll();
//			salePanle.removeAll();
//			settingPanle.removeAll();
//		}else if(o == close) {
//			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.RestartNeeded());
//			this.dispose();
//		}
//
//		CustOpts.custOps.saveData();
//
//		invalidate();
//		revalidate();
//		repaint();
//	}
//
//	private void clearCmd(String key, PicturePane panel) {
//		CustOpts.custOps.setKeyAndValue(key, "");
//		CustOpts.custOps.saveData();
//		//update ui
//		panel.removeAll();
//	}
//
//	private void addCmd(String key, int[] ids, PicturePane panel, int idx) {
//		String strTableCmds = (String)CustOpts.custOps.getValue(key);
//		if(strTableCmds == null)
//			strTableCmds = "";
//
//		if(strTableCmds.endsWith(",")) {
//			strTableCmds = strTableCmds.substring(0, strTableCmds.length() - 1);
//		}
//		for (int i : ids) {
//			strTableCmds += ("," + i);
//		}
//		CustOpts.custOps.setKeyAndValue(key, strTableCmds);
//
//		//update ui
//		initPanel(key, panel, idx);
//	}
//
	private void initPanel(String key, PicturePane panel, int idx) {

//		panel.removeAll();

		String TableCmds = (String)CustOpts.custOps.getValue(key);
		if(TableCmds == null) {
			TableCmds = defaultTableCmds[idx];
			CustOpts.custOps.setKeyAndValue(key, TableCmds);
		}
		btnListMap.put(key, new ArrayList<ISButton>());

		String[] sIDs = TableCmds.split(",");
		for (String id : sIDs) {
			if(id.length() > 0) {//the first element could be an ""
				try {
					idx = Integer.valueOf(id);
					ISButton comp = buttons[idx].clone();
//					((AbstractButton)comp).removeActionListener(((AbstractButton)comp).getActionListeners()[0]);
//					comp.addMouseListener(this);
//					comp.addMouseMotionListener(this);
					((ArrayList) btnListMap.get(key)).add(comp);
				}catch(Exception exp) {
					L.e("CmdBtnDlg", "exception when cloning a button and add into btnListMap.", exp);
					continue;
				}
			}
		}
		BarUtil.addFunctionButtons(panel, (ArrayList<ISButton>)btnListMap.get(key));
		BarUtil.layoutCommandButtons(panel, (ArrayList<ISButton>)btnListMap.get(key));
//		relayout();
	}
//
//	@Override
//	public void componentResized(ComponentEvent e) {
//		relayout();
//	}
//
//	@Override
//	public void componentMoved(ComponentEvent e) {}
//
//	@Override
//	public void componentShown(ComponentEvent e) {}
//
//	@Override
//	public void componentHidden(ComponentEvent e) {}
//
	private void relayout() {
//		int heigth = getHeight();
//		int width = getWidth();
//		int panelHeight = (heigth - 120) / 4;
//
//		//list
//		originalScrollBar.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP, 169, heigth - CustOpts.SIZE_EDGE * 2 - 60);
//
//		//buttons
//		toTable.setBounds(originalScrollBar.getX() + originalScrollBar.getWidth() + CustOpts.HOR_GAP,
//				CustOpts.VER_GAP + panelHeight/2,  40, 20);
//		fromTable.setBounds(toTable.getX(), toTable.getY() + CustOpts.VER_GAP + 20,  40, 20);
//
//		toBill.setBounds(toTable.getX(), toTable.getY() + panelHeight + CustOpts.VER_GAP,  40, 20);
//		fromBill.setBounds(toTable.getX(), toBill.getY() + CustOpts.VER_GAP + 20,  40, 20);
//
//		toSale.setBounds(toBill.getX(), toBill.getY() + panelHeight + CustOpts.VER_GAP,  40, 20);
//		fromSale.setBounds(toBill.getX(), toSale.getY() + CustOpts.VER_GAP + 20,  40, 20);
//
//		toSetting.setBounds(toSale.getX(), toSale.getY() + panelHeight + CustOpts.VER_GAP,  40, 20);
//		fromSetting.setBounds(toSale.getX(), toSetting.getY() + CustOpts.VER_GAP + 20,  40, 20);
//
//		//panel
//		tablePanle.setBounds(toTable.getX() + toTable.getWidth() + CustOpts.HOR_GAP,
//				CustOpts.VER_GAP,
//				width - originalScrollBar.getWidth() - toTable.getWidth() - CustOpts.HOR_GAP * 3 - CustOpts.SIZE_EDGE * 2 - 20,
//				panelHeight);
//		billPanle.setBounds(tablePanle.getX(), tablePanle.getY() + tablePanle.getHeight() + CustOpts.VER_GAP,
//				tablePanle.getWidth(), tablePanle.getHeight());
//		salePanle.setBounds(billPanle.getX(), billPanle.getY() + billPanle.getHeight() + CustOpts.VER_GAP,
//				billPanle.getWidth(), billPanle.getHeight());
//		settingPanle.setBounds(salePanle.getX(), salePanle.getY() + salePanle.getHeight() + CustOpts.VER_GAP,
//				salePanle.getWidth(), salePanle.getHeight());
//
//		reset.setBounds(settingPanle.getX(), settingPanle.getY() + settingPanle.getHeight() + CustOpts.VER_GAP, 80, 40);
//		close.setBounds(settingPanle.getX() + settingPanle.getWidth() - 80, reset.getY(), 80, 40);

		BarUtil.layoutCommandButtons(tablePanle, (ArrayList<ISButton>)btnListMap.get(KEY_TABLE_CMD));
		BarUtil.layoutCommandButtons(billPanle, (ArrayList<ISButton>)btnListMap.get(KEY_BILL_CMD));
		BarUtil.layoutCommandButtons(salePanle, (ArrayList<ISButton>)btnListMap.get(KEY_SALE_CMD));
		BarUtil.layoutCommandButtons(settingPanle, (ArrayList<ISButton>)btnListMap.get(KEY_SETTING_CMD));

	}

	//buttons=========================================================================================================
    public static void initButtons() {
    	//TablePanel
		btnAddTable = new FunctionButton(BarFrame.consts.AddTable());
		btnOrderManage = new FunctionButton(BarFrame.consts.BILLINGHISTORY());
		//btnWaiterReport = new FunctionButton(BarFrame.consts.WaiterReport());
		btnSetting = new FunctionButton(BarFrame.consts.SETTINGS());
		btnReport = new FunctionButton(BarFrame.consts.Report());
		btnMenu = new FunctionButton(BarFrame.consts.MENU());
		btnCheckInOut = new FunctionButton(BarFrame.consts.CheckOut());

		//BillListPanel
//		btnAddUser = new FunctionButton(BarFrame.consts.AddBill());
		btnPrintAll = new FunctionButton(BarFrame.consts.PrintAll());
		btnPrintOneBill = new FunctionButton(BarFrame.consts.PrintOneBill());
		btnPrintOneInVoice = new FunctionButton(BarFrame.consts.PrintOneInvoice());

		btnEqualBill = new FunctionToggleButton(BarFrame.consts.EqualBill());
		btnSplitItem = new FunctionToggleButton(BarFrame.consts.SplitItem());
		btnMoveItem = new FunctionToggleButton(BarFrame.consts.MoveItem());
		btnCombineAll = new FunctionButton(BarFrame.consts.CombineAll());

		btnSuspendAll = new FunctionButton(BarFrame.consts.SuspendAll());

    	//SalesPanel
    	btnCASH = new FunctionButton(BarFrame.consts.CASH());
    	btnDEBIT = new FunctionButton(BarFrame.consts.DEBIT());
    	btnVISA = new FunctionButton(BarOption.isCredit() ? BarFrame.consts.CREDIT() : BarFrame.consts.VISA());
        btnSplitBill = new FunctionButton(BarFrame.consts.SPLIT_BILL());
        btnRemoveItem = new FunctionButton(BarFrame.consts.REMOVEITEM());
        btnModify = new FunctionButton(BarFrame.consts.MODIFY());
        btnDiscItem = new FunctionToggleButton(BarFrame.consts.DISC_ITEM());
        btnQTY = new FunctionToggleButton(BarFrame.consts.QTY());
        btnChangePrice = new FunctionToggleButton(BarFrame.consts.ChangePrice());
        btnServiceFee = new FunctionButton(BarFrame.consts.SERVICEFEE());
        btnPrintBill = new FunctionButton(BarFrame.consts.TEMPORARY_BILL());

        btnReturn = new FunctionButton(BarFrame.consts.RETURN());
        btnAddBill = new FunctionButton(BarFrame.consts.AddBill());
        btnMASTER = new FunctionButton(BarFrame.consts.MASTER());
        btnOtherPay = new FunctionButton(BarFrame.consts.OtherPay());
        btnCancelAll = new FunctionButton(BarFrame.consts.CANCEL_ALL());
        btnVoidOrder = new FunctionButton(BarFrame.consts.VOID_ORDER());
        btnOpenDrawer = new FunctionButton(BarFrame.consts.OpenDrawer());
        btnDiscBill = new FunctionButton(BarFrame.consts.VolumnDiscount());
        btnRefund = new FunctionButton(BarFrame.consts.Refund());
        btnSend = new FunctionButton(BarFrame.consts.SEND());

        //btnLine_3_2 = new JToggleButton(BarFrame.consts.QTY());
		btnGIFTCARD = new FunctionButton(BarFrame.consts.GIFTCARD());
        btnDiscountCoupon = new FunctionButton(BarFrame.consts.COUPON());
		btnSuspend = new FunctionButton(BarFrame.consts.SUSPEND());
		btnCheckOrder = new FunctionButton(BarFrame.consts.BILLINGHISTORY());
		btnEN = new FunctionButton("EN");
		btnFR = new FunctionButton("FR");
		btnCN = new FunctionButton("CN");
		btnCheckOut = new FunctionButton(BarFrame.consts.CheckOut());

        //SettingPanel
        btnEmployee = new FunctionButton(BarFrame.consts.Operator());
        //btnLine_2_3 = new FunctionButton("TBD");
        btnPrinter = new FunctionButton(BarFrame.consts.PRINTER());
        btnTable = new FunctionButton(BarFrame.consts.TABLE());
        btnBillFoot = new FunctionButton(BarFrame.consts.BillInfo());
        btnModifySetting = new FunctionButton(BarFrame.consts.MODIFY());
        btnGiftCardSetting = new FunctionButton(BarFrame.consts.GIFTCARD());
        btnCoupon = new FunctionButton(BarFrame.consts.COUPON());
        btnRule = new FunctionButton(BarFrame.consts.COMBO());
        btnColor = new FunctionButton(BarFrame.consts.Color().toUpperCase());
        btnCheckInOutList = new FunctionButton(BarFrame.consts.WORK_LOG());
        btnVoidReason = new FunctionButton(BarFrame.consts.VoidReason());
        btnChangeTable = new FunctionToggleButton(BarFrame.consts.ChangeTable());

        buttons = new ISButton[] {
        		btnAddTable,	//0
        		btnOrderManage,
        		btnSetting,
        		btnReport,
        		btnMenu,
        		btnCheckInOut,		//5

//        		btnAddUser,
        		btnPrintAll,
//        		btnPrintOneBill,
//        		btnPrintOneInVoice,
        		btnEqualBill,
        		btnSplitItem,
        		btnMoveItem,
        		btnCombineAll,	//10
        		btnSuspendAll,

        		btnCASH,
        		btnDEBIT,
        		btnVISA,
        		btnMASTER,		//15
        		btnOtherPay,
        		btnGIFTCARD,
        		btnSplitBill,
        		btnRemoveItem,
        		btnModify,	//20
        		btnDiscItem,
        		btnChangePrice,
        		btnDiscBill,
        		btnServiceFee,
        		btnPrintBill,		//25

        		btnReturn,
        		btnAddBill,

        		btnCancelAll,
        		btnVoidOrder,
        		btnOpenDrawer,		//30
        		btnDiscountCoupon,
        		btnRefund,
        		btnSend,
        		btnEN,
        		btnFR,		//35
        		btnCN,

        		btnEmployee,
        		btnPrinter,
        		btnTable,
        		btnBillFoot,		//40
        		btnModifySetting,
        		btnGiftCardSetting,
        		btnCoupon,
        		btnRule,
        		btnColor,	//45
        		btnCheckInOutList,
        		btnQTY,
        		btnVoidReason,
        		btnChangeTable
        		};


        //listener------------------------------
        //tablePanel
        btnAddTable.addActionListener(Cmd_AddTable.getInstance());
        btnOrderManage.addActionListener(Cmd_CheckOrder.getInstance());
        //btnWaiterReport.addActionListener(this);
        btnSetting.addActionListener(Cmd_Setting.getInstance());
        btnReport.addActionListener(Cmd_Report.getInstance());
        btnMenu.addActionListener(Cmd_Menu.getInstance());
        btnCheckInOut.addActionListener(Cmd_CheckInOut.getInstance());
        //billList
//        btnAddUser.addActionListener(Cmd_AddUser.getInstance());
		btnPrintAll.addActionListener(Cmd_PrintAll.getInstance());
		btnPrintOneBill.addActionListener(Cmd_PrintOneBill.getInstance());
		btnPrintOneInVoice.addActionListener(Cmd_PrintOneInVoice.getInstance());
		btnEqualBill.addActionListener(Cmd_EqualBill.getInstance());
		btnCombineAll.addActionListener(Cmd_CombineAll.getInstance());
		btnSplitItem.addActionListener(Cmd_SplitItem.getInstance());
		btnMoveItem.addActionListener(Cmd_MoveItem.getInstance());
		btnSuspendAll.addActionListener(Cmd_SuspendAll.getInstance());
        //salesPanel
        btnCASH.addActionListener(Cmd_Pay.getInstance());
        btnDEBIT.addActionListener(Cmd_Pay.getInstance());
        btnVISA.addActionListener(Cmd_Pay.getInstance());
        btnSplitBill.addActionListener(Cmd_SplitBill.getInstance());
        btnRemoveItem.addActionListener(Cmd_RemoveItem.getInstance());
        btnModify.addActionListener(Cmd_Modify.getInstance());
        btnDiscItem.addActionListener(Cmd_DiscItem.getInstance());
        btnChangePrice.addActionListener(Cmd_ChangePrice.getInstance());
        btnServiceFee.addActionListener(Cmd_ServiceFee.getInstance());
        btnPrintBill.addActionListener(Cmd_PrintBill.getInstance());

        btnReturn.addActionListener(Cmd_Return.getInstance());
        btnAddBill.addActionListener(Cmd_AddBill.getInstance());
        btnMASTER.addActionListener(Cmd_Pay.getInstance());
        btnOtherPay.addActionListener(Cmd_OtherPay.getInstance());
        btnCancelAll.addActionListener(Cmd_CancelAll.getInstance());
        btnVoidOrder.addActionListener(Cmd_VoidOrder.getInstance());
        btnOpenDrawer.addActionListener(Cmd_OpenDrawer.getInstance());
        btnDiscBill.addActionListener(Cmd_DiscBill.getInstance());
        btnRefund.addActionListener(Cmd_Refund.getInstance());
        btnSend.addActionListener(Cmd_Send.getInstance());

		btnGIFTCARD.addActionListener(Cmd_GiftCard.getInstance());
		btnDiscountCoupon.addActionListener(Cmd_DiscountCoupon.getInstance());
		btnEN.addActionListener(Cmd_Lang.getInstance());
		btnFR.addActionListener(Cmd_Lang.getInstance());
		btnCN.addActionListener(Cmd_Lang.getInstance());
		btnSuspend.addActionListener(Cmd_Suspend.getInstance());
		btnCheckOrder.addActionListener(Cmd_CheckOrder.getInstance());
		btnCheckOut.addActionListener(Cmd_CheckInOut.getInstance());
        //
        btnEmployee.addActionListener(Cmd_Employee.getInstance());
        btnPrinter.addActionListener(Cmd_Printer.getInstance());
        btnTable.addActionListener(Cmd_Table.getInstance());
        btnBillFoot.addActionListener(Cmd_BillFoot.getInstance());
        btnModifySetting.addActionListener(Cmd_ModifySetting.getInstance());
        btnGiftCardSetting.addActionListener(Cmd_GiftCardSetting.getInstance());
        btnCoupon.addActionListener(Cmd_SettingCoupon.getInstance());
        btnRule.addActionListener(Cmd_SettingRule.getInstance());
        btnColor.addActionListener(Cmd_Color.getInstance());
        btnCheckInOutList.addActionListener(Cmd_CheckInOutList.getInstance());

        btnQTY.addActionListener(Cmd_QTY.getInstance());
        btnVoidReason.addActionListener(Cmd_VoidReason.getInstance());
        btnChangeTable.addActionListener(Cmd_ChangeTable.getInstance());
        groupButtons();
	}
    
    @SuppressLint("SuspiciousIndentation")
	private static void groupButtons() {
    	//get current customization
		Object strTableCmds = CustOpts.custOps.getValue(KEY_TABLE_CMD);
		Object strBillCmd = CustOpts.custOps.getValue(KEY_BILL_CMD);
		Object strSaleCmd = CustOpts.custOps.getValue(KEY_SALE_CMD);
		Object strSettingCmd = CustOpts.custOps.getValue(KEY_SETTING_CMD);
		
		groupedButtons = new ArrayList[]{new ArrayList<ISButton>(), new ArrayList<ISButton>(), new ArrayList<ISButton>(),new ArrayList<ISButton>()};
		//if not set, then use default.
		if(strTableCmds != null) {
			fillRegroupBtnWithCustimization(strTableCmds, groupedButtons[0]);
		}else {
			defaultTableCmds[0] = "0,1,30,2,3,4,5";
			groupedButtons[0].add(btnAddTable);
	    	groupedButtons[0].add(btnOrderManage);
	    	groupedButtons[0].add(btnOpenDrawer);
	    	//groupedButtons[0].add(btnWaiterReport);
	    	groupedButtons[0].add(btnChangeTable);
	    	groupedButtons[0].add(btnSetting);
	    	groupedButtons[0].add(btnReport);
	    	groupedButtons[0].add(btnMenu);
	    	groupedButtons[0].add(btnCheckInOut);
		}
		
		if(strBillCmd != null) {
			fillRegroupBtnWithCustimization(strBillCmd, groupedButtons[1]);
		}else {
			defaultTableCmds[1] = "27,7,8,11,9,10,12";
	    	//BillListPanel
	    	groupedButtons[1].add(btnReturn);
//	    	groupedButtons[1].add(btnAddUser);
	    	groupedButtons[1].add(btnPrintAll);
//	    	groupedButtons[1].add(btnPrintOneBill);
//	    	groupedButtons[1].add(btnPrintOneInVoice);
	    	groupedButtons[1].add(btnEqualBill);
	    	groupedButtons[1].add(btnCombineAll);
	    	groupedButtons[1].add(btnSplitItem);
	    	groupedButtons[1].add(btnMoveItem);
	    	groupedButtons[1].add(btnSuspendAll);
		}
		
		if(strSaleCmd != null) {
			fillRegroupBtnWithCustimization(strSaleCmd, groupedButtons[2]);
		} else {
			defaultTableCmds[2] = "12,13,14,18,19,20,21,22,24,25,26,27,15,28,29,30,23,32,33,17,16,31,11,1,2,3,4";
	    	//SalesPanel
	    	groupedButtons[2].add(btnCASH);
	    	groupedButtons[2].add(btnDEBIT);
	    	groupedButtons[2].add(btnVISA);
	    	groupedButtons[2].add(btnSplitBill);
	    	groupedButtons[2].add(btnRemoveItem);
	    	groupedButtons[2].add(btnModify);
	    	groupedButtons[2].add(btnDiscItem);
	    	groupedButtons[2].add(btnChangePrice);
	    	groupedButtons[2].add(btnServiceFee);
	    	groupedButtons[2].add(btnPrintBill);

	    	groupedButtons[2].add(btnReturn.clone());
	    	groupedButtons[2].add(btnOtherPay);
	    	groupedButtons[2].add(btnMASTER);
	    	groupedButtons[2].add(btnAddBill);
	    	groupedButtons[2].add(btnVoidOrder);
	    	groupedButtons[2].add(btnOpenDrawer.clone());
	    	groupedButtons[2].add(btnDiscBill);
	    	groupedButtons[2].add(btnRefund);
	    	groupedButtons[2].add(btnSend);

	    	groupedButtons[2].add(btnGIFTCARD);
	    	groupedButtons[2].add(btnCancelAll);
	    	groupedButtons[2].add(btnDiscountCoupon);
	    	groupedButtons[2].add(btnSuspend);
	    	groupedButtons[2].add(btnCheckOrder);
	    	groupedButtons[2].add(btnSetting.clone());
	    	groupedButtons[2].add(btnReport.clone());
	    	groupedButtons[2].add(btnCheckOut);
		}
		
		if(strSettingCmd != null) {
			fillRegroupBtnWithCustimization(strSettingCmd, groupedButtons[3]);
		}else {
			defaultTableCmds[3] = "26,37,38,39,40,41,42,43,44,45,46";
	    	//setting panels
	        groupedButtons[3].add(btnReturn.clone());
	        groupedButtons[3].add(btnEmployee);
	        groupedButtons[3].add(btnPrinter);
	        groupedButtons[3].add(btnTable);
	        groupedButtons[3].add(btnBillFoot);
	        groupedButtons[3].add(btnModifySetting);
	        groupedButtons[3].add(btnGiftCardSetting);
	        groupedButtons[3].add(btnCoupon);
	        groupedButtons[3].add(btnRule);
	        groupedButtons[3].add(btnColor);
	        groupedButtons[3].add(btnCheckInOutList);
	        groupedButtons[3].add(btnVoidReason);
		}
	}

	private static void fillRegroupBtnWithCustimization(Object customization, ArrayList<ISButton> groupedButtons) {
		
		String[] btns = ((String)customization).split(",");
		for (String string : btns) {
			try {
				int i = Integer.valueOf(string);
				groupedButtons.add(buttons[i].clone());
			}catch(Exception e) {
				continue;
			}
		}
	}	

//	JList<String> originalList;
//	JScrollPane originalScrollBar;
	
	ArrowButton toTable;
	ArrowButton toBill;
	ArrowButton toSale;
	ArrowButton toSetting;
	ArrowButton fromTable;
	ArrowButton fromBill;
	ArrowButton fromSale;
	ArrowButton fromSetting;
	
	PicturePane tablePanle;
	PicturePane billPanle;
	PicturePane salePanle;
	PicturePane settingPanle;
	PicturePane[] panels;
	
    private static FunctionButton reset;
    private static FunctionButton close;
    
	//TablePanel
    //private JToggleButton btnChangeMode;
	private static FunctionButton btnAddTable;
	private static FunctionButton btnOrderManage;
	//private static FunctionButton btnWaiterReport;
	private static FunctionButton btnSetting;
	private static FunctionButton btnReport;
	private static FunctionButton btnMenu;
	private static FunctionButton btnCheckInOut;
	
	//BillListPanel
//	private static FunctionButton btnAddUser;
	private static FunctionButton btnPrintAll;
	private static FunctionButton btnPrintOneBill;
	private static FunctionButton btnPrintOneInVoice;
	private static FunctionToggleButton btnEqualBill;
	private static FunctionToggleButton btnSplitItem;
	private static FunctionToggleButton btnMoveItem;
	private static FunctionButton btnCombineAll;
	private static FunctionButton btnSuspendAll;
	
	//SalesPanel
	private static FunctionButton btnCASH;
	private static FunctionButton btnDEBIT;
	private static FunctionButton btnVISA;
	private static FunctionButton btnMASTER;
	private static FunctionButton btnOtherPay;
	private static FunctionButton btnGIFTCARD;
	
	private static FunctionButton btnSplitBill;
	private static FunctionButton btnRemoveItem;
	private static FunctionButton btnModify;
	private static FunctionToggleButton btnDiscItem;
	private static FunctionToggleButton btnChangePrice;
	private static FunctionButton btnServiceFee;
	private static FunctionButton btnPrintBill;

	private static FunctionButton btnReturn;
	private static FunctionButton btnAddBill;
	   
	private static FunctionButton btnCancelAll;
	private static FunctionButton btnVoidOrder;
	private static FunctionButton btnOpenDrawer;
	private static FunctionButton btnDiscBill;
	private static FunctionButton btnRefund;
	private static FunctionButton btnSend;
	
	private static FunctionButton btnDiscountCoupon;
	private static FunctionButton btnEN;
	private static FunctionButton btnFR;
	private static FunctionButton btnCN;
	private static FunctionButton btnCheckOut;
	private static FunctionButton btnSuspend;
	private static FunctionButton btnCheckOrder;
	
	//setting panels
    private static FunctionButton btnEmployee;
    private static FunctionButton btnPrinter;
    private static FunctionButton btnTable;
    private static FunctionButton btnBillFoot;
    private static FunctionButton btnModifySetting;
    private static FunctionButton btnGiftCardSetting;
    private static FunctionButton btnCoupon;
    private static FunctionButton btnRule;
    private static FunctionButton btnColor;
    private static FunctionButton btnCheckInOutList;

	private static FunctionToggleButton btnQTY;
	private static FunctionButton btnVoidReason;
	private static FunctionToggleButton btnChangeTable;
	
    static ISButton[] buttons;
    
    //-------------------adding and removing a mark------------------
    private int startX = 0;
    private int startY = 0;
    ISButton btn = null;
//	@Override
//	public void mouseDragged(MouseEvent e) {}
//
//	@Override
//	public void mouseMoved(MouseEvent e) {}
//
//	@Override
//	public void mouseClicked(MouseEvent e) {
//		Object o =e.getSource();
//		JFileChooser requestFileChooser = new JFileChooser();
//	    requestFileChooser.setFileFilter(new FileFilterRequest());
//	    requestFileChooser.setMultiSelectionEnabled(false);
//
//	    String filePath = "";
//	    final int res = requestFileChooser.showOpenDialog(this);
//        if (res == JFileChooser.APPROVE_OPTION) {
//        	File file = requestFileChooser.getSelectedFile();
//        	filePath = file != null ? file.getAbsolutePath() : null;
//        }else {
//        	return;
//        }
//		if(o == tablePanle) {
//			BarOption.setTablePanelBK(filePath);
//		}else if(o == billPanle) {
//			BarOption.setBillPanelBK(filePath);
//		}else if(o == salePanle) {
//			BarOption.setSalePanelBK(filePath);
//		}else if(o == settingPanle) {
//			BarOption.setSettingPanelBK(filePath);
//		}
//	}
//
//	@Override
//	public void mousePressed(MouseEvent e) {
//		Object o = e.getSource();
//		if(o instanceof ISButton) {
//			btn = (ISButton)o;
//	    	startX = ((JComponent)btn).getX() + e.getX();
//	    	startY = ((JComponent)btn).getY() + e.getY();
//		}
//	}
//
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		Object o = e.getSource();
//		if(o instanceof ISButton) {	//if it's checkItem, then ignore the event.
//	    	int x = e.getX() + ((JComponent)o).getX();
//	    	int y = e.getY() + ((JComponent)o).getY();
//	    	for(int i = 0; i < 4; i++) {
//    			ArrayList<JComponent> btns =(ArrayList<JComponent>)btnListMap.get(LAYOUT_KEYS[i]);
//	    		//now find out which id's on startX, startY. and check if it's text is same.
//    			boolean findAlready = false;
//	    		for(int index = 0; index < btns.size(); index++) {
//	    			JComponent comp = btns.get(index);
//	    			if(comp.getX() < startX && comp.getX() + comp.getWidth() > startX
//	    					&& comp.getY() < startY && comp.getY() + comp.getHeight() > startY
//	    					&& ((ISButton)comp).getText().equals(btn.getText())) {
//
//	    				if(index >= 20) {	//@NOTE, because sometimes an More button is added, so should give a adjust.
//	    					index--;
//	    				}
//
//	    				if(x - startX > 20) {
//	    					adjustLayoutString(i, index, 0);
//	    		    	}else if(x - startX < -20) {
//	    		    		adjustLayoutString(i, index, 1);
////	    		    	}else if(y - startY > 20) {
////	    		    		adjustLayoutString(i, index, 2);
////	    		    	}else if(y - startY < -20) {
////	    		    		adjustLayoutString(i, index, 3);
//	    		    	}
//
//	    				findAlready = true;
//	    				break;
//	    			}
//	    		}
//	    		if(findAlready) {
//	    			break;
//	    		}
//    		}
//		}
//	}

	public void adjustLayoutString(int i, int index, int direction) {
		//second, since we have got the string and the index. let's modifiy the string.
		String TableCmds =  (String)CustOpts.custOps.getValue(LAYOUT_KEYS[i]);
		String[] sIDs = TableCmds.split(",");
		switch (direction) {
		case 0:
			if(index < sIDs.length - 1) {	//if not the last one, then move.
				
				String temp = sIDs[index];
				sIDs[index] = sIDs[index + 1];
				sIDs[index + 1] = temp;
			}else {		//@NOTE:be careful, because there might be a Morebutton, which will make the index bigger.
				sIDs[sIDs.length - 1] = "";	//while to remove, it's always safe to remove the last one in this case(right direction drag).
			}
			break;
		case 1:
			if(index > 0) {	//if not the last one, then move.
				String temp = sIDs[index];
				sIDs[index] = sIDs[index - 1];
				sIDs[index - 1] = temp;
			}else {		//remove
				sIDs[index] = "";
			}
			break;
//		case 2:
//			if(index < sIDs.length - 1) {	//if not the last one, then move.
//				String temp = sIDs[index];
//				sIDs[index] = sIDs[index + 1];
//				sIDs[index + 1] = temp;
//			}else {		//remove
//				sIDs[index] = "";
//			}
//			break;
//		case 3:
//			if(index < sIDs.length - 1) {	//if not the last one, then move.
//				String temp = sIDs[index];
//				sIDs[index] = sIDs[index + 1];
//				sIDs[index + 1] = temp;
//			}else {		//remove
//				sIDs[index] = "";
//			}
//			break;

		default:
			break;
		}
		
		StringBuilder newIDs = new StringBuilder();
		for (String sID : sIDs) {
			if(sID != null && sID.length() > 0) {
				newIDs.append(",").append(sID);
			}
		}
		CustOpts.custOps.setKeyAndValue(LAYOUT_KEYS[i], newIDs.length() > 0 ? newIDs.substring(1) : "");
		
		//update ui
		initPanel(LAYOUT_KEYS[i], panels[i], i);
	}

//	@Override
//	public void mouseEntered(MouseEvent e) {}
//
//	@Override
//	public void mouseExited(MouseEvent e) {}
	
//    /**
//     * This file filter will accept folders, jpeg, jpg and png files.
//     */
//    public static class FileFilterRequest extends FileFilter {
//        /**
//         * @see javax.swing.filechooser.FileFilter#accept(File)
//         */
//        @Override
//        public boolean accept(final File file) {
//            return (file.isDirectory() || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png"));
//        }
//
//        /**
//         * @see javax.swing.filechooser.FileFilter#getDescription()
//         */
//        @Override
//        public String getDescription() {
//            return "Requests";
//        }
//    }

    //from outside to inside, the status is divided into : completed/refunded/billprinted/saved/original
	public static void refreshBtnStatusForSalesPanel(int status) {
		for (Object btn : CmdBtnsManagerDlg.groupedButtons[2]) {
			String text = ((ISButton)btn).getText();
			if(status < DBConsts.ORIGINAL) {	//------------------------------refunded-----------
	    		if(BarFrame.consts.MORE().equals(text)
	    				|| BarFrame.consts.BILLINGHISTORY().equals(text)
	    				|| BarFrame.consts.SETTINGS().equals(text)
	    				|| BarFrame.consts.Report().equals(text)
	    				|| BarFrame.consts.WORK_LOG().equals(text) 
    					|| BarFrame.consts.RETURN().equals(text)) {
    				((ISButton)btn).setEnabled(true);
	    		}else {
	    			((ISButton)btn).setEnabled(false);
	    		}
	    	}else if(status >= DBConsts.VOIDED) {
	    		if(BarFrame.consts.MORE().equals(text)
	    				|| BarFrame.consts.BILLINGHISTORY().equals(text)
	    				|| BarFrame.consts.SETTINGS().equals(text)
	    				|| BarFrame.consts.Report().equals(text)
	    				|| BarFrame.consts.WORK_LOG().equals(text) 
    					|| BarFrame.consts.RETURN().equals(text)) {
    				((ISButton)btn).setEnabled(true);
	    		}else {
	    			((ISButton)btn).setEnabled(false);
	    		}
	    	}else if(status >= DBConsts.COMPLETED) {//---------------------completed-------------------
    			if(BarFrame.consts.MORE().equals(text)
    					|| BarFrame.consts.BILLINGHISTORY().equals(text)
    					|| BarFrame.consts.SETTINGS().equals(text)
    					|| BarFrame.consts.Report().equals(text)
    					|| BarFrame.consts.WORK_LOG().equals(text) 
    					|| BarFrame.consts.RETURN().equals(text)
    					|| BarFrame.consts.Refund().equals(text)
    					|| BarFrame.consts.CASH().equals(text)
    					|| BarFrame.consts.DEBIT().equals(text)
    					|| BarFrame.consts.MASTER().equals(text)
    					|| BarFrame.consts.VISA().equals(text)
    					|| BarFrame.consts.OtherPay().equals(text)
    					) {
    				((ISButton)btn).setEnabled(true);
    			}else {
    				((ISButton)btn).setEnabled(false);
    			}
    			
    			if(status >= DBConsts.VOIDED) {
    				if(BarFrame.consts.Refund().equals(text)) {
    					((ISButton)btn).setEnabled(false);
    				}
    			}
	    	}else if(status >= DBConsts.BILL_PRINTED){	//-----------------bill printed-----------------
	    		if(BarFrame.consts.Refund().equals(text)) {
    				((ISButton)btn).setEnabled(false);
	    		}else {
	    			((ISButton)btn).setEnabled(true);
	    		}
	    	} else{							//-------------------------------other status(original/order saved)--------------
	    		if(BarFrame.consts.VOID_ORDER().equals(text) 
	    				|| BarFrame.consts.Refund().equals(text)) {
    				((ISButton)btn).setEnabled(false);
	    		}else {
	    			((ISButton)btn).setEnabled(true);
	    		}
	    	}
		}
	}

	public static void disableNonPlatformPaymentBtns() {
		for (ISButton btn : CmdBtnsManagerDlg.groupedButtons[2]) {
			String text = btn.getText();
			if(BarFrame.consts.CASH().equals(text) || BarFrame.consts.DEBIT().equals(text) || BarFrame.consts.MASTER().equals(text) || BarFrame.consts.VISA().equals(text)) {
				btn.setEnabled(false);
			}
		}
	}
}
