package com.just.print.db.bean;

public class Category {

    private Long id;
    private String name1;
    private String name2;
    private String name3;
    private Integer displayIdx;
    private Integer status;
    private Integer version;

    public Category() {
    }

    public Category(Long id) {
        this.id = id;
    }

    public Category(Long id, String name1, String name2, String name3, Integer displayIdx, Integer status, Integer version) {
        this.id = id;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.displayIdx = displayIdx;
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

    public Integer getDisplayIdx() {
        return displayIdx;
    }

    public void setDisplayIdx(Integer displayIdx) {
        this.displayIdx = displayIdx;
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


    private String language[] = new String[3];

    public String getLanguage(int i) {
        if(i >= language.length || "null".equalsIgnoreCase(language[i]) || language[i] == null || language[i].length() == 0) {
            return language[0];
        }
        return language[i];
    }
    public void setLanguage(String language[]) {
        this.language = language;
        this.name1 = language[0];
    }
    // KEEP METHODS END

}
