package com.cufe.deepweb.common.orm.model;

/**
 * record the current status of crawler
 * primary key is (webId, round)
 */
public class Current {
    private long webId;
    /**
     * the round number of current round, for backward compatible
     * this column should be a string
     */
    private String round;

    /**
     * the four status of a round
     * this design just for backword compatible
     */
    private String M1status;
    private String M2status;
    private String M3status;
    private String M4status;

    /**
     * currently the downloaded data number
     */
    private Integer SampleData_sum;

    public Integer getRun() {
        return run;
    }

    public void setRun(Integer run) {
        this.run = run;
    }

    /**
     * the running status of current crawler
     * 0: no run
     * 1: run
     */
    private Integer run;


    public Long getWebId() {
        return webId;
    }

    public void setWebId(Long webId) {
        this.webId = webId;
    }


    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }


    public String getM1status() {
        return M1status;
    }

    public void setM1status(String m1status) {
        M1status = m1status;
    }


    public String getM2status() {
        return M2status;
    }

    public void setM2status(String m2status) {
        M2status = m2status;
    }


    public String getM3status() {
        return M3status;
    }

    public void setM3status(String m3status) {
        M3status = m3status;
    }


    public String getM4status() {
        return M4status;
    }

    public void setM4status(String m4status) {
        M4status = m4status;
    }


    public Integer getSampleData_sum() {
        return SampleData_sum;
    }

    public void setSampleData_sum(Integer sampleData_sum) {
        SampleData_sum = sampleData_sum;
    }
}
