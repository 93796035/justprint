package org.cas.client.platform.bar.action;

public class Cmd_BottlePay extends SecurityFeatureAction {
    private static Cmd_BottlePay instance;
    private Cmd_BottlePay() {}
    public static Cmd_BottlePay getInstance() {
        if (instance == null) instance = new Cmd_BottlePay();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Bottle pay";
    }
}
