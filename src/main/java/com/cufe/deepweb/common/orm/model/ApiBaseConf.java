package com.cufe.deepweb.common.orm.model;

public class ApiBaseConf {

    private long id;
    private long webId;
    private String prefix;

    private String inputXpath;
    /**
     * the address of search button，or an empty string if use keyboard enter
     */
    private String submitXpath;

    /**
     * the xpath used to locate the address of infoLink, if an empty string, execute the default operation of link collector
     */
    private String infoLinkXpath;

    private String payloadXpath;

    public String getInputXpath() {
        return inputXpath;
    }

    public void setInputXpath(String inputXpath) {
        this.inputXpath = inputXpath;
    }

    public String getInfoLinkXpath() {
        return infoLinkXpath;
    }

    public void setInfoLinkXpath(String infoLinkXpath) {
        this.infoLinkXpath = infoLinkXpath;
    }

    public String getSubmitXpath() {
        return submitXpath;
    }

    public void setSubmitXpath(String submitXpath) {
        this.submitXpath = submitXpath;
    }
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

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

    public String getPayloadXpath() {
        return payloadXpath;
    }

    public void setPayloadXpath(String payloadXpath) {
        this.payloadXpath = payloadXpath;
    }
}
