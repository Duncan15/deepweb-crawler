package com.cufe.deepweb.common.orm.model;

/**
 * 记录爬虫的当前状态
 */
public class Current {
    private Long webId;
    private String round;
    private String M1status;
    private String M2status;
    private String M3status;
    private String M4status;
    private Integer SampleData_sum;

    public Integer getfQueryLink_sum() {
        return this.fQueryLink_sum;
    }

    public void setfQueryLink_sum(Integer fQueryLink_sum) {
        this.fQueryLink_sum = fQueryLink_sum;
    }

    private Integer fQueryLink_sum;

    public Integer getfInfoLink_sum() {
        return this.fInfoLink_sum;
    }

    public void setfInfoLink_sum(Integer fInfoLink_sum) {
        this.fInfoLink_sum = fInfoLink_sum;
    }

    private Integer fInfoLink_sum;

    public Long getWebId() {
        return webId;
    }

    public void setWebId(Long webId) {
        this.webId = webId;
    }

    /**
     * 当前所处轮次
     */
    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    /**
     * 爬虫M1阶段的状态（生成关键词阶段）
     */
    public String getM1status() {
        return M1status;
    }

    public void setM1status(String m1status) {
        M1status = m1status;
    }

    /**
     * 爬虫M2阶段的状态（确定分页链接阶段）
     */
    public String getM2status() {
        return M2status;
    }

    public void setM2status(String m2status) {
        M2status = m2status;
    }

    /**
     * 爬虫M3阶段的状态（下载阶段）
     */
    public String getM3status() {
        return M3status;
    }

    public void setM3status(String m3status) {
        M3status = m3status;
    }

    /**
     * 爬虫M4阶段的状态（收尾工作）
     */
    public String getM4status() {
        return M4status;
    }

    public void setM4status(String m4status) {
        M4status = m4status;
    }

    /**
     * 当前爬取总量
     */
    public Integer getSampleData_sum() {
        return SampleData_sum;
    }

    public void setSampleData_sum(Integer sampleData_sum) {
        SampleData_sum = sampleData_sum;
    }
}
