package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.AikaStyleApplication;
import com.just.print.app.BarOption;
import com.just.print.db.bean.User;
import com.just.print.db.expand.DaoExpand;
import com.just.print.ui.activity.TableFragmentActivity;  // 确保导入了TableActivity

import java.util.ArrayList;
import java.util.List;

public class LoginDialogFragment extends DialogFragment {

    private static List<User> users = new ArrayList<User>();

    private static int USERTYPE = -1;
    public static final int EMPLOYEE_USER = 1;
    public static  final int MANAGER_USER = 2;
    public static  final int ADMIN_USER = 3;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);
        // 数字按钮事件
        for (int id : new int[]{
                R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5,
                R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button0}) {
            view.findViewById(id).setOnClickListener(this::onNumberClick);
        }

        // 设置后退按钮事件
        Button backButton = view.findViewById(R.id.button_left_arrow);
        backButton.setOnClickListener(v -> {
            EditText passwordField = view.findViewById(R.id.password_field);
            int length = passwordField.getText().length();
            if (length > 0) {
                passwordField.setText(passwordField.getText().delete(length - 1, length));
            }
        });

        view.findViewById(R.id.button_ok).setOnClickListener(v -> {

            startActivity(new Intent(getActivity(), TableFragmentActivity.class));
            //dismiss();  // 不能关闭，否则向右滑动看到的是空白屏。
        });
        initContent();
        builder.setView(view);
        return builder.create();
    }

    private void initContent() {
        users = DaoExpand.queryAllTables(AikaStyleApplication.instance.getDaoMaster().newSession().getUserDao());
        String sql = "select ID, Type, UserName, PASSWORD, LANG from useridentity where ID > 5";
    }
    public static int getUSERTYPE() {
        return USERTYPE;
    }

//    public static void setUSERTYPE(int uSERTYPE) {
//        USERTYPE = uSERTYPE;
//        if(USERTYPE >= MANAGER_USER) {
//            mainLayout.setBackgroundColor(BarOption.getCustomData("AdminMainFramelBK"));
//        }else {
//            mainLayout.setBackgroundColor(BarOption.getCustomData("AdminMainFrameBK"));
//        }
//    }

    public static boolean validatePassword(String input) {
        for (User user : users) {
            if(input.equals(user.getPwd())) {
                return false;
            }
        }
        return true;
    }

    private void onNumberClick(View v) {
        Button button = (Button) v;
        EditText passwordField = getDialog().findViewById(R.id.password_field);
        passwordField.append(button.getText());
    }

    private static LoginDialogFragment instance = null;
    public static LoginDialogFragment getInstance(){
        if(instance == null){
            instance = new LoginDialogFragment();
        }
        return instance;
    }
}
