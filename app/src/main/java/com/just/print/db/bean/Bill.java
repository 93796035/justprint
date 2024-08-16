package com.just.print.db.bean;

import com.just.print.app.AikaPos;

public class Bill {

    private Long id;
    private String createdTime;
    private String tableName;
    private String billIndex;
    private String openTime;
    private Integer Total;
    private Integer discount;
    private Integer serviceFee;
    private Integer cashReceived;
    private Integer debitReceived;
    private Integer visaReceived;
    private Integer masterReceived;
    private String otherReceived;
    private Integer tip;
    private Integer cashback;
    private Integer userId;
    private String comment;
    private Integer invoiceId;
    private Integer status;
    private Integer version;

    public Bill() {
    }

    public Bill(Long id) {
        this.id = id;
    }

    public Bill(Long id, String createdTime, String tableName, String billIndex, String openTime, Integer Total, Integer discount, Integer serviceFee, Integer cashReceived, Integer debitReceived, Integer visaReceived, Integer masterReceived, String otherReceived, Integer tip, Integer cashback, Integer userId, String comment, Integer invoiceId, Integer status, Integer version) {
        this.id = id;
        this.createdTime = createdTime;
        this.tableName = tableName;
        this.billIndex = billIndex;
        this.openTime = openTime;
        this.Total = Total;
        this.discount = discount;
        this.serviceFee = serviceFee;
        this.cashReceived = cashReceived;
        this.debitReceived = debitReceived;
        this.visaReceived = visaReceived;
        this.masterReceived = masterReceived;
        this.otherReceived = otherReceived;
        this.tip = tip;
        this.cashback = cashback;
        this.userId = userId;
        this.comment = comment;
        this.invoiceId = invoiceId;
        this.status = status;
        this.version = version;
    }

    public static void deleteAll() {
        String sql = "delete from bill";
        AikaPos.db.execSQL(sql);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
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

    public Integer getTotal() {
        return Total;
    }

    public void setTotal(Integer Total) {
        this.Total = Total;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(Integer serviceFee) {
        this.serviceFee = serviceFee;
    }

    public Integer getCashReceived() {
        return cashReceived;
    }

    public void setCashReceived(Integer cashReceived) {
        this.cashReceived = cashReceived;
    }

    public Integer getDebitReceived() {
        return debitReceived;
    }

    public void setDebitReceived(Integer debitReceived) {
        this.debitReceived = debitReceived;
    }

    public Integer getVisaReceived() {
        return visaReceived;
    }

    public void setVisaReceived(Integer visaReceived) {
        this.visaReceived = visaReceived;
    }

    public Integer getMasterReceived() {
        return masterReceived;
    }

    public void setMasterReceived(Integer masterReceived) {
        this.masterReceived = masterReceived;
    }

    public String getOtherReceived() {
        return otherReceived;
    }

    public void setOtherReceived(String otherReceived) {
        this.otherReceived = otherReceived;
    }

    public Integer getTip() {
        return tip;
    }

    public void setTip(Integer tip) {
        this.tip = tip;
    }

    public Integer getCashback() {
        return cashback;
    }

    public void setCashback(Integer cashback) {
        this.cashback = cashback;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
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
