package org.cas.client.platform.bar.dialog.modifyDish;

import android.database.Cursor;

import androidx.fragment.app.DialogFragment;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.i18n.BarDlgConst;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class AddModificationDlg extends DialogFragment
//        implements ActionListener, ListSelectionListener, KeyListener,
//        MouseListener, MouseMotionListener, Runnable, ComponentListener, ChangeListener
{

	public boolean isSettingMode = false;
	
    public static String[] backgrounds = new String[10];
    
	int oldIndex = 0;
    HashMap<Integer, String> selectionsMap = new HashMap<Integer, String>();
    
	private String defaultTabName = "     ";

	private String bunColor = "#fadcdc";

    private static AddModificationDlg instance;
    public static AddModificationDlg getInstance() {
    	if(instance == null) {
    		instance = new AddModificationDlg(BarFrame.instance);
    	}
    	return instance;
    }

    private AddModificationDlg(BarFrame prmParent) {//, String prmCategoryInfo) {
        super();
        initBackGrounds();
        initComponent(); // 组件初始化并布局
        initContent("", 0); // 初始化文本区和列表框数据
    }

    private void initBackGrounds() {
    	for(int i = 0; i < 10; i++) {
    		backgrounds[i] = BarOption.getBKForModify(i + 1);
    	}
	}
	
    /** Invoked when the component's size changes. */

    private void initComponent() {
//        setTitle(BarFrame.consts.MODIFY()); // 设置标题
//        getContentPane().setLayout(null);
//        int modifyDlgWidth = BarOption.getModifyDlgWidth();
//        modifyDlgWidth = modifyDlgWidth== 0 ? BarFrame.instance.menuPanel.getWidth() + CustOpts.SIZE_EDGE : modifyDlgWidth;
//
//        setBounds(CustOpts.SCRWIDTH - modifyDlgWidth, 0, modifyDlgWidth, CustOpts.SCRHEIGHT - 30); // 对话框的默认尺寸, get rid of the height of status bar。
//        setResizable(true);
//
//        // init--------------------------
//        midLabel = new JLabel(BarFrame.consts.AvailableModification()); // "可用类别"标签
//        txaCurContent = new JTextArea(); // 加入会滚动的文本区
//        topLabel = new JLabel(BarFrame.consts.AddNewModificationItem()); // "项目属于这些类别"标签
//        modificationList = new JList<CheckItem>();
//        tabbedPane = new JTabbedPane();
//        panelList = new JPanel();
//        scrollPanelAllMarks = new PIMScrollPane(panelList);
//        btnApplyToList = new JButton(BarFrame.consts.ApplyToList()); // 加至列表按钮
//        btnApplyToCategory = new JButton(BarFrame.consts.ApplyToCategory());
//        lblDspIdx = new JLabel(BarFrame.consts.DSPINDEX());
//        lblPrice = new JLabel(BarFrame.consts.Price());
//        valDspIdx = new JTextField();
//        valPrice = new JTextField();
//        btnApply =  new JButton(BarFrame.consts.Apply());
//        btnDelete = new JButton(BarFrame.consts.Delete());
//        resetBTN = new JButton(BarFrame.consts.RESET());
//        btnClose = new JButton(BarFrame.consts.OK());
//
//        // properties-------------------------
//        midLabel.setLabelFor(modificationList);
//        topLabel.setLabelFor(txaCurContent);
//
//        modificationList.setCellRenderer(new ModificationListRenderer());
//        modificationList.setBackground(null);
//        modificationList.setBorder(new LineBorder(Color.GRAY));
//        modificationList.setSelectionBackground(null);
//
//        scrollPanelAllMarks.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        scrollPanelAllMarks.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//        panelList.setLayout(null);//new WrapLayout());
//
//        txaCurContent.setBorder(new LineBorder(Color.GRAY));
//        midLabel.setDisplayedMnemonic('V');
//        topLabel.setDisplayedMnemonic('I');
//        btnApplyToList.setMnemonic('A');
//        btnApplyToCategory.setMnemonic('p');
//        btnApply.setMnemonic('D');
//        btnDelete.setMnemonic('D');
//        resetBTN.setMnemonic('R');
//        btnApplyToList.setMargin(new Insets(0, 0, 0, 0));
//        btnApplyToCategory.setMargin(new Insets(0, 0, 0, 0));
//        btnDelete.setMargin(new Insets(0, 0, 0, 0));
//        btnApply.setMargin(new Insets(0, 0, 0, 0));
//        resetBTN.setMargin(new Insets(0, 0, 0, 0));
//        resetBTN.setVisible(false);
//        btnApplyToList.setEnabled(false); // 一开始这项为禁止
//        btnApplyToCategory.setEnabled(false); // 一开始这项为禁止
//
//        // layout---------------------------
//        reLayout();
//
//        // build----------------------------
//        getContentPane().add(midLabel);
//        getContentPane().add(btnApplyToList);
//        getContentPane().add(btnApplyToCategory);
//        //only one of following two can be visible at one time.
//        getContentPane().add(tabbedPane);
//        getContentPane().add(scrollPanelAllMarks);
//
//        getContentPane().add(lblDspIdx);
//        getContentPane().add(valDspIdx);
//        getContentPane().add(lblPrice);
//        getContentPane().add(valPrice);
//        getContentPane().add(btnApply);
//        getContentPane().add(btnDelete);
//        getContentPane().add(resetBTN);
//        getContentPane().add(txaCurContent);
//        getContentPane().add(topLabel);
//        getContentPane().add(btnClose);
//
//        // listeners------------------------
//        btnApplyToList.addActionListener(this);
//        btnApplyToCategory.addActionListener(this);
//        btnApply.addActionListener(this);
//        btnDelete.addActionListener(this);
//        resetBTN.addActionListener(this);
//        btnClose.addActionListener(this);
//
//        tabbedPane.addChangeListener(this);
//        modificationList.addListSelectionListener(this);
//
//        modificationList.addMouseListener(this);
//
//        txaCurContent.addKeyListener(this);
//        modificationList.addKeyListener(this);
//
//        getContentPane().addComponentListener(this);
    }

    private void reLayout() {
//        btnClose.setBounds(getWidth() - CustOpts.getBTN_WIDTH(btnClose) * 2 - CustOpts.SIZE_EDGE * 2 - CustOpts.HOR_GAP * 2,
//        		getHeight() - CustOpts.BTN_HEIGHT * 3 - CustOpts.SIZE_EDGE - CustOpts.SIZE_TITLE - 2 * CustOpts.VER_GAP,
//        		CustOpts.getBTN_WIDTH(btnClose) * 2, CustOpts.BTN_HEIGHT * 3);
//
//        lblDspIdx.setBounds(tabbedPane.getX(), btnClose.getY(),
//        		lblDspIdx.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        valDspIdx.setBounds(lblDspIdx.getX() + lblDspIdx.getWidth() + CustOpts.HOR_GAP, lblDspIdx.getY(),
//        		30, CustOpts.BTN_HEIGHT);
//
//        lblPrice.setBounds(valDspIdx.getX() + valDspIdx.getWidth() + CustOpts.HOR_GAP, valDspIdx.getY(),
//        		lblPrice.getPreferredSize().width, CustOpts.BTN_HEIGHT);
//        valPrice.setBounds(lblPrice.getX() + lblPrice.getWidth() + CustOpts.HOR_GAP, lblPrice.getY(),
//        		60, CustOpts.BTN_HEIGHT);
//
//
//        if(BarFrame.instance.curDish == null) {
//            btnApply.setBounds(valPrice.getX() + valPrice.getWidth() + CustOpts.HOR_GAP, valPrice.getY(),
//            		CustOpts.getBTN_WIDTH(btnApply), CustOpts.BTN_HEIGHT);
//        	btnDelete.setBounds(btnApply.getX() + btnApply.getWidth() + CustOpts.HOR_GAP, btnApply.getY(),
//        			CustOpts.getBTN_WIDTH(btnApply), CustOpts.BTN_HEIGHT);
//        }else {
//            btnApply.setBounds(valPrice.getX() + valPrice.getWidth() + CustOpts.HOR_GAP, valPrice.getY(), 0, 0);
//        	btnDelete.setBounds(btnApply.getX() + btnApply.getWidth() + CustOpts.HOR_GAP, btnClose.getY(), 0, 0);
//        }
//
//        topLabel.setBounds(CustOpts.HOR_GAP, CustOpts.VER_GAP,
//        		getWidth(), CustOpts.LBL_HEIGHT);
//        txaCurContent.setBounds(topLabel.getX(), topLabel.getY() + CustOpts.LBL_HEIGHT,
//        		getWidth() - 2 * CustOpts.SIZE_EDGE - 3 * CustOpts.HOR_GAP,
//                CustOpts.LBL_HEIGHT * 3);
//        int width = 120;
//        btnApplyToList.setBounds(getWidth() - width - CustOpts.SIZE_EDGE * 6, txaCurContent.getY() + txaCurContent.getHeight() + CustOpts.VER_GAP,
//        		width, CustOpts.BTN_HEIGHT);
//        btnApplyToCategory.setBounds(btnApplyToList.getX() - CustOpts.HOR_GAP - 200, btnApplyToList.getY(),
//        		200, CustOpts.BTN_HEIGHT);
//        midLabel.setBounds( topLabel.getX(),
//        		txaCurContent.getY() + txaCurContent.getHeight() + CustOpts.VER_GAP,
//        		getWidth() - 2 * CustOpts.SIZE_EDGE - 3 * CustOpts.HOR_GAP, CustOpts.BTN_HEIGHT);
//
//        tabbedPane.setBounds(midLabel.getX(), midLabel.getY() + midLabel.getHeight() + CustOpts.VER_GAP, // "可用类别"列表框
//        		getWidth() - 2 * CustOpts.SIZE_EDGE - 3 * CustOpts.HOR_GAP,
//        		lblDspIdx.getY() - 2 * CustOpts.VER_GAP - midLabel.getY() - midLabel.getHeight());
//        scrollPanelAllMarks.setBounds(midLabel.getX(), CustOpts.VER_GAP, // "可用类别"列表框
//        		getWidth() - 2 * CustOpts.SIZE_EDGE - 3 * CustOpts.HOR_GAP,
//        		lblDspIdx.getY() - 2 * CustOpts.VER_GAP);
//        initPanelListContent();
//
////        resetBTN.setBounds(btnDelete.getX(), btnDelete.getY() + btnDelete.getHeight() + CustOpts.VER_GAP,
////                CustOpts.getBTN_WIDTH(), CustOpts.BTN_HEIGHT);
    }

    public void initContent( String prmCategoryInfo, int idx) {
//    	//must set content first, so the selections can be initialized base on it.
//    	//and set the content again in the last line, when got idea which belongs to this page.
//        txaCurContent.setText("null".equalsIgnoreCase(prmCategoryInfo) ? "" : prmCategoryInfo);
//        //decide which component to display, allInOnePanel or tabbledPane.
//    	scrollPanelAllMarks.setVisible(!isSettingMode);
//    	txaCurContent.setVisible(isSettingMode);
//    	tabbedPane.setVisible(isSettingMode);
//    	btnApplyToList.setVisible(isSettingMode);
//    	btnApplyToCategory.setVisible(isSettingMode);
//    	lblDspIdx.setVisible(isSettingMode);
//    	lblPrice.setVisible(isSettingMode);
//    	midLabel.setVisible(isSettingMode);
//    	topLabel.setVisible(isSettingMode);
//    	valDspIdx.setVisible(isSettingMode);
//    	valPrice.setVisible(isSettingMode);
//
//        if(isSettingMode) {
//        	listModel = new DefaultListModel<CheckItem>();
//        	initTabbedPaneContent();
//        	if(tabbedPane.getTabCount() > 0) {
//        		tabbedPane.setSelectedIndex(idx);
//        	}
//        }else {
//        	initPanelListContent();
//        }
//        initSelectionMap();
//        txaCurContent.setText(selectionsMap.get(idx));
//        reLayout();
    }

	private void initTabbedPaneContent() {
//		tabbedPane.removeAll(); 	//this removeAll will trigger the stateChanged method.
//
//		//must add a blank tab for adding none classified lales and for adding new category. (category and label are saved in same table, with different type)
//		tabbedPane.add(defaultTabName , new PIMScrollPane());
//
//        StringBuilder sql = new StringBuilder("select * from modification where type < 0 order by type desc");
//        try {
//			ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
//            rs.beforeFirst();
//            String category;
//            while (rs.next()) {
//            	switch (LoginDlg.USERLANG) {
//				case 0:
//					category = BarUtil.removeHTMLTAG(rs.getString("lang1"));		//add mothod will also trigger stateChanged method.
//					break;
//				case 1:
//					category = BarUtil.removeHTMLTAG(rs.getString("lang2"));		//add mothod will also trigger stateChanged method.
//					break;
//				case 2:
//					category = BarUtil.removeHTMLTAG(rs.getString("lang3"));		//add mothod will also trigger stateChanged method.
//					break;
//				default:
//					category = BarUtil.removeHTMLTAG(rs.getString("lang4"));		//add mothod will also trigger stateChanged method.
//					break;
//				}
//            	tabbedPane.add(category, new PIMScrollPane());
//            }
//
//            for(int i = 0; i < tabbedPane.getTabCount(); i++) {
//            	tabbedPane.setBackgroundAt(i, backgrounds[i%10]);
//            }
//		}catch(Exception exp) {
//			L.e("AddModificationDlg", "exception when change output back to original bill" + sql, exp);
//		}
	}
	
	//for grouped mark lables.
	private void initPanelListContent() {
//		panelList.removeAll();
//
//		int i = 0;
//		int y = 0;
//		//prepare the default panel
//		JPanel jpCategorizedMarks = new JPanel();
//		jpCategorizedMarks.setBorder(new TitledBorder(defaultTabName));
//		jpCategorizedMarks.setBackground(backgrounds[i%10]);
//		int height = initPanelContent(jpCategorizedMarks, 0); 	//because the category's type was defined to be from -1 to -n, and modification was from 0~n
//		jpCategorizedMarks.setPreferredSize(new Dimension(jpCategorizedMarks.getPreferredSize().width, height + 100));
//		jpCategorizedMarks.setBounds(0, y, panelList.getWidth(), height + 100);
//		panelList.add(jpCategorizedMarks);
//		i++;
//		y += jpCategorizedMarks.getHeight();
//
//		//add other panels;
//        StringBuilder sql = new StringBuilder("select * from modification where type < 0 order by type desc");
//        try {
//			ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
//            rs.beforeFirst();
//
//            while (rs.next()) {
//            	jpCategorizedMarks = new JPanel();
//        		//jpCategorizedMarks.setLayout(new WrapLayout());
//            	switch (LoginDlg.USERLANG) {
//				case 0:
//					jpCategorizedMarks.setBorder(new TitledBorder(BarUtil.removeHTMLTAG(rs.getString("lang1"))));
//					break;
//				case 1:
//					jpCategorizedMarks.setBorder(new TitledBorder(BarUtil.removeHTMLTAG(rs.getString("lang2"))));
//					break;
//				case 2:
//					jpCategorizedMarks.setBorder(new TitledBorder(BarUtil.removeHTMLTAG(rs.getString("lang3"))));
//					break;
//				default:
//					break;
//				}
//
//        		jpCategorizedMarks.setBackground(backgrounds[i%10]);
//        		height = initPanelContent(jpCategorizedMarks, -1 * rs.getInt("type")); 	//because the category's type was defined to be from -1 to -n, and modification was from 0~n
//        		jpCategorizedMarks.setPreferredSize(new Dimension(getPreferredSize().width, height));
//        		jpCategorizedMarks.setBounds(0, y, panelList.getWidth(), height);
//        		panelList.add(jpCategorizedMarks);
//        		i++;
//        		y += jpCategorizedMarks.getHeight();
//            }
//
//            panelList.setPreferredSize(new Dimension(scrollPanelAllMarks.getWidth(), y));
//		}catch(Exception exp) {
//			L.e("AddModificationDlg", "exception when fetching categorys of modification from db:" + sql, exp);
//		}
//
//		// TODO init the buttons in panels
//		revalidate();
//		invalidate();
//		repaint();
	}
	
//	//inside each group.
//    private int initPanelContent(JPanel panel, int type) {
//		ArrayList<String> allModification = getAllLangNamesFromDB(type);
//
//        //start to init current list.
//		JButton btn = null;
//		int x = 0;
//		int y = 0;
//
//		StringBuilder sql = new StringBuilder("select * from modification where type = ").append(type);
//        try {
//			ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
//            rs.beforeFirst();
//            String nameAndPrice;
//            while (rs.next()) {
//            	switch (LoginDlg.USERLANG) {
//				case 0:
//					nameAndPrice = BarUtil.removeHTMLTAG(rs.getString("lang1")) + "(" + BarOption.getMoneySign() + rs.getString("lang6") + ")";		//add mothod will also trigger stateChanged method.
//					break;
//				case 1:
//					nameAndPrice = BarUtil.removeHTMLTAG(rs.getString("lang2")) + "(" + BarOption.getMoneySign()  + rs.getString("lang6") + ")";		//add mothod will also trigger stateChanged method.
//					break;
//				case 2:
//					nameAndPrice = BarUtil.removeHTMLTAG(rs.getString("lang3")) + "(" + BarOption.getMoneySign()  + rs.getString("lang6") + ")";		//add mothod will also trigger stateChanged method.
//					break;
//				default:
//					nameAndPrice = BarUtil.removeHTMLTAG(rs.getString("lang4")) + "(" + BarOption.getMoneySign()  + rs.getString("lang6") + ")";		//add mothod will also trigger stateChanged method.
//					break;
//				}
//            	btn = new JButton(BarUtil.addHTMLTAG(nameAndPrice));
//            	btn.addMouseListener(this);
//            	btn.addMouseMotionListener(this);
//
//    			btn.setPreferredSize(new Dimension(BarOption.getModifyButtonWidth(), BarOption.getModifyButtonHeight()));
//            	btn.setBounds(x, y, BarOption.getModifyButtonWidth(), BarOption.getModifyButtonHeight());
//            	btn.setBackground(bunColor );
//            	panel.add(btn);
//
//            	if(x + btn.getPreferredSize().width > panelList.getWidth()) {
//            		x = 0;
//            		y += btn.getPreferredSize().height + CustOpts.VER_GAP;
//            	}
//            	x += btn.getPreferredSize().width + CustOpts.HOR_GAP;
//            }
//		}catch(Exception exp) {
//			L.e("AddModificationDlg", "exception when change output back to original bill" + sql, exp);
//		}
//        return btn == null ? 39 : btn.getY() + btn.getHeight();
//	}
//
//	//keep all selections in a map. key is category idx, value is separated string in top text area.
//	private void initSelectionMap() {
//        ArrayList<String> inputList = getInputModification();//the modifications in above text area.
//        int tabsize = tabbedPane.getTabCount();
//        for(int i = 0; i < tabsize; i++) {
//        	ArrayList<String> allLangNameStrOfCurTab = getAllLangNamesFromDB(i);
//        	StringBuilder selectedNameStrOfCurTab = new StringBuilder();
//        	for (int j = inputList.size() - 1; j >= 0; j--) {
//        		String label = inputList.get(j);
//				if(allLangNameStrOfCurTab.contains(label)) {
//					selectedNameStrOfCurTab.append(BarDlgConst.delimiter);
//					selectedNameStrOfCurTab.append(label);
//					inputList.remove(j);
//				}
//			}
//        	if(selectedNameStrOfCurTab.length() > 1) {
//        		selectionsMap.put(i, selectedNameStrOfCurTab.substring(1));
//        	}
//        }
//	}

    private ArrayList<String> getAllLangNamesFromDB(int idx) {
        StringBuilder sql = new StringBuilder("SELECT * FROM modification where status = ").append(DBConsts.ORIGINAL);
        if(idx == 0 ) {
        	sql.append(" and type is null or type = 0");
        }else if(idx > 0){
        	sql.append(" and type = ").append(idx);
        }else {
        	//append no type condition if the idx is -1;
        }
        sql.append(" order by lang5");
        
        ArrayList<String> nameVec = new ArrayList<String>();
        try {

            Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            while (rs.moveToNext()) {
            	StringBuilder sb = new StringBuilder(BarUtil.removeHTMLTAG(DBUtil.getCursorString(rs, "NAME1"))).append(BarDlgConst.semicolon);
            	sb.append(BarUtil.removeHTMLTAG(DBUtil.getCursorString(rs, "NAME2"))).append(BarDlgConst.semicolon);
            	sb.append(BarUtil.removeHTMLTAG(DBUtil.getCursorString(rs, "NAME3"))).append(BarDlgConst.semicolon);
                nameVec.add(sb.toString());
            }
            return nameVec;
            
        } catch (Exception e) {
            L.e("AddModificationDlg ", "excption when getAllLangNamesFromDB" + sql, e);
            return null;
        }
    }

    // 解析一个逗号分隔符处理的字符串 getTextAreaData
    private String[] stringToArray( String string) {
        if (string != null) {
            // 构建字符串分隔器
            StringTokenizer token = new StringTokenizer(string, BarDlgConst.delimiter);
            int size = token.countTokens();
            // 构建相应容量的字符串数组
            String[] indexes = new String[size];
            size = 0;
            // 循环加入,去掉空格的
            while (token.hasMoreTokens()) {
                indexes[size] = token.nextToken().trim();
                size++;
            }
            return indexes;
        } else {
            return null;
        }
    }

//    @Override
//	public void onClick(View o) {
//        if (o == btnApplyToList)
//            applyToListClicked();
//        else if (o == btnApplyToCategory)
//        	addToCategoryClicked();
//        else if (o == btnDelete)
//            deleteClicked();
//        else if (o == btnApply)
//        	applyProperties();
//        else if (o == resetBTN) {
//            //resetClicked();
//        }else if (o == btnClose) {
//            dispose();
//        }
//    }

	private void applyProperties() {
//    	//validate values;
//    	try {
//    		Float.parseFloat(valPrice.getText().length() > 0 ? valPrice.getText() : "0.00");
//    		Float.parseFloat(valDspIdx.getText().length() > 0 ? valDspIdx.getText() : "0");
//    	}catch(Exception e) {
//    		JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidInput());
//    		return;
//    	}
//
//    	ListModel<CheckItem> model = modificationList.getModel();
//    	if(model.getSize() == 0) {
//    		return;
//    	}
//
//    	for(int i = model.getSize() - 1; i >= 0; i--) {
//    		CheckItem item = model.getElementAt(i);
//    		if(!item.isSelected()) {
//    			continue;
//    		}
//	    	//update
//			StringBuilder sql = new StringBuilder("UPDATE modification set lang5 = '").append(valDspIdx.getText()).append("', ")
//					.append(" lang6 = '").append(BarUtil.formatMoney(Double.valueOf(valPrice.getText())));
//			if(tabbedPane.getSelectedIndex() == 0) {
//				sql.append("' where type is null or type = 0");
//			}else {
//				sql.append("' where type = ").append((tabbedPane.getSelectedIndex()));
//			}
//			sql.append(" and lang1 = '");
//
//			String lang1 = item.getName();
//			int p = lang1.indexOf(BarDlgConst.semicolon);
//			lang1 = p > 0 ? lang1.substring(0, p) : lang1;
//			sql.append(BarUtil.addHTMLTAG(lang1)).append("'");
//
//			try {
//				PIMDBModel.getStatement().executeUpdate(sql.toString());
//        		BarOption.updateLastModifiyTime();
//			}catch(Exception e) {
//				L.e("AddModification ", "Exception when updating a category into modification.", e);
//			}
//    	}
//    	int idx = tabbedPane.getSelectedIndex();
//	    this.initTabbedPaneContent();
//	    tabbedPane.setSelectedIndex(idx);
	}

	public boolean insertModification( String modification, String price) {
//        StringBuilder sql = new StringBuilder("INSERT INTO modification (lang1, lang2, lang3, type, status, lang6) VALUES( ?, ?, ?, ");
//        sql.append(tabbedPane.getSelectedIndex());
//        sql.append(", 0, '").append(price.length() == 0 ? "0" : price).append("')");
//
//        try {
//            PreparedStatement pstmt = PIMDBModel.getConnection().prepareStatement(sql.toString());
//            String[] langs = modification.split(BarDlgConst.semicolon);
//            pstmt.setString(1, langs.length > 0 ? BarUtil.addHTMLTAG(langs[0].trim()) : "");
//            pstmt.setString(2, langs.length > 1 ? BarUtil.addHTMLTAG(langs[1].trim()) : "");
//            pstmt.setString(3, langs.length > 2 ? BarUtil.addHTMLTAG(langs[2].trim()) : "");
//
//            int rows = pstmt.executeUpdate();
//    		BarOption.updateLastModifiyTime();
//
//            pstmt.close();
//            pstmt = null;
//
//            return rows != 0;
//        } catch (SQLException e) {
//        	ErrorUtil.write(e);
            return false;
//        }
    }
    
    public boolean updateToModification(String modification, String price){
        String[] langs = modification.split(BarDlgConst.semicolon);
    	StringBuilder sql = new StringBuilder("update modification set lang2 = '");
        sql.append(langs.length > 1 ? BarUtil.addHTMLTAG(langs[1]) : "");
        sql.append("', lang3 = '");
        sql.append(langs.length > 2 ? BarUtil.addHTMLTAG(langs[2]) : "");
        sql.append("', lang4 = '");
        sql.append(langs.length > 3 ? BarUtil.addHTMLTAG(langs[3]) : "");
        if(!"-123456".equals(price)) {
        	sql.append("', lang6 = '").append(price);
        }
        sql.append("' where lang1 = '").append(BarUtil.addHTMLTAG(langs[0])).append("';");

        try {
            AikaPos.db.execSQL(sql.toString());
    		BarOption.updateLastModifiyTime();

            return true;
        } catch (Exception e) {
        	ErrorUtil.write(e);
            return false;
        }
    }
    
    private boolean deleteModification( String modification, int idx) {
    	StringBuilder sql = new StringBuilder("DELETE FROM modification WHERE lang1 = '");
        String[] langs = modification.split(BarDlgConst.semicolon);
        sql.append(BarUtil.addHTMLTAG(langs[0]));
        sql.append("' and type = ").append(idx);

        try {
            AikaPos.db.execSQL(sql.toString());
    		BarOption.updateLastModifiyTime();

            return true;
        } catch (Exception e) {
        	ErrorUtil.write(e);
            return false;
        }
    }
    
    private boolean deleteCategory(int idx) {
    	if(idx > 0) {
    		idx = idx * -1;
    	}
    	StringBuilder sql = new StringBuilder("DELETE FROM modification where type = ").append(idx);
        try {
            AikaPos.db.execSQL(sql.toString());
            
            sql = new StringBuilder("update modification set type = type + 1 where type < " + idx);	//update the other category
            AikaPos.db.execSQL(sql.toString());
    		BarOption.updateLastModifiyTime();

            sql = new StringBuilder("update modification set type = type - 1 where type > " + (-1 * idx));	//update modification of other category.
            AikaPos.db.execSQL(sql.toString());
    		BarOption.updateLastModifiyTime();
            
            return true;
        } catch (Exception e) {
        	ErrorUtil.write(e);
            return false;
        }
    }
    
    //arraylize the content in text area, including removing the bad items in the list.
    private ArrayList<String> getInputModification() {
        // arraylise the content in the text area
//        String[] usedFields = stringToArray(txaCurContent.getText());
        // filter the array, and do filter the empty items, move the goodones into a list.
        ArrayList<String> lstModificationSelected = new ArrayList<String> ();
//        if (usedFields != null && usedFields.length > 0) {
//            for (int i = 0; i < usedFields.length; i++) {
//                if (usedFields[i].length() == 0) // 是空串不用加
//                    continue;
//                lstModificationSelected.add(usedFields[i]);
//            }
//        }
        lstModificationSelected.trimToSize();
        return lstModificationSelected;
    }
    
//    private ArrayList<String> getModificationsInListComponent(){
//    	// 现在列表框中的字段
//        Object[] modificationInListComponent = listModel.toArray();
//
//    	ArrayList<String> modelArr = new ArrayList<String>(modificationInListComponent.length);
//        // 用列表框模型中的数据的名字加入 list
//        for (int i = 0; i < modificationInListComponent.length; i++) {
//            modelArr.add(((CheckItem) modificationInListComponent[i]).getName());
//        }
//        return modelArr;
//    }
    
    private void deleteClicked() {
//    	if(JOptionPane.showConfirmDialog(BarFrame.instance, BarFrame.consts.AllSelectedItemWillBeDeleted(), DlgConst.DlgTitle,
//                JOptionPane.YES_NO_OPTION) != 0) {
//			return;
//		}
//
//    	ListModel<CheckItem> model = modificationList.getModel();
//    	if(model.getSize() > 0) {
//	    	for(int i = model.getSize() - 1; i >= 0; i--) {
//	    		CheckItem checkItem = model.getElementAt(i);
//	    		if(checkItem.isSelected()) {
//	    			int tmpSelectionIndex = i;
//
//	    	        deleteModification(checkItem.getName(), tabbedPane.getSelectedIndex());
//	    	        listModel.remove(tmpSelectionIndex);
//	    	        itemChanged = true;
//
//	    	        updateTextOfTextArea();
//	    		}
//	    	}
//	    	if(!itemChanged) {
//		    	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.AtLeastOneShouldBeSelected());
//		    	return;
//	    	}
//    	}else {//delete the group.
//    		int selectedTabIdx = tabbedPane.getSelectedIndex();
//    		if(selectedTabIdx == 0) {
//    			return;
//    		}
//    		deleteCategory(selectedTabIdx);
//    		tabbedPane.remove(selectedTabIdx);
//    	}
    }

//    private void resetClicked() {
//        // 先全清空
//        listModel.removeAllElements();
//        // 表示不要再写入磁盘了
//        itemChanged = false;
//
//        CASControl.ctrl.getModel().resetCategroyName();
//        ArrayList<String> defaultCate = getAllModification(tabbedPane.getSelectedIndex());
//        // 再把系统默认的数据加入
//        for (int i = 0, count = defaultCate.size(); i < count; i++) {
//            listModel.addElement(new CheckItem(defaultCate.get(i), false));
//        }
//        // 设置默认选项
//        modificationList.setSelectedIndex(0);
//        // TODO: 从数据库中读取信息并装配文本区和列表框
//        txaCurContent.setText(CASUtility.EMPTYSTR);
//
//        // 确定按钮置有效
//        if (!btnOK.isEnabled()) {
//            btnOK.setEnabled(true);
//        }
//        // 删除按钮置有效
//        if (!btnDelete.isEnabled()) {
//            btnDelete.setEnabled(true);
//        }
//    }

    public ArrayList<String> getListInCurrentListComponent() {
//    	if(listModel == null) {
//    		listModel = new DefaultListModel<CheckItem>();
//    	}
//        Object[] aryListModelData = listModel.toArray();

        // ArrayList 存放列表框中的数据中的字符串以便于判断包含关系
        ArrayList<String> lstListModelArr = new ArrayList<String>();//aryListModelData.length);
//
//        CheckItem tmpCheckItem;
//
//        // add the data in list model into a list
//        for (int i = 0; i < aryListModelData.length; i++) {
//            tmpCheckItem = (CheckItem) aryListModelData[i];
//            lstListModelArr.add(tmpCheckItem.getName());
//        }
        return lstListModelArr;
    }

    public int findIndexByLang1(ArrayList<String> existingList, String content) {
    	String strToCompare = content;
    	int p = strToCompare.indexOf(BarDlgConst.semicolon);
    	if(p > 0)
    		strToCompare = strToCompare.substring(0, p);
    	
    	for(int i = 0; i < existingList.size(); i++) {
			if(existingList.get(i).startsWith(strToCompare))
				return i;
		}
    	return -1;	
    }
    
    private void applyToListClicked() {
//        //get the array in textArea and in the list.
//        ArrayList<String> inputList = getInputModification();
//        ArrayList<String> existingList = getListInCurrentListComponent();
//
//        // indicate that some item in the list has been changed.
//        itemChanged = true;
//
//        // give a loop to check if the content in text area is same with any of the item in the list
//        for (int i = 0; i < inputList.size(); i++) {
//            // go through each input. check if each language indicated, add ";;;" to complete it if no.
//        	String strInput = inputList.get(i).toString();
//        	if(strInput.indexOf(";") < 0) {
//        		strInput = strInput + ";;;";
//        	}
//
//        	int index = findIndexByLang1(existingList, strInput);
//            if (index == -1) {
//            	insertModification(strInput, "");
//            	listModel.addElement(new CheckItem(strInput, true));
//            }else {
//            	if(!existingList.get(index).equals(strInput)) {
//            		listModel.setElementAt(new CheckItem(inputList.get(i), true), index);
//            		updateToModification(strInput, "-123456");
//            	}
//            }
//        }
//        // now it's time to check if the item in list has been exist in the textArea, if no, set unselected， If yes, set slected
//        existingList = getListInCurrentListComponent();
//        for (int i = 0; i < existingList.size(); i++) {
//            if (!inputList.contains(existingList.get(i).toString())) {
//            	listModel.setElementAt(new CheckItem(existingList.get(i).toString(), false), i);
//            } else {
//            	listModel.setElementAt(new CheckItem(existingList.get(i).toString(), true), i);
//            }
//        }
//        updateTextOfTextArea();
//
//        btnApplyToList.setEnabled(false);
//        btnApplyToCategory.setEnabled(false);
    }
    
    private void addToCategoryClicked(){
//		String[] langs = StringUtil.split(txaCurContent.getText(), BarDlgConst.semicolon);
//
//    	String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
//    	if(title.equals(defaultTabName)) {
//    		//insert
//    		StringBuilder sql = new StringBuilder("INSERT INTO modification (lang1, lang2, lang3, type, status) VALUES (?, ?, ?, ")
//    				.append((tabbedPane.getTabCount()) * -1).append(", 0)");
//    		try {
//    			PreparedStatement pstmt = PIMDBModel.getConnection().prepareStatement(sql.toString());
//                pstmt.setString(1, langs.length > 0 ? BarUtil.addHTMLTAG(langs[0].trim()) : "");
//                pstmt.setString(2, langs.length > 1 ? BarUtil.addHTMLTAG(langs[1].trim()) : "");
//                pstmt.setString(3, langs.length > 2 ? BarUtil.addHTMLTAG(langs[2].trim()) : "");
//
//                pstmt.executeUpdate();
//        		BarOption.updateLastModifiyTime();
//
//    		}catch(Exception e) {
//    			L.e("AddModification ", "Exception when insert a category into modification.", e);
//    		}
//    	}else {
//    		//update
//    		StringBuilder sql = new StringBuilder("UPDATE modification set lang1 = '").append(BarUtil.addHTMLTAG(langs[0])).append("', ").append(" lang2 = '");
//    		if(langs.length > 1) {
//    			sql.append(BarUtil.addHTMLTAG(langs[1]));
//    		}
//    		sql.append("', lang3 = '");
//    		if(langs.length > 2) {
//    			sql.append(BarUtil.addHTMLTAG(langs[2]));
//    		}
//    		sql.append("' where type = ").append(tabbedPane.getSelectedIndex() * -1);
//    		try {
//    			PIMDBModel.getStatement().executeUpdate(sql.toString());
//        		BarOption.updateLastModifiyTime();
//    		}catch(Exception e) {
//    			L.e("AddModification ", "Exception when updating a category into modification.", e);
//    		}
//    	}
//    	initContent("null", tabbedPane.getSelectedIndex());
    }
    
    //设置文本的显示,本方法由列模型自动检测出选中项,组成以逗号为分隔的字符串
    private void updateTextOfTextArea() {
//        // 用来组装处理文本区中的显示
//        StringBuilder sb = new StringBuilder();
//        int tmpSize = listModel.size();
//
//        // 用于取值的一个变量
//        CheckItem tmpItem;
//
//        // 循环判断列表框中是否有打勾的,有就加入字符串
//        for (int i = 0; i < tmpSize; i++) {
//            // 要造型的
//            tmpItem = listModel.get(i);
//            if (tmpItem.isSelected()) {
//            	String langs = tmpItem.getName();
//            	if(langs.indexOf(BarDlgConst.semicolon) < 0) {
//            		langs = getAllLanguage(langs);
//            	}
//                sb = sb.append(langs).append(BarDlgConst.delimiter).append(" ");
//            }
//        }
//        // 要不为空,就要去掉逗号尾巴
//        txaCurContent.setText(sb.length() == 0 ? sb.toString() : sb.substring(0, sb.length() - 2));
    }

    private String getAllLanguage(String lang) {
//    	StringBuilder sql = new StringBuilder("select * from modification where (type ");
//    	if(tabbedPane.getSelectedIndex() == 0) {
//    		sql.append("is null or type = 0)");
//    	}else {
//    		sql.append("= ").append(tabbedPane.getSelectedIndex()).append(")");
//    	}
//    	sql.append(" and status = ").append(DBConsts.ORIGINAL)
//    		.append(" and lang").append(LoginDlg.USERLANG + 1).append(" = '").append(lang).append("'");
//        try {
//			ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
//            rs.beforeFirst();
//            while (rs.next()) {
//            	return new StringBuilder(BarUtil.removeHTMLTAG(rs.getString("lang1"))).append(BarDlgConst.semicolon)
//            		.append(BarUtil.removeHTMLTAG(rs.getString("lang2"))).append(BarDlgConst.semicolon)
//					.append(BarUtil.removeHTMLTAG(rs.getString("lang3"))).append(BarDlgConst.semicolon).toString();
//            }
//		}catch(Exception exp) {
//			L.e("AddModificationDlg", "exception when querying full langs with sql " + sql, exp);
//		}
//        L.e("AddModificationDlg", "find no modification record!" + sql, null);
        return null;
	}

	/**
     * 重载,以后要去掉
     * 
    public void show(String modification) {
        txaCurContent.setText(modification);
        // 一开始这项为禁止
        btnOK.setEnabled(false);

        // 初始化列表框数据
        // 把文本框中字段还原为字符串数组
        String[] usedFields = stringToArray(modification);
        ArrayList<String> inputModification = getInputModification();
        ArrayList<String> allModification = getAllModification(tabbedPane.getSelectedIndex());
        
        // 反向操作,如果用户的输入导致新的字段的产生,在列表框模型中要加入,并存盘

        if (usedFields != null && usedFields.length > 0) {
            // 这个循环是加字段
            for (int i = 0; i < usedFields.length; i++) {
                if (!allModification.contains(usedFields[i])) {
                    // 加一个标志为真的
                    listModel.addElement(new CheckItem(usedFields[i], true));
                    // 保存到数据库中
                    CASControl.ctrl.getModel().addCategroyName(usedFields[i], i);
                }
            }

            // 这个循环是处理以下这个情况:如果用户在类别按钮旁的文本区中自己减了一个
            for (int i = 0; i < allModification.size(); i++) {
                if (!inputModification.contains(allModification.get(i).toString())) {
                    // 去掉一个标志为真的
                    listModel.setElementAt(new CheckItem(allModification.get(i).toString(), false), i);
                } else {
                    listModel.setElementAt(new CheckItem(allModification.get(i).toString(), true), i);
                }
            }
        }
        // 一开始这项为禁止
        btnApplyToList.setEnabled(false);
        btnApplyToCategory.setEnabled(false);
        setVisible(true);
    }*/
//
//	@Override
//	public void stateChanged(ChangeEvent e) {
//		//check if the stateChange is valid
//		JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
//		PIMScrollPane scrPane = (PIMScrollPane)tabbedPane.getSelectedComponent();
//		if(scrPane == null) {
//			return;
//		}
//		//clean the properties field
//		valDspIdx.setText("");
//		valPrice.setText("");
//
//		//back up the old index and relavent text in txaCurContent.
//		selectionsMap.put(oldIndex, txaCurContent.getText());
//
//		//update the oldIndex to the new value(current selection), then init the txaCurContent with previouse selection.
//		int idx = tabbedPane.getSelectedIndex();
//		oldIndex = idx;
//		txaCurContent.setText(selectionsMap.get(idx));
//
//		// reflect the content in text area into the status of the list items.
//        ArrayList<String> inputModification = getInputModification();
//        ArrayList<String> allModification = getAllLangNamesFromDB(idx);
//        //start to init current list.
//    	listModel.clear();
//        for (int i = 0, count = allModification.size(); i < count; i++) {
//        	String[] langs = allModification.get(i).split(BarDlgConst.semicolon);
//        	int langIdx = LoginDlg.USERLANG;
//        	String lang_Modify = langs.length > langIdx ? langs[langIdx] : langs[0];
//        	if(lang_Modify.length() == 0)
//        		lang_Modify = langs[0];
//
//            // 置检查标志
//            listModel.addElement(new CheckItem(BarFrame.instance.curDish == null ? allModification.get(i) : lang_Modify, inputModification.contains(allModification.get(i))));
//        }
//
//        // 反向操作,如果用户的输入导致新的字段的产生,在列表框模型中要加入,并存盘
//        if (inputModification != null && inputModification.size() > 0) {
//            for (int i = 0; i < inputModification.size(); i++) {
//            	boolean notContained = true;
//            	for (String string : allModification) {
//					if(string.indexOf(inputModification.get(i)) > -1) {
//						notContained = false;
//						break;
//					}
//				}
////                if (notContained) {
////                    // 加一个标志为真的
////                    listModel.addElement(new CheckItem(inputModification.get(i), true));
////                    // 保存到数据库中
////                    insertModification(inputModification.get(i));
////                }
//            }
//        }
//        modificationList.setModel(listModel);
//        scrPane.getViewport().add(modificationList);
//
//        SwingUtilities.invokeLater(this);
//	}
	
//    @Override
//	public void valueChanged(ListSelectionEvent e) {
//        /*
//         * 主要来处理文本区中的显示 if(e.getSource() == categoryList) { StringBuffer sb = new StringBuffer(); int tmpSize =
//         * categoryListModel.size(); CheckItem tmpItem; for(int i = 0; i < tmpSize ; i++) { tmpItem =
//         * (CheckItem)categoryList.getSelectedValue(); if(tmpItem.isSelected()) { sb =
//         * sb.append(tmpItem.getName()).append(this.delimiter); } } textAreaData = sb.toString();
//         * if(textAreaData.length() >= 0) { //textAreaData = textAreaData.substring(0,textAreaData.length()-
//         * delimiter.length()); } textArea.setText(textAreaData); } //
//         */
//    }
//
//    @Override
//	public void keyPressed(KeyEvent e) {
//        if (e.getSource() == modificationList && e.getKeyCode() == KeyEvent.VK_SPACE) {        // 处理列表框空格键选中
//            updateTextArea();
//            updateProperties();
//        }
//    }
//
//    private void updateTextArea() {
//        // 处理列表本身的选中状态
//        int index = modificationList.getSelectedIndex();
//        if (index != -1) {
//            CheckItem seleItem = modificationList.getSelectedValue();
//            listModel.set(index, new CheckItem(seleItem.getName(), !seleItem.isSelected()));
//
//            // 以列表框的模型来设置文本区中的显示
//            updateTextOfTextArea();
//        }
//    }
//
//    @Override
//	public void keyReleased(KeyEvent e) {}
//
//    @Override
//	public void keyTyped(KeyEvent e) {
//        // 处理文本区的击键
//        if (e.getSource() == txaCurContent) { 	// && (e.getKeyChar() != ' ' && e.getKeyChar() != ',' && e.getKeyCode() !=
//        										// e.VK_TAB))//(e.getKeyCode() != e.VK_SPACE && e.getKeyChar() != e.VK_COMMA))
//            SwingUtilities.invokeLater(this);
//        }
//    }

    //-------------------adding and removing a mark------------------
    private int stepCounter;
    private boolean isDragging;
    private int minStepCounter = CustOpts.custOps.getValue("minMoveStep") == null ? 5 : Integer.valueOf((String)CustOpts.custOps.getValue("minMoveStep"));
//	@Override
//	public void mouseDragged(MouseEvent e) {
//		stepCounter++;
//		if(stepCounter > minStepCounter ) {
//			stepCounter = 0;
//			isDragging = true;
//		}
//	}

//	@Override
//	public void mouseMoved(MouseEvent e) {}
//
//    @Override
//	public void mouseClicked(MouseEvent e) {}
//
//    @Override
//	public void mouseEntered(MouseEvent e) {}
//
//    @Override
//	public void mouseExited(MouseEvent e) {}
//
//    @Override
//	public void mousePressed(MouseEvent e) {
//        if (e.getSource() == modificationList) {
//            updateTextArea();
//            updateProperties();
//        }
//    }

    //this method is used for removing an mark in the billPanel by a drag action on markDialog.
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		Object o = e.getSource();
//		if(o instanceof JButton) {	//if it's checkItem, then ignore the event.
//			stepCounter = 0;
//			int selectedRow = ((SalesPanel)BarFrame.instance.panels[2]).billPanel.table.getSelectedRow();
//
//			if(isDragging == true) {
//				isDragging = false;
//				adjustLableContent(BarUtil.removeHTMLTAG(((JButton)o).getText()), selectedRow, true);
//			}else {
//				adjustLableContent(BarUtil.removeHTMLTAG(((JButton)o).getText()), selectedRow, false);
//			}
//		}
//	}
	
	//adjust the display of the modification, dish in memory, record in db, and also the total area.
	private void adjustLableContent(String modify, int selectedRow, boolean isRemoving) {
		if(!checkSelectedItemStatus(selectedRow)) {
			return;
		}
    	
    	//update the display on table
//    	PIMTable table = ((SalesPanel)BarFrame.instance.panels[2]).billPanel.table;
//    	int row = table.getSelectedRow();
//    	String oldContent = (String)table.getValueAt(row, 2);
//    	StringBuilder newContent = new StringBuilder();
//
//    	//find matching mark.
//    	int p = modify.indexOf("(");
//    	String unitPrice = modify.substring(p + 2, modify.length() -1); //remove the two "(",")"
//    	String matchingStr = " " + modify.substring(0, p) + " ";	//why leave a " " here? it's for putting a qty,e.g. x3
//    	int idx = oldContent.indexOf(matchingStr);
//    	if(idx > -1) {	//this modify allready appeared
//			int qtAndMoneyStartAt = idx + matchingStr.length();	// counted the two " ". note: there might be a "x"
//			int numAndPriceEnd = oldContent.indexOf("\n", qtAndMoneyStartAt);
//
//        	//money part calculating
//        	String qtAndMoneyPart = oldContent.substring(qtAndMoneyStartAt).trim();
//        	int moneyStartAt = qtAndMoneyPart.indexOf("=");
//        	int oldNum = Integer.valueOf(qtAndMoneyPart.substring(1, moneyStartAt).trim());
//    		int newNum = oldNum + (isRemoving ? -1 : 1);
//    		if(newNum != 0) {
//    			float newPrice = Float.valueOf(unitPrice) * newNum;
//        		newContent.append(oldContent.substring(0, qtAndMoneyStartAt))
//        		.append("x").append(newNum)
//        		.append("=").append(BarUtil.formatMoney(newPrice))
//        		.append(oldContent.substring(numAndPriceEnd));
//    		}else {
//    			newContent.append(oldContent.substring(0, idx)).append(oldContent.substring(numAndPriceEnd + 1));
//    		}
//    	}else {		//this modify is new added one.
//    		if(isRemoving) {
//    			return;
//    		}
//    		//TODO: if end with dis count , add
//    		p = oldContent.indexOf("-" + BarOption.getMoneySign());
//    		if(p > -1) {
//    			newContent.append(oldContent.substring(0, p)).append(matchingStr)
//    			.append("x1=").append(unitPrice).append("\n").append(oldContent.substring(p));
//    		}else {
//    			newContent.append(oldContent).append(matchingStr).append("x1=").append(unitPrice).append("\n");
//    		}
//    	}
//    	table.setValueAt(newContent.toString(), row, 2);
//
//
//    	//new price
//    	int newPriceInLabel = (int)(Float.valueOf(unitPrice) * 100);	//new labels action will add/decreas money
//    	int newtotal = Math.round(BarFrame.instance.curDish.getTotalPrice() + ((isRemoving ? -1 : 1) * newPriceInLabel));
//
//    	//update the orderAry
//    	BarFrame.instance.curDish.setModification(newContent.toString());
//    	BarFrame.instance.curDish.setTotalPrice(newtotal);
//     	//update the database
//     	int outputID = BarFrame.instance.curDish.getOutputID();
//     	if(outputID >= 0) {
//     		String sql = "update output set toltalprice = " + BarFrame.instance.curDish.getTotalPrice() + " where id = " + outputID;
//     		try {
//     			PIMDBModel.getStatement().executeUpdate(sql);
//     		}catch(Exception exp) {
//				L.e("AddModificationDlg", " exception when update total price for output!" + sql, exp);
//     		}
//     	}
//
//     	//update table display
//     	table.setValueAt(BarOption.getMoneySign() + BarUtil.formatMoney(newtotal/100.0), row, 3);
//     	((SalesPanel)BarFrame.instance.panels[2]).billPanel.updateTotalArea(false);
	}

	private boolean checkSelectedItemStatus(int selectedRow) {
//		BillPanel billPanel = ((SalesPanel)BarFrame.instance.panels[2]).billPanel;
//		if(selectedRow < 0 || selectedRow >= billPanel.getOrderedDishAry().size())
//			return false;
//
//		if(billPanel.getOrderedDishAry().get(selectedRow).getOutputID() >= 0) {
//			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.SendItemCanNotModify());
//			return false;
//		}
//    	if(!billPanel.notTooLateToChangeStatus()) {	//check if it's bill printed, if yes, need to reopen a bill.
//    		return false;
//    	}
    	return true;
	}
	
	private void updateProperties() {
//    	//reset
//    	boolean differentPricedItemSelected = false;
//    	valDspIdx.setText("");
//    	valPrice.setText("");
//
//    	//non empty check
//    	ListModel<CheckItem> model = modificationList.getModel();
//    	if(model.getSize() <= 0) {
//    		return;
//    	}
//
//    	//get the category of current item
//    	int category = tabbedPane.getSelectedIndex();
//
//    	for(int i = model.getSize() - 1; i >= 0; i--) {
//    		CheckItem checkItem = model.getElementAt(i);
//    		if(checkItem.isSelected()) {
//    			if(differentPricedItemSelected) {	//if different price item selected already
//    				break;
//    			}
//    			//get the lang1 name of current item.
//    			String lang1 = checkItem.getName();
//    	    	int idx = lang1.indexOf(BarDlgConst.semicolon);
//    	    	if(idx > 0) {
//    	        	lang1 = lang1.substring(0, idx);
//    	    	}
//    	    	//prepare the sql
//    	    	StringBuilder sql = new StringBuilder("SELECT * FROM modification where status = ").append(DBConsts.ORIGINAL);
//    	        if(category == 0 ) {
//    	        	sql.append(" and type is null or type = 0");
//    	        }else if(category > 0){
//    	        	sql.append(" and type = ").append(category);
//    	        }else {
//    	        	//append no type condition if the category is -1;
//    	        }
//    	        sql.append(" and lang1 = '").append(BarUtil.addHTMLTAG(lang1)).append("'");
//    	        //fetch the dspIdx and price
//    	        try {
//    		        ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
//    		        rs.beforeFirst();
//    		        rs.next();
//    		        String curIdx = rs.getString("lang5");
//    		        String currentPrice = rs.getString("lang6");
//    		        if(valPrice.getText().equals("")) {	//if it's the first, then update the value.
//	    				valDspIdx.setText(curIdx);
//    		        	valPrice.setText(currentPrice);
//    		        }else if(!valPrice.getText().equals(currentPrice)){//if there's value already, and value are different, then stop
//	    				valDspIdx.setText("");			     //updadting, just set a "" and setup the flag!
//    					valPrice.setText("");
//    					differentPricedItemSelected = true;
//    		        }
//    	        }catch(Exception e) {
//    	        	L.e("AddModification ", "Exception happend when update the value in propertiy fields with searched out records." + sql, e);
//    	        }
//	    	}
//    	}
	}

//    @Override
	public void run() {
//    	//check if there's new contented added into the txaCurContent.
//        String curContent = txaCurContent.getText(); // 要检查文本区中是否有新字段产生,如果有便要使'添至列表'按钮激活 把文本框中字段还原为字符串数组
//        ArrayList<String> listInListComponent = getListInCurrentListComponent();
//        if (curContent == null || curContent.length() == 0) {	//no content at all, means no contented added into it.
//        	for (int i = 0; i < listInListComponent.size(); i++) {
//	        	listModel.setElementAt(new CheckItem(listInListComponent.get(i).toString(), false), i);
//        	}
//            btnApplyToList.setEnabled(false);
//            btnApplyToCategory.setEnabled(false);
//            return;
//        }
//
//        ArrayList<String> textArr = getInputModification();
//
//        if (hasNewField(listInListComponent, textArr) && !btnApplyToList.isEnabled()) { // 添加到列表按钮置有效
//            btnApplyToList.setEnabled(true);
//        }
//        btnApplyToCategory.setEnabled(textArr.size() == 1);
//
//        updateProperties();
    }
    
    // if found any one not contained in list already, then means has new fields.
	private boolean hasNewField(ArrayList<String> listInListComponent, ArrayList<String> textArr) {
		boolean hasNewField = false; 
        for (int i = 0; i < textArr.size(); i++) {
            if (!listInListComponent.contains(textArr.get(i).toString())) {
                hasNewField = true;
                break;
            }
        }
        return hasNewField;
	}
    
//	public String getCategories() {
//        return txaCurContent.getText();
//    }
//
//    private JButton btnApplyToList; // 几个按钮,在右上方
//    private JButton btnApplyToCategory; // 几个按钮,在右上方
//    private JButton btnDelete;
//    private JButton resetBTN;
//
//    private JButton btnClose;
//    private JList<CheckItem> modificationList; // 列表框及其模型
//    private JTabbedPane tabbedPane;
//    private JPanel panelList;
//    private PIMScrollPane scrollPanelAllMarks;
//    private DefaultListModel<CheckItem> listModel;
//
//    private JLabel midLabel; // "可用类别"标签
//    private JLabel topLabel; // "项目属于这些类别"标签
//
//    private JTextArea txaCurContent; // 文本区及其模型
//    public static final int OFFSET = -1;// 构建列表框和文本区用的一个常量 以后要去除
//    private boolean itemChanged; // 字段增减标志,用于存盘
//
//    private JLabel lblDspIdx;
//    private JLabel lblPrice;
//    private JTextField valDspIdx;
//    private JTextField valPrice;
//    private JButton btnApply;

}
