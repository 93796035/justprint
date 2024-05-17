package com.just.print.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;

public class JOptionPane {
    public static final int YES_NO_OPTION = 0;

    public static void showMessageDialog(BarFrame instance, String atLeastTwoTableShouldBeSelected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(instance); // 使用当前Activity的上下文创建Builder
        builder.setTitle("Notice"); // 设置对话框标题
        builder.setMessage(BarFrame.consts.AtLeastTwoTableShouldBeSelected()); // 设置对话框显示的消息

        // 设置确认按钮
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 关闭对话框
            }
        });

        // 创建AlertDialog对象并显示
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static boolean showConfirmDialog(BarFrame instance, String content, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(instance);
        builder.setTitle(title);
        builder.setMessage(content);  // 假设你已经有适合安卓的方式来获取这个消息

        final boolean[] valueFR = new boolean[1];
        // 添加 "Yes" 按钮
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueFR[0] = true;
            }
        });

        // 添加 "No" 按钮
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueFR[0] =  false;
            }
        });

        // 创建并显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
        return valueFR[0];
    }

    public static void showInputDialog(String title, String message, InputDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BarFrame.instance);
        builder.setTitle(title);
        builder.setMessage(message);

        // 创建一个EditText用于输入
        final EditText input = new EditText(BarFrame.instance);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // 设置“确定”按钮及其动作
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onInputReceived(input.getText().toString());
            }
        });

        // 设置“取消”按钮及其动作
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                listener.onInputCanceled();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 输入监听器接口
    public interface InputDialogListener {
        void onInputReceived(String input);
        void onInputCanceled();
    }
}
