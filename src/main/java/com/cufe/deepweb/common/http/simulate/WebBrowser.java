package com.cufe.deepweb.common.http.simulate;

import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * support multi-threading
 */
public interface WebBrowser {
    /**
     * @param loginURL the login page link
     * @param username the login account username
     * @param password the login account password
     * @param usernameXpath the login username input's xpath
     * @param passwordXpath the login password input's xpath
     * @param submitXpath the login button's xpath
     * @return true if success
     */
    boolean login(String loginURL, String username, String password, String usernameXpath, String passwordXpath, String submitXpath);

    /**
     *
     * get the content corresponding to the specified url
     * @param URL
     * @return
     */
    Optional<String> getPageContent(String URL);

    /**
     * get the links from the specified page
     * @param query
     * @param collector the link collector which collect links from the page corresponding to the specified URL
     * @return
     */
    List<Info> getAllLinks(Query query, LinkCollector collector);

    void clearResource();
}
