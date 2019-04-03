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

    /**
     * this method is used by url based and json based queryLinkService to build queryLink
     * @param pgParams
     * @param pageNum
     * @return
     */
    public static int parsePageParameterAndGetTagetNum(String[] pgParams, int pageNum) {
        int startNum = Integer.parseInt(pgParams[0]);//the start number of pageNum，maybe 1 or 0
        int numInterval = Integer.parseInt(pgParams[1]);//the interval number of different pageNum corresponding to the neighbour query link
        int pgV = (pageNum - 1) * numInterval + startNum;//the final value occur in the query link
        return pgV;
    }


    @Override
    public void clearThreadResource() {
        this.browser.clearResource();
    }

    /**
     * the generator of query link
     */
    public abstract class QueryLinks {
        /**
         * all the query page's page number start at 1
         */
        private int counter = 1;
        private int pageNum;
        private String keyword;
        QueryLinks(int pageNum, String keyword) {
            this.pageNum = pageNum;
            this.keyword = keyword;
        }

        public int getCounter() {
            return this.counter;
        }
        public int getPageNum() {
            return this.pageNum;
        }

        /**
         * get next queryLink
         * @return null if can't generate next query link
         */
        public synchronized String next() {

            String ans = null;
            if (counter <= pageNum) {
                ans = buildQueryLink(keyword, counter);
                counter++;
            }
            return ans;
        }

        //for subclass to implement
        protected abstract String buildQueryLink(String keyword, int pageNum);
    }
}
