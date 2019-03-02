package com.cufe.deepweb.crawler.service;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.http.simulate.LinkCollector;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * service used to deal with things on query link
 */
public class QueryLinkService extends LinkService {
    private static final Logger logger = LoggerFactory.getLogger(QueryLinkService.class);
    private WebBrowser browser;
    private Deduplicator dedu;
    /**
     * the link collector to collect info link
     */
    private LinkCollector collector;

    /**
     * generate query link
     * it seems that invoke this method frequently would affect the performance
     * but in this method it's easy to maintain, so just ignore the small affect
     * @param keyword
     * @param pageNum note: start from 1
     * @return
     */
    private String buildQueryLink(String keyword, int pageNum) {
        try {
            keyword = URLEncoder.encode(keyword, Constant.webSite.getCharset());
        } catch (UnsupportedEncodingException ex) {

        }
        String queryLink = Constant.webSite.getPrefix();
        List<String> paramPairList = new ArrayList<>();
        paramPairList.add(Constant.webSite.getParamQuery()+"="+keyword);
        if (StringUtils.isNotBlank(Constant.webSite.getParamList()) && StringUtils.isNotBlank(Constant.webSite.getParamValueList())) {
            String[] params = Constant.webSite.getParamList().split(",");
            String[] paramVs = Constant.webSite.getParamValueList().split(",");
            for (int i = 0 ; i < params.length ; i++) {
                paramPairList.add(params[i] + "=" + paramVs[i]);//must add detect here
            }
        }
        String[] pgParams = Constant.webSite.getStartPageNum().split(",");
        int startNum = Integer.parseInt(pgParams[0]);//the start number of pageNum，maybe 1 or 0
        int numInterval = Integer.parseInt(pgParams[1]);//the interval number of different pageNum corresponding to the neighbour query link
        int pgV = (pageNum - 1) * numInterval + startNum;//the final value occur in the query link
        paramPairList.add(Constant.webSite.getParamPage() + "=" + pgV);
        if (!queryLink.endsWith("?")) {
            queryLink += "?";
        }
        queryLink += StringUtils.join(paramPairList, "&");

        return queryLink;
    }

    /**
     * confirm the total number of query link corresponding to the specified keyword
     * @param keyword
     * @return
     */
    private int getTotalPageNum(String keyword) {
        int endNum = this.incrementNum(keyword);//incremental to get the first empty page
        if (endNum == 1) return 0;
        int startNum = endNum / 2;
        return getEndPageNum(startNum, endNum, keyword);
    }
    private int getEndPageNum(int startNum, int endNum, String keyword) {
        String endContent = browser.getPageContent(buildQueryLink(keyword,endNum)).get();
        while (startNum < endNum) {
            int mid = (startNum + endNum) / 2;
            String midContent = browser.getPageContent(buildQueryLink(keyword, mid)).get();
            logger.trace("mid num is {}", mid);
            if(isSimilarity(midContent, endContent)) {
                endNum = mid;
            } else {
                startNum = mid;
            }
            if (endNum - startNum == 1) break;
        }
        return startNum;
    }
    /**
     * incremental to get the first empty page number
     * @param keyword
     * @return
     */
    private int incrementNum(String keyword) {
        int cur = 1;
        String testURL = buildQueryLink(keyword, cur);

        //the pre page's content
        String preContent = browser.getPageContent(testURL).get();
        //the current page's content
        String curContent;
        logger.trace("increment page num to {}", cur);
        while (true) {
            cur *= 2;
            testURL = buildQueryLink(keyword, cur);
            curContent = browser.getPageContent(testURL).orElse("");
            logger.trace("increment page num to {}", cur);
            if (isSimilarity(preContent, curContent)) break; //if current page is similar with the pre page, it seems that this two page are empty pages
            preContent = curContent;
        }
        return cur/2;//return this first empty page number
    }
    /**
     * judge that whether the two page are similar
     * this method use NLP method to judge
     * @param doc1
     * @param doc2
     * @return
     */
    private boolean isSimilarity(String doc1, String doc2) {
        Set<String> result = new HashSet<>();
        String[] page1 = ToAnalysis.parse(doc1).toString().split(",");
        String[] page2 = ToAnalysis.parse(doc2).toString().split(",");
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        set1.addAll(Arrays.asList(page1));
        set2.addAll(Arrays.asList(page2));
        double or = 0;
        result.addAll(set1);
        result.retainAll(set2);
        or = (double) result.size() / set1.size();
        return or > 0.8;
    }
//    private boolean isSimilarity(String doc1, String doc2) {
//        LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();//used to compute text distance
//        int gap = distance.apply(doc1.trim(), doc2.trim());
//        return gap < 500;
//    }

    /**
     *
     * @param browser
     */
    public QueryLinkService(WebBrowser browser, Deduplicator dedu) {
        this.browser = browser;
        this.dedu = dedu;
        this.collector = new InfoLinkCollector();
    }
    /**
     * get all the query links corresponding to the keyword
     * @param keyword
     * @return
     */
    public QueryLinks getQueryLinks(String keyword) {
        int num = getTotalPageNum(keyword);
        logger.info("total page num is {}", num);
        this.totalLinkNum = num;
        return new QueryLinks(num, keyword);
    }

    /**
     * judge whether the specified link is a query link or not
     * @param link
     * @return
     */
    public boolean isQueryLink(String link) {
        if (StringUtils.isBlank(link)) {
            return false;
        }
        if (link.startsWith(Constant.webSite.getPrefix())) {
            return true;
        }
        return false;
    }

    /**
     * get info links from the page pointed by the query link
     * @param queryLink
     * @return
     */
    public List<String> getInfoLinks(String queryLink) {
        List<String> links = null;
        links = browser.getAllLinks(queryLink, collector);
        if (links.size() == 0) {//record the number of failed query links
            this.failedLinkNum++;
            return Collections.emptyList();
        }
        links = links.stream().filter(link -> {//remove the repeated links and query links
            if (link.startsWith(Constant.webSite.getPrefix())) {
                return false;
            } else {
                return dedu.add(link);
            }
        }).collect(Collectors.toList());
        return links;
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

    /**
     * the generator of query link
     */
    public class QueryLinks {
        private final static int LIMIT = 5;
        private AtomicInteger curConsumeNum = new AtomicInteger(0);
        /**
         * all the query page's page number start at 1
         */
        private int counter = 1;
        private int pageNum;
        private String keyword;
        private QueryLinks(int pageNum, String keyword) {
            this.pageNum = pageNum;
            this.keyword = keyword;
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


        public int getCounter() {
            return this.counter;
        }
        public int getPageNum() {
            return this.pageNum;
        }
    }

    /**
     * InfoLinkCollector collect the info links from query page
     */
    class InfoLinkCollector extends LinkCollector {
        /**
         * thread-safe cleaner
         */
        private HtmlCleaner cleaner;
        /**
         * the pattern to recognize url
         */
        private Pattern pattern;
        private static final String re = "(https?:/|\\.)?(/([\\w-]+(\\.)?)+)+(\\?(([\\w-]+(\\.)?)+=(([\\w-]+(\\.)?)+)?(&)?)+)?";
        public InfoLinkCollector() {
            this.cleaner = new HtmlCleaner();
            this.pattern = Pattern.compile(re);
        }
        @Override
        public List<String> collect(String content, URL url) {
            List<String> links = new ArrayList<>();
            TagNode rootNode = null;
            try {
                rootNode = cleaner.clean(content);
            } catch (Exception ex) {
                logger.error("exception happen when parse html content", ex);
                return Collections.emptyList();
            }
            TagNode[] nodes = rootNode.getElementsByName("a", true);
            for (TagNode node : nodes) {
                String href = node.getAttributeByName("href");
                if (StringUtils.isNotBlank(href)) {
                    Matcher m = pattern.matcher(href);
                    if (m.lookingAt()) {//match the prefix of href, because sometimes the href is not format
                        href = href.substring(0, m.end());
                        if (href.startsWith(".") || href.startsWith("/")) {
                            try {
                                href = new URL(url, href).toString();
                            } catch (MalformedURLException ex) {
                                logger.error("url {} format error", href);
                                //if happen format error, just jump over this href
                                continue;
                            }
                        }
                        links.add(href);
                    }
                }
            }
            return links;
        }
    }
}

