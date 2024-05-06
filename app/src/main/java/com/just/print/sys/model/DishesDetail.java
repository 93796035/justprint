package com.just.print.sys.model;

import com.just.print.db.bean.Dish;

public class DishesDetail {
    private Dish dish;
    private int dishNum;

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getDishNum() {
        return dishNum;
    }

    public void setDishNum(int dishNum) {
        this.dishNum = dishNum;
    }


}
