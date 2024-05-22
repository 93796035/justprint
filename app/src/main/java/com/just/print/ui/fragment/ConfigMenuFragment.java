package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.Options;
import com.just.print.app.AikaPos;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.just.print.db.dao.CategoryDao;
import com.just.print.db.dao.DishDao;
import com.just.print.db.dao.M2M_MenuPrintDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.db.expand.State;
import com.just.print.ui.holder.ConfigAddMarkViewHolder;
import com.just.print.ui.holder.ConfigAddPrintViewHolder;
import com.just.print.ui.holder.ConfigCategoryViewHolder;
import com.just.print.ui.holder.ConfigMenuViewHolder;
import com.stupid.method.adapter.IXOnItemClickListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.DialogInterface.OnClickListener;

public class ConfigMenuFragment extends BaseFragment implements View.OnFocusChangeListener {//} implements IXOnItemLongClickListener {

    private static Dish modifyingMenu = null;
    public static int curCategory = 0;

    private Category modifyingCategory = null;

    XAdapter2<Category> categoryXAdapter;
    
    private String previouseCode = "  "; //give 3 empty space to avoid excption, because latter will use subString(1).
    List<Printer> addedPrints;
    XAdapter2<Printer> addPrintsAdapter;

    List<Mark> addedMarks;
    XAdapter2<Mark> addMarksAdapter;

    @XViewByID(R.id.lvCategory)
    ListView lvCategory;

    private Category mCategory;

    @XViewByID(R.id.tvMenuTitle)
    private TextView tvMenuTitle;

    @XViewByID(R.id.lvMenu)
    private ListView lvMenu;

    @XViewByID(R.id.addPrintListView)
    ListView addPrintListView;

    @XViewByID(R.id.addMarkListView)
    ListView addMarkListView;

    @XViewByID(R.id.showMarkSelection)
    CheckBox showMarkSelection;

    @XViewByID(R.id.curCategoryName)
    TextView curCategoryName;

    @XViewByID(R.id.etMenuID)
    private EditText etMenuId;

    @XViewByID(R.id.etMenu)
    private EditText etMenuName;

    DishDao menuDao;
    private boolean needAutoFill;
    private XAdapter2<Dish> menuXAdapter;
    private boolean originalSelectStatus;
    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();

        categoryXAdapter = new XAdapter2<Category>(getContext(), ConfigCategoryViewHolder.class);
        categoryXAdapter.setClickItemListener(categoryAdapterClick);
        //categoryXAdapter.setLongClickItemListener(this);
        lvCategory.setAdapter(categoryXAdapter);

        menuXAdapter = new XAdapter2<Dish>(getContext(), ConfigMenuViewHolder.class);
        menuXAdapter.setClickItemListener(categoryAdapterClick);
        lvMenu.setAdapter(menuXAdapter);

        etMenuId.setOnFocusChangeListener(this);
        etMenuName.setOnFocusChangeListener(this);

        menuDao = AikaPos.daoSession.getDishDao();

        loadCategory();
        loadMenu();
        if(categoryXAdapter.size() > 0) {
            curCategoryName.setText(categoryXAdapter.get(0).getName1());
        }
        curCategoryName.setTextColor(Color.WHITE);
        curCategoryName.setBackgroundColor(Options.getThemColor());
    }

    IXOnItemClickListener categoryAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            switch (view.getId()) {
                case android.R.id.content:
                    curCategory = i;
                    curCategoryName.setText(categoryXAdapter.get(i).getName1());
                    onChangeCategory(categoryXAdapter.get(i));
                    break;

                case R.id.modifyCategory:
                    //show up hte modification dialog.
                    modifyingCategory = categoryXAdapter.get(i);
                    showCategory();
                    TextView tvCategory = (TextView) findViewById(R.id.tvCategory);
                    tvCategory.setText(modifyingCategory.getName1());
                    TextView displayIdx = (TextView) findViewById(R.id.displayIdx);
                    displayIdx.setText(String.valueOf(modifyingCategory.getDisplayIdx()));

                    break;
                case R.id.delCategory:
                    Category cag = categoryXAdapter.get(i);
                    long cid = cag.getId();
//                    List<Dish> mList = menuDao._queryCategory_DishList(cid);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure to delete \"" + cag.getName1() + "\"?")
                            .setTitle("Notice");
//                    builder.setNegativeButton("Delete", new CategoryDeleteListener(mList, cag));
                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();

                    break;
                case R.id.modifyMenu:
                    findViewById(R.id.categoryLayout).setVisibility(View.GONE);
                    findViewById(R.id.menuLayout).setVisibility(View.VISIBLE);

                    modifyingMenu = menuXAdapter.get(i);
                    ((EditText)findViewById(R.id.etMenuID)).setText(menuXAdapter.get(i).getId().toString());
                    ((EditText)findViewById(R.id.etMenu)).setText(menuXAdapter.get(i).getName1());
                    Float price = menuXAdapter.get(i).getPrice();
                    ((EditText)findViewById(R.id.price)).setText(price == null ? "" : price.toString());

                    //preparing printers
                    addedPrints = new ArrayList<Printer>();
                    for(M2M_MenuPrint m2m_p : menuXAdapter.get(i).getM2M_MenuPrintList()){
                        addedPrints.add(m2m_p.getPrint());
                    }

                    addPrintsAdapter = new XAdapter2<Printer>(getContext(), addedPrints, ConfigAddPrintViewHolder.class);
                    addPrintListView.setAdapter(addPrintsAdapter);
                    addPrintsAdapter.setClickItemListener(new IXOnItemClickListener() {
                        @Override
                        public void onClickItem(View view, int i) {
                            addedPrints.remove(i);
                            addPrintsAdapter.notifyDataSetChanged();
                        }
                    });

//                    //preparing marks
//                    addedMarks = new ArrayList<Mark>();
//                    for(M2M_MenuMark m2m_p : menuXAdapter.get(i).getM2M_MenuMarkList()){
//                        addedMarks.add(m2m_p.getMark());
//                    }

                    addMarksAdapter = new XAdapter2<Mark>(getContext(), addedMarks, ConfigAddMarkViewHolder.class);
                    addMarkListView.setAdapter(addMarksAdapter);
                    addMarksAdapter.setClickItemListener(new IXOnItemClickListener() {
                        @Override
                        public void onClickItem(View view, int i) {
                            addedMarks.remove(i);
                            addMarksAdapter.notifyDataSetChanged();
                        }
                    });

                    //initial autoPopup check box status
                    originalSelectStatus = Options.getForcePopUpList().contains(menuXAdapter.get(i).getId().toString());
                    showMarkSelection.setChecked(originalSelectStatus);
                    break;
                case R.id.delMenu:
                    //menuXAdapter.get(i).logicDelete();
                    /*
                    M2M_MenuPrintDao m2mDao = getDaoMaster().newSession().getM2M_MenuPrintDao();
                    List<M2M_MenuPrint> m2mList = m2mDao._queryMenu_M2M_MenuPrintList(menuXAdapter.get(i).getId());
                    for(M2M_MenuPrint tmp:m2mList){
                        m2mDao.delete(tmp);
                    }
                    */
                    /*
                    List<M2M_MenuPrint> m2mlist=
                        menuXAdapter.get(i).getM2M_MenuPrintList();
                    for (M2M_MenuPrint m2m:m2mlist){
                        m2m.delete();
                    }
                    */

                    new AlertDialog.Builder(getActivity())
                            .setMessage("Are you sure to delete \"" + menuXAdapter.get(i).getName1() + "\"?")
                            .setTitle("Notice")
                            .setNegativeButton("Delete", new MenuDeleteListener(menuXAdapter.get(i)))
                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();

                    break;
            }
        }
    };

    private class CategoryDeleteListener implements DialogInterface.OnClickListener{
        List<Dish> menus;
        Category category;
        public CategoryDeleteListener(List<Dish> menus, Category category){
            this.menus = menus;
            this.category = category;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            for(Dish menu:menus) {
                //delete menu-printer maps
                List<M2M_MenuPrint> m2mList = menu.getM2M_MenuPrintList();
                for(M2M_MenuPrint m2m:m2mList) {
                    m2m.delete();
                }
                // delete menu.
                menu.delete();
            }
            category.logicDelete();
            loadCategory();
        }
    }

    private class MenuDeleteListener implements DialogInterface.OnClickListener{
        Dish menu;
        public MenuDeleteListener(Dish menu){
            this.menu = menu;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            List<M2M_MenuPrint> m2mList = menu.getM2M_MenuPrintList();
            for(M2M_MenuPrint m2m : m2mList) {
                m2m.delete();
            }

            menuDao.delete(menu);
            loadMenu();
        }
    }

    private void onChangeCategory(Category category) {
        mCategory = category;
        if (category != null) {
            tvMenuTitle.setText(category.getName1());
            loadMenu();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.config_menu_fragment;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(hasFocus) {
            String menuId = etMenuId.getText().toString();
            String menuName = etMenuName.getText().toString();
            needAutoFill = menuName.length() == 0 //menuName must be empty
                    && (menuId.length() == 0    //menuId must be empty or num+"."
                        || (menuId.endsWith(".")
                            && Integer.valueOf(menuId.substring(0, menuId.length() - 1)) > 0));
        }else if(needAutoFill){  //when lost focus, and the flag already set.
            needAutoFill = false;   //first reset flag.
            if (view == etMenuId){
                String menuId = etMenuId.getText().toString();
                int p = menuId.indexOf(".");
                if(p > 0 && Options.isCodeNumberConsidered()){
                    String intPart = menuId.substring(0, p);
                    if(Integer.valueOf(intPart) > 0){
                        etMenuName.setText(menuId.substring(p + 1));
                    }else {
                        etMenuName.setText(menuId);
                    }
                }else{
                    etMenuName.setText(menuId);
                }

            }else if(view == etMenuName){
                String menuName = etMenuName.getText().toString();
                String menuId = etMenuId.getText().toString();
                etMenuId.setText(menuId + menuName);
            }
        }
    }

    @XClick({R.id.showCategory})
    private void showCategory() {
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        findViewById(R.id.categoryLayout).setVisibility(View.VISIBLE);
        ((EditText)findViewById(R.id.tvCategory)).setText("");
        ((EditText)findViewById(R.id.displayIdx)).setText("");
    }

    @XClick({R.id.addMenuItem})
    private void addMenuItem() {
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
        findViewById(R.id.menuLayout).setVisibility(View.VISIBLE);
        modifyingMenu = null; //make sure the flag reset, so then ok is cliced, we know it's not update.
        etMenuId.setText(getDefaultCode());
        etMenuName.setText("");

        //printers
        addedPrints = addedPrints == null ? new ArrayList<Printer>(1) : addedPrints;
        addPrintsAdapter = new XAdapter2<Printer>(getContext(), addedPrints, ConfigAddPrintViewHolder.class);
        addPrintListView.setAdapter(addPrintsAdapter);
        addPrintsAdapter.setClickItemListener(new IXOnItemClickListener() {
            @Override
            public void onClickItem(View view, int i) {
                addedPrints.remove(i);
                addPrintsAdapter.notifyDataSetChanged();
            }
        });
        //marks
        addedMarks = addedMarks == null ? new ArrayList<Mark>(1) : addedMarks;
        addMarksAdapter = new XAdapter2<Mark>(getContext(), addedMarks, ConfigAddMarkViewHolder.class);
        addMarkListView.setAdapter(addMarksAdapter);
        addMarksAdapter.setClickItemListener(new IXOnItemClickListener() {
            @Override
            public void onClickItem(View view, int i) {
                addedMarks.remove(i);
                addMarksAdapter.notifyDataSetChanged();
            }
        });
        //set the ariginal status of the check box, while, be noticed that the ui might show selected
        //status, because we want to keep the check box same with previous item.
        originalSelectStatus = false;
    }

    private String getDefaultCode() {
        String defautlCode = "";
        if(!("1".equals(Options.isAddingDefaultCode()) || "true".equals(Options.isAddingDefaultCode()))){
            return defautlCode;
        }
        int endCode = parseInt(previouseCode);

        if(endCode >= 0){
            defautlCode = String.valueOf(endCode + 1);
        }else {
            int idx = previouseCode.indexOf(".");
            if (idx < 0) {
                idx = 0;
                if(previouseCode.length() > 0 && parseInt(previouseCode.substring(1)) <= 0) {
                    idx = 1;
                }
            }
            String preFix = previouseCode.substring(0, idx + 1);
            defautlCode = previouseCode.substring(idx + 1);
            endCode = parseInt(defautlCode);
            if (endCode > 0) {
               defautlCode = preFix + String.valueOf(endCode + 1);
            }
        }
        return defautlCode;
    }

    private int parseInt(String code ){
        try{
            return Integer.parseInt(code);
        }catch (Exception e){
            return -1;
        }
    }

    @XClick({R.id.addMenu})
    private void addOrModifyMenu(@XGetValueByView(fromId = R.id.etMenuID) TextView mid,
                                 @XGetValueByView(fromId = R.id.etMenu) TextView mname,
                                 @XGetValueByView(fromId = R.id.price) TextView mprice,
                                 @XGetValueByView(fromId = R.id.selectPrint) TextView print,
                                 @XGetValueByView(fromId = R.id.selectMark) TextView mark) {
        //valid category
        if (mCategory == null) {
            showToast("Please choose a category");
            return;
        }
        //valid price
        if (mid.getText().length() == 0 || mname.getText().length() == 0 || mprice.getText().length() == 0 ) {
            if(mid.getText().length() == 0) {
                showToast("Please input code");
            }else if(mname.getText().length() == 0){
                showToast("Please input name");
            }else if(mprice.getText().length() == 0){
                showToast("Please input price");
            }
            return;
        }
        Float price = 0.0f;
        try{
            price =Float.valueOf(String.valueOf(mprice.getText()));
        } catch(Exception e){
            showToast("Please input valid price. e.g. 15.99");
            return;
        }
        //valid printer
        if (addedPrints == null || addedPrints.size() == 0) {
            if(!Options.isMode2()) {    //allow dish without printer in mode 2
                showToast("Please select printer");
                return;
            }
        }
        //valid menuId
        Dish menu = menuDao.load(Long.valueOf(mid.getText().toString()));     //to filter out "is adding" and "menucode is ocupied" case.
        //modify case also need to check, in case that id is changed and new id duplicated with ohter menu.
        if(menu != null && menu.getStatus() == State.OPENNED) {
            if(modifyingMenu == null || !menu.getId().equals(modifyingMenu.getId())){ //creating or it's modifying
                 showToast("This menu name already exist, please choose a different name.");
                 return;
            }
        }
        /*
        if (menu != null && menu.getState() == State.delete){
            menu.setID(mid.getText().toString().trim());
            menu.setState(State.def);
            menu.setName1(mname.getText().toString().trim());
            menu.setCategoryId(mCategory.getId());
            menu.updateAndUpgrade();
        }
        */

        if(modifyingMenu == null){   //adding case
            menu = new Dish();
            menu.setId(Long.valueOf(mid.getText().toString()));
            menu.setStatus(State.OPENNED);
            menu.setName1(mname.getText().toString().trim());
            menu.setCategory(mCategory.getName1());
            menu.setPrice(price);
            menuDao.insert(menu);
            previouseCode = menu.getId().toString();
        }else{              //updating....now it's allow duplicate name case..
            menuDao.delete(modifyingMenu);
            modifyingMenu.setId(Long.valueOf(mid.getText().toString()));
            modifyingMenu.setStatus(State.OPENNED);
            modifyingMenu.setName1(mname.getText().toString().trim());
            modifyingMenu.setCategory(mCategory.getName1());
            modifyingMenu.setPrice(price);
            menuDao.insert(modifyingMenu);
            menu = modifyingMenu;
        }


//        menu.updateAndUpgrade();    //is this useful or not?

        //add printer link
        M2M_MenuPrintDao m2mDao = AikaPos.daoSession.getM2M_MenuPrintDao();
        if(modifyingMenu != null) {
            for (M2M_MenuPrint p : modifyingMenu.getM2M_MenuPrintList()) {
                m2mDao.delete(p);   //remove a record from db.
            }
        }
        for (Printer p : addedPrints) { //re-add record into db
            M2M_MenuPrint m2m = new M2M_MenuPrint();
            m2m.setDishId(menu.getId());
            m2m.setPrint(p);
            m2mDao.insertOrReplace(m2m);
        }

        //process the popup selection
        if(originalSelectStatus != showMarkSelection.isChecked()){
            if(showMarkSelection.isChecked()){
                Options.putCustomData(Options.forcePopUpMenuList, Options.getForcePopUpList() + ";" + menu.getId());
            }else{
                String existingStr = Options.getForcePopUpList();
                int p = existingStr.indexOf(menu.getId().toString());
                String newValue = existingStr.substring(0, p) + existingStr.substring(p + menu.getId().toString().length());
                newValue = newValue.replace(";;", ";");
                if(newValue.startsWith(";")){   //if it's the first, then there will be a ";" left in the position 0.
                    newValue = newValue.substring(1, newValue.length());
                }
                Options.putCustomData(Options.forcePopUpMenuList, newValue);
            }
        }

        Options.updataeLastModifyTime(null);
        modifyingMenu = null;
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        loadMenu();
    }

    @XClick({R.id.addCategory})
    private void addCategory(@XGetValueByView(fromId = R.id.tvCategory) TextView cname,
                             @XGetValueByView(fromId = R.id.displayIdx) TextView displayIdx) {
        if (cname.getText().length() == 0) {
            showToast("Please input category");
            return;
        }

        String idxString = displayIdx.getText().toString();
        Integer idx = 0;
        if(idxString.length() > 0) {
            try {
                idx = Integer.valueOf(idxString);
            } catch (Exception e) {
                showToast("Please input a valid number to indicate the display order");
                return;
            }
        }

        Category cat = null;
        if (modifyingCategory == null) {
            cat = new Category();
        }else {
            cat = modifyingCategory;
        }
        cat.setName1(cname.getText().toString());
        cat.setDisplayIdx(idx);

        cat.setStatus(State.OPENNED);
        AikaPos.daoSession.getCategoryDao().insertOrReplace(cat);
        cat.updateAndUpgrade();
        Options.updataeLastModifyTime(null);
        loadCategory();
        cname.setText("");
        modifyingCategory = null;
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
    }

    @XClick({R.id.selectPrint})
    private void selectPrint(final TextView view) {
        final List<Printer> data = DaoExpand.queryNotDeletedAll(AikaPos.daoSession.getPrinterDao());
        data.removeAll(addedPrints);
        if (data.size() == 0) {
            showToast("Please add printer first");
            return;
        }
        if (data.size() == 0) {
            showToast("No printer to select");
            return;
        }
        final CharSequence[] items = new CharSequence[data.size()];
        for (int i = 0, s = data.size(); i < s; i++) {
            Printer p = data.get(i);
            items[i] = p.getName() + "_" + p.getIp();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Please choose a printer");
        builder.setSingleChoiceItems(items, -1, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addedPrints.add(data.get(which));
                addPrintsAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @XClick({R.id.selectMark})
    private void selectMark(final TextView view) {
        List<Mark> data = DaoExpand.queryAllMarks(AikaPos.daoSession.getMarkDao());
        data.removeAll(addedMarks);
        if (data.size() == 0) {
            showToast("No marks to select");
            return;
        }
        final List<Mark> newData = OrderIdentifierFragment.sortMarks(data);
        final CharSequence[] items = new CharSequence[newData.size()];
        for (int i = 0, s = newData.size(); i < s; i++) {
            Mark p = newData.get(i);
            items[i] = p.getName1() + "_" + p.getId();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if("1".equals(Options.getCustomData(Options.isSingleMark)) || "true".equals(Options.getCustomData(Options.isSingleMark))){
            builder.setTitle("Please choose a mark");
            builder.setSingleChoiceItems(items, -1, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addedMarks.add(newData.get(which));
                    addMarksAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
        }else{
            builder.setTitle("Please choose marks");
            builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isSelected) {
                    if(isSelected) {
                        addedMarks.add(newData.get(which));
                    }else{
                        addedMarks.remove(newData.get(which));
                    }
                    addMarksAdapter.notifyDataSetChanged();
                }
            });
        }
        builder.show();
    }

    @XClick({R.id.cancelAddCat, R.id.cancelAddMenu})
    private void cancel() {
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
    }

    private void loadCategory() {

        List<Category> list = DaoExpand.queryAllNotDeleted(AikaPos.daoSession.getCategoryDao()).orderAsc(CategoryDao.Properties.DisplayIdx).list();
        categoryXAdapter.setData(list);
        categoryXAdapter.notifyDataSetChanged();

        menuXAdapter.clear();
        if (categoryXAdapter.size() > 0)
            onChangeCategory(categoryXAdapter.get(0));
        else
            menuXAdapter.notifyDataSetChanged();
    }

    private void loadMenu() {
        if (mCategory != null) {
            List<Dish> menuList = DaoExpand.queryMenuByCategory(AikaPos.daoSession.getDishDao(), mCategory);
            Collections.sort(menuList, OrderCategoryFragment.comparator);
            menuXAdapter.setData(menuList);
            menuXAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public String toString() {
        return "Menu Management";
    }

//    @Override
//    public boolean onLongClickItem(View view, int i) {
//        modifyingCategory = categoryXAdapter.get(i);
//        showCategory();
//        TextView tvCategory = (TextView) findViewById(R.id.tvCategory);
//        tvCategory.setText(modifyingCategory.getName1());
//
//        return true;
//    }
}
