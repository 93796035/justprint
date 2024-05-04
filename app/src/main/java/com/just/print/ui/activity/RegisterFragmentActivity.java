package com.just.print.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.just.print.R;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.ui.fragment.RegisterFragment;

public class RegisterFragmentActivity extends BaseFragmentActivity {

    private Fragment fragment = new RegisterFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        beginTransaction().add(R.id.content, fragment, fragment.getClass().getName()).commit();
    }

}
