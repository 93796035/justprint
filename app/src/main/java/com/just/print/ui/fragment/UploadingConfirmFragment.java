package com.just.print.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.BaseFragment;
import com.just.print.app.Options;
import com.just.print.util.DbUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;

import java.util.HashMap;
import java.util.List;

public class UploadingConfirmFragment extends BaseFragment{

    private HashMap<String,List<String>> contentForPrintMap;

    @Override
    protected int getLayoutId() {
        return R.layout.uploading_confirm_fragment;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();

        String lastSyncDate = Options.getLastModifyTime();
        if(lastSyncDate == null || lastSyncDate.length() == 0){
            lastSyncDate = "1";
        }
    }

    @XClick({R.id.btnConfirmUploadOK})
    private void onButtonOK(){
        String license = Options.getLicense();
        if(license.length() < 6){
            showToast("Please activate the application first.");
            return;
        }
        DbUtil.syncDbOntoServer(license, Options.getShopName(),true);
        getActivity().finish();
    }

    @XClick({R.id.buttonCancel})
    private void onButtonCancel(){
        getActivity().finish();
    }

}
