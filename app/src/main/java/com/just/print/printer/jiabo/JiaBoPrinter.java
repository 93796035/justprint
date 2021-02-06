package com.just.print.printer.jiabo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseActivity;
import com.just.print.util.ToastUtil;

import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JiaBoPrinter {

    private static final String TAG = JiaBoPrinter.class.getSimpleName();

    private CheckWifiConnThread checkWifiConnThread;//wifi连接线程监听

    private static final int REQUEST_CODE = 0x004;
    private static final int CONN_STATE_DISCONN = 0x007;//连接状态断开
    private static final int PRINTER_COMMAND_ERROR = 0x008; // 使用打印机指令错误
    public static final int MESSAGE_UPDATE_PARAMETER = 0x009;

    public static final int MESSAGE_CONNECT = 1;

    private byte[] esc = {0x10, 0x04, 0x02};    // ESC查询打印机实时状态指令
    private byte[] cpcl={0x1b,0x68};    // CPCL查询打印机实时状态指令
    private byte[] tsc = {0x1b, '!', '?'};    // TSC查询打印机状态指令
    private static final int CONN_MOST_DEVICES = 0x11;
    private static final int CONN_PRINTER = 0x12;

    public static final int CONN_STATE_DISCONNECT = 0x90;
    public static final int CONN_STATE_CONNECTING = CONN_STATE_DISCONNECT << 1;
    public static final int CONN_STATE_FAILED = CONN_STATE_DISCONNECT << 2;
    public static final int CONN_STATE_CONNECTED = CONN_STATE_DISCONNECT << 3;

    public static final String ACTION_QUERY_PRINTER_STATE = "action_query_printer_state";

    private ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder("ConnMoreDevicesActivity");


    private ThreadPool threadPool;

    private int counts;
    private int printcount=0;
    private boolean continuityprint=false;
    private int id = 0; //the first wifi-label printer, we currentluy support only 1 printer.

    private static JiaBoPrinter instance = null;
    public static JiaBoPrinter getInstance() {
        if (instance == null){
            instance = new JiaBoPrinter();
        }
        return instance;
    }
    private JiaBoPrinter(){
        //just make a private constructor
    }

    public void printLabel(String ip, String port, final String content) {
//        threadPool = ThreadPool.getInstance();
//        threadPool.addSerialTask(new Runnable() {
//            @Override
//            public void run() {
                if (DeviceConnFactoryManager.curPrinter.currentPrinterCommand == PrinterCommand.TSC) {
                    DeviceConnFactoryManager.curPrinter.sendDataImmediately(prepareWifiLabelContent(content));
                }
//            }
//        });
    }

    private static Vector<Byte> prepareWifiLabelContent(String content) {
        LabelCommand tsc = new LabelCommand();
        String size = AppData.getCustomData(AppData.labelsize);
        int w = 40;
        int h = 30;
        if(size != null){
            int p = size.indexOf(",");
            if(p > 0){
                w = Integer.valueOf(size.substring(0, p));
                h = Integer.valueOf(size.substring(p + 1));
            }
        }
        tsc.addSize(w, h); // 设置标签尺寸宽高，按照实际尺寸设置 单位mm

        int gap = 4;
        String gapStr = AppData.getCustomData(AppData.gap);
        if(gapStr != null && gapStr.length() > 0){
            try {
                gap = Integer.valueOf(gapStr);
            }catch(Exception e){
                //do nothing.
            }
        }
        tsc.addGap(gap); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0 单位mm

        tsc.addDirection(LabelCommand.DIRECTION.BACKWARD, LabelCommand.MIRROR.NORMAL);// 设置打印方向
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON); // 开启带Response的打印，用于连续打印

        //I think it's not necessary.....................
        int startX = 10;
        int startY = 10;
        String startXStr = AppData.getCustomData(AppData.StartX);
        if(startXStr != null && startXStr.length() > 0){
            try{startX = Integer.valueOf(startXStr);}catch(Exception e){}
        }
        String startYStr = AppData.getCustomData(AppData.StartY);
        if(startYStr != null && startYStr.length() > 0){
            try{startY = Integer.valueOf(startYStr);}catch(Exception e){}
        }
        tsc.addReference(startX, startY);// 设置原点坐标

        tsc.addDensity(LabelCommand.DENSITY.DNESITY4);//设置浓度
        tsc.addTear(EscCommand.ENABLE.ON); // 撕纸模式开启
        tsc.addCls();// 清除打印缓冲区

        //contents------------------------------------------
        String[] contents = content.split("\n");
        int height = 2;
        int fontSize = 2;
        String fontSizeStr = AppData.getCustomData(AppData.labelFont);
        if(fontSizeStr != null && fontSizeStr.length() > 0){
            try{fontSize = Integer.valueOf(fontSizeStr);}catch(Exception e){}
        }
        LabelCommand.FONTMUL fontMul = LabelCommand.FONTMUL.MUL_2;
        if(fontSize == 1){
            fontMul = LabelCommand.FONTMUL.MUL_1;
        } else if(fontSize == 3){
            fontMul = LabelCommand.FONTMUL.MUL_3;
        }else if(fontSize == 4){
            fontMul = LabelCommand.FONTMUL.MUL_4;
        }

        for(int i = 0; i < contents.length; i++){
            tsc.addText(10, 10 + (fontSize * 20 + 2) * i, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, //the unit here ar pixel....
                    fontMul, fontMul, contents[i]); // 绘制简体中文
        }

        //打印繁体 tsc.addUnicodeText(10,32, LabelCommand.FONTTYPE.TRADITIONAL_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"BIG5碼繁體中文字元","BIG5");
        //打印韩文 tsc.addUnicodeText(10,60, LabelCommand.FONTTYPE.KOREAN, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"Korean 지아보 하성","EUC_KR");

        // 绘制图片 Bitmap b = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.gprinter);
        //tsc.addBitmap(10, 80, LabelCommand.BITMAP_MODE.OVERWRITE, 300, b);
        //绘制二维码 tsc.addQRCode(10,380, LabelCommand.EEC.LEVEL_L, 5, LabelCommand.ROTATION.ROTATION_0, " www.smarnet.cc");
        // 绘制一维条码 tsc.add1DBarcode(10, 500, LabelCommand.BARCODETYPE.CODE128, 100, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, "SMARNET");

        tsc.addPrint(1, 1);  // 打印标签
        // 打印标签后 蜂鸣器响 tsc.addSound(2, 100);
        //开启钱箱 tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);

        return  tsc.getCommand();// 返回数据
    }


    private void sendContinuityPrint() {
        ThreadPool.getInstance().addSerialTask(new Runnable() {
            @Override
            public void run() {
                    ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder("MainActivity_sendContinuity_Timer");
                    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, threadFactoryBuilder);
                    scheduledExecutorService.schedule(threadFactoryBuilder.newThread(new Runnable() {
                        @Override
                        public void run() {
                            counts--;
                            DeviceConnFactoryManager.curPrinter.sendDataImmediately(PrintContent.getLabel());
                        }
                    }), 1000, TimeUnit.MILLISECONDS);
                }
        });
    }
//    /**
//     * 获取当前连接设备信息
//     * @return
//     */
//    private String getConnDeviceInfo() {
//        String str = "";
//        DeviceConnFactoryManager deviceConnFactoryManager = DeviceConnFactoryManager.arry[id];
//        if (deviceConnFactoryManager != null
//                && deviceConnFactoryManager.getConnState()) {
//            if ("USB".equals(deviceConnFactoryManager.getConnMethod().toString())) {
//                str += "USB\n";
//                str += "USB Name: " + deviceConnFactoryManager.usbDevice().getDeviceName();
//            } else if ("WIFI".equals(deviceConnFactoryManager.getConnMethod().toString())) {
//                str += "WIFI\n";
//                str += "IP: " + deviceConnFactoryManager.getIp() + "\t";
//                str += "Port: " + deviceConnFactoryManager.getPort();
//                checkWifiConnThread=new CheckWifiConnThread(deviceConnFactoryManager.getIp(),handler);//开启监听WiFi线程
//                checkWifiConnThread.start();
//            } else if ("BLUETOOTH".equals(deviceConnFactoryManager.getConnMethod().toString())) {
//                str += "BLUETOOTH\n";
//                str += "MacAddress: " + deviceConnFactoryManager.getMacAddress();
//            } else if ("SERIAL_PORT".equals(deviceConnFactoryManager.getConnMethod().toString())) {
//                str += "SERIAL_PORT\n";
//                str += "Path: " + deviceConnFactoryManager.getSerialPortPath() + "\t";
//                str += "Baudrate: " + deviceConnFactoryManager.getBaudrate();
//            }
//        }
//        return str;
//    }
}
