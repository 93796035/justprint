package org.cas.client.platform.bar.action;

public class Cmd_InstantPay extends SecurityFeatureAction {
    private static Cmd_InstantPay instance;
    private Cmd_InstantPay() {}
    public static Cmd_InstantPay getInstance() {
        if (instance == null) instance = new Cmd_InstantPay();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Instant pay";
    }
}
