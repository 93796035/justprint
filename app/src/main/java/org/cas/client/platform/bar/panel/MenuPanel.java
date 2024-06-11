package org.cas.client.platform.bar.panel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.CustomizedRule;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.Printer;
import com.just.print.ui.panel.PicturePane;
import com.just.print.ui.panel.SalesPanel;
import com.just.print.util.JOptionPane;
import com.just.print.util.StringUtils;

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

public class MenuPanel extends PicturePane implements View.OnClickListener {

	private int curCategoryPage = 0;
	private int categoryQtPerPage = 0;

	private int curMenuPage = 0;
	private int menuQTPerPage = 0;

	Integer categoryColumn;
	Integer categoryRow;
	Integer menuColumn;
	Integer menuRow;

	public String[][] categoryNameMetrix;
	public ArrayList<ArrayList<CategoryToggleButton>> onSrcCategoryTgbMatrix = new ArrayList<>();
	public CategoryToggleButton tgbActiveCategory;

	// Dish is more complicated than category, it's divided by category first, then divided by page.
	public String[][] dishNameMetrix; // the structure must be [3][index]. it's more convenient than [index][3]
	public String[][] classifiedDishNameMetrix; // it's sub set of all menuNameMetrix
	private ArrayList<ArrayList<MenuButton>> onSrcMenuBtnMatrix = new ArrayList<>();

	private Printer[] printers;
	private CustomizedRule[] rules;
	private Dish[] dishAry;
	private List<Dish> classifiedDishAry;
	ArrayList<Dish> selectdDishAry = new ArrayList<>();
	private int classifiedMenuIndex;

	public MenuPanel(String image) {
		super(image);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = inflater.inflate(getLayoutId(), container, false);
		initComponent();
		return mRoot;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_menu_panel;
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

		btnPageUpCategory.setMargin(0, 0, 0, 0);
		btnPageDownCategory.setMargin(0, 0, 0, 0);
		btnPageUpMenu.setMargin(0, 0, 0, 0);
		btnPageDownMenu.setMargin(0, 0, 0, 0);

		btnPageUpCategory.setVisibility(GONE);
		btnPageDownCategory.setVisibility(GONE);
		btnPageUpMenu.setVisibility(GONE);
		btnPageDownMenu.setVisibility(GONE);

		btnPageUpCategory.setOnClickListener(this);
		btnPageDownCategory.setOnClickListener(this);
		btnPageUpMenu.setOnClickListener(this);
		btnPageDownMenu.setOnClickListener(this);

		initPrinters();
		initCategoryAndDishes();
		initRules();
	}

	public void reLayout() {
		// 获取宽度和高度
		int width = mRoot.getWidth();
		int height = mRoot.getHeight();

		// category area--------------
		Float categoryHeight = BarOption.getCategoryAreaPortion();
		int h_gap = CustOpts.HOR_GAP / 2;
		int v_gap = CustOpts.VER_GAP / 2;
		int categeryBtnWidth = (width - h_gap * (categoryColumn - 1)) / categoryColumn;

		if (btnPageDownCategory.getVisibility() == VISIBLE || btnPageUpCategory.getVisibility() == VISIBLE) {
			categeryBtnWidth = (width - h_gap * categoryColumn - BarFrame.consts.SCROLLBAR_WIDTH) / categoryColumn;
		}
		int categeryBtnHeight = (int) ((height * categoryHeight - v_gap * (categoryRow - 1)) / categoryRow);

		for (int r = 0; r < categoryRow; r++) {
			if (r == onSrcCategoryTgbMatrix.size()) {
				return;
			}
			for (int c = 0; c < categoryColumn; c++) {
				if (c == onSrcCategoryTgbMatrix.get(r).size()) {
					return;
				}
				ToggleButton toggleButton = onSrcCategoryTgbMatrix.get(r).get(c);
				GridLayout.LayoutParams params = new GridLayout.LayoutParams();
				params.width = categeryBtnWidth;
				params.height = categeryBtnHeight;
				params.setMargins(h_gap / 2, v_gap / 2, h_gap / 2, v_gap / 2);
				toggleButton.setLayoutParams(params);
			}
		}

		// menu area--------------
		if (onSrcMenuBtnMatrix.size() == 0) {
			return;
		}
		int menuY = (categeryBtnHeight + v_gap) * categoryRow + v_gap;
		int menuBtnWidth = (width - h_gap * (menuColumn - 1)) / menuColumn;

		if (btnPageUpMenu.getVisibility() == VISIBLE || btnPageDownMenu.getVisibility() == VISIBLE) {
			menuBtnWidth = (width - h_gap * menuColumn - BarFrame.consts.SCROLLBAR_WIDTH) / menuColumn;
		}
		int menuBtnHeight = (int) ((height * (1 - categoryHeight) - v_gap * (menuRow + 1)) / menuRow);

		for (int r = 0; r < menuRow; r++) {
			if (r == onSrcMenuBtnMatrix.size()) {
				return;
			}
			for (int c = 0; c < menuColumn; c++) {
				if (c == onSrcMenuBtnMatrix.get(r).size()) {
					return;
				}
				MenuButton menuButton = onSrcMenuBtnMatrix.get(r).get(c);
				GridLayout.LayoutParams params = new GridLayout.LayoutParams();
				params.width = menuBtnWidth;
				params.height = menuBtnHeight;
				params.setMargins(h_gap / 2, v_gap / 2, h_gap / 2, v_gap / 2);
				menuButton.setLayoutParams(params);
			}
		}
	}

	public void initRules() {
		int tmpPos;
		//load all printers--------------------------
		String sql = "select * from Customized_Rule where status != -100 order by disp_Idx";
		try {
			Cursor rs = AikaPos.db.rawQuery(sql, null);
			tmpPos = rs.getCount();
			setRules(new CustomizedRule[tmpPos]);
			tmpPos = 0;
			while (rs.moveToNext()) {
				rules[tmpPos] = new CustomizedRule();
				rules[tmpPos].setId(Long.valueOf(DBUtil.getCursorInt(rs, "_id")));
				rules[tmpPos].setRulename(DBUtil.getCursorString(rs, "ruleName"));
				rules[tmpPos].setContent(DBUtil.getCursorString(rs, "content"));
				rules[tmpPos].setAction(DBUtil.getCursorInt(rs, "action")); // discount price( it's an int formatted)
				rules[tmpPos].setDispIdx(DBUtil.getCursorInt(rs, "dspIdx"));
				tmpPos++;
			}
			rs.close();
		} catch (Exception e) {
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
			Cursor rs = AikaPos.db.rawQuery("select _ID, name1, name2, name3 from CATEGORY where displayIdx >= 0 order by displayIdx", null);
			int tmpPos = rs.getCount();
			categoryNameMetrix = new String[3][tmpPos];
			PrintService.allCategory = new Category[tmpPos];
			tmpPos = 0;
			while (rs.moveToNext()) {
				categoryNameMetrix[0][tmpPos] = DBUtil.getCursorString(rs, "NAME1");
				categoryNameMetrix[1][tmpPos] = DBUtil.getCursorString(rs, "NAME2");
				categoryNameMetrix[2][tmpPos] = DBUtil.getCursorString(rs, "NAME3");

				PrintService.allCategory[tmpPos] = new Category();
				PrintService.allCategory[tmpPos].setId(DBUtil.getCursorLong(rs, "_id"));
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
					"select _id, name1, name2, name3, PRICE, gst, qst,  size, printerIds, CATEGORY, pricePomp, menuPomp, ModifyPomp, displayIdx from Dish where status == 0 or status is null order by displayIdx",
					null);
			int tmpPos = rs.getCount();
			dishNameMetrix = new String[3][tmpPos];
			dishAry = new Dish[tmpPos];
			//compose the record into dish objects--------------
			tmpPos = 0;
			while (rs.moveToNext()) { // @NOTE: don't load all the content, because menu can be many
				dishNameMetrix[0][tmpPos] = DBUtil.getCursorString(rs, "NAME1");
				dishNameMetrix[1][tmpPos] = DBUtil.getCursorString(rs, "NAME2");
				if (dishNameMetrix[1][tmpPos] == null || dishNameMetrix[1][tmpPos].trim().length() == 0) {
					dishNameMetrix[1][tmpPos] = dishNameMetrix[0][tmpPos];
				}
				dishNameMetrix[2][tmpPos] = DBUtil.getCursorString(rs, "NAME3");
				if (dishNameMetrix[2][tmpPos] == null || dishNameMetrix[2][tmpPos].trim().length() == 0) {
					dishNameMetrix[2][tmpPos] = dishNameMetrix[0][tmpPos];
				}

				dishAry[tmpPos] = new Dish();
				dishAry[tmpPos].setId(DBUtil.getCursorLong(rs, "_id"));
				dishAry[tmpPos].setLanguage(0, dishNameMetrix[0][tmpPos]);
				dishAry[tmpPos].setLanguage(1, dishNameMetrix[1][tmpPos]);
				dishAry[tmpPos].setLanguage(2, dishNameMetrix[2][tmpPos]);
				dishAry[tmpPos].setPrice(DBUtil.getCursorInt(rs, "PRICE") / 100.0f);
				dishAry[tmpPos].setGst(DBUtil.getCursorInt(rs, "GST"));
				dishAry[tmpPos].setQst(DBUtil.getCursorInt(rs, "QST"));
				dishAry[tmpPos].setSize(DBUtil.getCursorInt(rs, "SIZE"));
				dishAry[tmpPos].setPrinterIds(DBUtil.getCursorString(rs, "printerIds"));
				dishAry[tmpPos].setCategory(DBUtil.getCursorString(rs, "category"));
				dishAry[tmpPos].setPricePomp(Boolean.valueOf(DBUtil.getCursorString(rs, "pricePomp")));
				dishAry[tmpPos].setMenuPomp(Boolean.valueOf(DBUtil.getCursorString(rs, "menuPomp")));
				dishAry[tmpPos].setModifyPomp(Boolean.valueOf(DBUtil.getCursorString(rs, "ModifyPomp")));
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
				printers[tmpPos].setId(DBUtil.getCursorLong(rs, "_id"));
				printers[tmpPos].setName(DBUtil.getCursorString(rs, "NAME"));
				printers[tmpPos].setIp(DBUtil.getCursorString(rs, "IP"));
				printers[tmpPos].setStyle(DBUtil.getCursorInt(rs, "STYLE")); // index p1, p2.....
				printers[tmpPos].setCategory(DBUtil.getCursorInt(rs, "langType"));
				printers[tmpPos].setStatus(DBUtil.getCursorInt(rs, "STATUS"));
				tmpPos++;
			}
			rs.close();

			// rearrange into map
			for (Printer printer : printers) {
				if (printer.getIp().trim().length() > 0) {
					PrintService.ipPrinterMap.put(printer.getIp().trim(), printer);
				}
			}
		} catch (Exception e) {
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

		// clean current category and menus from both screen and metrix if have---------------
		onSrcCategoryTgbMatrix.clear();
		onSrcMenuBtnMatrix.clear();

		// create new buttons and add onto the screen (no layout yet)------------
		GridLayout categoryButtonContainer = mRoot.findViewById(R.id.categoryContainer);
		categoryButtonContainer.setColumnCount(categoryColumn);
		categoryButtonContainer.setRowCount(categoryRow);
		categoryButtonContainer.removeAllViews();
		int globleCategoryIdxOfCurCategory = curCategoryPage * categoryQtPerPage;
		for (int r = 0; r < categoryRow; r++) {
			ArrayList<CategoryToggleButton> btnCategoryArry = new ArrayList<>();
			for (int c = 0; c < categoryColumn; c++) {

				CategoryToggleButton btnCategory = new CategoryToggleButton(globleCategoryIdxOfCurCategory + 1);
				btnCategory.setGravity(Gravity.CENTER);
				btnCategory.setPadding(8, 8, 8, 8);
				categoryButtonContainer.addView(btnCategory);
				btnCategory.setOnClickListener(this);
				btnCategoryArry.add(btnCategory);

				if (globleCategoryIdxOfCurCategory < categoryNameMetrix[0].length) {
					String defaultText = categoryNameMetrix[0][globleCategoryIdxOfCurCategory];
					int lang = CustOpts.custOps.getUserLang();
					if (lang > categoryNameMetrix.length - 1)
						lang = categoryNameMetrix.length - 1;
					String text = categoryNameMetrix[lang][globleCategoryIdxOfCurCategory];
					text = getTextForDsp(BarUtil.empty(text) ? defaultText : text);
					btnCategory.setText(text);
					btnCategory.setTextOn(text);
					btnCategory.setTextOff(text);

					btnCategory.setLang1Text(defaultText);
					if (tgbActiveCategory != null) {
						String categoryString = categoryNameMetrix[lang][globleCategoryIdxOfCurCategory];
						if (categoryString == null || categoryString.length() == 0) {
							categoryString = categoryNameMetrix[0][globleCategoryIdxOfCurCategory];
						}
						if (categoryString.equalsIgnoreCase(tgbActiveCategory.getText().toString())) {
							btnCategory.setSelected(true);
						}
					}
				}else{
					btnCategory.setText("\n");
					btnCategory.setTextOn("\n");
					btnCategory.setTextOff("\n");
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
		classifiedDishAry = new ArrayList<>();
		classifiedMenuIndex = 0;

		for (int i = 0; i < dishAry.length; i++) {
			if (dishAry[i].getCategory().equals(tgbActiveCategory.getLang1Text())) {

				classifiedDishNameMetrix[0][classifiedMenuIndex] = dishNameMetrix[0][i];
				classifiedDishNameMetrix[1][classifiedMenuIndex] = dishNameMetrix[1][i];
				classifiedDishNameMetrix[2][classifiedMenuIndex] = dishNameMetrix[2][i];

				classifiedDishAry.add(dishAry[i]);
				//make sure the display index are lined
				if (dishAry[i].getDisplayIdx() != classifiedMenuIndex + 1) {
					try {

						StringBuilder sql = new StringBuilder("UPDATE product SET INDEX = ").append(classifiedMenuIndex + 1)
								.append(" where _id = ").append(dishAry[i].getId());
						AikaPos.db.execSQL(sql.toString());
						BarOption.updateLastModifiyTime();
					} catch (Exception exp) {
						exp.printStackTrace();
					}
					dishAry[i].setDisplayIdx(classifiedMenuIndex + 1);
				}

				classifiedMenuIndex++;
			}
		}

		GridLayout dishButtonContainer = mRoot.findViewById(R.id.dishContainer);
		dishButtonContainer.setRowCount(menuRow);
		dishButtonContainer.setColumnCount(menuColumn);
		dishButtonContainer.removeAllViews();
		int globleMenuIdxOfCurCategory = curMenuPage * menuQTPerPage;    //the ones which will be on current page.
		for (int r = 0; r < menuRow; r++) {
			ArrayList<MenuButton> btnMenuArry = new ArrayList<>();
			for (int c = 0; c < menuColumn; c++) {
				MenuButton btnMenu = new MenuButton(globleMenuIdxOfCurCategory + 1);
				dishButtonContainer.addView(btnMenu);
				btnMenu.setOnClickListener(this);
				btnMenuArry.add(btnMenu);
				if (globleMenuIdxOfCurCategory < classifiedMenuIndex) {    //the last page could be not full page.
					Dish dish = classifiedDishAry.get(globleMenuIdxOfCurCategory);
					btnMenu.setDish(dish);
					int lang = CustOpts.custOps.getUserLang();
					if (lang > classifiedDishNameMetrix.length - 1)
						lang = classifiedDishNameMetrix.length - 1;
					String text = classifiedDishNameMetrix[lang][globleMenuIdxOfCurCategory];
					text = getTextForDsp(BarUtil.empty(text) ? classifiedDishNameMetrix[0][globleMenuIdxOfCurCategory] : text);
					if (BarOption.showPriceOnMenu()) {
						text += "\n";
						text += BarUtil.formatMoney(dish.getPrice() / 100.0);
					}
					btnMenu.setText(text);
					globleMenuIdxOfCurCategory++;
				}else{
					btnMenu.setText(getTextForDsp(BarOption.showPriceOnMenu() ? "\n\n" : "\n"));
				}
			}
			onSrcMenuBtnMatrix.add(btnMenuArry);
		}

		adjustPageArrowStatus(globleCategoryIdxOfCurCategory, globleMenuIdxOfCurCategory);

		mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {// 添加 OnGlobalLayoutListener
			@Override
			public void onGlobalLayout() {
				mRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);// 确保只调用一次
				reLayout();
			}
		});
	}

	private String getTextForDsp(String s) {
		if(!s.contains("\n")){
			s += "\n";
		}
		return s;
	}

	private void adjustPageArrowStatus(int globleCategoryIdxOfCurCategory, int globleMenuIdxOfCurCategory) {
		//for category panel page up
		if (globleCategoryIdxOfCurCategory <= categoryQtPerPage && curCategoryPage == 0) {        //the last displayed category is less than categoryQtPerPage, means
			btnPageUpCategory.setVisibility(GONE);                            // now it's on first page. no need to display up arrow.
		} else {
			btnPageUpCategory.setVisibility(VISIBLE);            //as long as not the first page, page up should display.
		}
		//for category panel page down
		int lastCateIdx = categoryRow * categoryColumn * (curCategoryPage + 1);
		if (lastCateIdx > categoryNameMetrix[0].length) {//if is the last page, and not full of last page, then should not display page down arrow.
			btnPageDownCategory.setVisibility(GONE);
		} else if (BarFrame.instance == null || (lastCateIdx == categoryNameMetrix[0].length && BarFrame.instance.curPanelIdx != 3)) {//if full of page, but is sales page, then hide.
			btnPageDownCategory.setVisibility(GONE);
		} else {    // if there's more than one page to display, and currently it's not last page then show it.
			btnPageDownCategory.setVisibility(VISIBLE);
		}

		//for menu panel page up
		if (globleMenuIdxOfCurCategory <= menuQTPerPage && curMenuPage == 0) {        //the last displayed menu is less than curMenuPerPage, means
			btnPageUpMenu.setVisibility(GONE);                    // now it's on first page. no need to display up arrow.
		} else {
			btnPageUpMenu.setVisibility(VISIBLE);            //as long as not the first page, page up should display.
		}
		//for menu panel page down
		int lastIdx = menuRow * menuColumn * (curMenuPage + 1);
		if (lastIdx > classifiedMenuIndex) {//if is the last page, and not full of last page, then should not display page down arrow.
			btnPageDownMenu.setVisibility(GONE);
		} else if (BarFrame.instance == null //ignore the case when app not finished start and the BarFrame is still null.
				|| (lastIdx == classifiedMenuIndex && BarFrame.instance.curPanelIdx != 3)) {//if it's full of page, but is not in setting page then hide!
			btnPageDownMenu.setVisibility(GONE);
		} else {    // if there's more than one page to display, and currently it's not last page then show it.
			btnPageDownMenu.setVisibility(VISIBLE);
		}
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
					initCategoryAndDishes();    //fill menu buttons with menus belong to this category.
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
					if (tgbActiveCategory == null || tgbActiveCategory.getText() == null || tgbActiveCategory.getText().length() < 1) {
						JOptionPane.showMessageDialog(BarFrame.consts.SetCatogoryFirst());
						return;
					}
					new DishDlg(menuButton.getDspIndex()).show(BarFrame.instance.getSupportFragmentManager(), "DishDialog");
				}
			} else { // if it's not empty
				if (getParent() == BarFrame.instance.panels[3]) {
					new DishDlg(menuButton.getDish()).show(BarFrame.instance.getSupportFragmentManager(), "DishDialog");
				} else if (getParent() == BarFrame.instance.panels[2]) {
					// add into selection.
					Dish dish = menuButton.getDish();
					addIntoBillPane(dish);
				} else {
					//do nothing, currently this case is when displayed in a dialog, which is popped up from table pane.
				}
			}
		}
		// arrow buttons-------------------------------------------------------------------------------------------------------
		else if (o instanceof ArrowButton) {
			if (o == btnPageUpCategory) {
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

	public Fragment getParent() {
		Fragment parentFragment = getParentFragment();
		if (parentFragment != null) {
			return parentFragment;
		}
		return BarFrame.instance.panels[2];
	}

	public void addIntoBillPane(Dish comboDish) {
		BillPanel billPanel = ((SalesPanel) BarFrame.instance.panels[2]).billPanel;
		if (!billPanel.notTooLateToChangeStatus()) {
			return;
		}
		//if it's counter mode and this is the first dish adding to the bill Panel, then update the openTime to now.
		//because the bill is created when last bill saved, and the open time generated at that time point does not make sense
		//while the time when the first dish added will make more sense (more importantly, it will make the SEV time in MEV machine more close
		//to the MEV time in mev machine, we believe it will make the mev log info looks more normal.)
		if (BarOption.isCounterMode() && billPanel.getOrderedDishAry().size() == 0) {
			//update the openTime.
			String timeStr = BarUtil.df.format(new Date());
			StringBuilder sql = new StringBuilder("update bill set openTime = '").append(timeStr)
					.append("' where _id = ").append(billPanel.getBillID());
			try {
				AikaPos.db.execSQL(sql.toString());
				//updating the open time of the table--------for counter mode, this step is necessary, because user might need to switch between the
				//opened tables(table is worked as bills in counter mode.)
				sql = new StringBuilder("update dining_Table set opentime = '").append(timeStr)
						.append("' WHERE name = '").append(BarFrame.instance.getCurTable()).append("'");
				AikaPos.db.execSQL(sql.toString());
				BarFrame.instance.valOpenTime.setText(timeStr);
			} catch (Exception e) {
				L.e("MenuPanel ", "Updating opentime when adding first dish: " + sql, e);
			}

		}
		if (comboDish.getPrice() < 0) {    //if it's comboDish.
			//match a rule, to get the relevant dishes
			CustomizedRule[] rules = BarFrame.menuPanel.getRules();
			for (CustomizedRule rule : rules) {
				if (rule.getRulename().equals(comboDish.getLanguage(0))
						|| rule.getRulename().equals(comboDish.getLanguage(1))
						|| rule.getRulename().equals(comboDish.getLanguage(2))) {
					//get the string from rule
					ArrayList<String> dishStrs = new ArrayList<>();
					String[] disIds = rule.getContent().split(",");
					for (String disId : disIds) {
						for (Dish dish : dishAry) {
							if (String.valueOf(dish.getId()).equals(disId)) {
								billPanel.addContentToList(dish);
								break;
							}
						}
					}
					break;
				}
			}
		} else {
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
