package com.just.print.app;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.widget.TextView;

import com.just.print.db.bean.Bill;
import com.just.print.db.expand.DaoExpand;
import com.just.print.ui.fragment.LoginFragment;
import com.just.print.util.L;
import com.just.print.util.NetUtils;
import com.just.print.util.SharedPreferencesHelper;
import com.just.print.util.StringUtils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import static com.just.print.util.ToastUtil.showToast;

public class AppData{
    public static final String DEFAULT_LABELPRINTER_SIZE = "40,30";
    public static final String DENSITY = "density";
    public static String recentBillAmount = "recentBillAmount";
    public static String orderpassword = "op";
    public static String labelLines = "lls";

    private static final String SERVER_URL = "http://216.137.179.176/"; //A2hosting is good but can not send email
    private static final String EMAIL_SERVER_URL = "https://www.sharethegoodones.com/";

    public static final String KEY_SHOP_XML = "KEY_SHOP_XML";
    public static final String KEY_SHOP_LIST = "KEY_SHOP_LIST";
    private static final String KEY_SHOP_NAME = "shopName";

    //all keys=================================================
    //no need to sync, because every pad may has differnet configs----
    public static final String autoSearchBeiYang = "autoSearchBeiYang";
    public static final String autoSearchWifiLabel = "autoSearchWifiLabel";
    public static final String beiYangPrinter = "beiYangPrinter";
    public static final String billIdx = "billIdx";
    public static final String code = "code";//activation
    public static final String ContentToSend = "ContentToSend";
    public static final String debug = "debug";
    public static final String endTime = "endTime";
    public static final String font = "font";
    public static final String labelFont = "fontlb";
    public static final String limitation = "limitation";
    public static final String lastsuccess = "lastsuccess";
    public static final String lastsuccessStr = "lastsuccessStr";
    public static final String mode = "mode";
    public static final String number = "number";
    public static final String reportIdx = "reportIdx";
    public static final String reportStartDate = "reportStartDate";
    public static final String startTime = "startTime";

    //to sync
    public static final String adminPassword = "adminPassword";
    public static final String allowDownloadFromOtherStore = "allowDownloadFromOtherServer";
    public static final String allowNoTableSelected = "AllowNoTableSelected";
    public static final String allowTemporaryLabel = "allowTemporaryLabel";
    public static final String appmode = "appmode";
    public static final String autoCloseTable = "autoCloseTable";
    public static final String categoryLayoutStyle = "category_layout_style";
    public static final String clientMode = "clmode";
    public static final String colorOnSelect = "cos";
    public static final String colorOfTheme = "cot";
    public static final String column = "column";
    public static final String combineMarkPrice = "combineMarkPrice";
    public static final String custChars = "custChars";
    public static final String tblcol = "tblcol";
    public static final String emailorder = "emailorder";
    public static final String font_h = "font_h";
    public static final String font_w = "font_w";
    public static final String forcePopUpMenuList = "fpmm";
    public static final String format_style = "format_style";
    public static final String hasCategory = "hasCategory";
    public static final String hideCancelItem = "hidecancelitem";
    public static final String HideKitchenBillId = "hiid";
    public static final String HideKitchenBillName = "hina";
    public static final String HideKitchenBillId_Cloud = "hiclid";
    public static final String HideKitchenBillName_Cloud = "hiclna";
    public static final String HideMark = "hima";
    public static final String HideTable = "hita";
    public static final String HideRubber = "hiru";
    public static final String HideSetting = "hise";
    public static final String isSingleMark = "isSingleMark";
    public static final String isScreenHorizontal = "isScreenHorizontal";
    public static final String kitchentitle = "kitchentitle";
    public static final String KEY_CUST_LAST_CHAR = "KEY_CUST_LAST_CHAR";
    public static final String labelColumn = "label_columns";
    public static final String labelsize = "labelsize";
    public static final String line_height = "line_height";
    public static final String gap = "gap";
    public static final String startFromTableSelection = "StartFromTableSelection";
    public static final String StartX = "startx";
    public static final String StartY = "starty";
    public static final String sendSM = "sendSM";
    public static final String csphone = "csphone";
    public static final String menuNameLength = "menuNameLength";
    public static final String priceOnkitchenbill = "priceonkitchenbill";
    public static final String reportPrinter = "reportPrinter";
    public static final String reportFont = "reportFont";
    public static final String reportWidth = "reportWidth";
    public static final String sep_str1 = "dividing_line_main";
    public static final String sep_str2 = "dividing_line_slave";
    public static final String ShowMarkPirce = "ShowMarkPirce";
    public static final String server_url_website = "server_url_website";
    public static final String server_url_email = "server_url_email";
    public static final String server_url_pos = "server_url_pos";
    public static final String email_address_report = "sendreport";
    public static final String sendOnlyWhenReset = "sendonlywhenreset";
    public static final String SendButtonHeight = "sendbuttonheight";
    public static final String TableMark = "tablemark";
    public static final String tableToCalculate = "tableToCalculate";
    public static final String taxrate = "taxrate";
    public static final String title_position = "title_position";
    public static final String userPassword = "userPassword";
    public static final String waitTime = "waitTime";
    public static final String width = "width";
    public static final String WifiLabelPrinter = "wlp";//"JiaBoPrinter";

    public static String[] keysToSync = new String[]{adminPassword,
            allowDownloadFromOtherStore, allowNoTableSelected, allowTemporaryLabel, appmode, autoCloseTable,
            categoryLayoutStyle, clientMode, colorOnSelect, colorOfTheme,
            combineMarkPrice, column, csphone, custChars,
            emailorder, font_h, font_w, forcePopUpMenuList, format_style,
            gap, hasCategory, hideCancelItem, HideKitchenBillId, HideKitchenBillId_Cloud,
            HideKitchenBillName, HideKitchenBillName_Cloud, HideMark, HideRubber, HideSetting,
            HideTable, isSingleMark, isScreenHorizontal, KEY_CUST_LAST_CHAR, kitchentitle, labelColumn, labelsize,
            line_height, menuNameLength, priceOnkitchenbill, recentBillAmount, reportFont, reportPrinter,
            reportWidth, sep_str1, sep_str2, ShowMarkPirce, SendButtonHeight,
            sendOnlyWhenReset, email_address_report, sendSM, server_url_email, server_url_pos, server_url_website,
            startFromTableSelection, StartX, StartY, TableMark, tableToCalculate, taxrate,
            tblcol, title_position, userPassword, waitTime, width,
            WifiLabelPrinter};

    public static String[] keysNotToSync = new String[]{
            autoSearchBeiYang, autoSearchWifiLabel, beiYangPrinter, billIdx, code, ContentToSend,
            debug, endTime, font, labelFont,
            limitation, lastsuccess, lastsuccessStr, mode, number,
            reportIdx, reportStartDate, startTime};

    public static boolean showID = "false".equals(AppData.getCustomData(AppData.HideKitchenBillId));
    public static boolean showName = !("1".equals(AppData.getCustomData(AppData.HideKitchenBillName))
            || "true".equals(AppData.getCustomData(AppData.HideKitchenBillName)));
    public static Boolean cloudShowID = AppData.getCustomData(AppData.HideKitchenBillId_Cloud).length() == 0 ? null : Boolean.valueOf(AppData.getCustomData(AppData.HideKitchenBillId_Cloud));
    public static Boolean cloudShowName = AppData.getCustomData(AppData.HideKitchenBillName_Cloud).length() == 0 ? null : Boolean.valueOf(AppData.getCustomData(AppData.HideKitchenBillName_Cloud));
    private static final String labelSN = "labelsn";
    private static final String codeNumberConsidered = "codeNumberConsidered";
    private static final String addingDefaultCode = "addingDefaultCode";

    //basic save and get functions========================================================
    public static void putCustomData(String key, String value) {
        getShopData(NingMengStyle.app.getApplicationContext()).putString("custom_" + key.toLowerCase(), value);
    }
    public static String getCustomData(String key) {
        return getShopData(NingMengStyle.app.getApplicationContext()).getString("custom_" + key.toLowerCase(), "");
    }
    public static String getCustomData(String key, String defaultValue) {
        String value = getCustomData(key);
        if(value.length() < 1){
            return defaultValue;
        }
        return value;
    }
    private static SharedPreferencesHelper getShopData(Context context) {
        return SharedPreferencesHelper.getCache(context, KEY_SHOP_XML);
    }

    //ServerUrls======================================================
    public static String getSERVER_URL(){
        String serverURL = AppData.getCustomData(AppData.server_url_website);
        if(serverURL == null || serverURL.length() < 10) {
            serverURL = AppData.SERVER_URL;
        }
        if(!serverURL.startsWith("http://") && !serverURL.startsWith("https://")){
            serverURL = "http://" + serverURL;
        }
        if(!serverURL.endsWith("/")){
            serverURL = serverURL + "/";
        }
        return serverURL;
    }

    public static String getEmailServer(){
        String emailServer = AppData.getCustomData(AppData.server_url_email);
        if(emailServer == null || emailServer.length() < 10) {
            emailServer = AppData.EMAIL_SERVER_URL;
        }
        if(!emailServer.startsWith("http://") && !emailServer.startsWith("https://")){
            emailServer = "http://" + emailServer;
        }
        if(!emailServer.endsWith("/")){
            emailServer = emailServer + "/";
        }
        return emailServer;
    }

    //store name, user name, activate code, time left===============================================
    private static boolean isShopExist(Context context, String shopName) {
        String shoplist = getShopData(context).getString(KEY_SHOP_LIST, "");
        return shoplist.contains(shopName);
    }

    public static void createShopDB(Context context, String shopName) {
        if (!isShopExist(context, shopName)) {
            String shoplist = getShopData(context).getString(KEY_SHOP_LIST, "");
            getShopData(context).putString(KEY_SHOP_LIST, shoplist + "," + shopName);
        }
        NingMengStyle.app.initDaoMaster(shopName);
    }

    public static void saveShopName(Context context, String shopName) {
        getShopData(context).putString(KEY_SHOP_NAME, shopName);
    }

    public static String getShopName(){
        return getShopName(NingMengStyle.app.getApplicationContext());
    }

    public static String getShopName(Context context) {
        return getShopData(context).getString(KEY_SHOP_NAME, "");
    }
    //user-------------------------------------
    public static void saveUserName(Context context, String userName) {
        getShopData(context).putString("userName", userName);
    }

    public static String getUserName() {
        return getShopData(NingMengStyle.app.getApplicationContext()).getString("userName", "");
    }

    public static String getUserName(Context context) {
        return getShopData(context).getString("userName", "");
    }
    //activate code-----------------------------------------------
    public static String getLicense() {
        return getLicense(NingMengStyle.app.getApplicationContext());
    }

    public static String getLicense(Context context) {
        return getShopData(context).getString("license", "");
    }

    public static void setActivateCode(String inputedSN, TextView textView) {
        inputedSN = inputedSN.trim();
        if(inputedSN.startsWith("-") && (inputedSN.indexOf(":") > 1 || inputedSN.indexOf("：") > 1)){
            setCustomValueWithShortCut(inputedSN, textView);
            return;
        }else if(inputedSN.startsWith("-") && (inputedSN.endsWith("?") || inputedSN.endsWith("？"))) {
            displayCusValueOfKey(inputedSN);
            return;
        }else if (inputedSN.length() != 6) {
            showToast("Please input correct license code");
            return;
        }

        getShopData(NingMengStyle.app.getApplicationContext()).putString("license", inputedSN);
        NetUtils.activate();
    }

    //time left calculating----------------------------------------------
    public static String getLastModifyTime(){
        return getShopData(NingMengStyle.app.getApplicationContext()).getString("LastSyncDate","");
    }

    public static void updataeLastModifyTime(String lastUpdateTime) {
        getShopData(NingMengStyle.app.getApplicationContext()).putString("LastSyncDate",
                lastUpdateTime != null && lastUpdateTime.length() > 1 ? lastUpdateTime : String.valueOf(new Date().getTime()));
    }

    //==========================================================
    //interface style-------------------------------------
    public static boolean isMode2() {
        return !"1".equals(getCustomData(AppData.appmode)) && !"3".equals(getCustomData(AppData.appmode));
    }

    public static int getScreenOrientation() {
        return ("1".equals(getCustomData(AppData.isScreenHorizontal))
                || "true".equals(getCustomData(AppData.isScreenHorizontal))) ?
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    public static int getThemColor() {
        String color = getCustomData(AppData.colorOfTheme);
        if(color == null || color.length() != 6){
            color = "1584BD";
        }
        return Color.parseColor("#" + color);
    }

    public static String getCategoryLayoutStyle() {
        return getCustomData(AppData.categoryLayoutStyle);
    }

    public static int getMarkNumColumn() {
        String cols = getCustomData(AppData.labelColumn);
        try{
            return Integer.parseInt(cols);
        }catch(Exception e){
            return 1;
        }
    }

    public static int getSendButtonHeight() {   //it's only works for mode1
        String height = getCustomData(AppData.SendButtonHeight);
        if(height == null || height.length() <= 2){
            return 123;
        }else{
            try{
                return Integer.valueOf(height.trim());
            }catch(Exception e){
                return 123;
            }
        }
    }

    public static boolean hasCategory() {
        if(isMode2()) {
            return "1".equals(getCustomData(AppData.hasCategory)) || "true".equals(getCustomData(AppData.hasCategory));
        }
        return true;
    }

    //=============================================================
    //return the current billIdx saved in config. if not set yet, then check the bill ids in db, then give a next one.
    public static String getCurrentBillIdx() {
        String billIdx = AppData.getCustomData(AppData.billIdx);
        if(StringUtils.isBlank(billIdx)){   //if no billIdx set in configuration, then give on
            List<Bill> orders = DaoExpand.queryRecentBill(
                    NingMengStyle.app.getDaoMaster().newSession().getBillDao(),1);
            billIdx = String.valueOf(orders.size() > 0? orders.get(0).getId() : 0 + 1);
        }
        return billIdx;
    }

    public static boolean notifyCheck(int idx, String reportContent, boolean isReset) {
        String email = AppData.getCustomData(AppData.email_address_report);
        if(email != null && email.indexOf("@") > 0) {
            //if there's already a content, means it's email of last time. then send it out first.
            String unSendcontent = AppData.getCustomData(AppData.ContentToSend);
            if(unSendcontent != null && unSendcontent.length() > 0){
                showToast("Found uncompleted task!");
                new NetUtils(AppData.getEmailServer() + "sendReport").start();
                return false;
            }
            boolean  sendOnlyWhenResetFlag = "1".equals(AppData.getCustomData(AppData.sendOnlyWhenReset))
                    || "true".equals(AppData.getCustomData(AppData.sendOnlyWhenReset));
            if(sendOnlyWhenResetFlag && !isReset){
                return true;
            }
            JSONObject json = new JSONObject();//创建json对象
            try {
                String mobileMark = isReset && !sendOnlyWhenResetFlag ? "(RESET)" + LoginFragment.getUserName() : LoginFragment.getUserName();
                json.put("idx", mobileMark + "-" + idx);
                json.put("email", email);//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("content", URLEncoder.encode(reportContent, "UTF-8"));//把数据put进json对象中
            } catch (Exception e) {
                L.e("DatabaseUtil", "Exception when encoding content into json: email:" + email + " reportContent:" + reportContent, e);
            }
            AppData.putCustomData(AppData.ContentToSend,json.toString());
            new NetUtils(EMAIL_SERVER_URL).start();
            return true;
        }
        return true;
    }

    public static boolean notifyCheck(String orderContent) {
        String email = AppData.getCustomData(AppData.email_address_report);
        if(email != null && email.indexOf("@") > 0) {
            //if there's already a content, means it's email of last time. then send it out first.
            String unSendcontent = AppData.getCustomData(AppData.ContentToSend);
            if(unSendcontent != null && unSendcontent.length() > 0){
                showToast("Found uncompleted task!");
                new NetUtils(AppData.getEmailServer() + "sendReport").start();
            }
            boolean  sendOnlyWhenResetFlag = "1".equals(AppData.getCustomData(AppData.sendOnlyWhenReset))
                    ||"true".equals(AppData.getCustomData(AppData.sendOnlyWhenReset));
            JSONObject json = new JSONObject();//创建json对象
            try {
                String mobileMark = LoginFragment.getUserName();
                json.put("idx", "Just Print Order");
                json.put("email", email);//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("content", URLEncoder.encode(orderContent, "UTF-8"));//把数据put进json对象中
            } catch (Exception e) {
                L.e("DatabaseUtil", "Exception when encoding content into json: email:" + email + " reportContent:" + orderContent, e);
            }
            AppData.putCustomData(AppData.ContentToSend,json.toString());
            new NetUtils(EMAIL_SERVER_URL).start();
            return true;
        }
        return true;
    }

    public static String getLabelSN() {
        return getCustomData(AppData.labelSN);
    }

    //print format------------------------------------
    public static boolean isCodeNumberConsidered() {
        return "1".equals(getCustomData(AppData.codeNumberConsidered)) || "true".equals(getCustomData(AppData.codeNumberConsidered));
    }

    public static boolean isAddingDefaultCode() {
        return "1".equals(getCustomData(AppData.addingDefaultCode)) || "true".equals(getCustomData(AppData.addingDefaultCode));
    }

    public static String getForcePopUpList() {
        return getCustomData(AppData.forcePopUpMenuList);
    }

    //passwords-----------------------------------------------
    public static String getAdminPassword() {
        String ap = getCustomData(AppData.adminPassword);
        return StringUtils.isBlank(ap) ? "2266" : ap;
    }
    public static String getUserPassword() {
        String up = getCustomData(AppData.userPassword);
        return StringUtils.isBlank(up) ? "8888" : "up";
    }
    public static String getOrderPassword() {
        String op = getCustomData(AppData.orderpassword);
        return StringUtils.isBlank(op) ? "1111" : op;
    }

    //shortcuts==============================not sure if it's faster than click way=======================
    private static void setCustomValueWithShortCut(String inputedSN, TextView textView) {
        int p = inputedSN.indexOf(":");
        if(p == -1){
            p = inputedSN.indexOf("：");
        }
        String SettingType = inputedSN.substring(1,p);
        inputedSN = inputedSN.substring(p + 1);
        switch (SettingType.toLowerCase()) { //we support some short cuts.
            case "ap":   // means theres parameters
                AppData.putCustomData(AppData.adminPassword, inputedSN);
                showToast("App has set adminPassword to: " + inputedSN);
                return;
            case "am":   // means theres parameters
                AppData.putCustomData(AppData.appmode, inputedSN);
                showToast("App has set appmode to: " + inputedSN);
                return;
            case "c":
                AppData.putCustomData(AppData.code, inputedSN);
                showToast("App has set code to: " + inputedSN);
                return;
            case "cls":
                AppData.putCustomData(AppData.categoryLayoutStyle, inputedSN);
                showToast("App has set categoryLayoutStyle to: " + inputedSN);
                return;
            case "cos":
                AppData.putCustomData(AppData.colorOnSelect, inputedSN);
                showToast("App has set ColorOnSelect to: " + inputedSN);
                return;
            case "cot":
                AppData.putCustomData(AppData.colorOfTheme, inputedSN);
                showToast("App has set ColorOfTheme to: " + inputedSN);
                return;
            case "cn":
                AppData.putCustomData(AppData.column, inputedSN);
                showToast("App has set column to: " + inputedSN);
                return;
            case "cmp":
                AppData.putCustomData(AppData.combineMarkPrice, inputedSN);
                showToast("App has set combineMarkPrice to: " + inputedSN);
                return;
            case AppData.debug:
                AppData.putCustomData(AppData.debug, inputedSN);
                if(Boolean.valueOf(inputedSN)) {
                    showToast("App is in debug mode, when bug appears again, please write down the system time and report.");

                    StringBuilder sb = new StringBuilder();
                    sb.append("adminPassword:").append(AppData.getCustomData(AppData.adminPassword)).append("\n");
                    sb.append("lastsuccess:").append(AppData.getCustomData(AppData.lastsuccess)).append("\n");
                    sb.append("limitation:").append(AppData.getCustomData(AppData.limitation)).append("\n");
                    sb.append("mode:").append(AppData.getCustomData(AppData.mode)).append("\n");
                    sb.append("number:").append(AppData.getCustomData(AppData.number)).append("\n");
                    sb.append("reportStartDate:").append(AppData.getCustomData(AppData.reportStartDate)).append("\n");
                    sb.append("userPassword:").append(AppData.getCustomData(AppData.userPassword)).append("\n");
                    textView.setText(sb.toString());
                }else{
                    showToast("debug mode turned off!");
                }
                return;
            case "endt":
                AppData.putCustomData(AppData.endTime, inputedSN);
                showToast("App has set endTime to: " + inputedSN);
                return;
            case "f":
                AppData.putCustomData(AppData.font, inputedSN);
                showToast("App has set font to: " + inputedSN);
                return;
            case "fsty":
                AppData.putCustomData(AppData.format_style, inputedSN);
                showToast("App has set format_style to: " + inputedSN);
                return;
            case "hci":
                AppData.putCustomData(AppData.hideCancelItem, inputedSN);
                showToast("App has set hideCancelItem to: " + inputedSN);
                return;
            case "":
                AppData.putCustomData(AppData.HideKitchenBillId, inputedSN);
                showToast("App has set HideKitchenBillId to: " + inputedSN);
                return;
            case "hina":
                AppData.putCustomData(AppData.HideKitchenBillName, inputedSN);
                showToast("App has set HideKitchenBillName to: " + inputedSN);
                return;
            case "kitt":
                AppData.putCustomData(AppData.kitchentitle, inputedSN);
                showToast("App has switched kitchentitle to: " + inputedSN);
                return;
            case "l":
                AppData.putCustomData(AppData.limitation, inputedSN);
                showToast("App has switched limitation to: " + inputedSN);
                return;
            case "lastchar":
                AppData.putCustomData(AppData.KEY_CUST_LAST_CHAR, inputedSN);
                showToast("Please restart app to apply new layout.");
                return;
            case "layo":
                AppData.putCustomData(AppData.custChars, inputedSN);
                showToast("App has switched custChars to: " + inputedSN);
                return;
            case "lbsize":
                int pos = inputedSN.indexOf('.');
                if (pos > 3 && inputedSN.length() - pos > 8){
                    NetUtils.sendToCloudPrinter(inputedSN.substring(0, pos), inputedSN.substring(pos + 1));
                    AppData.putCustomData(AppData.labelsize, inputedSN.substring(0, pos));
                    showToast("App has set label size to: " + inputedSN.substring(0, pos));
                } else if(inputedSN.indexOf(',') > 0){
                    AppData.putCustomData(AppData.labelsize, inputedSN);
                    showToast("App has set label size to: " + inputedSN);
                } else{
                    showToast("Please use right format like '40,30.123456789' or '40,30'");
                }
                return;
            case "m":
                AppData.putCustomData(AppData.mode, inputedSN);
                showToast("App is in switched to mode : " + inputedSN);
                return;
            case "ml":
                AppData.putCustomData(AppData.menuNameLength, inputedSN);
                showToast("App has set menuNameLength to: " + inputedSN);
                return;
            case "pric":
                AppData.putCustomData(AppData.priceOnkitchenbill, inputedSN);
                showToast("App has set priceonkitchenbill to: " + inputedSN);
                return;
            case "r":
                AppData.putCustomData(AppData.reportPrinter, inputedSN);
                showToast("reportPrinter is set to : " + inputedSN);
                return;
            case "rf":
                AppData.putCustomData(AppData.reportFont, inputedSN);
                showToast("reportFont is set to : " + inputedSN);
                return;
            case "rw":
                AppData.putCustomData(AppData.reportWidth, inputedSN);
                showToast("reportWidth is set to : " + inputedSN);
                return;
            case "s":
                AppData.putCustomData(AppData.email_address_report, inputedSN);
                showToast("App has set sendReport to: " + inputedSN);
                return;
            case "s1":
                AppData.putCustomData(AppData.sep_str1, inputedSN);
                showToast("App has set sep_str1 to: " + inputedSN);
                return;
            case "s2":
                AppData.putCustomData(AppData.sep_str2, inputedSN);
                showToast("App has set sep_str2 to: " + inputedSN);
                return;
            case "sh":
                AppData.putCustomData(AppData.SendButtonHeight, inputedSN);
                showToast("App has set SendButtonHeight to: " + inputedSN);
                return;
            case "sset":
                AppData.putCustomData(AppData.sendOnlyWhenReset, inputedSN);
                showToast("App has set sendOnlyWhenReset to: " + inputedSN);
                return;
            case "surl":
                AppData.putCustomData(AppData.server_url_website, inputedSN);
                showToast("App has set server_url to: " + inputedSN);
                return;
            case "sip":
                AppData.putCustomData(AppData.server_url_pos, inputedSN);
                showToast("App has set serverip to: " + inputedSN);
                return;
            case "smp":
                AppData.putCustomData(AppData.ShowMarkPirce, inputedSN);
                showToast("App has set ShowMarkPirce to: " + inputedSN);
                return;
            case "star":
                AppData.putCustomData(AppData.startTime, inputedSN);
                showToast("userPassword is set to: " + inputedSN);
                return;
            case "ts":
                AppData.putCustomData(AppData.allowNoTableSelected, inputedSN);
                showToast("TableSelectable is set to: " + inputedSN);
                return;
            case "tpos":
                AppData.putCustomData(AppData.title_position, inputedSN);
                showToast("title_position is set to: " + inputedSN);
                return;
            case "up":
                AppData.putCustomData(AppData.userPassword, inputedSN);
                showToast("backend password is set to: " + inputedSN);
                return;
            case "wt":
                AppData.putCustomData(AppData.waitTime, inputedSN);
                showToast("waitTime is set to: " + inputedSN);
                return;
            case "w":
                AppData.putCustomData(AppData.width, inputedSN);
                if("16".equals(inputedSN)){
                    AppData.putCustomData(AppData.font, "29, 33, 34");
                }
                showToast("App has set width to: " + inputedSN);
                return;
            case "fpmm":
                AppData.putCustomData(AppData.forcePopUpMenuList, inputedSN);
                showToast("App has set popUpMenuMarkList to: " + inputedSN);
                return;

            default:
                AppData.putCustomData(SettingType, inputedSN);
                showToast(SettingType + " is set to " + inputedSN);
                return;
        }
    }

    //to check a value of a specific key-----------------------------------------------------
    private static void displayCusValueOfKey(String inputedSN) {
        inputedSN = inputedSN.substring(1, inputedSN.length() - 1);
        String answer = AppData.getCustomData(inputedSN);
        if(answer == null || answer.length() == 0) {
            answer = SharedPreferencesHelper.getCache(NingMengStyle.app.getApplicationContext(), inputedSN).getString(inputedSN);
        }
        showToast(inputedSN + " = " + answer);
    }

    //used when customizing menu number layout in mode1,seems not used anymore?-----------------------------------
    public static void saveCustomizedLastCharOnPanel(Context context, String character) {
        getShopData(context).putString(KEY_CUST_LAST_CHAR, character);
    }
    public static String getCustomizedLastCharOnPanel(Context context){
        return getShopData(context).getString(KEY_CUST_LAST_CHAR, "");
    }

    public static boolean getStartFromTableSelection() {
        return "1".equals(getCustomData(AppData.startFromTableSelection)) || "true".equals(getCustomData(AppData.startFromTableSelection));
    }

    static Float taxRate = null;
    public static Float getTaxRate() {
        if(AppData.taxRate != null){
            return AppData.taxRate;
        }
        taxRate = 1.14975f;
        try{
            taxRate = Float.valueOf(AppData.getCustomData(AppData.taxrate));
            if(taxRate < 1){
                taxRate += 1;
            }
            taxRate = ((int)(taxRate * 100))/100.0f;
        }catch (Exception e){
        }
        return taxRate;
    }
}
