package com.just.print.db.expand;

import android.database.Cursor;

import com.just.print.db.bean.Bill;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Dish;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.BillDao;
import com.just.print.db.dao.DishDao;
import com.just.print.db.dao.MarkDao;
import com.just.print.db.dao.PrinterDao;
import com.just.print.db.dao.SaleRecordDao;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;

public final class DaoExpand {

    static public <T, K> List<T> queryNotDeletedAll(AbstractDao<T, K> dao) {
        return queryAllNotDeleted(dao).list();
    }

    static public <T, K> List<T> queryAllNotDeletedMark(AbstractDao<T, K> dao) {
        return dao.queryBuilder().where(DishDao.Properties.Version.gt(State.DELETED)).list();
    }

    static public List<Dish> queryMenuByCategory(DishDao menuDao, Category category) {
        return addQueryBuilderNotDelete(menuDao.queryBuilder().where(DishDao.Properties.Category.eq(category.getId()))).list();
    }

    public static final <T> QueryBuilder<T> addQueryBuilderNotDelete(QueryBuilder<T> builder) {
        return builder.where(DishDao.Properties.Status.notEq(State.DELETED));
    }

    static public <T, K> QueryBuilder<T> queryAllNotDeleted(AbstractDao<T, K> dao) {
        return dao.queryBuilder().where(DishDao.Properties.Status.notEq(State.DELETED));
    }

    static public List<Dish> queryMenuByCategory(Category category, DishDao dao) {
        return dao.queryBuilder().where(DishDao.Properties.Category.eq(category.getId()), DishDao.Properties.Status.notEq(State.DELETED)).list();
    }

    static public List<SaleRecord> querySalesByBill(String billId, SaleRecordDao dao) {
        int p = billId.indexOf("\n");
        if(p > -1){
            billId = billId.substring(0, p);
        }
        List<SaleRecord> records = dao.loadAll();
        for (int i = records.size() - 1; i >= 0; i--) {
            if (!billId.equals(records.get(i).getBillId())) {
                records.remove(i);
            }
        }
        return records;

// this line always return 0 row, don't know how to write correctly       return dao.queryBuilder().where(SaleRecordDao.Properties.Bid.eq(billId), SaleRecordDao.Properties.State.notEq(State.delete)).list();
    }

    static public <T, K> List<T> queryNotDeletedAll(AbstractDao<T, K> dao, String shop) {
        return dao.queryBuilder().where(DishDao.Properties.Status.notEq(State.DELETED)).list();
    }

    static public <T, K> List<T> queryAllTables(AbstractDao<T, K> dao) {
        return dao.queryBuilder().where(DishDao.Properties.Version.le(State.DELETED)).list();
    }

    public static List<Mark> queryAllMarks(MarkDao markDao) {
        return markDao.queryBuilder().where(DishDao.Properties.Version.gt(State.DELETED)).list();
    }

    static public List<Dish> queryFuzzyMenu(DishDao menuDao, String ID) {
        List<Dish> lists = menuDao.queryBuilder().where(DishDao.Properties.Id.eq(ID)).list();
        if(lists == null || lists.size() == 0){
            lists = menuDao.queryBuilder().where(DishDao.Properties.Id.like(ID + "%")).list();
        }
        if(lists == null || lists.size() == 0){
            lists = menuDao.queryBuilder().where(DishDao.Properties.Id.like("%" + ID + "%")).list();
        }
        return lists;
    }

    static public int queryMaxVersion(AbstractDao dao) {

        int result = -1;
        Cursor c = dao.getDatabase().rawQuery(String.format("select max(%s) from %s", "VERSION", dao.getTablename()), new String[]{});
        if (c.moveToFirst())
            result = c.getInt(0);
        c.close();
        return result;
    }

    /**
     *
     * 获得主打印机 没有 则返回null
     */
    public static Printer getFirstPrint(PrinterDao printerDao) {
        List<Printer> list = printerDao.queryBuilder().where(PrinterDao.Properties.Style.eq(1)).limit(1).list();
        return list.size() > 0 ? list.get(0) : null;
    }

    static public List<Bill> queryRecentBill(BillDao billDao, int num) {
        List<Bill> bills = billDao.loadAll();
        Collections.sort(bills, new Comparator<Bill>() {
            @Override
            public int compare(Bill o1, Bill o2) {
                Long billIdx1 = o1.getId();
                Long billIdx2 = o2.getId();
                return (int)(billIdx2 - billIdx1);
            }
        });
        return bills.subList(0,num < bills.size() ? num : bills.size());
    }

//    public static void updateAllPrintTo0(PrinterDao printerDao) {
//        List<Printer> list = printerDao.queryBuilder().where(PrinterDao.Properties.FirstPrint.eq(1)).list();
//        for (Printer p : list) {
//            p.setFirstPrint(0);
//            p.updateAndUpgrade();
//        }
//
//    }
}
