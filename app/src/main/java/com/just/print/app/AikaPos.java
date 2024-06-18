package com.just.print.app;

import com.just.print.R;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.just.print.db.bean.DiningTable;
import com.just.print.db.bean.User;
import com.just.print.db.dao.DaoMaster;
import com.just.print.db.dao.DaoSession;
import com.just.print.db.dao.DiningTableDao;
import com.just.print.db.dao.UserDao;
import com.just.print.ui.fragment.LoginDlg;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.casutil.L;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        // 检查User表是否为空
        if (isTableEmpty(db, "USER")) {
            insertInitialUsers(daoSession); //this is the right way, which access db with a list in one shot.
            initOtherTableWithRawSQL(daoSession); //this is for quicket migration from Windows.
            copyKeystoreToInternalStorage();
        }
    }

    public void copyKeystoreToInternalStorage() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.jckconfig); // 假设keystore文件保存在res/raw文件夹中
            OutputStream outputStream = new FileOutputStream(new File(getFilesDir(), "jckconfig"));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTableEmpty(SQLiteDatabase db, String tableName) {
        Cursor rs = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        boolean isEmpty = true;
        if (rs.moveToFirst()) {
            isEmpty = rs.getInt(0) == 0;
        }
        rs.close();
        return isEmpty;
    }

    private void insertInitialUsers(DaoSession session) {
        UserDao userDao = session.getUserDao();
        List<User> users = new ArrayList<>();
        String currentTime = BarUtil.df.format(new Date());
        users.add(new User(null, "user", "0", "Operator1", 0, currentTime, LoginDlg.USER_STATUS, "", "", "1111", 0, 0));
        users.add(new User(null, "user", "1", "Operator2", 1, currentTime, LoginDlg.USER_STATUS, "", "", "2222", 0, 0));
        users.add(new User(null, "user", "2", "Manager", 2, currentTime, LoginDlg.ADMIN_STATUS, "", "", "3333", 0, 0));
        users.add(new User(null, "user", "3", "Admin", 1, currentTime, LoginDlg.SUPER_ADMIN, "", "", "4444", 0, 0));
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
        String sql = "INSERT INTO PRINTER (NAME, CATEGORY, LANGTYPE, IP, STYLE, STATUS) VALUES ('P1', 0, 0, 'mev', 1, 42)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (NAME, CATEGORY, LANGTYPE, IP, STYLE, STATUS) VALUES ('P2', 0, 1, '192.168.1.88', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (NAME, CATEGORY, LANGTYPE, IP, STYLE, STATUS) VALUES ('P3', 0, 2, '192.168.1.87', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (NAME, CATEGORY, LANGTYPE, IP, STYLE, STATUS) VALUES ('P4', 0, 2, '192.168.1.86', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (NAME, CATEGORY, LANGTYPE, IP, STYLE, STATUS) VALUES ('P5', 0, 2, '192.168.1.85', 0, 32)";
        db.execSQL(sql);
        sql = "INSERT INTO PRINTER (NAME, CATEGORY, LANGTYPE, IP, STYLE, STATUS) VALUES ('P6', 0, 2, '192.168.1.84', 0, 32)";
        db.execSQL(sql);
    }

    private void createEvaluationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS evaluation (" +
                "ID INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, userId INTEGER, " +
                "tableName TEXT, receive INTEGER, target INTEGER, profit INTEGER);";
        db.execSQL(sql);
    }

    private void createBillTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Bill (" +
                "ID INTEGER PRIMARY KEY, createtime TEXT, tableName TEXT, BillIndex TEXT, " +
                "total INTEGER, discount INTEGER, serviceFee INTEGER, cashReceived INTEGER, " +
                "debitReceived INTEGER, visaReceived INTEGER, masterReceived INTEGER, " +
                "otherReceived TEXT, tip INTEGER, cashback INTEGER, STATUS INTEGER, " +
                "userId INTEGER, Comment TEXT, opentime TEXT, invoiceId INTEGER);";
        db.execSQL(sql);
    }

    private void createWEBSRMBatchTable() {
        String sql = "CREATE TABLE IF NOT EXISTS WEBSRMBATCH (" +
                "ID INTEGER PRIMARY KEY, userId INTEGER, jsonToSend TEXT, " +
                "signatransm TEXT, emprCertifSEV TEXT, strUrlQRCode TEXT);";
        db.execSQL(sql);
    }

    private void createOperationLogTable() {
        String sql = "CREATE TABLE IF NOT EXISTS OPERATIONLOG (" +
                "ID INTEGER PRIMARY KEY, OperationType INTEGER, userId INTEGER, " +
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
