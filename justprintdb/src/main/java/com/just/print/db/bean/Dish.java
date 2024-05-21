package com.just.print.db.bean;

import android.database.DatabaseUtils;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.db.dao.DaoSession;
import com.just.print.db.dao.DishDao;
import com.just.print.db.dao.M2M_MenuPrintDao;
import com.just.print.db.dao.M2M_SaleRecordMarkDao;
import com.just.print.ui.fragment.LoginDlg;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.casutil.L;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.DaoException;
// KEEP INCLUDES END
/**
 * Entity mapped to table DISH.
 */
public class Dish {

    private Long id;
    private String name1;
    private String name2;
    private String name3;
    private Float price;
    private Integer gst;
    private Integer qst;
    private Integer size;
    private String printerIds;
    private Integer displayIdx;
    private Boolean pricePomp;
    private Boolean menuPomp;
    private Boolean ModifyPomp;
    private Long categoryId;
    private Integer status;
    private Integer version;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient DishDao myDao;

    private List<M2M_MenuPrint> m2M_MenuPrintList;
    private List<M2M_SaleRecordMark> m2M_SaleRecordMarkList;

    // KEEP FIELDS - put your custom fields here
    public Dish(int id, String category, String name1, String name2, String name3, String printerStr) {
        setId((long)id);
        setCategoryId(Long.valueOf(category));
        setLanguage(0, name1);
        setLanguage(1, name2);
        setLanguage(2, name3);
        setPrinterIds(printerStr);
    }
    // KEEP FIELDS END

    public Dish() {
    }

    public Dish(Long id) {
        this.id = id;
    }

    public Dish(Long id, String name1, String name2, String name3, Float price, Integer gst, Integer qst, Integer size, String printerIds, Integer displayIdx, Boolean pricePomp, Boolean menuPomp, Boolean ModifyPomp, Long categoryId, Integer status, Integer version) {
        this.id = id;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.price = price;
        this.gst = gst;
        this.qst = qst;
        this.size = size;
        this.printerIds = printerIds;
        this.displayIdx = displayIdx;
        this.pricePomp = pricePomp;
        this.menuPomp = menuPomp;
        this.ModifyPomp = ModifyPomp;
        this.categoryId = categoryId;
        this.status = status;
        this.version = version;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDishDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getGst() {
        return gst;
    }

    public void setGst(Integer gst) {
        this.gst = gst;
    }

    public Integer getQst() {
        return qst;
    }

    public void setQst(Integer qst) {
        this.qst = qst;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getPrinterIds() {
        return printerIds;
    }

    public void setPrinterIds(String printerIds) {
        this.printerIds = printerIds;
    }

    public Integer getDisplayIdx() {
        return displayIdx;
    }

    public void setDisplayIdx(Integer displayIdx) {
        this.displayIdx = displayIdx;
    }

    public Boolean getPricePomp() {
        return pricePomp;
    }

    public void setPricePomp(Boolean pricePomp) {
        this.pricePomp = pricePomp;
    }

    public Boolean getMenuPomp() {
        return menuPomp;
    }

    public void setMenuPomp(Boolean menuPomp) {
        this.menuPomp = menuPomp;
    }

    public Boolean getModifyPomp() {
        return ModifyPomp;
    }

    public void setModifyPomp(Boolean ModifyPomp) {
        this.ModifyPomp = ModifyPomp;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<M2M_MenuPrint> getM2M_MenuPrintList() {
        if (m2M_MenuPrintList == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            M2M_MenuPrintDao targetDao = daoSession.getM2M_MenuPrintDao();
            List<M2M_MenuPrint> m2M_MenuPrintListNew = targetDao._queryDish_M2M_MenuPrintList(id);
            synchronized (this) {
                if(m2M_MenuPrintList == null) {
                    m2M_MenuPrintList = m2M_MenuPrintListNew;
                }
            }
        }
        return m2M_MenuPrintList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetM2M_MenuPrintList() {
        m2M_MenuPrintList = null;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<M2M_SaleRecordMark> getM2M_SaleRecordMarkList() {
        if (m2M_SaleRecordMarkList == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            M2M_SaleRecordMarkDao targetDao = daoSession.getM2M_SaleRecordMarkDao();
            List<M2M_SaleRecordMark> m2M_SaleRecordMarkListNew = targetDao._queryDish_M2M_SaleRecordMarkList(id);
            synchronized (this) {
                if(m2M_SaleRecordMarkList == null) {
                    m2M_SaleRecordMarkList = m2M_SaleRecordMarkListNew;
                }
            }
        }
        return m2M_SaleRecordMarkList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetM2M_SaleRecordMarkList() {
        m2M_SaleRecordMarkList = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    public void changeOutputStatusInDB(int status) {
        if(getOutputID() < 0) {
            return;
        }
        setStatus(status);

        StringBuilder sql = new StringBuilder("update output set deleted = ").append(status)
                .append(", arrearage = ").append((int)(System.currentTimeMillis() / 1000))
                .append(" where id = ").append(getOutputID());
        try {
            AikaPos.db.execSQL(sql.toString());
        } catch (Exception exp) {
            L.e("Dish", "exception when exec sql："+sql, exp);
        }
    }
    public int getOutputID() {
        return outputID;
    }

    public void setOutputID(int outputID) {
        this.outputID = outputID;
    }

    public static int createOutput(Dish dish, String billIndex, String createTime) {
        return createSplitedOutput(dish, billIndex, 1, createTime);
    }

    public static int createSplitedOutput(Dish dish, String billIndex, float splitRate, String createdTime) {
        // 使用PreparedStatement替代Statement以提高性能和安全性

        StringBuilder sql = new StringBuilder("INSERT INTO output(SUBJECT, CONTACTID, PRODUCTID, AMOUNT, TOLTALPRICE, DISCOUNT, CONTENT, EMPLOYEEID, TIME, CATEGORY, FOLDERID) VALUES (")
           .append("'").append(BarFrame.instance.getCmbCurTable().getSelectedItem().toString()).append("',") // SUBJECT
           .append("'").append(billIndex).append("',") // CONTACTID
           .append(dish.getId()).append(",") // PRODUCTID
           .append(dish.getNum()).append(",") // AMOUNT
           .append(dish.getPrice() * splitRate).append(",") // TOLTALPRICE
           .append(dish.getDiscount() * splitRate).append("',") // DISCOUNT
           .append("'").append(dish.getModification()).append("',") // CONTENT
           .append(LoginDlg.USERID).append(",") // EMPLOYEEID
           .append("'").append(BarFrame.instance.valOpenTime.getText()).append("',") // TIME
           .append("'").append(createdTime != null ? createdTime : dish.getOrderedTime()).append("',")// CATEGORY
           .append(dish.getBillID()); // FOLDERID
        AikaPos.db.execSQL(sql.toString()); // 执行插入操作
        return (int) DatabaseUtils.longForQuery(AikaPos.db, "SELECT last_insert_rowid()", null);
    }

    public static String getDisplayableNumWithX(int num) {
        //first pick out the number on 100,0000 and 10000 position
        return getDisplayableNum(num).append("x").toString();
    }

    public static StringBuilder getDisplayableNum(int num) {
        //first pick out the number on 100,0000 and 10000 position
        int pK = num /(BarOption.MaxQTY * 100);
        if(num > BarOption.MaxQTY * 100) {
            num = num %(BarOption.MaxQTY * 100);
        }
        int pS = num /BarOption.MaxQTY;
        if(num > BarOption.MaxQTY) {
            num = num % BarOption.MaxQTY;
        }
        StringBuilder strNum = new StringBuilder(String.valueOf(num));
        if(pS > 0)
            strNum.append("/").append(pS);
        if(pK > 0)
            strNum.append("/").append(pK);
        return strNum;
    }
    //none saving fields-----------------------------------------
    private int num = 1;
    private float discount;
    private int outputID = -1;
    private String modification;
    private String customerIndex;
    private String orderedTime;
    private int billID;
    private int totalPrice;	//TOLTALPRICE = " + (dish.getPrice() - dish.getDiscount()) * splitRate
    private Integer ruleMark;
    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

    public float getDiscount() {
        return discount;
    }
    public void setDiscount(float discount){
        this.discount = discount;
    }

    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }

    public String getOrderedTime() {
        return orderedTime;
    }
    public void setOrderedTime(String time){
        this.orderedTime = time;
    }

    public int getBillID() {
        return billID;
    }

    public void setBillID(int billID) {
        this.billID = billID;
    }

    public int getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getRuleMark() {
        return ruleMark;
    }

    public void setRuleMark(Integer ruleMark) {
        this.ruleMark = ruleMark;
    }

    public String getCustomerIndex() {
        return customerIndex;
    }

    public void setCustomerIndex(String customerIndex) {
        this.customerIndex = customerIndex;
    }

    public String getLanguage(int i) {
        if(i == 0){
            return name1;
        }else{
            String name = name1;
            if(i == 2){
                name = name2;
            }else if (i == 3){
                name = name3;
            }else{
                name = name1;
            }
            if(name == null){
                name =  name1;
            }
            return name;
        }
    }

    public void setLanguage(int i,
                            String language) {
        if(i == 0){
            name1 = language;
        }else if (i == 1){
            name2 = language;
        }else if (i == 2){
            name3 = language;
        }else{
            name1 = language;
        }
        if(name1 == null){
            name1 = language;
        }
    }

    @Override
    public Dish clone(){
        Dish dish = new Dish();
        dish.setBillID(billID);
        dish.setCustomerIndex(customerIndex);
        dish.setCategoryId(getCategoryId());
        dish.setDiscount(discount);
        dish.setDisplayIdx(getDisplayIdx());
        dish.setGst(gst);
        dish.setQst(qst);
        dish.setId(id);
        dish.setLanguage(0, name1);
        dish.setLanguage(1, name2);
        dish.setLanguage(2, name3);
        dish.setModification(modification);
        dish.setNum(num);
        dish.setOrderedTime(orderedTime);
        dish.setOutputID(outputID);
        dish.setPrice(price);
        dish.setPrinterIds(getPrinterIds());
        dish.setMenuPomp(getMenuPomp());
        dish.setModifyPomp(getModifyPomp());
        dish.setPricePomp(getPricePomp());
        dish.setQst(qst);
        dish.setSize(size);
        dish.setTotalPrice(totalPrice);
        dish.setStatus(status);
        return dish;
    }

    public static void splitOutputList(ArrayList<Dish> selectdDishAry, int splitAmount, String billIndex, int billID) {
        for(int i = 0; i < selectdDishAry.size(); i++) {
            Dish dish = selectdDishAry.get(i);
            if(billID > 0)
                dish.setBillID(billID);
            splitOutput(dish, splitAmount, billIndex, dish.getBillID());
        }
    }

    public static void splitOutput(Dish dish, int splitAmount, String billIndex) {
        splitOutput(dish, splitAmount, billIndex, 0);
    }

    public static void splitOutput(Dish dish, int splitAmount, String billIndex, int billId) {
        int num = dish.getNum();			//current amount
        //first pick out the number on 100,0000 and 10000 position
        int pK = num /(BarOption.MaxQTY * 100);
        if(num > BarOption.MaxQTY * 100) {
            num = num %(BarOption.MaxQTY * 100);
        }
        int pS = num /BarOption.MaxQTY;
        if(num > BarOption.MaxQTY) {
            num = num % BarOption.MaxQTY;
        }
        //calculate the new rate of dividing price, and the new num to update into the db.
        float splitRate = (float) 1.0 / splitAmount;	//default division rate.
        int pX = splitAmount * BarOption.MaxQTY;

        //check the new split number should be added onto pS or pK.
        if(pS > 0) {	//while if it's already a float, then reset the division rate.
            //dont' need ot do it as the total price is not price anymore, now we save final pirce in total price. splitRate = splitRate / pS;
            num += pS * BarOption.MaxQTY ;		//if more than one time division, put the number on higher position.
            pX *= 100;
        }
        if(pK > 0) {
            //splitRate = splitRate / pK;
            num += pK * BarOption.MaxQTY * 100;
            pX *= 100;
        }
        num += pX;
        if(billIndex == null) {		//updating the original output.
            StringBuilder sql = new StringBuilder("update output set amount = ").append(num)
                    .append(", category = ").append(dish.getBillID())
                    .append(", TOLTALPRICE = ").append(dish.getTotalPrice() * splitRate)
                    .append(", discount = ").append(dish.getDiscount() * splitRate)
                    .append(" where id = ").append(dish.getOutputID());
            try {
                AikaPos.db.execSQL(sql.toString());
            } catch (Exception exp) {
                L.e("Dish ", "splitOutput" + sql, exp);
            }
        }else {						//creating a splited one.
            dish.setNum(num);
            createSplitedOutput(dish, billIndex, splitRate, null);
        }
    }
    //create new outputs.
    public static void splitOutputList(ArrayList<Dish> selectdDishAry, int splitAmount, String billIndex) {
        splitOutputList(selectdDishAry, splitAmount, billIndex, 0);
    }
    // KEEP METHODS END

}
