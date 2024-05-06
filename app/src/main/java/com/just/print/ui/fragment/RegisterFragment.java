package com.just.print.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.just.print.R;
import com.just.print.app.BarOption;
import com.just.print.app.BaseFragment;
import com.just.print.net.UDPService;
import com.just.print.sys.model.AbsModel;
import com.just.print.sys.model.QueryShopResult;
import com.just.print.ui.activity.LoginFragmentActivity;
import com.just.print.ui.activity.OrderFragmentActivity;
import com.just.print.util.Base64Util;
import com.just.print.util.DbUtil;
import com.just.print.util.NetUtils;
import com.just.print.util.StringUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;


/**
 * the interface is for inputting shop name and user name.
 */
public class RegisterFragment extends BaseFragment implements UDPService.UDPCallback {
    private static String registeredName;
    public static String getUserName() {
        return registeredName;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.register_fragment;
    }

    @XViewByID(R.id.userName)
    EditText userName;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        registeredName = BarOption.getUserName(getContext());
        String shopName = BarOption.getShopName(getContext());
        if (!StringUtils.isEmpty(registeredName)) {
            userName.setText(registeredName);
            userName.setEnabled(false);
            findViewById(R.id.confirmUserName).setVisibility(View.GONE);
            findViewById(R.id.storeName).setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(shopName)) {
            //showToast(shopName);
            BarOption.createShopDB(getContext(), shopName);
            startActivity(new Intent(getContext(), LoginFragmentActivity.class));
            getActivity().finish();
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


    @Override
    public boolean onCallback(final boolean status, final int requestCode, AbsModel result) {
        switch (requestCode) {
            case 1:
                final QueryShopResult result2 = result == null ? null : (QueryShopResult) result;
                if (status && result2 != null && result2.isExists()) {

                    byte[] datas = Base64Util.decode(result2.getFileBase64());
                    DbUtil.writeDBByte(datas);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status && result2 != null && result2.isExists()) {
                            BarOption.saveShopName(getContext(), result2.getShopName());
                            BarOption.createShopDB(getContext(), result2.getShopName());
                            //showToast("店铺存在");
                            startActivity(new Intent(getContext(), OrderFragmentActivity.class));
                            getActivity().finish();
                        } else {
//                            String string = ((TextView) findViewById(R.id.shopName)).getText().toString();
//                            AppData.saveShopName(getContext(), string);
//                            AppData.createShopDB(getContext(), string);
                            findViewById(R.id.createShop).setVisibility(View.VISIBLE);
                            showToast("store not existing, or no device belongs to this store in network was found");
                        }
                    }
                });
                break;
        }
        return true;
    }

    @XClick({R.id.confirmUserName})
    private void onConfirmUserName(View view) {
        String userName = this.userName.getText().toString().trim();
        if (StringUtils.isEmpty(userName)) {
            showToast("Please input tablet name");
        } else if("asdfas".equals(userName)) {
            showToast("Please input tablet name");
        } else {
            findViewById(R.id.storeName).setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);

            int p = userName.indexOf("-p:");
            if(p > -1){// means theres parameters
                String password = userName.substring(p+3).trim();
                BarOption.putCustomData(BarOption.userPassword, password);
                userName = userName.substring(0, p).trim();
            }
            int n = userName.lastIndexOf("\n");
            if(n > -1){// means theres parameters
                userName = userName.substring(n+1).trim();
            }
            BarOption.saveUserName(getContext(), userName);
            this.userName.setEnabled(false);
        }
    }

    @XClick({R.id.createShop})
    private void createShop(@XGetValueByView(fromId = R.id.shopName) String shopName) {
        if (shopName == null || shopName.trim().length() == 0) {
            showToast("Please input the name of the store.");
            return;
        }
        shopName = shopName.trim();//user might type enter instead of click confirm button....remove the possible "\n" at the end or begining...
        int p = shopName.indexOf("-p:");
        if(p > -1){// means theres parameters
            String password = shopName.substring(p+3).trim();
            BarOption.putCustomData(BarOption.adminPassword, password);
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

        BarOption.saveShopName(getContext(), shopName);
        BarOption.createShopDB(getContext(), shopName);

        DbUtil.createPrinter("192.168.8.10","10",1,1, "");
        DbUtil.createPrinter("192.168.8.20","20",1,1, "");
        DbUtil.createPrinter("192.168.8.30","30",1,1, "");
        DbUtil.createPrinter("192.168.8.40","40",1,1, "");

        //initialize the days left and the stated days on cloud.
        NetUtils.activate();
        startActivity(new Intent(getContext(), OrderFragmentActivity.class));
        getActivity().finish();
    }

}
