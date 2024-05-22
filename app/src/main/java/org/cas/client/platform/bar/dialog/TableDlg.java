package org.cas.client.platform.bar.dialog;

import org.cas.client.platform.bar.uibeans.TableButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 微软的TextField的长度限制是228（约）， 本类的限制因为没有规格约束，暂定为912（约） // ID I NTEGER IDENTITY PRIMARY KEY // DELETED BIT DEFAULT false //
 * language1 CODE VARCHAR(255) // language2 MNEMONIC VARCHAR(255) // language3 SUBJECT VARCHAR(255) // price PRICE
 * INTEGER // gst FOLDERID INTEGER // qst STORE INTEGER // size COST INTEGER // printer BRAND VARCHAR(255) // CATEGORY
 * CATEGORY VARCHAR(255) // prompmenu UNIT VARCHAR(255) // prompprice CONTENT VARCHAR(255) // ModifyPomp
 * VARCHAR(255)
 */
public class TableDlg// extends JDialog implements ICASDialog, ActionListener, ComponentListener
{

	private TabbleSettingDlg settingTableDlg;
	private List<TableButton> btnTables = new ArrayList<TableButton>();	//@NOTE: don't let the logo button inside the list. 
	public static Integer[] typeAry = new Integer[]{0,  11, 12, 13, 14, 21, 22, 31, 32, 33, 34, 41, 42, 43, 44, 61,  62, 63,  64, 81, 82, 83, 84 };

	public static int[] widthAry    = new int[] {   69, 42, 69, 42, 69, 96, 42, 96, 69, 96, 69, 96, 84, 96, 96, 96, 124, 138, 96, 96, 168,168,96};
	public static int[] heightAry   = new int[] {   69, 69, 42, 69, 42, 42, 96, 69, 96, 69, 96, 84, 96, 96, 96, 124, 96, 96, 138, 168,96, 96, 168};
	
	//if called by cmd_AddTable, the settingTabbleDlg will be null!
    public TableDlg(TabbleSettingDlg pFrame, TableButton button) {
//    	super(pFrame);
//    	if(pFrame != null) {
//    		settingTableDlg = pFrame;
//    	}
//    	if(button == null) {
//    		button =  new TableButton();
//    		button.setTypeStatusAndName("", "", 0, 0, "");
//    		button.setId(-1);
//    		btnTables.add(button);
//    	}else {
//    		for (TableButton tableButton : TabbleSettingDlg.btnTables) {
//				if (!tableButton.getBackground().equals(settingTableDlg.bg) && !tableButton.getTableName().equals("logo")) {
//					btnTables.add(tableButton);
//				}
//			}
//    	}
//        initDialog(button);
//        reLayout();
    }

    /*
     * 对话盒的布局独立出来，为了在对话盒尺寸发生改变后，界面各元素能够重新布局， 使整体保持美观。尤其在Linux系列的操作系统上，所有的对话盒都必须准备好应对用户的拖拉改变尺寸。
     * @NOTE:因为setBounds方法本身不会触发事件导致重新布局，所以本方法中设置Bounds之后调用了reLayout。
     */
    public void reLayout() {
//        // name------------
//        sptName.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP, getWidth() - CustOpts.SIZE_EDGE * 2 - CustOpts.HOR_GAP
//               - CustOpts.HOR_GAP, CustOpts.SEP_HEIGHT + 2);
//
//        lblName.setBounds(CustOpts.HOR_GAP * 2, sptName.getY() + sptName.getHeight() + CustOpts.VER_GAP,
//                lblName.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        tfdName.setBounds(lblName.getX() + lblName.getWidth() + CustOpts.HOR_GAP, lblName.getY(),
//        		sptName.getWidth() - lblName.getWidth() - CustOpts.HOR_GAP * 2,  CustOpts.BTN_HEIGHT);
//
//        int otherAreaHeight = LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS ? 0 : 200;
//
//        // bounds--------
//        sptBounds.setBounds(sptName.getX(), tfdName.getY() + tfdName.getHeight() + CustOpts.VER_GAP + otherAreaHeight,
//                sptName.getWidth(), CustOpts.SEP_HEIGHT + 2);
//        lblX.setBounds(CustOpts.HOR_GAP * 2, sptBounds.getY() + sptBounds.getHeight() + CustOpts.VER_GAP,
//        		lblX.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        tfdX.setBounds(lblX.getX() + lblX.getWidth() + CustOpts.HOR_GAP,
//        		lblX.getY(), sptName.getWidth()/2 - lblX.getWidth() - CustOpts.HOR_GAP * 2, CustOpts.BTN_HEIGHT);
//        lblY.setBounds(tfdX.getX() + tfdX.getWidth() + CustOpts.HOR_GAP, lblX.getY(),
//        		lblY.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        tfdY.setBounds(lblY.getX() + lblY.getWidth() + CustOpts.HOR_GAP, lblY.getY(),
//        		sptName.getWidth()/2 - lblY.getWidth() - CustOpts.HOR_GAP * 2, CustOpts.BTN_HEIGHT);
        
//        lblWidth.setBounds(CustOpts.HOR_GAP * 2, lblX.getY() + lblX.getHeight() + CustOpts.VER_GAP,
//        		lblWidth.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        tfdWidth.setBounds(lblWidth.getX() + lblWidth.getWidth() + CustOpts.HOR_GAP,
//        		lblWidth.getY(), sptName.getWidth()/2 - lblWidth.getWidth() - CustOpts.HOR_GAP * 2, CustOpts.BTN_HEIGHT);
//        
//        lblHeight.setBounds(tfdWidth.getX() + tfdWidth.getWidth() + CustOpts.HOR_GAP, lblWidth.getY(), 
//        		lblHeight.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        tfdHeight.setBounds(lblHeight.getX() + lblHeight.getWidth() + CustOpts.HOR_GAP, lblHeight.getY(), 
//        		sptName.getWidth()/2 - lblHeight.getWidth() - CustOpts.HOR_GAP * 2, CustOpts.BTN_HEIGHT);
//
//        // other-----------
//        sptType.setBounds(sptBounds.getX(), lblX.getY() + CustOpts.BTN_HEIGHT + CustOpts.VER_GAP,
//        		sptBounds.getWidth(), CustOpts.SEP_HEIGHT + 2);
//        lblCategory.setBounds(sptType.getX() + CustOpts.HOR_GAP, sptType.getY() + sptType.getHeight()
//                + CustOpts.VER_GAP, lblCategory.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        cmbCategory.setBounds(lblCategory.getX() + lblCategory.getWidth() + CustOpts.HOR_GAP, lblCategory.getY(),
//                sptName.getWidth() - lblCategory.getX() - lblCategory.getWidth() - CustOpts.HOR_GAP * 4 - 40, CustOpts.BTN_HEIGHT);
//
//        ok.setBounds(getWidth() / 2 - CustOpts.HOR_GAP - CustOpts.getBTN_WIDTH(ok),
//        		getHeight() - CustOpts.BTN_HEIGHT - CustOpts.VER_GAP - 45,
//        		CustOpts.getBTN_WIDTH(ok),
//                CustOpts.BTN_HEIGHT);
//        cancel.setBounds(ok.getWidth() + ok.getX() + CustOpts.HOR_GAP * 2,
//        		ok.getY(),
//        		CustOpts.getBTN_WIDTH(cancel),
//                CustOpts.BTN_HEIGHT);
//
//        validate();
    }

//    /**
//     * Invoked when an action occurs. NOTE:PIM的绝大多数用于新建和编辑的对话盒，对于确定事件的处理，采用如下规则：
//     * 即：先出发监听器事件，监听器根据IPIMDialog接口的方法getContent（）取出对话盒中的 记录。监听器负责将记录存入Model，监听器最后负责将对话盒释放。
//     * 目的是让所有对话盒只认识一个叫Record的东西，不认识别的。
//     *
//     * @param e
//     *            动作事件
//     */
//    @Override
//    public void actionPerformed(
//            ActionEvent e) {
//        Object o = e.getSource();
//        if (o == ok) {
//        	if(btnTables.size() == 1) {				//single table modifying or creating new table.
//                //check name
//            	String name = tfdName.getText();
//            	if(name.length() < 1 && btnTables.size() == 1) { //if modifying a single table, the name shouldn't be empty.
//            		JOptionPane.showMessageDialog(this, DlgConst.InvalidInput);
//            		tfdName.grabFocus();
//    				return;
//            	}
//        		updateTable(name, btnTables.get(0).getId(), settingTableDlg == null);
//        	}else {											//multi tables modifying.
//	    		for (TableButton tableButton : btnTables) {
//					updateTable(tableButton.getTableName(), tableButton.getId(), settingTableDlg == null);
//				}
//        	}
//
//        } else if (o == cancel) {
//            dispose();
//        }
//    }
//
//	private void updateTable(String name, int id, boolean isTemprallyCreatedTbl) {
//		StringBuilder sql = new StringBuilder();
//		if(id == -1 ) {	//if has no index means it's new table/
//			if(!checkInput(tfdX) || !checkInput(tfdY)) {
//				JOptionPane.showMessageDialog(this, DlgConst.InvalidInput);
//				return;
//			}
//
//			Integer type = (Integer)cmbCategory.getSelectedItem();
//			if(settingTableDlg == null){//if called from Cmd_AddTable, then give it negative tpye.
//				type = -type;
//			}
//			sql.append("INSERT INTO DINING_TABLE (name, posX, posY, type, displayIdx) VALUES (?, ")
//			.append(tfdX.getText()).append(", ").append(tfdY.getText()).append(", ")
//			.append(type).append(", ").append(isTemprallyCreatedTbl ? -1 : 0).append(")");
//		}else {
//			sql.append("Update DINING_TABLE set name = ?");
//			if(checkInput(tfdX)) {
//				sql.append(", posX = ").append(tfdX.getText());
//			}
//			if(checkInput(tfdY)) {
//				sql.append(", posY = ").append(tfdY.getText());
//			}
//
//			sql.append(", type = ").append(cmbCategory.getSelectedItem())
//			.append(" where id = ").append(id);
//		}
//
//		try {
//			PreparedStatement pstmt = PIMDBModel.getConnection().prepareStatement(sql.toString());
//            pstmt.setString(1, name);
//            pstmt.executeUpdate();
//		    dispose();
//		    if(settingTableDlg != null)
//		    	settingTableDlg.initContent();
//		    else
//		    	((TablesPanel)BarFrame.instance.panels[0]).initContent();
//		}catch(Exception exp) {
//			JOptionPane.showMessageDialog(this, DlgConst.InvalidInput);
//		}
//	}
//
//	private boolean checkInput(JTextField tfd) {
//		try {
//			Integer.valueOf(tfd.getText());
//		}catch(Exception exp) {
//			tfd.grabFocus();
//			return false;
//		}
//		return true;
//	}

    private void initDialog(TableButton btnTable) {
//    	setModal(true);
//        setTitle(btnTable.getTableName().length() > 0 ? btnTable.getTableName() : BarFrame.consts.TABLE());
//        setResizable(false);
//        //
//        sptName = new PIMSeparator(BarFrame.consts.Name());
//        lblName = new JLabel(BarFrame.consts.TableName());
//        tfdName = new JTextField(btnTables.size() == 1 ? btnTable.getTableName() : "");//----------
//
//        sptBounds = new PIMSeparator(BarFrame.consts.Position());
//        lblX = new JLabel("Horizontal");
//        tfdX = new JTextField();
//        lblY = new JLabel("Vertical");
//        tfdY = new JTextField();
//        lblWidth = new JLabel("Width");
//        tfdWidth = new JTextField();
//        lblHeight = new JLabel("Height");
//        tfdHeight = new JTextField();
//
//        sptType = new PIMSeparator(OptionDlgConst.OPTION_OTHER);
//        lblCategory = new JLabel(BarFrame.consts.Categary());
//        cmbCategory = new JComboBox<Integer>();
//
//        ok = new JButton(DlgConst.OK);
//        cancel = new JButton(DlgConst.CANCEL);
//
//        cmbCategory.setModel(new DefaultComboBoxModel<Integer>(typeAry));
//        cmbCategory.setSelectedItem(btnTable.getType());
//
//
//        ok.setMnemonic('o');
//        ok.setMargin(new Insets(0, 0, 0, 0));
//
//        setBounds((CustOpts.SCRWIDTH - 321) / 2, (CustOpts.SCRHEIGHT - 220) / 2,
//        		321, LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS ? 220 : 160);
//        getContentPane().setLayout(null);
//        getRootPane().setDefaultButton(ok);
//
//        // 搭建－－－－－－－－－－－－－
//        getContentPane().add(sptName);
//        getContentPane().add(lblWidth);
//        getContentPane().add(tfdWidth);
//        getContentPane().add(lblHeight);
//        getContentPane().add(tfdHeight);
//        getContentPane().add(lblX);
//        getContentPane().add(tfdX);
//        getContentPane().add(lblY);
//        getContentPane().add(tfdY);
//
//        getContentPane().add(sptBounds);
//
//        getContentPane().add(sptType);
//        getContentPane().add(lblCategory);
//        getContentPane().add(cmbCategory);
//        getContentPane().add(lblName);
//        getContentPane().add(tfdName);
//
//        getContentPane().add(cancel);
//        getContentPane().add(ok);
//
//        // 加监听器－－－－－－－－
//        cmbCategory.addActionListener(this);
//        ok.addActionListener(this);
//        cancel.addActionListener(this);
//        addComponentListener(this);
//
//        if(btnTables.size() > 1) {
//        	tfdX.setText("");
//	        tfdY.setText("");
//	        tfdWidth.setText("");
//	        tfdHeight.setText("");
//        }else if(btnTable.getTableName().length() > 0) {
//	        tfdX.setText(String.valueOf(btnTable.getX()));
//	        tfdY.setText(String.valueOf(btnTable.getY()));
//	        tfdWidth.setText(String.valueOf(btnTable.getWidth()));
//	        tfdHeight.setText(String.valueOf(btnTable.getHeight()));
//        }else {
//	        tfdX.setText(String.valueOf(getX() + getWidth() / 2 - 21));
//	        tfdY.setText(String.valueOf(getY() - 60));
//	        tfdWidth.setText("42");
//	        tfdHeight.setText("42");
//        }
//        SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				tfdName.requestFocus();
//			}
//		});
    }


//    private PIMSeparator sptName;
//    private JLabel lblName;
//    private JTextField tfdName;
//
//    private PIMSeparator sptBounds;
//    private JLabel lblWidth;
//    private JTextField tfdWidth;
//    private JLabel lblHeight;
//    private JTextField tfdHeight;
//    private JLabel lblX;
//    private JTextField tfdX;
//    private JLabel lblY;
//    private JTextField tfdY;
    
//    private PIMSeparator sptType;
//    private JLabel lblCategory;
//    private JComboBox<Integer> cmbCategory;
//
//    private JButton ok;
//    private JButton cancel;
}
