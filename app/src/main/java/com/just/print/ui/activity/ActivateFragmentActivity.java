package com.just.print.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.NingMengStyle;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.util.AppUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
//when system start up, MainFragment will decide to start loginFragmentActivity or this activity
//base on the timeleft > 0 or not, at that time, configMainFragmentActivity and relevant fragment not exist...
//I guess that's the reason of this class exists here.
public class ActivateFragmentActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);
        new StupidReflect(this).init();
    }

    @XClick({R.id.inputCodeButton})
    void submit(@XGetValueByView(fromId = R.id.inputCodeText) TextView textView) {
        //check if has internet.
        if(!AppUtils.hasInternet(NingMengStyle.app.getApplicationContext())){
            showToast("Please connect the device to internet, then try again.");
            return;
        }

        String inputedSN = String.valueOf(textView.getText());
        AppData.setActivateCode(inputedSN, textView);

        while(true) {
            //when inputted activate code, should not go to login activity(that activity will check name and store name then go to Main Interface)
            AppUtils.sleep(1000);
            String number = AppData.getCustomData(AppData.number);
            if(Long.valueOf(number) > 0){
                startActivity(new Intent(this, LoginFragmentActivity.class));
                break;
            }
        }
    }

}
