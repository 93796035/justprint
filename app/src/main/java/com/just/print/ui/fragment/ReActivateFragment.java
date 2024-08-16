package com.just.print.ui.fragment;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BaseFragment;
import com.just.print.app.Options;
import com.just.print.util.AppUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

public class ReActivateFragment extends BaseFragment{

    @XViewByID(R.id.inputCodeText)
    EditText inputCodeText;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_activate;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        inputCodeText.setHint("SN code for " + Options.getShopName());
    }

    @XClick({R.id.inputCodeButton})
    void submit(@XGetValueByView(fromId = R.id.inputCodeText) TextView textView) {
        //check if has internet.
        if(!AppUtils.hasInternet(AikaPos.instance.getApplicationContext())){
            showToast("Please connect the device to internet, then try again.");
            return;
        }

        String inputedSN = String.valueOf(textView.getText()).trim();
        Options.setActivateCode(inputedSN, textView);

        getActivity().finish();
    }

}
