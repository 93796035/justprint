package org.cas.client.platform.bar.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.cascustomize.CustOpts;

public class Cmd_Menu implements SamActionListener {

	private static Cmd_Menu instance;
	private Cmd_Menu() {}
	public static Cmd_Menu getInstance() {
		if(instance == null)
			instance = new Cmd_Menu();
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
	public void actionPerformed(ActionEvent e) {
		JDialog dialog = new JDialog(BarFrame.instance, true);
		JPanel jpanel = new JPanel();
		jpanel.setPreferredSize(new Dimension(5, 10));
		dialog.add(jpanel,BorderLayout.WEST);
		dialog.add(BarFrame.instance.menuPanel);
		 
		Dimension tmpSrcSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = BarFrame.instance.menuPanel.getWidth() + CustOpts.HOR_GAP * 2 + CustOpts.SIZE_EDGE * 2;
		int height = BarFrame.instance.menuPanel.getHeight() + CustOpts.VER_GAP * 2 + CustOpts.SIZE_TITLE;
		dialog.setBounds((tmpSrcSize.width - width) / 2, (tmpSrcSize.height - height) / 2,
				width, height);
		dialog.setVisible(true);
	}
}
