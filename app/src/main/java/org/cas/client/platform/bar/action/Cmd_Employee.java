package org.cas.client.platform.bar.action;

import android.view.View;

import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.dialog.DishDlg;
import org.cas.client.platform.bar.dialog.statistics.EmployeeListDlg;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_Employee implements SamActionListener {

	private static Cmd_Employee instance;
	private Cmd_Employee() {}
	public static Cmd_Employee getInstance() {
		if(instance == null)
			instance = new Cmd_Employee();
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
		new EmployeeListDlg().show(BarFrame.instance.getSupportFragmentManager(), "Employees");
	}
}
