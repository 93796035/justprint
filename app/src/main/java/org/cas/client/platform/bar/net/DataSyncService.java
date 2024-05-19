package org.cas.client.platform.bar.net;

import com.just.print.app.BarFrame;
import com.just.print.db.bean.Dish;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.BarOption;
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
import java.util.ArrayList;
import java.util.List;

public class DataSyncService extends Thread{

    private List<Dish> lastSelection;	
    private String tableName;
    private String serverIpAndPort;
    
    public DataSyncService(String serverIpAndPort, String tableName, List<Dish> dishes){
    	this.serverIpAndPort = serverIpAndPort;
        this.tableName = tableName;
        this.lastSelection = new ArrayList();
	    for(Dish dish : dishes){ 	//clone it, in case the parameter is a reference to the one in using (which could be changed any time)
	        lastSelection.add(dish);
	    }
    }
    
    @Override
    public void run() {     //must use a cloned one, because if use CustomerSelection.getInstance().getSelectedDishes(), it might be cleaned before thread started.
        if(lastSelection != null && lastSelection.size() > 0) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = prepareConnection("http://" + serverIpAndPort + "/AikaPos/newOrders");
                JSONObject json = new JSONObject();
                json.put("table", URLEncoder.encode(tableName == null || tableName.length() == 0 ? BarOption.getShopName() :  BarOption.getShopName() + tableName, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("billIndex", URLEncoder.encode(BarFrame.instance.getCurBillIndex(), "UTF-8"));
                StringBuilder orderContent = new StringBuilder();
                for (Dish dish : lastSelection) {
                    orderContent.append("DishStart:");
                    orderContent.append(URLEncoder.encode(dish.getCategoryId().toString(), "UTF-8")).append("\n");	//category
                    orderContent.append(URLEncoder.encode(dish.getName1(), "UTF-8")).append("\n");			//menu name

                    orderContent.append(Double.valueOf(dish.getPrice()/100.0)).append("\n");					//price
                    orderContent.append(dish.getNum()).append("\n");

                    orderContent.append("MarkStart:");
                    String markStr = dish.getModification();
                    if(markStr != null && markStr.length() > 0) {
                    	String[] marks = markStr.split(",");
	                    for (String mark : marks) {
//	                        orderContent.append(URLEncoder.encode(mark.getName(), "UTF-8")).append("\n");
//	                        orderContent.append(mark.getQt()).append("\n");     //number
//	                        orderContent.append(mark.getState()).append("\n");  //displayIndex
//	                        orderContent.append(mark.getVersion()).append("\n");//price
	                    }
                     }

                    json.put("orderContent", orderContent.toString());
                }

                writeOut(urlConnection, json.toString());//把JSON对象按JSON的编码格式转换为字符串

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
                    String rjson = readBackFromConnection(urlConnection);

                    L.d("zxy", "rjson=" + rjson);//rjson={"json":true}
                } else {
                    L.e("L", "response code of data sync request is:" + urlConnection.getResponseCode(), null);
                }
            } catch (Exception e) {
                L.e("L", "Exception happened when sending dishes to server: " + serverIpAndPort, null);//rjson={"json":true}
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                }
            }
        }
    }
    
    public static HttpURLConnection prepareConnection(String uri) throws Exception {
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
        urlConnection.setRequestMethod("POST");//设置请求的方式
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置消息的类型
        urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接
        return urlConnection;
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
}