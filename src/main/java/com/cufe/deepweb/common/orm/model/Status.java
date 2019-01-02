package com.cufe.deepweb.common.orm.model;

/**
 * record the history round information
 * primary key is (webId, round, type)
 */
public class Status {
    private Long webId;
    /**
     * the unique id of status table
     */
    private Long statusId;

    /**
     * the round of current row corresponding to
     */
    private String round;

    /**
     * the record type: info or query
     */
    private String type;

    /**
     * the number of infoLink which is downloaded successfully in this round
     */
    private Integer fLinkNum;

    /**
     * the number of infoLink which is failed to download in this round
     */
    private Integer sLinkNum;

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
