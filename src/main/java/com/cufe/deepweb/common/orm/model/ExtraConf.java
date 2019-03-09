package com.cufe.deepweb.common.orm.model;

public class ExtraConf {
    private long id;

    private long webId;

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
     * the username input's Xpath
     */
    private String userNameXpath;

    /**
     * the password input's Xpath
     */
    private String passwordXpath;

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

    public String getUserNameXpath() {
        return userNameXpath;
    }

    public void setUserNameXpath(String userNameXpath) {
        this.userNameXpath = userNameXpath;
    }

    public String getPasswordXpath() {
        return passwordXpath;
    }

    public void setPasswordXpath(String passwordXpath) {
        this.passwordXpath = passwordXpath;
    }

    public String getSubmitXpath() {
        return submitXpath;
    }

    public void setSubmitXpath(String submitXpath) {
        this.submitXpath = submitXpath;
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

    public void setDatabaseSize(Integer databaseSize) {
        this.databaseSize = databaseSize;
    }
}
