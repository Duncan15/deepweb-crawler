package com.cufe.deepweb.crawler.service.querys;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.http.simulate.WebBrowser;

public class JsonBaseQueryLinkService extends QueryLinkService {
    private CusHttpClient httpClient;
    public JsonBaseQueryLinkService(WebBrowser browser, Deduplicator dedu, CusHttpClient httpClient) {
        super(browser, dedu);
        this.httpClient = httpClient;
    }
}
