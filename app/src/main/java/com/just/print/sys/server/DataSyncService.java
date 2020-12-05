package com.just.print.sys.server;

import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseActivity;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.util.AppUtils;
import com.just.print.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据同步服务
 * Created by wangx on 2016/10/31.
 */
public class DataSyncService extends BaseActivity implements Runnable{

    private static DataSyncService dataSyncService;
    protected String serverIpAndPort;
    protected String lastDownload;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ArrayList<SelectionDetail> lastSelection;
    public String tableName;

    public static DataSyncService getInstance() {
        if(dataSyncService == null) {
            dataSyncService = new DataSyncService();
        }
        return dataSyncService;
    }

    private DataSyncService(){
        serverIpAndPort = AppData.getCustomData(AppData.serverip);

        executorService.execute(this);
    }

    @Override
    public void run() {     //must use bk, because if use CustomerSelection.getInstance().getSelectedDishes(), it might be cleaned before thread started.
        while(true) {
            if (lastSelection != null && lastSelection.size() > 0) { //if there's order to send, then send.
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = AppData.prepareConnection("http://" + serverIpAndPort + "/security/newOrders","POST");
                    JSONObject json = new JSONObject();//创建json对象
                    json.put("table", URLEncoder.encode(StringUtils.isBlank(tableName) ? AppData.getUserName() : AppData.getUserName() + tableName, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                    json.put("billIndex", URLEncoder.encode("1", "UTF-8"));//把数据put进json对象中
                    StringBuilder orderContent = new StringBuilder();
                    for (SelectionDetail selectionDetail : lastSelection) {
                        orderContent.append("DishStart:");
                        Menu menu = selectionDetail.getDish();
                        Category categoryStr = Applic.app.getDaoMaster().newSession().getCategoryDao().load(menu.getCid());
                        orderContent.append(URLEncoder.encode(categoryStr.getCname(), "UTF-8")).append("\n");
                        orderContent.append(URLEncoder.encode(menu.getMname(), "UTF-8")).append("\n");

                        orderContent.append(selectionDetail.getDish().getPrice()).append("\n");
                        orderContent.append(selectionDetail.getDishNum()).append("\n");

                        List<Mark> marks = selectionDetail.getMarkList();
                        orderContent.append("MarkStart:");
                        for (Mark mark : marks) {
                            orderContent.append(URLEncoder.encode(mark.getName(), "UTF-8")).append("\n");
                            orderContent.append(mark.getQt()).append("\n");     //number
                            orderContent.append(mark.getState()).append("\n");  //displayIndex
                            orderContent.append(mark.getVersion()).append("\n");//price
                        }

                        json.put("orderContent", orderContent.toString());
                    }

                    AppData.writeOut(urlConnection, json.toString());//把JSON对象按JSON的编码格式转换为字符串

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
                        String rjson = AppData.readBackFromConnection(urlConnection);

                        Log.d("zxy", "rjson=" + rjson);//rjson={"json":true}
                    } else {
                        Log.e("L", "response code is:" + urlConnection.getResponseCode());
                    }
                } catch (Exception e) {
                    Log.e("L", "Exception happened when sending dishes to server: " + serverIpAndPort);//rjson={"json":true}
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                    }
                }
            }

            lastDownload = AppData.getCustomData(AppData.lastDownload);
            if(lastDownload != null && lastDownload.length() > 0 && !"null".equalsIgnoreCase(lastDownload)){ //if the lastDownload
                // is set, then download new orders.
                String account = AppData.getShopName();
                //download new records after last download time:
                HttpURLConnection urlConnection = null;
                try {
                    //TODO: this one should use "GET" as method, while don't know why it get can not reach back end from here. so I have to keep post for now.
                    urlConnection = AppData.prepareConnection(AppData.getSERVER_URL() + "requestNewOrders" + "/" + account + "/" + lastDownload, "POST");
                    JSONObject json = new JSONObject();//创建json对象
                    json.put("lastDownload", lastDownload);
                    json.put("shop",  account);
                    json.put("licence", AppData.getLicense());
                    String newLastDownload = null;
                    AppData.writeOut(urlConnection, json.toString());//把JSON对象按JSON的编码格式转换为字符串
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
                        String rjson = AppData.readBackFromConnection(urlConnection);
                        //send sms
                        newLastDownload = processTheOrders(rjson);
                        Log.d("zxy", "rjson=" + rjson);//rjson={"json":true}
                    } else {
                        Log.e("L", "response code is:" + urlConnection.getResponseCode());
                    }

                    AppData.putCustomData(AppData.lastDownload, newLastDownload != null ? newLastDownload : lastDownload);
                } catch (Exception e) {
                    Log.e("L", "Exception happened when requestNewOrders from server: " + e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                    }
                }
            }

            AppUtils.sleep(1000);
        }
    }

    private String processTheOrders(String jsonStr){
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
                    int id = mainOrder.getInt("id");
                    if(id > lastId){
                        lastId = id;
                    }
                    String number = mainOrder.getString("colorProportionReceived");
                    String name = mainOrder.getString("sizeTable");
                    String address = mainOrder.getString("clientSideModelNumber");
                    String delieverdate = mainOrder.getString("delieverdate");
                    String price = mainOrder.getString("payCondition");
                    StringBuilder sb = new StringBuilder();
                    sb.append("Dear ").append(name).append(", your order #").append(number).append(" will be delivered to ")
                            .append(address).append(" at ").append(delieverdate).append(". If it's not your order, please call us at 5145525771, thanks");
//                    new JSSMS().sendSMS("5145525771", sb.toString());
                    sendSMSMessage("5145525771", sb.toString());
                    sendSMS("5145525771", sb.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return String.valueOf(lastId);
    }

    protected void sendSMSMessage(String phoneNumber, String content) {
        try{
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, DataSyncService.class), 0);

            SmsManager smgr = SmsManager.getDefault();
            smgr.sendTextMessage("5145583266",null,"hiiii", pi,null);
//            smgr.sendTextMessage(phoneNumber,null,content, pi,null);
            Toast.makeText(DataSyncService.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(DataSyncService.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
        }

//        private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
//            }
//        }
    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage("14389210090", null, "36999", null, null);
//                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }
//        }
//
//    }

    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        try {
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                    SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), 0);

            //        // ---When the SMS has been sent---
            //        registerReceiver(new BroadcastReceiver() {
            //            @Override
            //            public void onReceive(Context arg0, Intent arg1) {
            //                switch (getResultCode()) {
            //                    case Activity.RESULT_OK:
            //                        Toast.makeText(getBaseContext(), "SMS sent",
            //                                Toast.LENGTH_SHORT).show();
            //                        break;
            //                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            //                        Toast.makeText(getBaseContext(), "Generic failure",
            //                                Toast.LENGTH_SHORT).show();
            //                        break;
            //                    case SmsManager.RESULT_ERROR_NO_SERVICE:
            //                        Toast.makeText(getBaseContext(), "No service",
            //                                Toast.LENGTH_SHORT).show();
            //                        break;
            //                    case SmsManager.RESULT_ERROR_NULL_PDU:
            //                        Toast.makeText(getBaseContext(), "Null PDU",
            //                                Toast.LENGTH_SHORT).show();
            //                        break;
            //                    case SmsManager.RESULT_ERROR_RADIO_OFF:
            //                        Toast.makeText(getBaseContext(), "Radio off",
            //                                Toast.LENGTH_SHORT).show();
            //                        break;
            //                }
            //            }
            //        }, new IntentFilter(SENT));
            //
            //        // ---When the SMS has been delivered---
            //        registerReceiver(new BroadcastReceiver() {
            //            @Override
            //            public void onReceive(Context arg0, Intent arg1) {
            //                switch (getResultCode()) {
            //                    case Activity.RESULT_OK:
            //                        Toast.makeText(getBaseContext(), "SMS delivered",
            //                                Toast.LENGTH_SHORT).show();
            //                        break;
            //                    case Activity.RESULT_CANCELED:
            //                        Toast.makeText(getBaseContext(), "SMS not delivered",
            //                                Toast.LENGTH_SHORT).show();
            //                        break;
            //                }
            //            }
            //        }, new IntentFilter(DELIVERED));

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }catch(Exception e){
            System.out.println("..........");
        }
    }
}
