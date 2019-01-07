package com.cufe.deepweb.common.http.simulate;

import java.net.URL;
import java.util.List;

/**
 * the abstract class of link collector
 * this class defines the the collector's behaviour
 * the implementation of this class must be thread-safe
 */
public abstract class LinkCollector {
    /**
     * collect the links fit this collector's demand
     * @param content
     * @param url the URL which could get this content
     * @return
     */
    public abstract List<String> collect(String content, URL url);
}
