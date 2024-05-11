package com.just.print.app;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.just.print.R;
import com.just.print.db.bean.DiningTable;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.User;
import com.just.print.ui.fragment.ActivateFragment;
import com.just.print.ui.fragment.RegisterFragment;
import com.just.print.ui.panel.BillListPanel;
import com.just.print.util.L;
import com.just.print.util.StringUtils;
import com.just.print.util.ToastUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.DBUtil;
import org.cas.client.platform.bar.i18n.BarDlgConst;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class BarFrame extends BaseFragmentActivity {

    private String VERSION = "powered by ShareTheGoodOnes.com V003.030.0616";
    public static String MODETRANS = "O";
    public static BarFrame instance;
    public static BarDlgConst consts;

//    public static CustomerFrame customerFrame;
    private static User currentUser;
    public static List<DiningTable> diningTables;

    public Dish curDish;
    public int curPanelIdx;
    //curBillID is currently only used for displaying a expired bill, which is to say, when showingExpiredBill is set to true;
    private int curBillID;

    private boolean isShowingAnExpiredBill;

//    private DefaultComboBoxModel<String> tableNames = new DefaultComboBoxModel<String>(new String[] {""});//there's an empty item for tableMode, when no table selected.
    public boolean ignoreLogin;
//    public static NumberPanelDlg numberPanelDlg;
//    public static DiscountDlg discountDlg;
//    public static PayDlg payDlg;
//    public static CustomerFrame customerFrame;
//    public static GraphicsDevice secondScreen;

    private static Timer inactivityTimer;

    @XViewByID(R.id.cmbCurTable)
    public Spinner cmbCurTable;
    @XViewByID(R.id.valOperator)
    public TextView valOperator;
    @XViewByID(R.id.valCurBillIdx)
    public TextView valCurBillIdx;
    @XViewByID(R.id.valOpenTime)
    public TextView valOpenTime;

    public static User getCurrentUser() {
        return currentUser;
    }
    public static void setCurrentUser(User currentUser) {
        BarFrame.currentUser = currentUser;
        BarFrame.instance.valOperator.setText(currentUser.getNickName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        new StupidReflect(this).init();

        //this block should be useless, because dubug is already set value.-----no, it's static, so give a init here.
        // or maybe onCreate method could be called more than one time? so every time switch back to this page,
        // the oncreate will be called again? so we can update the debug setting without restart app?
        try {
            L.debug = Boolean.valueOf(Options.getCustomData(Options.debug));
        }catch(Exception e){
            L.i("the value of debug in customerr Data ", "can  not be parsered into a boolean!");
        }

        //working time check
        if(isOutOfWorkingTime()
                && StringUtils.isBlank(Options.getCustomData(Options.sendSM))){ //only when it's not used for sending sms, care about it's night or day version.
            ToastUtil.showToast("Invalidate operation detected, please contact info@ShareTheGoodOnes.com for technique support!");
            finish();   //No need to add thread to make it stay. even the app quit, the showToast will still on screen for a few seconds.
            return;
        }
//        if (alreadyExist()) {
//            ToastUtil.showToast("An instance is already running!");
//            return;
//        }

        //software life left time check
        long timeLeft = checkDaysleft();
        if (timeLeft > 0) {
            if (timeLeft < 3024000000l) {   //3024000000L == 35days
                ToastUtil.showToast("Application is about to expire! Please re-activate it!");
            }
            switchFragment(new RegisterFragment());
        } else {
            ToastUtil.showToast("Application expired! Please re-activate it!");
            switchFragment(new ActivateFragment());
        }
    }

    public void populateSpinnerWithTables() {
        // 假设 diningTables 已经被初始化并且包含数据
        ArrayAdapter<DiningTable> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, diningTables);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbCurTable.setAdapter(adapter);
    }

    //@note: there's a issue in this method!!!!!
    //@in some device, it always return a negative value and cause reactive dlg displayed.
    private long checkDaysleft() {
        //none limitation check
        if("none".equals(Options.getCustomData(Options.limitation))){             L.d("limitationMode", "none");
            return 3024000000l + 1;
        }

        //time of last open, @note:if existing last open time is not valid, then use lastSuccess will not be set.
        long timepassed = 0l;
        String lastsuccessStr = Options.getCustomData(Options.lastsuccessStr);    L.d("lastSuccessStr:",lastsuccessStr);
        try{
            Long lastSuccess= Long.valueOf(lastsuccessStr);
            timepassed = new Date().getTime() - lastSuccess;            L.d("timePassed:",timepassed);        //time passed since last open.
        }catch(Exception e){
            L.e("MainActivity", "the lastsuccessStr is not valid long", e);
        }

        //if timeLeftStr is valid, then it has a chance to turn the timeLeft to be a number bigger than 0.
        long timeLeft = 0;
        String lastTimeLeft = Options.getCustomData(Options.number);               L.d("timeLeft(before deduct:", lastTimeLeft);
        if(StringUtils.isBlank(lastTimeLeft)){
            lastTimeLeft = "1";
        }
        try {
            //the time left from last calculation, minus time passed. @note: we use abs, so is the time is negative, will still be minused!
            timeLeft = Long.valueOf(lastTimeLeft) - Math.abs(timepassed);    L.d("timeLeft - timePassed:", timeLeft);

            //update the number and lastsuccess into local cache.
            Options.putCustomData(Options.lastsuccessStr, String.valueOf(new Date().getTime()));
            Options.putCustomData(Options.number, String.valueOf(timeLeft));              L.d("update new number with:", timeLeft);
        }catch(Exception e){
            L.e("MainActivity", "the left time number can not be pasered into a long", e);
        }

        return timeLeft;
    }

    private boolean isOutOfWorkingTime(){

        String startTime = Options.getCustomData(Options.startTime);
        String endTime = Options.getCustomData(Options.endTime);
        String curTime = "";

        DateFormat df = new SimpleDateFormat("HHmm");
        try {
            Date d = new Date();
            curTime = df.format(d);
        }catch(Exception e){
            ToastUtil.showToast("Error detected with system time, please contact info@ShareTheGoodOnes.com for technique support.");
        }

        if(!StringUtils.isBlank(startTime)){
            if(curTime.compareTo(startTime) < 0) {
                return true;
            }
        }

        if(!StringUtils.isBlank(endTime)){
            if(curTime.compareTo(endTime) > 0) {
                return true;
            }
        }

        return false;
    }

    public void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.panel_container);
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if(fragment != null) {  //if the parameter is null, only hide existing one, don't show any new one.
            Fragment existingFragment = fragmentManager.findFragmentByTag(fragment.getClass().getSimpleName());
            if (existingFragment == null) {
                // Fragment 不在 fragmentManager 的列表中，添加它
                transaction.add(R.id.panel_container, fragment, fragment.getClass().getSimpleName());
            } else {
                // Fragment 已经存在，只需显示它
                transaction.show(existingFragment);
            }
        }
        transaction.commit();
    }

    public int createAnEmptyBill(String tableName, String openTime, int newBillIdx, int status){
        //validate parameters
        tableName = BarUtil.empty(tableName) ? cmbCurTable.getSelectedItem().toString() : tableName;
        openTime = BarUtil.empty(openTime) ? valOpenTime.getText().toString() : openTime;
        if(newBillIdx <= 0) {
            newBillIdx = BillListPanel.getANewBillIdx(tableName, openTime);
        }
        //create a bill for it. in case there will be something like order fee in future. this bill has no create time yet, so will not display minutes on buttom in counter mode.(counter
        //mode calculated created time, table mode calculates waited minutes by opentime.
        StringBuilder sql = new StringBuilder(
                "INSERT INTO bill(createtime, tableID, BillIndex, total, discount, tip, serviceFee, cashback, EMPLOYEEID, Comment, opentime, status) VALUES ('")
                .append(BarUtil.df.format(new Date())).append("', '")//		        "INSERT INTO bill(tableID, BillIndex, total, discount, tip, serviceFee, cashback, EMPLOYEEID, Comment, opentime, status) VALUES ('")
                .append(tableName).append("', '")	//table
                .append(newBillIdx).append("', ")			//bill
                .append(0).append(", ")	//total
                .append(0).append(", ")
                .append(0).append(", ")
                .append(0).append(", ")
                .append(0).append(", ")	//discount
                .append(currentUser.getId()).append(", '")		//emoployid
                .append(MODETRANS.equals("F") ? "for training" : "").append("', '")
                .append(openTime).append("', ")
                .append(status).append(")");				//content
        try {
            AikaPos.db.execSQL(sql.toString());
            sql = new StringBuilder("Select id from bill where opentime = '").append(openTime)
                    .append("' and tableID = '").append(tableName)
                    .append("' and BillIndex = '").append(newBillIdx).append("'");
            Cursor cursor = AikaPos.db.rawQuery(sql.toString(), null);
            if (cursor != null && cursor.moveToFirst()) {
                return DBUtil.getCursorInt(cursor, "id");
            }
        }catch(Exception exp) {
            L.e("createABill", "exception when creating a bill: " + sql, exp);
        }
        return -1;
    }

    public static String prepareLicenceJSONString() {
        JSONObject json = new JSONObject();//创建json对象
        try {
            json.put("licence", BarOption.getLicense());//使用URLEncoder.encode对特殊和不可见字符进行编码
        } catch (JSONException e) {
            L.e("BarFrame", "unexpected exception when putting license into json object." + BarOption.getLicense(), e);
        }
        String jsonstr = json.toString();//把JSON对象按JSON的编码格式转换为字符串
        return jsonstr;
    }

    public String getCurBillIndex() {
        String curBillIndex = valCurBillIdx.getText().toString();
        if(curBillIndex == null || curBillIndex.trim().length() == 0 || "0".equals(curBillIndex))
            return "0";	//0 means there's only one customer or one bill in the table.
        else
            return curBillIndex;
    }

    public void setCurBillIdx(String curBillIndex) {
        if(curBillIndex == null || curBillIndex.trim().length() == 0 || "0".equals(curBillIndex))
            valCurBillIdx.setText("");
        else
            valCurBillIdx.setText(curBillIndex);
    }

    public int getCurBillID() {
        return curBillID;
    }

    public void setCurBillID(int curBillID) {
        this.curBillID = curBillID;
    }

}
