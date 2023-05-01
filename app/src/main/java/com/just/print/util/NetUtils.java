package com.just.print.util;

import static com.just.print.util.ToastUtil.showToast;

import android.support.annotation.NonNull;

import com.just.print.app.AppData;
import com.just.print.app.NingMengStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

//response for one time message send. for regulaly sending message to server, please use DAtaSyncService.
public class NetUtils extends Thread{

    public String remoteUrlAndPort;
    private String contentToSend;
    public NetUtils(String url, String contentToSend){
        this.remoteUrlAndPort = url;
        this.contentToSend = contentToSend;
    }

    //activate==============================================
    public static void activate() {
        String accountInfo = getAccountJsonForActivate();
        if(accountInfo != null){
            AppData.putCustomData(AppData.ContentToSend, accountInfo);//把JSON对象按JSON的编码格式转换为字符串
            new NetUtils(AppData.getSERVER_URL() + "activeJustPrintAccount", accountInfo).start();
        }
    }

    public static String getAccountJsonForActivate() {
        StringBuilder content = new StringBuilder(AppData.getLicense());
        content.append(",");
        content.append(AppData.getShopName());
        content.append("-");
        content.append(AppData.getUserName());//如果有需要对特殊和不可见字符进行编码，使用URLEncoder.encode

        JSONObject json = new JSONObject();//创建json对象
        try {
            json.put("username", content.toString());//使用URLEncoder.encode对特殊和不可见字符进行编码
        }catch(Exception e){
            showToast("Please provide valid shop name, user name and license number!");
            L.e("TAG", "USERLOGIN_FAILED", e);
            return null;
        }
        return json.toString();
    }

    //send to cloud printer=======================================================
    // actually send to STGO, then send to cloud printer from there
    public static void sendToCloudPrinter(String content, String sn) {
        if (sn != null && sn.length() > 0) {
            JSONObject json = new JSONObject();//创建json对象
            try {
                json.put("content", URLEncoder.encode(content, "UTF-8"));//把数据put进json对象中
                json.put("filepath", URLEncoder.encode(sn, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                String size = AppData.getCustomData(AppData.width);
                size = (size == null || size.length() == 0) ? "24" : size;
                json.put("width", size);
            }catch(Exception e){
                L.e("DatabaseUtil", "Exception when encoding content into json: filepath:" + sn + " content:" + content, e);
            }
            AppData.putCustomData(AppData.ContentToSend, json.toString());
            new NetUtils(AppData.getSERVER_URL() + "cloudPrint", json.toString()).start();
            //is following old comment a real problem? what is “seperate print"??? and now we've already put the copy of the
            //contnet "into" the thread through the constructor.
            //it's ran in an thread, so when is a seperate print, one bill will generate
            //many lable print in short time, the threads will use a contentToSend which is alreaded rewote by next print task.
        }
    }

    //the run method=========================================================
    @Override
    public void run() {
        super.run();
        if(contentToSend.length() > 0 && AppUtils.hasInternet(NingMengStyle.app.getApplicationContext())){
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = prepareConnection(remoteUrlAndPort, "POST");
                NetUtils.writeOut(urlConnection, contentToSend);

                if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){//得到服务端的返回码是否连接成功

                    String responseString = NetUtils.readBackFromConnection(urlConnection);
                    AppData.putCustomData(AppData.ContentToSend, null);
                    long timeLeft = 0;
                    try{
                        timeLeft = Long.valueOf(responseString);    // if returned a number, then it must be  left time---(the activation case)
                        if(timeLeft > 0){//if success
                            AppData.putCustomData(AppData.number,responseString);
                            AppData.putCustomData(AppData.lastsuccess, String.valueOf(new Date().getTime()));

                            showToast("Application is activated successfully!");
                        }else{
                            showToast("No time left on server: "+ timeLeft);
                        }
                    }catch (Exception e){
                        if(responseString.length() > 2) {           //for the sending content to cloud printer case, the return value is "", for uploading, the return is "OK".
                            absortTheConfiguration(responseString); //if come here, so the return back is configurations
                        }
                    }
                }else{
                    showToast("Please provide valid shop name, user name and license number!");
                }
            } catch (Exception e) {
                L.e("TAG", "USERLOGIN_FAILED", e);
                showToast("USERLOGIN_FAILED");
            }finally{
                if(urlConnection != null)
                    urlConnection.disconnect();//使用完关闭TCP连接，释放资源
            }
        }
    }

    public static HttpURLConnection prepareConnection(String uri, String method) throws Exception {
        HttpURLConnection urlConnection;
        URL url = new URL(uri);
        urlConnection = (HttpURLConnection) url.openConnection();//打开http连接
        urlConnection.setConnectTimeout(30000);//连接的超时时间
        urlConnection.setUseCaches(false);//不使用缓存
        //urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
        urlConnection.setInstanceFollowRedirects(true);//是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
        urlConnection.setReadTimeout(30000);//响应的超时时间
        urlConnection.setDoInput(true);//设置这个连接是否可以写入数据
        urlConnection.setDoOutput(true);//设置这个连接是否可以输出数据
        urlConnection.setRequestMethod(method);//设置请求的方式
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置消息的类型
        urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接
        return urlConnection;
    }

    private void absortTheConfiguration(String responseString) {
        String[] strs = responseString.split(",");
        int count = 0;
        for (String entry : strs) {
            int i = entry.indexOf(":");
            if(i > 0){
                String value = entry.substring(i + 1);
                AppData.putCustomData(entry.substring(0, i), value);
                if(!"".equals(value)){
                    count++;
                }
            }
        }
        ToastUtil.showToast(count + " configurations in this device has been refreshed successfully.");
    }

    @NonNull
    public static String readBackFromConnection(HttpURLConnection urlConnection) throws IOException, JSONException {
        //------------字节流读取服务端返回的数据------------
        //InputStream in = urlConnection.getInputStream();//用输入流接收服务端返回的回应数据
        //BufferedInputStream bis = new BufferedInputStream(in);//高效缓冲流包装它，这里用的是字节流来读取数据的，当然也可以用字符流
        //byte[] b = new byte[1024];
        //int len = -1;
        //StringBuffer buffer = new StringBuffer();//用来接收数据的StringBuffer对象
        //while((len=bis.read(b))!=-1){
        //buffer.append(new String(b, 0, len));//把读取到的字节数组转化为字符串
        //}
        //in.close();
        //bis.close();
        //Log.d("zxy", buffer.toString());//{"json":true}
        //JSONObject rjson = new JSONObject(buffer.toString());//把返回来的json编码格式的字符串数据转化成json对象
        //------------字符流读取服务端返回的数据------------
        InputStream in = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String str = null;
        StringBuffer buffer = new StringBuffer();
        while((str = br.readLine())!=null){//BufferedReader特有功能，一次读取一行数据
            buffer.append(str);
        }
        in.close();
        br.close();
        return buffer.toString();
    }

    public static void writeOut(HttpURLConnection urlConnection, String jsonstr) throws Exception{
        //-------------使用字节流发送数据--------------
        //OutputStream out = urlConnection.getOutputStream();
        //BufferedOutputStream bos = new BufferedOutputStream(out);//缓冲字节流包装字节流
        //byte[] bytes = jsonstr.getBytes("UTF-8");//把字符串转化为字节数组
        //bos.write(bytes);//把这个字节数组的数据写入缓冲区中
        //bos.flush();//刷新缓冲区，发送数据
        //out.close();
        //bos.close();
        //------------字符流写入数据------------
        OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
        bw.write(jsonstr);//把json字符串写入缓冲区中
        bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
        out.close();
        bw.close();//使用完关闭
    }
}
