package com.just.print.ui.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.just.print.R;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.ui.fragment.RegisterFragment;

public class RegisterFragmentActivity extends BaseFragmentActivity {

    private Fragment registerFragment = new RegisterFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        beginTransaction().add(R.id.content, registerFragment, registerFragment.getClass().getName()).commit();
    }

}
