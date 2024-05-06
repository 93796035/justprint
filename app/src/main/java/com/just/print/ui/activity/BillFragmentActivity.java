package com.just.print.ui.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.just.print.R;
import com.just.print.app.BarOption;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.ui.adapter.MyPagerAdapter;
import com.just.print.ui.fragment.OrderCategoryFragment;
import com.just.print.ui.fragment.OrderIdentifierFragment;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.List;

public class BillFragmentActivity extends BaseFragmentActivity {
    @XViewByID(R.id.pager)
    private ViewPager viewPager;  //对应的viewPager
    private PagerAdapter adapter = null;

    private static List<Fragment> fragmentList = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        new StupidReflect(this).init();
        if(BarOption.hasCategory()) {
            fragmentList.add(OrderCategoryFragment.getInstance());
        }
        //fragmentList.add(new OrderMenuFragment());
        fragmentList.add(OrderIdentifierFragment.getInstance());
        adapter = new MyPagerAdapter(getSupportFragmentManager(), fragmentList);
        try {
            if (viewPager != null) {
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(1);
                findViewById(R.id.pager).setBackgroundColor(BarOption.getThemColor());
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    public void onPageScrollStateChanged(int state) {}
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                    public void onPageSelected(int position) {
                        if(position == 0 && BarOption.hasCategory()){
                            ((OrderCategoryFragment)fragmentList.get(0)).categoryExXAdapter.notifyDataSetChanged();
                       }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
