package com.just.print.ui.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.BaseActivity;
import com.just.print.sys.server.DataSyncService;
import com.just.print.ui.adapter.MyPagerAdapter;
import com.just.print.ui.fragment.OrderCategoryFragment;
import com.just.print.ui.fragment.OrderIdentifierFragment;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends BaseActivity {
    @XViewByID(R.id.pager)
    private ViewPager viewPager;  //对应的viewPager
    private PagerAdapter adapter = null;

    //List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
    private static List<Fragment> fragmentList = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity);
        new StupidReflect(this).init();
        if(AppData.hasCategory()) {
            fragmentList.add(OrderCategoryFragment.getInstance());
        }
        //fragmentList.add(new OrderMenuFragment());
        fragmentList.add(OrderIdentifierFragment.getInstance());
        adapter = new MyPagerAdapter(getSupportFragmentManager(), fragmentList);
        try {
            if (viewPager != null) {
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(1);
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    public void onPageScrollStateChanged(int state) {}
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                    public void onPageSelected(int position) {
                        if(position == 0){
                            ((OrderCategoryFragment)fragmentList.get(0)).categoryExXAdapter.notifyDataSetChanged();
                       }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //the application has logged in, then if has has lastDownload, then response for sending sms for
        if(AppData.getCustomData(AppData.lastDownload) != null){
            DataSyncService.getInstance();//initialize and start the data sync thread.....
        }
    }
/*
    @XClick({R.id.gotoConfig})
    private void gotoConfig() {
        startActivity(new Intent(this, ConfigActivity.class));
    }
*/
}
