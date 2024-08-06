package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.just.print.app.BarFrame;

public abstract class SamDialog extends DialogFragment implements View.OnClickListener {
    public Dialog dialog;
    public SamDialog(){
        dialog = new Dialog(BarFrame.instance);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
    }

    public FrameLayout.LayoutParams getLayout(View view, int topMargin, int leftMargin) {
        return getLayout(view, topMargin, leftMargin, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public FrameLayout.LayoutParams getLayout(View view, int topMargin, int leftMargin, int width) {
        return getLayout(view, topMargin, leftMargin, width, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public FrameLayout.LayoutParams getLayout(View view, int topMargin, int leftMargin, int width, int height) {
        view.setTop(topMargin);
        view.setLeft(leftMargin);
        view.setMinimumWidth(width);
        view.setMinimumHeight(height);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width,height);

        layoutParams.topMargin = topMargin;
        layoutParams.leftMargin = leftMargin;

        return layoutParams;
    }

    public Button createButton(String text) {
        Button button = new Button(BarFrame.instance);
        button.setText(text);
        return button;
    }

    public TextView createLable(String s) {
        TextView textView = new TextView(BarFrame.instance);
        textView.setTextColor(Color.WHITE);
        return textView;
    }
}
