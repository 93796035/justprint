package org.cas.client.platform.bar.dialog;

import androidx.fragment.app.DialogFragment;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.NumButton;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;

public class NumberPanelDlg extends DialogFragment
//        implements ActionListener, ComponentListener
{
	public static String curContent = "";
	public boolean isPercentage = false;
	public static boolean confirmed;
	private ISButton btnSource;
	//flag
	boolean isAllContentSelected;
	private int maxInput;	//open it if need to set a maxValue for the input number.
	
    /**
     * Creates a new instance of ContactDialog
     * 
     * @called by PasteAction 为Copy邮件到联系人应用。
     */
    public NumberPanelDlg() {
        initComponent();
    }

    /*
     * 对话盒的布局独立出来，为了在对话盒尺寸发生改变后，界面各元素能够重新布局， 使整体保持美观。尤其在Linux系列的操作系统上，所有的对话盒都必须准备好应对用户的拖拉改变尺寸。
     * @NOTE:因为setBounds方法本身不会触发事件导致重新布局，所以本方法中设置Bounds之后调用了reLayout。
     */
    public void reLayout() {

        tfdQTY.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_HEIGHT);
        lblQTY.setBounds(tfdQTY.getX() + tfdQTY.getWidth() + CustOpts.HOR_GAP, tfdQTY.getY(), lblQTY.getPreferredSize().width, lblQTY.getPreferredSize().height);
        
        num1.setBounds(CustOpts.HOR_GAP,  tfdQTY.getY() + tfdQTY.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num2.setBounds(num1.getX() + num1.getWidth() + CustOpts.HOR_GAP, num1.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num3.setBounds(num2.getX() + num2.getWidth() + CustOpts.HOR_GAP, num1.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num4.setBounds(num1.getX(), num1.getY() + num1.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num5.setBounds(num4.getX() + num4.getWidth() + CustOpts.HOR_GAP, num4.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num6.setBounds(num5.getX() + num5.getWidth() + CustOpts.HOR_GAP, num5.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num7.setBounds(num4.getX(), num4.getY() + num1.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num8.setBounds(num7.getX() + num7.getWidth() + CustOpts.HOR_GAP, num7.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num9.setBounds(num8.getX() + num8.getWidth() + CustOpts.HOR_GAP, num8.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        num0.setBounds(num7.getX(), num7.getY() + num7.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        point.setBounds(num0.getX() + num0.getWidth() + CustOpts.HOR_GAP, num0.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        if(point.isVisible())
        	back.setBounds(point.getX() + point.getWidth() + CustOpts.HOR_GAP, point.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        else
        	back.setBounds(num0.getX() + num0.getWidth() + CustOpts.HOR_GAP, num0.getY(), CustOpts.BTN_WIDTH_NUM * 2 + CustOpts.HOR_GAP, CustOpts.BTN_WIDTH_NUM);

        percent.setBounds(num3.getX() + num3.getWidth() + CustOpts.HOR_GAP, num3.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
        
        ok.setBounds(num3.getX() + num3.getWidth() + CustOpts.HOR_GAP,
        		percent.isVisible() ?  num6.getY() : num3.getY(),
    			CustOpts.BTN_WIDTH_NUM, 
    			percent.isVisible() ? CustOpts.BTN_WIDTH_NUM * 3 + CustOpts.VER_GAP * 2 : CustOpts.BTN_WIDTH_NUM * 4 + CustOpts.VER_GAP * 3);

        validate();
    }

    public void setContents(String qty) {
    	
    	if(qty!= null) { //remove the "x" in the string.
    		if(qty.endsWith("x")) {
    			qty = qty.substring(0, qty.length() - 1).trim();
    			try {
    				Integer.valueOf(qty);
    			}catch(Exception e) {
    				ErrorUtil.write("non integer string found! when setontents() called in NumberPanelDlg class.");
    			}
    		}
    	}else {
    		ErrorUtil.write("unexpected null String when setontents() called in NumberPanelDlg class.");
    	}
    	
        tfdQTY.setText(qty);
        tfdQTY.requestFocus();
        tfdQTY.selectAll();
        isAllContentSelected = true;
    }

    public void setAction(ActionListener action) {
    	this.ok.addActionListener(action);
    	getContentPane().validate();
    	getContentPane().repaint();
    	reLayout();
    }
    
    /**
     * Invoked when an action occurs. NOTE:PIM的绝大多数用于新建和编辑的对话盒，对于确定事件的处理，采用如下规则：
     * 即：先出发监听器事件，监听器根据IPIMDialog接口的方法getContent（）取出对话盒中的 记录。监听器负责将记录存入Model，监听器最后负责将对话盒释放。
     * 目的是让所有对话盒只认识一个叫Record的东西，不认识别的。
     * 
     * @param e
     *            动作事件
     */
    @Override
    public void actionPerformed(
            ActionEvent e) {
        curContent = tfdQTY.getText();
        
        Object o = e.getSource();
        if (o == ok) {
        	//check content
        	try {
            	if(curContent.endsWith("%")) {
                	String tContent = curContent.substring(0, curContent.length() - 1);
            		Float f = Float.valueOf(tContent);
            		curContent = String.valueOf(f/100f);
            		isPercentage = true;
            	}else {
            		Float.valueOf(curContent);
            		isPercentage = false;
            	}
        	}catch(Exception exp) {
                JOptionPane.showMessageDialog(this, BarFrame.consts.FORMATERROR());
        		return;
        	}
        	
        	confirmed = true;
        	this.setVisible(false);
            return;
        } 
        
        if(isAllContentSelected) {
        	curContent = "";
        }
        
        if (o == back) {
            if (curContent != null && curContent.length() > 0) {
                tfdQTY.setText(curContent.substring(0, curContent.length() - 1));
                if(!BarOption.isPointerInputStyle()) {
                	tfdQTY.setText("");
                }
            }
        } else if (o == num1) {
        	concatReceived("1");
        } else if (o == num2) {
            concatReceived("2");
        } else if (o == num3) {
            concatReceived("3");
        } else if (o == num4) {
            concatReceived("4");
        } else if (o == num5) {
            concatReceived("5");
        } else if (o == num6) {
            concatReceived("6");
        } else if (o == num7) {
            concatReceived("7");
        } else if (o == num8) {
            concatReceived("8");
        } else if (o == num9) {
            concatReceived("9");
        } else if (o == num0) {
            concatReceived("0");
        } else if(o == point) {
            concatReceived(".");
        } else if(o == percent) {
            concatReceived("%");
        }
        
        isAllContentSelected = false;
    }
    
    private void concatReceived(String num){
    	String newStr = tfdQTY.getText().concat(num);
    	
    	if(!BarOption.isPointerInputStyle() && point.isVisible()) {//if the point button not showed up, even if the flag is set, no need to do the twist.
    		if(num.equals("%")) {
    			if(newStr.startsWith(".")) {
    				newStr = newStr.substring(1);
    			}
    		}else if(num.equals(".")) {
    			int p = newStr.indexOf(".");
        		if(p != newStr.lastIndexOf(".")) {
        			newStr = newStr.substring(0, p) + newStr.substring(p + 1);
        		}
        		newStr = newStr + "00";
        	}else if(newStr.length() == 1) {
    			if(!newStr.equals(".")){
    				newStr = ".0" + newStr;
    			}
    		}else if(newStr.length() == 2) {
    			if(newStr.indexOf(".") < 0) {
    				newStr = "." + newStr;
    			}
    		}else {
    			int i = newStr.indexOf(".");
    			if(i >= 0) {
    				newStr = newStr.substring(0, i) + newStr.substring(i + 1, newStr.length());	//remove the point
    			}
    			newStr = newStr.substring(0, newStr.length() - 2) + "." + newStr.substring(newStr.length() - 2, newStr.length()); //put the point right place.
			}
    		if(newStr.startsWith("0")) {
    			newStr = newStr.substring(1);
    		}
    	}
    	
    	if(maxInput > 0 && Float.valueOf(newStr) > maxInput) {
    		JOptionPane.showMessageDialog(this, BarFrame.consts.InvalidInput());
    	}else {
    		tfdQTY.setText(newStr);
    	}
    }
    
	@Override
	public void componentResized(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {
		if(btnSource != null)
			btnSource.setSelected(false);
	}

    void initComponent() {
    	getContentPane().removeAll();
    	
        setTitle(BarFrame.consts.QTY());
        setResizable(false);
        setModal(false);
        //setAlwaysOnTop(true);

        // 初始化－－－－－－－－－－－－－－－－
        tfdQTY = new JTextField();
        lblQTY = new JLabel(BarFrame.consts.QTYNOTICE());
        ok = new NumButton("<html><h1 style='text-align: center; padding-bottom: 5px; color:#18F507;'>✔</h1></html>");
        back = new NumButton("←");
        num1 = new NumButton("1");
        num2 = new NumButton("2");
        num3 = new NumButton("3");
        num4 = new NumButton("4");
        num5 = new NumButton("5");
        num6 = new NumButton("6");
        num7 = new NumButton("7");
        num8 = new NumButton("8");
        num9 = new NumButton("9");
        num0 = new NumButton("0");
        point = new NumButton(BarOption.isPointerInputStyle() ? "." : "00");
        percent = new JButton("%");

        // 属性设置－－－－－－－－－－－－－－
        // ok.setFont(CustOpts.custOps.getFontOfDefault());

        tfdQTY.setHorizontalAlignment(SwingConstants.RIGHT);
        getContentPane().setBackground(BarOption.getBK("Login"));
        back.setMargin(new Insets(0, 0, 0, 0));
        ok.setMargin(new Insets(0, 0, 0, 0));
        num1.setMargin(back.getMargin());
        num2.setMargin(back.getMargin());
        num3.setMargin(back.getMargin());
        num4.setMargin(back.getMargin());
        num5.setMargin(back.getMargin());
        num6.setMargin(back.getMargin());
        num7.setMargin(back.getMargin());
        num8.setMargin(back.getMargin());
        num9.setMargin(back.getMargin());
        num0.setMargin(back.getMargin());
        point.setMargin(back.getMargin());
        percent.setMargin(back.getMargin());
        // 布局---------------
        int tHight = CustOpts.BTN_HEIGHT + CustOpts.BTN_WIDTH_NUM * 4 + 7 * CustOpts.VER_GAP
                + CustOpts.SIZE_EDGE + CustOpts.SIZE_TITLE;
        int tWidth = (CustOpts.BTN_WIDTH_NUM + CustOpts.HOR_GAP) * 4 + CustOpts.SIZE_EDGE * 2 + CustOpts.HOR_GAP + 5;

        setBounds((CustOpts.SCRWIDTH - tWidth) / 2, (CustOpts.SCRHEIGHT - tHight) / 2, tWidth, tHight); // 对话框的默认尺寸。
        getContentPane().setLayout(null);
        
        // 搭建－－－－－－－－－－－－－
        getContentPane().add(tfdQTY);
        getContentPane().add(lblQTY);
        getContentPane().add(ok);
        getContentPane().add(back);
        getContentPane().add(num1);
        getContentPane().add(num2);
        getContentPane().add(num3);
        getContentPane().add(num4);
        getContentPane().add(num5);
        getContentPane().add(num6);
        getContentPane().add(num7);
        getContentPane().add(num8);
        getContentPane().add(num9);
        getContentPane().add(num0);
        getContentPane().add(point);
        getContentPane().add(percent);

        // 加监听器－－－－－－－－
        ok.addActionListener(this);
        back.addActionListener(this);
        num1.addActionListener(this);
        num2.addActionListener(this);
        num3.addActionListener(this);
        num4.addActionListener(this);
        num5.addActionListener(this);
        num6.addActionListener(this);
        num7.addActionListener(this);
        num8.addActionListener(this);
        num9.addActionListener(this);
        num0.addActionListener(this);
        point.addActionListener(this);
        percent.addActionListener(this);
        addComponentListener(this);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	curContent = "";
            }
        });
        
        // init Contents
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tfdQTY.grabFocus();
            }
        });
        reLayout();
    }

	public void setBtnSource(ISButton btnSource) {
		this.btnSource = btnSource;
		confirmed = false;
		curContent = "";
		tfdQTY.setText("");
	}

	public void setFloatSupport(boolean setFloatSupport) {
		point.setVisible(setFloatSupport);
		reLayout();
	}

	public void setPercentSupport(boolean setPercentSupport) {
		percent.setVisible(setPercentSupport);
	}
	
	public void setNotice(String notice) {
		lblQTY.setText(notice);
	}
	
    private NumButton num1;
    private NumButton num2;
    private NumButton num3;
    private NumButton num4;
    private NumButton num5;
    private NumButton num6;
    private NumButton num7;
    private NumButton num8;
    private NumButton num9;
    private NumButton num0;
    private NumButton point;
    private NumButton back;
    private JButton percent;
    private NumButton ok;

    public JTextField tfdQTY;
    private JLabel lblQTY;
    
}
