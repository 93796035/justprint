package com.just.print.ui.panel;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.Options;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.sys.server.DataSyncService;
import com.just.print.ui.adapter.MyPagerAdapter;
import com.just.print.ui.fragment.OrderCategoryFragment;
import com.just.print.ui.fragment.OrderIdentifierFragment;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.List;

public class OrderFragmentActivity extends BaseFragmentActivity {
    @XViewByID(R.id.pager)
    private ViewPager viewPager;  //对应的viewPager
    private PagerAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_order);
//        new StupidReflect(this).init();
//        List<Fragment> fragmentList = new ArrayList<Fragment>();
//        if(Options.hasCategory()) {
//            fragmentList.add(OrderCategoryFragment.getInstance());
//        }
//        //fragmentList.add(new OrderMenuFragment());
//        fragmentList.add(OrderIdentifierFragment.getInstance());
//        adapter = new MyPagerAdapter(getSupportFragmentManager(), fragmentList);
//
//        if (viewPager != null) {
//            viewPager.setAdapter(adapter);
//            viewPager.setCurrentItem(1);
//            findViewById(R.id.pager).setBackgroundColor(Options.getThemColor());
//            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                public void onPageScrollStateChanged(int state) {}
//                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
//
//                public void onPageSelected(int position) {
//                    if(position == 0 && Options.hasCategory()){
//                        ((OrderCategoryFragment)fragmentList.get(0)).categoryExXAdapter.notifyDataSetChanged();
//                   }
//                }
//            });
//        }
//        //the application has logged in, then if has has lastDownLoad, then response for sending sms for
//        if(Options.getCustomData(Options.sendSM) != null){
//            DataSyncService dataSyncService = DataSyncService.getInstance();//initialize and start the data sync thread.....
//            dataSyncService.setActivity(this);
//        }
    }
}
