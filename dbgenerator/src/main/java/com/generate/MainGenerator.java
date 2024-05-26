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
        Entity user = schema.addEntity("USER");
        user.addIdProperty();
        user.addStringProperty("firstName").columnName("firstName");
        user.addStringProperty("secondName").columnName("secondName");
        user.addStringProperty("nickName").columnName("nickName");
        user.addIntProperty("lang");
        user.addStringProperty("createTime").columnName("createTime");
        user.addIntProperty("role");// 1 is the biggest
        user.addStringProperty("noTPS").columnName("noTPS");
        user.addStringProperty("noTVQ").columnName("noTVQ");
        user.addStringProperty("pwd");

        //  @table Printer
        //  @pk id
        // +---------+---------+---------+---------+
        // |    id   |   pname |   ip    |   note  |
        // +---------+---------+---------+---------+
        Entity printer = schema.addEntity("Printer");
        printer.addIdProperty();
        printer.addStringProperty("ip");
        printer.addStringProperty("name");
        printer.addIntProperty("style");
        printer.addIntProperty("category");
        printer.addIntProperty("langType").columnName("langType");

        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity dinningTable = schema.addEntity("DiningTable");
        dinningTable.addIdProperty();
        dinningTable.addStringProperty("name");
        dinningTable.addStringProperty("openTime").columnName("openTime");
        dinningTable.addIntProperty("displayIdx").columnName("displayIdx");
        dinningTable.addIntProperty("posX").columnName("posX");
        dinningTable.addIntProperty("posY").columnName("posY");
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
        category.addIntProperty("displayIdx").columnName("displayIdx");

        //  @table Menu
        //  @pk id
        //  @fk pid, category
        //  pid->Printer.id;
        //  category->Category.id;
        // +---------+---------+---------+---------+
        // |    id   |   pid   |  category    |   manem |
        // +---------+---------+---------+---------+
        Entity dish = schema.addEntity("Dish");//category->dish
        dish.addIdProperty();
        dish.addStringProperty("name1").columnName("NAME1");
        dish.addStringProperty("name2").columnName("NAME2");
        dish.addStringProperty("name3").columnName("NAME3");
        dish.addFloatProperty("price");
        dish.addIntProperty("gst");
        dish.addIntProperty("qst");
        dish.addIntProperty("size");
        dish.addStringProperty("printerIds").columnName("printerIds");
        dish.addIntProperty("displayIdx").columnName("displayIdx");//size
        dish.addBooleanProperty("pricePomp").columnName("pricePomp");//size
        dish.addBooleanProperty("menuPomp").columnName("menuPomp");//size
        dish.addBooleanProperty("ModifyPomp").columnName("ModifyPomp");//size
        dish.addStringProperty("category").columnName("category");

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  mid    |   manem |
        // +---------+---------+---------+---------+
        // printer and dish are multi to multi, add a dish table.
        Entity menuPrint = schema.addEntity("M2M_MenuPrint");
        menuPrint.addIdProperty();
        Property.PropertyBuilder printerId = menuPrint.addLongProperty("printerId").columnName("printerId");
        Property.PropertyBuilder menuId = menuPrint.addLongProperty("dishId").columnName("dishId");
        menuPrint.addToOne(printer, printerId.getProperty(), "print");
        menuPrint.addToOne(dish, menuId.getProperty(), "dish");
        dish.addToMany(menuPrint, menuId.getProperty());

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  category    |   manem |
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
        saleRecord.addStringProperty("tableName").columnName("tableName");
        saleRecord.addStringProperty("customerNumber").columnName("customerNumber");
        saleRecord.addStringProperty("openTime").columnName("openTime");

        saleRecord.addIntProperty("productId").columnName("productId");
        saleRecord.addFloatProperty("amount");
        saleRecord.addFloatProperty("totalPrice").columnName("totalPrice");
        saleRecord.addFloatProperty("discount");
        saleRecord.addStringProperty("modifications");
        saleRecord.addIntProperty("userId").columnName("userId");
        saleRecord.addIntProperty("billId").columnName("billId");
        saleRecord.addStringProperty("createdTime").columnName("createdTime");

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  mid    |   manem |
        // +---------+---------+---------+---------+
        // printer and dish are multi to multi, add a dish table.
        Entity saleRecordMark = schema.addEntity("M2M_SaleRecordMark");
        saleRecordMark.addIdProperty();
        Property.PropertyBuilder saleRecId = saleRecordMark.addLongProperty("saleRecId").columnName("saleRecId");
        Property.PropertyBuilder markId = saleRecordMark.addLongProperty("markId").columnName("markId");
        saleRecordMark.addToOne(saleRecord, saleRecId.getProperty(), "saleRecord");
        saleRecordMark.addToOne(mark, markId.getProperty(), "mark");
        dish.addToMany(saleRecordMark, markId.getProperty());

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  mid    |   manem |
        // +---------+---------+---------+---------+
        Entity bill = schema.addEntity("Bill");
        bill.addIdProperty();
        bill.addStringProperty("createdTime").columnName("createdTime");
        bill.addStringProperty("tableName").columnName("tableName");
        bill.addStringProperty("customerNumber").columnName("customerNumber");
        bill.addStringProperty("openTime").columnName("openTime");
        bill.addIntProperty("Total");
        bill.addIntProperty("discount");
        bill.addIntProperty("serviceFee").columnName("serviceFee");
        bill.addIntProperty("cashReceived").columnName("cashReceived");
        bill.addIntProperty("debitReceived").columnName("debitReceived");
        bill.addIntProperty("visaReceived").columnName("visaReceived");
        bill.addIntProperty("masterReceived").columnName("masterReceived");
        bill.addStringProperty("otherReceived").columnName("otherReceived");
        bill.addIntProperty("tip");
        bill.addIntProperty("cashback");
        bill.addIntProperty("userId").columnName("userId");
        bill.addStringProperty("comment");
        bill.addIntProperty("invoiceId").columnName("invoiceId");

        // +---------+-----------+Customizations+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity customizations = schema.addEntity("Customizations");
        customizations.addIdProperty();
        customizations.addStringProperty("key");
        customizations.addStringProperty("value");
        customizations.addStringProperty("type");


        // +---------+-----------+Customizations+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity customizedRule = schema.addEntity("CustomizedRule");
        customizedRule.addIdProperty();
        customizedRule.addStringProperty("rulename");
        customizedRule.addStringProperty("content");
        customizedRule.addIntProperty("action");
        customizedRule.addIntProperty("dispIdx");
        customizedRule.addStringProperty("ext1");
        customizedRule.addStringProperty("ext2");
        customizedRule.addStringProperty("ext3");

        // +---------+--------WebsrmBatch---+------------+---------+
        // |    id   |   mname   |  number    |   price |
        // +---------+-----------+------------+---------+
        Entity websrmBatch = schema.addEntity("WebsrmBatch");
        websrmBatch.addIdProperty();
        websrmBatch.addStringProperty("jsonToSend").columnName("jsonToSend");
        websrmBatch.addStringProperty("signatransm");
        websrmBatch.addStringProperty("emprCertifSev").columnName("emprCertifSev");
        websrmBatch.addStringProperty("strUrlQRCode").columnName("strUrlQRCode");
        websrmBatch.addIntProperty("userId").columnName("userId");

        // +---------+-----------+------------+---------+
        // |    id   |   mname   |  number    |   Device |
        // +---------+-----------+------------+---------+
        Entity device = schema.addEntity("Device");//在线设备
        device.addStringProperty("ip");
        device.addStringProperty("userName").columnName("userName");

        // +---------+---------+---------+---------+
        // |    id   |   pid   |  category   |   manem |
        // +---------+---------+---------+---------+
        //  @table Log
        Entity log = schema.addEntity("Log");
        log.setTableName("Logs");
        log.addIdProperty();
        log.addStringProperty("msg");
        log.addStringProperty("name").columnName("name");
        log.addDateProperty("date");
        log.addIntProperty("logType").columnName("logType");

        //-------------添加,state,version---------------
        for (Entity en : schema.getEntities()) {
            en.addIntProperty("status");// default:0, del:1,
            en.addIntProperty("version");
        }
    }

    protected String getOutputPath() {
        return "../justprintdb/src/main/java/";
    }
}
