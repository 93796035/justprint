package com.just.print.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.BarOption;
import com.just.print.app.BaseFragment;
import com.just.print.util.DbUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;

import java.util.HashMap;
import java.util.List;

public class UploadingConfigConfirmFragment extends BaseFragment{

    private HashMap<String,List<String>> contentForPrintMap;

    @Override
    protected int getLayoutId() {
        return R.layout.uploading_config_confirm_fragment;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();

        String lastSyncDate = BarOption.getLastModifyTime();
        if(lastSyncDate == null || lastSyncDate.length() == 0){
            lastSyncDate = "1";
        }
    }

    @XClick({R.id.btnConfirmUploadOK})
    private void onButtonOK(){
        String license = BarOption.getLicense();
        if(license.length() < 6){
            showToast("Please activate the application first.");
            return;
        }
        DbUtil.syncConfigOntoServer(license, BarOption.getShopName(),true);
        getActivity().finish();
    }

    @XClick({R.id.buttonCancel})
    private void onButtonCancel(){
        getActivity().finish();
    }

}
