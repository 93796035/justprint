package org.cas.client.platform.bar.uibeans;

import android.view.View;

import org.cas.client.platform.bar.action.SamActionListener;

public interface ISButton {
    ISButton clone();

    void addActionListener(SamActionListener actionListener);

    //for satisfy JToogleButton interfaces
    void setSelected(boolean b);

    boolean isSelected();

    //for satisfy JButton
    String getText();

    void setText(String text);

    void setBounds(int i, int i1, int btnWidth, int btnHeight);

    void setEnabled(boolean b);
}
