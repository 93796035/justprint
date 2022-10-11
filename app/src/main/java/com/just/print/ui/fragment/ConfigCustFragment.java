package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.BaseFragment;
import com.just.print.ui.holder.ConfigCustViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigCustFragment extends BaseFragment implements OnClickItemListener {

    @XViewByID(R.id.keysToSync)
    ListView keyToSyncgridView;
    XAdapter2<String> custXAdapter;

    @XViewByID(R.id.cus_name)
    private TextView cusName = null;

    @XViewByID(R.id.cus_value)
    private TextView cusValue = null;

    @Override
    protected int getLayoutId() {
        return R.layout.config_cust_fragment;
    }

    private String onEditCust = null;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }

    @XClick({R.id.verifyPassword})
    private void verifyPassword(@XGetValueByView(fromId = R.id.adminPassword) String password) {
        if(!AppData.getAdminPassword().equals(password)){
            showToast("Please input correct password!");
            return;
        }
        findViewById(R.id.confirmPassword).setVisibility(View.GONE);
        //display the customizes.
        findViewById(R.id.custEditor).setVisibility(View.VISIBLE);
        custXAdapter = new XAdapter2<String>(getContext(), ConfigCustViewHolder.class);
        keyToSyncgridView.setAdapter(custXAdapter);
        custXAdapter.setClickItemListener(this);
        loadCustomizes();
    }

    private void loadCustomizes() {
        List<String> list = Arrays.asList(AppData.keysToSync);
        List<String> model = new ArrayList();
        for (String key : list) {
            model.add(key + ":" + AppData.getCustomData(key));
        }

        List<String> list2 = Arrays.asList(AppData.keysNotToSync);
        for (String key : list2) {
            model.add(key + ":" + AppData.getCustomData(key));
        }

        custXAdapter.setData(model);
        custXAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickItem(View view, int i) {
        final String customization = custXAdapter.getItem(i);
        onEditCust = customization;
        int pos = onEditCust.indexOf(":");
        cusName.setText(customization.substring(0, pos));
        cusValue.setText(customization.substring(pos + 1));
    }

    @XClick({R.id.updateCust})
    private void confirmChange(){
        if (onEditCust != null) {
            AppData.putCustomData(cusName.getText().toString(), cusValue.getText().toString());
            cusName.setText("");
            cusValue.setText("");
            onEditCust = null;
            custXAdapter.notifyDataSetChanged();
            AppData.updataeLastModifyTime(null);
            loadCustomizes();
        }
    }

}
