package com.cufe.deepweb.common.orm.model;

public class JsonBaseConf {
    private long id;

    public long getWebId() {
        return webId;
    }

    public void setWebId(long webId) {
        this.webId = webId;
    }

    private long webId;

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
     * use JsonPointer to locate the address of total page number
     */
    private String totalAddress;


    /**
     * use JsonPointer to locate the address of content array
     */
    private String contentAddress;


    /**
     * the rule indicates that how to build infoLink from a unit of content array
     */
    private String linkRule;

    /**
     * the rule indicates that how to build payload content from a unit of content array
     */
    private String payloadRule;


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

    public String getContentAddress() {
        return contentAddress;
    }

    public void setContentAddress(String contentAddress) {
        this.contentAddress = contentAddress;
    }

    public String getLinkRule() {
        return linkRule;
    }

    public void setLinkRule(String linkRule) {
        this.linkRule = linkRule;
    }

    public String getPayloadRule() {
        return payloadRule;
    }

    public void setPayloadRule(String payloadRule) {
        this.payloadRule = payloadRule;
    }
}
