package com.cufe.deepweb.common.orm.model;

public class WebSite {
    /**
     * 网站的唯一标示ID
     */
    private long webId;
    /**
     * 网站名称
     */
    private String webName;
    /**
     * 网站首页URL
     */
    private String indexUrl;
    /**
     * 搜索链接前缀（？之前部分）
     */
    private String prefix;
    /**
     * 关键词参数名称
     */
    private String paramQuery;
    /**
     * 分页参数名称，如果分别有pageNum和pageSize，则把pageSize归入paramList
     */
    private String paramPage;
    /**
     * 包含, 格式如下：
     * startPageNum,pageInterval
     */
    private String startPageNum;
    /**
     * 其他参数名称，以,分割
     */
    private String paramList;
    /**
     * 其他参数值，以,分割
     */
    private String paramValueList;
    private String runningMode;
    /**
     * 指定的下载线程数量
     */
    private Integer threadNum;
    private Integer timeout;
    private String charset;
    /**
     * 数据库保证以/结尾
     */
    private String workFile;
    /**
     * 用户名输入框的Xpath
     */
    private String userParam;
    /**
     * 密码输入框的Xpath
     */
    private String pwdParam;

    /**
     * 提交按钮的Xpath
     */
    private String submitXpath;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 登录页面链接（如果此项不为空，则账号密码相关不能为空）
     */
    private String loginUrl;

    private Long databaseSize;
    private String driver;
    private String usable;
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

    public Long getDatabaseSize() {
        return databaseSize;
    }

    public void setDatabaseSize(Long databaseSize) {
        this.databaseSize = databaseSize;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUsable() {
        return usable;
    }

    public void setUsable(String usable) {
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
