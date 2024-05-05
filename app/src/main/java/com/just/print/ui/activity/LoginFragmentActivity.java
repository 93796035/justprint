package com.just.print.ui.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.ui.adapter.MyPagerAdapter;
import com.just.print.ui.fragment.LoginDialogFragment;
import com.just.print.ui.fragment.OrderCategoryFragment;
import com.just.print.ui.fragment.OrderIdentifierFragment;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.List;

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
