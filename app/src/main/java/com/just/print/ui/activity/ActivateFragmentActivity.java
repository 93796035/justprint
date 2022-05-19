package com.just.print.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.util.AppUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;

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
        if(!AppUtils.hasInternet(Applic.app.getApplicationContext())){
            showToast("Please connect the device to internet, then try again.");
            return;
        }

        String inputedSN = String.valueOf(textView.getText());
        AppData.setActivateCode(inputedSN, textView);

        while(true) {
            //when inputted activate code, should not go to login activity(that activaty will check name and store name then go to Main Interface)
            AppUtils.sleep(1000);
            String number = AppData.getCustomData(AppData.number);
            if(Long.valueOf(number) > 0){
                startActivity(new Intent(this, LoginFragmentActivity.class));
                break;
            }
        }
    }

}
