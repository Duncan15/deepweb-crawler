package com.cufe.deepweb.common.orm.model;

public class ApiBaseConf {

    private long id;
    private long webId;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String prefix;
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
}
