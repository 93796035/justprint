package com.just.print.app;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.just.print.R;
import com.just.print.ui.fragment.LoginDlg;

import org.cas.client.platform.bar.BarUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class AikaPos extends Application {

    public volatile static AikaPos instance;
    public static SQLiteDatabase db;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initDaoMaster();
        new XCrashHandler().init(this);
    }

    public void initDaoMaster() {
        MyDatabaseHelper helper = new MyDatabaseHelper(this);
        db = helper.getWritableDatabase();//database file is generated now
        // 先检查User表是否存在
        if (isTableExists(db, "USER")) {
            // 如果User表存在，检查是否为空
            if (isTableEmpty(db, "USER")) {
                insertInitialUsers(); // Insert initial users if the table is empty
            }
        } else {
            createTables(); // 假设有一个方法来创建User表
            insertInitialData(); // 添加其他初始化数据
            insertHardwareData();// 插入硬件设备数据
            insertInitialUsers(); // 然后插入初始数据
        }
        copyKeystoreToInternalStorage();
    }

    // 检查表是否存在的方法
    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
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

    private void insertInitialUsers() {
        String sql = "insert into user (name, familyName, nickName, address, lang, joinTime, role, noTPS, noTVQ, pwd, ANNIVERSARY, status, version) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String currentTime = BarUtil.df.format(new Date());
        db.execSQL(sql, new Object[]{"Michael", "Johndan", "ShuaiGe", "10754 l'hotel", 0, currentTime, LoginDlg.USER_STATUS, "", "", "1111", "", 0, 0});
        db.execSQL(sql, new Object[]{"Tao", "Jiang", "Operator2","1214 cardinal", 1, currentTime, LoginDlg.USER_STATUS, "", "", "2222", "", 0, 0});
        db.execSQL(sql, new Object[]{"Wei", "Hao", "hw", "Manager", 2, currentTime, LoginDlg.ADMIN_STATUS, "", "", "3333", "", 0, 0});
        db.execSQL(sql, new Object[]{"Eric", "Jiang", "Admin","1210 cardinal", 1, currentTime, LoginDlg.SUPER_ADMIN, "", "", "4444", "", 0, 0});
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

    private void createTables(){
        //user
        db.execSQL("CREATE TABLE IF NOT EXISTS USER (_id INTEGER PRIMARY KEY, NAME TEXT, familyName TEXT, nickName TEXT, ADDRESS TEXT, " +
                "LANG INTEGER, joinTime TEXT, ROLE INTEGER, noTPS TEXT, noTVQ TEXT, PWD TEXT, ANNIVERSARY TEXT, STATUS INTEGER, VERSION INTEGER);");

        //printer
        db.execSQL("CREATE TABLE IF NOT EXISTS PRINTER (_id INTEGER PRIMARY KEY, IP TEXT, NAME TEXT, STYLE INTEGER, CATEGORY INTEGER, " +
                "langType INTEGER, STATUS INTEGER, VERSION INTEGER);");

        //DiningTable
        db.execSQL("CREATE TABLE IF NOT EXISTS DINING_TABLE (_id INTEGER PRIMARY KEY, NAME TEXT, openTime TEXT, displayIdx INTEGER, posX INTEGER, " +
                "posY INTEGER, WIDTH INTEGER, HEIGHT INTEGER, TYPE INTEGER, STATUS INTEGER, VERSION INTEGER);");
        //SystemInfo
        db.execSQL("CREATE TABLE IF NOT EXISTS SYSTEMINFO (_id INTEGER PRIMARY KEY, keyName TEXT, VALUE TEXT, STATUS INTEGER, VERSION INTEGER);");

        //evaluation
        db.execSQL("CREATE TABLE IF NOT EXISTS EVALUATION (_id INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, userId INTEGER, " +
                    "tableName TEXT, RECEIVE INTEGER, TARGET INTEGER, PROFIT INTEGER, STATUS INTEGER, VERSION INTEGER);");

        //Bill
        db.execSQL("CREATE TABLE IF NOT EXISTS BILL (_id INTEGER PRIMARY KEY, createdTime TEXT, tableName TEXT, billIndex TEXT, " +
                "TOTAL INTEGER, DISCOUNT INTEGER, serviceFee INTEGER, cashReceived INTEGER, " +
                "debitReceived INTEGER, visaReceived INTEGER, masterReceived INTEGER, " +
                "otherReceived TEXT, TIP INTEGER, CASHBACK INTEGER, STATUS INTEGER, " +
                "userId INTEGER, COMMENT TEXT, openTime TEXT, invoiceId INTEGER, VERSION INTEGER);");

        //WEBSRMBATCH
        db.execSQL("CREATE TABLE IF NOT EXISTS WEBSRMBATCH (" +
                "_id INTEGER PRIMARY KEY, userId INTEGER, jsonToSend TEXT, signatransm TEXT, emprCertifSEV TEXT, strUrlQRCode TEXT);");

        //OPERATIONLOG
        db.execSQL("CREATE TABLE IF NOT EXISTS OPERATIONLOG (_id INTEGER PRIMARY KEY, OperationType INTEGER, userId INTEGER, " +
                "BILLSTATUS INTEGER, BILLID INTEGER, OUTPUTID INTEGER, tablename TEXT, " +
                "openTime TEXT, billIdx TEXT, dishName TEXT, dishPrice INTEGER, " +
                "orderTime TEXT, operationTime TEXT, other1 INTEGER, other2 INTEGER, " +
                "other3 TEXT, other4 TEXT);");

        // Category
        db.execSQL("CREATE TABLE IF NOT EXISTS CATEGORY (" +
                "_id INTEGER PRIMARY KEY, NAME1 TEXT, NAME2 TEXT, NAME3 TEXT, displayIdx INTEGER, STATUS INTEGER, VERSION INTEGER);");

        //Dish
        db.execSQL("CREATE TABLE IF NOT EXISTS DISH (" +
                "_id INTEGER PRIMARY KEY, NAME1 TEXT, NAME2 TEXT, NAME3 TEXT, PRICE INTEGER,"
                + "GST TEXT, QST TEXT, SIZE TEXT, printerIds TEXT, displayIdx INTEGER, pricePomp BOOLEAN,"
                + "menuPomp BOOLEAN, ModifyPomp BOOLEAN, CATEGORY TEXT, STATUS INTEGER, VERSION INTEGER);");

        //Mark
        db.execSQL("CREATE TABLE IF NOT EXISTS MARK (" +
                "_id INTEGER PRIMARY KEY, NAME1 TEXT, NAME2 TEXT, NAME3 TEXT, TYPE INTEGER, STATUS INTEGER, VERSION INTEGER);");

        //SALE_RECORD
        db.execSQL("CREATE TABLE IF NOT EXISTS SALE_RECORD (" +
                "_id INTEGER PRIMARY KEY, tableName TEXT, billIndex TEXT, openTime TEXT, productId INTEGER," +
                "AMOUNT FLOAT, totalPrice FLOAT, DISCOUNT, FLOAT, MODIFICATIONS TEXT, userId INTEGER, billId INTEGER, createdTime TEXT," +
                "modifiedTime TEXT, STATUS INTEGER, VERSION INTEGER);");

        //Customizations
        db.execSQL("CREATE TABLE IF NOT EXISTS CUSTOMIZATIONS (_id INTEGER PRIMARY KEY, key TEXT, VALUE TEXT, TYPE TEXT, STATUS INTEGER, VERSION INTEGER);");

        // CustomizedRule
        db.execSQL("CREATE TABLE IF NOT EXISTS CUSTOMIZED_RULE (_id INTEGER PRIMARY KEY, ruleName TEXT, CONTENT TEXT," +
                " ACTION INTEGER, displayIdx INTEGER, ext1 TEXT, ext2 TEXT, ext3 TEXT, STATUS INTEGER, VERSION INTEGER);");

        // Device
        db.execSQL("CREATE TABLE IF NOT EXISTS DEVICE (_id INTEGER PRIMARY KEY, IP TEXT, userName TEXT, STATUS INTEGER, VERSION INTEGER);");

        // Logs
        db.execSQL("CREATE TABLE IF NOT EXISTS LOG (_id INTEGER PRIMARY KEY, MSG TEXT, NAME TEXT, logType INTEGER, STATUS INTEGER, VERSION INTEGER);");
    }


    private void insertInitialData() {
        String sql = "INSERT INTO customizations (key, value) VALUES ('NextOrderId', 0)";
        db.execSQL(sql);
        sql = "INSERT INTO Bill (tableName, total) VALUES ('default', 0)";
        db.execSQL(sql);
        // More initial data as needed...
        sql = "Insert into dining_Table (NAME, openTime, displayIdx, posX, posY, width, height, type, status, version) values (?,?,?,?,?,?,?,?,?,?)";
        db.execSQL(sql, new Object[]{"01", "", 1, 100, 50, 40, 40, 11, 0, 0});
        db.execSQL(sql, new Object[]{"02", "", 2, 400, 50, 40, 40, 22, 0, 0});
        db.execSQL(sql, new Object[]{"03", "", 3, 600, 50, 40, 40, 33, 0, 0});
        db.execSQL(sql, new Object[]{"04", "", 4, 800, 50, 40, 40, 44, 0, 0});
    }

    public File getDBPath() {
        return getDatabasePath("twens");
    }

}