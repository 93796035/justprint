package com.just.print.ui;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

    public static void setEnabledRecursive(ViewGroup viewGroup, boolean enabled) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.setEnabled(enabled);
            if (child instanceof ViewGroup) {
                setEnabledRecursive((ViewGroup) child, enabled);
            }
        }
        viewGroup.setEnabled(enabled);
    }
}
