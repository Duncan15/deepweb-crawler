package com.cufe.deepweb.common.orm.model;

/**
 * the pattern for building index
 */
public class Pattern {
    private long id;
    private long webId;
    /**
     * the pattern name for showing in front end
     * and this value would be used as the field name in building index
     */
    private String patternName;

    /**
     * the xpath of this pattern
     */
    private String xpath;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getWebId() {
        return webId;
    }

    public void setWebId(Long webId) {
        this.webId = webId;
    }


    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
}
