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
        boolean userTableExists = isTableExists(db, "USER");
        createTables();
        ensureSecurityCompatibleSchema();
        // 先检查User表是否存在
        if (userTableExists) {
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
                "BILLSTATUS INTEGER, BILLID INTEGER, OUTPUTID INTEGER, tableName TEXT, " +
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


    private void ensureSecurityCompatibleSchema() {
        ensureColumnExists("BILL", "customerId", "INTEGER");
        ensureColumnExists("BILL", "last_modified", "TEXT DEFAULT '1900-01-01 00:00:00'");
        ensureColumnExists("BILL", "cloud_sync_status", "INTEGER DEFAULT 0");
        ensureColumnExists("BILL", "vipAppliedDue", "INTEGER DEFAULT 0");
        ensureColumnExists("BILL", "ownerTerminal", "TEXT");
        ensureColumnExists("BILL", "plan_note", "TEXT");

        ensureColumnExists("DINING_TABLE", "last_modified", "TEXT DEFAULT '1900-01-01 00:00:00'");
        ensureColumnExists("DINING_TABLE", "cloud_sync_status", "INTEGER DEFAULT 0");

        ensureColumnExists("CATEGORY", "cloud_sync_status", "INTEGER DEFAULT 0");
        ensureColumnExists("CATEGORY", "last_modified", "TEXT DEFAULT '1900-01-01 00:00:00'");

        ensureColumnExists("DISH", "code", "TEXT");
        ensureColumnExists("DISH", "unit", "TEXT");
        ensureColumnExists("DISH", "brand", "TEXT");
        ensureColumnExists("DISH", "cost", "INTEGER DEFAULT 0");
        ensureColumnExists("DISH", "stock", "INTEGER DEFAULT 0");
        ensureColumnExists("DISH", "coverDays", "REAL DEFAULT 0");
        ensureColumnExists("DISH", "weekly_sales_qty", "INTEGER DEFAULT 0");
        ensureColumnExists("DISH", "monthly_sales_qty", "INTEGER DEFAULT 0");
        ensureColumnExists("DISH", "case_qty", "INTEGER");
        ensureColumnExists("DISH", "last_modified", "TEXT DEFAULT '1900-01-01 00:00:00'");
        ensureColumnExists("DISH", "cloud_sync_status", "INTEGER DEFAULT 0");

        ensureColumnExists("CUSTOMIZATIONS", "cloud_sync_status", "INTEGER DEFAULT 0");
        ensureColumnExists("CUSTOMIZED_RULE", "cloud_sync_status", "INTEGER DEFAULT 0");
        ensureColumnExists("USER", "cloud_sync_status", "INTEGER DEFAULT 0");
        ensureColumnExists("PRINTER", "cloud_sync_status", "INTEGER DEFAULT 0");
        ensureColumnExists("SYSTEMINFO", "cloud_sync_status", "INTEGER DEFAULT 0");

        db.execSQL("CREATE TABLE IF NOT EXISTS MODIFICATION (" +
                "_id INTEGER PRIMARY KEY, LANG1 TEXT, LANG2 TEXT, LANG3 TEXT, LANG4 TEXT, LANG5 TEXT, LANG6 TEXT, " +
                "TYPE INTEGER, STATUS INTEGER, VERSION INTEGER);");

        db.execSQL("CREATE TABLE IF NOT EXISTS TODO (" +
                "_id INTEGER PRIMARY KEY, TASK TEXT, SCHEDULE TEXT, DONE INTEGER DEFAULT 0, CREATED_AT TEXT, " +
                "DELETED INTEGER DEFAULT 0, DONE_AT TEXT, cloud_sync_status INTEGER DEFAULT 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS CLOUDSYNC_META (" +
                "_id INTEGER PRIMARY KEY, ENTITY TEXT, LAST_SYNC_AT TEXT, LAST_ERROR TEXT, STATUS INTEGER DEFAULT 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS PRODUCT_PRICE_HISTORY (" +
                "_id INTEGER PRIMARY KEY, PRODUCT_ID INTEGER, PRODUCT_CODE TEXT, PRODUCT_NAME TEXT, OLD_PRICE INTEGER, " +
                "NEW_PRICE INTEGER, OLD_COST INTEGER, NEW_COST INTEGER, CHANGE_TYPE TEXT, EMPLOYEE_ID INTEGER, " +
                "CHANGED_AT TEXT, NOTE TEXT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS PRODUCT (" +
                "_id INTEGER PRIMARY KEY, CODE TEXT, MNEMONIC TEXT, SUBJECT TEXT, PRICE INTEGER, COST INTEGER DEFAULT 0, " +
                "BRAND TEXT, UNIT TEXT, PRODUCAREA TEXT, CONTENT TEXT, CATEGORY TEXT, FOLDERID INTEGER, QST INTEGER, " +
                "STOCK INTEGER DEFAULT 0, COVERDAYS REAL DEFAULT 0, WEEKLY_SALES_QTY INTEGER DEFAULT 0, " +
                "MONTHLY_SALES_QTY INTEGER DEFAULT 0, CASE_QTY INTEGER, displayIdx INTEGER, STATUS INTEGER, VERSION INTEGER, " +
                "last_modified TEXT DEFAULT '1900-01-01 00:00:00', cloud_sync_status INTEGER DEFAULT 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS OUTPUT (" +
                "_id INTEGER PRIMARY KEY, SUBJECT TEXT, CONTACTID INTEGER, PRODUCTID INTEGER, AMOUNT FLOAT, TOTALPRICE FLOAT, " +
                "DISCOUNT FLOAT, CONTENT TEXT, EMPLOYEEID INTEGER, TIME TEXT, CATEGORY TEXT, FOLDERID INTEGER, " +
                "DELETED INTEGER DEFAULT 0, STATUS INTEGER, VERSION INTEGER);");

        db.execSQL("CREATE TABLE IF NOT EXISTS EMPLOYEE (" +
                "_id INTEGER PRIMARY KEY, NAME TEXT, ROLE INTEGER, STATUS INTEGER, VERSION INTEGER, cloud_sync_status INTEGER DEFAULT 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS HARDWARE (" +
                "_id INTEGER PRIMARY KEY, NAME TEXT, IP TEXT, CATEGORY INTEGER, STYLE INTEGER, STATUS INTEGER, VERSION INTEGER, " +
                "cloud_sync_status INTEGER DEFAULT 0);");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_BILL_TABLE_OPENTIME_STATUS ON BILL (tableName, openTime, STATUS);");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_SALE_RECORD_BILL ON SALE_RECORD (billId, STATUS);");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_SALE_RECORD_TABLE_TIME ON SALE_RECORD (tableName, openTime, STATUS);");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DISH_CATEGORY_DISPLAY ON DISH (CATEGORY, STATUS, displayIdx);");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PRODUCT_CATEGORY_DISPLAY ON PRODUCT (CATEGORY, STATUS, displayIdx);");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_OUTPUT_SUBJECT_TIME_DELETED ON OUTPUT (SUBJECT, TIME, DELETED);");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PRODUCT_PRICE_HISTORY_PRODUCT ON PRODUCT_PRICE_HISTORY (PRODUCT_ID, CHANGED_AT);");
    }

    private void ensureColumnExists(String tableName, String columnName, String columnDef) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            while (cursor.moveToNext()) {
                String existingName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equalsIgnoreCase(existingName)) {
                    return;
                }
            }
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDef);
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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
