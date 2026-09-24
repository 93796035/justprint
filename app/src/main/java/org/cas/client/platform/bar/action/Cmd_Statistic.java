package org.cas.client.platform.bar.action;

public class Cmd_Statistic extends SecurityFeatureAction {
    private static Cmd_Statistic instance;
    private Cmd_Statistic() {}
    public static Cmd_Statistic getInstance() {
        if (instance == null) instance = new Cmd_Statistic();
        return instance;
    }
    @Override
    protected void run() {
        openReport();
    }
    @Override
    protected String featureName() {
        return "Statistic";
    }
}
