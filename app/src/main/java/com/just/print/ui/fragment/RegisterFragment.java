package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.just.print.R;
import com.just.print.app.BarFrame;
import com.just.print.app.BaseFragment;
import com.just.print.app.Options;
import com.just.print.util.NetUtils;
import com.just.print.util.StringUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;


/**
 * the interface is for inputting shop name and user name.
 */
public class RegisterFragment extends BaseFragment{
    private static String registeredName;
    public static String getUserName() {
        return registeredName;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_register;
    }

    @XViewByID(R.id.deviceId)
    EditText userName;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        registeredName = Options.getUserName(getContext());
        String shopName = Options.getShopName(getContext());
        if (!StringUtils.isEmpty(registeredName)) {
            userName.setText(registeredName);
            userName.setEnabled(false);
            findViewById(R.id.confirmDeviceId).setVisibility(View.GONE);
            findViewById(R.id.mandatoryCodeArea).setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(shopName)) {
            BarFrame.instance.switchFragment(null);
            LoginDlg.getInstance().show(getActivity().getSupportFragmentManager(), "login_dialog");
        }
    }

//    @XClick(R.id.queryShop)
//    private void queryShop(@XGetValueByView(fromId = R.id.shopName) String shopName) {
//        if (StringUtils.isEmpty(shopName)) {
//            showToast("Please input the name for the store.");
//            return;
//        }
//        if (!AppData.existShop(getContext(), shopName)) {
//            QueryShopRequest request = new QueryShopRequest();
//            request.setShopName(shopName);
//            AikaStyle.app.mUDPService.sendRequest(request, 1, this);
//        }
//    }


    @XClick({R.id.confirmDeviceId})
    private void onConfirmUserName(View view) {
        String userName = this.userName.getText().toString().trim();
        if (StringUtils.isEmpty(userName)) {
            showToast("Please input tablet name");
        } else if("asdfas".equals(userName)) {
            showToast("Please input tablet name");
        } else {
            findViewById(R.id.mandatoryCodeArea).setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);

            int p = userName.indexOf("-p:");
            if(p > -1){// means theres parameters
                String password = userName.substring(p+3).trim();
                Options.putCustomData(Options.userPassword, password);
                userName = userName.substring(0, p).trim();
            }
            int n = userName.lastIndexOf("\n");
            if(n > -1){// means theres parameters
                userName = userName.substring(n+1).trim();
            }
            Options.saveUserName(getContext(), userName);
            this.userName.setEnabled(false);
        }
    }

    @XClick({R.id.createShop})
    private void createShop(@XGetValueByView(fromId = R.id.mandatoryCode) String shopName) {
        if (shopName == null || shopName.trim().length() == 0) {
            showToast("Please input the name of the store.");
            return;
        }
        shopName = shopName.trim();//user might type enter instead of click confirm button....remove the possible "\n" at the end or begining...
        int p = shopName.indexOf("-p:");
        if(p > -1){// means theres parameters
            String password = shopName.substring(p+3).trim();
            Options.putCustomData(Options.adminPassword, password);
            shopName = shopName.substring(0, p).trim();
        }

        if(shopName.contains("\n")){
            String first = shopName.substring(0, shopName.indexOf("\n"));
            int n = shopName.lastIndexOf("\n");
            String last = shopName.substring(n + 1);
            if(first.equals(last)){
                shopName = first;
            }else{
                shopName = first.length() > last.length() ? first : last;
            }
        }

        Options.saveShopName(getContext(), shopName);
        //initialize the days left and the stated days on cloud.
        NetUtils.activate();

        BarFrame.instance.switchFragment(null);
        LoginDlg.getInstance().show(getActivity().getSupportFragmentManager(), "login_dialog");
    }

}
