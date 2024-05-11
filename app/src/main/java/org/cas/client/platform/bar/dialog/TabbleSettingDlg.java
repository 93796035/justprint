package org.cas.client.platform.bar.dialog;

import com.just.print.util.L;

import org.cas.client.platform.bar.uibeans.TableButton;

import java.sql.Statement;
import java.util.ArrayList;

public class TabbleSettingDlg// extends JDialog implements ActionListener,
{
	
//	Color bg = new Color(132,34, 22);

    private TableButton btnPressed;
    private boolean isDragged;
    private int xGap, yGap;

//	private PicturePane picturePane;
    
    static ArrayList<TableButton> btnTables = new ArrayList<TableButton>();
    
//    public TabbleSettingDlg(JFrame pParent) {
//        super(pParent, false);
//        initDialog();
//    }
//
//    // ActionListner----------------------------------
//    @Override
//    public void actionPerformed( ActionEvent e) {
//        JButton o = (JButton)e.getSource();
//        if(o instanceof TableButton) {
//        	if(o.getBackground() != bg) {
//        		o.setBackground(bg);
//        	}else {
//        		o.setBackground(null);
//        	}
//        }else {
//        	if (o == btnMore) {
//        		//add on into db
//        		Statement smt = PIMDBModel.getStatement();
//        		boolean isAnyTableButtonSelected = false;
//        		for (TableButton tableButton : btnTables) {
//					if (!tableButton.getBackground().equals(bg) && tableButton.getType() != DBConsts.LOGO_TYPE) {
//						isAnyTableButtonSelected = true;
//						String tableName = tableButton.getTableName();
//						try {
//							int value = Integer.valueOf(tableName) + 1;
//							boolean isNameUsed = isNameUserd(String.valueOf(value), btnTables);
//							if(isNameUsed) {
//								createTempTable(smt, tableButton);
//							}else {	//the new name is number and is not used
//								createdAlignedTable(smt, tableButton, value);
//							}
//						}catch(Exception exp) {	// if copying non-number table will trigger the exception and create temporal name table.
//							createTempTable(smt, tableButton);
//						}
//					}
//				}
//        		//if no table selected, then Add a new table();
//        		if(!isAnyTableButtonSelected) {
//        			new TableDlg(this, null).setVisible(true);
//        		}
//        		initContent();
//        	}else if(o == btnLess) {
//        		Statement smt = PIMDBModel.getStatement();
//        		for (TableButton tableButton : btnTables) {
//					if (!tableButton.getBackground().equals(bg) && tableButton.getType() != DBConsts.LOGO_TYPE) {
//						int id = tableButton.getId();
//						String sql = "delete from DINING_TABLE where id = " + id;
//						try {
//							smt.execute(sql);
//						}catch(Exception exp) {
//							ErrorUtil.write(exp);
//						}
//					}
//				}
//        		initContent();
//        	}else if (o ==btnModify) {
//        		for (TableButton tableButton : btnTables) {
//					if (!tableButton.getBackground().equals(bg) && tableButton.getType() != DBConsts.LOGO_TYPE) {
//						new TableDlg(this, tableButton).setVisible(true);
//						return;
//					}
//        		}
//        	}else if(o == btnApply) {
//        		try {
//        			absortNewTables();
//        		}catch(Exception exp) {
//        			L.e("TableSetting ", "Exception happend when absortNewTables:", exp);
//        		}
//        	}else if(o == btnClose) {
//        		setVisible(false);
//        	}
//        }
//
//    }

	private void createdAlignedTable(Statement smt, TableButton tableButton, int value) {
		String tableName = String.valueOf(value);
		int x = (value / 10) * (tableButton.getWidth() + 10) + 10;
		int y = (value - (value / 10) * 10) * (tableButton.getHeight() + 10) + 10;
		StringBuilder sql = new StringBuilder("INSERT INTO DINING_TABLE (name, posX, posY, width, height, type) VALUES ('")
				.append(tableName).append("', ").append(x).append(", ")
				.append(y).append(", ")
				.append(tableButton.getWidth()).append(", ").append(tableButton.getHeight()).append(", ")
				.append(tableButton.getType()).append(")");
		try {
			smt.executeUpdate(sql.toString());
		}catch(Exception exp) {
			L.e("TabbleSettingDlg", "exception when execSQL" + sql, exp);
		}
	}

	private boolean isNameUserd(String value, ArrayList<TableButton> btnTables) {
		for (TableButton tableButton2 : btnTables) {
			if(tableButton2.getText().equals(value)) {	//if new number already used, then created temporal name table
				return true;
			}
		}
		return false;
	}

	private void createTempTable(Statement smt, TableButton tableButton) {
		String tableName = tableButton.getTableName() + "_copy";
		String sql = "INSERT INTO DINING_TABLE (name, posX, posY, width, height, type) VALUES ('"
				+ tableName + "', " + (tableButton.getX() + 10) + ", " + (tableButton.getY() + 10) + ", "
				+ tableButton.getWidth() + ", " + tableButton.getHeight() + ", " + tableButton.getType() + ")";
		try {
			smt.executeUpdate(sql);
		}catch(Exception exp) {
			L.e("TabbleSettignDlg", "exception when exec " + sql, exp);
		}
	}

    /** 本方法用于设置View上各个组件的尺寸。 */
//    @Override
//    public void reLayout() {
//        int panelHeight = getHeight() - CustOpts.SIZE_EDGE;
//        int tBtnWidht = 120;
//        int tBtnHeight = 50;
//
//        // command buttons--------------
//        btnClose.setBounds(getWidth() - tBtnWidht - CustOpts.HOR_GAP - CustOpts.SIZE_EDGE - 50,
//        		panelHeight - tBtnHeight - CustOpts.VER_GAP - CustOpts.SIZE_EDGE - 50,
//        		tBtnWidht, tBtnHeight);
//        btnModify.setBounds(btnClose.getX(),
//        		btnClose.getY() - tBtnHeight * 2 - CustOpts.VER_GAP * 2,
//        		tBtnWidht, tBtnHeight);
//        btnApply.setBounds(btnModify.getX(),
//        		btnModify.getY() - tBtnHeight - CustOpts.VER_GAP,
//        		tBtnWidht, tBtnHeight);
//        btnLess.setBounds(btnModify.getX(),
//        		btnApply.getY() - tBtnHeight - CustOpts.VER_GAP,
//        		tBtnWidht, tBtnHeight);
//        btnMore.setBounds(btnModify.getX(),
//        		btnLess.getY() - tBtnHeight - CustOpts.VER_GAP,
//        		tBtnWidht, tBtnHeight);
//
//		invalidate();
//		revalidate();
//		repaint();
//    }
//
//    private void initDialog() {
//    	picturePane = new PicturePane(CustOpts.custOps.getMainPaneBGImg());
//        btnMore = new JButton(BarFrame.consts.Add());
//        btnLess = new JButton(BarFrame.consts.Delete());
//        btnModify = new JButton(BarFrame.consts.Modify());
//        btnApply = new JButton(BarFrame.consts.Apply());
//        btnClose = new JButton(BarFrame.consts.Close());
//
//        // border----------
//        setBounds(BarFrame.instance.getX(), BarFrame.instance.getY(), BarFrame.instance.getWidth(), BarFrame.instance.getHeight());
//        picturePane.setLayout(null);
//
//        // built
//        picturePane.add(btnMore);
//        picturePane.add(btnLess);
//        picturePane.add(btnModify);
//        picturePane.add(btnApply);
//        picturePane.add(btnClose);
//
//        // add listener
//        addComponentListener(this);
//
//        btnMore.addActionListener(this);
//        btnLess.addActionListener(this);
//        btnModify.addActionListener(this);
//        btnApply.addActionListener(this);
//        btnClose.addActionListener(this);
//
//        initContent();
//
//        add(picturePane);
//    }

    // menu and category buttons must be init after initContent---------
//	void initContent() {
//		//clean existing btns
//		for (int i = btnTables.size() - 1; i >=0; i--) {
//			TableButton tableToggleButton = btnTables.get(i);
//			btnTables.remove(i);
//			picturePane.remove(tableToggleButton);
//		}
//		//renite buttons.
//		try {
//            Statement smt = PIMDBModel.getReadOnlyStatement();
//
//            // load all the categorys---------------------------
//            ResultSet rs = smt.executeQuery("select ID, Name, posX, posY, width, height, type, status from dining_Table");
//            rs.beforeFirst();
//
//            while (rs.next()) {
//            	TableButton tableToggleButton = new TableButton();
//
//    			tableToggleButton.setBackground(bg);
//            	tableToggleButton.setId(rs.getInt("ID"));
//            	int x = rs.getInt("posX");
//        		int y = rs.getInt("posY");
//        		int w = rs.getInt("width");
//        		int h = rs.getInt("height");
//        		int type = rs.getInt("type");
//        		if(x > getWidth() - (type == DBConsts.LOGO_TYPE ? 133 : 99)) {
//        			x = getWidth() - (type == DBConsts.LOGO_TYPE ? 133 : 99);
//        		}
//        		if(x < 0) {
//        			x = 0;
//        		}
//        		int cmdBtnHeight = (BarFrame.instance.getHeight() - 80) / 10;
//        		if(y > getHeight() - (type == DBConsts.LOGO_TYPE ? 200 + cmdBtnHeight : 160)) {
//        			y =  getHeight() - (type == DBConsts.LOGO_TYPE ? 200 + cmdBtnHeight : 160);
//        		}
//        		if(y < 0) {
//        			y = 0;
//        		}
//        		if(w <= 10) {
//        			w = 99;
//        		}
//        		if(h <= 10) {
//        			h = 99;
//        		}
//        		tableToggleButton.setBounds(x, y, w, h);
//            	tableToggleButton.setTypeStatusAndName("", "", rs.getInt("type"), 0, rs.getString("Name"));		//it's rectanglee or round?
//            	//tableToggleButton.setMargin(new Insets(0, 0, 0, 0));
//    			tableToggleButton.addActionListener(this);
//    			tableToggleButton.addMouseMotionListener(new MouseMotionListener(){
//    	        	@Override
//					public void mouseDragged(MouseEvent e) {
//    	        		if(btnPressed != null) {
//    	        			btnPressed.setLocation(btnPressed.getX() + e.getX() - xGap, btnPressed.getY() + e.getY() - yGap);
//    	        			isDragged = true;
//    	        		}
//    	        	}
//    	        	@Override
//					public void mouseMoved(MouseEvent e) {}
//    	        });
//    			tableToggleButton.addMouseListener(this);
//    			picturePane.add(tableToggleButton);
//
//            	btnTables.add(tableToggleButton);
//            }
//
//            rs.close();// 关闭
//            smt.close();
//		}catch(Exception e) {
//			L.e("Table Setting", "Unexpected exception when init the tables from db.", e);
//		}
//		reLayout();
//	}
//
//	private void absortNewTables() throws SQLException{
//		String sql;
//        String path = CASUtility.getPIMDirPath();
//        int length = path.length();
//        File read = new File(path);
//        File[] files = read.listFiles();
//        ArrayList<String> arry = new ArrayList<String>();
//        FileSystemUtil.getAllFilesIntoArray(files, arry, length);
//        for (String string : arry) {
//			if(string.endsWith(".png")) {
//				string = string.substring(0, string.length() - 4);
//				int p = string.lastIndexOf("\\");
//				if(p > -1) {
//					string = string.substring(p + 1);
//				}
//
//				boolean alreadyAdded = false;
//				for (TableButton bableButton : btnTables) {
//					if(string.equalsIgnoreCase(bableButton.getTableName())) {
//						alreadyAdded = true;
//						break;
//					}
//				}
//				if(alreadyAdded) {
//					continue;
//				}
//				String numberPart = "";
//				for(int i = 0; i < string.length(); i++) {
//					if(string.charAt(i) >= '1' && string.charAt(i) <= '9') {
//						numberPart = string.substring(i);
//						break;
//					}
//				}
//				for(int i = numberPart.length() - 1; i >= 0; i--) {
//					if(string.charAt(i) >= '1' && string.charAt(i) <= '9') {
//						numberPart = numberPart.substring(0, i + 1);
//						break;
//					}
//				}
//				int idx = Integer.valueOf(numberPart);
//				int row = idx / 10 + 1;
//				int col = idx % 10;
//
//				sql = new StringBuilder("INSERT INTO DINING_TABLE (name, DSP_INDEX, posX, posY, width, height, type) VALUES ('")
//						.append(string).append("', 1, ")		//dsp_index is currently used to point to the client id. -1 means not pointing to any client.
//						.append(col * 90 + CustOpts.HOR_GAP).append(", ")
//						.append(row * 90 + CustOpts.VER_GAP).append(", 90, 90, ")
//						.append(DBConsts.LOGO_TYPE + idx).append(")").toString();		//type == -2 mean the table will get image with name as file name.
//				PIMDBModel.getStatement().executeUpdate(sql);
//			}
//		}
//        initContent();
//	}
//    private JButton btnMore;
//    private JButton btnLess;
//    private JButton btnApply;
//    private JButton btnModify;
//    private JButton btnClose;
//
//	@Override
//	public void mouseClicked(MouseEvent e) {}
//
//	@Override
//	public void mousePressed(MouseEvent e) {
//		Object o = e.getSource();
//		if( o instanceof TableButton) {
//			btnPressed = (TableButton)o;
//			xGap = e.getX();
//			yGap = e.getY();
//		}
//	}
//
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		Object o = e.getSource();
//		if(o instanceof TableButton) {
//			if(btnPressed != null && isDragged) {
//				TableButton btn = (TableButton)o;
//				//updateDB;
//				String sql = "Update DINING_TABLE set posX = " + (btn.getX() + e.getX() - xGap) + ", posY = " + (btn.getY() + e.getY()-yGap) + " where id = " + btn.getId();
//	        	try {
//	        		PIMDBModel.getStatement().execute(sql);
//	                initContent();
//	        	}catch(Exception exp) {
//	        		ErrorUtil.write(exp);
//	        	}
//				reLayout();
//			}
//		}
//
//		isDragged = false;
//	}
//
//	@Override
//	public void mouseEntered(MouseEvent e) {}
//
//	@Override
//	public void mouseExited(MouseEvent e) {}
}
