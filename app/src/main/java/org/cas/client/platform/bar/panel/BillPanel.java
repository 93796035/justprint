package org.cas.client.platform.bar.panel;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.CustomizedRule;
import com.just.print.db.bean.Dish;
import com.just.print.ui.adapter.MyAdapter;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.BillListPanel;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.action.Cmd_DiscItem;
import org.cas.client.platform.bar.action.Cmd_SplitItem;
import org.cas.client.platform.bar.action.SamActionListener;
import org.cas.client.platform.bar.dialog.CmdBtnsManagerDlg;
import org.cas.client.platform.bar.dialog.RefundItemDlg;
import org.cas.client.platform.bar.i18n.BarDlgConst;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.DataSyncService;
import org.cas.client.platform.bar.net.NetUtil;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.print.PrintUtil;
import org.cas.client.platform.bar.uibeans.FunctionToggleButton;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.web_srm.ValuePossible;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BillPanel extends BaseFragment
		implements View.OnClickListener, View.OnTouchListener, SamActionListener //, ComponentListener, PIMTableRenderAgent, ListSelectionListener
{

	public static final String DISCBILL = "#DISCBILL";
	public static final String SERVICEFEE = "#SERVICEFEE";
	SalesPanel salesPanel;
	private CustomerFrame customerFrame;
	BillListPanel billListPanel;
	public FunctionToggleButton billButton;

	private int stepCounter;
	private int minStepCounter = CustOpts.custOps.getValue("minMoveStep") == null ? 5 : Integer.valueOf((String)CustOpts.custOps.getValue("minMoveStep"));

    private boolean isDragging;
    private ArrayList<Dish> orderedDishAry = new ArrayList<Dish>();

    //Bill property (not for specific item).the info should be retrieved from bill record if have.
    private int billID;
    private String createdTime;
    public float totalGst;
	public float totalQst;
	public float subTotal;
	public int tip;
	public int serviceFee;
	public int totalOnBill;	//is currently only used to monitor if there's a case which will cause the wrong total was wrote into the bill record.
	public float discount;

	private Map<Integer, Dish> mapForPrintedItemRemoving = new HashMap<Integer, Dish>();

    int received;
    int cashback;
    private String comment = "";

	private int status = DBConsts.ORIGINAL; //TODO:should be removed in future, because the data can be fetched from DB. keep a copy here is risky.
	public String customerIndex;

	public BillPanel(SalesPanel salesPanel) {
		this.salesPanel = salesPanel;
	}

	public BillPanel(BillListPanel billListPanel, FunctionToggleButton billButton) {
		this.billListPanel = billListPanel;
		this.billButton = billButton;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_bill_panel;
	}

	@Override
	public void onCreated(Bundle savedInstanceState) {
		new StupidReflect(this, getView()).init();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		new StupidReflect(this, getView()).init();
		initComponent();  // 初始化组件逻辑
		initContent();
	}

	//find the new (not saved) items, then send then to kitchen, save then to db. send to server...
	public boolean unsavedItemsCheck() {
		//if there's any new bill, send it to kitchen first, and this also made the SALE_RECORD generated.
		List<Dish> dishes = getNewDishes();
		if(dishes.size() == 0) {	//no new item added, then nothing need to send to kitchen or sever or db, just return false to tell calling to print internal bill.
			return false;
		}
		//if all record are new, means it's adding a new bill.otherwise, it's adding SALE_RECORD to exixting bill.
//		if(dishes.size() == orderedDishAry.size()) {	//didn't set the idx when bill created, because don't wanto display idx if there's only 1 bill.
//		    BarFrame.instance.valCurBillIdx.setText(String.valueOf(BillListPanel.getANewBillIdx()));
//		}

		sendDishesToKitchen(dishes, false);
		sendDishesToDB(dishes);
		sendDishesToServer(dishes);

//		table.repaint();//to update the color of dishes, it's saved, so it's not red anymore.
		return true;
	}

	//if there's new dish added.... update the total value field of bill record.
	//and make sure new added dish will be updated with new information.
	public void billPricesUpdateToDB() {
		DBUtil.updatePriceAndCreateTimeInBillRecord(this, 0, null);		//in case if added service fee or discout of bill. ??? so this means, when added discount, will not save to db immediatly?
		initContent();	//always need to initContent, to make sure dish in selection ary has new property. e.g. saved dish should has different color.,
	}

	//if the tableName is null, then use the sameID of original bill.
	public int cloneCurrentBillRecord(String tableName, String billIndex, int total) {
		if(total < 0) {
			total = Math.round(Float.valueOf(valTotlePrice.getText().toString()) * 100);
		}

		//get other field out from db:
		StringBuilder sql = new StringBuilder("select * from bill where _id = " + getBillID());
    	try {
    		Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            rs.moveToNext();
            String opentime = DBUtil.getCursorString(rs, "openTime");
        	int cashReceived = DBUtil.getCursorInt(rs, "cashReceived");
        	int debitReceived = DBUtil.getCursorInt(rs, "debitReceived");
            int visaReceived = DBUtil.getCursorInt(rs, "visaReceived");
            int masterReceived = DBUtil.getCursorInt(rs, "masterReceived");
            String otherreceived = DBUtil.getCursorString(rs, "otherReceived");
            if(otherreceived == null) {
            	otherreceived = "";
            }
        	int cashBack = DBUtil.getCursorInt(rs, "CASHBACK");
            int tip = DBUtil.getCursorInt(rs, "TIP");
            int status = DBUtil.getCursorInt(rs, "STATUS");

			//generate a bill in db and update the SALE_RECORD with the new bill id
			String createtime = BarUtil.df.format(new Date());
			sql = new StringBuilder(
	            "INSERT INTO BILL(createtime, tableName, BillIndex, total, discount, tip, serviceFee, cashback, userId, Comment,")
	            .append(" opentime, cashReceived, debitReceived, visaReceived, masterReceived, otherreceived, status) VALUES ('")
				.append(createtime).append("', '")
	            .append(tableName == null ? DBUtil.getCursorString(rs, "tableName") : tableName).append("', '")					//table
	            .append(billIndex == null ? DBUtil.getCursorString(rs, "BillIndex") : billIndex).append("', ")	//bill
	            .append(total < 0 ? DBUtil.getCursorInt(rs, "total") : total).append(", ")//Math.round(Float.valueOf(valTotlePrice.getText()) * 100)/num).append(", ")	//total
	            .append(discount).append(", ")
	            .append(tip).append(", ")
	            .append(serviceFee).append(", ")			//currently used for storing service fee -_-!
	            .append(cashBack).append(", ")	//discount
	            .append(LoginDlg.USERID).append(", '")		//emoployid
	            .append(comment).append("', '")
	            .append(opentime).append("', ")
	            .append(cashReceived).append(", ")
	            .append(debitReceived).append(", ")
	            .append(visaReceived).append(", ")
	            .append(masterReceived).append(", '")
	            .append(otherreceived).append("', ")
	            .append(status).append(")");

			AikaPos.db.execSQL(sql.toString());

		   	sql = new StringBuilder("Select _id from BILL where createdTime = '").append(createtime)
		   			.append("' and billIndex = '").append(billIndex).append("'");
            rs = AikaPos.db.rawQuery(sql.toString(), null);
            rs.moveToNext();
            return DBUtil.getCursorInt(rs, "_id");
		 }catch(Exception e) {
			L.e("BillPanel", "exception when cloning current bill record:" + sql,e);
			return -1;
		 }
	}

	void sendDishToKitchen(Dish dish, boolean isCancelled) {
		List<Dish> dishes = new ArrayList<Dish>();
		dishes.add(dish);
		sendDishesToKitchen(dishes, isCancelled);
	}

	//send to printer
	public void sendDishesToKitchen(List<Dish> dishes, boolean isCancelled) {
		if(dishes == null || dishes.size() == 0) {
			return;
		}
		//prepare the printing String and do printing
		String curTable = BarFrame.instance.cmbCurTable.getSelectedItem().toString();
		String curCustomerIdx = BarFrame.instance.getCurBillIndex();
		String waiterName = BarFrame.instance.valOperator.getText().toString();
		PrintService.printOrderList(dishes, curTable, curCustomerIdx, waiterName, isCancelled);
	}

	//save to db SALE_RECORD
	//before we call this, be sure to create a new bill, which has copied all the SALE_RECORD of previous bill.
	//which is to say the new dishes should belongs to the new bill.
	//??why do we have to copy the outputs and make the copied ones pointing to new bill?----just to keep the capability of futher unexpectable possibilities.
	//like need to report/study the change history of the order.
	private void sendDishesToDB(List<Dish> dishes) {
		try {
		    for (Dish dish : dishes) {
		    	String curBillIndex = BarFrame.instance.getCurBillIndex();
		    	String currentTime = BarUtil.df.format(new Date());
		    	DBUtil.createOutput(dish, curBillIndex, currentTime);	//at this moment, the num should have not been splitted.
		        //in case some store need to stay in the interface after clicking the send button.
                StringBuilder sql = new StringBuilder("Select _id, openTime from SALE_RECORD where tableName = '")
                    .append(BarFrame.instance.getCmbCurTable().getSelectedItem().toString())
                    .append("' and billIndex = ").append(curBillIndex)
                    .append(" and productId = ").append(dish.getId())
                    .append(" and AMOUNT = ").append(dish.getNum())
                    .append(" and totalPrice = ").append(dish.getTotalPrice())
                    .append(" and DISCOUNT = ").append(dish.getDiscount())
                    .append(" and userId = ").append(LoginDlg.USERID)
                	.append(" and createdTime = '").append(currentTime).append("'");
                Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
                while (rs.moveToNext()) {
                	dish.setOutputID(DBUtil.getCursorInt(rs, "_id"));
                	dish.setOrderedTime(DBUtil.getCursorString(rs, "openTime"));
                }
		    }
		}catch(Exception exp) {
			JOptionPane.showMessageDialog(FORMATERROR);
		    L.e("BillPanel", "Exception when sendingDishes to DB", exp);
		}
	}
	String FORMATERROR = "Invalid input format found, please correct and try again.";

	private void sendDishesToServer(List<Dish> dishes) {
		if(dishes == null || dishes.size() == 0) {
			return;
		}
		String serverip = BarOption.getServerIp();
		if(serverip != null && serverip.trim().length() >= 7) {
			new DataSyncService(serverip, BarFrame.instance.getCmbCurTable().getSelectedItem().toString(), dishes).start();
		}
	}

	public List<Dish> getNewDishes() {
		List<Dish> newDishes = new ArrayList<Dish>();
		for (Dish dish : getOrderedDishAry()) {
			if(dish.getOutputID() > -1)	//if it's already saved into db, ignore.
				continue;
			else {
				newDishes.add(dish);
			}
		}
		return newDishes;
	}

	public List<Dish> getNotBilledDishes() {
		List<Dish> notBilledDishes = new ArrayList<Dish>();
		for (Dish dish : getOrderedDishAry()) {
			if(dish.getStatus() != null && dish.getStatus() >= DBConsts.BILL_PRINTED)	//if it's already billed, ignore.
				continue;
			else {
				notBilledDishes.add(dish);
			}
		}
		return notBilledDishes;
	}

    @Override
	public void onClick(View o) {
    	int selectedRow = table.getSelectedRow();
		if(selectedRow < 0){
			return;
		}
    	Dish dish = getOrderedDishAry().get(selectedRow);
	    if(o == moreButton) {
			if(dish.getOutputID() >= 0) {	//already saved
				BarFrame.setStatusMes(BarFrame.consts.SendItemCanNotModify());
				addContentToList(getOrderedDishAry().get(selectedRow));
			}else {			//not saved yet  //NOTE:getNum() couldn't be bigger than 10000, if it's saved, + button will insert a new line.
				int tQTY = getOrderedDishAry().get(selectedRow).getNum();
				dish.setTotalPrice(dish.getTotalPrice() + dish.getTotalPrice() / tQTY);
				dish.setNum(++tQTY);

				table.setValueAt(tQTY % BarOption.MaxQTY + "x", selectedRow, 0); //so the qty could be usd to save other info on high bit
				table.setValueAt(BarOption.getMoneySign()
						+ BarUtil.formatMoney(dish.getTotalPrice()/100f),
						selectedRow, 3);
		    	checkWithRules();
			}
			logOperation(1, DBConsts.ORIGINAL, dish);
	    } else if (o == lessButton) {
			if(dish.getOutputID() < 0) {	//if it's already send, then do the removeItem.
				if (adjustOnlyUI(selectedRow, dish))
					return;
			}else {
				adjustBothUIandDB();
			}

			if(dish.getRuleMark() != null) {
				checkWithRules();
			}
			updateTotalArea(false);
        }else if(o == billButton){		//when bill button on top are clicked.
        	if(status >= DBConsts.COMPLETED || status < DBConsts.ORIGINAL) {	//@Note mess the order of each money.) {
        		JOptionPane.showMessageDialog(BarFrame.consts.ActionNotAllowed());
        		billButton.setSelected(false);
        		return;
        	}
        	if(billListPanel != null && Cmd_SplitItem.getInstance().getSourceBtn().isSelected()) {
        		billButton.setSelected(!billButton.isSelected());
        		return;
        	}
    		BarFrame.instance.setCurBillIdx(((FunctionToggleButton)o).getText());
            BarFrame.instance.switchMode(2);//no need to set open time? THE OPEN TIME of every bill are same?
		}else{

		}

	}

	private boolean adjustOnlyUI(int selectedRow, Dish dish) {
		if(getOrderedDishAry().get(selectedRow).getNum() == 1) {
			if("true".equals(CustOpts.custOps.getValue("noticeForLastOne"))) {
				if (JOptionPane.showConfirmDialog(BarFrame.consts.COMFIRMDELETEACTION2(), "WARNING!")) {
					table.setSelectedRow(-1);
					return true;
				}
				removeFromSelection(selectedRow);
			}else {
				removeFromSelection(selectedRow);
			}
		} else {
			int tQTY = getOrderedDishAry().get(selectedRow).getNum();
			dish.setTotalPrice(dish.getTotalPrice() - dish.getTotalPrice() / tQTY);
			dish.setNum(--tQTY);
			table.setValueAt(tQTY + "x", selectedRow, 0);
			table.setValueAt(BarOption.getMoneySign()
					+ BarUtil.formatMoney(dish.getTotalPrice()/100f),
					selectedRow, 3);
		}
		logOperation(-1, DBConsts.ORIGINAL, dish);
		return false;
	}

	public void adjustBothUIandDB() {

		Dish curDish = BarFrame.instance.curDish;

		if(curDish == null) {//check if there's an item selected.
			JOptionPane.showMessageDialog(BarFrame.consts.OnlyOneShouldBeSelected());
			return;
		}
		int outputId = curDish.getOutputID();
		if(outputId < 0) {
			//only do clean from UI, because the SALE_RECORD not generated yet, and will not affect the total in bill.
			removeFromSelection(table.getSelectedRow());
			logOperation(-1, DBConsts.ORIGINAL, curDish);
		} else {// if it's send
			JOptionPane.showConfirmDialog(
					BarFrame.consts.COMFIRMDELETEACTION(),
					org.cas.client.resources.international.DlgConst.DlgTitle,
					new JOptionPane.ConfirmDialogCallback() {
				@Override
				public void onResult(boolean confirmed) {
					if (confirmed) {
						deleteAsavedSaleRecord(curDish, outputId);
					}
				}
			});
		}
	}

	private void deleteAsavedSaleRecord(Dish curDish, int outputId) {
		//clean SALE_RECORD from db immediately.
		//No need to do it, will be called in method removeFromSelection() BillListPanel.curDish.changeOutputStatus(DBConsts.DELETED);
		//send cancel message to kitchen
		if(getStatus() < DBConsts.BILL_PRINTED) {
			Dish dish = curDish.clone();
			dish.setNum(1);
			sendDishToKitchen(dish, true);//if billPrinted, means already finished prepare and dish is on table.no need to tell kitchen to stop it.
			//update current bill is OK, because no bill printed yet. so no need to expire and regenerate a new bill.
			DBUtil.updatePriceAndCreateTimeInBillRecord(this, 0, null);//update bill info, must be after the screen update, because will get total from screen.
			//update SALE_RECORD number.(because not print temporary bill, so no need to set the out put status to exipired)
			DBUtil.updateOutput(dish.getOutputID(), "AMOUNT", curDish.getNum() - 1);
			if(curDish.getNum() == 1) {
				DBUtil.updateOutput(dish.getOutputID(), "STATUS", DBConsts.DELETED);
			}
			logOperation(-1,DBConsts.SAVED, curDish);
		}else { //if already billPrinted or refund, add to hashMapForRefund, waiting for confirmation action.
			if(mapForPrintedItemRemoving.containsKey(outputId)) {
				Dish dish = (Dish) mapForPrintedItemRemoving.get(outputId);
				dish.setNum(dish.getNum() + 1);
				dish.setTotalPrice(0 - curDish.getTotalPrice() / curDish.getNum() * dish.getNum());//unit price * current number
				dish.setDiscount(curDish.getDiscount() / curDish.getNum() * dish.getNum());
			}else {
				Dish dish = curDish.clone();
				dish.setNum(1);
				dish.setTotalPrice(0 - curDish.getTotalPrice() / curDish.getNum());
				dish.setDiscount(curDish.getDiscount() / curDish.getNum());
				mapForPrintedItemRemoving.put(outputId, dish);
			}

			logOperation(-1, curDish.getStatus(), curDish);
		}

		if(curDish.getNum() > 1) {
			int tQTY = curDish.getNum();
			curDish.setTotalPrice(curDish.getTotalPrice() - curDish.getTotalPrice() / tQTY);
			curDish.setNum(--tQTY);
			table.setValueAt(tQTY + "x", table.getSelectedRow(), 0);
			table.setValueAt(BarOption.getMoneySign()
					+ BarUtil.formatMoney(curDish.getTotalPrice()/100f),
					table.getSelectedRow(), 3);

			updateTotalArea(false);	//save to db immediately, because for lessing a saved SALE_RECORD, its nature. when removing a whole line,

		}else { //clean from screen.
			removeFromSelection(table.getSelectedRow());
		}
		//facture_original is not possible to be there, when we do formating the bill, will call pushEndMessage, there, we check if the comment is empty,
		//if empty will directly add the facture_original to the print content, not modifying the db and comment.
		String comment = getComment();
		if(getStatus() == DBConsts.BILL_PRINTED && !comment.contains(PrintService.FACTURE_REVISEE)) { //if comment is not empty, it could be: 1/refto: ---before printing the second bill.
			setComment(comment + PrintService.FACTURE_REVISEE);				//if it's already revised, don't add it again, for receipt, we remove the revisee if there is one.
			DBUtil.updateBill(getBillID(), "comment", getComment());
		}else if(getStatus() == DBConsts.COMPLETED && !comment.contains(ValuePossible.RE_FUND)) { //if comment is not empty, it could be: 1/refto: ---before printing the second bill.
			setComment(comment + ValuePossible.RE_FUND);				//if it's already revised, don't add it again, for receipt, we remove the revisee if there is one.
			DBUtil.updateBill(getBillID(), "comment", getComment());
		}
	}

	/*
	 * operationType: 1 means adding -1 mean removing
	 */
	private void logOperation(int operationType, int status, Dish dish) {
		StringBuilder sql = new StringBuilder(
				"INSERT INTO operationlog (OperationType, userId, BILLSTATUS, billId, outputId, tablename, opentime, billIdx, dishName, dishPrice, orderTime, operationTime) VALUES (")
				.append(operationType).append(", ")
				.append(LoginDlg.USERID).append(", ")
				.append(status).append(", ")
				.append(dish.getBillID()).append(", ")
				.append(dish.getOutputID()).append(", '")
				.append(BarFrame.instance.getCurTable()).append("', '")
				.append(BarFrame.instance.valOpenTime.getText()).append("', '")
				.append(BarFrame.instance.getCurBillIndex()).append("', '")
				.append(dish.getName1()).append("', ")
				.append(dish.getPrice()).append(", '")
				.append(dish.getOrderedTime()).append("', '")
				.append(BarUtil.df.format(new Date())).append("');");

			AikaPos.db.execSQL(sql.toString());
	}

//	@deprecated
	//was used when click send button, and found isFastFoodMode==true, then instead of returning back to table view, stay in
	//the sales view. now I decide to use unified processor : initContent().
//    void resetTableArea() {
//    	resetStatus();
//        Object[][] tValues = new Object[0][tblSelectedDish.getColumnCount()];
//        tblSelectedDish.setDataVector(tValues, header);
//        resetColWidth(scrContent.getWidth());
//        updateTotleArea();
//    }

    //table selection listener---------------------
	public void valueChanged(int selectedRow) {
		table.setSelectedRow(selectedRow);
		if (selectedRow < 0 || getOrderedDishAry().size() <= selectedRow) {
			BarFrame.instance.curDish = null;
			moreButton.setVisibility(View.INVISIBLE);
			lessButton.setVisibility(View.INVISIBLE);
			return;
		}
		if (status < DBConsts.COMPLETED && status >= DBConsts.ORIGINAL) {
			moreButton.setVisibility(View.VISIBLE);
			lessButton.setVisibility(View.VISIBLE);
		}
		if (getParentFragment() instanceof RefundItemDlg) {
			lessButton.setVisibility(View.VISIBLE);
		}

		Dish selectedDish = getOrderedDishAry().get(selectedRow);
		if (salesPanel != null) {
			BarFrame.instance.curDish = selectedDish;
			if (BarFrame.numberPanelDlg.isVisible()) {
				String value = table.getValueAt(selectedRow, 0);
				if (value != null)
					BarFrame.numberPanelDlg.setContents(value);
			}
			if (Cmd_DiscItem.getInstance().getSourceBtn().isSelected()) {
				String value = table.getValueAt(selectedRow, 2);
				if (value != null)
					BarFrame.numberPanelDlg.setContents(value);
			}
		} else if (billListPanel != null) {
			if (status >= DBConsts.COMPLETED) {
				return;
			}
			if (Cmd_SplitItem.getInstance().getSourceBtn().isSelected()) {
				billButton.setSelected(!billButton.isSelected());
				return;
			}
			if (BarFrame.instance.curDish != null && billListPanel.getCurBillPanel() != null && billListPanel.getCurBillPanel() != this) {
				billListPanel.moveDishToBill(this);
				BarFrame.instance.curDish = null;
			} else {
				billButton.setSelected(true);
				BarFrame.instance.curDish = selectedDish;
			}
		}
	}

//	//table row color----------------------------------------------------------
//	@Override
//	public Color getBackgroundAtRow(int row) {
//		return null;
//	}
//
//	@Override
//	public Color getForegroundAtRow(int row) {
//		if(row < getOrderedDishAry().size()) {
//			Dish dish = getOrderedDishAry().get(row);
//			if(dish.getOutputID() > -1) {
//				return Color.GRAY;
//			}else {
//				return Color.BLACK;
//			}
//		}else
//			return null;
//	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 用户开始触摸
//				if(v == scrContent.getViewport()) {
					if(billListPanel != null) {
						if(status >= DBConsts.COMPLETED) {	//if the bill is closed, then ignore.
							return false;
						}
						if(Cmd_SplitItem.getInstance().getSourceBtn().isSelected()) {	//if in splite item mode, then do nothing but select the bill button.
							billButton.setSelected(!billButton.isSelected());
							return false;
						}
						//if not in split item mode (split item button not pressed.)
						if(billListPanel.getCurBillPanel() == null) {
							billListPanel.getSelectedBillPannels().add(this);
							billButton.setSelected(!billButton.isSelected());
							return false;
						}
						if(BarFrame.instance.curDish != null && billListPanel.getCurBillPanel() != this) {	//this there's already an item ready for move.
							billListPanel.moveDishToBill(this);
							BarFrame.instance.curDish = null;
						}else {	//no current item ready for split, then just select the.
							billButton.setSelected(!billButton.isSelected());
							if(billButton.isSelected()) {
								BarFrame.instance.setCurBillIdx(billButton.getText());
								BarFrame.instance.setCurBillID(getBillID());
							}else {
								BillPanel activePanel = billListPanel.getCurBillPanel();
								if(activePanel != null) {
									BarFrame.instance.setCurBillIdx(activePanel.billButton.getText());
									BarFrame.instance.setCurBillID(activePanel.getBillID());
								}else {
									BarFrame.instance.setCurBillIdx("");
									BarFrame.instance.setCurBillID(0);
								}
							}
						}
					}
//				}
				break;
			case MotionEvent.ACTION_UP:
				stepCounter = 0;
				if(isDragging == true) {
					isDragging = false;
					int selectedRow = ((PIMTable)v).getSelectedRow();
					if(selectedRow < 0 || selectedRow >= getOrderedDishAry().size())
						return false;

					if(salesPanel != null && !BarFrame.numberPanelDlg.isVisible()) {	//if qty button not seleted.
						if(getOrderedDishAry().get(selectedRow).getOutputID() >= 0) {
							if (JOptionPane.showConfirmDialog(BarFrame.consts.COMFIRMDELETEACTION(), org.cas.client.resources.international.DlgConst.DlgTitle)) {// 确定删除吗？
								table.setSelectedRow(-1);
								return false;
							}
						}

						if(!notTooLateToChangeStatus()) {	//check if it's bill printed, if yes, need to reopen a bill.
							return false;
						}

						Dish dish = getOrderedDishAry().get(selectedRow);
						removeFromSelection(selectedRow);
						if(dish.getOutputID() > -1) {	//if already saved, then check if there's a refTo information in comment, if yes, means this SALE_RECORD belongs to a printed bill. should be put into void.
							DBUtil.changeOutputStatusInDB(dish, comment.contains(PrintUtil.OLD_SUBTOTAL) ? DBConsts.VOIDED : DBConsts.DELETED);	//if contains no ref_info, means the SALE_RECORD is just saved and send to
						} 																												//kitchen, not appeared in previous bill, then just change it to STATUS.
						//update array second.
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				stepCounter++;
				if(stepCounter > minStepCounter ) {
					stepCounter = 0;
					isDragging = true;
				}
				break;
		}
		return true; // 返回true表示事件已被处理
	}

	//add dish into the billPanel, tiggered by "+"button or the bubttons on menuPanel. \
	//moving items between bill doesn't go here.
	void addContentToList(Dish dish) {
		if (!notTooLateToChangeStatus()) {
			return;
		}

		Dish newDish = dish.clone(); // Clone the dish
		newDish.setOutputID(-1);
		newDish.setNum(1);
		int price = dish.getPrice();

		if (dish.getPricePomp() && BarOption.isTreatPricePromtAsTaxInclude()) {
			price = (int) Math.round(price / ((100 + BarOption.getGST() + BarOption.getQST()) / 100.0));
		}
		newDish.setTotalPrice(price * 1);
		newDish.setOrderedTime(BarUtil.df.format(new Date()));
		newDish.setCustomerIndex(BarFrame.instance.getCurBillIndex());
		newDish.setBillID(getBillID());
		getOrderedDishAry().add(newDish); // Add the dish to the list
		BarFrame.instance.curDish = newDish;

		// Update the adapter and scroll to the new item
		myAdapter.addDish(newDish);
		table.scrollToPosition(myAdapter.getItemCount() - 1);
		setSelectedPosition(myAdapter.getItemCount() - 1);

		updateTotalArea(false);
		updateStatusMsg();
	}

	private void setSelectedPosition(int position) {
		int previousPosition = myAdapter.getSelectedPosition();
		myAdapter.setSelectedPosition(position);
		if (previousPosition != -1) {
			myAdapter.notifyItemChanged(previousPosition);
		}
		myAdapter.notifyItemChanged(position);
	}

	private void updateStatusMsg() {
		BarFrame.instance.setStatusMes(table.getRowCount() + BarFrame.consts.itemAdded());
	}

    private void checkWithRules() {
    	CustomizedRule[] rules = BarFrame.menuPanel.getRules();
    	if(rules.length < 1) {
    		return;
    	}

    	//reset the ruleId in the dishes and reset the discount value.
		removeTheRuleEffect();

		// back up the rules and selected dishes.
		ArrayList<CustomizedRule> copyOfRules = new ArrayList<>();
		for (CustomizedRule rule : rules) {
			copyOfRules.add(rule);
		}
		ArrayList<Dish> copyOfSelection = new ArrayList<Dish>();
		for (Dish dish : getOrderedDishAry()) {
			copyOfSelection.add(dish);
		}

		while(copyOfRules.size() > 0) {	//note: can not use while(copyOfRules.size() > 0), because
			CustomizedRule rule = copyOfRules.get(0);
			if(ruleSatisfied(rule, copyOfSelection)) {	//all contained in current selection.
				discount += DBUtil.getActionPrice(rule);			// add the action of the rule into the discount.
				//@note: should not remove in this case, because satisfied rule could be used many times.
			}else {
				copyOfRules.remove(rule);				// when a rule not satisfied, the rule will be removed from list.
			}
		}
		updateTotalArea(false);
	}

	//reset the ruleId in the dishes and reset the discount value.
	private void removeTheRuleEffect() {
		List<Integer> ruleIds = new ArrayList<Integer>();
		for(Dish dish : getOrderedDishAry()) {	//step1, find out what rules used, and remove all te rule mark from dishes.
			Integer ruleId = dish.getRuleMark();
			if(ruleId != null) {	//care only not-null case, clean it, and if it not added ruleIds, add into the list.
				ruleIds.add(ruleId);	//added it into the "applied rule" collection, note: could be more than one and duplicated.

//can not do like this, because one combo could be selected multitimes.
//				for(Dish dish2 : orderedDishAry) {
//					if(dish2.getRuleMark() == ruleId) {
//
//					}
//				}
				for(CustomizedRule rule: BarFrame.menuPanel.getRules()) {	//step2, take donw the number from current discount according to each applied rules
					if(ruleId == rule.getId().intValue()) {
						String[] ids = rule.getContent().split(",");
						for (String id : ids) {
							for(Dish dish2 : getOrderedDishAry()) {
								if(dish2.getRuleMark() == ruleId && String.valueOf(dish2.getId()).equals(id)) {
									dish2.setRuleMark(null); //clean all related dish, not only current one.
									break;	//must stop, in case the dish belongs to other rule was cleaned.
								}
							}
						}
					}
				}
			}
		}
		for (Integer ruleId : ruleIds) {
			for(CustomizedRule rule: BarFrame.menuPanel.getRules()) {	//step2, take donw the number from current discount according to each applied rules
				if(ruleId == rule.getId().intValue()) {
					discount -= DBUtil.getActionPrice(rule);
					break;
				}
			}
		}
	}

	//if all the dishes in the rule is contained in selectionList, then return the dishlist, so they can be removed).
	private boolean ruleSatisfied(CustomizedRule rule, ArrayList<Dish> copyOfSelection) {
		String[] dishIdsInRule = rule.getContent().split(",");
		List<Dish> matchedDishesByRule = new ArrayList<Dish>();
		for(String id : dishIdsInRule) {
			for(int i = copyOfSelection.size() - 1; i >= 0; i--) {
				Dish dish = copyOfSelection.get(i);
				if(id.equals(String.valueOf(dish.getId()))) {
					matchedDishesByRule.add(dish);
					copyOfSelection.remove(dish);
					break;
				}
			}
		}
		//be very careful to the size, because if there's a "," in the end, then the size is not acurate.
		if(matchedDishesByRule.size() == dishIdsInRule.length) {
			//mark those dished with a flag indication the rule id. so when it's removed from the selection,
			//all relevent dishs will be removed the flag, and a action indicated price will
			//be added back. then a checkWithRules will be performed.
			for(Dish dish : matchedDishesByRule) {
				dish.setRuleMark(rule.getId().intValue());	//this dish is the dish in selection, not a copy.
			}
			return true;
		}else {	//if not satisfied, add back for next use of next rule.
			for(Dish dish: matchedDishesByRule) {
				copyOfSelection.add(dish);
			}
			return false;
		}
	}

    public void updateTotalArea(boolean skipSavingToDB) {
    	float gstRate = BarOption.getGST();
    	float qstRate = BarOption.getQST();
    	totalGst = 0;
    	totalQst = 0;
    	subTotal = 0;

    	for(Dish dish: getOrderedDishAry()) {
    		//get out the num.
    		int num = dish.getNum();
    		int pK = num /(BarOption.MaxQTY * 100);
    		if(num > BarOption.MaxQTY * 100) {
    			num = num %(BarOption.MaxQTY * 100);
    		}
    		int pS = num /BarOption.MaxQTY;
    		if(num > BarOption.MaxQTY) {
    			num = num % BarOption.MaxQTY;
    		}

    		int totalPrice = dish.getTotalPrice();
    		float gst = totalPrice * (dish.getGst() * gstRate / 100f);	//an item could have a different tax rate.
    		float qst = totalPrice * (dish.getQst() * qstRate / 100f);

//@NOTE: the price is already the final item totalprice (even the discount calculated), so no need to devide again.
//    		if(pS > 0) {
//    			price /= pS;
//    			gst /= pS;
//    			qst /= pS;
//    		}
//    		if(pK > 0) {
//    			price /= pK;
//    			gst /= pK;
//    			qst /= pK;
//    		}

    		subTotal += totalPrice;
    		totalGst += gst;
    		totalQst += qst;
    	}

    	subTotal -= discount;
    	subTotal += serviceFee;

    	if(BarOption.isDiscountAffectTax()) {
    		totalGst -= Math.round(discount * gstRate / 100f);
    		totalQst -= Math.round(discount * qstRate / 100f);
    	}

    	if(BarOption.isServiceFeeAffectTax()) {
    		totalGst += Math.round(serviceFee * gstRate / 100f);
    		totalQst += Math.round(serviceFee * qstRate / 100f);
    	}
    	totalGst = Math.round(totalGst);
    	totalQst = Math.round(totalQst);
    	this.updateLabelValues(discount, serviceFee, subTotal, totalGst, totalQst);
    	String totalPrice = BarUtil.formatMoney((subTotal + totalGst + totalQst)/100f);
    	if(totalOnBill > 0 && totalOnBill != subTotal + totalGst + totalQst) { //this can happen in split bill pane, when moving a dish to another bill.
    		valTotlePrice.setText(BarUtil.canadianPennyRound(totalPrice));
            BarFrame.setStatusMes("!!!" + BarFrame.consts.DISCOUNT_BILL() + BarFrame.consts.Applied() + "!!! " + BarFrame.consts.getPennyRounded() + totalPrice + " ");
    		totalOnBill = 0;
    		//no need to log error, because when add dish to a printed bill, will cause this case.
    		//L.e("BillPanel ", totalPrice + "the calculated total price are different from the total OnBill!" + totalOnBill, null);
    	}else {
    		valTotlePrice.setText(totalPrice);
            BarFrame.setStatusMes(BarFrame.consts.getPennyRounded() + BarUtil.canadianPennyRound(valTotlePrice.getText().toString()));
    	}
        //I comment this out because this should be done in cmd layer.
//    	if(!skipSavingToDB) {//I removed the condition "getBillID() > 0 && " because it's shouldn't be possible.
//	    	upateBillInfoIntoDB();
//    	}
//
//		if(BarFrame.secondScreen != null && salesPanel != null) {
//			BillPanel billPanel2 = BarFrame.customerFrame.billPanel;
//
//			billPanel2.updateLabelValues(discount, serviceFee, subTotal, totalGst, totalQst);
//			billPanel2.valTotlePrice.setText(valTotlePrice.getText());
//		}
    }

	public void updateLabelValues(float discount, int serviceFee, float subTotal, float totalGst, float totalQst) {
		lblDiscount.setText(discount > 0 ? BarFrame.consts.Discount() + ":-" + BarUtil.formatMoney((discount)/100f) : "");
    	lblServiceFee.setText(serviceFee > 0 ? BarFrame.consts.Serv() + ":" + BarUtil.formatMoney((serviceFee)/100f) : "");
    	lblSubTotle.setText(BarFrame.consts.Subtotal() + ":" + BarUtil.formatMoney(subTotal/100f));
    	lblTPS.setText(BarFrame.consts.GST() + ":" + BarUtil.formatMoney(totalGst/100f));
    	lblTVQ.setText(BarFrame.consts.QST() + ":" + BarUtil.formatMoney(totalQst/100f));
	}

    public void initContent() {
    	//first check is a billPane in billListPanel or it's a billPane in salesPanel
    	customerIndex = billButton != null ? billButton.getText() : BarFrame.instance.getCurBillIndex();
		//used STATUS <= 1, means both uncompleted and normally completed will be displayed, unnormally delted recored will be delted = 100
		String tableName = (String)BarFrame.instance.getCurTable();
		if(tableName.length() == 0) {	//add this check in case background thread update tables every minute, might reinit the model of cmbCurTable, now I've added check to avoid it.
			L.e("BillPane", "no table info found from cmbCurtable", null);
		}
		String openTime = BarFrame.instance.valOpenTime.getText().toString();	//if it in cmd_return action, the opentime will be null

		//@Note, only when showing an expried bill (from checklistdlg), we give a billID to the method, for other case, we leave it empty.
		//the BarFrame.instance.getCurBillID() always has value available, but we don't use it on purpose, because if the billID is empty, we'll know it's not a expired bill, the logic of fetching SALE_RECORD is easier.
		//
		//!!!be careful that this openTime on BarFrame is from tableButton's started Time, TableButton's started time could be Bill's created time(in counter mode), or opentime (table mode)
		//why? becaue in counter mode, we feel table opened time is not necessary(new table is created and opened as soon as the dish on old table is saved, while maybe it's not used for long time until next customer
		//come in....), so, it make more sense to calculate form the time that dish send to kitchen.
		initContent(BarFrame.instance.isShowingAnExpiredBill() ? String.valueOf(BarFrame.instance.getCurBillID()) : "",
				customerIndex, tableName, openTime,
				true);
	}

    public void initContent(String billId, String billIndex, String tableName, String openTime, boolean skipSavingToDB) {
    	this.customerIndex = billIndex; //in case someone call this from outside.
    	if((billId == null || "".equals(billId)) && (openTime == null || "".equals(openTime))) {
			//we can find bill by _id, if no _id, then openTime is a "must have" to locate a sigle bill.
    		//when returned from panel1, then login, then if it's counter mode, will switch to panel[2], in this case, there no start time displayed.
    		//because no table has been clicked. all we have is tableName and Bill Idx. (wait, who called BarFrame.instance.setCurBillID()? why not set vlStartTime also.)
    		//
        	//TODO: i think we need to call this, NO? resetProperties();
    		String[][] tValues = new String[0][4];
			myAdapter.data = tValues;
			// may need this if throw exception: myAdapter.notifyDataSetChanged();
    		updateTotalArea(skipSavingToDB);
    		resetColWidth();
//    		if(BarFrame.secondScreen != null) {
//            	BarFrame.customerFrame.billPanel.resetColWidth();
//            }
    		return;
    	}

    	resetProperties();

    	//get outputs of current table and bill id.
		try {
			// update the discount and service fee, and tip info, and (don't forget the billID).
			initBillProperties(billId, billIndex, tableName, openTime);

			setOrderedDishAry(initTableWithOutput(billId, billIndex, tableName, openTime));	//make the table display the dishes in this bill first. (SALE_RECORD of dumped bill will not match billId.)

			// do not set the default selected value, if it's used in billListDlg.
			if (salesPanel != null && getOrderedDishAry().size() > 0) {
				table.setSelectedRow(getOrderedDishAry().size() - 1);
				valueChanged(getOrderedDishAry().size() - 1);
			}
		} catch (Exception e) {
			L.e("BillPanel", " exception when initContent()", e);
		}

		updateTotalArea(skipSavingToDB);
		switch (status) {
		case DBConsts.BILL_PRINTED:
			findViewById(R.id.tableListView).setBackgroundColor(Color.GREEN);
			break;
		case DBConsts.SUSPENDED:
			findViewById(R.id.tableListView).setBackgroundColor(Color.RED);
			break;
		case DBConsts.COMPLETED:
			findViewById(R.id.tableListView).setBackgroundColor(Color.GRAY);
			break;
		case DBConsts.VOIDED:
			findViewById(R.id.tableListView).setBackgroundColor(Color.DKGRAY);
			break;
		case DBConsts.EXPIRED:
			findViewById(R.id.tableListView).setBackgroundColor(Color.YELLOW);
			break;
		case DBConsts.DELETED:
			findViewById(R.id.tableListView).setBackgroundColor(Color.BLACK);
			break;

		default:
			findViewById(R.id.tableListView).setBackgroundColor(Color.TRANSPARENT);
		}

		if(salesPanel != null && customerFrame == null && billButton == null) {
			CmdBtnsManagerDlg.refreshBtnStatusForSalesPanel(status);
			if(BarOption.getPlatformCharacters().contains(tableName.subSequence(0, 1))) { // if it's a aplatform bill (base on the table name, we can tell this), we need to refresh
				CmdBtnsManagerDlg.disableNonPlatformPaymentBtns();
			}
		}

		//reset the flag which is only used for showing expired bills.
		BarFrame.instance.setShowingAnExpiredBill(false);
	}

	private ArrayList<Dish> initTableWithOutput(String billId, String billIndex, String tableName, Object openTime) throws SQLException {
		long intCreateTime = 0;
		try {
			intCreateTime = BarUtil.df.parse(createdTime).getTime() / 1000;
		} catch (ParseException e1) {
			L.e("BillPanel", "unexpected exception when parsing create time to a int value:" + createdTime, e1);
		}

		ArrayList<Dish> dishAry = new ArrayList<Dish>();
		StringBuilder sql = new StringBuilder("select SALE_RECORD.createdTime as SALE_RECORD_CREATEDTime,")
				.append("SALE_RECORD.STATUS as SALE_RECORD_STATUS, SALE_RECORD.productId as SALE_RECORD_productId,")
				.append("SALE_RECORD.openTime as SALE_RECORD_openTime, SALE_RECORD.modifiedTime as SALE_RECORD_modifiedTime,")
				.append("SALE_RECORD.DISCOUNT as SALE_RECORD_DISCOUNT, SALE_RECORD.MODIFICATIONS as SALE_RECORD_MODIFICATIONS,")
				.append("SALE_RECORD.AMOUNT as SALE_RECORD_AMOUNT, SALE_RECORD._id as SALE_RECORD_id,")
				.append("SALE_RECORD.billId as SALE_RECORD_billId, SALE_RECORD.totalPrice as SALE_RECORD_totalPrice,")
				.append("DISH._id as DISH_ID, DISH.category as DISH_category,")
				.append("DISH.displayIdx as DISH_displayIdx, DISH.GST as DISH_GST, DISH._id as DISH_id, ")
				.append("DISH.NAME1 as DISH_NAME1, DISH.NAME2 as DISH_NAME2, DISH.NAME3 as DISH_NAME3, ")
				.append("DISH.PRICE as DISH_PRICE, DISH.printerIds as DISH_printerIds, DISH.menuPomp as DISH_menuPomp, ")
				.append("DISH.ModifyPomp as DISH_ModifyPomp, DISH.pricePomp as DISH_pricePomp, DISH.QST as DISH_QST, ")
				.append("DISH.SIZE as DISH_SIZE ")
				.append("from SALE_RECORD, DISH where SALE_RECORD.tableName = '").append(tableName)
				.append("' and billIndex = ").append(billIndex)
				.append(" AND SALE_RECORD_productId = DISH_ID and SALE_RECORD_openTime = '").append(openTime).append("'");

		if (billId != null && billId.length() > 0) {
			sql.append(" and SALE_RECORD_CREATEDTime <= '").append(createdTime).append("'")
					.append(" and (SALE_RECORD_STATUS is null or SALE_RECORD_STATUS < ").append(DBConsts.DELETED).append(")");
		} else {
			sql.append(" and (SALE_RECORD_STATUS is null or SALE_RECORD_STATUS < ").append(DBConsts.VOIDED).append(")");
		}

		Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);

		String[][] datas = new String[rs.getCount()][];
		for(int i = 0; i < datas.length; i++){
			rs.moveToNext();
			int modifiedTime = DBUtil.getCursorInt(rs, "SALE_RECORD_modifiedTime");
			if(DBUtil.getCursorInt(rs, "SALE_RECORD_STATUS") == DBConsts.VOIDED && modifiedTime < intCreateTime ) {
				continue;
			}
			Dish dish = new Dish();
			dish.setCategory(DBUtil.getCursorString(rs, "DISH_category"));
			dish.setDiscount(DBUtil.getCursorInt(rs, "SALE_RECORD_DISCOUNT"));
			dish.setDisplayIdx(DBUtil.getCursorInt(rs, "DISH_displayIdx"));
			dish.setGst(DBUtil.getCursorInt(rs, "DISH_GST"));
			dish.setId(DBUtil.getCursorLong(rs, "DISH_id"));
			dish.setName1(DBUtil.getCursorString(rs, "DISH_NAME1"));
			dish.setName2(DBUtil.getCursorString(rs, "DISH_NAME2"));
			dish.setName3(DBUtil.getCursorString(rs, "DISH_NAME3"));
			dish.setModification(DBUtil.getCursorString(rs, "SALE_RECORD_MODIFICATIONS"));
			dish.setNum(DBUtil.getCursorInt(rs, "SALE_RECORD_AMOUNT"));
			dish.setOutputID(DBUtil.getCursorInt(rs, "SALE_RECORD_id"));
			dish.setPrice(DBUtil.getCursorInt(rs, "DISH_PRICE"));
			dish.setPrinterIds(DBUtil.getCursorString(rs, "DISH_printerIds"));
			dish.setMenuPomp(Boolean.valueOf(DBUtil.getCursorString(rs, "DISH_menuPomp")));
			dish.setModifyPomp(Boolean.valueOf(DBUtil.getCursorString(rs, "DISH_ModifyPomp")));
			dish.setPricePomp(Boolean.valueOf(DBUtil.getCursorString(rs, "DISH_pricePomp")));
			dish.setQst(DBUtil.getCursorInt(rs, "DISH_QST"));
			dish.setSize(DBUtil.getCursorInt(rs, "DISH_SIZE"));
			dish.setCustomerIndex(billIndex);
			dish.setOrderedTime(DBUtil.getCursorString(rs, "SALE_RECORD_CREATEDTime"));
			dish.setBillID(DBUtil.getCursorInt(rs, "SALE_RECORD_billId"));
			dish.setTotalPrice(DBUtil.getCursorInt(rs, "SALE_RECORD_totalPrice"));
			dish.setStatus(DBUtil.getCursorInt(rs, "SALE_RECORD_STATUS"));
			dishAry.add(dish);

			String[] tValues = new String[4];
			tValues[0] = DBUtil.getDisplayableNumWithX(dish.getNum());

			tValues[1] = NetUtil.trimContentFromHTML(dish.getLanguage(LoginDlg.USERLANG));
			if(tValues[1] == null || tValues[1].trim().length() == 0){
				tValues[1] = NetUtil.trimContentFromHTML(dish.getLanguage(0));
			}

			String modification = dish.getModification();
			if(modification == null){
				modification = "";
			}
			String[] marks = modification.split(BarDlgConst.delimiter);
			StringBuilder sb = new StringBuilder();
			for (String mark : marks) {
				if (mark.trim().isEmpty()) {
					continue;
				}
				String[] langs = mark.split(BarDlgConst.semicolon);
				String lang = langs.length > LoginDlg.USERLANG ? langs[LoginDlg.USERLANG] : langs[0];
				if (lang.isEmpty() || "null".equalsIgnoreCase(lang)) {
					lang = langs[0].isEmpty() || "null".equalsIgnoreCase(lang) ? "" : langs[0];
				}
				sb.append(BarUtil.changeToLangForModify(lang, LoginDlg.USERLANG)).append(" ");
			}
			if(dish.getDiscount() == 0) {
				tValues[2] = sb.toString();
			}else{
				tValues[2] = sb.append("  -").append(BarOption.getMoneySign()).append(BarUtil.formatMoney(dish.getDiscount() / 100.0)).toString();
			}

			tValues[3] =  BarOption.getMoneySign() + dish.getTotalPrice() / 100f;
			datas[i] = tValues;
		}
		myAdapter.data = datas;
		// may need this if throw exception: myAdapter.notifyDataSetChanged();

		resetColWidth();
		return dishAry;
	}


	private void initBillProperties(String billId, String billIdx, String tableName, String openTime) throws SQLException {
		StringBuilder sql = billId != null && billId.length() > 0 ?
				new StringBuilder("select * from Bill where _id = ").append(billId)
				: new StringBuilder("select * from Bill where billIndex = '").append(billIdx).append("'")
				.append(" and tableName = '").append(tableName).append("'")
				.append(" and opentime = '").append(openTime).append("'")
				.append(" and (status is null or status < ").append(DBConsts.EXPIRED).append(")");// for expired bills, for sure it will have billId.

		Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
		if(rs.moveToNext()) {
		    discount = DBUtil.getCursorInt(rs, "DISCOUNT");
		    serviceFee = DBUtil.getCursorInt(rs, "serviceFee");
		    tip = DBUtil.getCursorInt(rs, "serviceFee");
		    cashback = DBUtil.getCursorInt(rs, "CASHBACK");
		    status = DBUtil.getCursorInt(rs, "STATUS");
		    comment = DBUtil.getCursorString(rs, "COMMENT");
		    int pSF = comment.lastIndexOf(SERVICEFEE);
		    int pDB = comment.lastIndexOf(DISCBILL);
		    if(pSF > -1) {
		    	serviceFee = Integer.valueOf(comment.substring(pSF + 11, comment.indexOf("@", pSF)));
		    }
		    if(pDB > -1) {
		    	discount = Integer.valueOf(comment.substring(pDB + 9, comment.indexOf("@", pDB)));
		    }
		    totalOnBill = DBUtil.getCursorInt(rs, "TOTAL");
		    createdTime = DBUtil.getCursorString(rs, "createdTime");
		    billID = DBUtil.getCursorInt(rs, "_id");
		}
	}

	private void resetProperties(){
        getOrderedDishAry().clear();
        discount = 0;
        tip = 0;
        serviceFee = 0;
        received = 0;
        cashback = 0;
        //set bill ID to 0 is not helpful, it should be updated in each call. setBillID(0);
        comment = "";
        status = DBConsts.ORIGINAL;
//        setBackground(null);

        totalGst = 0;
    	totalQst = 0;
    	subTotal = 0;
        totalOnBill = 0;

		mapForPrintedItemRemoving.clear();
    }

    public void resetColWidth() {
//    	int tableWidth = getWidth();
//        PIMTableColumn tmpCol1 = table.getColumnModel().getColumn(0);
//        tmpCol1.setWidth(30);
//        tmpCol1.setPreferredWidth(30);
//
//        PIMTableColumn tmpCol4 = table.getColumnModel().getColumn(3);
//        tmpCol4.setWidth(69);
//        tmpCol4.setPreferredWidth(69);
//
//        //at first, teh tableWidth is 0, then after, the tableWidth will be 260.
//        PIMTableColumn tmpCol2 = table.getColumnModel().getColumn(1);
//        int defaultWidth = (tableWidth - tmpCol1.getWidth() - tmpCol4.getWidth()) / 2 + 3;
//        tmpCol2.setWidth(BarOption.getNameColWidth(defaultWidth));
//        tmpCol2.setPreferredWidth(BarOption.getNameColWidth(defaultWidth));
//
//        PIMTableColumn tmpCol3 = table.getColumnModel().getColumn(2);
//        defaultWidth = (tableWidth - tmpCol1.getWidth() - tmpCol2.getWidth() - tmpCol4.getWidth()) - 3;
//        tmpCol3.setWidth(defaultWidth - (scrContent.getVerticalScrollBar().isVisible() ? scrContent.getVerticalScrollBar().getWidth() : 0));
//        tmpCol3.setPreferredWidth(defaultWidth - (scrContent.getVerticalScrollBar().isVisible() ? scrContent.getVerticalScrollBar().getWidth() : 0));
//
//        table.setRowHeight(billButton == null ? BarOption.getTableRowHeight() : BarOption.getListViewTableRowHeight());
//
//        table.validate();
//        table.revalidate();
//        table.invalidate();
    }

    void removeFromSelection(int selectedRow) {

		int tValidRowCount = getUsedRowCount(); // get the used RowCount
    	if(selectedRow < 0 || selectedRow > tValidRowCount - 1) {
    		JOptionPane.showMessageDialog(BarFrame.consts.OnlyOneShouldBeSelected());
    		L.e("BillPanel", "Unexpected row number when calling removeAtSelection : " + selectedRow, null);
    		return;
    	}

    	//update db first
    	Dish dish = getOrderedDishAry().get(selectedRow);

		getOrderedDishAry().remove(selectedRow);

    	if(dish.getRuleMark() != null) {
    		checkWithRules();
    	}

    	//update the table view
		removeSelectedLineFromTable(selectedRow, tValidRowCount);
	}

	private void removeSelectedLineFromTable(int selectedRow, int tValidRowCount) {
		int tColCount = 4;
		String[][] tValues = new String[tValidRowCount - 1][tColCount];
		for (int r = 0; r < tValidRowCount; r++) {
			if(r == selectedRow) {
				continue;
			}else {
				int rowNum = r > selectedRow ? r : r + 1;
			    for (int c = 0; c < tColCount; c++)
			        tValues[rowNum-1][c] = ((MyAdapter)table.getAdapter()).getValueAt(r, c);
			}
		}
		((MyAdapter)table.getAdapter()).data = tValues;
		table.adapter.notifyDataSetChanged();
		resetColWidth();
//        if(BarFrame.secondScreen != null) {
//        	BarFrame.customerFrame.billPanel.resetColWidth();
//        }
		table.setSelectedRow(tValues.length - 1); //@Note this will trigger a value change event, to set the curDish.
		valueChanged(tValues.length - 1);
		updateTotalArea(false);
		updateStatusMsg();
	}

    //make sure it's not too late to change, return true means still OK, move on, return false means the bill is too old to move on.
	public boolean notTooLateToChangeStatus() {
		if(status >= DBConsts.COMPLETED || status < DBConsts.ORIGINAL) {//check if the bill is already completed.
			JOptionPane.showMessageDialog(BarFrame.consts.ActionNotAllowed());
		    return false;
		}
		return true;
	}

	//actually this mentod works only for the partially adjusted outputs. it doesn't touch(expire and recreate) those was not modified.
	public void expireAndRegenerateModifiedOutputs(int status) {
		Map<Integer, Dish> originalDishes = getOrderedDishAry().stream()
				.collect(Collectors.toMap(Dish::getOutputID, dish -> dish));
		//base on the hashMapFroReturn values, adjust the SALE_RECORD status, and the bill cash values.
		StringBuilder sql = null;
		for(Dish dish: mapForPrintedItemRemoving.values()) {
			try {
				//clone and expire old SALE_RECORD, modify the  new SALE_RECORD.
				Dish originalDish = originalDishes.get(dish.getOutputID());
				if(originalDish != null) {
					int newOutputId = DBUtil.createOutput(originalDish, BarFrame.instance.getCurBillIndex(), originalDish.getOrderedTime());
					//为啥dish中记录的是要退还de钱，不是还剩下的钱呢？可能是为了打印时方便？现在为了算出来剩余的钱给新生成的output，必须做些调整：
					sql = new StringBuilder("update SALE_RECORD set amount = ").append(originalDish.getNum())
							.append(", toltalPrice = ").append(originalDish.getTotalPrice())
							.append(", discount = ").append(originalDish.getDiscount())
							.append(", STATUS = ").append(status)
							.append(", createdTime = '").append(BarUtil.df.format(new Date()))
							.append("' where _id = ").append(newOutputId);
					AikaPos.db.execSQL(sql.toString());
					//create ouput records specially for the decreased items, make them status as voided, so they can appear in the report.
					newOutputId = DBUtil.createOutput(dish, BarFrame.instance.getCurBillIndex(), dish.getOrderedTime());
					sql = new StringBuilder("update SALE_RECORD set STATUS = ").append(DBConsts.VOIDED)
							.append(", createdTime = '").append(BarUtil.df.format(new Date()))
							.append("' where _id = ").append(newOutputId);
					AikaPos.db.execSQL(sql.toString());
				}else {
					int newOutputId = DBUtil.createOutput(dish, BarFrame.instance.getCurBillIndex(), dish.getOrderedTime());
					sql = new StringBuilder("update SALE_RECORD set STATUS = ").append(DBConsts.VOIDED)
							.append(", createdTime = '").append(BarUtil.df.format(new Date()))
							.append("' where _id = ").append(newOutputId);
					AikaPos.db.execSQL(sql.toString());
				}
				sql = new StringBuilder("update SALE_RECORD set STATUS = ").append(DBConsts.EXPIRED)
						.append(" where _id = ").append(dish.getOutputID());
				AikaPos.db.execSQL(sql.toString());
				//TODO: if the
				//TODO: and the bill cash values, bill status
			} catch (Exception e) {
		        L.e("expireAndRegenerateOutputs", "数据库更新异常", e);
			}
	    }
	}

	//caller can specify the billIdx, if the billIdx is not specified, then use the billIdx on Frame.
	public void expireAndReGenerateBill(String customerIdx) {
		if(customerIdx == null || customerIdx.length() == 0) {
			customerIdx = BarFrame.instance.getCurBillIndex();
		}

		try {
	 		//Generate new bill with ref to expired bill everything else use the data on current billPane
	 		//@NOTE:Currently the SALE_RECORD will be choosed by table and billIdx, so old SALE_RECORD will go to new bill automatically.
	 		//      the bad thing is, if user open the dumped old bill, then the removed item will be disappears and new added item will appear on old bill also.
	 		//      this will be a known bug.
			//      TDOO:we can make it better by searching expired SALE_RECORD and filter out new created SALE_RECORD when initContent for a expired bill. government will need to check the expired bills.
	 		//??what do we do when removing an saved item from billPanel, and decrease conten from billPanel?
			//TODO: when print temporary bill, or move item between bills, should check the hashMap to see if their's any item change in the SALE_RECORD.
			// if there'is change (including removed and modified),  exprie old SALE_RECORD and regenerate a new SALE_RECORD.
			//      but seems when refunding a completed bill, every time click btnLess, will modify the SALE_RECORD db, so when refun?
			//
	 		if(status >= DBConsts.BILL_PRINTED || status < DBConsts.ORIGINAL) {
		 		StringBuilder newComment = new StringBuilder(PrintUtil.REF_TO).append(getBillID());
				if(status >= DBConsts.COMPLETED || status < DBConsts.ORIGINAL) {	//if already paid, then need to know old moneys, so in mev can report how much added or returned.
					newComment.append("F");
				}
				newComment.append("\n").append(PrintUtil.DATE_REF).append(this.getZippedCreateTime());
				newComment.append("\n").append(PrintUtil.OLD_SUBTOTAL).append(BarUtil.formatMoney(subTotal / 100.0));	//this value will be needed anyway.
				if(status >= DBConsts.COMPLETED || status < DBConsts.ORIGINAL) {	//@Note mess the order of each money.
					newComment.append("\n").append(PrintUtil.OLD_GST).append(BarUtil.formatMoney(totalGst / 100.0))
						.append("\n").append(PrintUtil.OLD_QST).append(BarUtil.formatMoney(totalQst / 100.0))
						.append("\n").append(PrintUtil.OLD_TOTAL).append(valTotlePrice.getText());
				}

				//as long as a bill is regenerated, the status of the bill must be at least billprinted.
				//now the problem is shall we remember the old price? if we append the old moneys, then when we calculate the mtTransAvTaxes, sub total will minus
				//the oldSubtotal, which means the mtTransAVTaxes will eventurally be the differents----this is good for modified invoice, not for modified check.
				//so we currently decide only when it's completed, we append the old moneys. that mean, when we are calculating a mtTransAvTaxes for checks, we
				//should remenber that there's only one old money(subtotal) in end message if it's not completed, but bill printed....we have to keep this one because
				//when the check modified, we need to know the old subtotal in ref part.
				this.comment = newComment.toString();	//set the comment property, so when creating a new bill base on current one, will copy the comment into the new bill.
			}
			 //TODO: why here is a logic bug, currently it will never be reached.....redo it when understand the meaning of government.
			 else if(status < DBConsts.ORIGINAL){	//according to revenue test case, if it's refunded bill, when regenerate a bill base on it, should have no ref part, (personally don't understand it yet).
				this.comment = "";
			}

	 		// create a new bill=============================================================================
	 		int newBillID = cloneCurrentBillRecord(null,
					customerIdx,
					Math.round(Float.valueOf(valTotlePrice.getText().toString()) * 100));

			//expire the old bill
			StringBuilder sql = new StringBuilder("update bill set status = ").append(DBConsts.EXPIRED)
	 				.append(" where _id = ").append(getBillID());
			AikaPos.db.execSQL(sql.toString());
	 		//================================================================================================

	        //when we reopen a refunded bill, we create a new bill which is a original bill, so we must clean the received money with the refund count.
	 		if(status < DBConsts.ORIGINAL){	//save the old money numbers, in case the old status is negative(means have returned some/not all money).
	 			StringBuilder sb = new StringBuilder("select * from bill where _id = " + newBillID);

 	    		Cursor rs = AikaPos.db.rawQuery(sb.toString(), null);
 	            rs.moveToNext();

 	            int oldCashReceived =DBUtil.getCursorInt(rs, "cashReceived");
 	            int oldDebitReceived = DBUtil.getCursorInt(rs, "debitReceived");
 	            int oldVisaReceived = DBUtil.getCursorInt(rs, "visaReceived");
 	            int oldMasterReceived = DBUtil.getCursorInt(rs, "masterReceived");

 	            //clean cashback and tip first, and clean status at the same time.
 	        	//set status = 0 immediatly when refund is covered by received money, in case another receive can also cover the refund, will be minus twice.
 	            int newCashReceived = oldCashReceived + cashback;	//cashReceived is always related with cashback.
	        	if(newCashReceived + status > 0) {
	        		newCashReceived += status;
	        		status = -1;	//-1 means negative 1 cent, right? we have to keep it negative, because if it's 0, will mean this bill is a new opened bill.
	        	}else {
	        		status += newCashReceived;
	        		newCashReceived = 0;
	        	}

 	        	//debitReceived
 	        	int newDebitReceived = oldDebitReceived;
 	        	if(oldDebitReceived > tip) { //as long as there's enough money, we suppose tip was from the money of this payment.
 	        		newDebitReceived -= tip;
 	        	}
 	        	if(newDebitReceived + status > 0) { //after count out tip, the money is more close to subtotal
    				newDebitReceived += status;		//now if there's enough money, we suppose the refund was also from this money.
    				status = -1;
    			} else {							//if no enough money, take all money form this payment, and calculate how much left
    				status += newDebitReceived;		//will use credit money to continue the refund.
    				newDebitReceived = 0;
    			}

 	        	//visaReceived
 	            int newVisaReceived = oldVisaReceived;
 	            if(newVisaReceived > tip) {			//
 	            	newVisaReceived -= tip;
 	        	}
    			if(newVisaReceived + status > 0) {
    				newVisaReceived += status;
    				status = -1;
    			} else {
    				status += newVisaReceived;
    				newVisaReceived = 0;
    			}

 	            //masterReceived
 	            int newMasterReceived = oldMasterReceived;
 	            if(newMasterReceived > tip) {
 	            	newMasterReceived -= tip;
 	        	}
    			if(newMasterReceived + status > 0) {
    				newMasterReceived += status;
    				status = -1;
    			} else {
    				status += newMasterReceived;
    				newMasterReceived = 0;
    			}

 	            sql = new StringBuilder("update bill set TIP = 0, CASHBACK = 0, STATUS = ").append(status).append(", cashReceived = ").append(newCashReceived)
 	            		.append(", debitReceived = ").append(newDebitReceived)
 	            		.append(", visaReceived = ").append(newVisaReceived)
 	            		.append(", masterReceived = ").append(newMasterReceived)
 	            		.append(" where _id = ").append(newBillID);
				AikaPos.db.execSQL(sql.toString());
 		 	    status = DBConsts.ORIGINAL;	//will be used when clicking buttons, set to original, so will not trigger warning dialogs.
	 		}else if(status < DBConsts.COMPLETED){
	 			//when splitting a bill in billPrinted status. new generated bill should be changed to saved status
	 			DBUtil.updateBill(newBillID, "STATUS", DBConsts.SAVED);
	 		}
	 		//NOTE:following changing will not affect the interface when reopenning a completed bill from SaleRecordDlg.
   			//tip must be able to set to 0.
	        tip = 0;
	        cashback = 0;
	 		setBillID(newBillID);			//will be used when adding new item into the bill
		}catch(Exception exp) {
			L.e("SalesPane", "Exception happenned when converting bill's status to 0", exp);
		}
	}

	private int getUsedRowCount() {
        for (int i = 0, len = table.getRowCount(); i < len; i++)
            if (table.getValueAt(i, 0) == null)
                return i; // 至此得到 the used RowCount。
        return table.getRowCount();
    }

    void reLayout() {
//    	// table area-------------
//        int poxX = 0;
//        int posY = 0;
//        int scrContentHeight = getHeight() - BarFrame.consts.TotalAreaHeight;
//        if(billButton != null) {
//        	billButton.setBounds(poxX, posY, getWidth(), CustOpts.BTN_HEIGHT + 16);
//        	posY += billButton.getHeight();
//// BarFrame.consts.TotalAreaHeight; has made up enought room for billbuton. the left is enough for a total Price.
////        	scrContentHeight -= billButton.getHeight() - valTotlePrice.getPreferredSize().height - CustOpts.VER_GAP * 3;
//        }
//        scrContent.setBounds(poxX, posY, getWidth(), scrContentHeight);
//
//		// sub total-------
//		if(billButton == null){
//        	moreButton.setBounds(scrContent.getX() + scrContent.getWidth() - BarFrame.consts.SCROLLBAR_WIDTH,
//        			scrContent.getY() + scrContent.getHeight() + CustOpts.VER_GAP * 3,
//            		BarFrame.consts.SCROLLBAR_WIDTH, BarFrame.consts.SCROLLBAR_WIDTH + 4);
//            lessButton.setBounds(moreButton.getX() - CustOpts.HOR_GAP - BarFrame.consts.SCROLLBAR_WIDTH, moreButton.getY(),
//            		BarFrame.consts.SCROLLBAR_WIDTH, BarFrame.consts.SCROLLBAR_WIDTH + 4);
//
//    		valTotlePrice.setBounds(scrContent.getX(), scrContent.getY() + scrContent.getHeight() + CustOpts.VER_GAP,
//    				lessButton.getX() - scrContent.getX() - 5, BarFrame.consts.TotalAreaHeight - CustOpts.VER_GAP - 3);
//
//        }else {
//    		valTotlePrice.setBounds(scrContent.getX(), scrContent.getY() + scrContent.getHeight() - 15,
//    				scrContent.getWidth() - 5, BarFrame.consts.TotalAreaHeight - CustOpts.VER_GAP - 3);
//        }
//        lblSubTotle.setBounds(scrContent.getX(), scrContent.getY() + scrContent.getHeight() + 40 + CustOpts.VER_GAP * 2,
//        		150, lblSubTotle.getPreferredSize().height);
//        lblDiscount.setBounds(scrContent.getX(), scrContent.getY() + scrContent.getHeight() + CustOpts.VER_GAP,
//        		110, lblSubTotle.getHeight());
//        lblServiceFee.setBounds(lblDiscount.getX(), lblDiscount.getY() + lblDiscount.getHeight() + CustOpts.VER_GAP,
//        		110, lblDiscount.getHeight());
//
//		lblTPS.setBounds(scrContent.getX() + lblServiceFee.getWidth(), lblDiscount.getY(),
//				100, lblTPS.getPreferredSize().height);
//		lblTVQ.setBounds(lblTPS.getX(), lblTPS.getY() + lblTPS.getHeight() + CustOpts.VER_GAP,
//				100, lblTVQ.getPreferredSize().height);
//
    }

    private void initComponent() {
        lblSubTotle.setText(BarFrame.consts.Subtotal());
        lblTPS.setText(BarFrame.consts.GST());
        lblTVQ.setText(BarFrame.consts.QST());

//        String bg = BarOption.getBK("Bill");
//    	if(bg == null) {
//    		bg = "#d8d8d8";
//    	}
//        setBackgroundColor(Color.parseColor(bg));
//        setLayout(null);
													//in that method will check if the mirrorTable exist.
//        table.setRenderAgent(this);
//        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
//        table.setFont(BarOption.getSelectionFont());
//        if(BarOption.isMultiLineCellRender()) {
//        	table.setDefaultRenderer(Object.class, new TextAreaPIMTableCellRenderer());
//        }
		headerName.setText(BarFrame.consts.ProdName());
		headerQty.setText(BarFrame.consts.Count());
		headerPrice.setText(BarFrame.consts.Price());
		myAdapter = new MyAdapter(this, new String[][]{});
        table.setAdapter(myAdapter);
        //seems following 4 lines useless...because the table.getColumnModel().getColumn(1) seems doesn't work.
//        DefaultPIMTableCellRenderer tCellRender = new DefaultPIMTableCellRenderer();
//        tCellRender.setOpaque(true);
//        tCellRender.setBackground(Color.LIGHT_GRAY);
//        table.getColumnModel().getColumn(1).setCellRenderer(tCellRender);

//        valTotlePrice.setFont(BarOption.lessBigFont);
//        valTotlePrice.setHorizontalAlignment(SwingConstants.RIGHT);
        //@do_not_work! valTotlePrice.setHorizontalAlignment(SwingConstants.RIGHT);
        //@work! valTotlePrice.setAlignmentX(Component.RIGHT_ALIGNMENT);
      //@do_not_work!valTotlePrice.setBackground(Color.RED);
      //@do_not_work!valTotlePrice.setOpaque(false);

//        TextView tLbl = new TextView();
//        tLbl.setAlpha(1.0f);
//        tLbl.setBackgroundColor(Color.GRAY);
//        scrContent.setCorner(JScrollPane.LOWER_RIGHT_CORNER, tLbl);
//        Font tFont = PIMPool.pool.getFont((String) CustOpts.custOps.hash2.get(PaneConsts.DFT_FONT), Font.PLAIN, 40);

        // Margin-----------------
//        Image temp = PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("more2.png"));
//        ImageIcon icon = new ImageIcon(temp);
//        moreButton.setBorder(null);
//        moreButton.setIcon(icon);
//
//        temp = PIMPool.pool.getImage(PaneConsts.IAMGE_PATH.concat("less2.png"));
//        icon = new ImageIcon(temp);
//        lessButton.setBorder(null);
//        lessButton.setIcon(icon);
//
//        // border----------
//        table.setBorder(null);
//        table.setShowGrid(false);
        // forcus-------------
        table.setFocusable(false);

        // built
        if(billButton != null) {
//        	add(billButton);
        	billButton.addActionListener(this);
        }else {
//            add(moreButton);
//            add(lessButton);
//            add(lblDiscount);
//            add(lblServiceFee);
//            add(lblSubTotle);
//            add(lblTPS);
//            add(lblTVQ);
        }
//        add(valTotlePrice);
//        add(scrContent);
//
//        addComponentListener(this);
        moreButton.setOnClickListener(this);
        lessButton.setOnClickListener(this);
//        table.addMouseMotionListener(this);
//        table.addMouseListener(this);
        table.adapter.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				valueChanged(position);
			}
		});
//        scrContent.getViewport().addMouseListener(this);
		reLayout();
    }

    public int getBillID() {
		return billID;
	}

	public void setBillID(int billID) {
		this.billID = billID;
	}

    public String getZippedCreateTime(){
    	String zippedStr = createdTime.replace(" ", "");
    	zippedStr = zippedStr.replace("-", "");
    	zippedStr = zippedStr.replace(":", "");
    	return zippedStr;
    }

	@XViewByID(R.id.headerName)
	private TextView headerName;
	@XViewByID(R.id.headerQty)
	private TextView headerQty;
	@XViewByID(R.id.headerModify)
	private TextView headerModify;
	@XViewByID(R.id.headerPrice)
	private TextView headerPrice;

	@XViewByID(R.id.tableListView)
	public PIMTable table;
	public MyAdapter myAdapter;
	@XViewByID(R.id.lblSubTotle)
    public TextView lblSubTotle;
	@XViewByID(R.id.lblTPS)
    public TextView lblTPS;
	@XViewByID(R.id.lblTVQ)
    public TextView lblTVQ;
    @XViewByID(R.id.lblDiscount)
	public TextView lblDiscount;
	@XViewByID(R.id.lblServiceFee)
    public TextView lblServiceFee;
	@XViewByID(R.id.valTotlePrice)
    public TextView valTotlePrice;
	@XViewByID(R.id.moreButton)
    private Button moreButton;
	@XViewByID(R.id.lessButton)
    private Button lessButton;

	public String[] header;

	@XViewByID(R.id.billButton)
    private ISButton sourceBtn;

	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public ArrayList<Dish> getOrderedDishAry() {
		return orderedDishAry;
	}
	public void setOrderedDishAry(ArrayList<Dish> orderedDishAry) {
		this.orderedDishAry = orderedDishAry;
	}
	public List<Dish> getRemovedDishAry() {
		//if it's not partial refund, then for now, must be changing the payment action,
		//(when changing paymen method, will secretly send a refund all transaction to web mev.
		if(mapForPrintedItemRemoving.isEmpty()) {
			return orderedDishAry;
		}

		ArrayList<Dish> refundedDishAry =  new ArrayList<>();
		for(Dish dish : mapForPrintedItemRemoving.values()) {
			refundedDishAry.add(dish);
		}
		return refundedDishAry;
	}

	public interface OnItemClickListener {
		void onItemClick(int position);
	}
}
