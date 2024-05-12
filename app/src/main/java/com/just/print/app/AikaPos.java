package com.just.print.app;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.just.print.db.bean.DiningTable;
import com.just.print.db.bean.User;
import com.just.print.db.dao.DaoMaster;
import com.just.print.db.dao.DaoSession;
import com.just.print.db.dao.DiningTableDao;
import com.just.print.db.dao.UserDao;
import com.just.print.ui.fragment.LoginDialogFragment;

import org.cas.client.platform.bar.BarUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AikaPos extends Application {

    public volatile static AikaPos instance;
    public static SQLiteDatabase db;
    public static DaoSession daoSession;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initDaoMaster();
        new XCrashHandler().init(this);
    }

    public void initDaoMaster() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                this,
                getDBPath().getAbsolutePath(),
                null);
        db = helper.getWritableDatabase();//database file is generated now
        daoSession = new DaoMaster(db).newSession();
        // 检查User表是否存在
        if (isTableExists(db, "USER")) {
            // 检查User表是否为空
            if (isTableEmpty(db, "USER")) {

                insertInitialUsers(daoSession); //this is the right way, which access db with a list in one shot.
                initOtherTableWithRawSQL(daoSession); //this is for quicket migration from Windows.
            }
        }
    }

    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    private boolean isTableEmpty(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        boolean isEmpty = true;
        if (cursor.moveToFirst()) {
            isEmpty = cursor.getInt(0) == 0;
        }
        cursor.close();
        return isEmpty;
    }

    private void insertInitialUsers(DaoSession session) {
        UserDao userDao = session.getUserDao();
        List<User> users = new ArrayList<>();
        String currentTime = BarUtil.df.format(new Date());
        users.add(new User(null, "user", "1", "Operator1", 1, currentTime, LoginDialogFragment.EMPLOYEE_USER, "", "", "1111", 0, 0));
        users.add(new User(null, "user", "2", "Operator2", 2, currentTime, LoginDialogFragment.EMPLOYEE_USER, "", "", "2222", 0, 0));
        users.add(new User(null, "user", "3", "Manager", 3, currentTime, LoginDialogFragment.MANAGER_USER, "", "", "3333", 0, 0));
        users.add(new User(null, "user", "4", "Admin", 2, currentTime, LoginDialogFragment.ADMIN_USER, "", "", "4444", 0, 0));
        userDao.insertInTx(users);
    }

    private void initOtherTableWithRawSQL(DaoSession session) {
        insertHardwareData();// 插入硬件设备数据
        createEvaluationTable(); // 创建评估表
        createBillTable();// 创建Bill表
        createWEBSRMBatchTable();// 创建WEBSRMBatch表
        createOperationLogTable();// 创建操作日志表
        insertInitialData(session); // 添加其他初始化数据
    }

    private void insertHardwareData() {
        String sql = "INSERT INTO PRINTER (name, category, langType, ip, style, status) VALUES ('P1', 0, 0, 'mev', 1, 42)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (name, category, langType, ip, style, status) VALUES ('P2', 0, 1, '192.168.1.88', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (name, category, langType, ip, style, status) VALUES ('P3', 0, 2, '', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (name, category, langType, ip, style, status) VALUES ('P4', 0, 2, '', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (name, category, langType, ip, style, status) VALUES ('P5', 0, 2, '', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (name, category, langType, ip, style, status) VALUES ('P6', 0, 2, '', 0, 32)";
        db.execSQL(sql);
    }

    private void createEvaluationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS evaluation (" +
                "ID INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, EMPLOYEEID INTEGER, " +
                "SUBJECT TEXT, receive INTEGER, target INTEGER, profit INTEGER);";
        db.execSQL(sql);
    }

    private void createBillTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Bill (" +
                "ID INTEGER PRIMARY KEY, createtime TEXT, tableID TEXT, BillIndex TEXT, " +
                "total INTEGER, discount INTEGER, serviceFee INTEGER, cashReceived INTEGER, " +
                "debitReceived INTEGER, visaReceived INTEGER, masterReceived INTEGER, " +
                "otherReceived TEXT, tip INTEGER, cashback INTEGER, status INTEGER, " +
                "EMPLOYEEID INTEGER, Comment TEXT, opentime TEXT, invoiceId INTEGER);";
        db.execSQL(sql);
    }

    private void createWEBSRMBatchTable() {
        String sql = "CREATE TABLE IF NOT EXISTS WEBSRMBATCH (" +
                "ID INTEGER PRIMARY KEY, EMPLOYEEID INTEGER, jsonToSend TEXT, " +
                "signatransm TEXT, emprCertifSEV TEXT, strUrlQRCode TEXT);";
        db.execSQL(sql);
    }

    private void createOperationLogTable() {
        String sql = "CREATE TABLE IF NOT EXISTS OPERATIONLOG (" +
                "ID INTEGER PRIMARY KEY, OperationType INTEGER, EMPLOYEEID INTEGER, " +
                "BILLSTATUS INTEGER, BILLID INTEGER, OUTPUTID INTEGER, tablename TEXT, " +
                "opentime TEXT, billIdx TEXT, dishName TEXT, dishPrice INTEGER, " +
                "orderTime TEXT, operationTime TEXT, other1 INTEGER, other2 INTEGER, " +
                "other3 TEXT, other4 TEXT);";
        db.execSQL(sql);
    }

    private void insertInitialData(DaoSession session) {
        SQLiteDatabase db = session.getDatabase();
        String sql = "INSERT INTO customizations (key, value) VALUES ('NextOrderId', 0)";
        db.execSQL(sql);
        sql = "INSERT INTO Bill (tableName, total) VALUES ('default', 0)";
        db.execSQL(sql);
        // More initial data as needed...
        DiningTableDao diningTableDao = session.getDiningTableDao();
        List<DiningTable> diningTables = new ArrayList<>();
        //public DiningTable(Long id, String name, String openTime, Integer dspIdx, Integer posX, Integer posY, Integer width, Integer height, Integer type, Integer status, Integer version) {
        diningTables.add(new DiningTable(null, "01", "", 1, 100, 50, 40, 40, 11, 0, 0));
        diningTables.add(new DiningTable(null, "02", "", 2, 200, 50, 40, 40, 22, 0, 0));
        diningTables.add(new DiningTable(null, "03", "", 3, 300, 50, 40, 40, 33, 0, 0));
        diningTables.add(new DiningTable(null, "04", "", 4, 400, 50, 40, 40, 44, 0, 0));
        diningTableDao.insertInTx(diningTables);
    }

    public File getDBPath() {
        return getDatabasePath("twens");
    }

}
