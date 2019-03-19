package com.cufe.deepweb.crawler.service.infos.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Info implements Serializable {
    private Logger logger = LoggerFactory.getLogger(Info.class);
    /**
     * the url representing this info-link
     */
    private String url;

    /**
     * the additional information for the link
     */
    private Map<String, String> payload;

    private Info(String link) {
        this.url = link;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getPayload() {
        return this.payload;
    }

    public static Info link(String link) {
        return new Info(link);
    }

    public Info addPayLoad(String field, String content) {
        if (this.payload == null) {
            this.payload = new HashMap<>();
        }
        this.payload.put(field, content);
        return this;
    }
    public Info fixUrl(URL url) {
        if (this.url.startsWith(".") || this.url.startsWith("/")) {
            try {
                this.url = new URL(url, this.url).toString();
            } catch (MalformedURLException ex) {
                logger.error("url {} format error", this.url);
                //if happen format error, just jump over this href
            }
        }
        return this;
    }
}
