package com.just.print.ui.activity;

import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.ui.fragment.TableFragment;

public class TableFragmentActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, TableFragment.getInstance())
                .commit();
    }
}
