package com.cufe.deepweb.common.http.simulate;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.ObjectPool;
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
    private ObjectPool<WebClient> clientPool;//webclient pool
    private volatile boolean isLogin;

    /**
     * initialize a new HtmlUnitBrowser
     * @param pool
     */
    public HtmlUnitBrowser(ObjectPool<WebClient> pool){
        this.clientPool = pool;
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
        WebClient client = null;
        try {
            client = this.clientPool.borrowObject();
            HtmlTextInput userNameInput = null;//用户名输入框
            HtmlPasswordInput passwordInput = null;//密码输入框
            HtmlElement button = null;//登录按钮,最好不要限定为必须button
            HtmlPage page = null;
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
        } catch (Exception ex) {
            logger.error("Eception happen when get login page", ex);
            return false;
        } finally {
            try {
                if (client != null) {
                    this.clientPool.returnObject(client);
                }
            } catch (Exception ex2) {
                //ignored
            }
        }
    }

    @Override
    public Optional<String> getPageContent(String URL) {
        WebClient client = null;

        try {
            client = clientPool.borrowObject();
            HtmlPage page = client.getPage(URL);
            return Optional.ofNullable(page.getBody().asText());
        } catch (Exception ex) {
            logger.error("Exception happen when get page content", ex);
            return Optional.empty();
        } finally {
            try {
                if (client != null) {
                    this.clientPool.returnObject(client);
                }
            }catch (Exception ex) {
                //ignored
            }
        }

    }

    public List<String> getAllLinks(String URL, LinkCollector collector) {
        WebClient client = null;

        try {
            client = this.clientPool.borrowObject();
            HtmlPage page = client.getPage(URL);
            return collector.collect(page.asXml(), page.getUrl());
        } catch (Exception ex) {
            logger.error("Exception happen when get page content from {}", URL);
            return Collections.emptyList();
        } finally {
            try {
                if (client != null) {
                    this.clientPool.returnObject(client);
                }
            } catch (Exception ex) {
                //ignored
            }
        }
    }

    @Deprecated
    public List<String> getAllLinks(String URL) {
        List<String> links = new ArrayList<>();
        WebClient client = null;

        try {
            client = this.clientPool.borrowObject();
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

        } finally {
            try {
                if (client != null) {
                    this.clientPool.returnObject(client);
                }
            } catch (Exception ex) {
                //ignored
            }
        }
        return links;
    }

}
