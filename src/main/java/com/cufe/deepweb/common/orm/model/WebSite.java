package com.cufe.deepweb.common.orm.model;

public class WebSite {
    /**
     * the unique ID of website
     */
    private long webId;
    /**
     * the name of website
     * this column just for easy to distinct the website in front end
     */
    private String webName;
    /**
     * the URL of website's home page
     * this column temperately no use
     */
    private String indexUrl;

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

    /**
     * this column temperately no use
     */
    private String runningMode;


    /**
     * the work directory of website,
     * if the directory no exists, the program can't success to startup
     * the program promise that when this value is got from database, it's end with /
     */
    private String workFile;



    private Short driver;
    private Short usable;

    /**
     * the createtime of this row
     */
    private String createtime;
    private String creator;

    public long getWebId() {
        return this.webId;
    }

    public void setWebId(long webId) {
        this.webId = webId;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
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

    public String getRunningMode() {
        return runningMode;
    }

    public void setRunningMode(String runningMode) {
        this.runningMode = runningMode;
    }

    public String getWorkFile() {
        return workFile;
    }

    public void setWorkFile(String workFile) {
        this.workFile = workFile;
    }

    public short getDriver() {
        return driver;
    }

    public void setDriver(short driver) {
        this.driver = driver;
    }

    public short getUsable() {
        return usable;
    }

    public void setUsable(short usable) {
        this.usable = usable;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

}
