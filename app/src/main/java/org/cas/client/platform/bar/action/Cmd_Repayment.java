package org.cas.client.platform.bar.action;

public class Cmd_Repayment extends SecurityFeatureAction {
    private static Cmd_Repayment instance;
    private Cmd_Repayment() {}
    public static Cmd_Repayment getInstance() {
        if (instance == null) instance = new Cmd_Repayment();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Repayment";
    }
}
