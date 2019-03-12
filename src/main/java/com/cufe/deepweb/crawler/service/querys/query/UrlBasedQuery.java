package com.cufe.deepweb.crawler.service.querys.query;

public class UrlBasedQuery extends Query {
    private String url;
    UrlBasedQuery(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
