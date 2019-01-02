package com.cufe.deepweb.crawler;

import com.cufe.deepweb.common.orm.model.Current;
import com.cufe.deepweb.common.orm.model.WebSite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Constant {
    private Constant() { }

    /**
     * the basic configuration of website
     */
    public static WebSite webSite;
    /**
     * the current information of website
     */
    public static Current current;
    /**
     * the round at which crawler currently stands
     */
    public static int round;
    /**
     * the relative address of fulltext index
     */
    public final static String FT_INDEX_ADDR="index/fulltext/";
    /**
     * the downloaded resource's relative address，not only includes HTML
     */
    public final static String HTML_ADDR = "html/";
    /**
     * the relative address of data which is stored for restarting
     */
    public final static String DATA_ADDR = "data/";
    /**
     * the field name of fulltext index
     */
    public final static String FT_INDEX_FIELD = "fulltext";

    /**
     * the document type which crawler can understand
     */
    public final static Set<String> docTypes = new HashSet<String>() {
        {
            add(".pdf");
            add(".doc");
            add(".docx");
            add(".xls");
            add((".xlsx"));
            add(".html");
        }
    };
    /**
     * the field name and xpath map which crawler builds at startup
     */
    public final static Map<String, String> patternMap = new HashMap<String, String>() {
        {
            put(FT_INDEX_FIELD,"//body");
        }
    };

    /**
     * the link type in status table：info link
     */
    public final static String STATUS_TYPE_INFO = "info";
    /**
     * the link type in status table：query link
     */
    public final static String STATUS_TYPE_QUERY = "query";
    /**
     * the crawler status in current table
     */
    public final static String CURRENT_STATUS_INACTIVE = "inactive";
    public final static String CURRENT_STATUS_ACTIVE = "active";
    public final static String CURRENT_STATUS_DONE = "done";

    /**
     * the size of message queue for downloading data
     */
    public final static int QUEUE_SIZE = 10_000;
}
