package com.cufe.deepweb;

import com.cufe.deepweb.common.orm.model.Current;
import com.cufe.deepweb.common.orm.model.WebSite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Constant {
    private Constant() { }

    /**
     * 网站的基本配置信息
     */
    public static WebSite webSite;
    /**
     * 网站的当前状态配置信息
     */
    public static Current current;
    /**
     * 当前爬虫所处轮次
     */
    public static int round;
    /**
     * 全文索引相对地址
     */
    public final static String FT_INDEX_ADDR="index/fulltext/";
    /**
     * 下载的资源文件相对地址，不仅仅包括HTML
     */
    public final static String HTML_ADDR = "html/";
    /**
     * 重启信息的相对地址
     */
    public final static String DATA_ADDR = "data/";
    /**
     * 全文索引的field name
     */
    public final static String FT_INDEX_FIELD = "fulltext";

    /**
     * 爬虫能识别的文本类型
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
     * 爬虫会建立索引的field和xpaht对
     */
    public final static Map<String, String> patternMap = new HashMap<String, String>() {
        {
            put(FT_INDEX_FIELD,"//body");
        }
    };

    /**
     * status表的链接类型：信息链接
     */
    public final static String STATUS_TYPE_INFO = "info";
    /**
     * status表的链接类型：查询链接
     */
    public final static String STATUS_TYPE_QUERY = "query";
    /**
     * current表中的爬虫阶段状态
     */
    public final static String CURRENT_STATUS_INACTIVE = "inactive";
    public final static String CURRENT_STATUS_ACTIVE = "active";
    public final static String CURRENT_STATUS_DONE = "done";
}
