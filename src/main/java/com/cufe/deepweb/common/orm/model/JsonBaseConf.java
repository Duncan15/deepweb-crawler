package com.cufe.deepweb.common.orm.model;

public class JsonBaseConf {
    private long id;
    private String prefix;
    /**
     * the keyword parameter name
     */
    private String paramQuery;
    /**
     * the page parameter name
     */
    private String paramPage;
    /**
     * the page strategy
     * include comma(,), following is the format:
     * startPageNum,pageInterval
     */
    private String pageStrategy;
    /**
     * the constant string in the query link
     */
    private String constString;

    /**
     * the address of total page number in the json response
     * example 1:
     * [0].num
     *
     * json:[{"num":100}]
     *
     * example 2:
     * total
     *
     * json:{"total":100, content:[]}
     */
    private String totalAddress;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getParamQuery() {
        return paramQuery;
    }

    public void setParamQuery(String paramQuery) {
        this.paramQuery = paramQuery;
    }

    public String getParamPage() {
        return paramPage;
    }

    public void setParamPage(String paramPage) {
        this.paramPage = paramPage;
    }

    public String getPageStrategy() {
        return pageStrategy;
    }

    public void setPageStrategy(String pageStrategy) {
        this.pageStrategy = pageStrategy;
    }

    public String getConstString() {
        return constString;
    }

    public void setConstString(String constString) {
        this.constString = constString;
    }

    public String getTotalAddress() {
        return totalAddress;
    }

    public void setTotalAddress(String totalAddress) {
        this.totalAddress = totalAddress;
    }
}
