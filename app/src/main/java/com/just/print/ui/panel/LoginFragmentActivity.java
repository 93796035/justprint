package com.just.print.ui.panel;

import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.ui.fragment.LoginDialogFragment;

public class LoginFragmentActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) { // 确保在初次创建时添加 Fragment
            LoginDialogFragment.getInstance().show(getSupportFragmentManager(), "login_dialog");
        }
    }
}
