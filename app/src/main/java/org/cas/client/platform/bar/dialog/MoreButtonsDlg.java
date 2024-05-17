package org.cas.client.platform.bar.dialog;

import androidx.fragment.app.DialogFragment;

import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.MoreButton;
import org.cas.client.platform.cascustomize.CustOpts;

import java.util.ArrayList;

public class MoreButtonsDlg extends DialogFragment //implements WindowFocusListener
{
	
    SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
	private ArrayList<ISButton> buttons;

    public MoreButtonsDlg(ArrayList<ISButton> buttons) {
    	super();
    	this.buttons = buttons;
//    	setIconImage(CustOpts.custOps.getFrameLogoImage());
        initPanel();
    }

	public void show(MoreButton btnMore) {
		reLayout(btnMore);
//		this.setVisible(true);
	}
	
	private void reLayout(MoreButton btnMore) {
		int btnWidth = btnMore.getWidth();
		int btnHeight = btnMore.getHeight();
		
        // command buttons--------------
        int buttonQT = buttons.size();
        int row;
        int col;
        if(buttonQT <= 0) {
        	row = 0;
        	col = 0;
        }else if(buttonQT <= 1) {
        	col = 1;
        	row = buttonQT;
        }else {
        	col = 2;
        	row = buttonQT%2 + buttonQT/2;
        }
        
        for(int i = 0; i < buttonQT; i++) {
        	buttons.get(i).setBounds(CustOpts.HOR_GAP + btnWidth * (i < row ? 0 : 1) + CustOpts.HOR_GAP * (i < row ? 0 : 1),
        			CustOpts.VER_GAP + btnHeight * (i < row ? i : i - row) + CustOpts.VER_GAP * (i < row ? i : i - row),
        			btnWidth, btnHeight);
        }
        
        //dialog		
		int width = btnWidth * col + CustOpts.HOR_GAP * (col + 1) + CustOpts.SIZE_EDGE * 2 + 15;
		int height = btnHeight * row + CustOpts.VER_GAP * (row + 1) + CustOpts.SIZE_EDGE * 2 + 50;
//		int x = btnMore.getX() + salesPanel.getRootPane().getParent().getX();
//		int y = salesPanel.getRootPane().getHeight() - 80 + salesPanel.getRootPane().getParent().getY();
//		setBounds(x - width * (col - 1) - 40, y - height + 80, width, height);
	}
	
	private void initPanel() {
		// 布局---------------
//		PicturePane contentPane = new PicturePane();
//		contentPane.setLayout(null);
//		contentPane.MAX_CMDBTN_QT = 80;
//		setContentPane(contentPane);
//		BarUtil.addFunctionButtons(contentPane, buttons);
	}
}
