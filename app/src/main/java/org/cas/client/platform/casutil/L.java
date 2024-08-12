package org.cas.client.platform.casutil;

import android.util.Log;

import com.just.print.app.Options;
import com.just.print.app.AikaPos;
import com.just.print.sys.server.DataSyncService;
import com.just.print.util.AppUtils;

/**
 * Created by Simon&Nicholas on 7/28/2017.
 */

public class L{
    private static int index = 0;

    public static boolean debug = false;    //I don't use the value from AppData directly,
                        // because sometimes I want to temorary set it, not save to config.(when restart, I want the flag be cleaned)
    public static void d(String tag, Object msg){
        if(debug && AppUtils.hasInternet(AikaPos.instance.getApplicationContext())) {
            sendToServer(Options.getShopName() + "_" + Options.getUserName(), tag + ":" + String.valueOf(msg));
        }
        Log.d(tag, String.valueOf(msg));
    }

    public static void e(String tag, String msg, Throwable e){
        debug = true;
        if(debug && AppUtils.hasInternet(AikaPos.instance.getApplicationContext())) {
            sendToServer(Options.getShopName() + "_" + Options.getUserName(), tag + ":" + msg);
        }
        Log.i(tag, msg, e);
    }

    public static void i(Class tag, String msg){
        if(debug && AppUtils.hasInternet(AikaPos.instance.getApplicationContext())) {
            sendToServer(Options.getShopName() + "_" + Options.getUserName(), tag.getName() + ":" + msg);
        }
        Log.i(tag.getName(), msg);
    }
    public static void i(String tag, String msg){
        if(debug && AppUtils.hasInternet(AikaPos.instance.getApplicationContext())) {
            sendToServer(Options.getShopName() + "_" + Options.getUserName(), tag + ":" + msg);
        }
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg, Throwable e){
        if(debug && AppUtils.hasInternet(AikaPos.instance.getApplicationContext())) {
            sendToServer(Options.getShopName() + "_" + Options.getUserName(), tag + ":" + msg);
        }
        Log.d(tag, msg, e);
    }

    private static void sendToServer(String tag, String msg){
        DataSyncService.debugMsgs.add(index++ +"_" + tag + "_" + msg);
    }
}
