package com.generate;


import org.junit.Test;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class MainGenerator {

    @Test
    public void gen() {
        generateAll(1, "com.just.print");
    }

    public void generateAll(int version, String dir) {
        String dbroot = dir + ".db";

        Schema schema = new Schema(version, dbroot + ".bean");
        schema.setDefaultJavaPackageDao(dbroot + ".dao");
        schema.setDefaultJavaPackageTest(dbroot + ".test");
        schema.enableKeepSectionsByDefault();
        schema.enableActiveEntitiesByDefault();
        create(schema);

        File f = new File(getOutputPath());
        if (!f.exists())
            f.mkdirs();

        try {
            new DaoGenerator().generateAll(schema, f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void create(Schema schema) {

        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity user = schema.addEntity("User");
        user.addIdProperty();
        user.addStringProperty("firstName");
        user.addStringProperty("secondName");
        user.addStringProperty("nickName");
        user.addIntProperty("role");// 1 is the biggest
        user.addStringProperty("noTPS");
        user.addStringProperty("noTVQ");
        user.addStringProperty("pwd");

        //  @table Printer
        //  @pk id
        // +---------+---------+---------+---------+
        // |    id   |   pname |   ip    |   note  |
        // +---------+---------+---------+---------+
        Entity printer = schema.addEntity("Printer");
        printer.addIdProperty();
        printer.addStringProperty("ip").columnName("IP");
        printer.addStringProperty("pname").columnName("P_NAME");
        printer.addStringProperty("note").columnName("NOTE");
        printer.addIntProperty("firstPrint").columnName("STYLE");
        printer.addIntProperty("type").columnName("CATEGORY");

        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity dinningTable = schema.addEntity("DiningTable");
        dinningTable.addIdProperty();
        dinningTable.addStringProperty("name");
        dinningTable.addStringProperty("openTime");
        dinningTable.addIntProperty("dspIdx");
        dinningTable.addIntProperty("posX");
        dinningTable.addIntProperty("posY");
        dinningTable.addIntProperty("width");
        dinningTable.addIntProperty("height");
        dinningTable.addIntProperty("type");

        //  @table Category 类别
        //  @pk id
        // +---------+---------+-------------+
        // |    id   |   cname |  displayIdx |
        // +---------+---------+-------------+
        Entity category = schema.addEntity("Category");
        category.addIdProperty();
        category.addStringProperty("name1").columnName("NAME1");
        category.addStringProperty("name2").columnName("NAME2");
        category.addStringProperty("name3").columnName("NAME3");
        category.addIntProperty("displayIdx").columnName("DISPLAY_IDX");

        //  @table Menu
        //  @pk id
        //  @fk pid, categoryId
        //  pid->Printer.id;
        //  categoryId->Category.id;
        // +---------+---------+---------+---------+
        // |    id   |   pid   |  categoryId    |   manem |
        // +---------+---------+---------+---------+
        Entity dish = schema.addEntity("Dish");//category->dish
        dish.addIdProperty();
        dish.addStringProperty("name1").columnName("NAME1");
        dish.addStringProperty("name2").columnName("NAME2");
        dish.addStringProperty("name3").columnName("NAME3");
        dish.addFloatProperty("price");//price
        dish.addIntProperty("gst");//price
        dish.addIntProperty("qst");//price
        dish.addIntProperty("size");//size
        dish.addStringProperty("printerIds");
        dish.addIntProperty("displayIdx");//size
        dish.addBooleanProperty("pricePomp");//size
        dish.addBooleanProperty("menuPomp");//size
        dish.addBooleanProperty("ModifyPomp");//size
        Property.PropertyBuilder categoryId = dish.addLongProperty("categoryId");
        category.addToMany(dish, categoryId.getProperty());

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  mid    |   manem |
        // +---------+---------+---------+---------+
        // printer and dish are multi to multi, add a dish table.
        Entity menuPrint = schema.addEntity("M2M_MenuPrint");
        menuPrint.addIdProperty();
        Property.PropertyBuilder printerId = menuPrint.addLongProperty("printerId");
        Property.PropertyBuilder menuId = menuPrint.addLongProperty("dishId");
        menuPrint.addToOne(printer, printerId.getProperty(), "print");
        menuPrint.addToOne(dish, menuId.getProperty(), "dish");
        dish.addToMany(menuPrint, menuId.getProperty());

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  categoryId    |   manem |
        // +---------+---------+---------+---------+
        //  @table Mark
        Entity mark = schema.addEntity("Mark");
        mark.addIdProperty();
        mark.addStringProperty("name1").columnName("NAME1").unique();
        mark.addStringProperty("name2").columnName("NAME2").unique();
        mark.addStringProperty("name3").columnName("NAME3").unique();
        mark.addIntProperty("type");

        // @table SaleRecord
        //  @pk id
        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity saleRecord = schema.addEntity("SaleRecord");
        saleRecord.addIdProperty();
        saleRecord.addStringProperty("tableName");
        saleRecord.addStringProperty("customerNumber");
        saleRecord.addStringProperty("openTime");

        saleRecord.addIntProperty("productId");
        saleRecord.addFloatProperty("amount");
        saleRecord.addFloatProperty("totalPrice");
        saleRecord.addFloatProperty("discount");
        saleRecord.addStringProperty("modifications");
        saleRecord.addIntProperty("userId");
        saleRecord.addIntProperty("billId");
        saleRecord.addStringProperty("createdTime");

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  mid    |   manem |
        // +---------+---------+---------+---------+
        // printer and dish are multi to multi, add a dish table.
        Entity saleRecordMark = schema.addEntity("M2M_SaleRecordMark");
        saleRecordMark.addIdProperty();
        Property.PropertyBuilder saleRecId = saleRecordMark.addLongProperty("saleRecId");
        Property.PropertyBuilder markId = saleRecordMark.addLongProperty("markId");
        saleRecordMark.addToOne(saleRecord, saleRecId.getProperty(), "saleRecord");
        saleRecordMark.addToOne(mark, markId.getProperty(), "mark");
        dish.addToMany(saleRecordMark, markId.getProperty());

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  mid    |   manem |
        // +---------+---------+---------+---------+
        Entity bill = schema.addEntity("Bill");
        bill.addIdProperty();
        bill.addStringProperty("createdTime");
        bill.addStringProperty("tableName");
        bill.addStringProperty("customerNumber");
        bill.addStringProperty("openTime");
        bill.addIntProperty("Total");
        bill.addIntProperty("discount");
        bill.addIntProperty("serviceFee");
        bill.addIntProperty("caseReceived");
        bill.addIntProperty("debitReceived");
        bill.addIntProperty("visaReceived");
        bill.addIntProperty("masterReceived");
        bill.addStringProperty("otherReceived");
        bill.addIntProperty("tip");
        bill.addIntProperty("cashback");
        bill.addIntProperty("userId");
        bill.addStringProperty("comment");
        bill.addIntProperty("invoiceId");

        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity customization = schema.addEntity("Customization");
        customization.addIdProperty();
        customization.addStringProperty("key");
        customization.addStringProperty("value");
        customization.addStringProperty("type");

        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity websrmBatch = schema.addEntity("WebsrmBatch");
        websrmBatch.addIdProperty();
        websrmBatch.addStringProperty("jsonToSend");
        websrmBatch.addStringProperty("signatransm");
        websrmBatch.addStringProperty("emprCertifSev");
        websrmBatch.addStringProperty("strUrlQRCode");
        websrmBatch.addIntProperty("employeeId");

        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity device = schema.addEntity("Device");//在线设备
        device.addStringProperty("ip");
        device.addStringProperty("userName");

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  categoryId    |   manem |
        // +---------+---------+---------+---------+
        //  @table Log
        Entity log = schema.addEntity("Log");
        log.setTableName("Logs");
        log.addIdProperty();
        log.addStringProperty("msg");
        log.addStringProperty("uName");
        log.addDateProperty("date");
        log.addIntProperty("logType");

        //-------------添加,state,version---------------
        for (Entity en : schema.getEntities()) {
            en.addIntProperty("state").columnName("STATE");// default:0, del:1,
            en.addIntProperty("version").columnName("VERSION");
        }
    }

    protected String getOutputPath() {
        return "../justprintdb/src/main/java/";
    }
}
