package com.just.print.db.bean;

public class DiningTable {

    private Long id;
    private String name;
    private String openTime;
    private Integer displayIdx;
    private Integer posX;
    private Integer posY;
    private Integer width;
    private Integer height;
    private Integer type;
    private Integer status;
    private Integer version;

    public DiningTable() {
    }

    public DiningTable(Long id) {
        this.id = id;
    }

    public DiningTable(Long id, String name, String openTime, Integer displayIdx, Integer posX, Integer posY, Integer width, Integer height, Integer type, Integer status, Integer version) {
        this.id = id;
        this.name = name;
        this.openTime = openTime;
        this.displayIdx = displayIdx;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public Integer getDisplayIdx() {
        return displayIdx;
    }

    public void setDisplayIdx(Integer displayIdx) {
        this.displayIdx = displayIdx;
    }

    public Integer getPosX() {
        return posX;
    }

    public void setPosX(Integer posX) {
        this.posX = posX;
    }

    public Integer getPosY() {
        return posY;
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
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

    @Override
    public String toString() {
        return name; // 返回桌子的名称或其他唯一标识符
    }

}
