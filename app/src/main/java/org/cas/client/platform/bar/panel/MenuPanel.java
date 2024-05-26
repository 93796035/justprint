package org.cas.client.platform.bar.panel;

import android.database.Cursor;
import android.view.View;
import android.widget.ToggleButton;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.CustomizedRule;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.Printer;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.dialog.CategoryDlg;
import org.cas.client.platform.bar.dialog.DishDlg;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.ArrowButton;
import org.cas.client.platform.bar.uibeans.CategoryToggleButton;
import org.cas.client.platform.bar.uibeans.MenuButton;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MenuPanel extends View implements View.OnClickListener {
	
    private int curCategoryPage = 0;
    private int categoryQtPerPage = 0;

    private int curMenuPage = 0;
    private int menuQTPerPage = 0;

    Integer categoryColumn;
    Integer categoryRow;
    Integer menuColumn;
    Integer menuRow;

    public String[][] categoryNameMetrix;
    public ArrayList<ArrayList<CategoryToggleButton>> onSrcCategoryTgbMatrix = new ArrayList<ArrayList<CategoryToggleButton>>();
    public CategoryToggleButton tgbActiveCategory;
    
    //Dish is more complecated than category, it's devided by category first, then divided by page.
    public String[][] dishNameMetrix;// the struction must be [3][index]. it's more convenient than [index][3]
    public String[][] classifiedDishNameMetrix;// it's sub set of all menuNameMetrix
    private ArrayList<ArrayList<MenuButton>> onSrcMenuBtnMatrix = new ArrayList<ArrayList<MenuButton>>();

    private Printer[] printers;
    private CustomizedRule[] rules;
    private Dish[] dishAry;
    private List<Dish> classifiedDishAry;
    ArrayList<Dish> selectdDishAry = new ArrayList<Dish>();
	private int classifiedMenuIndex;
	public MenuPanel() {
        super(BarFrame.instance);
		initComponent();
	}

	public void initComponent() {
	    categoryRow = BarOption.getCategoryRow();
	    categoryColumn = BarOption.getCategoryCol();
	    menuRow = BarOption.getDishRow();
	    menuColumn = BarOption.getDishCol();
	    
		btnPageUpCategory = new ArrowButton("↑");
        btnPageDownCategory = new ArrowButton("↓");
        btnPageUpMenu = new ArrowButton("↑");
        btnPageDownMenu = new ArrowButton("↓");
        
//        btnPageUpCategory.setMargin(new Insets(0,0,0,0));
//        btnPageDownCategory.setMargin(btnPageUpCategory.getInsets());
//        btnPageUpMenu.setMargin(btnPageUpCategory.getInsets());
//        btnPageDownMenu.setMargin(btnPageUpCategory.getInsets());
//
//        btnPageUpCategory.setVisibility(GONE);
//        btnPageDownCategory.setVisibility(GONE);
//        btnPageUpMenu.setVisibility(GONE);
//        btnPageDownMenu.setVisibility(GONE);
//
//        btnPageUpCategory.addActionListener(this);
//        btnPageDownCategory.addActionListener(this);
//        btnPageUpMenu.addActionListener(this);
//        btnPageDownMenu.addActionListener(this);
//
//        setLayout(null);
//        setOpaque(false);
        initPrinters();
		initCategoryAndDishes();
		initRules();
		reLayout();
//		validate();
//		repaint();
	}
	
    public void initRules() {
		int tmpPos;
		//load all printers--------------------------
		String sql = "select * from Customized_Rule where status != -100 order by disp_Idx";
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
			tmpPos = rs.getCount();
			setRules(new CustomizedRule[tmpPos]);
			tmpPos = 0;
			while (rs.moveToNext()) {
				rules[tmpPos] = new CustomizedRule();
				rules[tmpPos].setId(Long.valueOf(DBUtil.getCursorInt(rs, "_id")));
				rules[tmpPos].setRulename(DBUtil.getCursorString(rs, "ruleName"));
				rules[tmpPos].setContent(DBUtil.getCursorString(rs, "content"));
				rules[tmpPos].setAction(DBUtil.getCursorInt(rs,"action")); // discount price( it's an int formatted)
				rules[tmpPos].setDispIdx(DBUtil.getCursorInt(rs,"dspIdx"));
				tmpPos++;
			}
			rs.close();
		}catch(Exception e) {
			L.e("MenuPanel", "exception when initPrinters", e);
		}
	}

	public void initCategoryAndDishes() {
        loadAllCategorys();
        loadAllDishes();
        curMenuPage = 0;
        reInitCategoryAndMenuBtns();
    }

	private void loadAllCategorys() {
		try {
            // load all the categorys---------------------------
            Cursor rs = AikaPos.db.rawQuery("select _ID, name1, name2, name3 from CATEGORY  where displayIdx >= 0 order by displayIdx", null);
            int tmpPos = rs.getCount();
            categoryNameMetrix = new String[3][tmpPos];
            PrintService.allCategory = new Category[tmpPos];
            tmpPos = 0;
            while (rs.moveToNext()) {
                categoryNameMetrix[0][tmpPos] = DBUtil.getCursorString(rs, "name1");
                categoryNameMetrix[1][tmpPos] = DBUtil.getCursorString(rs, "name2");
                categoryNameMetrix[2][tmpPos] = DBUtil.getCursorString(rs, "name3");
                
                PrintService.allCategory[tmpPos] =  new Category();
                PrintService.allCategory[tmpPos].setId(DBUtil.getCursorLong(rs,"_id"));
                PrintService.allCategory[tmpPos].setDisplayIdx(tmpPos);
                PrintService.allCategory[tmpPos].setLanguage(new String[]{categoryNameMetrix[0][tmpPos],
                		categoryNameMetrix[1][tmpPos], categoryNameMetrix[2][tmpPos]});
                
                tmpPos++;
            }
            rs.close();// 关闭
        } catch (Exception e) {
            L.e("MenuPanel ", "exception when loading all categories", e);
        }
	}
	
	private void loadAllDishes() {
		try {
            // load all the dishes----------------------------
            Cursor rs = AikaPos.db.rawQuery(
				"select _id, name1, name2, name3, PRICE, gst, qst,  size, printerIds, CATEGORY, pricePomp, menuPomp, ModifyPomp, displayIdx from dish where status == 0 order by displayIdx",
					null);
            int tmpPos = rs.getCount();
            dishNameMetrix = new String[3][tmpPos];
            dishAry = new Dish[tmpPos];
            //compose the record into dish objects--------------
            tmpPos = 0;
            while (rs.moveToNext()) { // @NOTE: don't load all the content, because menu can be many
                dishNameMetrix[0][tmpPos] = DBUtil.getCursorString(rs, "CODE");
                dishNameMetrix[1][tmpPos] = DBUtil.getCursorString(rs, "MNEMONIC");
                if (dishNameMetrix[1][tmpPos] == null || dishNameMetrix[1][tmpPos].trim().length() == 0) {
                	dishNameMetrix[1][tmpPos] = dishNameMetrix[0][tmpPos];
                }
                dishNameMetrix[2][tmpPos] = DBUtil.getCursorString(rs, "SUBJECT");
                if (dishNameMetrix[2][tmpPos] == null || dishNameMetrix[2][tmpPos].trim().length() == 0) {
                	dishNameMetrix[2][tmpPos] = dishNameMetrix[0][tmpPos];
                }

                dishAry[tmpPos] = new Dish();
                dishAry[tmpPos].setId(DBUtil.getCursorLong(rs,"_id"));
                dishAry[tmpPos].setLanguage(0, dishNameMetrix[0][tmpPos]);
                dishAry[tmpPos].setLanguage(1, dishNameMetrix[1][tmpPos]);
                dishAry[tmpPos].setLanguage(2, dishNameMetrix[2][tmpPos]);
                dishAry[tmpPos].setPrice(DBUtil.getCursorInt(rs,"PRICE")/100.0f);
                dishAry[tmpPos].setGst(DBUtil.getCursorInt(rs,"gst"));
                dishAry[tmpPos].setQst(DBUtil.getCursorInt(rs,"qst"));
                dishAry[tmpPos].setSize(DBUtil.getCursorInt(rs,"size"));
                dishAry[tmpPos].setPrinterIds(DBUtil.getCursorString(rs, "printerids"));
                dishAry[tmpPos].setCategory(DBUtil.getCursorString(rs, "CATEGORY"));
                dishAry[tmpPos].setPricePomp(Boolean.valueOf(DBUtil.getCursorString(rs, "pricePomp")));
                dishAry[tmpPos].setMenuPomp(Boolean.valueOf(DBUtil.getCursorString(rs, "menuPomp")));
                dishAry[tmpPos].setModifyPomp(Boolean.valueOf(DBUtil.getCursorString(rs, "modifyPomp")));
                dishAry[tmpPos].setDisplayIdx(DBUtil.getCursorInt(rs, "displayIdx"));
                tmpPos++;
            }
            rs.close();// 关闭
			
        } catch (Exception e) {
            L.e("MenuPanel ", "exception when loading all dishes", e);
        }
	}

	public void initPrinters() {
		
		int tmpPos;
		//load all printers--------------------------
		String sql = "select * from Printer where category = 0 order by _id";
		try {
			Cursor rs = AikaPos.db.rawQuery(sql, null);
			tmpPos = rs.getCount();
			setPrinters(new Printer[tmpPos]);
			tmpPos = 0;
			while (rs.moveToNext()) {
				printers[tmpPos] = new Printer();
				printers[tmpPos].setId(DBUtil.getCursorLong(rs,"_id"));
				printers[tmpPos].setName(DBUtil.getCursorString(rs,"NAME"));
				printers[tmpPos].setIp(DBUtil.getCursorString(rs,"IP"));
				printers[tmpPos].setStyle(DBUtil.getCursorInt(rs,"STYLE")); // index p1, p2.....
				printers[tmpPos].setCategory(DBUtil.getCursorInt(rs,"langType"));
				printers[tmpPos].setStatus(DBUtil.getCursorInt(rs,"STATUS"));
				tmpPos++;
			}
			rs.close();
			
			// rearrange into map
			for(Printer printer : printers){
				if(printer.getIp().trim().length() > 0) {
					PrintService.ipPrinterMap.put(printer.getIp().trim(),printer);
				}
			}
		}catch(Exception e) {
			L.e("MenuPanel", "exception when initPrinters", e);
		}
	}

    // menu and category buttons must be init after initContent---------
    public void reInitCategoryAndMenuBtns() {
        // validate rows and columns first(in case they are changed into bad value)--------
        categoryColumn = (categoryColumn == null || categoryColumn < 4) ? 5 : categoryColumn;
        categoryRow = (categoryRow == null || categoryRow < 1 || categoryRow > 9) ? 3 : categoryRow;
        categoryQtPerPage = categoryColumn * categoryRow;

        menuColumn = (menuColumn == null || menuColumn < 1) ? 4 : menuColumn;
        menuRow = (menuRow == null || menuRow < 1) ? 4 : menuRow;
        menuQTPerPage = menuColumn * menuRow;

        // clean current catogory and menus from both screen and metrix if have---------------
//        removeAll();
//        add(btnPageUpCategory);
//        add(btnPageDownCategory);
//        add(btnPageUpMenu);
//        add(btnPageDownMenu);
        
        onSrcCategoryTgbMatrix.clear();
        onSrcMenuBtnMatrix.clear();

        // create new buttons and add onto the screen (no layout yet)------------
        int globleCategoryIdxOfCurCategory = curCategoryPage * categoryQtPerPage;
        for (int r = 0; r < categoryRow; r++) {
            ArrayList<CategoryToggleButton> btnCategoryArry = new ArrayList<CategoryToggleButton>();
            for (int c = 0; c < categoryColumn; c++) {
                
                CategoryToggleButton btnCategory = new CategoryToggleButton(globleCategoryIdxOfCurCategory + 1);
//                btnCategory.setMargin(new Insets(0, 0, 0, 0));
//                add(btnCategory);
//                btnCategory.addActionListener(this);
                btnCategoryArry.add(btnCategory);
                
                if (globleCategoryIdxOfCurCategory < categoryNameMetrix[0].length) {
                	String defaultText = categoryNameMetrix[0][globleCategoryIdxOfCurCategory];
                	int lang = CustOpts.custOps.getUserLang();
                	if(lang > categoryNameMetrix.length - 1)
                		lang = categoryNameMetrix.length - 1;
                	String text = categoryNameMetrix[lang][globleCategoryIdxOfCurCategory];
                    btnCategory.setText(BarUtil.empty(text) ? defaultText : text);
                    btnCategory.setLang1Text(defaultText);
                    if (tgbActiveCategory != null) {
                    	String categoryString = categoryNameMetrix[lang][globleCategoryIdxOfCurCategory];
                    	if(categoryString == null || categoryString.length() == 0) {
                    		categoryString = categoryNameMetrix[0][globleCategoryIdxOfCurCategory];
                    	}
                    	if(categoryString.equalsIgnoreCase(tgbActiveCategory.getText().toString())) {
                    		btnCategory.setSelected(true);
                    	}
                    }
                }
                
                globleCategoryIdxOfCurCategory++;
            }
            onSrcCategoryTgbMatrix.add(btnCategoryArry);
        }

        // if no activeCategory, use the first one on screen.
        if (tgbActiveCategory == null) {
            tgbActiveCategory = onSrcCategoryTgbMatrix.get(0).get(0);
            tgbActiveCategory.setSelected(true);
        }

        // initialize on screen menus===============================================================
        //find out menus matching to current category and current lang
        classifiedDishNameMetrix = new String[3][dishNameMetrix[0].length];
        classifiedDishAry = new ArrayList<Dish>();
        classifiedMenuIndex = 0;
        for (int i = 0; i < dishAry.length; i++) {
			if(dishAry[i].getCategory().equals(tgbActiveCategory.getLang1Text())) {
				
				classifiedDishNameMetrix[0][classifiedMenuIndex] = dishNameMetrix[0][i];
				classifiedDishNameMetrix[1][classifiedMenuIndex] = dishNameMetrix[1][i];
				classifiedDishNameMetrix[2][classifiedMenuIndex] = dishNameMetrix[2][i];
				
				classifiedDishAry.add(dishAry[i]);
				//make sure the display index are lined
				if(dishAry[i].getDisplayIdx() != classifiedMenuIndex + 1) {
					try {

			            StringBuilder sql = new StringBuilder("UPDATE product SET INDEX = ").append(classifiedMenuIndex + 1)
			                            	.append(" where ID = ").append(dishAry[i].getId());
			            AikaPos.db.execSQL(sql.toString());
    	        		BarOption.updateLastModifiyTime();
		            }catch(Exception exp) {
		                exp.printStackTrace();
		            }
					dishAry[i].setDisplayIdx(classifiedMenuIndex + 1);
				}
				
				classifiedMenuIndex++;
			}
		}
        
        int globleMenuIdxOfCurCategory = curMenuPage * menuQTPerPage;	//the ones which will be on current page.
        for (int r = 0; r < menuRow; r++) {
            ArrayList<MenuButton> btnMenuArry = new ArrayList<MenuButton>();
            for (int c = 0; c < menuColumn; c++) {
                MenuButton btnMenu = new MenuButton(globleMenuIdxOfCurCategory + 1);
//                btnMenu.setMargin(new Insets(0, 0, 0, 0));
//                add(btnMenu);
                btnMenu.setOnClickListener(this);
                btnMenuArry.add(btnMenu);
                if (globleMenuIdxOfCurCategory < classifiedMenuIndex) {	//the last page could be not full page.
                	Dish dish = classifiedDishAry.get(globleMenuIdxOfCurCategory);
                    btnMenu.setDish(dish);
                	int lang = CustOpts.custOps.getUserLang();
                	if(lang > classifiedDishNameMetrix.length - 1)
                		lang = classifiedDishNameMetrix.length - 1;
                	String text = classifiedDishNameMetrix[lang][globleMenuIdxOfCurCategory];
                	if(BarOption.showPriceOnMenu()) {
                		if(text.endsWith("</center></html>")) {
                			text = text.substring(0, text.length() - 16);
                		}
                		text += "<br>";
                		text += BarUtil.formatMoney(dish.getPrice() / 100.0);
                		text += "</center></html>";
                		if(!text.startsWith("<html><center>")) {
                			text = "<html><center>" + text;
                		}
                	}
                    btnMenu.setText(BarUtil.empty(text) ? classifiedDishNameMetrix[0][globleMenuIdxOfCurCategory] : text);
                    globleMenuIdxOfCurCategory++;
                }
            }
            onSrcMenuBtnMatrix.add(btnMenuArry);
        }
        
        adjustPageArrowStatus(globleCategoryIdxOfCurCategory, globleMenuIdxOfCurCategory);
    }

	private void adjustPageArrowStatus(int globleCategoryIdxOfCurCategory, int globleMenuIdxOfCurCategory) {
		
		//for category panel page up
        if(globleCategoryIdxOfCurCategory <= categoryQtPerPage && curCategoryPage == 0) {		//the last displayed category is less than categoryQtPerPage, means
        	btnPageUpCategory.setCursorVisible(false);				  			// now it's on first page. no need to display up arrow.
        }else {
        	btnPageUpCategory.setCursorVisible(true);			//as long as not the first page, page up should display.
        }
        //for category panel page down
        int lastCateIdx = categoryRow * categoryColumn * (curCategoryPage + 1);
        if(lastCateIdx > categoryNameMetrix[0].length) {//if is the last page, and not full of last page, then should not display page down arrow.
        	btnPageDownCategory.setCursorVisible(false);
        }else if(BarFrame.instance == null ||(lastCateIdx == categoryNameMetrix[0].length && BarFrame.instance.curPanelIdx != 3)) {//if full of page, but is sales page, then hide.
        	btnPageDownCategory.setCursorVisible(false);
        }else {	// if there's more than one page to display, and currently it's not last page then show it.
        	btnPageDownCategory.setCursorVisible(true);
        }
		
		//for menu panel page up
        if(globleMenuIdxOfCurCategory <= menuQTPerPage && curMenuPage == 0) {		//the last displayed menu is less than curMenuPerPage, means
            btnPageUpMenu.setCursorVisible(false);				  	// now it's on first page. no need to display up arrow.
        }else {
        	btnPageUpMenu.setCursorVisible(true);			//as long as not the first page, page up should display.
        }
        //for menu panel page down
        int lastIdx = menuRow * menuColumn * (curMenuPage + 1);
        if(lastIdx > classifiedMenuIndex) {//if is the last page, and not full of last page, then should not display page down arrow.
        	btnPageDownMenu.setCursorVisible(false);
        }else if(BarFrame.instance == null //ignore the case when app not finished start and the BarFrame is still null.
        		|| (lastIdx == classifiedMenuIndex  && BarFrame.instance.curPanelIdx != 3)) {//if it's full of page, but is not in setting page then hide!
        	btnPageDownMenu.setCursorVisible(false);
        }else {	// if there's more than one page to display, and currently it's not last page then show it.
        	btnPageDownMenu.setCursorVisible(true);
        }
	}

    public void reLayout() {
        // category area--------------
        Float categoryHeight = BarOption.getCategoryAreaPortion();
        int h_gap = CustOpts.HOR_GAP / 2;
        int v_gat = CustOpts.VER_GAP / 2;
        int categeryBtnWidth = (getWidth() - h_gap * (categoryColumn - 1)) / categoryColumn;
        if(btnPageDownCategory.getVisibility() == View.VISIBLE|| btnPageUpCategory.getVisibility() == View.VISIBLE) {
        	categeryBtnWidth = (getWidth() - h_gap * categoryColumn - BarFrame.consts.SCROLLBAR_WIDTH) / categoryColumn;
        }
        int categeryBtnHeight =
                (int) ((getHeight() * categoryHeight - v_gat * (categoryRow - 1)) / categoryRow);

        for (int r = 0; r < categoryRow; r++) {
        	if(r == onSrcCategoryTgbMatrix.size()) {
        		return;
        	}
            for (int c = 0; c < categoryColumn; c++) {
            	if(c == onSrcCategoryTgbMatrix.get(r).size()) {
            		return;
            	}
                ToggleButton toggleButton = onSrcCategoryTgbMatrix.get(r).get(c);
//                toggleButton.setBounds((categeryBtnWidth + h_gap) * c,
//                        (categeryBtnHeight + v_gat) * r, categeryBtnWidth, categeryBtnHeight);
            }
        }
//        btnPageUpCategory.setBounds((categeryBtnWidth + h_gap) * categoryColumn, 0,
//        	BarFrame.consts.SCROLLBAR_WIDTH, BarFrame.consts.SCROLLBAR_WIDTH * 2);
//        btnPageDownCategory.setBounds(btnPageUpCategory.getX(),
//                btnPageUpCategory.getY() + btnPageUpCategory.getHeight() + v_gat,
//                BarFrame.consts.SCROLLBAR_WIDTH, BarFrame.consts.SCROLLBAR_WIDTH * 2);

        // menu area--------------
        if(onSrcMenuBtnMatrix.size() == 0) {
        	return;
        }
        int menuY = (categeryBtnHeight + v_gat) * categoryRow + v_gat;
        int menuBtnWidth = (getWidth() - h_gap * (menuColumn - 1)) / menuColumn;
        if(btnPageUpMenu.getVisibility() == View.VISIBLE|| btnPageDownMenu.getVisibility() == View.VISIBLE) {
        	menuBtnWidth = (getWidth() - h_gap * menuColumn - BarFrame.consts.SCROLLBAR_WIDTH) / menuColumn;
        }
        int menuBtnHeight = (int) ((getHeight() * (1 - categoryHeight) - v_gat * (menuRow + 1)) / menuRow);
        for (int r = 0; r < menuRow; r++) {
        	if(r == onSrcMenuBtnMatrix.size()) {
        		return;
        	}
            for (int c = 0; c < menuColumn; c++) {
            	if(c == onSrcMenuBtnMatrix.get(r).size()) {
            		return;
            	}
//                onSrcMenuBtnMatrix.get(r).get(c).setBounds((menuBtnWidth + h_gap) * c, menuY + (menuBtnHeight + v_gat) * r, menuBtnWidth, menuBtnHeight);
            }
        }
//        btnPageUpMenu.setBounds((menuBtnWidth + h_gap) * menuColumn,
//        		getHeight() - BarFrame.consts.SCROLLBAR_WIDTH * 4 - v_gat,
//                BarFrame.consts.SCROLLBAR_WIDTH,
//                BarFrame.consts.SCROLLBAR_WIDTH * 2);
//        btnPageDownMenu.setBounds(btnPageUpMenu.getX(), btnPageUpMenu.getY() + btnPageUpMenu.getHeight()
//                + v_gat, BarFrame.consts.SCROLLBAR_WIDTH, BarFrame.consts.SCROLLBAR_WIDTH * 2);

    }
    
	@Override
    public void onClick(View o) {
        // category buttons---------------------------------------------------------------------------------
        if (o instanceof CategoryToggleButton) {
            CategoryToggleButton categoryToggle = (CategoryToggleButton) o;
            String text = categoryToggle.getText().toString();
            if (text == null || text.length() == 0) { // check if it's empty
            	if (getParent() == BarFrame.instance.panels[3]) { // and it's in setting panel, add a Category.
                    CategoryDlg addCategoryDlg = new CategoryDlg(this);
                    addCategoryDlg.setCategoryBtn(categoryToggle);
					addCategoryDlg.show(BarFrame.instance.getSupportFragmentManager(), "CategoryDialog");
                }
            } else { // if it's not empty
                if (!text.equals(tgbActiveCategory.getText())) {
                   //change active toggle button, and update active menus.

                   tgbActiveCategory.setSelected(false);

                   tgbActiveCategory = categoryToggle;
                   initCategoryAndDishes();	//fill menu buttons with menus belong to this category.
                   reLayout();
                } else if (getParent() == BarFrame.instance.panels[3]) {
                   CategoryDlg categoryDlg = new CategoryDlg(this);
                   categoryDlg.setCategoryBtn(categoryToggle);
                   categoryDlg.show(BarFrame.instance.getSupportFragmentManager(), "CategoryDialog");
                } else {
                	((CategoryToggleButton) o).setSelected(true);
                }
            }
        }
        // menu buttons------------------------------------------------------------------------------------------
        else if (o instanceof MenuButton) {
            MenuButton menuButton = (MenuButton) o;
            String text = menuButton.getText().toString();
            if (text == null || text.length() == 0) { // check if it's empty
                if (getParent() == BarFrame.instance.panels[3]) { // and it's setting panel, add a Category.
                	if(tgbActiveCategory == null || tgbActiveCategory.getText() == null || tgbActiveCategory.getText().length() < 1) {
    					JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.SetCatogoryFirst());
    					return;
                	}
                    new DishDlg(menuButton.getDspIndex()).show(BarFrame.instance.getSupportFragmentManager(), "DishDialog");
                }
            } else { // if it's not empty
                if (getParent() == BarFrame.instance.panels[3]) {
                    new DishDlg(menuButton.getDish()).show(BarFrame.instance.getSupportFragmentManager(), "DishDialog");
                } else if (getParent() == BarFrame.instance.panels[2]){
                    // add into selection.
                	Dish dish = menuButton.getDish();
                	addIntoBillPane(dish);
                } else {
                	//do nothing, currently this case is when displayed in a dialog, which is poped up from table pane.
                }
            }
        } 
        // arrow buttons-------------------------------------------------------------------------------------------------------
        else if(o instanceof ArrowButton) {
	        if(o == btnPageUpCategory) {
	            curCategoryPage--;
	            // adjust status
	            btnPageDownCategory.setVisibility(VISIBLE);
	            if (curCategoryPage == 0) {
	                btnPageUpCategory.setVisibility(GONE);
	            }
	
	            reInitCategoryAndMenuBtns();
	            reLayout();
	        } else if (o == btnPageDownCategory) {
	            curCategoryPage++;
	            // adjust status
	            btnPageUpCategory.setVisibility(VISIBLE);
	            if (curCategoryPage * categoryQtPerPage > categoryNameMetrix.length) {
	                btnPageDownCategory.setVisibility(GONE);
	            }
	
	            reInitCategoryAndMenuBtns();
	            reLayout();
	        } else if (o == btnPageUpMenu) {
	            curMenuPage--;
	            btnPageDownMenu.setVisibility(VISIBLE);
	            if (curMenuPage == 0) {
	                btnPageUpMenu.setVisibility(GONE);
	            }
	            reInitCategoryAndMenuBtns();
	            reLayout();
	        } else if (o == btnPageDownMenu) {
	            curMenuPage++;
	            btnPageUpMenu.setVisibility(VISIBLE);
	            if (curMenuPage * menuQTPerPage > dishNameMetrix[0].length) {
	                btnPageDownMenu.setVisibility(GONE);
	            }
	            reInitCategoryAndMenuBtns();
	            reLayout();
	        }
        }
	}

	public void addIntoBillPane(Dish comboDish) {
		BillPanel billPanel = ((SalesPanel)BarFrame.instance.panels[2]).billPanel;
		if(!billPanel.notTooLateToChangeStatus()) {
			return;
		}
		//if it's counter mode and this is the first dish adding to the bill Panel, then update the openTime to now.
		//because the bill is created when last bill saved, and the open time generated at that time point does not make sense
		//while the time when the first dish added will make more sense (more importantly, it will make the SEV time in MEV machine more close
		//to the MEV time in mev machine, we believe it will make the mev log info looks more normal.)
		if(BarOption.isCounterMode() && billPanel.getOrderedDishAry().size() == 0) {
			//update the openTime.
			String timeStr = BarUtil.df.format(new Date());
    		StringBuilder sql = new StringBuilder("update bill set openTime = '").append(timeStr)
     				.append("' where id = ").append(billPanel.getBillID());
    		try {
	     		AikaPos.db.execSQL(sql.toString());
	     		//updating the opentime of the table--------for counter mode, this step is necessary, because user might need to switch between the 
	     		//openned tables(table is worked as bills in counter mode.)
	     		sql = new StringBuilder("update dining_Table set opentime = '").append(timeStr)
	    				.append("' WHERE name = '").append(BarFrame.instance.getCurTable()).append("'");
				AikaPos.db.execSQL(sql.toString());
	     		BarFrame.instance.valOpenTime.setText(timeStr);
    		} catch (Exception e) {
    			L.e("MenuPanel ", "Updating opentime when adding first dish: " + sql, e);
			}

		}
		if(comboDish.getPrice() < 0) {	//if it's comboDish.
			//match a rule, to get the relevant dishes
			CustomizedRule[] rules = BarFrame.menuPanel.getRules();
			for (CustomizedRule rule : rules) {
				if(rule.getRulename().equals(comboDish.getLanguage(0))
						||rule.getRulename().equals(comboDish.getLanguage(1))
						||rule.getRulename().equals(comboDish.getLanguage(2))) {
					//get the string from rule
		    		ArrayList<String> dishStrs = new ArrayList<String>();
		    		String[] disIds = rule.getContent().split(",");
		    		for (String disId : disIds) {
		    			for (Dish dish : dishAry) {
							if(String.valueOf(dish.getId()).equals(disId)) {
								billPanel.addContentToList(dish);
								break;
							}
						}
					}
					break;
				}
			}
		}else {
			billPanel.addContentToList(comboDish);
		}
	}

    public Printer[] getPrinters() {
		return printers;
	}

	public void setPrinters(Printer[] printers) {
		this.printers = printers;
	}

	private ArrowButton btnPageUpCategory;
    private ArrowButton btnPageDownCategory;
    private ArrowButton btnPageUpMenu;
    private ArrowButton btnPageDownMenu;
	
    public Dish[] getDishAry() {
		return dishAry;
	}

	public CustomizedRule[] getRules() {
		return rules;
	}

	public void setRules(CustomizedRule[] rules) {
		this.rules = rules;
	}
}
