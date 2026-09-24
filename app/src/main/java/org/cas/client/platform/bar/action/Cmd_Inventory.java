package org.cas.client.platform.bar.action;

public class Cmd_Inventory extends SecurityFeatureAction {
    private static Cmd_Inventory instance;
    private Cmd_Inventory() {}
    public static Cmd_Inventory getInstance() {
        if (instance == null) instance = new Cmd_Inventory();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Inventory";
    }
}
