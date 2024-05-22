package org.cas.client.platform.bar.dialog;

import androidx.fragment.app.DialogFragment;

import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.ErrorUtil;

public class DiscountDlg extends DialogFragment
//        implements ActionListener, ComponentListener, MouseListener
{
	
	private static final String ComboCategoryName = "Combo";
	
	public static String curContent = "";
	public boolean isPercentage = false;
	public static boolean confirmed;
	private ISButton btnSource;
	//flag
	boolean isAllContentSelected;
	private int maxInput;
	
    /**
     * Creates a new instance of ContactDialog
     * 
     * @called by PasteAction 为Copy邮件到联系人应用。
     */
    public DiscountDlg() {
        initComponent();
    }

    /*
     * 对话盒的布局独立出来，为了在对话盒尺寸发生改变后，界面各元素能够重新布局， 使整体保持美观。尤其在Linux系列的操作系统上，所有的对话盒都必须准备好应对用户的拖拉改变尺寸。
     * @NOTE:因为setBounds方法本身不会触发事件导致重新布局，所以本方法中设置Bounds之后调用了reLayout。
     */
    public void reLayout() {

//    	btnCombo.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP, btnCombo.getPreferredSize().width, btnCombo.getPreferredSize().height);
//        tfdQTY.setBounds(100, CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM * 2, CustOpts.BTN_HEIGHT + CustOpts.VER_GAP);
//        lblQTY.setBounds(tfdQTY.getX() + tfdQTY.getWidth() + CustOpts.HOR_GAP, tfdQTY.getY(), lblQTY.getPreferredSize().width, tfdQTY.getHeight());
//
//        btn5.setBounds(CustOpts.HOR_GAP,  lblQTY.getY() + lblQTY.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn10.setBounds(btn5.getX() + btn5.getWidth() + CustOpts.HOR_GAP, btn5.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn15.setBounds(btn5.getX(), btn5.getY() + btn5.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn20.setBounds(btn10.getX(), btn15.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn25.setBounds(btn15.getX(), btn15.getY() + btn15.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn30.setBounds(btn20.getX(), btn25.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn40.setBounds(btn25.getX(), btn25.getY() + btn25.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        btn50.setBounds(btn30.getX(), btn30.getY() + btn30.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//
//        num1.setBounds(btn10.getX() + btn10.getWidth() + CustOpts.HOR_GAP * 2, btn10.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num2.setBounds(num1.getX() + num1.getWidth() + CustOpts.HOR_GAP, num1.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num3.setBounds(num2.getX() + num2.getWidth() + CustOpts.HOR_GAP, num1.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num4.setBounds(num1.getX(), num1.getY() + num1.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num5.setBounds(num4.getX() + num4.getWidth() + CustOpts.HOR_GAP, num4.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num6.setBounds(num5.getX() + num5.getWidth() + CustOpts.HOR_GAP, num5.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num7.setBounds(num4.getX(), num4.getY() + num1.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num8.setBounds(num7.getX() + num7.getWidth() + CustOpts.HOR_GAP, num7.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num9.setBounds(num8.getX() + num8.getWidth() + CustOpts.HOR_GAP, num8.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        num0.setBounds(num7.getX(), num7.getY() + num7.getHeight() + CustOpts.VER_GAP, CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        point.setBounds(num0.getX() + num0.getWidth() + CustOpts.HOR_GAP, num0.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        if(point.isVisible())
//        	back.setBounds(point.getX() + point.getWidth() + CustOpts.HOR_GAP, point.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//        else
//        	back.setBounds(num0.getX() + num0.getWidth() + CustOpts.HOR_GAP, num0.getY(), CustOpts.BTN_WIDTH_NUM * 2 + CustOpts.HOR_GAP, CustOpts.BTN_WIDTH_NUM);
//
//        percent.setBounds(num3.getX() + num3.getWidth() + CustOpts.HOR_GAP, num3.getY(), CustOpts.BTN_WIDTH_NUM, CustOpts.BTN_WIDTH_NUM);
//
//        ok.setBounds(num3.getX() + num3.getWidth() + CustOpts.HOR_GAP,
//        		percent.isVisible() ?  num6.getY() : num3.getY(),
//    			CustOpts.BTN_WIDTH_NUM,
//    			percent.isVisible() ? CustOpts.BTN_WIDTH_NUM * 3 + CustOpts.VER_GAP * 2 : CustOpts.BTN_WIDTH_NUM * 4 + CustOpts.VER_GAP * 3);
//
//        validate();
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
    		}else {
        		ErrorUtil.write("String not start with 'x'! when setontents() called in NumberPanelDlg class.");
    		}
    	}else {
    		ErrorUtil.write("unexpected null String when setontents() called in NumberPanelDlg class.");
    	}
//        tfdQTY.setText(qty);
//        tfdQTY.requestFocus();
//        tfdQTY.selectAll();
        isAllContentSelected = true;
    }

//    public void setAction(ActionListener action) {
//    	this.ok.addActionListener(action);
//    	getContentPane().validate();
//    	getContentPane().repaint();
//    	reLayout();
//    }
    
    /**
     * Invoked when an action occurs. NOTE:PIM的绝大多数用于新建和编辑的对话盒，对于确定事件的处理，采用如下规则：
     * 即：先出发监听器事件，监听器根据IPIMDialog接口的方法getContent（）取出对话盒中的 记录。监听器负责将记录存入Model，监听器最后负责将对话盒释放。
     * 目的是让所有对话盒只认识一个叫Record的东西，不认识别的。
     *            动作事件
     */
//    @Override
//    public void onClick(View v) {
//        curContent = tfdQTY.getText();
//
//        Object o = e.getSource();
//        if (o == ok) {
//        	checkContentAndClose();
//			return;
//        }
//
//        if(isAllContentSelected) {
//        	curContent = "";
//        }
//
//        if (o == back) {
//            if (curContent != null && curContent.length() > 0) {
//                tfdQTY.setText(curContent.substring(0, curContent.length() - 1));
//                if(!BarOption.isPointerInputStyle()) {
//                	tfdQTY.setText("");
//                }
//            }
//        } else if(o == btn5) {
//        	setDisCountValueTo(5);
//        } else if(o == btn10) {
//        	setDisCountValueTo(10);
//        } else if(o == btn15) {
//        	setDisCountValueTo(15);
//        } else if(o == btn20) {
//        	setDisCountValueTo(20);
//        } else if(o == btn25) {
//        	setDisCountValueTo(25);
//        } else if(o == btn30) {
//        	setDisCountValueTo(30);
//        } else if(o == btn40) {
//        	setDisCountValueTo(40);
//        } else if(o == btn50) {
//        	setDisCountValueTo(50);
//        } else if (o == num1) {
//            concatReceived("1");
//        } else if (o == num2) {
//            concatReceived("2");
//        } else if (o == num3) {
//            concatReceived("3");
//        } else if (o == num4) {
//            concatReceived("4");
//        } else if (o == num5) {
//            concatReceived("5");
//        } else if (o == num6) {
//            concatReceived("6");
//        } else if (o == num7) {
//            concatReceived("7");
//        } else if (o == num8) {
//            concatReceived("8");
//        } else if (o == num9) {
//            concatReceived("9");
//        } else if (o == num0) {
//            concatReceived("0");
//        } else if(o == point) {
//            concatReceived(".");
//        } else if(o == percent) {
//            concatReceived("%");
//        }
//
//        isAllContentSelected = false;
//    }
    
    private void concatReceived(String num){
//    	String newStr = tfdQTY.getText().concat(num);
//
//    	if(!BarOption.isPointerInputStyle() && point.isVisible()) {//if the point button not showed up, even if the flag is set, no need to do the twist.
//    		if(num.equals("%")) {
//    			if(newStr.startsWith(".")) {
//    				newStr = newStr.substring(1);
//    			}
//    		}else if(num.equals(".")) {
//    			int p = newStr.indexOf(".");
//        		if(p != newStr.lastIndexOf(".")) {
//        			newStr = newStr.substring(0, p) + newStr.substring(p + 1);
//        		}
//        		newStr = newStr + "00";
//        	}else if(newStr.length() == 1) {
//    			if(!newStr.equals(".")){
//    				newStr = ".0" + newStr;
//    			}
//    		}else if(newStr.length() == 2) {
//    			if(newStr.indexOf(".") < 0) {
//    				newStr = "." + newStr;
//    			}
//    		}else {
//    			int i = newStr.indexOf(".");
//    			if(i >= 0) {
//    				newStr = newStr.substring(0, i) + newStr.substring(i + 1, newStr.length());	//remove the point
//    			}
//    			newStr = newStr.substring(0, newStr.length() - 2) + "." + newStr.substring(newStr.length() - 2, newStr.length()); //put the point right place.
//			}
//    		if(newStr.startsWith("0")) {
//    			newStr = newStr.substring(1);
//    		}
//    	}
//
//    	if(maxInput > 0 && Float.valueOf(newStr) > maxInput) {
//    		JOptionPane.showMessageDialog(this, BarFrame.consts.InvalidInput());
//    	}else {
//    		tfdQTY.setText(newStr);
//    	}
    }
    
	private void checkContentAndClose() {
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
			
			confirmed = true;
//			this.setVisible(false);
		}catch(Exception exp) {
		    JOptionPane.showMessageDialog(this, BarFrame.consts.FORMATERROR());
		}
	}

	private void setDisCountValueTo(int i) {
//		tfdQTY.setText(i+"%");
//		curContent = i+"%";
//		for (ActionListener listener : ok.getActionListeners()) {
//			listener.actionPerformed(new ActionEvent(ok, -1, ""));
//		}
	}

//	@Override
//	public void componentResized(ComponentEvent e) {}
//
//	@Override
//	public void componentMoved(ComponentEvent e) {}
//
//	@Override
//	public void componentShown(ComponentEvent e) {}
//
//	@Override
//	public void componentHidden(ComponentEvent e) {
//		if(btnSource != null)
//			btnSource.setSelected(false);
//	}

    void initComponent() {
//    	getContentPane().removeAll();
//
//        setTitle(BarFrame.consts.QTY());
//        setResizable(false);
//        setModal(false);
//        //setAlwaysOnTop(true);
//
//        // 初始化－－－－－－－－－－－－－－－－
//        btnCombo = new JButton(BarFrame.consts.COMBO());
//        tfdQTY = new JTextField();
//        lblQTY = new JLabel(BarFrame.consts.QTYNOTICE());
//        ok = new NumButton("<html><h1 style='text-align: center; padding-bottom: 5px; color:#18F507;'>✔</h1></html>");
//        back = new NumButton("←");
//
//        btn5 = new JButton("5%");
//        btn10 = new JButton("10%");
//        btn15 = new JButton("15%");
//        btn20 = new JButton("20%");
//        btn25 = new JButton("25%");
//        btn30 = new JButton("30%");
//        btn40 = new JButton("40%");
//        btn50 = new JButton("50%");
//
//        num1 = new NumButton("1");
//        num2 = new NumButton("2");
//        num3 = new NumButton("3");
//        num4 = new NumButton("4");
//        num5 = new NumButton("5");
//        num6 = new NumButton("6");
//        num7 = new NumButton("7");
//        num8 = new NumButton("8");
//        num9 = new NumButton("9");
//        num0 = new NumButton("0");
//        point = new NumButton(BarOption.isPointerInputStyle() ? "." : "00");
//        percent = new JButton("%");
//
//        // 属性设置－－－－－－－－－－－－－－
//        // ok.setFont(CustOpts.custOps.getFontOfDefault());
//        getContentPane().setBackground(BarOption.getBK("Login"));
//        tfdQTY.setHorizontalAlignment(SwingConstants.RIGHT);
//        back.setMargin(new Insets(0, 0, 0, 0));
//        ok.setMargin(new Insets(0, 0, 0, 0));
//        btn5.setMargin(back.getMargin());
//        btn10.setMargin(back.getMargin());
//        btn15.setMargin(back.getMargin());
//        btn20.setMargin(back.getMargin());
//        btn25.setMargin(back.getMargin());
//        btn30.setMargin(back.getMargin());
//        btn40.setMargin(back.getMargin());
//        btn50.setMargin(back.getMargin());
//        num1.setMargin(back.getMargin());
//        num2.setMargin(back.getMargin());
//        num3.setMargin(back.getMargin());
//        num4.setMargin(back.getMargin());
//        num5.setMargin(back.getMargin());
//        num6.setMargin(back.getMargin());
//        num7.setMargin(back.getMargin());
//        num8.setMargin(back.getMargin());
//        num9.setMargin(back.getMargin());
//        num0.setMargin(back.getMargin());
//        point.setMargin(back.getMargin());
//        percent.setMargin(back.getMargin());
//        // 布局---------------
//        int tHight = CustOpts.BTN_HEIGHT + CustOpts.BTN_WIDTH_NUM * 4 + 7 * CustOpts.VER_GAP
//                        + CustOpts.SIZE_EDGE + CustOpts.SIZE_TITLE;
//        int tWidth = (CustOpts.BTN_WIDTH_NUM + CustOpts.HOR_GAP) * 6 + CustOpts.SIZE_EDGE * 2 + CustOpts.HOR_GAP + 5;
//
//        setBounds((CustOpts.SCRWIDTH - tWidth) / 2, (CustOpts.SCRHEIGHT - tHight) / 2, tWidth, tHight); // 对话框的默认尺寸。
//        getContentPane().setLayout(null);
//
//        // 搭建－－－－－－－－－－－－－
//        getContentPane().add(btnCombo);
//        getContentPane().add(tfdQTY);
//        getContentPane().add(lblQTY);
//        getContentPane().add(ok);
//        getContentPane().add(back);
//        getContentPane().add(btn5);
//        getContentPane().add(btn10);
//        getContentPane().add(btn15);
//        getContentPane().add(btn20);
//        getContentPane().add(btn25);
//        getContentPane().add(btn30);
//        getContentPane().add(btn40);
//        getContentPane().add(btn50);
//        getContentPane().add(num1);
//        getContentPane().add(num2);
//        getContentPane().add(num3);
//        getContentPane().add(num4);
//        getContentPane().add(num5);
//        getContentPane().add(num6);
//        getContentPane().add(num7);
//        getContentPane().add(num8);
//        getContentPane().add(num9);
//        getContentPane().add(num0);
//        getContentPane().add(point);
//        getContentPane().add(percent);
//
//        // 加监听器－－－－－－－－
//        btnCombo.addMouseListener(this);
//        lblQTY.addMouseListener(this);
//        ok.addActionListener(this);
//        back.addActionListener(this);
//        btn5.addActionListener(this);
//        btn10.addActionListener(this);
//        btn15.addActionListener(this);
//        btn20.addActionListener(this);
//        btn25.addActionListener(this);
//        btn30.addActionListener(this);
//        btn40.addActionListener(this);
//        btn50.addActionListener(this);
//        num1.addActionListener(this);
//        num2.addActionListener(this);
//        num3.addActionListener(this);
//        num4.addActionListener(this);
//        num5.addActionListener(this);
//        num6.addActionListener(this);
//        num7.addActionListener(this);
//        num8.addActionListener(this);
//        num9.addActionListener(this);
//        num0.addActionListener(this);
//        point.addActionListener(this);
//        percent.addActionListener(this);
//        addComponentListener(this);
//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//            	curContent = "";
//            }
//        });
//
//        // init Contents
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                tfdQTY.grabFocus();
//            }
//        });
//        reLayout();
    }

	public void setBtnSource(ISButton btnSource) {
		this.btnSource = btnSource;
		confirmed = false;
		curContent = "";
//		tfdQTY.setText("");
	}

	public void setFloatSupport(boolean setFloatSupport) {
//		point.setVisible(setFloatSupport);
		reLayout();
	}

	public void setPercentSupport(boolean setPercentSupport) {
//		percent.setVisible(setPercentSupport);
	}
	
	public void setNotice(String notice) {
//		lblQTY.setText(notice);
	}
	
	private BarFrame barFrame;
//    private NumButton num1;
//    private NumButton num2;
//    private NumButton num3;
//    private NumButton num4;
//    private NumButton num5;
//    private NumButton num6;
//    private NumButton num7;
//    private NumButton num8;
//    private NumButton num9;
//    private NumButton num0;
//    private NumButton point;
//    private NumButton back;
//    private JButton percent;
//    private NumButton ok;
//
//    private JButton btn5;
//    private JButton btn10;
//    private JButton btn15;
//    private JButton btn20;
//    private JButton btn25;
//    private JButton btn30;
//    private JButton btn40;
//    private JButton btn50;
//
//    public JTextField tfdQTY;
//    private JLabel lblQTY;
//    private JButton btnCombo;

//	@Override
//	public void mouseClicked(MouseEvent e) {
//		if(e.getSource() == lblQTY && e.getClickCount() > 1 || e.getSource() == btnCombo) {
//			StringBuilder content = new StringBuilder();
//			StringBuilder noticeForContent = new StringBuilder();
//
//			//check the selection content:
//			SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
//			BillPanel billPanel = salesPanel.billPanel;
//			ArrayList<Dish> selection = billPanel.getOrderedDishAry();
//			for (Dish dish : selection) {
//				content.append(",").append(dish.getId());
//				noticeForContent.append(",").append(BarUtil.getPlainTextOut(dish.getLanguage(CustOpts.custOps.getUserLang())));
//			}
//			if(content.length() < 2) {
//				JOptionPane.showMessageDialog(null, BarFrame.consts.InvalidInput());
//				return;
//			}
//
//			//check discount content.
//			String actionStr = tfdQTY.getText();
//			if(actionStr == null || actionStr.length() == 0) {
//				JOptionPane.showMessageDialog(null, BarFrame.consts.InvalidInput());
//				return;
//			}
//
//			//ask for a name
//			StringBuilder notice = new StringBuilder(BarFrame.consts.IfContains()).append(noticeForContent.substring(1))
//					.append(BarFrame.consts.willDiscount()).append(BarOption.getMoneySign()).append(actionStr)
//					.append(BarFrame.consts.giveAName());
//			String ruleName = JOptionPane.showInputDialog(null, notice.append(BarFrame.consts.ComboName()));
//			if(ruleName == null || ruleName.length() == 0) {
//				return;
//			}
//
//			Float f;
//			if(actionStr.endsWith("%")) {
//		    	String tContent = actionStr.substring(0, actionStr.length() - 1);
//				f = Float.valueOf(tContent);
//				if(f > 100) {
//					JOptionPane.showMessageDialog(null, BarFrame.consts.InvalidInput());
//					return;
//				}
//			}else {
//				f = Float.valueOf(actionStr) * 100;
//			}
//			//sql = "CREATE CACHED TABLE Customized_Rule (id INTEGER IDENTITY PRIMARY KEY, ruleName VARCHAR(255),  content VARCHAR(255), action INTEGER, status INTEGER,  ext1 VARCHAR(255),  ext2 VARCHAR(255),  ext3 VARCHAR(255),)";
//			StringBuilder sql = new StringBuilder("INSERT INTO Customized_Rule (ruleName, content, action, status, dspIdx) VALUES ('").append(ruleName)
//				    .append("', '").append(content.substring(1)).append("', ").append(Math.round(f)).append(", 0, 0)");
//
//			try {
//    			PIMDBModel.getStatement().executeUpdate(sql.toString());
//    		}catch(Exception exp) {
//    			L.e("DiscountDlg ", "Exception when insert a rule into Customized_Rule.", exp);
//    		}
//
//			Rule rule = new Rule();
//			sql = new StringBuilder("select * from Customized_Rule where ruleName = '").append(ruleName)
//					.append("' and content = '").append(content.substring(1)).append("'");
//			try {
//	            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//	            rs.beforeFirst();
//	            rs.moveToNext();
//	            rule.setId(DBUtil.getCursorInt(rs,"id"));
//	            rule.setRuleName(DBUtil.getCursorString(rs,"ruleName"));
//	            rule.setContent(DBUtil.getCursorString(rs,"content"));
//	            rule.setAction(DBUtil.getCursorInt(rs,"action"));
//	            rs.close();// 关闭
//	        } catch (SQLException exp) {
//	            L.e("DiscountDlg ", "Exception when fetching a rule." + sql, exp);
//	        }
//
//			Rule[] rules = BarFrame.menuPanel.getRules();
//			Rule[] rules2 = new Rule[rules.length + 1];
//			System.arraycopy(rules, 0, rules2, 0, rules.length);
//
//			rules2[rules.length] = rule;
//			BarFrame.menuPanel.setRules(rules2);
//
//			if(JOptionPane.showConfirmDialog(null, BarFrame.consts.ComboCreated() + BarFrame.consts.createComboMenu(),
//					 BarFrame.consts.ComboMenu(), JOptionPane.YES_OPTION) == 0) {
//				//if selected yes!
//				//Start to create the button.
//				String category = ComboCategoryName;
//				String menuName = ruleName;
//				CreateNewOrderAction.getInstance().makeSureCategoryExist(category);
//				CreateNewOrderAction.getInstance().makeSureDishExist(category, menuName, -1);
//			}
//
//		}
//	}
//
//	@Override
//	public void mousePressed(MouseEvent e) {}
//
//	@Override
//	public void mouseReleased(MouseEvent e) {}
//
//	@Override
//	public void mouseEntered(MouseEvent e) {}
//
//	@Override
//	public void mouseExited(MouseEvent e) {}

	public void setComboBtnVisible(boolean b) {
//		btnCombo.setVisible(b);
	}
    
}
