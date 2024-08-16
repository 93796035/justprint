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
        // 检查User表是否为空
        if (isTableEmpty(db, "USER")) {
            insertInitialUsers(); //this is the right way, which access db with a list in one shot.
            initOtherTableWithRawSQL(); //this is for quicket migration from Windows.
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

    private void insertInitialUsers() {
        String sql = "insert into user (name, familyName, nickName, address, lang, joinTime, role, noTPS, noTVQ, pwd, ANNIVERSARY, status, version) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String currentTime = BarUtil.df.format(new Date());
        db.execSQL(sql, new Object[]{"Michael", "Johndan", "ShuaiGe", "10754 l'hotel", 0, currentTime, LoginDlg.USER_STATUS, "", "", "1111", "", 0, 0});
        db.execSQL(sql, new Object[]{"Tao", "Jiang", "Operator2","1214 cardinal", 1, currentTime, LoginDlg.USER_STATUS, "", "", "2222", "", 0, 0});
        db.execSQL(sql, new Object[]{"Wei", "Hao", "hw", "Manager", 2, currentTime, LoginDlg.ADMIN_STATUS, "", "", "3333", "", 0, 0});
        db.execSQL(sql, new Object[]{"Eric", "Jiang", "Admin","1210 cardinal", 1, currentTime, LoginDlg.SUPER_ADMIN, "", "", "4444", "", 0, 0});
    }

    private void initOtherTableWithRawSQL() {
        insertHardwareData();// 插入硬件设备数据
        createSystemInfoTable();
        createUserTable();
        createEvaluationTable(); // 创建评估表
        createBillTable();// 创建Bill表
        createWEBSRMBatchTable();// 创建WEBSRMBatch表
        createOperationLogTable();// 创建操作日志表
        insertInitialData(); // 添加其他初始化数据
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
    private void createSystemInfoTable(){
        String sql = "CREATE TABLE IF NOT EXISTS systeminfo (" +
                "_id INTEGER PRIMARY KEY, keyName TEXT, value TEXT, STATUS INTEGER, " +
                "VERSION INTEGER);";
        db.execSQL(sql);
    }

    private void createUserTable(){
        String sql = "CREATE TABLE IF NOT EXISTS USER (" +
                "_id INTEGER PRIMARY KEY, name TEXT, familyName TEXT, nickName TEXT, address TEXT, " +
                "lang INTEGER, joinTime TEXT, role INTEGER, noTPS TEXT, noTVQ TEXT, pwd TEXT, ANNIVERSARY TEXT);";
        db.execSQL(sql);
    }

    private void createEvaluationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS evaluation (" +
                "_id INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, userId INTEGER, " +
                "tableName TEXT, receive INTEGER, target INTEGER, profit INTEGER);";
        db.execSQL(sql);
    }

    private void createBillTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Bill (" +
                "_id INTEGER PRIMARY KEY, createdtime TEXT, tableName TEXT, BillIndex TEXT, " +
                "total INTEGER, discount INTEGER, serviceFee INTEGER, cashReceived INTEGER, " +
                "debitReceived INTEGER, visaReceived INTEGER, masterReceived INTEGER, " +
                "otherReceived TEXT, tip INTEGER, cashback INTEGER, STATUS INTEGER, " +
                "userId INTEGER, Comment TEXT, openTime TEXT, invoiceId INTEGER);";
        db.execSQL(sql);
    }

    private void createWEBSRMBatchTable() {
        String sql = "CREATE TABLE IF NOT EXISTS WEBSRMBATCH (" +
                "_id INTEGER PRIMARY KEY, userId INTEGER, jsonToSend TEXT, " +
                "signatransm TEXT, emprCertifSEV TEXT, strUrlQRCode TEXT);";
        db.execSQL(sql);
    }

    private void createOperationLogTable() {
        String sql = "CREATE TABLE IF NOT EXISTS OPERATIONLOG (" +
                "_id INTEGER PRIMARY KEY, OperationType INTEGER, userId INTEGER, " +
                "BILLSTATUS INTEGER, BILLID INTEGER, OUTPUTID INTEGER, tablename TEXT, " +
                "openTime TEXT, billIdx TEXT, dishName TEXT, dishPrice INTEGER, " +
                "orderTime TEXT, operationTime TEXT, other1 INTEGER, other2 INTEGER, " +
                "other3 TEXT, other4 TEXT);";
        db.execSQL(sql);
    }

    private void insertInitialData() {
        String sql = "INSERT INTO customizations (key, value) VALUES ('NextOrderId', 0)";
        db.execSQL(sql);
        sql = "INSERT INTO Bill (tableName, total) VALUES ('default', 0)";
        db.execSQL(sql);
        // More initial data as needed...
        sql = "Insert into diningTable (name, openTime, dspIdx, posX, posY, width, height, type, status, version) values (?,?,?,?,?,?,?,?,?,?)";
        db.execSQL(sql, new Object[]{"01", "", 1, 100, 50, 40, 40, 11, 0, 0});
        db.execSQL(sql, new Object[]{"02", "", 2, 400, 50, 40, 40, 22, 0, 0});
        db.execSQL(sql, new Object[]{"03", "", 3, 600, 50, 40, 40, 33, 0, 0});
        db.execSQL(sql, new Object[]{"04", "", 4, 800, 50, 40, 40, 44, 0, 0});
    }

    public File getDBPath() {
        return getDatabasePath("twens");
    }

}
//    // +---------+---------+---------+---------+
//    // Printer
//    // +---------+---------+---------+---------+
//    Entity printer = schema.addEntity("Printer");
//        printer.addIdProperty();
//                printer.addStringProperty("ip");
//                printer.addStringProperty("name");
//                printer.addIntProperty("style");
//                printer.addIntProperty("category");
//                printer.addIntProperty("langType").columnName("langType");
//
//                // +---------+-----------+------------+---------+
//                // DiningTable
//                // +---------+-----------+------------+---------+
//                Entity dinningTable = schema.addEntity("DiningTable");
//                dinningTable.addIdProperty();
//                dinningTable.addStringProperty("name");
//                dinningTable.addStringProperty("openTime").columnName("openTime");
//                dinningTable.addIntProperty("displayIdx").columnName("displayIdx");
//                dinningTable.addIntProperty("posX").columnName("posX");
//                dinningTable.addIntProperty("posY").columnName("posY");
//                dinningTable.addIntProperty("width");
//                dinningTable.addIntProperty("height");
//                dinningTable.addIntProperty("type");
//
//                // +---------+---------+-------------+
//                // Category
//                // +---------+---------+-------------+
//                Entity category = schema.addEntity("Category");
//                category.addIdProperty();
//                category.addStringProperty("name1").columnName("NAME1");
//                category.addStringProperty("name2").columnName("NAME2");
//                category.addStringProperty("name3").columnName("NAME3");
//                category.addIntProperty("displayIdx").columnName("displayIdx");
//
//                //  @table Menu
//                //  @pk id
//                //  @fk pid, category
//                //  pid->Printer.id;
//                //  category->Category.id;
//                // +---------+---------+---------+---------+
//                // Dish
//                // +---------+---------+---------+---------+
//                Entity dish = schema.addEntity("Dish");//category->dish
//                dish.addIdProperty();
//                dish.addStringProperty("name1").columnName("NAME1");
//                dish.addStringProperty("name2").columnName("NAME2");
//                dish.addStringProperty("name3").columnName("NAME3");
//                dish.addIntProperty("price");
//                dish.addIntProperty("gst");
//                dish.addIntProperty("qst");
//                dish.addIntProperty("size");
//                dish.addStringProperty("printerIds").columnName("printerIds");
//                dish.addIntProperty("displayIdx").columnName("displayIdx");//size
//                dish.addBooleanProperty("pricePomp").columnName("pricePomp");//size
//                dish.addBooleanProperty("menuPomp").columnName("menuPomp");//size
//                dish.addBooleanProperty("ModifyPomp").columnName("ModifyPomp");//size
//                dish.addStringProperty("category").columnName("category");
//
//                // +---------+---------+---------+---------+
//                // M2M_MenuPrint
//                // +---------+---------+---------+---------+
//                // printer and dish are multi to multi, add a dish table.
//                Entity menuPrint = schema.addEntity("M2M_MenuPrint");
//                menuPrint.addIdProperty();
//                Property.PropertyBuilder printerId = menuPrint.addLongProperty("printerId").columnName("printerId");
//                Property.PropertyBuilder menuId = menuPrint.addLongProperty("dishId").columnName("dishId");
//                menuPrint.addToOne(printer, printerId.getProperty(), "print");
//                menuPrint.addToOne(dish, menuId.getProperty(), "dish");
//                dish.addToMany(menuPrint, menuId.getProperty());
//
//                // +---------+---------+---------+---------+
//                // Mark
//                // +---------+---------+---------+---------+
//                //  @table Mark
//                Entity mark = schema.addEntity("Mark");
//                mark.addIdProperty();
//                mark.addStringProperty("name1").columnName("NAME1").unique();
//                mark.addStringProperty("name2").columnName("NAME2").unique();
//                mark.addStringProperty("name3").columnName("NAME3").unique();
//                mark.addIntProperty("type");
//
//                // @table SaleRecord
//                //  @pk id
//                // +---------+-----------+------------+---------+
//                // SaleRecord
//                // +---------+-----------+------------+---------+
//                Entity saleRecord = schema.addEntity("SaleRecord");
//                saleRecord.addIdProperty();
//                saleRecord.addStringProperty("tableName").columnName("tableName");
//                saleRecord.addStringProperty("billIndex").columnName("billIndex");
//                saleRecord.addStringProperty("openTime").columnName("openTime");
//
//                saleRecord.addIntProperty("productId").columnName("productId");
//                saleRecord.addFloatProperty("amount");
//                saleRecord.addFloatProperty("totalPrice").columnName("totalPrice");
//                saleRecord.addFloatProperty("discount");
//                saleRecord.addStringProperty("modifications");
//                saleRecord.addIntProperty("userId").columnName("userId");
//                saleRecord.addIntProperty("billId").columnName("billId");
//                saleRecord.addStringProperty("createdTime").columnName("createdTime");
//                saleRecord.addStringProperty("modifiedTime").columnName("modifiedTime");
//
//                // +---------+---------+---------+---------+
//                // M2M_SaleRecordMark
//                // +---------+---------+---------+---------+
//                // printer and dish are multi to multi, add a dish table.
//                Entity saleRecordMark = schema.addEntity("M2M_SaleRecordMark");
//                saleRecordMark.addIdProperty();
//                Property.PropertyBuilder saleRecId = saleRecordMark.addLongProperty("saleRecId").columnName("saleRecId");
//                Property.PropertyBuilder markId = saleRecordMark.addLongProperty("markId").columnName("markId");
//                saleRecordMark.addToOne(saleRecord, saleRecId.getProperty(), "saleRecord");
//                saleRecordMark.addToOne(mark, markId.getProperty(), "mark");
//                dish.addToMany(saleRecordMark, markId.getProperty());
//
//                // +---------+---------+---------+---------+
//                // Bill
//                // +---------+---------+---------+---------+
//                Entity bill = schema.addEntity("Bill");
//                bill.addIdProperty();
//                bill.addStringProperty("createdTime").columnName("createdTime");
//                bill.addStringProperty("tableName").columnName("tableName");
//                bill.addStringProperty("billIndex").columnName("billIndex");
//                bill.addStringProperty("openTime").columnName("openTime");
//                bill.addIntProperty("Total");
//                bill.addIntProperty("discount");
//                bill.addIntProperty("serviceFee").columnName("serviceFee");
//                bill.addIntProperty("cashReceived").columnName("cashReceived");
//                bill.addIntProperty("debitReceived").columnName("debitReceived");
//                bill.addIntProperty("visaReceived").columnName("visaReceived");
//                bill.addIntProperty("masterReceived").columnName("masterReceived");
//                bill.addStringProperty("otherReceived").columnName("otherReceived");
//                bill.addIntProperty("tip");
//                bill.addIntProperty("cashback");
//                bill.addIntProperty("userId").columnName("userId");
//                bill.addStringProperty("comment");
//                bill.addIntProperty("invoiceId").columnName("invoiceId");
//
//                // +---------+-----------+Customizations+---------+
//                // Customizations
//                // +---------+-----------+------------+---------+
//                Entity customizations = schema.addEntity("Customizations");
//                customizations.addIdProperty();
//                customizations.addStringProperty("key");
//                customizations.addStringProperty("value");
//                customizations.addStringProperty("type");
//
//
//                // +---------+-----------+Customizations+---------+
//                // CustomizedRule
//                // +---------+-----------+------------+---------+
//                Entity customizedRule = schema.addEntity("CustomizedRule");
//                customizedRule.addIdProperty();
//                customizedRule.addStringProperty("rulename");
//                customizedRule.addStringProperty("content");
//                customizedRule.addIntProperty("action");
//                customizedRule.addIntProperty("dispIdx");
//                customizedRule.addStringProperty("ext1");
//                customizedRule.addStringProperty("ext2");
//                customizedRule.addStringProperty("ext3");
//
////        // +---------+--------WebsrmBatch---+------------+---------+
////        // WebsrmBatch
////        // +---------+-----------+------------+---------+
////        Entity websrmBatch = schema.addEntity("WebsrmBatch");
////        websrmBatch.addIdProperty();
////        websrmBatch.addStringProperty("jsonToSend").columnName("jsonToSend");
////        websrmBatch.addStringProperty("signatransm");
////        websrmBatch.addStringProperty("emprCertifSev").columnName("emprCertifSev");
////        websrmBatch.addStringProperty("strUrlQRCode").columnName("strUrlQRCode");
////        websrmBatch.addIntProperty("userId").columnName("userId");
//
//                // +---------+-----------+------------+---------+
//                // Device
//                // +---------+-----------+------------+---------+
//                Entity device = schema.addEntity("Device");//在线设备
//                device.addStringProperty("ip");
//                device.addStringProperty("userName").columnName("userName");
//
//                // +---------+---------+---------+---------+
//                // Log
//                // +---------+---------+---------+---------+
//                //  @table Log
//                Entity log = schema.addEntity("Log");
//                log.setTableName("Logs");
//                log.addIdProperty();
//                log.addStringProperty("msg");
//                log.addStringProperty("name").columnName("name");
//                log.addDateProperty("date");
//                log.addIntProperty("logType").columnName("logType");
//
//                //-------------添加,state,version---------------
//                for (Entity en : schema.getEntities()) {
//                en.addIntProperty("status");// default:0, del:1,
//                en.addIntProperty("version");
//                }
//                }