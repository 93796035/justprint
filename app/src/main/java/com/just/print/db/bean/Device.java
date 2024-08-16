package com.just.print.db.bean;

public class Device {

    private String ip;
    private String userName;
    private Integer status;
    private Integer version;

    public Device() {
    }

    public Device(String ip, String userName, Integer status, Integer version) {
        this.ip = ip;
        this.userName = userName;
        this.status = status;
        this.version = version;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
