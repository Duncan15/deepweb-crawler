package com.cufe.deepweb.crawler.service.querys;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.http.simulate.LinkCollector;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.crawler.service.LinkService;

public abstract class QueryLinkService extends LinkService {
    protected WebBrowser browser;
    protected Deduplicator dedu;
    /**
     * the link collector to collect info link
     */
    protected LinkCollector collector;

    public QueryLinkService(WebBrowser browser, Deduplicator dedu) {
        this.browser = browser;
        this.dedu = dedu;
    }
    /**
     * export the browser used by this service
     * @return
     */
    public WebBrowser getWebBrowser() {
        return this.browser;
    }


    @Override
    public void clearThreadResource() {
        this.browser.clearResource();
    }
}
