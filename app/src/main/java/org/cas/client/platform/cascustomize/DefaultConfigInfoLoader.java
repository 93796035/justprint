package org.cas.client.platform.cascustomize;

import android.content.Context;
import android.os.FileUtils;
import android.util.Log;

import com.just.print.app.BarFrame;
import com.just.print.util.L;

import org.cas.client.platform.casutil.TaoEncrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;

class DefaultConfigInfoLoader {
    final private String LINK_STRING = "=";
    final private String ENTER_STRING = ";";

    /** 构造器 */
    DefaultConfigInfoLoader() {
    }

    /** 取得PIM的config文件的路径, @Called by ConfigFileWriter */
    static String getConfigFilePath() {
        return "PIMConfig.ini";
    }

    /**
     * @called by:FileCustomOptions;
     * @param prmHash
     *            所有用户CustOptimized信息通过本方法调入hash中。
     */
    void loadCustmizedData(Context context, Hashtable prmHash) {
        // 将磁盘中文件中的信息读入字符串tmpFileText。----------------------------------------
        String tmpConfigFilePath = getConfigFilePath();
        // System.out.println("tmpConfigFilePath is : " + tmpConfigFilePath);
        String tmpFileText = "";
        try {
            FileInputStream fis = context.openFileInput(tmpConfigFilePath);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            tmpFileText = sb.toString();
            tmpFileText = TaoEncrypt.decrypt(tmpFileText, 1);
            // FileInputStream fileInput = new FileInputStream(tmpConfigFilePath);
            //
            // byte[] buffer = new byte[(int) new File(tmpConfigFilePath).length()]; // 建立用户初始化设置文件对象。
            // fileInput.read(buffer);
            // fileInput.close();
            // tmpFileText = new String(buffer); // 将用户初始化设置文件信息读入字符串。
        } catch (IOException e) {
            return;
        } catch (Exception e) {
            L.e("DefaultConfigInfoLoader", "exception happened when decryption", e);
            return;
        }
        // System.out.println("tmpFileText(UTF-8) is : " + tmpFileText);
        // 将字符串中的信息存入Hashtable。----------------------------------------
        int tmpLINKPos;
        int tmpENTERPos = 0;
        int tmpTextLen = tmpFileText.length();

        if (tmpFileText.endsWith("}"))
            tmpFileText = tmpFileText.substring(0, tmpTextLen - 1);
        // @NOTE:不可以去掉前面的“{”号，因为每次取Key的时候是从前一个“，”号位置＋＋开始取值，
        // 去掉“{”号会导致如“Vertical”变成了“ertical”的错误。
        // if (tmpFileText.startsWith("{"))
        // tmpFileText = tmpFileText.substring(1);
        // 至此去掉字符串两端的“{”和“}”--------------------------

        tmpFileText = tmpFileText.trim(); // 去掉字符串两端其他无意义的字符。
        tmpTextLen = tmpFileText.length();

        String tmpKey = null;
        String tmpValue = null;
        for (; tmpENTERPos < tmpTextLen && tmpENTERPos > -1;) {
            tmpLINKPos = tmpFileText.indexOf(LINK_STRING, tmpENTERPos); // 得到等于号的位置。
            if (tmpLINKPos <= tmpENTERPos) // 如果等号位置不在回车位置之前，说明不对了，或到头了。
                return;
            tmpKey = tmpFileText.substring(++tmpENTERPos, tmpLINKPos).trim(); // 得到key值（等于号前面的值）。

            tmpENTERPos = tmpFileText.indexOf(ENTER_STRING, tmpLINKPos); // 得到回车符得位置。
            if (tmpENTERPos <= tmpLINKPos) // 如果回车号位置不在等号位置之前，说明不对了，或到头了。
                tmpENTERPos = tmpTextLen; // 此处不可直接返回，因为不该漏掉最后一个value。
            // @NOTE:可能需要判断是否是等于，如果最后一个符号是“＝”会不会出错？
            tmpValue = tmpFileText.substring(++tmpLINKPos, tmpENTERPos);
            if(!"BillFootInfo".equals(tmpKey))	//don't want to lost the empty spaces at the head of the info.
                tmpValue = tmpValue.trim();
//            System.out.println("--------------------");
//            System.out.println("key:" + tmpKey);
//            System.out.println("tmpValue：" + tmpValue);
            prmHash.put(tmpKey, tmpValue);
        }
    }

    /**
     * @called by:FileCustomOptions; hash2中的内容包括两项：供验证主应用是否有被添加删除的AppNameStr；和菜单工具条的结构信息。
     * @param prmHash
     *            需要load的信息在prmHash中存储着
     */
    void loadSystemData(
            Hashtable prmHash) {
        prmHash.put("APP_Captions", "Bar");
        prmHash.put("APP_Names", "Bar");
        prmHash.put("DefaultFont", "宋体");
    }

    /**
     * @called by:FileCustomOptions; 根据传入的应用名，构造路径字符串，然后读入信息到参数hash表中。
     * @param prmHash
     *            需要load的信息在prmHash中存储着
     */
    void loadAppData(
            String prmAppName,
            Hashtable prmHash) {
        String tmpFileText = "";
        try {
            // Assuming the file is in the assets folder
            InputStream is = BarFrame.instance.getAssets().open(prmAppName.toLowerCase() + "/" + prmAppName + ".ini");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            is.close();
            tmpFileText = sb.toString();
        } catch (IOException e) {
            Log.e("DefaultConfigInfoLoader", "Failed to load app data", e);
            return;
        }

        // 将字符串中的信息存入Hashtable。----------------------------------------
        tmpFileText = tmpFileText.trim();
        int tmpLINKPos;
        int tmpENTERPos = 0;
        int tmpTextLen = tmpFileText.length();

        if (tmpFileText.trim().endsWith("}"))
            tmpFileText = tmpFileText.substring(0, tmpTextLen - 1);
        // @NOTE:不可以去掉前面的“{”号，因为每次取Key的时候是从前一个“，”号位置＋＋开始取值，
        // 去掉“{”号会导致如“Vertical”变成了“ertical”的错误。
        // if (tmpFileText.startsWith("{"))
        // tmpFileText = tmpFileText.substring(1);
        // 至此去掉字符串两端的“{”和“}”--------------------------

        tmpFileText = tmpFileText.trim(); // 去掉字符串两端其他无意义的字符。
        tmpTextLen = tmpFileText.length();

        String tmpKey = null;
        String tmpValue = null;
        for (; tmpENTERPos < tmpTextLen && tmpENTERPos > -1;) {
            tmpLINKPos = tmpFileText.indexOf(LINK_STRING, tmpENTERPos); // 得到等于号的位置。
            if (tmpLINKPos <= tmpENTERPos) // 如果等号位置不在回车位置之前，说明不对了，或到头了。
                return;
            tmpKey = tmpFileText.substring(++tmpENTERPos, tmpLINKPos).trim();// 得到key值（等于号前面的值）。

            tmpENTERPos = tmpFileText.indexOf(ENTER_STRING, tmpLINKPos);// 得到回车符得位置。
            if (tmpENTERPos <= tmpLINKPos) // 如果回车号位置不在等号位置之前，说明不对了，或到头了。
                tmpENTERPos = tmpTextLen; // 此处不可直接返回，因为不该漏掉最后一个value。
            // @NOTE:可能需要判断是否是等于，如果最后一个符号是“＝”会不会出错？
            tmpValue = tmpFileText.substring(++tmpLINKPos, tmpENTERPos).trim();
            prmHash.put(tmpKey, tmpValue);
        }
    }
}