package com.just.print.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.just.print.R;
import com.just.print.app.AikaStyleApplication;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.DiningTable;
import com.just.print.db.expand.DaoExpand;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.List;

public class DiningTableFragment extends BaseFragment{

    private static final String TAG = "TableFragment";

    @XViewByID(R.id.temporallyAddedLabel)

    private static XAdapter2<DiningTable> tableXAdapter;

    public static List<DiningTable> diningTables;
    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loadDiningTables();
        View rootView = inflater.inflate(R.layout.fragment_dining_table, container, false);
        for (DiningTable table : diningTables) {
            Button button = new Button(getActivity());
            button.setText("Table");  // 设置按钮文本，也可以根据table的属性动态设置

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    convertDpToPixel(table.getWidth(), getActivity()),
                    convertDpToPixel(table.getHeight(), getActivity())
            );
            params.leftMargin = convertDpToPixel(table.getPosX(), getActivity());
            params.topMargin = convertDpToPixel(table.getPosY(), getActivity());

            ((RelativeLayout) rootView).addView(button, params);
        }

        return rootView;
    }

    private int convertDpToPixel(int dp, Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static void loadDiningTables() {
        diningTables = DaoExpand.queryAllTables(AikaStyleApplication.instance.getDaoMaster().newSession().getDiningTableDao());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dining_table;
    }

    private static DiningTableFragment instance = null;
    public static DiningTableFragment getInstance(){
        if(instance == null){
            instance = new DiningTableFragment();
        }
        return instance;
    }
}
