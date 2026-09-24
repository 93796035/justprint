package org.cas.client.platform.bar.action;

public class Cmd_SalesHistory extends SecurityFeatureAction {
    private static Cmd_SalesHistory instance;
    private Cmd_SalesHistory() {}
    public static Cmd_SalesHistory getInstance() {
        if (instance == null) instance = new Cmd_SalesHistory();
        return instance;
    }
    @Override
    protected void run() {
        openReport();
    }
    @Override
    protected String featureName() {
        return "Sales history";
    }
}
