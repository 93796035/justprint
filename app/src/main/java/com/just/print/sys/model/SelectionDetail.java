package com.just.print.sys.model;

import com.just.print.db.bean.Dish;
import com.just.print.db.bean.Mark;

import java.util.ArrayList;
import java.util.List;

public class SelectionDetail {

	private Dish dish;
	private float dishNum;
	private String pIP;
	private List<Mark> markList;//don't know why, but if I initialize it here, it will still be null when getting it....

	public List<Mark> getMarkList() {
		if(markList == null){
			markList = new ArrayList<Mark>();
		}
		return markList;
	}

	public void setMarkList(List<Mark> markList) {
		this.markList = markList;
	}

	public Dish getDish() {
		return dish;
	}

	public void setDish(Dish dish) {
		this.dish = dish;
	}

	public float getDishNum() {
		return dishNum;
	}

	public void setDishNum(float dishNum) {
		this.dishNum = dishNum;
	}

	public String getpIP() {
		return pIP;
	}

	public void setpIP(String pIP) {
		this.pIP = pIP;
	}

	public SelectionDetail clone(){
		SelectionDetail selectionDetail = new SelectionDetail();
		selectionDetail.setDishNum(dishNum);
		selectionDetail.setDish(dish);
		selectionDetail.setMarkList(this.markList);
		selectionDetail.setpIP(this.pIP);
		return selectionDetail;
	}
}
