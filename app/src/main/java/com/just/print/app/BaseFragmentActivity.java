package com.just.print.app;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.just.print.util.ToastUtil;

public class BaseFragmentActivity extends FragmentActivity implements EventBus {
    final EventBusImpl eventBus = new EventBusImpl();
    final public void post(String eventName, Object... argument) {
        eventBus.post(eventName, argument);
    }
    final public void register(String eventName, EventBusImpl.EventHandler handler) {
        eventBus.register(eventName, handler);
    }
    final public void unregister(String eventName) {
        eventBus.unregister(eventName);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static void showToast(String text) {
        ToastUtil.showToast(BarFrame.instance.getBaseContext(), text);
    }
    public FragmentTransaction beginTransaction() {
        return getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
