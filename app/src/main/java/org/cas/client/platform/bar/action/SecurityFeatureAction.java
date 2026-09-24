package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.uibeans.ISButton;

abstract class SecurityFeatureAction implements SamActionListener {

    private ISButton sourceBtn;

    @Override
    public void setSourceBtn(ISButton sourceBtn) {
        this.sourceBtn = sourceBtn;
    }

    public ISButton getSourceBtn() {
        return sourceBtn;
    }

    @Override
    public void onClick(View v) {
        run();
    }

    protected void run() {
        JOptionPane.showMessageDialog(featureName()
                + " is part of the security AikaPos desktop feature set. "
                + "The Android entry is now wired; the dedicated Android screen still needs migration.");
    }

    protected abstract String featureName();

    protected void openReport() {
        Cmd_Report.getInstance().onClick(null);
    }

    protected void openSettings() {
        BarFrame.instance.switchMode(3);
    }
}
