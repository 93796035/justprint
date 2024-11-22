package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.uibeans.NumButton;

public abstract class SamDialog extends DialogFragment implements View.OnClickListener {
    public int LABEL_WIDTH = 100;
    public int BUTTON_WIDTH = 120;
    public int BUTTON_HEIGHT = 100;
    public int VALUE_WIDTH = 100;
    public int VALUE_HEIGHT = 70;
    public Dialog dialog;
    protected String title;
    protected void setTitle(String title){
        this.title = title;
    }

    public SamDialog(){
        dialog = new Dialog(BarFrame.instance);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (closeCallback != null) {
            closeCallback.onDialogClose();
        }
    }

    public FrameLayout.LayoutParams getLayoutParams(View view, int topMargin, int leftMargin) {
        return getLayoutParams(view, topMargin, leftMargin, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public FrameLayout.LayoutParams getLayoutParams(View view, int topMargin, int leftMargin, int width) {
        return getLayoutParams(view, topMargin, leftMargin, width, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public FrameLayout.LayoutParams getLayoutParams(View view, int topMargin, int leftMargin, int width, int height) {
        view.setTop(topMargin);
        view.setLeft(leftMargin);
        view.setMinimumWidth(width);
        view.setMinimumHeight(height);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width,height);

        layoutParams.topMargin = topMargin;
        layoutParams.leftMargin = leftMargin;

        return layoutParams;
    }

    public Button createButton() {
        return createButton("");
    }
    public Button createButton(String text) {
        Button button = new Button(BarFrame.instance);
        button.setText(text);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    public NumButton createNumButton(String text) {
        NumButton btn = new NumButton(getActivity());
        btn.setText(text);
        btn.setGravity(Gravity.CENTER);
        return btn;
    }

    public TextView createLabel() {
        return createLabel("", -1, Gravity.LEFT, Color.WHITE);
    }
    public TextView createLabel(String s) {
        return createLabel(s, -1, Gravity.LEFT, Color.WHITE);
    }
    public TextView createLabel(String s, int gravity){
        return createLabel(s, -1, gravity, Color.WHITE);
    }
    public TextView createLabel(String s, int textSize, int gravity, int color) {
        TextView textView = new TextView(BarFrame.instance);
        textView.setTextColor(color);
        textView.setText(s);
        if(textSize > 0) {
            textView.setTextSize(textSize);
        }
        textView.setGravity(gravity);
        return textView;
    }

    public EditText createEditText(){
        EditText editText = new EditText(BarFrame.instance);
        editText.setTextSize(16);
        editText.setPadding(8, 0, 0, 0);
        return editText;
    }

    public CheckBox createCheckBox(String text) {
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setText(text);
        checkBox.setTextColor(Color.WHITE); // 设置文字颜色
        checkBox.setPadding(8, 8, 8, 8);    // 设置内边距
        checkBox.setScaleX(0.8f);
        checkBox.setScaleY(0.8f);
        return checkBox;
    }

    private DialogCloseCallback closeCallback;

    public void setCloseCallback(DialogCloseCallback closeCallback) {
        this.closeCallback = closeCallback;
    }
}
