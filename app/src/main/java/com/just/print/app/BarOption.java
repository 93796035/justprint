package com.just.print.app;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.widget.TextView;

import com.just.print.db.bean.Bill;
import com.just.print.db.expand.DaoExpand;
import com.just.print.ui.fragment.RegisterFragment;
import com.just.print.util.L;
import com.just.print.util.NetUtils;
import com.just.print.util.SharedPreferencesHelper;
import com.just.print.util.StringUtils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import static com.just.print.util.ToastUtil.showToast;

public class BarOption {
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
    public static final String colorOfTheme2 = "cot2";
    public static final String combineMarkPrice = "combineMarkPrice";
    public static final String custChars = "custChars";
    public static final String custTogos = "custTogos";
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
    public static final String labelColumn = "label_columns";
    public static final String labelPrinters = "labelPrinters";//lan printer is currently using JiaBoPrinter;
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

    public static String[] keysToSync = new String[]{adminPassword,
            allowDownloadFromOtherStore, allowNoTableSelected, allowTemporaryLabel, appmode, autoCloseTable,
            categoryLayoutStyle, clientMode, colorOnSelect, colorOfTheme,
            combineMarkPrice, column, csphone, custChars, custTogos,
            emailorder, font_h, font_w, forcePopUpMenuList, format_style,
            gap, hasCategory, hideCancelItem, HideKitchenBillId, HideKitchenBillId_Cloud,
            HideKitchenBillName, HideKitchenBillName_Cloud, HideMark, HideRubber, HideSetting,
            HideTable, isSingleMark, isScreenHorizontal, kitchentitle,
            labelPrinters, labelColumn, labelsize,
            line_height, menuNameLength, priceOnkitchenbill, recentBillAmount, reportFont, reportPrinter,
            reportWidth, sep_str1, sep_str2, ShowMarkPirce, SendButtonHeight,
            sendOnlyWhenReset, email_address_report, sendSM, server_url_email, server_url_pos, server_url_website,
            startFromTableSelection, StartX, StartY, TableMark, tableToCalculate, taxrate,
            tblcol, title_position, userPassword, waitTime, width};

    public static String[] keysNotToSync = new String[]{
            autoSearchBeiYang, autoSearchWifiLabel, beiYangPrinter, billIdx, code, ContentToSend,
            debug, endTime, font, labelFont,
            limitation, lastsuccess, lastsuccessStr, mode, number,
            reportIdx, reportStartDate, startTime};

    public static boolean showID = !"true".equals(BarOption.getCustomData(BarOption.HideKitchenBillId));//show id by default
    public static boolean showName = !("1".equals(BarOption.getCustomData(BarOption.HideKitchenBillName))
            || "true".equals(BarOption.getCustomData(BarOption.HideKitchenBillName)));                  //don't show by default.
    public static Boolean cloudHideID = Boolean.valueOf(BarOption.getCustomData(BarOption.HideKitchenBillId_Cloud).length() == 0 ?
            (BarOption.getCustomData(BarOption.HideKitchenBillId).length() == 0 ?
                    String.valueOf(!BarOption.showID)
                    : BarOption.getCustomData(BarOption.HideKitchenBillId))
            : BarOption.getCustomData(BarOption.HideKitchenBillId_Cloud));
    public static Boolean cloudHideName = Boolean.valueOf(BarOption.getCustomData(BarOption.HideKitchenBillName_Cloud).length() == 0 ?
            (BarOption.getCustomData(BarOption.HideKitchenBillName).length() == 0 ?
                    String.valueOf(!BarOption.showName)
                    : BarOption.getCustomData(BarOption.HideKitchenBillName))
            : BarOption.getCustomData(BarOption.HideKitchenBillName_Cloud));
    private static final String codeNumberConsidered = "codeNumberConsidered";
    private static final String addingDefaultCode = "addingDefaultCode";

    //basic save and get functions========================================================
    public static void putCustomData(String key, String value) {
        getShopData(AikaStyleApplication.instance.getApplicationContext()).putString("custom_" + key.toLowerCase(), value);
    }
    public static String getCustomData(String key) {
        return getShopData(AikaStyleApplication.instance.getApplicationContext()).getString("custom_" + key.toLowerCase(), "");
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
        String serverURL = BarOption.getCustomData(BarOption.server_url_website);
        if(serverURL == null || serverURL.length() < 10) {
            serverURL = BarOption.SERVER_URL;
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
        String emailServer = BarOption.getCustomData(BarOption.server_url_email);
        if(emailServer == null || emailServer.length() < 10) {
            emailServer = BarOption.EMAIL_SERVER_URL;
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
    public static void saveShopName(Context context, String shopName) {
        getShopData(context).putString(KEY_SHOP_NAME, shopName);
    }

    public static String getShopName(){
        return getShopName(AikaStyleApplication.instance.getApplicationContext());
    }

    public static String getShopName(Context context) {
        return getShopData(context).getString(KEY_SHOP_NAME, "");
    }
    //user-------------------------------------
    public static void saveUserName(Context context, String userName) {
        getShopData(context).putString("userName", userName);
    }

    public static String getUserName() {
        return getShopData(AikaStyleApplication.instance.getApplicationContext()).getString("userName", "");
    }

    public static String getUserName(Context context) {
        return getShopData(context).getString("userName", "");
    }
    //activate code-----------------------------------------------
    public static String getLicense() {
        return getLicense(AikaStyleApplication.instance.getApplicationContext());
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

        getShopData(AikaStyleApplication.instance.getApplicationContext()).putString("license", inputedSN);
        NetUtils.activate();
    }

    //time left calculating----------------------------------------------
    public static String getLastModifyTime(){
        return getShopData(AikaStyleApplication.instance.getApplicationContext()).getString("LastSyncDate","");
    }

    public static void updataeLastModifyTime(String lastUpdateTime) {
        getShopData(AikaStyleApplication.instance.getApplicationContext()).putString("LastSyncDate",
                lastUpdateTime != null && lastUpdateTime.length() > 1 ? lastUpdateTime : String.valueOf(new Date().getTime()));
    }

    //==========================================================
    //interface style-------------------------------------
    public static boolean isMode2() {
        return !"1".equals(getCustomData(BarOption.appmode)) && !"3".equals(getCustomData(BarOption.appmode));
    }

    public static int getScreenOrientation() {
        return (StringUtils.isBlank(getCustomData(BarOption.isScreenHorizontal))
                ||"1".equals(getCustomData(BarOption.isScreenHorizontal))
                || "true".equals(getCustomData(BarOption.isScreenHorizontal))) ?
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    public static int getThemColor() {
        String color = getCustomData(BarOption.colorOfTheme);
        if(color == null || color.length() != 6){
            color = "5aacdb";
        }
        return Color.parseColor("#" + color);
    }

    public static int getThemColor2() {
        String color = getCustomData(BarOption.colorOfTheme2);
        if(color == null || color.length() != 6){
            color = "EE4C20";
        }
        return Color.parseColor("#" + color);
    }
    public static String getCategoryLayoutStyle() {
        String flow = getCustomData(BarOption.categoryLayoutStyle);
        if(StringUtils.isBlank(flow)){
            flow = "flow";
        }
        return flow;
    }

    public static int getMarkNumColumn() {
        String cols = getCustomData(BarOption.labelColumn);
        try{
            return Integer.parseInt(cols);
        }catch(Exception e){
            return 1;
        }
    }

    public static int getSendButtonHeight() {   //it's only works for mode1
        String height = getCustomData(BarOption.SendButtonHeight);
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
            return "1".equals(getCustomData(BarOption.hasCategory)) || "true".equals(getCustomData(BarOption.hasCategory));
        }
        return true;
    }

    //=============================================================
    //return the current billIdx saved in config. if not set yet, then check the bill ids in db, then give a next one.
    public static String getCurrentBillIdx() {
        String billIdx = BarOption.getCustomData(BarOption.billIdx);
        if(StringUtils.isBlank(billIdx)){   //if no billIdx set in configuration, then give on
            List<Bill> orders = DaoExpand.queryRecentBill(
                    AikaStyleApplication.instance.getDaoMaster().newSession().getBillDao(),1);
            billIdx = String.valueOf(orders.size() > 0? orders.get(0).getId() : 0 + 1);
        }
        return billIdx;
    }

    public static boolean notifyCheck(int idx, String reportContent, boolean isReset) {
        String email = BarOption.getCustomData(BarOption.email_address_report);
        if(email != null && email.indexOf("@") > 0) {
            //if there's already a content, means it's email of last time. then send it out first.
            String unSendcontent = BarOption.getCustomData(BarOption.ContentToSend);
            if(unSendcontent != null && unSendcontent.length() > 0){
                showToast("Found uncompleted task!");
                new NetUtils(BarOption.getEmailServer() + "sendReport", unSendcontent).start();
                return false;
            }
            boolean  sendOnlyWhenResetFlag = "1".equals(BarOption.getCustomData(BarOption.sendOnlyWhenReset))
                    || "true".equals(BarOption.getCustomData(BarOption.sendOnlyWhenReset));
            if(sendOnlyWhenResetFlag && !isReset){
                return true;
            }
            JSONObject json = new JSONObject();//创建json对象
            try {
                String mobileMark = isReset && !sendOnlyWhenResetFlag ? "(RESET)" + RegisterFragment.getUserName() : RegisterFragment.getUserName();
                json.put("idx", mobileMark + "-" + idx);
                json.put("email", email);//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("content", URLEncoder.encode(reportContent, "UTF-8"));//把数据put进json对象中
            } catch (Exception e) {
                L.e("DatabaseUtil", "Exception when encoding content into json: email:" + email + " reportContent:" + reportContent, e);
            }
            BarOption.putCustomData(BarOption.ContentToSend,json.toString());
            new NetUtils(EMAIL_SERVER_URL, json.toString()).start();
            return true;
        }
        return true;
    }

    public static boolean notifyCheck(String orderContent) {
        String email = BarOption.getCustomData(BarOption.email_address_report);
        if(email != null && email.indexOf("@") > 0) {
            //if there's already a content, means it's email of last time. then send it out first.
            String unSendcontent = BarOption.getCustomData(BarOption.ContentToSend);
            if(unSendcontent != null && unSendcontent.length() > 0){
                showToast("Found uncompleted task!");
                new NetUtils(BarOption.getEmailServer() + "sendReport", unSendcontent).start();
            }
            boolean  sendOnlyWhenResetFlag = "1".equals(BarOption.getCustomData(BarOption.sendOnlyWhenReset))
                    ||"true".equals(BarOption.getCustomData(BarOption.sendOnlyWhenReset));
            JSONObject json = new JSONObject();//创建json对象
            try {
                String mobileMark = RegisterFragment.getUserName();
                json.put("idx", "Just Print Order");
                json.put("email", email);//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("content", URLEncoder.encode(orderContent, "UTF-8"));//把数据put进json对象中
            } catch (Exception e) {
                L.e("DatabaseUtil", "Exception when encoding content into json: email:" + email + " reportContent:" + orderContent, e);
            }
            BarOption.putCustomData(BarOption.ContentToSend,json.toString());
            new NetUtils(EMAIL_SERVER_URL, json.toString()).start();
            return true;
        }
        return true;
    }

    //print format------------------------------------
    public static boolean isCodeNumberConsidered() {
        return "1".equals(getCustomData(BarOption.codeNumberConsidered)) || "true".equals(getCustomData(BarOption.codeNumberConsidered));
    }

    public static boolean isAddingDefaultCode() {
        return "1".equals(getCustomData(BarOption.addingDefaultCode)) || "true".equals(getCustomData(BarOption.addingDefaultCode));
    }

    public static String getForcePopUpList() {
        return getCustomData(BarOption.forcePopUpMenuList);
    }

    //passwords-----------------------------------------------
    public static String getAdminPassword() {
        String ap = getCustomData(BarOption.adminPassword);
        return StringUtils.isBlank(ap) ? "2266" : ap;
    }
    public static String getUserPassword() {
        String up = getCustomData(BarOption.userPassword);
        return StringUtils.isBlank(up) ? "8888" : up;
    }
    public static String getOrderPassword() {
        String op = getCustomData(BarOption.orderpassword);
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
                BarOption.putCustomData(BarOption.adminPassword, inputedSN);
                showToast("App has set adminPassword to: " + inputedSN);
                return;
            case "am":   // means theres parameters
                BarOption.putCustomData(BarOption.appmode, inputedSN);
                showToast("App has set appmode to: " + inputedSN);
                return;
            case "c":
                BarOption.putCustomData(BarOption.code, inputedSN);
                showToast("App has set code to: " + inputedSN);
                return;
            case "cls":
                BarOption.putCustomData(BarOption.categoryLayoutStyle, inputedSN);
                showToast("App has set categoryLayoutStyle to: " + inputedSN);
                return;
            case "cos":
                BarOption.putCustomData(BarOption.colorOnSelect, inputedSN);
                showToast("App has set ColorOnSelect to: " + inputedSN);
                return;
            case "cot":
                BarOption.putCustomData(BarOption.colorOfTheme, inputedSN);
                showToast("App has set ColorOfTheme to: " + inputedSN);
                return;
            case "cn":
                BarOption.putCustomData(BarOption.column, inputedSN);
                showToast("App has set column to: " + inputedSN);
                return;
            case "cmp":
                BarOption.putCustomData(BarOption.combineMarkPrice, inputedSN);
                showToast("App has set combineMarkPrice to: " + inputedSN);
                return;
            case BarOption.debug:
                BarOption.putCustomData(BarOption.debug, inputedSN);
                if(Boolean.valueOf(inputedSN)) {
                    showToast("App is in debug mode, when bug appears again, please write down the system time and report.");

                    StringBuilder sb = new StringBuilder();
                    sb.append("adminPassword:").append(BarOption.getCustomData(BarOption.adminPassword)).append("\n");
                    sb.append("lastsuccess:").append(BarOption.getCustomData(BarOption.lastsuccess)).append("\n");
                    sb.append("limitation:").append(BarOption.getCustomData(BarOption.limitation)).append("\n");
                    sb.append("mode:").append(BarOption.getCustomData(BarOption.mode)).append("\n");
                    sb.append("number:").append(BarOption.getCustomData(BarOption.number)).append("\n");
                    sb.append("reportStartDate:").append(BarOption.getCustomData(BarOption.reportStartDate)).append("\n");
                    sb.append("userPassword:").append(BarOption.getCustomData(BarOption.userPassword)).append("\n");
                    textView.setText(sb.toString());
                }else{
                    showToast("debug mode turned off!");
                }
                return;
            case "endt":
                BarOption.putCustomData(BarOption.endTime, inputedSN);
                showToast("App has set endTime to: " + inputedSN);
                return;
            case "f":
                BarOption.putCustomData(BarOption.font, inputedSN);
                showToast("App has set font to: " + inputedSN);
                return;
            case "fpmm":
                BarOption.putCustomData(BarOption.forcePopUpMenuList, inputedSN);
                showToast("App has set popUpMenuMarkList to: " + inputedSN);
                return;
            case "fsty":
                BarOption.putCustomData(BarOption.format_style, inputedSN);
                showToast("App has set format_style to: " + inputedSN);
                return;
            case "hci":
                BarOption.putCustomData(BarOption.hideCancelItem, inputedSN);
                showToast("App has set hideCancelItem to: " + inputedSN);
                return;
            case "hiid":
                BarOption.putCustomData(BarOption.HideKitchenBillId, inputedSN);
                showToast("App has set HideKitchenBillId to: " + inputedSN);
                return;
            case "hina":
                BarOption.putCustomData(BarOption.HideKitchenBillName, inputedSN);
                showToast("App has set HideKitchenBillName to: " + inputedSN);
                return;
            case "kitt":
                BarOption.putCustomData(BarOption.kitchentitle, inputedSN);
                showToast("App has switched kitchentitle to: " + inputedSN);
                return;
            case "l":
                BarOption.putCustomData(BarOption.limitation, inputedSN);
                showToast("App has switched limitation to: " + inputedSN);
                return;
            case "lastchar":
                BarOption.putCustomData(BarOption.custTogos, inputedSN);
                showToast("Please restart app to apply new layout.");
                return;
            case "layo":
                BarOption.putCustomData(BarOption.custChars, inputedSN);
                showToast("App has switched custChars to: " + inputedSN);
                return;
            case "lbsize":
                int pos = inputedSN.indexOf('.');
                if (pos > 3 && inputedSN.length() - pos > 8){
                    NetUtils.sendToCloudPrinter(inputedSN.substring(0, pos), inputedSN.substring(pos + 1));
                    BarOption.putCustomData(BarOption.labelsize, inputedSN.substring(0, pos));
                    showToast("App has set label size to: " + inputedSN.substring(0, pos));
                } else if(inputedSN.indexOf(',') > 0){
                    BarOption.putCustomData(BarOption.labelsize, inputedSN);
                    showToast("App has set label size to: " + inputedSN);
                } else{
                    showToast("Please use right format like '40,30.123456789' or '40,30'");
                }
                return;
            case "m":
                BarOption.putCustomData(BarOption.mode, inputedSN);
                showToast("App is in switched to mode : " + inputedSN);
                return;
            case "ml":
                BarOption.putCustomData(BarOption.menuNameLength, inputedSN);
                showToast("App has set menuNameLength to: " + inputedSN);
                return;
            case "pric":
                BarOption.putCustomData(BarOption.priceOnkitchenbill, inputedSN);
                showToast("App has set priceonkitchenbill to: " + inputedSN);
                return;
            case "r":
                BarOption.putCustomData(BarOption.reportPrinter, inputedSN);
                showToast("reportPrinter is set to : " + inputedSN);
                return;
            case "rf":
                BarOption.putCustomData(BarOption.reportFont, inputedSN);
                showToast("reportFont is set to : " + inputedSN);
                return;
            case "rw":
                BarOption.putCustomData(BarOption.reportWidth, inputedSN);
                showToast("reportWidth is set to : " + inputedSN);
                return;
            case "s":
                BarOption.putCustomData(BarOption.email_address_report, inputedSN);
                showToast("App has set sendReport to: " + inputedSN);
                return;
            case "s1":
                BarOption.putCustomData(BarOption.sep_str1, inputedSN);
                showToast("App has set sep_str1 to: " + inputedSN);
                return;
            case "s2":
                BarOption.putCustomData(BarOption.sep_str2, inputedSN);
                showToast("App has set sep_str2 to: " + inputedSN);
                return;
            case "sh":
                BarOption.putCustomData(BarOption.SendButtonHeight, inputedSN);
                showToast("App has set SendButtonHeight to: " + inputedSN);
                return;
            case "sset":
                BarOption.putCustomData(BarOption.sendOnlyWhenReset, inputedSN);
                showToast("App has set sendOnlyWhenReset to: " + inputedSN);
                return;
            case "surl":
                BarOption.putCustomData(BarOption.server_url_website, inputedSN);
                showToast("App has set server_url to: " + inputedSN);
                return;
            case "sip":
                BarOption.putCustomData(BarOption.server_url_pos, inputedSN);
                showToast("App has set serverip to: " + inputedSN);
                return;
            case "smp":
                BarOption.putCustomData(BarOption.ShowMarkPirce, inputedSN);
                showToast("App has set ShowMarkPirce to: " + inputedSN);
                return;
            case "star":
                BarOption.putCustomData(BarOption.startTime, inputedSN);
                showToast("userPassword is set to: " + inputedSN);
                return;
            case "ts":
                BarOption.putCustomData(BarOption.allowNoTableSelected, inputedSN);
                showToast("TableSelectable is set to: " + inputedSN);
                return;
            case "tpos":
                BarOption.putCustomData(BarOption.title_position, inputedSN);
                showToast("title_position is set to: " + inputedSN);
                return;
            case "up":
                BarOption.putCustomData(BarOption.userPassword, inputedSN);
                showToast("backend password is set to: " + inputedSN);
                return;
            case "wt":
                BarOption.putCustomData(BarOption.waitTime, inputedSN);
                showToast("waitTime is set to: " + inputedSN);
                return;
            case "w":
                BarOption.putCustomData(BarOption.width, inputedSN);
                if("16".equals(inputedSN)){
                    BarOption.putCustomData(BarOption.font, "29, 33, 34");
                }
                showToast("App has set width to: " + inputedSN);
                return;
            case "wlp":
                BarOption.putCustomData(BarOption.labelPrinters, inputedSN);
                showToast("App has set labelCloudPrinterSN to: " + inputedSN);
                return;

            default:
                BarOption.putCustomData(SettingType, inputedSN);
                showToast(SettingType + " is set to " + inputedSN);
                return;
        }
    }

    //to check a value of a specific key-----------------------------------------------------
    private static void displayCusValueOfKey(String inputedSN) {
        inputedSN = inputedSN.substring(1, inputedSN.length() - 1);
        String answer = BarOption.getCustomData(inputedSN);
        if(answer == null || answer.length() == 0) {
            answer = SharedPreferencesHelper.getCache(AikaStyleApplication.instance.getApplicationContext(), inputedSN).getString(inputedSN);
        }
        showToast(inputedSN + " = " + answer);
    }

    //used when customizing menu number layout in mode1,seems not used anymore?-----------------------------------
    public static void saveCustomizedLastCharOnPanel(Context context, String character) {
        getShopData(context).putString(custTogos, character);
    }
    public static String getCustomizedLastCharOnPanel(Context context){
        return getShopData(context).getString(custTogos, "");
    }

    public static boolean getStartFromTableSelection() {
        return "1".equals(getCustomData(BarOption.startFromTableSelection)) || "true".equals(getCustomData(BarOption.startFromTableSelection));
    }

    static Float taxRate = null;
    public static Float getTaxRate() {
        if(BarOption.taxRate != null){
            return BarOption.taxRate;
        }
        taxRate = 1.14975f;
        try{
            taxRate = Float.valueOf(BarOption.getCustomData(BarOption.taxrate));
            if(taxRate < 1){
                taxRate += 1;
            }
            taxRate = ((int)(taxRate * 100))/100.0f;
        }catch (Exception e){
        }
        return taxRate;
    }

    public static String getCustomChars() {
        return getCustomData(BarOption.custChars, "ABC*DFH+");
    }

    public static String getCustomTogos() {
        return getCustomData(BarOption.custTogos, "Doda,Uber,Skip,Fant");
    }

    public static String getDensity() {
        String density = getCustomData(BarOption.DENSITY);
        if("".equals(density)){
            density = "4";
        }
        return density;
    }
}
