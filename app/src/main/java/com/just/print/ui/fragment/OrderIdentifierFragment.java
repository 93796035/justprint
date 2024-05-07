package com.just.print.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.BarOption;
import com.just.print.app.AikaStyleApplication;
import com.just.print.app.BaseFragment;
import com.just.print.app.EventBus;
import com.just.print.db.bean.Bill;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.M2M_SaleRecordMark;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.BillDao;
import com.just.print.db.dao.MarkDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.UC;
import com.just.print.ui.activity.ConfigFragmentActivity;
import com.just.print.ui.holder.ConfigBillViewHolder;
import com.just.print.ui.holder.ConfigOrderViewHolder;
import com.just.print.ui.holder.OrderCategoryButtonFullWidthViewHolder;
import com.just.print.ui.holder.OrderMarksSelectionViewHolder;
import com.just.print.ui.holder.OrderIdentifierItemViewHolder;
import com.just.print.ui.holder.OrderIdentifierMarkViewHolder;
import com.just.print.ui.holder.OrderMenuViewHolder;
import com.just.print.ui.holder.TableIdentifierViewHolder;
import com.just.print.ui.holder.OrderCategoryButtonNatureWidthViewHolder;
import com.just.print.util.L;
import com.just.print.util.StringUtils;
import com.stupid.method.adapter.IXOnItemClickListener;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class OrderIdentifierFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener, OnClickItemListener, EventBus.EventHandler{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String EVENT_ADD_MENU = "EVENT_ADD_MENU=";
    private static final String TAG = "OrderIdentifierFragment";

    private static OrderIdentifierFragment instance = null;
    private static boolean isCancel;
    private List<Mark> allMarks;
    private List<Mark> marksOfCurDish;
    private String reportContent;
    private XAdapter2<Bill> billsXAdapter;
    private BillDao billDao = AikaStyleApplication.instance.getDaoMaster().newSession().getBillDao();

    public static OrderIdentifierFragment getInstance(){
        if(instance == null){
            instance = new OrderIdentifierFragment();
        }
        return instance;
    }

    @XViewByID(R.id.odInfoInputed)
    EditText odInfoInputed;

    @XViewByID(R.id.odIdInput)
    private TextView odIdInput;

    @XViewByID(R.id.odIdfrName)
    private TextView odIdfrName;

    //two switchers---------------------------
    @XViewByID(R.id.menuMarkSwitcher)
    private ViewSwitcher menuMarkSwitcher;

    @XViewByID(R.id.menuTableSwitcher)
    private ViewSwitcher menuTableSwitcher;

    //for selecting menu operations--------------
    @XViewByID(R.id.categoryArea)
    private LinearLayout categoryArea;

    @XViewByID(R.id.odIdCategoryGrid)
    private GridView odIdCategoryGrid;
    @XViewByID(R.id.categoryFlowList)
    FlowListView categoryFlowList;

    @XViewByID(R.id.odMenuGv)
    private GridView odMenuGv;

    @XViewByID(R.id.odIdShortCutMarksGrid)
    private GridView odIdShortCutMarksGrid;

    @XViewByID(R.id.odIdFrLoutMenuList)
    private ListView odIdFrLoutMenuList;

    //buttons on top area in mainDialog----------------
    @XViewByID(R.id.odIdTableBtn1)
    ToggleButton odIdTable1;

    //buttons on bottom area in mainDialog----------------
    @XViewByID(R.id.odIdTable2)
    private static ToggleButton odIdTable2;

    @XViewByID(R.id.odIdCancelBtn2)
    private static ImageButton odIdCancel2;

    @XViewByID(R.id.odIdConfigBtn2)
    private ImageButton odIdConfigBtn2;

    @XViewByID(R.id.odIdSndBtn2)
    private ImageButton odIdSndBtn2;

    //elements on for selecting bill and selecting table---------------
    @XViewByID(R.id.odTableGrid)
    private GridView odTableGrid;

    @XViewByID(R.id.allMarkList)
    GridView markAllList;

    @XViewByID(R.id.billList)
    FlowListView billListView;

    @XViewByID(R.id.billIdx)
    TextView tvBillIdx;
    @XViewByID(R.id.billIdx2)
    TextView tvBillIdx2;
    @XViewByID(R.id.table)
    TextView tvTable;
    @XViewByID(R.id.table2)
    TextView tvTable2;
    @XViewByID(R.id.time)
    TextView tvOpenTime;
    @XViewByID(R.id.openTime)
    TextView tvOpenTime2;

    @XViewByID(R.id.subtotal)
    TextView tvSubtoal;
    @XViewByID(R.id.subtotal2)
    TextView tvSubtoal2;
    @XViewByID(R.id.total)
    TextView tvTotal;
    @XViewByID(R.id.total2)
    TextView tvTotal2;
    @XViewByID(R.id.cancelShowingBill)
    Button cancelShowingBill;
    @XViewByID(R.id.OKCloseTable)
    Button OKCloseTable;
    @XViewByID(R.id.orderList)
    ListView ordersView;
    @XViewByID(R.id.orderList2)
    ListView ordersView2;
    //inputted table name-------------------
    @XViewByID(R.id.tableName)
    private EditText etTableName;
    @XViewByID(R.id.temporallyAddedLabel)
    private EditText temporallyAddedLabel;
    private Dish storedMenu;

    XAdapter2<Mark> markShorCutXAdapter;
    XAdapter2<SelectionDetail> dishesXAdapter;
    XAdapter2<String> categoryXAdapter;
    XAdapter2<String> categoryXAdapter2;
    XAdapter2<String> itemXAdapter;
    private static XAdapter2<Mark> tableXAdapter;
    XAdapter2<Mark> markAllXAdapter;

    private List<Mark> shortCutMarks;

    public static List<SelectionDetail> bkOfLastSelection;
//    private static CharSequence bkOfLastTable;
    static int times = 0;
    String[] categoryNames = {""};
    String[] items;// = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "H", "S", "U", "+", "togo", "canc"};

    public static List<Mark> tables = new ArrayList<Mark>();
    boolean allowNoTableSelected = "1".equals(BarOption.getCustomData(BarOption.allowNoTableSelected))
            || "true".equals(BarOption.getCustomData(BarOption.allowNoTableSelected));
    private String billIdToMregeTo;
    @Override
    public void onCreated(Bundle savedInstanceState) {
        getEventBus().register(EVENT_ADD_MENU, this);
        new StupidReflect(this, getView()).init();

        storedMenu = null;        //the model of the menus.

        findViewById(R.id.tableSelectionView).setBackgroundColor(BarOption.getThemColor());
        tableXAdapter = new XAdapter2<Mark>(getContext(), TableIdentifierViewHolder.class);
        tableXAdapter.setClickItemListener(this.tableXAdapterClick);
        loadTables();
        tableXAdapter.setData(tables);
        odTableGrid.setAdapter(tableXAdapter);

        if(BarOption.isMode2()) {
            //prepare the categoryNames and items.
            List<Category> categories = OrderCategoryFragment.getCategoryList();
            categoryNames = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                categoryNames[i] = categories.get(i).getName1();
            }

            List<Dish> menus;
            findViewById(R.id.odIdInfo).setVisibility(View.GONE);
            findViewById(R.id.topButtons).setVisibility(View.GONE);
            findViewById(R.id.odIdTableBtn1).setVisibility(View.GONE);
            findViewById(R.id.odIdCancelBtn1).setVisibility(View.GONE);
            findViewById(R.id.odIdConfigBtn1).setVisibility(View.GONE);
            findViewById(R.id.odIdSndBtn0).setVisibility(View.GONE);
            odIdCategoryGrid.setBackgroundColor(BarOption.getThemColor());
            findViewById(R.id.categoryFlowListPane).setBackgroundColor(BarOption.getThemColor());
            findViewById(R.id.categoryArea).setBackgroundColor(BarOption.getThemColor());
            findViewById(R.id.billSelectButtonArea).setBackgroundColor(BarOption.getThemColor());
            odIdCancel2.setOnLongClickListener(this);
            odIdSndBtn2.setOnLongClickListener(this);

            if("flow".equals(BarOption.getCategoryLayoutStyle())){
                odIdCategoryGrid.setVisibility(View.GONE);
            }else{
                categoryFlowList.setVisibility(View.GONE);
            }
            findViewById(R.id.odIdTools).setBackgroundColor(BarOption.getThemColor());

            if(categories.size() > 1) {
                ((GridView)odIdCategoryGrid).setNumColumns(categories.size());
                menus = OrderCategoryFragment.getCategorizedContent().get(categories.get(0));
            }else{
                //odIdCategoryGrid.setVisibility(View.GONE);
                if(categories.size() == 0){
                    menus = new ArrayList<Dish>();
                }else {
                    menus = OrderCategoryFragment.getCategorizedContent().get(categories.get(0));
                }
            }

            //might need more col for menu.
            String col = BarOption.getCustomData(BarOption.column);
            if("".equals(col)){
                col = "1";
            }
            try {
                ((GridView) findViewById(R.id.odMenuGv)).setNumColumns(Integer.valueOf(col));
                int tableViewColumn = 4;
                try{
                    tableViewColumn = Integer.valueOf(BarOption.getCustomData(BarOption.tblcol));
                }catch(Exception e){}
                ((GridView) findViewById(R.id.odTableGrid)).setNumColumns(tableViewColumn);
            }catch(Exception e){
                //ignore, it's normal user didn't change the setting. then leave it to be 2.
            }

            //get the menu for current category.
            items = new String[menus.size()];
            for (int i = 0; i < menus.size(); i++) {
                items[i] = menus.get(i).getId().toString();
            }
            if(tables.size() > 0) {
                odIdTable2.setText("T");
            }else{
                odIdTable2.setText(BarOption.getCustomData(BarOption.billIdx));
            }

            if("1".equals(BarOption.getCustomData(BarOption.clientMode)) || "true".equals(BarOption.getCustomData(BarOption.clientMode))){
                odIdTable2.setVisibility(View.GONE);
                odIdCancel2.setVisibility(View.GONE);
                odIdConfigBtn2.setVisibility(View.GONE);
            }

            if("1".equals(BarOption.getCustomData(BarOption.HideTable)) || "true".equals(BarOption.getCustomData(BarOption.HideTable))){
                odIdTable2.setVisibility(View.GONE);
            }
            if("1".equals(BarOption.getCustomData(BarOption.HideRubber)) || "true".equals(BarOption.getCustomData(BarOption.HideRubber))){
                odIdCancel2.setVisibility(View.GONE);
            }
            if("1".equals(BarOption.getCustomData(BarOption.HideSetting)) || "true".equals(BarOption.getCustomData(BarOption.HideSetting))){
                odIdConfigBtn2.setVisibility(View.GONE);
            }
        }else{
            odIdCategoryGrid.setVisibility(View.GONE);
            categoryFlowList.setVisibility(View.GONE);
            findViewById(R.id.odIdTools).setVisibility(View.GONE);
            //the "must have" buttons
            List<String> ary = new ArrayList<String>();
            for (int i = 1; i <= 9; i++) {
                ary.add(String.valueOf(i));
            }
            ary.add(String.valueOf(0));

            //add the custChars, if not defined, then add an s and a u.
            String custChars = BarOption.getCustomChars();
            for (int i = 0; i < custChars.length(); i++) {
                ary.add(custChars.substring(i, i + 1));
            }

            //the table shortcut including dordash/skip/uber... are difined by custTogos
            String custTogos = BarOption.getCustomTogos();
            String[] lables = custTogos.split(",");
            for (String label : lables) {
                ary.add(label.trim());
            }
            //cancel button must be put as the last element.
            ary.add("Canc");

            items = new String[ary.size()];
            items = ary.toArray(items);
            View btnSend = findViewById(R.id.odIdSndBtn0);
            btnSend.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BarOption.getSendButtonHeight()));
        }

        categoryXAdapter = new XAdapter2<String>(getActivity(), Arrays.asList(categoryNames), OrderCategoryButtonFullWidthViewHolder.class);
        categoryXAdapter.setClickItemListener(this.categoryXAdapterClick);
        odIdCategoryGrid.setAdapter(categoryXAdapter);

        categoryXAdapter2 = new XAdapter2<String>(getActivity(), Arrays.asList(categoryNames), OrderCategoryButtonNatureWidthViewHolder.class);
        categoryXAdapter2.setClickItemListener(this.categoryXAdapterClick2);
        categoryFlowList.setAdapter(categoryXAdapter2);

        itemXAdapter = new XAdapter2<String>(getActivity(), Arrays.asList(items), OrderIdentifierItemViewHolder.class);
        itemXAdapter.setClickItemListener(this.itemXAdapterClick);
        odMenuGv.setAdapter(itemXAdapter);
//        odMenuGv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        odIdTable1.setTextOn(null);
        odIdTable1.setTextOff(null);
        odIdTable1.setText("");
        odIdTable1.setChecked(true);
        odIdTable1.setOnClickListener(this);

        if(tables.size() > 0) {
            odIdTable2.setOnClickListener(this);
        }

        //initSelected dishes part.
        dishesXAdapter = new XAdapter2<SelectionDetail>(getContext(), OrderMenuViewHolder.class);
        dishesXAdapter.setClickItemListener(this);
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        odIdFrLoutMenuList.setAdapter(dishesXAdapter);

        //add Marks
        allMarks = sortMarks(DaoExpand.queryAllMarks(AikaStyleApplication.getMarkDao()));//deleted is used for table name....
        //add shortcut Marks.
        initShortCutMarks();
        if("1".equalsIgnoreCase(BarOption.getCustomData(BarOption.allowTemporaryLabel)) || "true".equalsIgnoreCase(BarOption.getCustomData(BarOption.allowTemporaryLabel))) {
            findViewById(R.id.temporaryLabelArea).setVisibility(View.VISIBLE);
        }

        //init MarkSelection panel
        markAllXAdapter = new XAdapter2<Mark>(getContext(), OrderMarksSelectionViewHolder.class);
        markAllXAdapter.setClickItemListener(this);
        markAllXAdapter.setData(allMarks);
        markAllList.setAdapter(markAllXAdapter);
        markAllList.setNumColumns(BarOption.getMarkNumColumn());

        if("1".equalsIgnoreCase(BarOption.getCustomData(BarOption.clientMode)) || "true".equalsIgnoreCase(BarOption.getCustomData(BarOption.clientMode))){
            findViewById(R.id.mainDialog).setVisibility(View.GONE);
        }else{
            findViewById(R.id.odInfoCollect).setVisibility(View.GONE);
        }

        //init the existing bills area.
        billsXAdapter = new XAdapter2<Bill>(getContext(), ConfigBillViewHolder.class);
        billsXAdapter.setClickItemListener(this);
        billListView.setAdapter(billsXAdapter);
        if(BarOption.getStartFromTableSelection() && tables.size() > 0) {
            menuTableSwitcher.setDisplayedChild(1);
        }
    }

    @XClick({R.id.odInfoOK})
    private void onConfirmUserName(View view) {
        String inputContent = odInfoInputed.getText().toString().trim();
        if (StringUtils.isEmpty(inputContent) || inputContent.length() < 1) {
            showToast("Please input a name:");
        } else {
            String userPassword = BarOption.getCustomData(BarOption.userPassword);
            if(userPassword == null || userPassword.length() == 0){
                userPassword = "88882288";
            }

            if(userPassword.equals(inputContent)){
                startActivity(new Intent(getContext(), ConfigFragmentActivity.class));
            }else {
                hideInputMethod(getActivity());
                findViewById(R.id.odInfoCollect).setVisibility(View.INVISIBLE);
                findViewById(R.id.mainDialog).setVisibility(View.VISIBLE);
                CustomerSelection.getInstance().setTableName(inputContent);
                odInfoInputed.setText("");
            }
        }
    }
    public void hideInputMethod(Activity activity){
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        InputMethodManager imm = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadTables() {
        tables = DaoExpand.queryAllTables(AikaStyleApplication.instance.getDaoMaster().newSession().getMarkDao());
        Collections.sort(tables, new Comparator<Mark>() {
            @Override
            public int compare(Mark mark, Mark t1) {
                return mark.getStatus() - t1.getStatus();
            }
        });
        tableXAdapter.setData(tables);
    }

    boolean longClickFlag = false;
    @Override
    public boolean onLongClick(View view) {
        List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
        //check if dish selected.
        if (selectionDetails == null || selectionDetails.size() == 0) {
            showToast("Nothing selected!");
            this.billIdToMregeTo = null;
            return false;
        }

        //init the existing bills area.
        List<Bill> orders = new ArrayList<>();
        try{
            String recentNum = BarOption.getCustomData(BarOption.recentBillAmount);
            orders = DaoExpand.queryRecentBill(billDao, StringUtils.isBlank(recentNum) ? 20 : Integer.valueOf(recentNum));
        }catch(Exception e){
            L.e("orders", "exception when fetching sale orders", e);
            //means the bill table not existing. for old version there are no bill table yet, so when update them into new version, and downloaded the old database, then will have this issue.
        }
        billsXAdapter.setData(orders);

        switch (view.getId()) {
            case R.id.odIdSndBtn2:
                //check if table selected.
                if (!allowNoTableSelected && tables.size() > 0) {
                    String curTable = odIdTable2.getText().toString();
                    if (curTable.length() == 0 || "T".equals(curTable)) {
                        loadTables();
                        menuTableSwitcher.setDisplayedChild(1);
                        return false;
                    }
                }
                longClickFlag = true;   //set the flag, because the exeControlCommand emthod will also be triggered when long click happenned.
                // in the action of ok button of recentOrders view, the printCurrentSelection method will be called.
                findViewById(R.id.mainDialog).setVisibility(View.INVISIBLE);
                findViewById(R.id.recentOrders).setVisibility(View.VISIBLE);
                break;
            case R.id.odIdCancelBtn2:
                longClickFlag = true;
                isCancel = true;
                findViewById(R.id.mainDialog).setVisibility(View.INVISIBLE);
                findViewById(R.id.recentOrders).setVisibility(View.VISIBLE);
                break;
        }
        return false;
    }

    @XClick({R.id.OKSelectBillId, R.id.cancelSelectBillId,
             R.id.odIdCancelBtn1, R.id.odIdCancelBtn2, R.id.odIdConfigBtn1, R.id.odIdConfigBtn2, R.id.odIdSndBtn0, R.id.odIdSndBtn1, R.id.odIdSndBtn2,
            R.id.cancelShowingBill, R.id.OKCloseTable})
    private void exeControlCommand(View v) {
        if(longClickFlag){
            longClickFlag = false;
            return;
        }
        List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
        switch (v.getId()) {
            case R.id.odIdConfigBtn1:
            case R.id.odIdConfigBtn2:
                startActivity(new Intent(getContext(), ConfigFragmentActivity.class));
                break;
            case R.id.odIdCancelBtn1:
                if( menuMarkSwitcher.getDisplayedChild() == 0) {
                    DelOneText();
                }else{
                    categoryArea.setVisibility(View.VISIBLE);
                    menuMarkSwitcher.setDisplayedChild(0);
                }
                break;
            case R.id.odIdCancelBtn2:
                //check if dish selected.
                if(selectionDetails == null || selectionDetails.size() == 0){
                    showToast("Nothing selected!");
                    this.billIdToMregeTo = null;
                    return;
                }
                printCurrentSelection(billIdToMregeTo,true);
                break;
            case R.id.odIdSndBtn0:
            case R.id.odIdSndBtn2:
                //check if dish selected.
                if(selectionDetails == null || selectionDetails.size() == 0){
                    showToast("Nothing selected!");
                    this.billIdToMregeTo = null;
                    return;
                }
                //check if table not selected.
                if(!allowNoTableSelected && tables.size() > 0){
                    String curTable = (v.getId() == R.id.odIdSndBtn0 ? odIdTable1 : odIdTable2).getText().toString();
                    if(curTable.length() == 0 || "T".equals(curTable)){
                        loadTables();
                        menuTableSwitcher.setDisplayedChild(1);
                        return;
                    }
                    //so...table selected...update the table status.
                    String aotoCloseTables = BarOption.getCustomData(BarOption.autoCloseTable);
                    if(!"".equalsIgnoreCase(aotoCloseTables)
                        && !"all".equalsIgnoreCase(aotoCloseTables)
                        && !aotoCloseTables.contains(curTable)) {
                        MarkDao markDao = AikaStyleApplication.instance.getDaoMaster().newSession().getMarkDao();
                        List<Mark> tables = markDao.loadAll();
                        for (Mark table : tables) {
                            if (curTable.equals(table.getName1())) {
                                table.setVersion(Integer.valueOf(BarOption.getCurrentBillIdx()) * -1);
                                markDao.insertOrReplace(table);
                                break;
                            }
                        }
                    }
                }
                String orderContent = printCurrentSelection(billIdToMregeTo, false);
                if("1".equalsIgnoreCase(BarOption.getCustomData(BarOption.emailorder)) || "true".equalsIgnoreCase(BarOption.getCustomData(BarOption.emailorder))){
                    BarOption.notifyCheck(orderContent);
                }
                showToast("ORDER SENT, THANK YOU.");
                if("1".equalsIgnoreCase(BarOption.getCustomData(BarOption.clientMode)) || "true".equalsIgnoreCase(BarOption.getCustomData(BarOption.clientMode))){
                    findViewById(R.id.mainDialog).setVisibility(View.INVISIBLE); //it's better to be run after 10 seconds
                    findViewById(R.id.odInfoCollect).setVisibility(View.VISIBLE);
                }else if(BarOption.getStartFromTableSelection() && tables.size() > 0){
                    loadTables();
                    menuTableSwitcher.setDisplayedChild(1);
                }
                break;
            case R.id.odIdSndBtn1:
                if(BarOption.isMode2()){
                    findViewById(R.id.topButtons).setVisibility(View.GONE);
                }
                String adminPassword = BarOption.getAdminPassword();
                if(adminPassword.equals(odIdTable1.getText().toString())
                        || adminPassword.equals(odIdInput.getText().toString())){
                    //print report
                    HashSet billsNotCount = new HashSet();
                    for(Mark table : OrderIdentifierFragment.tables){
                        if(table.getVersion() != -1){
                            billsNotCount.add(String.valueOf(table.getVersion() * -1));
                            showToast("Unclosed table will not be calculated into report!");
                        }
                    }
                    List<SaleRecord> orders = AikaStyleApplication.instance.getDaoMaster().newSession().getSaleRecordDao().loadAll();
                    for(int i = orders.size() - 1; i >= 0; i--){
                        if(billsNotCount.contains(orders.get(i).getBillId())){
                            orders.remove(i);
                        }
                    }

                    if(orders == null || orders.size() == 0){
                        showToast("No report to print! The sales record has been cleaned!");
                    }else {
                        //print code:
                        reportContent = WifiPrintService.getInstance().exePrintReportCommand(billsNotCount, orders, String.valueOf(new Date().getTime()));
                        findViewById(R.id.menuMarkSwitcher).setVisibility(View.INVISIBLE);
                        findViewById(R.id.topButtons).setVisibility(View.INVISIBLE);
                        findViewById(R.id.alertResetDlg).setVisibility(View.VISIBLE);
                    }
                    odIdTable1.setText("");
                    odIdInput.setText("");
                }else if( menuMarkSwitcher.getDisplayedChild() == 0) {
                    if (odIdTable1.isChecked()) {    //waiting for input table num status. always allowed!
                        odIdTable1.setChecked(false);
                        CustomerSelection.getInstance().setTableName(odIdTable1.getText().toString());
                    } else {                            //inputting dishes status. need to check print status.
//this is marked off because we changed to allow user to add new order, just when user try to send will do a check and pump to ask if will go on.
//                        if (bkOfLastSelection != null) {      //categorizedContent not all send to printer yet.
//                            //if last order has only one dish and it happened to be same
//                            //as the one selected this time, waiter will think the roll back is not happenning.
//                            //we decided to not display last order--- rollbackLastOrder();
//                            ToastUtil.showToast("Printing...Please wait.");
//                            return;
//                        } else
                        if (storedMenu != null) {     //last order send to printer well.
                            addNewSelection(storedMenu);
                            odIdInput.setText("");
                            odIdfrName.setText("");
                            if (dishesXAdapter != null) {
                                loadOrderMenu();
                            }
                        }
                    }
                }else{  //the button is working for label selection interface.
                    updateSelectionOfCurrentDish();
                }
                break;
            case R.id.cancelSelectBillId:
                billIdToMregeTo = null;
                isCancel = false;
                findViewById(R.id.mainDialog).setVisibility(View.VISIBLE); //it's better to be run after 10 seconds
                findViewById(R.id.recentOrders).setVisibility(View.INVISIBLE);
                break;
            case R.id.OKSelectBillId:
                //in case the billIdx is not a number, report error, and do nothing.
                try {
                    billIdToMregeTo = tvBillIdx.getText().toString().substring(UC.ORDER_NUMBER.length());
                }catch (Exception e){
                    L.e("OrderIdentifierFragment",
                            "Unexcepted id in label:" + tvBillIdx.getText().toString().substring("    Will append new selection to bill: ".length()), e);
                    billIdToMregeTo = null;
                    isCancel = false;
                    return;
                }

                CustomerSelection.getInstance().setTableName(tvTable.getText().toString().substring(UC.TABLE.length()));
                String orderContent2 = printCurrentSelection(billIdToMregeTo, isCancel);
                if("1".equalsIgnoreCase(BarOption.getCustomData(BarOption.emailorder)) || "true".equalsIgnoreCase(BarOption.getCustomData(BarOption.emailorder))){
                    BarOption.notifyCheck(orderContent2);
                }
                showToast("ORDER SENT, THANK YOU.");
                if("1".equalsIgnoreCase(BarOption.getCustomData(BarOption.clientMode)) || "true".equalsIgnoreCase(BarOption.getCustomData(BarOption.clientMode))){
                    findViewById(R.id.mainDialog).setVisibility(View.INVISIBLE); //it's better to be run after 10 seconds
                    findViewById(R.id.odInfoCollect).setVisibility(View.VISIBLE);
                }
                findViewById(R.id.mainDialog).setVisibility(View.VISIBLE); //it's better to be run after 10 seconds
                findViewById(R.id.recentOrders).setVisibility(View.INVISIBLE);

                isCancel = false;
                break;
            case R.id.cancelShowingBill:
                findViewById(R.id.billOnTable).setVisibility(View.GONE);
                menuTableSwitcher.setDisplayedChild(0);
                break;
            case R.id.OKCloseTable:
                String tableName = tvTable2.getText().toString().substring(UC.TABLE.length());
                //update the table status to be -1 means no bill on it, the table closed..
                MarkDao markDao = AikaStyleApplication.instance.getDaoMaster().newSession().getMarkDao();
                List<Mark> tables = markDao.loadAll();
                for (Mark table : tables) {
                    if(tableName.equals(table.getName1())){
                        table.setVersion(-1);
                        markDao.insertOrReplace(table);
                        loadTables();
                        break;
                    }
                }
                findViewById(R.id.billOnTable).setVisibility(View.GONE);
                break;
//            case R.id.odMarkSelectionCancel:
//                categoryArea.setVisibility(View.VISIBLE);
//                menuMarkSwitcher.setDisplayedChild(0);
//                break;
//
//            case R.id.odMarkSelectionOK:
//                updateSelectionOfCurrentDish();
//                break;
        }

    }

    @XClick({R.id.buttonCancel})
    private void notResetReport(){
        findViewById(R.id.alertResetDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertResetDlg).setMinimumHeight(0);
        findViewById(R.id.topButtons).setY(0);
        findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        findViewById(R.id.menuMarkSwitcher).setVisibility(View.VISIBLE);
        findViewById(R.id.topButtons).invalidate();
        //check if need to send email
        try{
            int reportIdx = 1;
            reportIdx = Integer.valueOf(BarOption.getCustomData(BarOption.reportIdx));
            BarOption.notifyCheck(reportIdx, reportContent, false);
        }catch(Exception e){
            //It's OK if reportIdx is null
        }
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void resetReport(){
        int reportIdx = 1;
        //check if need to send email
        try{
            reportIdx = Integer.valueOf(BarOption.getCustomData(BarOption.reportIdx));
        }catch(Exception e){
            //It's OK if reportIdx is null
        }
        if(!BarOption.notifyCheck(reportIdx, reportContent, true)){
            return;
        }

        //when printed succcesfully, clean all records, and update now as the next reportStartDate
        AikaStyleApplication.instance.getDaoMaster().newSession().getSaleRecordDao().deleteAll();
        AikaStyleApplication.instance.getDaoMaster().newSession().getBillDao().deleteAll();
        BarOption.putCustomData(BarOption.reportStartDate, String.valueOf(new Date().getTime()));
        BarOption.putCustomData(BarOption.billIdx, "1");        //reset kitchenbillIndex

        BarOption.putCustomData(BarOption.reportIdx, String.valueOf(reportIdx + 1));

        findViewById(R.id.alertResetDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertResetDlg).setMinimumHeight(0);
        findViewById(R.id.topButtons).setY(0);
        findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        findViewById(R.id.menuMarkSwitcher).setVisibility(View.VISIBLE);
        findViewById(R.id.topButtons).invalidate();

    }

    private void updateSelectionOfCurrentDish() {
        for(int i = 0; i < allMarks.size(); i++){
            Mark mark = allMarks.get(i);
            boolean matched = false;
            for(int j = 0; j < marksOfCurDish.size(); j++){
                if(marksOfCurDish.get(j).getName1().equals(mark.getName1())){
                    if(mark.getQt() == 0){
                        marksOfCurDish.remove(j);
                    }else {
                        marksOfCurDish.get(j).setQt(mark.getQt());
                    }
                    matched = true;
                    break;
                }
            }
            if(!matched && mark.getQt() > 0){
                marksOfCurDish.add(mark.clone());
            }
        }

        String temperalLabel = temporallyAddedLabel.getText().toString();
        if(temperalLabel.length() > 0){
            Mark temporaryMark = new Mark();
            int p = temperalLabel.indexOf("$");
            if(p > 0){
                String priceStr = temperalLabel.substring(p + 1).trim();
                temporaryMark.setVersion((int)(Float.valueOf(priceStr) * 100));
                temperalLabel = temperalLabel.substring(0, p);
            }else{
                temporaryMark.setVersion(0);
            }

            p = temperalLabel.indexOf( "*");
            if(p > 0){
                String QT = temperalLabel.substring(p + 1).trim();
                temporaryMark.setQt(Integer.valueOf(QT));
                temperalLabel = temperalLabel.substring(0, p);
            }else{
                temporaryMark.setQt(1);
            }
            temporaryMark.setName1(temperalLabel);
            marksOfCurDish.add(temporaryMark);
            temporallyAddedLabel.setText("");
        }
        categoryArea.setVisibility(View.VISIBLE);
        menuMarkSwitcher.setDisplayedChild(0);
        loadOrderMenu();
    }

    private String printCurrentSelection(final String billToMregeTo, final boolean isCancel) {
        final String result = WifiPrintService.getInstance().saveAndSyncCommand(billToMregeTo, isCancel);
        if (result.startsWith("192.") || result.startsWith("10.")) {
            // pup up a dialog to ask if user want to continue.....
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Last print on " + result + " not completed. Continue? ");
            builder.setTitle("WARNING").setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //do we need to clean bkSelections?????
                    confirmPrintOK();
                    //clean the printer maps, to make sure it can pass the check when call itself again to send content to printer.
                    WifiPrintService.getInstance().addProblematicPrinter(result);
                    WifiPrintService.getInstance().reInitPrintRelatedMaps();
                    WifiPrintService.getInstance().resetFlags();
                    printCurrentSelection(billToMregeTo, isCancel);
                }
            });
            builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            this.billIdToMregeTo = null;
            return "";
        } else {
            this.billIdToMregeTo = null;
            backupAndClearOrderMenu();
            return result;
        }
    }

    //currently it's used by number buttons and the note tag buttons. both types are buttons in holder.
    IXOnItemClickListener itemXAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            L.d(TAG, "onClickItem");
            Dish tmpMenu;
            L.d(TAG, String.valueOf(view.getId()) + String.valueOf(i));
            switch (view.getId()) {
                case R.id.buttonholder: //number buttons.
                    if (BarOption.isMode2()) {
                        tmpMenu = SearchMenuFromDB(items[i]);
                        SelectionDetail newSelection = addNewSelection(tmpMenu);
                        if (dishesXAdapter != null) {
                            loadOrderMenu();
                        }
                        //if it's defined to be show up lable(mark) panel
                        if(BarOption.getForcePopUpList().contains(items[i])) {
                            marksOfCurDish = newSelection.getMarkList();
                            showMarksPanel(newSelection.getDish());
                        }

                    } else {
                        if (i < 10) {
                            appendText(Integer.toString((i + 1) % 10));
                        } else if (i == items.length - 1) {
                            odIdTable1.setText("");
                            //added the selected dish with negative price. (return a non-null string means print success.
                            if (printCurrentSelection(billIdToMregeTo, true).length() > 0) {    //arranged to print---didn't met the case that previous print not finished yet.
                                isCancel = true;
                            }
                        } else if (i < 10 + BarOption.getCustomChars().length()) {         //following are fixed buttons.
                            appendText(items[i]);
                        } else {
                            odIdTable1.setText(items[i]);
                            CustomerSelection.getInstance().setTableName(odIdTable1.getText().toString());
                        }

                        if (!odIdTable1.isChecked()) {
                            tmpMenu = SearchMenuFromDB(odIdInput.getText().toString());
                            if (null != tmpMenu) {
                                odIdfrName.setText(tmpMenu.getName1());
                            } else {
                                odIdfrName.setText("");
                            }
                        }
                    }
                    break;
                case R.id.tagid:            //not tag buttons.
                    L.d(TAG, "case tag");
                    List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
                    //Mark Press
                    if (selectionDetails != null && selectionDetails.size() > 0) {
                        Mark selectedMark = markShorCutXAdapter.getItem(i);
                        L.d(TAG, String.valueOf(selectedMark.select));
                        List<Mark> marks = selectionDetails.get(selectionDetails.size() - 1).getMarkList();
                        boolean matched = false;
                        for(int m = 0; m < marks.size(); m++){
                            Mark mark = marks.get(m);
                            if(mark.getName1().equals(selectedMark.getName1())){
                                mark.setQt(mark.getQt() + 1);
                                matched = true;
                                break;
                            }
                        }
                        if(!matched){
                            Mark m = selectedMark.clone();
                            m.setQt(1);
                            marks.add(m);
                        }
                        loadOrderMenu();
                    }
                    break;
//                case R.id.tableID:
//                    odIdTable2.setText("BLAH");
//                    break;
            }

            //odIdfrName.setText(menuXAdapter.get(i).getName1());
        }
    };

    IXOnItemClickListener tableXAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int idx) {
            L.d(TAG, "onClickTable" + view.getId() + String.valueOf(idx));
            Mark selectedTable = OrderIdentifierFragment.tables.get(idx);
            String tableName = selectedTable.getName1();
            if("桌".equals(tableName) || "号".equals(tableName) ||  "Table".equalsIgnoreCase(tableName.trim()) || "Number".equalsIgnoreCase(tableName)){
                findViewById(R.id.mainDialog).setVisibility(View.INVISIBLE);
                findViewById(R.id.inputTableName).setVisibility(View.VISIBLE);
            }else {
                odIdTable2.setText(tableName);
                odIdTable1.setText(tableName);
            }
            //check if there's a unclosed bill on this table.
            if(selectedTable.getVersion() == -1){ //negative means a mark is used as table, -1 means closed table
                CustomerSelection.getInstance().setTableName(tableName);// can not use it for now, because if input dlg poped up, this line will excuted before input message return back.odIdTable2.getText().toString());
                findViewById(R.id.billOnTable).setVisibility(View.GONE);
                menuTableSwitcher.setDisplayedChild(0);
            }else { //less than -1 means openned table, and the number is bill number.
                Bill bill = billDao.load(-1L * selectedTable.getVersion());
                showOrderDetail(bill);
            }
        }
    };

    @XClick({R.id.tableNameOK})
    private void confirmTableName(@XGetValueByView(fromId = R.id.tableName) String tableName) {

        odIdTable2.setText(tableName);
        CustomerSelection.getInstance().setTableName(tableName);
        //print code:
        etTableName.setText("");
        findViewById(R.id.inputTableName).setVisibility(View.INVISIBLE);
        findViewById(R.id.mainDialog).setVisibility(View.VISIBLE);
    }

    //currently it's used by number buttons and the note tag buttons. both types are buttons in holder.
    IXOnItemClickListener categoryXAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int idx) {
            L.d(TAG, "onClickCategory" + view.getId() + String.valueOf(idx));

            List<Dish> menus = OrderCategoryFragment.getCategorizedContent().get(OrderCategoryFragment.getCategoryList().get(idx));
            items = new String[menus.size()];
            for (int i = 0; i < menus.size(); i++) {
                items[i] = menus.get(i).getId().toString();
            }
            itemXAdapter.setData(Arrays.asList(items));
            for(int i = 0; i < categoryXAdapter.getCount(); i++) {
                odIdCategoryGrid.getChildAt(i).setBackground(null);
            }

            odIdCategoryGrid.getChildAt(idx).setBackgroundColor(BarOption.getThemColor());
        }
    };

    IXOnItemClickListener categoryXAdapterClick2 = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int idx) {
            L.d(TAG, "onClickCategory" + view.getId() + String.valueOf(idx));

            List<Dish> menus = OrderCategoryFragment.getCategorizedContent().get(OrderCategoryFragment.getCategoryList().get(idx));
            items = new String[menus.size()];
            for (int i = 0; i < menus.size(); i++) {
                items[i] = menus.get(i).getId().toString();
            }
            itemXAdapter.setData(Arrays.asList(items));
            for(int i = 0; i < categoryXAdapter.getCount(); i++) {
                categoryFlowList.getChildAt(i).setBackground(null);
            }

            categoryFlowList.getChildAt(idx).setBackgroundColor(BarOption.getThemColor());
        }
    };
    @Override
    protected int getLayoutId() {
        return R.layout.order_identifier_fragment;
    }

    @Override
    public void handleEvent(String eventName, Object... argument) {
        if (EVENT_ADD_MENU.equals(eventName)) {
            loadOrderMenu();
        }
    }

    @Override
    public void onClick(View v) {
        int num;
        switch (v.getId()) {
            case R.id.odIdTableBtn1:
                L.d(TAG, "ClickTableButton");
                if (odIdTable1.isChecked()) {
                    odIdTable1.setText("");
                }
                break;
            case R.id.odIdTable2:
                L.d(TAG, "ClickedTableButton");
                if(tables.size() > 0) {
                    if (menuMarkSwitcher.getDisplayedChild() == 0){
                        loadTables();
                        //hide unnecessary buttons.
                        menuTableSwitcher.setDisplayedChild(1);
                    }
                }
                break;
        }
    }

    private void appendText(String str) {
        if (odIdTable1.isChecked()) {
            odIdTable1.append(str);
        } else {
            odIdInput.append(str);
        }
    }

    private void DelOneText() {
        if (odIdTable1.isChecked()) {
            if (odIdTable1.length() > 0) {
                odIdTable1.setText(odIdTable1.getText().subSequence(0, odIdTable1.length() - 1));
            }
        } else {
            if (odIdInput.length() > 0) {
                odIdInput.setText(odIdInput.getText().subSequence(0, odIdInput.length() - 1));
            }
        }
    }

    private Dish SearchMenuFromDB(String context) {
        List<Dish> mnlist = DaoExpand.queryFuzzyMenu(AikaStyleApplication.instance.getDaoMaster().newSession().getDishDao(), context);
        if (mnlist.size() >= 1) {
            Collections.sort(mnlist, new Comparator<Dish>() {
                @Override
                public int compare(Dish m1, Dish m2) {
                    return m1.getName1().compareTo(m2.getName1());
                }
            });
            storedMenu = mnlist.get(0);
            return mnlist.get(0);
        }
        if (context.length() == 0) {
            return null;
        }
        return null;
    }

    @Override
    public void onClickItem(View view, int i) {
        L.d(TAG, "onClickItem");
        switch (view.getId()) {
            case R.id.oddelDish:
                SelectionDetail a = dishesXAdapter.get(i);
                //dishesXAdapter.remove(a);
                CustomerSelection.getInstance().deleteSelectedDish(a);
                loadOrderMenu();
                break;

            case R.id.odMnLoutMarkBtn:
                marksOfCurDish = dishesXAdapter.get(i).getMarkList();
                showMarksPanel(dishesXAdapter.get(i).getDish());
                break;

            default: //when items on recent bill list view is clicked
                Bill order = billsXAdapter.getData().get(i);
                showOrderDetail(order);
        }
    }

    private void showOrderDetail(Bill order) {
        String billID = order.getId().toString();
        String tableName = order.getTableName();
        String openTime = order.getOpenTime().substring(0, order.getOpenTime().length() - 3);
        String subTotalStr = String.format("%.2f", Float.valueOf(order.getTotal()) / 100.0);
        String totalStr = String.format("%.2f", Float.valueOf(order.getTotal())/100.0 * BarOption.getTaxRate());
        //        OrderIdentifierFragment.getInstance().dishesXAdapter.setData(order.getSelectedDishes());
        final List<SaleRecord> data = DaoExpand.querySalesByBill(order.getId().toString(), AikaStyleApplication.instance.getDaoMaster().newSession().getSaleRecordDao());

        XAdapter2<SaleRecord> adapter = new XAdapter2<SaleRecord>(getContext(), ConfigOrderViewHolder.class);
        adapter.setData(data);
        //updating the ui for bill selection panel

        tvBillIdx.setText(UC.ORDER_NUMBER + billID);
        tvTable.setText(UC.TABLE + tableName);
        tvOpenTime.setText(UC.OPEN_TIME + openTime);
        tvSubtoal.setText(UC.SUB_TOTAL + subTotalStr);
        tvTotal.setText(UC.TOTAL + totalStr);
        ordersView.setAdapter(adapter);
        //updating panel for table selection panel
        tvBillIdx2.setText(UC.ORDER_NUMBER + billID);
        tvTable2.setText(UC.TABLE + tableName);
        tvOpenTime2.setText(UC.OPEN_TIME + openTime);
        tvSubtoal2.setText(UC.SUB_TOTAL + subTotalStr);
        tvTotal2.setText(UC.TOTAL + totalStr);
        XAdapter2<SaleRecord> adapter2 = new XAdapter2<SaleRecord>(getContext(), ConfigOrderViewHolder.class);
        adapter2.setData(data);

        ordersView2.setAdapter(adapter2);
        findViewById(R.id.billOnTable).setVisibility(View.VISIBLE);
    }

    private void showMarksPanel(Dish menu){   //final onChoiceMarks choiceMarks) {
        if(BarOption.isMode2()){
            findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        }
        if (marksOfCurDish == null) {
            marksOfCurDish = new ArrayList<Mark>();
        }

        List<M2M_SaleRecordMark> markList = menu.getM2M_SaleRecordMarkList();
        if(markList != null && markList.size() > 0) {
            List<Mark> unsortedMarks = new ArrayList<>();
            for (M2M_SaleRecordMark m2m_MenuMark : markList) {
                unsortedMarks.add(m2m_MenuMark.getMark());
            }
            allMarks = sortMarks(unsortedMarks);
        }else{
            allMarks = sortMarks(DaoExpand.queryAllMarks(AikaStyleApplication.getMarkDao()));
        }

        //if mark is already selected, then remove it from the display list.
        for(int j = 0; j < allMarks.size(); j++) {
            Mark mark = allMarks.get(j);
            boolean matched = false;
            for(int i = 0; i < marksOfCurDish.size(); i++){
                String markName = marksOfCurDish.get(i).getName1();
                if(mark == null || mark.getName1() == null){
                    continue;
                }
                if(mark.getName1().equals(markName)){
                    matched = true;
                    mark.setQt(marksOfCurDish.get(i).getQt());
                    break;
                }
            }

            if(!matched) {
                mark.setQt(0);
            }
        }


        markAllXAdapter.setData(allMarks);
        markAllList.setAdapter(markAllXAdapter);
        markAllList.setSelection(0);

        if (menuMarkSwitcher.getDisplayedChild() == 0){
            categoryArea.setVisibility(View.GONE);
            menuMarkSwitcher.setDisplayedChild(1);
        }
//        new AlertDialog.Builder(this.getActivity()).setMultiChoiceItems(markNames, selectedQT, new DialogInterface.OnMultiChoiceClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                if (isChecked)
//                    sa.put(Integer.toString(which), which);
//                else
//                    sa.remove(Integer.toString(which));
//            }
//        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                List<Mark> result = new ArrayList<Mark>();
//                for (Integer i : sa.values()) {
//                    result.add(allMarks.get(i));
//                }
//                choiceMarks.onChoiceMarks(result);
//
//            }
//        }).setNegativeButton("Cancel", null).show();
    }

    private SelectionDetail addNewSelection(Dish menu) {
        SelectionDetail selectedDish = new SelectionDetail();
        selectedDish.setDish(menu);
        selectedDish.setMarkList(new ArrayList<Mark>());
        if(shortCutMarks != null) {
            L.d(TAG, shortCutMarks.toString());
        }
        selectedDish.setDishNum(1);
        CustomerSelection.getInstance().addSelectedDish(selectedDish);
        return selectedDish;
    }

    private void loadOrderMenu() {
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        dishesXAdapter.notifyDataSetChanged();
    }

    public static List<Mark> sortMarks(List<Mark> unsortedMarks) {
        List<Mark> sortedMarks = new ArrayList<Mark>();
        for (int i = 0; i < unsortedMarks.size(); i++) {
            Mark mark = unsortedMarks.get(i);
            if(mark == null){
                continue;   //in case the mark is removed but here not refreshed.
            }
            boolean inserted = false;
            for (int j = 0; j < sortedMarks.size(); j++) {
                if (sortedMarks.get(j).getStatus() > mark.getStatus()) {
                    sortedMarks.add(j, mark);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                sortedMarks.add(mark);
            }
        }
        for(int i = 0; i < 36 - sortedMarks.size(); i++){
            sortedMarks.add(new Mark());
        }
        return sortedMarks;
    }

    private void initShortCutMarks() {
        if("1".equalsIgnoreCase(BarOption.getCustomData(BarOption.HideMark)) || "true".equalsIgnoreCase(BarOption.getCustomData(BarOption.HideMark))) {
            odIdShortCutMarksGrid.setVisibility(View.GONE);
        }else{
            shortCutMarks = new ArrayList<Mark>(3);
            List<Mark> marks = new ArrayList<Mark>();
            //because we want to make sure the panel which contains the grid view has curtain height,
            //so we've made sure the allMarks must have at least values.
            marks.add(allMarks.get(0));
            marks.add(allMarks.get(1));
            markShorCutXAdapter = new XAdapter2<Mark>(getActivity(), marks, OrderIdentifierMarkViewHolder.class);
            markShorCutXAdapter.setClickItemListener(this.itemXAdapterClick);
            odIdShortCutMarksGrid.setAdapter(markShorCutXAdapter);
        }
    }

    private void backupAndClearOrderMenu() {
        //bk and clear selected menu
        bkOfLastSelection = new ArrayList<SelectionDetail>();
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            bkOfLastSelection.add(selectionDetail);
        }
        CustomerSelection.getInstance().clearMenu();

        //bk and clear bkOfLastTable
        //bkOfLastTable = odIdTableTbtn.getText();
        odIdTable1.setText("");
        odIdTable1.setChecked(true);
        loadOrderMenu();
        if(tables.size() == 0) {
            odIdTable2.setText(BarOption.getCustomData(BarOption.billIdx));
        }else{
            odIdTable2.setText("");
            CustomerSelection.getInstance().setTableName("");
        }
    }

    /**no one is calling this mehtod now, because we not response feels like app goes wrong. and user might input again.
     //waiting 15 seconds or untile confirmPrintOK() is called.
     private void waitForPrintSuccess(){
     int i = 0;
     while (true){
     i++;
     AppUtils.sleep(1000);
     if( lastSelection == null && "TB".equals(bkOfLastTable)){
     return;
     }
     }
     }

     //no one is calling this mehtod now, because we relized the order will not lost, it stay in ipConentMap
     //when connection come back, it will be printed out.
     private void rollbackLastOrder() {
     //save the menu into database.
     for(SelectionDetail selectionDetail : lastSelection){
     CustomerSelection.getInstance().addSelectedDish(selectionDetail);
     }

     //clear selected menu
     odIdTableTbtn.setText(bkOfLastTable);
     odIdTableTbtn.setChecked(false);
     loadOrderMenu();

     }*/

    public static void confirmPrintOK(){
        bkOfLastSelection = null;
        isCancel = false;
        //bkOfLastTable = "TB";
    }

}
