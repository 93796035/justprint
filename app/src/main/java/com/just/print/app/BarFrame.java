package com.just.print.app;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.just.print.R;
import com.just.print.db.bean.DiningTable;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.User;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.BillListPanel;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.ui.panel.SettingPanel;
import com.just.print.ui.panel.TablesPanel;
import com.just.print.util.JOptionPane;
import com.just.print.util.StringUtils;
import com.just.print.util.ToastUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.action.Cmd_CheckInOut;
import org.cas.client.platform.bar.dialog.CmdBtnsManagerDlg;
import org.cas.client.platform.bar.dialog.DiscountDlg;
import org.cas.client.platform.bar.dialog.NumberPanelDlg;
import org.cas.client.platform.bar.dialog.PayDlg;
import org.cas.client.platform.bar.dialog.statistics.CheckInOutListDlg;
import org.cas.client.platform.bar.i18n.BarDlgConst;
import org.cas.client.platform.bar.i18n.BarDlgConst0;
import org.cas.client.platform.bar.i18n.BarDlgConst1;
import org.cas.client.platform.bar.i18n.BarDlgConst2;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.net.BackGroundTimerThread;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.bar.panel.CustomerFrame;
import org.cas.client.platform.bar.panel.MenuPanel;
import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.bar.web_srm.ValuePossible;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BarFrame extends BaseFragmentActivity {

    private String VERSION = "powered by ShareTheGoodOnes.com V003.030.0616";
    public static String MODETRANS = "O";
    public static BarFrame instance;
    public static BarDlgConst consts;

//    public static CustomerFrame customerFrame;
    private static User currentUser;
    public Dish curDish;
    public int curPanelIdx;
    //curBillID is currently only used for displaying a expired bill, which is to say, when showingExpiredBill is set to true;
    private int curBillID;

    private boolean isShowingAnExpiredBill;

    public static MenuPanel menuPanel;
//    private DefaultComboBoxModel<String> tableNames = new DefaultComboBoxModel<String>(new String[] {""});//there's an empty item for tableMode, when no table selected.
    public boolean ignoreLogin;
    public static NumberPanelDlg numberPanelDlg;
    public static DiscountDlg discountDlg;
    public static PayDlg payDlg;
    public static CustomerFrame customerFrame;
//    public static GraphicsDevice secondScreen;

    private boolean isActive;

    String oldTable;
    public boolean ignoreItemChange;	//to temperally make the comboBox deaf to the value changes.

    public BaseFragment[] panels = new BaseFragment[4];

    private static final int AUTO_LOGOUT_TIME = 5 * 60 * 1000;  // 5分钟无活动自动登出

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
        setContentView(R.layout.activity_main);
        new StupidReflect(this).init();
        instance = this;
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
//            switchFragment(new RegisterFragment());
        } else {
            ToastUtil.showToast("Application expired! Please re-activate it!");
//            switchFragment(new ActivateFragment());
        }

        panels[0] = new TablesPanel();
        panels[1] = new BillListPanel();
        panels[2] = new SalesPanel();
        panels[3] = new SettingPanel();
        //==========================================================
        menuPanel = new MenuPanel();	//have to be after initModel, before new BarFrame().
        switch (CustOpts.custOps.getUserLang()){//before login, the system language is the last change of userLanguage, (depends on who logged in last time.
            case 0:
                consts = new BarDlgConst0();
                break;
            case 1:
                consts = new BarDlgConst1();
                break;
            case 2:
                consts = new BarDlgConst2();
                break;
            default:
                consts = new BarDlgConst1();
        }

        CmdBtnsManagerDlg.initButtons();

        numberPanelDlg = new NumberPanelDlg();
        discountDlg = new DiscountDlg();
        payDlg = new PayDlg();


        //single user or not, display login dialog or mainInterface.
        if(BarOption.isSingleUser()) {
            singleUserLoginProcess();
        }

        //counter mode or not
        if(BarOption.isCounterMode()) {
//            instance.setCurBillIdx("1");
//
//            instance.valOpenTime.setText(BarUtil.df.format(new Date()));
//            instance.lblOpenTime.setVisibility(View.GONE);
//            instance.valOpenTime.setVisibility(View.GONE);
//
//            ((SalesPanel)instance.panels[2]).billPanel.setBillID(instance.getCurBillID());
//            instance.switchMode(0);	//inside it will check, if it's counter mode, will automatically open Sales mode.
//
//            //BarFrame.instance.switchToOtherBill();
//            //following code was copied from switchToOtherBill, modified some where like:  && !tablesPanel.btnTables.get(i).getText().equals(tableName))
//            //just to a different one and rmoved the status check like isCountermode(), cause here is already counter mode.
//            String tableName = (String)BarFrame.instance.cmbCurTable.getSelectedItem();
//            if(!instance.isTheOnlyBillInTable(null, null, instance.curBillID)) {	//if there's other bill in this table, then view other bills.
//                instance.switchMode(1);
//            }else {
//                //the differentce with actionAfterBillClosed is lack of this line.//closeATable(null, null);	//when closing a table with check the "DeleteTableWhenClosing" flag, and this flag should has been set when set to Countermode.
//                TablesPanel tablesPanel = (TablesPanel)instance.panels[0];
//                //tablesPanel.initContent();
//                //if there's other unclosed table?..maybe can get from tablesPanel instead of query from db?
//                boolean foundUnclosedTable = false;
//                int i = 0;
//                for (; i < tablesPanel.btnTables.size(); i++) {
//                    if(tablesPanel.btnTables.get(i).getStatus() > 0 && !tablesPanel.btnTables.get(i).getText().equals(tableName)) {
//                        foundUnclosedTable = true;
//                        break;
//                    }
//                }
//
//                if(foundUnclosedTable) {
//                    ActionEvent evt = new ActionEvent(tablesPanel.btnTables.get(i), 0, null);
//                    tablesPanel.actionPerformed(evt);
//                }else {			//if no, create a new table wich next billIdx(will be table id)
//                    int billIdx = BarOption.popKitchenBillIdx();
//                    CreateNewOrderAction.makeSureTableExistsAndOpened(String.valueOf(billIdx), BarUtil.df.format(new Date()));
//                    TableButton tableToggleButton = new TableButton();
//                    tableToggleButton.setText(String.valueOf(billIdx));
//                    tableToggleButton.setTableName(String.valueOf(billIdx));
//                    ActionEvent evt = new ActionEvent(tableToggleButton, 0, null);
//                    tablesPanel.actionPerformed(evt);
//                }
//            }
        }else {
            instance.switchMode(0);	//while instance is still null if don't put it in the later.
        }

        //this thread will start a request thread every 10 seconds to fetch new order from server..
        //also will trigger a refresh of tableivew for the waiting time.
        new BackGroundTimerThread().start();


        //activation check
        validateActivation();
//        if(!"OK".equals(returnStr)) { 	// if not valid, might because of expired, then give clean the bill head,
//            if(!"OK".equals(validateActivation(returnStr))) {	// give another valid with passcode (first time with null).
//                System.exit(0);
//            }
//        }

        //this line is kind of slow, and because the frame is no decoration by default, so
        //comment it for now. instance.setExtendedState(BarOption.getDefaultWindowStatus());
//        SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//		        //TDOO:print an empty report, hope this can make the opendrawer command work.
//				//isn't this move can also make sure the mev connected printer work?
//				ArrayList<String> formattedString = new ArrayList<String>();
//				for(int i = 0; i < 15; i++) {
//					formattedString.add("");
//				}
//		    	PrintService.printTemporaryReport( BarFrame.menuPanel.getPrinters()[0].getIp(), formattedString);
//			}
//		});
    }

    private void validateActivation() {
        if(BarOption.getBillHeadInfo() == null || BarOption.getBillHeadInfo().trim().length() == 0 ) { //if store info not found, means first time use.
            JOptionPane.showInputDialog("Activate", consts.activeCode(), new JOptionPane.InputDialogListener() {
                @Override
                public void onInputReceived(String input) {

                    if (input == null || input.length() != 6 && !input.equals("9")) {
                        JOptionPane.showMessageDialog(null, "Please input correct license code");
                        return;
                    }
                    BarOption.setLicense(input.equals("9") ? "ER0001" : input);

                    activateAccount();
                }

                @Override
                public void onInputCanceled() {
                    Toast.makeText(AikaPos.instance.getApplicationContext(), "Activation canceled! Please activate to let the software ready to work.", Toast.LENGTH_SHORT).show();
                }
            });
        }else {	//already started
            L.d("bill info is:", BarOption.getBillHeadInfo());
            long timeLeft = checkDaysleft();
            if(timeLeft > 0) {
                if (timeLeft < 3024000000l) {   //3024000000L == 35days
                    JOptionPane.showMessageDialog(null, "Application is about to expire! Please re-activate it!");
                }
                //Guess Revenue Quebec want his data record keep insistant, so they don't want the app to use a
                //different Appareil ID, so they know the data is from which machine.
                if(BarFrame.instance.menuPanel.getPrinters()[0].getIp().startsWith("mev")
                        && BarOption.APPAREIL_ID_FORMAT.equals(BarOption.getCertifiedDeviceId())) {
                    existingIdApprlCheck();
                }else {
                    MevUtils.checkCertificateDate();//check certificate time
                }
            }else {
                BarOption.setBillHeadInfo(null);
                CustOpts.custOps.saveData();//save to hd.
//                return activateCode;	//because the activateCode is null, so this will cause a dialog for inputting activate code,
                //but after inputting code, will not have chance to come in again and do the authenticate. so I think should do system.exit()
            }
        }
    }

    private void activateAccount() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://216.137.179.176/activeAccount";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String jsonString = prepareLicenceJSONString();  // Implement this method to prepare your JSON payload

        RequestBody body = RequestBody.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    processActivationResponse(responseString);
                    runOnUiThread(() -> {
                        existingIdApprlCheck();
                    });
                } else {
                    runOnUiThread(() -> {
                        JOptionPane.showMessageDialog("Software expired, please contact us at info@ShareTheGoodOnes.com");
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    // Handle failure in activation
                    JOptionPane.showMessageDialog("Activation failed with error code 520, please contact us at info@ShareTheGoodOnes.com");
                });
            }
        });
    }

    private static void existingIdApprlCheck() {
        if (BarOption.getBillingNumber() == null || BarOption.getBillingNumber().length() < 5) {
            BarOption.setBillingNumber(BarOption.getLicense());
            // No need to check again: BarOption.APPAREIL_ID_FORMAT.equals(BarOption.getCertifiedDeviceId())
            // it's already checked, and only when it's true, can reach to this method.
            JOptionPane.showConfirmDialog(BarFrame.instance, "Avez-vous un identifiant pour cet appareil (IDAPPRL)?", "APPAREIL_ID", new JOptionPane.ConfirmDialogCallback() {
                @Override
                public void onResult(boolean confirmed) {
                    if (confirmed) {
                        JOptionPane.showInputDialog("identifiant d'appareil", "<html>Veuillez inscrirer votre identifiant d'appareil<br> avant d'effectuer une requête:</html>", new JOptionPane.InputDialogListener() {
                            @Override
                            public void onInputReceived(String input) {
                                BarOption.setCertifiedDeviceId(input);
                            }

                            @Override
                            public void onInputCanceled() {
                                Toast.makeText(AikaPos.instance.getApplicationContext(), "certificate request canceled! Please input \"identifiant d\'appareil\" to let the software ready to work.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // if the input idApprl not valid, will end up not saved, when getting this configuration will get "0000-0000-0000".
                        if (BarOption.APPAREIL_ID_FORMAT.equals(BarOption.getCertifiedDeviceId())) {
                            JOptionPane.showMessageDialog(null, "inputted idApprl is not in valid format.");
                            existingIdApprlCheck();
                        }
                    }
                    new AskWebMevToSignTask().execute(ValuePossible.Modif.AJO); // 使用name()方法将枚举转换为String
                }
            });
        }
    }

    // AsyncTask to execute askWebMevToSignTheCertificates
    private static class AskWebMevToSignTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            MevUtils.askWebMevToSignTheCertificates(params[0]); // 使用字符串参数
            return null;
        }
    }

    private void processActivationResponse(String response) {
        int p = response.indexOf("_");
        if (p == -1) {
            JOptionPane.showMessageDialog("Activation failed with error code 520, please contact us at info@ShareTheGoodOnes.com");
            return;
        }

        // Process the response as in your existing code
        String timeLeft = response.substring(0, p).trim();
        long time = "none".equals(timeLeft) ? 100L * 365 * 24 * 3600 * 1000 : Long.parseLong(timeLeft) * 24 * 3600 * 1000;
        if (time > 0) {
            BarOption.setActivateTimeLeft(String.valueOf(time * 24 * 3600 * 1000));
            BarOption.setLastSuccessStr(String.valueOf(new Date().getTime()));

            String billHeadInfo = response.substring(p + 1);
            //get out bill head info
            p = billHeadInfo.indexOf("_");
            if (p > 0) {
                BarOption.setBillHeadInfo(billHeadInfo.substring(0, p));
                //get out GST-----put all string left into gst first, then get the first part for the real gst.
                String GST = billHeadInfo.substring(p + 1);
                p = GST.indexOf("_");
                if(p > 0) {
                    BarOption.setGSTAccount(GST.substring(0, p));
                    //get out QST----put all string left into qst first, then get the first part as the real qst.
                    String QST = GST.substring(p + 1);
                    p = QST.indexOf("_");
                    if(p > 0) {
                        BarOption.setQSTAccount(QST.substring(0, p));
                        String authorizationCode = QST.substring(p + 1);
                        p = authorizationCode.indexOf("_");
                        if(p > 0) {
                            BarOption.setMandatoryAuthorizationCode(authorizationCode.substring(0, p));
                            String billingNumber = authorizationCode.substring(p + 1);
                            p = billingNumber.indexOf("_");
                            if(p > 0) {
                                BarOption.setBillingNumber(billingNumber.substring(0, p));

                                //get out the last part: activated times, this value is updated on server before return to here.
                                //if there's more contents, goes before this line, it's like put string left into XXX,
                                //then check if there's "_", if yes, get the real value XXX, all string after "_" goes into ActivateTimes......
                                //activateTimes must be the last part.
                                //Be noticed that if you added new part, should consider if it need to be encr
                                BarOption.setActivatedTimes(billingNumber.substring(p + 1));
                            }else {
                                BarOption.setBillingNumber(billingNumber); //ER0001
                            }
                        }else {
                            BarOption.setMandatoryAuthorizationCode(authorizationCode);
                        }
                    }else {
                        BarOption.setQSTAccount(QST);
                    }
                }else {
                    BarOption.setGSTAccount(GST);
                }
            }else {
                BarOption.setBillHeadInfo(billHeadInfo);
            }

            // @NOTE: can not use JOptionPane.showMessageDialog, because it will be hided by
            // LoginDlg, and stuck there.
            setStatusMes("Application is activated successfully!");
        } else {
            JOptionPane.showMessageDialog(null,
                    "Software expired, please contact us at info@ShareTheGoodOnes.com");
        }
    }

    public static void setStatusMes(String pMes) {
        lblStatus.setText(pMes);
    }

    public static HttpURLConnection prepareConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true); // 对于POST请求必须设置为true
        connection.setConnectTimeout(15000); // 设置连接超时
        connection.setReadTimeout(15000); // 设置读取超时
        return connection;
    }

    public static void writeOut(HttpURLConnection connection, String content) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = content.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }

    public static String readBackFromConnection(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }

    //---------------automatic logout every 5 minutes----------------
    private Handler inactivityHandler = new Handler();
    private Runnable inactivityRunnable = new Runnable() {
        @Override
        public void run() {
            if (isActivityVisible()) {
                Cmd_CheckInOut.getInstance().onClick(null);
            }
        }
    };
    public void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable);
        inactivityHandler.postDelayed(inactivityRunnable, BarOption.getAutoLogoutTime() * 60000); // 假设getAutoLogoutTime()返回的是分钟
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;  // 当活动在前台时设置为true
        resetInactivityTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        inactivityHandler.removeCallbacks(inactivityRunnable);
    }
    //=======================================
    public boolean isActivityVisible() {
        return isActive;
    }


    public void populateSpinnerWithTables(List<DiningTable> diningTables) {
        // 假设 diningTables 已经被初始化并且包含数据
        adapter = new ArrayAdapter<>(this,
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
                "INSERT INTO bill(createtime, tableName, BillIndex, total, discount, tip, serviceFee, cashback, userId, Comment, opentime, status) VALUES ('")
                .append(BarUtil.df.format(new Date())).append("', '")//		        "INSERT INTO bill(tableName, BillIndex, total, discount, tip, serviceFee, cashback, userId, Comment, opentime, status) VALUES ('")
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
                    .append("' and tableName = '").append(tableName)
                    .append("' and BillIndex = '").append(newBillIdx).append("'");
            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            if (rs != null && rs.moveToFirst()) {
                return DBUtil.getCursorInt(rs, "id");
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

    public void userCheckOut() {
        //try clean webmev batch.
        MevUtils.flushBatchWithCurTransaction();

        if(BarOption.isSingleUser()) {
//            CheckInOutListDlg.updateCheckInRecord();
//            Window[] windows = Window.getWindows();
//            for (Window window : windows) {
//                if (window.isVisible() && window instanceof JDialog) {
//                    window.setVisible(false);
//                }
//            }
//            instance.setVisible(false);
//            singleUserLoginProcess();
//            BarFrame.instance.switchMode(BarOption.isCounterMode() ? 2 : 0);
        }else {	//for multi user case, when sign out button clicked, if user inputted right pw, then do the signout.
            BarFrame.instance.switchFragment(null);
            LoginDlg.getInstance().show(getSupportFragmentManager(), "login_dialog");
            if (LoginDlg.PASSED == true) {
                CheckInOutListDlg.updateCheckInRecord();
                //@note: lowdown a little the level, to enable the admin do sales work.
                if (LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS) {
                    LoginDlg.setUSERTYPE(LoginDlg.USER_STATUS);
                }
                BarFrame.instance.switchMode(BarOption.isCounterMode() ? 2 : 0);
            }else {	//if closed the dialog without inputting the right pw, then system exit.
                CustOpts.custOps.saveData();
                BarFrame.instance.finish();
            }
        }
    }

    public boolean adminAuthentication() {
        int oldUserType = LoginDlg.getUSERTYPE();
        int oldID = LoginDlg.USERID;
        int oldUserLang = LoginDlg.USERLANG;
        String oldUserName = LoginDlg.USERNAME;

        LoginDlg.getInstance().noRefresh = true;
        LoginDlg.getInstance().show(getSupportFragmentManager(), "login_dialog");
        if (LoginDlg.PASSED == true && LoginDlg.getUSERTYPE() == LoginDlg.ADMIN_STATUS) { // 如果用户选择了确定按钮。
//Let's change to everything keep the same after the admin authentication.
//        	valOperator.setText(LoginDlg.USERNAME);
//            setStatusMes(consts.ADMIN_MODE());
//the interface will be changed to yellow color for warning.
//            revalidate();
            LoginDlg.setUSERTYPE(oldUserType);
            LoginDlg.USERID = oldID;
            LoginDlg.USERLANG = oldUserLang;
            LoginDlg.USERNAME = oldUserName;
            return true;
        }
        return false;
    }

    public int switchMode(int index) {
//        if(curPanelIdx == 3) {	//if it's switching from setting panel, then ask to do a login.
//            if(ignoreLogin == false) {
//                if(BarOption.isSingleUser()) {
//                    instance.setVisible(false);
//                    new LoginDlg(this).setVisible(true);
//                    if(!LoginDlg.PASSED) {
//                        CASControl.ctrl.exitSystem();
//                    }
//                    instance.setVisible(true);
//                    valOperator.setText(LoginDlg.USERNAME);
//                }
//            }else {
//                ignoreLogin = false;
//            }
//        }
//        curDish = null;
//        setStatusMes("");
//        if (index == 3) {		//setting
//            if (!superAdminAuthentication())
//                return -1;
//            //shouldn't do this, when switch back, if there's no time, don't know where to continue.//resetStatus();
//        }else if(LoginDlg.getUSERTYPE() >= LoginDlg.SUPER_ADMIN) {
//            index = 3;
//        }else if(index == 2) {	//sale
//            initSalesPanel();
//        }else if(index == 1) {	//bill
//            ((BillListPanel)panels[1]).initContent();
//        }else if(index == 0) {	//table
//            resetStatus();
//            ((TablesPanel)panels[0]).initContent();
//        }
//
//        for (JComponent panel : panels) {
//            panel.setVisible(false);
//        }
//
//        reLayout();	//recalculate the panel size---because if it's counter mode, need to display tablespane and salesPane together,
//        //and when back to tablespane, it also need to relayout, otherwise, will display blank.
//        panels[index].setVisible(true);
//
//        if(BarOption.isCounterMode()) {
//            if(index == 1 || index == 2) {
//                panels[0].setVisible(true);// this line not necessary, because in layout, will decide if panels[0] visible or not base on there's multi table or not.
//                String tableName = String.valueOf(cmbCurTable.getSelectedItem());
//                ((TablesPanel)panels[0]).initContent();// might be switched from temperal table mode.(in counter mode, user can switch to table mode, to select a preset table)
//                ignoreItemChange = true;
//                BarFrame.instance.cmbCurTable.getModel().setSelectedItem(tableName);	//? why use .getModel() instead of directly cmbCurTable.setSelectedItem(tableName)?
//                TableButton tableButton = ((TablesPanel)BarFrame.instance.panels[0]).getTableButtonByName(tableName);
//                if(tableButton != null) {
//                    BarFrame.instance.valOpenTime.setText(tableButton.getOpenTime());
//                }
//            }
//        }
//
//        curPanelIdx = index;
//        if(index > 1) {	//salespanel and setting pannel need menu panel on it.
//            //it's x, y might have been changed if user clicked menu command on tablePenal
//            BillPanel billPanel = ((SalesPanel)BarFrame.instance.panels[2]).billPanel;
//            int xMenuArea = billPanel.getX() + billPanel.getWidth() + CustOpts.HOR_GAP / 2;
//            menuPanel.setLocation(xMenuArea, billPanel.getY());
//
//            menuPanel.reInitCategoryAndMenuBtns();
//            menuPanel.reLayout();
//            panels[index].add(menuPanel);
//        }
//
//        panels[index].requestFocus();

        return 0;
    }

    public static void singleUserLoginProcess() {
//        BarFrame.instance.switchFragment(null);
//        LoginDialogFragment.getInstance().show(BarFrame.instance.getSupportFragmentManager(), "login_dialog");
//        if (LoginDlg.PASSED == true) { // 如果用户选择了确定按钮。
//            instance.setVisible(true);
//            checkSignIn();
//        }else {	//the case that user clicked X button.
//            Cas
//        }
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
    //what should we do? add new bill, switch back to table view? bill list view? or switch to another table? open a new table?

    public void actionAfterBillClosed() {
        if(BarOption.getDefaultTableName().equals(BarFrame.instance.cmbCurTable.getSelectedItem().toString())) {
            //this is for a special case, don't know if will be needed, if is't on Default table, will add a new bill in current table.
            //the bill will not has it's own open time, while will have close time, anyway, don't think it's good for restaurent, but might fit the deparnuer.
            valOpenTime.setText(BarUtil.df.format(new Date()));
            addNewBillInCurTable();
        }else {
            if(isTableEmpty(null, null)) {
                closeTableAndBillInDB(null, null);	//when closing a table with check the "DeleteTableWhenClosing" flag, and this flag should has been set when set to Countermode.
                TablesPanel tablesPanel = (TablesPanel)panels[0];
                tablesPanel.initContent();
//                if(BarOption.isCounterMode()) {
//                    tablesPanel.switchToOtherTable();
//                }else {
                    switchMode(0);
//                }

            } else {	//if there's other bill in this table, then view other bills.
                switchMode(1);
            }
        }
    }

    public void closeTableAndBillInDB(String tableName, String openTime) {
        //all the bill should already completed when table closed except those was generated while has no output on it.
        //this is the designed time to clean those bills.
        StringBuilder sql = new StringBuilder("update bill set status = ").append(DBConsts.DELETED)
                .append(" WHERE tableName = '").append(tableName)
                .append("' and OPENTIME = '").append(openTime)
                .append("' and (status IS NULL OR status <= ").append(DBConsts.SAVED).append(")");	//there are bills which was generated during split, which status is "saved".
        try {
            AikaPos.db.execSQL(sql.toString());
            if(BarOption.isDeleteTableWhenClose()) {
                sql = new StringBuilder("delete from dining_Table WHERE name = '").append(tableName).append("'");
            }else {
                sql = new StringBuilder("update dining_Table set status = ").append(DBConsts.ORIGINAL)
                        .append(", opentime = '' WHERE name = '").append(tableName).append("'");
            }
            AikaPos.db.execSQL(sql.toString());
        }catch(Exception exp) {
            L.e("change table", "exception when recover table:" + sql, exp);
        }
    }

    //add new bill with a new billID and billIdx.
    public void addNewBillInCurTable() {
        String tableName = instance.cmbCurTable.getSelectedItem().toString();
        String openTime = instance.valOpenTime.getText().toString();
        SalesPanel salesPanel = (SalesPanel)panels[2];
        BillPanel billPanel = salesPanel.billPanel;

        int newBillIdx = BillListPanel.getANewBillIdx(null, null);

        int billId = createAnEmptyBill(tableName, openTime, newBillIdx, 0);
        billPanel.setBillID(billId);

        setCurBillIdx(String.valueOf(newBillIdx));
        switchMode(2);
    }

    // @NOTE in first solution, we don't check the bills here, because it's OK there could be bills (an empty bill, or none-closed bills
    //generated during the splitting bill), while the bad thing is that before the table closed, there's bill displaying in the check
    //order list, which is not big problem but might cause the confusion of the store owner.....
    //Then, I thought about a new sulution--when combining all or move item, should check the left output numbers of the bill, if no left, then put it to dumpt.
    //while the bad thing of this solution is that, when uncombinning will have trouble, because it need to find the unclosed billPanes, if those bill closed,
    //then the billPanel will not be found, then the button will not display unCombine, and even we do uncombine, we will need to change the status back.
    //So, finally, I think it's better to let the unclosed bills display in check list box, anyway, they are not closed yet, they are waiting for uncombine.
    private boolean isTableEmpty(String tableName, String openTime){
        //validate parameters
        tableName = BarUtil.empty(tableName) ? cmbCurTable.getSelectedItem().toString() : tableName;
        openTime = BarUtil.empty(openTime) ? valOpenTime.getText().toString() : openTime;

        try {
            StringBuilder sql = new StringBuilder("SELECT DISTINCT contactID from output where SUBJECT = '").append(tableName)
                    .append("' and (deleted is null or deleted = ").append(DBConsts.ORIGINAL)
                    .append(") and time = '").append(openTime).append("'");

            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            return rs.getCount() == 0;
        } catch (Exception exp) {
            L.e("BarFrame", " exception when checking if isTableEmpty", exp);
        }
        return false ;
    }

    public String getCurTable() {
        return (String)cmbCurTable.getSelectedItem();
    }

    /**
     * used when single user login, or when multiuser adding table or open table.
     * if already has record, then ignore, if not have record yet. then add one.
     */
    public static void checkSignIn() {
        //first make sure the name is displayed on BarFrame heder area.
        instance.valOperator.setText(LoginDlg.USERNAME);

        String time = BarUtil.df.format(new Date());
        int p = time.indexOf(" ");
        time = time.substring(0, p + 1) + BarOption.getStartTime();

        String sql = "Select * from evaluation where userId = " + LoginDlg.USERID + " and startTime > '" + time
                + "' and endTime is null";

        try {
            Cursor rs =  AikaPos.db.rawQuery(sql, null);
            rs.moveToNext();
            if(DBUtil.getCursorInt(rs, "id") > -1);	//if not throwning exception (means there is a record), then do nothing.
            return;
        }catch(Exception exp) {			//otherwise(if no record yet), generate a record.
            sql = "INSERT INTO evaluation(startTime, userId, subject) VALUES ('" + BarUtil.df.format(new Date())
                    + "', " + LoginDlg.USERID + ", '" + LoginDlg.USERNAME +"')";
            try {
                AikaPos.db.execSQL(sql);
            }catch(Exception exp2) {
                L.e("BarFrame ", "exception when insert into evaluation:" + sql, exp2);
            }
        }
    }

    @XViewByID(R.id.cmbCurTable)
    public Spinner cmbCurTable;
    public ArrayAdapter<DiningTable> adapter;
    @XViewByID(R.id.valOperator)
    public TextView valOperator;

    @XViewByID(R.id.lblCurBillIdx)
    public static TextView lblCurBillIdx;
    @XViewByID(R.id.valCurBillIdx)
    public TextView valCurBillIdx;

    @XViewByID(R.id.lblOpenTime)
    public TextView lblOpenTime;

    @XViewByID(R.id.valOpenTime)
    public TextView valOpenTime;

    @XViewByID(R.id.btnRetryPrinter)
    public Button btnRetryPrinter;

    @XViewByID(R.id.btnFlushMevBatch)
    public Button btnFlushMevBatch;

    @XViewByID(R.id.lblStatus)
    public static TextView lblStatus;

    public void hideBillIdx(boolean b) {
        lblCurBillIdx.setVisibility(b ? View.GONE : View.VISIBLE);
        valCurBillIdx.setVisibility(b ? View.GONE : View.VISIBLE);
    }

    public Spinner getCmbCurTable() {
        return cmbCurTable;
    }

    public boolean isShowingAnExpiredBill() {
        return isShowingAnExpiredBill;
    }
    public void setShowingAnExpiredBill(boolean isShowingAnExpiredBill) {
        this.isShowingAnExpiredBill = isShowingAnExpiredBill;
    }

    public void updateOutputStatus(int status){
        //update status of relevant outputs to be completed.
        StringBuilder sql = new StringBuilder("update output set deleted = ").append(status)
                .append(" where subject = '").append(cmbCurTable.getSelectedItem())
                .append("' and contactID = ").append(getCurBillIndex())
                .append(" and (deleted is null or deleted < ").append(DBConsts.COMPLETED).append(")");
        try {
            AikaPos.db.execSQL(sql.toString());
        } catch (Exception e) {
            L.e("BarFrame", "unexpected error occured whenn updating output status.", e);
        }
    }
}
