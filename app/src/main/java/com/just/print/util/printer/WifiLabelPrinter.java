package com.just.print.util.printer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.just.print.app.Applic;

import java.util.Vector;

public class WifiLabelPrinter {
    private static final int REQUEST_CODE = 0x004;
    private static final int CONN_STATE_DISCONN = 0x007;//连接状态断开
    private static final int PRINTER_COMMAND_ERROR = 0x008; // 使用打印机指令错误
    public static final int MESSAGE_UPDATE_PARAMETER = 0x009;

    private byte[] esc = {0x10, 0x04, 0x02};    // ESC查询打印机实时状态指令
    private byte[] cpcl={0x1b,0x68};    // CPCL查询打印机实时状态指令
    private byte[] tsc = {0x1b, '!', '?'};    // TSC查询打印机状态指令
    private static final int CONN_MOST_DEVICES = 0x11;
    private static final int CONN_PRINTER = 0x12;

    private Handler mHandler;

    private ThreadPool threadPool;

    private int id = 0; //the first wifi-label printer, we currentluy support only 1 printer.

    private static WifiLabelPrinter instance = null;
    public static WifiLabelPrinter getInstance() {
        if (instance == null){
            instance = new WifiLabelPrinter();
        }
        return instance;
    }
    private WifiLabelPrinter(){
        //just make a private constructor
    }

    public void connect(String strIp, String strPort) {
        if (!TextUtils.isEmpty(strIp) && !TextUtils.isEmpty(strPort)) {
            Message message = Message.obtain();
            message.what = MESSAGE_UPDATE_PARAMETER;
            Bundle bundle = new Bundle();
            bundle.putString("Ip", strIp);
            bundle.putString("Port", strPort);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }

    public void disConn(View view) {
        if (DeviceConnFactoryManager.getArry()[id] == null || !DeviceConnFactoryManager.getArry()[id].getConnState()) {
            Utils.toast(Applic.app, "Please connect the printer first");
            return;
        }
        mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
    }

    public void printLabel(String ip, String port, final String content) {
        //TODO: generate connection:

        threadPool = ThreadPool.getInstance();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                final DeviceConnFactoryManager[] deviceConnFactoryManagers = DeviceConnFactoryManager.getArry();
                if (deviceConnFactoryManagers[id] == null || !deviceConnFactoryManagers[id].getConnState()) {
                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                    return;
                }
                if (deviceConnFactoryManagers[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    deviceConnFactoryManagers[id].sendDataImmediately(prepareWifiLabelContent(content));
                    //TODO: close the connection... is it necessary?
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    public static Vector<Byte> prepareWifiLabelContent(String content) {
        LabelCommand tsc = new LabelCommand();
        tsc.addSize(60, 75); // 设置标签尺寸宽高，按照实际尺寸设置 单位mm
        tsc.addGap(0); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0 单位mm
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);// 设置打印方向
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON); // 开启带Response的打印，用于连续打印
        tsc.addReference(0, 0);// 设置原点坐标
        tsc.addDensity(LabelCommand.DENSITY.DNESITY4);//设置浓度
        tsc.addTear(EscCommand.ENABLE.ON); // 撕纸模式开启
        tsc.addCls();// 清除打印缓冲区

        tsc.addText(10, 0, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                content); // 绘制简体中文
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
}
