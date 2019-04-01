package com.cufe.deepweb.common.orm.model;

public class JsonBaseConf {
    private long id;
    private String prefix;
    /**
     * the HTTP method to access the prefix, such as GET or POST
     */
    private String method;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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
}
