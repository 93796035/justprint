package org.cas.client.platform.bar.action;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.cas.client.platform.bar.model.Dish;
import org.cas.client.platform.bar.pannel.BarFrame;
import org.cas.client.platform.bar.pannel.BillPanel;
import org.cas.client.platform.bar.pannel.SalesPanel;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.bar.uibeans.SamActionListener;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.L;
import org.cas.client.platform.pimmodel.PIMDBModel;

public class Cmd_DiscountCoupon implements SamActionListener {

	private static Cmd_DiscountCoupon instance;
	private Cmd_DiscountCoupon() {}
	public static Cmd_DiscountCoupon getInstance() {
		if(instance == null)
			instance = new Cmd_DiscountCoupon();
		return instance;
	}
	
	private ISButton sourceBtn;
	
	public ISButton getSourceBtn() {
		return sourceBtn;
	}
	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SalesPanel salesPanel = (SalesPanel)BarFrame.instance.panels[2];
		BillPanel billPanel = salesPanel.billPanel;
		
		String couponCode  = JOptionPane.showInputDialog(null, BarFrame.consts.couponCode());
		if(couponCode == null || couponCode.length() == 0)
			return;
		
		StringBuilder sql = new StringBuilder("SELECT * from hardware where category = 1 and name = '").append(couponCode)
				.append("' and (status is null or status = 0)");
		try {
			ResultSet rs = PIMDBModel.getReadOnlyStatement().executeQuery(sql.toString());
			rs.afterLast();
            rs.relative(-1);
            int tmpPos = rs.getRow();
            if(tmpPos == 0) {	//if there's no this coupon number in database, then warning and return.
            	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.InvalidCoupon());
            	return;
            }else {			//if the number is OK.
            	//get out every field of first matching record.
            	rs.beforeFirst();
                tmpPos = 0;
                rs.next();
                int id = rs.getInt("id");
                int category = rs.getInt("style");
                String productCode = rs.getString("IP");
                int value = rs.getInt("langType");
                
                //check if the coupon can be applied on current bill
                int langIdx = CustOpts.custOps.getUserLang();
                ArrayList<String> nameAry = getAppliableDishNames(productCode, langIdx); //if nameAry is null, mean apply to whole bill.
                ArrayList<Dish> dishesOnBill = billPanel.getOrderedDishAry();
                ArrayList<Dish> matchedDishesOnBill = getMatchedItem(dishesOnBill, nameAry, langIdx);
                //@NOTE: if nameAry and matchedDishesOnBill are null, mean apply to whole bill.
                if(matchedDishesOnBill != null && matchedDishesOnBill.size() == 0) {
                	JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.couponNotApplyToBill());
                	return;
                }
                
                //if matchedDishesOnBill is null, then apply the coupon to the whole bill.
                if(matchedDishesOnBill == null) {
                    if(category == 0) {//mean the price is absolute price, not percentage.
                    	int total = Math.round(Float.valueOf(salesPanel.billPanel.valTotlePrice.getText()) * 100);
                    	value = value > total ?  total : value;
                    	salesPanel.discountBill(value);
                    }else {
                    	value = Math.round((salesPanel.billPanel.subTotal + salesPanel.billPanel.discount) * (Float.valueOf(value/100f) / 100f));
                    	salesPanel.discountBill(value);
                    }
                    //recalculate the left
                    
                    //if the total is 0, then close cur bill.
                    if("0.00".equals(salesPanel.billPanel.valTotlePrice.getText())) {
                    	PrintService.printReceipt(salesPanel.billPanel, false, true, true);
                    	BarFrame.instance.completeCurrentBill();
                    	BarFrame.instance.actionAfterBillClosed(); 
                    }
                } else {//apply the coupon only to the dish item.
                	//find out the most expensive dish
                	Dish mostExpensiveDish = null;
                	for (Dish dish : matchedDishesOnBill) {
                		mostExpensiveDish = mostExpensiveDish.getPrice() < dish.getPrice() ? dish : mostExpensiveDish;
					}
                	//calculate coupon value:
                	if(category == 0) {//mean the price is absolute price, not persentage.
                		value = value > mostExpensiveDish.getPrice() ? mostExpensiveDish.getPrice() : value;
                    }else {
                    	value = Math.round(mostExpensiveDish.getPrice() * (Float.valueOf(value) / 100f));
                    }
                	
                	salesPanel.discountADish(value, mostExpensiveDish);
                }
                
            	//update the status of the coupon.
            	sql = new StringBuilder("update hardware set status = 1 where id = ").append(id);
            	PIMDBModel.getStatement().executeUpdate(sql.toString());
            }
		}catch(Exception exp) {
			L.e("Redeem Coupon", "exception happend when redeem coupon: " + sql, exp);
		}
	}
	
	private ArrayList<String> getAppliableDishNames(String productCode, int langIdx) {
    	ArrayList<String> appliableDishes = new ArrayList<String>();
    	if(productCode == null || productCode.trim().length() == 0) {
    		return null;
    	}
    	//if it's a category
    	String[] codes = productCode.split(",");
        Dish[] dishAry = BarFrame.menuPanel.getDishAry();
    	for(int m = 0; m < codes.length; m++) {
			if(codes[m].length() == 0) {
				continue;
			}
			boolean matched = false;
	    	for(int i = 0; i < BarFrame.menuPanel.categoryNameMetrix[0].length; i++) {
	    		//check 3 languages
    			if(codes[m].trim().equalsIgnoreCase(BarFrame.menuPanel.categoryNameMetrix[0][i].trim())
    					|| codes[m].trim().equalsIgnoreCase(BarFrame.menuPanel.categoryNameMetrix[1][i].trim())
    					|| codes[m].trim().equalsIgnoreCase(BarFrame.menuPanel.categoryNameMetrix[2][i].trim())) {
    				//add all relavant dishes
    		        for (int j = 0; j < dishAry.length; j++) {
    					if(dishAry[j].getCATEGORY().trim().equals(BarFrame.menuPanel.categoryNameMetrix[0][i].trim())) {
    						appliableDishes.add(dishAry[j].getLanguage(langIdx));
    					}
    		        }
    				matched = true;
    				break;
    			}
	    	}
	    	
	    	//didn't match any category, then it's a menu name, add the lang0 into the list directly.
	    	if(!matched) {
	    		for (int j = 0; j < dishAry.length; j++) {
					if(codes[m].trim().equalsIgnoreCase(dishAry[j].getLanguage(0).trim())
						|| codes[m].trim().equalsIgnoreCase(dishAry[j].getLanguage(1).trim())
						|| codes[m].trim().equalsIgnoreCase(dishAry[j].getLanguage(2).trim())){
						appliableDishes.add(dishAry[j].getLanguage(langIdx));
					}
		        }
	    	}
    	}
		return appliableDishes;
	}
	
    //find out which dishes in current bill can be apply on the coupon.
    private ArrayList<Dish> getMatchedItem(ArrayList<Dish> dishesOnBill, ArrayList<String> nameAry, int langIdx) {
    	if(nameAry == null) {
    		return null;
    	}
    	ArrayList<Dish> appliableDishes = new ArrayList<Dish>();
    	for (Dish dish : dishesOnBill) {
			if(nameAry.contains(dish.getLanguage(langIdx))) {
				appliableDishes.add(dish);
			}
		}
		return appliableDishes;
	}
}
