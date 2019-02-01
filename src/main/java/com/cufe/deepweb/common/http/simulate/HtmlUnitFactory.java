package com.cufe.deepweb.common.http.simulate;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class HtmlUnitFactory extends BasePooledObjectFactory<WebClient> {
    private CookieManager cookieManager;
    private int timeout;
    public HtmlUnitFactory(CookieManager cookieManager, int timeout) {
        this.cookieManager = cookieManager;
        this.timeout = timeout;
    }
    @Override
    public WebClient create() {
        WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
        client.setCookieManager(cookieManager);
        client.getOptions().setCssEnabled(false);//headless browser no need to support css
        client.getOptions().setDownloadImages(false);//headless browser no need to support download imgs
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);//wouldn't log when access error
        client.getOptions().setThrowExceptionOnScriptError(false);//wouldn't log when js run error
        client.getOptions().setTimeout(timeout);//set the timeout for browser to connect
        client.getOptions().setDoNotTrackEnabled(true);
        client.getOptions().setHistoryPageCacheLimit(1);//limit the cache number
        client.getOptions().setHistorySizeLimit(1);
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        client.waitForBackgroundJavaScript(timeout);
        return client;
    }

    @Override
    public PooledObject<WebClient> wrap(WebClient obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(final PooledObject<WebClient> p)
            throws Exception  {
        p.getObject().close();
    }
}
