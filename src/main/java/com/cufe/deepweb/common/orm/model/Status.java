package com.cufe.deepweb.common.orm.model;

public class Status {
    private Long webId;
    private Long statusId;//status表ID
    private String round;//当前轮次
    private String type;//类型
    private Integer fLinkNum;//该轮成功爬取链接数量
    private Integer sLinkNum;//该轮失败爬取链接数量

    public Long getWebId() {
        return webId;
    }

    public void setWebId(Long webId) {
        this.webId = webId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getfLinkNum() {
        return fLinkNum;
    }

    public void setfLinkNum(Integer fLinkNum) {
        this.fLinkNum = fLinkNum;
    }

    public Integer getsLinkNum() {
        return sLinkNum;
    }

    public void setsLinkNum(Integer sLinkNum) {
        this.sLinkNum = sLinkNum;
    }
}
