package com.just.print.db.bean;

/**
 * Entity mapped to table PRINTER.
 */
public class Printer {

    private Long id;
    private String ip;
    private String name;
    private Integer style;
    private Integer category;
    private Integer langType;
    private Integer status;
    private Integer version;

    public Printer() {
    }

    public Printer(Long id) {
        this.id = id;
    }

    public Printer(Long id, String ip, String name, Integer style, Integer category, Integer langType, Integer status, Integer version) {
        this.id = id;
        this.ip = ip;
        this.name = name;
        this.style = style;
        this.category = category;
        this.langType = langType;
        this.status = status;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStyle() {
        return style;
    }

    public void setStyle(Integer style) {
        this.style = style;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getLangType() {
        return langType;
    }

    public void setLangType(Integer langType) {
        this.langType = langType;
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
    public boolean equals(Object o) {
        if (o instanceof Printer) {
            return ((Printer) o).getId() == getId();
        } else return false;
    }
    // KEEP METHODS END

}
