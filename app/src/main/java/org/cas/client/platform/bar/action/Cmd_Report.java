package org.cas.client.platform.bar.action;

import android.database.Cursor;
import android.view.View;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.dialog.statistics.ReportDlg;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.uibeans.ISButton;

public class Cmd_Report implements SamActionListener {

	private static Cmd_Report instance;
	private Cmd_Report() {}
	public static Cmd_Report getInstance() {
		if(instance == null)
			instance = new Cmd_Report();
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
		if(!BarOption.isSingleUser()) {
			LoginDlg.getInstance().noRefresh = true;
			LoginDlg.getInstance().show(BarFrame.instance.getSupportFragmentManager(), "login_dialog");
	        if (LoginDlg.PASSED == true) {
	    		ReportDlg dlg = new ReportDlg();
//	    		dlg.setVisible(true);
	        }
		}else {
			if (BarOption.isNeedLoginBeforeReport()) {
				LoginDlg loginDlg = LoginDlg.getInstance();
				loginDlg.noRefresh = true;

				loginDlg.setOnLoginDismissListener(() -> {  // 无参Lambda表达式
					// 检查登录是否成功
					if (LoginDlg.PASSED) {
						if (LoginDlg.getUSERTYPE() == LoginDlg.SUPER_ADMIN) {
							StringBuilder sql = new StringBuilder("select * from systeminfo order by _id desc limit 1");
							try {
								// 获取与批次ID相关的打印时间
								Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
								if (rs.moveToNext()) {
									String endTime = DBUtil.getCursorString(rs, "version");
									sql = new StringBuilder("select * from Bill where openTime > '").append(endTime).append("'");
									rs = AikaPos.db.rawQuery(sql.toString(), null);
									int tmpPos = rs.getCount();
									if (tmpPos > 0) {
										JOptionPane.showMessageDialog(BarFrame.consts.CloseBatchFirst());
										return;
									}
								}
							} catch (Exception exp) {
								return;
							}
						}
						BarFrame.checkSignIn();

						// 打开报告对话框
						ReportDlg dlg = new ReportDlg();
						dlg.show(BarFrame.instance.getSupportFragmentManager(), "Report");
					}
				});

				// 显示登录对话框
				loginDlg.show(BarFrame.instance.getSupportFragmentManager(), "login_dialog");
			}

		}
	}
}
