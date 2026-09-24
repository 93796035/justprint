package org.cas.client.platform.bar.action;

public class Cmd_StockIn extends SecurityFeatureAction {
    private static Cmd_StockIn instance;
    private Cmd_StockIn() {}
    public static Cmd_StockIn getInstance() {
        if (instance == null) instance = new Cmd_StockIn();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Stock in";
    }
}
