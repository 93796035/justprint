package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.print.Command;
import org.cas.client.platform.bar.print.PrintUtil;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.L;

import java.io.OutputStream;
import java.net.Socket;

public class Cmd_OpenDrawer implements SamActionListener {

	private static Cmd_OpenDrawer instance;
	private Cmd_OpenDrawer() {}
	public static Cmd_OpenDrawer getInstance() {
		if(instance == null)
			instance = new Cmd_OpenDrawer();
		return instance;
	}
	
	private ISButton sourceBtn;
	
	public ISButton getSourceBtn() {
		return sourceBtn;
	}
	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}
	
	@Override
	public void onClick(View v) {

	}
}
