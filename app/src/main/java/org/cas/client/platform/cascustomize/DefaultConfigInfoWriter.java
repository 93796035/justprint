package org.cas.client.platform.cascustomize;

import android.content.Context;
import android.util.Log;

import com.just.print.app.BarFrame;

import org.cas.client.platform.casutil.TaoEncrypt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * ＠Note：只有Config.ini也即hash中的内容需要保存，其他内容是只读的。
 */

class DefaultConfigInfoWriter {
    /** Creates new DefaultModelWriter */
    DefaultConfigInfoWriter() {
        configFilePath = DefaultConfigInfoLoader.getConfigFilePath();
    }

    /**
     * Save data.
     */
    public void saveData(Hashtable<String, String> prmHash) {
        FileOutputStream fos = null;
        try {
            String data = TaoEncrypt.encryptPassword(prmHash.toString()); // 加密数据
            fos = BarFrame.instance.openFileOutput(configFilePath, Context.MODE_PRIVATE); // 使用私有模式打开文件
            fos.write(data.getBytes("UTF-8")); // 写入数据
        } catch (IOException e) {
            Log.e("Error", "File write failed", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close(); // 关闭文件输出流
                } catch (IOException e) {
                    Log.e("Error", "Error closing file", e);
                }
            }
        }
    }
    
    // variables--------------------------------------------------------------------------------------------------------------
    protected FileOutputStream fileOutput;
    private String configFilePath;
}