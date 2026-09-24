package org.cas.client.platform.bar.action;

public class Cmd_Settlement extends SecurityFeatureAction {
    private static Cmd_Settlement instance;
    private Cmd_Settlement() {}
    public static Cmd_Settlement getInstance() {
        if (instance == null) instance = new Cmd_Settlement();
        return instance;
    }
    @Override
    protected void run() {
        openReport();
    }
    @Override
    protected String featureName() {
        return "Daily settlement";
    }
}
