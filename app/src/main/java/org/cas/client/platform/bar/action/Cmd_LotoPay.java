package org.cas.client.platform.bar.action;

public class Cmd_LotoPay extends SecurityFeatureAction {
    private static Cmd_LotoPay instance;
    private Cmd_LotoPay() {}
    public static Cmd_LotoPay getInstance() {
        if (instance == null) instance = new Cmd_LotoPay();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Loto pay";
    }
}
