package org.cas.client.platform.casutil;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.just.print.app.BarFrame;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义缓冲区，final类。
 */
public final class PIMPool {
    /* enter string */
    public static final String ENTER_STRING = "\r\n";

    /* boolean's true string */
    public static final String BOOLEAN_TRUE = "true";

    /* boolean's false string */
    public static final String BOOLEAN_FALSE = "false";

    /* integer type string */
    public static final String INTEGER_TYPE = "Integer:";

    /* boolean type string */
    public static final String BOOLEAN_TYPE = "Boolean:";

    /* string type string */
    public static final String STRING_TYPE = "String:";
    private Map<String, Drawable> imagePool = new HashMap<>();
    // --------------------------------------------------------------------------
    private static PIMPool instance = null;
    public static PIMPool getInstance(Context context){
        if(instance == null){
            instance = new PIMPool();
        }
        return instance;
    }
    public Drawable getImage(String imageName) {
        if (!imagePool.containsKey(imageName)) {
            int resourceId = BarFrame.instance.getResources().getIdentifier(imageName, "drawable", BarFrame.instance.getPackageName());
            Drawable drawable = ContextCompat.getDrawable(BarFrame.instance, resourceId);
            imagePool.put(imageName, drawable);
        }
        return imagePool.get(imageName);
    }

    public Drawable getIcon(String iconName) {
        return getImage(iconName); // Icons and images are handled the same way in Android
    }
}
