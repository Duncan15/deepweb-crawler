package com.cufe.deepweb.common.orm.model;

/**
 * 索引Pattern
 */
public class Pattern {
    private Long id;
    private Long webId;
    private String patternName;
    private String xpath;

    /**
     * 唯一标示ID
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 对应的webID
     */
    public Long getWebId() {
        return webId;
    }

    public void setWebId(Long webId) {
        this.webId = webId;
    }

    /**
     * 索引fieldName
     */
    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    /**
     * 索引内容对应的xpath
     */
    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
}
