package com.just.print.db.bean;

public class Mark {

    private Long id;
    private String name1;
    private String name2;
    private String name3;
    private Integer type;
    private Integer status;
    private Integer version;

    public boolean select;

    private transient int qt;
    public int getQt() {
        return qt;
    }
    public void setQt(int num) {
        this.qt = num;
    }
    // KEEP FIELDS END

    public Mark() {
    }

    public Mark(Long id) {
        this.id = id;
    }

    public Mark(Long id, String name1, String name2, String name3, Integer type, Integer status, Integer version) {
        this.id = id;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
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

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
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

    public Mark clone(){
        Mark mark = new Mark();
        mark.setName1(name1);
        mark.setName2(name2);
        mark.setName3(name3);
        mark.setId(id);
        mark.setStatus(status);
        mark.setQt(qt);
        mark.setType(type);
        mark.setVersion(version);
        return mark;
    }

}
