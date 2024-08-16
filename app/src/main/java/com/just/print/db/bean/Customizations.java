package com.just.print.db.bean;

public class Customizations {

    private Long id;
    private String key;
    private String value;
    private String type;
    private Integer status;
    private Integer version;

    public Customizations() {
    }

    public Customizations(Long id) {
        this.id = id;
    }

    public Customizations(Long id, String key, String value, String type, Integer status, Integer version) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.type = type;
        this.status = status;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
