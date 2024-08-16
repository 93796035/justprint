package com.just.print.db.bean;
public class User {

    private Long id;
    private String name;
    private String familyName;
    private String nickName;
    private String address;
    private Integer lang;
    private String joinTime;
    private Integer role;
    private String noTPS;
    private String noTVQ;
    private String pwd;
    private String ANNIVERSARY;
    private Integer status;
    private Integer version;

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    public User(Long id, String name, String familyName, String nickName, String address, Integer lang, String joinTime, Integer role, String noTPS, String noTVQ, String pwd, String ANNIVERSARY, Integer status, Integer version) {
        this.id = id;
        this.name = name;
        this.familyName = familyName;
        this.nickName = nickName;
        this.address = address;
        this.lang = lang;
        this.joinTime = joinTime;
        this.role = role;
        this.noTPS = noTPS;
        this.noTVQ = noTVQ;
        this.pwd = pwd;
        this.ANNIVERSARY = ANNIVERSARY;
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

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getLang() {
        return lang;
    }

    public void setLang(Integer lang) {
        this.lang = lang;
    }

    public String getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(String joinTime) {
        this.joinTime = joinTime;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getNoTPS() {
        return noTPS;
    }

    public void setNoTPS(String noTPS) {
        this.noTPS = noTPS;
    }

    public String getNoTVQ() {
        return noTVQ;
    }

    public void setNoTVQ(String noTVQ) {
        this.noTVQ = noTVQ;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getANNIVERSARY() {
        return ANNIVERSARY;
    }

    public void setANNIVERSARY(String ANNIVERSARY) {
        this.ANNIVERSARY = ANNIVERSARY;
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
