package org.cas.client.platform.bar.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.i18n.BarDlgConst;
import org.cas.client.platform.bar.i18n.BarDlgConst0;
import org.cas.client.platform.bar.i18n.BarDlgConst1;
import org.cas.client.platform.bar.i18n.BarDlgConst2;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.CASUtility;
import org.cas.client.platform.casutil.PIMPool;
import org.cas.client.resource.international.PaneConsts;

public class CustomerFrame extends JWindow implements ComponentListener{
	Image temp = null;
	private ArrayList<ImageIcon> icons;
	private int curIdx;

	public CustomerFrame() {
		initComponent();
	}
 
	void initComponent() {
		int langIdx = BarOption.getCustomerFrameLang();
		switch (langIdx) {
			case 0:
	       		consts = new BarDlgConst0();
	            break;
	        case 1:
	       		consts = new BarDlgConst1();
	            break;
	        case 2:
	       		consts = new BarDlgConst2();
	            break;
	        default:
	        	consts = new BarDlgConst1();
		}
        billPanel = new BillPanel(this, consts);
		
		//init the adv pictures
		icons = new ArrayList<>();
    	String fileName = CASUtility.getPIMDirPath() + CustOpts.custOps.getValue("CustomerFrameBK");//"CustomerFrameBG.jpg";
		if(new File(fileName + ".jpg").exists()) {
			icons.add(new ImageIcon(fileName + ".jpg"));
		}
		
		int i = 1;
		while(new File(fileName + i + ".jpg").exists()) {
			icons.add(new ImageIcon(fileName + i + ".jpg"));
			i++;
		}
		if(icons.isEmpty()) {
			icons.add(new ImageIcon(BarOption.getCustomerFrameBK()));
		}
    	imagePanel = new JLabel(icons.get(0));
    	URL url = PIMPool.pool.getClass().getResource(PaneConsts.IAMGE_PATH.concat("logoOnCustomerFrame.jpg"));
    	lblLogo = new JLabel("Powered by AikaPos");//new ImageIcon(url));
    	lblLogo.setForeground(Color.white);
    	lblLogo.setFont(new Font("Arial", Font.ITALIC, 20));
    	lblReceived = new JLabel(consts.Receive() + " : ");
    	lblChange = new JLabel(consts.Change() + " : ");
    	
    	valReceived = new JLabel();
    	valChange = new JLabel();
    	
//    	lblLogo.setBackground(new Color(120, 95, 64));
//    	lblLogo.setOpaque(true);
//    	lblLogo.setFont(BarOption.lessBigFont);
    	
    	lblReceived.setFont(BarOption.moderateBigFont);
    	lblChange.setFont(BarOption.bigFont);
    	valReceived.setFont(BarOption.moderateBigFont);
    	valChange.setFont(BarOption.bigFont);

    	valReceived.setAlignmentX(JLabel.RIGHT);
    	valChange.setAlignmentX(Label.RIGHT);
    	imagePanel.add(lblLogo);
    	
        // properties
        Color bg = BarOption.getBK("Sales");
    	if(bg == null) {
    		bg = new Color(216,216,216);
    	}
		setBackground(bg);
        setLayout(null);
        
        getContentPane().add(billPanel);
        getContentPane().add(imagePanel);
        getContentPane().add(lblReceived);
        getContentPane().add(lblChange);
        getContentPane().add(valReceived);
        getContentPane().add(valChange);

        // add listener
        addComponentListener(this);
		reLayout();
		billPanel.setSize(CustOpts.SCRWIDTH/2 - CustOpts.HOR_GAP * 4, billPanel.getPreferredSize().height);
        billPanel.resetColWidth();
    }
	
    public void reLayout() {
        billPanel.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP, 
        		getWidth()/2 - CustOpts.HOR_GAP * 4, getHeight() - CustOpts.VER_GAP * 9);
        
        imagePanel.setBounds(billPanel.getX() + billPanel.getWidth() + CustOpts.HOR_GAP, billPanel.getY(),
        		getWidth()/2 - CustOpts.HOR_GAP,  getHeight() - CustOpts.VER_GAP * 9  - 200);
        
        int width = lblLogo.getPreferredSize().width;
        int height = lblLogo.getPreferredSize().height;
        lblLogo.setBounds(imagePanel.getWidth() - width, imagePanel.getHeight() - height, width, height);
        
    	lblReceived.setBounds(imagePanel.getX(), imagePanel.getY() + imagePanel.getHeight() + CustOpts.VER_GAP,
    			lblReceived.getPreferredSize().width, 100);
    	lblChange.setBounds(lblReceived.getX(), lblReceived.getY() + lblReceived.getHeight() + CustOpts.VER_GAP, 
    			lblChange.getPreferredSize().width, 100);
    	
    	valReceived.setBounds(lblReceived.getX() + lblReceived.getWidth(), lblReceived.getY(), imagePanel.getWidth() - lblReceived.getWidth(), 100);
    	valChange.setBounds(lblChange.getX() + lblChange.getWidth(), lblChange.getY(), imagePanel.getWidth() - lblChange.getWidth(), 100);
    }

	@Override
	public void componentResized(ComponentEvent e) {
        reLayout();
	}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}

	public BillPanel billPanel;
	public JLabel imagePanel;
	private JLabel lblLogo;
	private JLabel lblReceived;
	private JLabel lblChange;
	private JLabel valReceived;
	private JLabel valChange;
	private BarDlgConst consts;

	public void initContent() {
		billPanel.initContent();
	}

	public void updateChange(String received, String left, boolean isPayingWithCash) {
		valReceived.setText(BarOption.getMoneySign() + received);
		valChange.setText(BarOption.getMoneySign() + left);
		if(!left.startsWith("-") && !isPayingWithCash) {
			lblChange.setText(consts.Tip() + " : ");
		}else {
			lblChange.setText(consts.Change() + " : ");
		}
	}

	public void resetContent() {
		int tColCount = billPanel.table.getColumnCount();
		Object[][] tValues = new Object[0][tColCount];
		billPanel.table.setDataVector(tValues, billPanel.header);
		billPanel.resetColWidth();

		billPanel.updateTotalArea(true);
		updateChange("", "", false);
	}

	public void refresh() {
		curIdx++;
    	if(getWidth() > 0 && getHeight() > 0) {
	   		temp = icons.get(curIdx % icons.size()).getImage().getScaledInstance(getWidth()/2 - CustOpts.HOR_GAP,  getHeight() - CustOpts.VER_GAP * 9 - 200,
				icons.get(curIdx % icons.size()).getImage().SCALE_DEFAULT);
		
	   		imagePanel.setIcon(new ImageIcon(temp));
    	}
    	
		if(BarFrame.instance.curPanelIdx != 2 && (BarFrame.payDlg.changeDlg == null || !BarFrame.payDlg.changeDlg.isVisible())) {
			imagePanel.setBounds(0, 0, getWidth(), getHeight());
	        int width = lblLogo.getPreferredSize().width;
	        int height = lblLogo.getPreferredSize().height;
	        lblLogo.setBounds(imagePanel.getWidth() - width, imagePanel.getHeight() - height, width, height);
			billPanel.setVisible(false);
			lblReceived.setVisible(false);
			lblChange.setVisible(false);
			valChange.setVisible(false);
			valReceived.setVisible(false);
			getContentPane().invalidate();
			getContentPane().revalidate();
			getContentPane().repaint();
		}else {
			reLayout();
			billPanel.setVisible(true);
			lblReceived.setVisible(true);
			lblChange.setVisible(true);
			valChange.setVisible(true);
			valReceived.setVisible(true);
			getContentPane().invalidate();
			getContentPane().revalidate();
		}
	}
	
}
