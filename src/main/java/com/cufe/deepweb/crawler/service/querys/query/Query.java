package com.cufe.deepweb.crawler.service.querys.query;


public class Query {
    public static UrlBasedQuery asUrlBased(String url) {
        return new UrlBasedQuery(url);
    }
    public static ApiBasedQuery asApiBased(String url, String inputXpath, String submitXpath, String keyword) {
        return new ApiBasedQuery(url, inputXpath, submitXpath, keyword);
    }
}
