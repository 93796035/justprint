package com.just.print.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.panel.BillPanel;

import java.util.concurrent.CountDownLatch;

public class JOptionPane {
    public static final int YES_NO_OPTION = 0;

    public static void showMessageDialog(String atLeastTwoTableShouldBeSelected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BarFrame.instance); // 使用当前Activity的上下文创建Builder
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
    public static void showMessageDialog(Object o,  String atLeastTwoTableShouldBeSelected) {
        showMessageDialog(atLeastTwoTableShouldBeSelected);
    }

    public static void showConfirmDialog(Object instance, String content, String title, ConfirmDialogCallback callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(BarFrame.instance);
        builder.setTitle(title);
        builder.setMessage(content);  // 假设你已经有适合安卓的方式来获取这个消息

        // 添加 "Yes" 按钮
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onResult(true);
            }
        });

        // 添加 "No" 按钮
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onResult(false);
            }
        });

        // 创建并显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
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

    public static String showInputDialog(String title, String message) {
        final String[] result = new String[]{null};
        final CountDownLatch latch = new CountDownLatch(1);

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
                result[0] = input.getText().toString();
                latch.countDown();
            }
        });

        // 设置“取消”按钮及其动作
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                latch.countDown();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        try {
            // 等待用户输入
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }


    public static boolean showConfirmDialog(String content, String title) {
        final boolean[] result = {false};
        final CountDownLatch latch = new CountDownLatch(1);

        AlertDialog.Builder builder = new AlertDialog.Builder(BarFrame.instance);
        builder.setTitle(title);
        builder.setMessage(content);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                result[0] = true;
                latch.countDown();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                result[0] = false;
                latch.countDown();
            }
        });

        // 显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    // 输入监听器接口
    public interface InputDialogListener {
        void onInputReceived(String input);
        void onInputCanceled();
    }

    //==================================================
    public interface ConfirmDialogCallback {
        void onResult(boolean confirmed);
    }
}
