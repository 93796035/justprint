package com.just.print.sys.server;

import com.just.print.sys.model.SelectionDetail;

import java.util.ArrayList;
import java.util.List;

public class CustomerSelection {

    private static CustomerSelection instance;
    private String tableName;   //we can not keep an Mark object here, because some table not exists, it's a tempral name.
                                //and it has no relavant table(mark) in db...so we can not use id to indicate it in selection.
    private List<SelectionDetail> selectedDishes;
    private List<String> mkList;

    public static CustomerSelection getInstance() {
        if (instance == null) {
            instance = new CustomerSelection();
        }
        return instance;
    }

    private CustomerSelection() {
        tableName ="";
        selectedDishes = new ArrayList<SelectionDetail>();
    }

    public int addSelectedDish(SelectionDetail mn) {
        selectedDishes.add(mn);
        return 0;
    }

    public int deleteSelectedDish(SelectionDetail i) {
        selectedDishes.remove(i);
        return 0;
    }

    public int deleteSelectedDish(int i) {
        selectedDishes.remove(i);
        return 0;
    }

    public int clearMenu() {
        selectedDishes.clear();
        return 0;
    }

    public List<SelectionDetail> getSelectedDishes() {
        return selectedDishes;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
