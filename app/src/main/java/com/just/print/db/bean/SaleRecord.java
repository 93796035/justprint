package com.just.print.db.bean;

public class SaleRecord {

    private Long id;
    private String tableName;
    private String billIndex;
    private String openTime;
    private Integer productId;
    private Float amount;
    private Float totalPrice;
    private Float discount;
    private String modifications;
    private Integer userId;
    private Integer billId;
    private String createdTime;
    private String modifiedTime;
    private Integer status;
    private Integer version;

    public SaleRecord() {
    }

    public SaleRecord(Long id) {
        this.id = id;
    }

    public SaleRecord(Long id, String tableName, String billIndex, String openTime, Integer productId, Float amount, Float totalPrice, Float discount, String modifications, Integer userId, Integer billId, String createdTime, String modifiedTime, Integer status, Integer version) {
        this.id = id;
        this.tableName = tableName;
        this.billIndex = billIndex;
        this.openTime = openTime;
        this.productId = productId;
        this.amount = amount;
        this.totalPrice = totalPrice;
        this.discount = discount;
        this.modifications = modifications;
        this.userId = userId;
        this.billId = billId;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.status = status;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getBillIndex() {
        return billIndex;
    }

    public void setBillIndex(String billIndex) {
        this.billIndex = billIndex;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount;
    }

    public String getModifications() {
        return modifications;
    }

    public void setModifications(String modifications) {
        this.modifications = modifications;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
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

}
