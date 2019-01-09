package com.cufe.deepweb.common.http.simulate;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 使用HtmlUnit实现的Browser
 */
public final class HtmlUnitBrowser implements WebBrowser {
    private Logger logger = LoggerFactory.getLogger(HtmlUnitBrowser.class);
    static {
        //关闭HtmlUnit日志输出
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }
    private Builder builder;
    private CookieManager cookieManager;
    private volatile boolean isLogin;
    private ThreadLocal<WebClient> client = new ThreadLocal<WebClient>() {
        @Override
        protected WebClient initialValue() {
            WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
            client.setCookieManager(cookieManager);
            client.getOptions().setCssEnabled(false);//headless browser不需要css支持
            client.getOptions().setDownloadImages(false);//同样不需要下载图片，节省带宽
            client.getOptions().setJavaScriptEnabled(true);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);//当访问错误时不打出日志
            client.getOptions().setThrowExceptionOnScriptError(false);//当js运行出错不打出日志
            client.getOptions().setTimeout(builder.timeout);//设置模拟器连接网络的超时参数
            client.getOptions().setDoNotTrackEnabled(true);//不让浏览行为被记录
            client.getOptions().setHistoryPageCacheLimit(1);//限制缓存大小
            client.getOptions().setHistorySizeLimit(1);
            client.setAjaxController(new NicelyResynchronizingAjaxController());
            client.waitForBackgroundJavaScript(builder.timeout);
            logger.trace("create new webclient");
            client.close();
            return client;
        }
        @Override
        public void remove() {
            get().close();
            super.remove();
        }
    };
    private HtmlUnitBrowser(Builder builder){
        this.builder = builder;
        this.cookieManager = new CookieManager();//设置全局统一的cookieManager
        this.isLogin = false;//cookieManager是否保留登录信息

    }



    /**
     * override WebBrowser的方法
     * @param loginURL 登录页面链接
     * @param username 登录用户名
     * @param password 登录密码
     * @param usernameXpath 登录用户名输入框的xpath
     * @param passwordXpath 登录密码输入框的xpath
     * @return
     */
    @Override
    public boolean login(String loginURL, String username, String password, String usernameXpath, String passwordXpath, String submitXpath) {
        WebClient client = this.client.get();
        HtmlTextInput userNameInput = null;//用户名输入框
        HtmlPasswordInput passwordInput = null;//密码输入框
        HtmlElement button = null;//登录按钮,最好不要限定为必须button
        HtmlPage page = null;
        try{
            page = client.getPage(loginURL);
            if ((!StringUtils.isBlank(usernameXpath)) && (!StringUtils.isBlank(passwordXpath)) && (!StringUtils.isBlank(submitXpath))) {//如果xpath都有指定
                List uList = page.getByXPath(usernameXpath);
                List pList = page.getByXPath(passwordXpath);
                List sList = page.getByXPath(submitXpath);
                if (!uList.isEmpty()) {
                    userNameInput = (HtmlTextInput)uList.get(0);
                    userNameInput.setText(username);
                }
                if (!pList.isEmpty()) {
                    passwordInput = (HtmlPasswordInput)pList.get(0);
                    passwordInput.setText(password);
                }
                if (!sList.isEmpty()) {
                    button = (HtmlElement) sList.get(0);
                    button.click();
                    isLogin = true;//修改登录状态为已登录
                    return true;
                }
            }//必须指定xpath，否则无法登录，后续可拓展成指定id等等
            return false;
        }catch (IOException ex) {
            logger.error("IOEception happen when get login page", ex);
            return false;
        }
    }

    @Override
    public Optional<String> getPageContent(String URL) {
        WebClient client = this.client.get();
        try {
            HtmlPage page = client.getPage(URL);
            return Optional.ofNullable(page.getBody().asText());
        } catch (Exception ex) {
            logger.error("Exception happen when get page content", ex);
            return Optional.empty();
        }

    }

    public List<String> getAllLinks(String URL, LinkCollector collector) {
        WebClient client = this.client.get();
        try {
            HtmlPage page = client.getPage(URL);
            return collector.collect(page.asXml(), page.getUrl());
        } catch (Exception ex) {
            logger.error("Exception happen when get page content from {}", URL);
            return Collections.emptyList();
        }
    }

    @Deprecated
    public List<String> getAllLinks(String URL) {
        List<String> links = new ArrayList<>();
        WebClient client = this.client.get();
        try {
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
        }
        return links;
    }

    @Override
    public void clearThreadResource() {
        this.client.remove();
    }

    /**
     * 获得该模拟器的cookieManager
     * @return
     */
    public CookieManager getCookieManager() {
        return this.cookieManager;
    }


    /**
     * 构造器
     */
    public static class Builder{
        private int timeout;
        public Builder(){
            timeout = 90_000;

        }

        /**
         *
         * @param timeout 超时时间/s
         * @return
         */
        public Builder setTimeout(int timeout){
            this.timeout = timeout;
            return this;
        }
        public HtmlUnitBrowser build(){
            return new HtmlUnitBrowser(this);
        }
    }
}
