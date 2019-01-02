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
     * the specified download thread number, default number is 5
     */
    private Integer threadNum;

    /**
     * the specified timeout in HTTP communication
     * this parameter's meaning is a little indistinct
     * the reality behavior would't absolutely obey this timeout
     * default value is 3000
     */
    private Integer timeout;

    /**
     * the specified charset of the crawled website
     * this value is very important in crawling
     * so in crawling the program would first to auto detect the website's charset
     * only when can't find it, the program would use this value
     */
    private String charset;

    /**
     * the work directory of website,
     * if the directory no exists, the program can't success to startup
     * the program promise that when this value is got from database, it's end with /
     */
    private String workFile;



    /**
     * the username input's Xpath
     */
    private String userParam;

    /**
     * the password input's Xpath
     */
    private String pwdParam;

    /**
     * the submit button's Xpath
     */
    private String submitXpath;

    /**
     * the username for dynamic login
     */
    private String userName;
    /**
     * the password for dynamic login
     */
    private String password;
    /**
     * the URL of login page
     * if this URL is not null, then the userParam,pwdParam,submitXpath,userName,password can't be null
     */
    private String loginUrl;

    /**
     * this column temperately no use
     */
    private Integer databaseSize;


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

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getWorkFile() {
        return workFile;
    }

    public void setWorkFile(String workFile) {
        this.workFile = workFile;
    }

    public String getUserParam() {
        return userParam;
    }

    public void setUserParam(String userParam) {
        this.userParam = userParam;
    }

    public String getPwdParam() {
        return pwdParam;
    }

    public void setPwdParam(String pwdParam) {
        this.pwdParam = pwdParam;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public Integer getDatabaseSize() {
        return databaseSize;
    }

    public void setDatabaseSize(int databaseSize) {
        this.databaseSize = databaseSize;
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
    public String getSubmitXpath() {
        return this.submitXpath;
    }

    public void setSubmitXpath(String submitXpath) {
        this.submitXpath = submitXpath;
    }

}
