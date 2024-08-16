package com.just.print.sys.server;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.just.print.app.AikaPos;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.app.Options;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.Mark;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.ui.panel.OrderFragmentActivity;
import com.just.print.util.AppUtils;
import com.just.print.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

/**
 * currently includ
 * 1/sending new order to AikaPos,
 * 2/download new order of current store from stgo.
 */
public class DataSyncService extends BaseFragmentActivity implements Runnable{

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    private static DataSyncService dataSyncService;
    protected static String server_url_pos;
    protected static String stgoServerURL;
    protected static String ErrorLogURL;

    protected static String lastDownLoad = Options.getCustomData(Options.sendSM);
    public static String lastModifiedTime = Options.getLastModifyTime();
    public static Vector<String> debugMsgs = new Vector<String>();

    public ArrayList<SelectionDetail> lastSelection;
    public String tableName;
    public String billIndex;
    public Double totalPrice;
    public String openTime;
    private OrderFragmentActivity orderFragmentActivity;

    public static DataSyncService getInstance() {
        if(dataSyncService == null) {
            dataSyncService = new DataSyncService();
            server_url_pos = Options.getCustomData(Options.server_url_pos);
            stgoServerURL = Options.getSERVER_URL();
            ErrorLogURL = "https://team.sharethegoodones.com"; //not allowed to customize.
            Executors.newSingleThreadExecutor().execute(dataSyncService);
        }
        return dataSyncService;
    }

    private DataSyncService(){
    }

    @Override
    public void run() {     //must use bk, because if use CustomerSelection.getInstance().getSelectedDishes(), it might be cleaned before thread started.
        HttpURLConnection urlConnection = null;
        while(true) {
            //if serverIp set, send a request to AikaPos for latest menu
            // it shouldn't hurt to keep bothering the AikaPos，if there's connection issue, we can give alert immediately;//
            if(server_url_pos.length() > 8){
                try {
                    //=============================first job===================================================
                    urlConnection = NetUtils.prepareConnection("http://" + server_url_pos + "/AikaPos/synchronizeFromServer", "POST");
                    JSONObject json = new JSONObject();//创建json对象
                    //put in timestamp.------------------------
                    json.put("lastModifiedTime", lastModifiedTime);//if later then time on server, nothing happens, if late will get menu from server.
                    //load the new orders-----------------------
                    if (lastSelection != null && lastSelection.size() > 0) { //if there's order to send, then send.
                        json.put("totalPrice", totalPrice);
                        json.put("openTime", openTime);
                        json.put("waiter", Options.getUserName());
                        json.put("table", URLEncoder.encode(tableName, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                        json.put("billIndex", billIndex);//把数据put进json对象中
                        StringBuilder orderContent = new StringBuilder();
                        for (SelectionDetail selectionDetail : lastSelection) {
                            orderContent.append("DishStart:");
                            Dish menu = selectionDetail.getDish();
                            String categoryStr = menu.getCategory();
                            orderContent.append(URLEncoder.encode(categoryStr, "UTF-8")).append("\n");
                            orderContent.append(URLEncoder.encode(menu.getName1(), "UTF-8")).append("\n");
                            orderContent.append(selectionDetail.getDish().getPrice()).append("\n");
                            orderContent.append(selectionDetail.getDishNum()).append("\n");
                            List<Mark> marks = selectionDetail.getMarkList();
                            orderContent.append("MarkStart:");
                            for (Mark mark : marks) {
                                orderContent.append(URLEncoder.encode(mark.getName1(), "UTF-8")).append("\n");
                                orderContent.append(mark.getQt()).append("\n");     //number
                                orderContent.append(mark.getStatus()).append("\n");  //displayIndex
                                orderContent.append(mark.getVersion()).append("\n");//price
                            }
                            json.put("orderContent", orderContent.toString());
                        }
                    }
                    //check the response from server----------------------------------
                    NetUtils.writeOut(urlConnection, json.toString());//把JSON对象按JSON的编码格式转换为字符串
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
                        String jsonStr = NetUtils.readBackFromConnection(urlConnection);
                        jsonStr = new JSONObject(jsonStr).getString("body");
                        if(jsonStr.length() > 0) {
                            JSONArray jsonObject = new JSONArray(jsonStr);
//                            List<Printer> printers = absorbPrinters(new JSONArray((String)jsonObject.get(0)), printerDao);
//                            List<Mark> marks = absorbMarksAndTables(new JSONArray((String)jsonObject.get(1)), new JSONArray((String)jsonObject.get(2)));
//                            com.just.print.app.Options.putCustomData(com.just.print.app.Options.forcePopUpMenuList, autoPopUpMenus.toString());
                            Options.updataeLastModifyTime((String)jsonObject.get(5));
                        }
                        //add relation between dish and mark and printers
                        if(lastSelection != null) {
                            lastSelection.clear();
                        }
                    } else {
                        Log.e("L", "response code is:" + urlConnection.getResponseCode());
                    }
                } catch (Exception e) {
                    Log.e("L", "Exception happened when sending dishes to server: " + server_url_pos);//rjson={"json":true}
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                    }
                }
            }

            //===================Second Job==================================
            //I put the content download from stgo under sendSM flag. (when first use, should manually set sendSM to "true"
            //then every second will send message to stgo to ask for new order of it's store, if new order returned, then send SM.
            lastDownLoad = Options.getCustomData(Options.sendSM);
            if(lastDownLoad != null && lastDownLoad.length() > 0 && !"null".equalsIgnoreCase(lastDownLoad) && !"false".equalsIgnoreCase(lastDownLoad)){ //if the lastDownLoad
                // is set, then download new orders.
                String account = Options.getShopName();
                //download new records after last download time:
                try {
                    //TODO: this one should use "GET" as method, while don't know why it get can not reach back end from here. so I have to keep post for now.
                    urlConnection = NetUtils.prepareConnection(stgoServerURL + "requestNewOrders" + "/" + account + "/" + lastDownLoad, "POST");
                    JSONObject json = new JSONObject();//创建json对象
                    json.put("lastDownLoad", lastDownLoad);
                    json.put("shop",  account);
                    json.put("licence", Options.getLicense());
                    String newLastDownload = null;
                    NetUtils.writeOut(urlConnection, json.toString());//把JSON对象按JSON的编码格式转换为字符串
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
                        String rjson = NetUtils.readBackFromConnection(urlConnection);
                        //send sms
                        newLastDownload = prepareSMContent(rjson);
                    } else {
                        Log.e("L", "response code is:" + urlConnection.getResponseCode());
                    }
                    //put it into config, so the data still there even the app closed.next start up will keep working on it.
                    Options.putCustomData(Options.sendSM, newLastDownload != null ? newLastDownload : lastDownLoad);
                } catch (Exception e) {
                    Log.e("L", "Exception happened when requestNewOrders from server: " + e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                    }
                }
            }

            //third job, sync error messages======================================
            if(debugMsgs.size() > 0) {
                String msg = debugMsgs.get(0);
                debugMsgs.remove(0);

                try {
                    urlConnection = NetUtils.prepareConnection(ErrorLogURL +"/useraccounts/loglog", "POST");

                    JSONObject json = new JSONObject();//创建json对象
                    json.put("tag", URLEncoder.encode(Options.getUserName(), "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                    json.put("msg", URLEncoder.encode(msg, "UTF-8"));//把数据put进json对象中
                    String jsonstr = json.toString();//把JSON对象按JSON的编码格式转换为字符串

                    NetUtils.writeOut(urlConnection, jsonstr);

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
                        String rjson = NetUtils.readBackFromConnection(urlConnection);
                    } else {
                        Log.d("L", "response code is:" + urlConnection.getResponseCode());
                    }
                } catch (Exception e) {
                    Log.d("L", "Exception happened when sending log to server: " + ErrorLogURL +"/useraccounts/loglog");//rjson={"json":true}
                } finally {
                    if(urlConnection != null) {
                        urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                    }
                }
            }

            AppUtils.sleep(1000);
        }
    }

//    private List<Printer> absorbPrinters(JSONArray jsonPrinters, PrinterDao printerDao) throws JSONException {
//        printerDao.deleteAll();
//        List<Printer> printers = new ArrayList<>();
//        if (jsonPrinters.length() > 0) {
//            for (int i = 0; i < jsonPrinters.length(); i++) {
//                JSONObject jo = jsonPrinters.getJSONObject(i);
//                Printer printer = new Printer();
//                printer.setIp(jo.getString("ip"));
//                printer.setPname(jo.getString("pname"));
//                printer.setFirstPrint(jo.getInt("firstPrint"));
//                printer.setType(jo.getInt("type"));
//                printer.setState(jo.getInt("state"));
//                printer.setNote(jo.getString("note"));
//                printer.setState(State.OPENNED);
//                printer.setId(printerDao.insert(printer));
//                printers.add(printer);
//            }
//        }
//        return printers;
//    }

    private Long getCategoryId(String categoryName, List<Category> categories) {
        for(Category category : categories){
            if(category.getName1().equals(categoryName)){
                return category.getId();
            }
        }
        return 0l;
    }

    private String prepareSMContent(String jsonStr){
        int lastId = 0;
        String idFR;
        try {
            JSONArray jsonObject = new JSONArray(jsonStr);
            JSONArray mainOrders = new JSONArray((String)jsonObject.get(0));
            if(mainOrders.length() == 0){
                return null;
            }
//            JSONArray materials = new JSONArray((String)jsonObject.get(1));

            int length = mainOrders.length();
            for(int i = 0; i < length; i++){
                JSONObject mainOrder = mainOrders.getJSONObject(i);
                String phone = mainOrder.getString("sampleRequirement");
                if(phone != null && phone.length() > 0){
                    int id = mainOrder.getInt("_id");
                    if(id > lastId){
                        lastId = id;
                    }
                    String number = mainOrder.getString("colorProportionReceived");
                    String name = mainOrder.getString("sizeTable");
                    String address = mainOrder.getString("clientSideModelNumber");
                    String delieverdate = mainOrder.getString("delieverdate");
                    String price = mainOrder.getString("payCondition");
                    StringBuilder sb = new StringBuilder();
                    String csphone = Options.getCustomData(Options.csphone);
                    if(csphone == null || csphone.length() < 8){
                        csphone = "5148986618";
                    }
                    sb.append("Dear ").append(name).append(", your order #").append(number).append(" will be delivered to ")
                            .append(address).append(" at ").append(delieverdate).append(". If it's not your order, please call us at " + csphone + ", thanks");
                    sendSMSMessage(phone, sb.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return String.valueOf(lastId);
    }

    public static String SMS_SEND_ACTIOIN = "SMS_SEND_ACTIOIN";
    public static String SMS_DELIVERED_ACTION = "SMS_DELIVERED_ACTION";

    private void sendSMSMessage(String phoneNumber, String content) {

        if(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
            if (ContextCompat.checkSelfPermission(AikaPos.instance.getApplicationContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                //this is display an interface for user to click send button, not OK for us, we need to send automatically and silently
//                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));
//                    intent.putExtra("sms_body", content);
//                    orderActivity.startActivity(intent);

                SmsManager smsManager = SmsManager.getDefault();
                try {
                    /* 建立自定义Action常数的Intent(给PendingIntent参数之用) */
                    Intent itSend = new Intent(SMS_SEND_ACTIOIN);
                    Intent itDeliver = new Intent(SMS_DELIVERED_ACTION);

                    /* sentIntent参数为传送后接受的广播信息PendingIntent */
//                    PendingIntent mSendPI = PendingIntent.getBroadcast(AikaPos.instance.getApplicationContext(), 0, itSend, 0);
//
//                    /* deliveryIntent参数为送达后接受的广播信息PendingIntent */
//                    PendingIntent mDeliverPI = PendingIntent.getBroadcast(AikaPos.instance.getApplicationContext(), 0, itDeliver, 0);
//                    List<String> divideContents = smsManager.divideMessage(content);
//                    for (String text:divideContents) {
//                        /* 发送SMS短信，注意倒数的两个PendingIntent参数 */
//                        smsManager.sendTextMessage(phoneNumber, null, text, mSendPI, mDeliverPI);
//                    }

                }catch(Exception e) {
                    Toast.makeText(DataSyncService.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }else{//没有权限，向用户请求权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(orderFragmentActivity, Manifest.permission.SEND_SMS)) {
                //TODO: show an guid for user to open the permission.
            } else {
                ActivityCompat.requestPermissions(orderFragmentActivity, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }


    public void setActivity(OrderFragmentActivity orderActivity) {
        this.orderFragmentActivity = orderActivity;
    }
}
