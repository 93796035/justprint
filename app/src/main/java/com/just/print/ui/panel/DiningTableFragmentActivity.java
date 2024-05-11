package com.just.print.ui.panel;

import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.BaseFragmentActivity;

public class DiningTableFragmentActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, TablesPanel.getInstance())
                .commit();
    }
}
