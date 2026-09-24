package org.cas.client.platform.bar.action;

public class Cmd_CategorySell extends SecurityFeatureAction {
    private static Cmd_CategorySell instance;
    private Cmd_CategorySell() {}
    public static Cmd_CategorySell getInstance() {
        if (instance == null) instance = new Cmd_CategorySell();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Category sell";
    }
}
