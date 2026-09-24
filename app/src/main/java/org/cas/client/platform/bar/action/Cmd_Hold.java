package org.cas.client.platform.bar.action;

public class Cmd_Hold extends SecurityFeatureAction {
    private static Cmd_Hold instance;
    private Cmd_Hold() {}
    public static Cmd_Hold getInstance() {
        if (instance == null) instance = new Cmd_Hold();
        return instance;
    }
    @Override
    protected String featureName() {
        return "Hold order";
    }
}
