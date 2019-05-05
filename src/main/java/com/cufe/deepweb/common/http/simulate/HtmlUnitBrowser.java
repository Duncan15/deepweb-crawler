package com.cufe.deepweb.common.http.simulate;

import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.query.ApiBasedQuery;
import com.cufe.deepweb.crawler.service.querys.query.Query;
import com.cufe.deepweb.crawler.service.querys.query.UrlBasedQuery;
import com.cufe.deepweb.common.retry.Try;
import com.cufe.deepweb.crawler.Constant;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * the Browser implemented by HtmlUnit
 */
public final class HtmlUnitBrowser implements WebBrowser {
    private Logger logger = LoggerFactory.getLogger(HtmlUnitBrowser.class);
    static {
        //close HtmlUnit log output
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }
    private int timeout;
    private CookieManager cookieManager;
    private volatile boolean isLogin;
    ThreadLocal<WebClient> threadClient = new ThreadLocal<WebClient>() {
        @Override
        protected WebClient initialValue() {
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
            client.setJavaScriptTimeout(timeout);
            client.waitForBackgroundJavaScript(timeout);
            return client;
        }

        @Override
        public void remove() {
            get().close();
            super.remove();
        }
    };
    /**
     * initialize a new HtmlUnitBrowser
     */
    public HtmlUnitBrowser(CookieManager cookieManager, int timeout){
        this.timeout = timeout;
        this.cookieManager = cookieManager;
        this.isLogin = false;//if cookieManager has preserved the login information

    }



    /**
     * override WebBrowser's method
     * @param loginURL login page URL
     * @param username account name
     * @param password login password
     * @param usernameXpath the xpath of account input
     * @param passwordXpath the xpath of password input
     * @param submitXpath the xpath of submit button
     * @return
     */
    @Override
    public boolean login(String loginURL, String username, String password, String usernameXpath, String passwordXpath, String submitXpath) {
        WebClient client = null;
        try {
            client = threadClient.get();
            HtmlTextInput userNameInput = null;//the username input
            HtmlPasswordInput passwordInput = null;//the password input
            HtmlElement button = null;//the login button, probably no a button
            HtmlPage page = null;
            page = client.getPage(loginURL);
            if ((!StringUtils.isBlank(usernameXpath)) && (!StringUtils.isBlank(passwordXpath)) && (!StringUtils.isBlank(submitXpath))) {//如果xpath都有指定
                logger.info("start to login");
                List uList = page.getByXPath(usernameXpath);
                List pList = page.getByXPath(passwordXpath);
                List sList = page.getByXPath(submitXpath);
                if (!uList.isEmpty()) {
                    userNameInput = (HtmlTextInput)uList.get(0);
                    userNameInput.setText(username);
                    logger.info("get login username input");
                }
                if (!pList.isEmpty()) {
                    passwordInput = (HtmlPasswordInput)pList.get(0);
                    passwordInput.setText(password);
                    logger.info("get login password input");
                }
                if (!sList.isEmpty()) {
                    button = (HtmlElement) sList.get(0);
                    button.click();
                    client.waitForBackgroundJavaScript(3_000);
                    logger.info("get login submit button");
                    isLogin = true;//change the login status to login
                    return true;
                }
            }//必须指定xpath，否则无法登录，后续可拓展成指定id等等
            return false;
        } catch (Exception ex) {
            logger.error("Exception happen when get login page", ex);
            return false;
        }
    }

    @Override
    public Optional<String> getPageContent(String URL) {
        WebClient client = threadClient.get();
        HtmlElement body = new Try<HtmlElement>(3).run(() -> {
                HtmlElement ans = null;
                try {
                    ans = ((HtmlPage)client.getPage(URL)).getBody();
                } catch (Exception ex) {
                    logger.error("Exception happen when get page content from" + URL, ex);
                }
                return ans;

        });
        return Optional.ofNullable(body.asText());

    }

    public List<Info> getAllLinks(Query query, LinkCollector collector) {
        if (query instanceof UrlBasedQuery) {
            return getLinksFromUrlBasedQuery((UrlBasedQuery) query, collector);
        } else if (query instanceof ApiBasedQuery) {
            return getLinksFromApiBasedQuery((ApiBasedQuery) query, collector);
        } else {
            return Collections.emptyList();
        }
    }

    @Deprecated
    public List<String> getAllLinks(String URL) {
        List<String> links = new ArrayList<>();
        WebClient client = null;

        try {
            client = threadClient.get();
            HtmlPage page = client.getPage(URL);
            URL curURL = page.getUrl();//the URL of current page
            List<HtmlAnchor> anchors = page.getAnchors();
            for (HtmlAnchor anchor : anchors) {
                String anchorURL;
                String hrefAttr = anchor.getHrefAttribute().trim();
                if ("".equals(hrefAttr)) continue;//if the value of href is blank, just jump next

                if (!hrefAttr.startsWith("http")) {//if no start with http, this href is relative address or no use http protocol
                    if (hrefAttr.startsWith(".") || hrefAttr.startsWith("/")) {//if start with . or /, this href is relative address
                        anchorURL = new URL(curURL, hrefAttr).toString();
                    } else { //no use http protocol, just jump next
                        continue;
                    }
                } else {//if start with http, use this href directly
                    anchorURL = hrefAttr;
                }
                links.add(anchorURL);
            }
        }catch (IOException ex) {
            logger.error("IOException happen when get page content", ex);
        } catch (NullPointerException ex) {
            logger.error("NullPointerException happen when get page content", ex);
        } catch (Exception ex) {

        }
        return links;
    }

    @Override
    public void clearResource() {
        threadClient.remove();

    }

    private List<Info> getLinksFromUrlBasedQuery(UrlBasedQuery query, LinkCollector collector) {
        String URL = query.getUrl();
        WebClient client = threadClient.get();
        HtmlPage page = retryGetPage(client, URL);
        return collector.collect(page.asXml(), page.getUrl(), Constant.urlBaseConf.getInfoLinkXpath(), null);
    }
    private List<Info> getLinksFromApiBasedQuery(ApiBasedQuery query, LinkCollector collector) {
        WebClient client = threadClient.get();
        HtmlPage page = retryGetPage(client, query.getUrl());
        if (page == null) {
            return Collections.emptyList();
        }
        //if can't find the input, directly exist
        if (StringUtils.isBlank(Constant.apiBaseConf.getInputXpath())) {
            return Collections.emptyList();
        }

        HtmlTextInput input = null;//the keyword input
        HtmlElement button = null;// the submit button

        //search the input from the main page
        List inputList = page.getByXPath(Constant.apiBaseConf.getInputXpath());

        //if after search the main page and all the iframe, the inputList still is empty, exit
        if (inputList.isEmpty()) {
            logger.error("can't get keyword input by Xpath:{} in HTML:{}", Constant.apiBaseConf.getInputXpath(), page.asXml());
            return Collections.emptyList();
        }

        logger.info("get the keyword input");
        input = (HtmlTextInput) inputList.get(0);
        page.setFocusedElement(input);
        input.setText(query.getKeyword());


        //below has locate the main page or iframe, here is no need to locate it again
        if (StringUtils.isBlank(Constant.apiBaseConf.getSubmitXpath())) {//if submitXpath is a empty string, use the keyboard enter
            //the keyboard enter operation is invalid, temporarily don't use it
            logger.info("undefined submit button xpath, use the keyboard return");
            input.fireEvent(Event.TYPE_KEY_UP);
            page = (HtmlPage) client.getCurrentWindow().getEnclosedPage();
        } else {
            List buttonList = page.getByXPath(Constant.apiBaseConf.getSubmitXpath());
            if (!buttonList.isEmpty()) {
                logger.info("get submit button, click it");
                button = (HtmlElement) buttonList.get(0);
                try {
                    page = button.click();
                } catch (IOException ex) {
                    //ignored
                }

            } else {
                logger.error("can't get submit button, exit");
                return Collections.emptyList();
            }
        }


        //try 3 times to wait .3 second each for filling the page.
        List<Info> links = null;
        for (int i = 0; i < 3; i++) {
            synchronized (page) {
                try {
                    page.wait(3_000);
                } catch (InterruptedException ex) {
                    //ignored
                }
            }
            if (!(links = collector.collect(page.asXml(), page.getUrl(), Constant.apiBaseConf.getInfoLinkXpath(), Constant.apiBaseConf.getPayloadXpath())).isEmpty()) {
                logger.info("have collect infos, continue to execute");
                break;
            }
            logger.info("can't collect infos from page, retry");
        }
        return links;
    }


    private HtmlPage retryGetPage(WebClient client, String url) {
        return new Try<HtmlPage>(3).run(() -> {
                HtmlPage ans = null;
                ans = client.getPage(url);
                Objects.requireNonNull(ans.getBody());
                return ans;

        });
    }
    public static void main(String[] args) {

    }
}
