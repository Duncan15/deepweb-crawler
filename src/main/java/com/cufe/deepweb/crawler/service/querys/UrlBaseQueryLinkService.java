package com.cufe.deepweb.crawler.service.querys;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.query.Query;
import com.cufe.deepweb.common.http.simulate.LinkCollector;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * service used to deal with things on query link
 */
public class UrlBaseQueryLinkService extends QueryLinkService {
    private final Logger logger = LoggerFactory.getLogger(UrlBaseQueryLinkService.class);

    /**
     *
     * @param browser
     */
    public UrlBaseQueryLinkService(WebBrowser browser, Deduplicator dedu) {
        super(browser, dedu);
        this.collector = new InfoLinkCollector();
    }

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
            keyword = URLEncoder.encode(keyword, Constant.extraConf.getCharset());
        } catch (UnsupportedEncodingException ex) {
            //ignored
        }
        String queryLink = Constant.urlBaseConf.getPrefix();
        List<String> paramPairList = new ArrayList<>();
        paramPairList.add(Constant.urlBaseConf.getParamQuery()+"="+keyword);
        if (StringUtils.isNotBlank(Constant.urlBaseConf.getParamList()) && StringUtils.isNotBlank(Constant.urlBaseConf.getParamValueList())) {
            String[] params = Constant.urlBaseConf.getParamList().split(",");
            String[] paramVs = Constant.urlBaseConf.getParamValueList().split(",");
            for (int i = 0 ; i < params.length ; i++) {
                paramPairList.add(params[i] + "=" + paramVs[i]);//must add detect here
            }
        }
        String[] pgParams = Constant.urlBaseConf.getStartPageNum().split(",");
        paramPairList.add(Constant.urlBaseConf.getParamPage() + "=" + parsePageParameterAndGetTagetNum(pgParams, pageNum));
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
        String[] page1 = NlpAnalysis.parse(doc1).toString().split(",");
        String[] page2 = NlpAnalysis.parse(doc2).toString().split(",");
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        set1.addAll(Arrays.asList(page1));
        set2.addAll(Arrays.asList(page2));
        double or = 0;
        result.addAll(set1);
        result.retainAll(set2);
        or = (double) result.size() / set1.size();
        logger.trace("similarity is {}", or);
        return or > 0.95;
    }
//    private boolean isSimilarity(String doc1, String doc2) {
//        LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();//used to compute text distance
//        int gap = distance.apply(doc1.trim(), doc2.trim());
//        return gap < 500;
//    }

    /**
     * get all the query links corresponding to the keyword
     * @param keyword
     * @return
     */
    public QueryLinks getQueryLinks(String keyword) {
        int num = getTotalPageNum(keyword);
        logger.info("total page num is {}", num);
        this.totalLinkNum = num;
        return new UrlBaseQueryLinks(num, keyword);
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
        if (link.startsWith(Constant.urlBaseConf.getPrefix())) {
            String[] values = Constant.urlBaseConf.getParamValueList().split(",");
            if(values.length == 0) {
                return true;
            }
            for(String e : values) {
                if(link.contains(e)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get info links from the page pointed by the query link
     * @param queryLink
     * @return
     */
    public List<Info> getInfoLinks(String queryLink) {
        List<Info> links = null;
        links = browser.getAllLinks(Query.asUrlBased(queryLink), collector);
        if (links.size() == 0) {//record the number of failed query links
            this.failedLinkNum++;
            return Collections.emptyList();
        }
        logger.trace("queryLink:{}, infoLinks after dedu:{}", queryLink, Arrays.toString(links.toArray()));
        return links;
    }


    public class UrlBaseQueryLinks extends QueryLinks {

        private UrlBaseQueryLinks(int pageNum, String keyword) {
            super(pageNum, keyword);
        }

        @Override
        protected String buildQueryLink(String keyword, int pageNum) {
            return UrlBaseQueryLinkService.this.buildQueryLink(keyword, pageNum);
        }


    }

    /**
     * InfoLinkCollector collect the info links from query page
     */
    class InfoLinkCollector extends LinkCollector {

        public InfoLinkCollector() {
            super();
        }
        @Override
        public List<Info> privateOp(List<Info> links) {
            links = links.stream().filter(link -> {//remove the repeated links and query links
                if (isQueryLink(link.getUrl())) {
                    return false;
                } else {
                    return dedu.add(link.getUrl());
                }
            }).collect(Collectors.toList());
            return links;
        }
    }
}

