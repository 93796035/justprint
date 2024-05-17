package org.cas.client.platform.bar.action;

import android.view.View;

import org.cas.client.platform.bar.uibeans.ISButton;

public interface SamActionListener extends View.OnClickListener{
	void setSourceBtn(ISButton sourceBtn);
}
