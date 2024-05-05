package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;

public class LoginDialogFragment extends DialogFragment {

    private static LoginDialogFragment instance = null;
    public static LoginDialogFragment getInstance(){
        if(instance == null){
            instance = new LoginDialogFragment();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);

        view.findViewById(R.id.button_ok).setOnClickListener(v -> {
            dismiss();
        });

//        view.findViewById(R.id.button_cancel).setOnClickListener(v -> {
//            dismiss();
//        });

        // 设置数字按钮事件
        for (int id : new int[]{R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button0}) {
            view.findViewById(id).setOnClickListener(this::onNumberClick);
        }

        builder.setView(view);
        return builder.create();
    }

    private void onNumberClick(View v) {
        Button button = (Button) v;
        EditText passwordField = getDialog().findViewById(R.id.password_field);
        passwordField.append(button.getText());
    }
}
