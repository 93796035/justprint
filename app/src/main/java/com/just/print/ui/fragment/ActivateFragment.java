package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.just.print.R;
import com.just.print.app.BarFrame;
import com.just.print.app.Options;
import com.just.print.util.AppUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
//when system start up, MainFragment will decide to start loginFragment or this fragment
//base on the timeleft > 0 or not, at that time, configMainFragmentActivity and relevant fragment not exist...
//I guess that's the reason of this class exists here.
public class ActivateFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activate, container, false);
        new StupidReflect(this, view).init();
        return view;
    }

    @XClick({R.id.inputCodeButton})
    void submit(@XGetValueByView(fromId = R.id.inputCodeText) TextView textView) {
        //check if has internet.
        if (!AppUtils.hasInternet(getContext())) {
            Toast.makeText(getContext(), "Please connect the device to internet, then try again.", Toast.LENGTH_LONG).show();
            return;
        }

        String inputedSN = String.valueOf(textView.getText());
        Options.setActivateCode(inputedSN, textView);
        // Start a new thread to keep checking the activation status.
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    AppUtils.sleep(1000);  // Sleep for a while before checking again.
                    String number = Options.getCustomData(Options.number);
                    if (Long.valueOf(number) > 0) {
                        // If the condition is met, switch to the RegisterFragment.
                        if (isAdded()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((BarFrame) getActivity()).switchFragment(new RegisterFragment());
                                }
                            });
                        }
                        break;
                    }
                }
            }
        }).start();
    }
}
