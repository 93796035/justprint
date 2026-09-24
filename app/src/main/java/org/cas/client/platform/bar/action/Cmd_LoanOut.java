package org.cas.client.platform.bar.action;

public class Cmd_LoanOut extends SecurityFeatureAction {
    private static Cmd_LoanOut instance;
    private Cmd_LoanOut() {}
    public static Cmd_LoanOut getInstance() {
        if (instance == null) instance = new Cmd_LoanOut();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Loan out";
    }
}
