package com.cufe.deepweb.common.orm.model;

public class UrlBaseConf {
    private long id;

    private long webId;

    /**
     * the prefix of search URL
     * （the part before ?）
     */
    private String prefix;
    /**
     * the keyword parameter name
     */
    private String paramQuery;
    /**
     * the pageNum parameter name
     * if there exists pageNum and pageSize, then append the pageSize parameter into paramList,
     * because the pageSize parameter's value has no need to change in crawling
     */
    private String paramPage;
    /**
     * include comma(,), following is the format:
     * startPageNum,pageInterval
     */
    private String startPageNum;
    /**
     * the other parameters' name, divided by comma(,)
     */
    private String paramList;
    /**
     * the other parameters' value corresponding to the paramList, divided by comma(,)
     */
    private String paramValueList;

    public String getInfoLinkXpath() {
        return infoLinkXpath;
    }

    public void setInfoLinkXpath(String infoLinkXpath) {
        this.infoLinkXpath = infoLinkXpath;
    }

    /**
     * assistant parameter to help to locate the info-link's address, this parameter can be empty
     */
    private String infoLinkXpath;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWebId() {
        return webId;
    }

    public void setWebId(long webId) {
        this.webId = webId;
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

    public String getStartPageNum() {
        return startPageNum;
    }

    public void setStartPageNum(String startPageNum) {
        this.startPageNum = startPageNum;
    }

    public String getParamList() {
        return paramList;
    }

    public void setParamList(String paramList) {
        this.paramList = paramList;
    }

    public String getParamValueList() {
        return paramValueList;
    }

    public void setParamValueList(String paramValueList) {
        this.paramValueList = paramValueList;
    }
}
