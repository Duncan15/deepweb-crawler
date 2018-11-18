package com.cufe.deepweb.crawler.service;

import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于处理查询链接相关的服务
 */
public class QueryLinkService extends LinkService {
    private static final Logger logger = LoggerFactory.getLogger(QueryLinkService.class);
    private WebBrowser browser;


    /**
     * 生成查询链接(虽然频繁调用此方法会损耗性能，但从可维护的角度这点性能可以忽略)
     * @param keyword 关键词
     * @param pageNum 第几页,统一从1开始
     * @return
     */
    private String buildQueryLink(String keyword, int pageNum) {
        String queryLink = Constant.webSite.getPrefix();
        List<String> paramPairList = new ArrayList<>();
        paramPairList.add(Constant.webSite.getParamQuery()+"="+keyword);
        if (StringUtils.isBlank(Constant.webSite.getParamList()) || StringUtils.isBlank(Constant.webSite.getParamValueList())) {
            String[] params = Constant.webSite.getParamList().split(",");
            String[] paramVs = Constant.webSite.getParamValueList().split(",");
            for (int i = 0 ; i < params.length ; i++) {
                paramPairList.add(params[i] + "=" + paramVs[i]);
            }
        }
        String[] pgParams = Constant.webSite.getStartPageNum().split(",");
        int startNum = Integer.parseInt(pgParams[0]);//分页参数的初始值，有可能是1或者0
        int numInterval = Integer.parseInt(pgParams[1]);//分页参数的递增间隔
        int pgV = (pageNum - 1) * numInterval + startNum;//生成的链接的分页参数值
        paramPairList.add(Constant.webSite.getParamPage() + "=" + pgV);
        if (!queryLink.endsWith("?")) {
            queryLink += "?" + StringUtils.join(paramPairList,"&");
        }
        return queryLink;
    }

    /**
     * 确定keyword所能获得的分页链接总数
     * @param keyword
     * @return
     */
    private int getTotalPageNum(String keyword) {
        int endNum = this.incrementNum(keyword);//指数递增获取第一个空页页码
        int startNum = endNum/2;
        return getEndPageNum(startNum, endNum, keyword);
    }
    private int getEndPageNum(int startNum, int endNum, String keyword) {
        String endContent = browser.getPageContent(buildQueryLink(keyword,endNum)).get();
        int counter = 30;//计数器，防止二分出错执行次数太多
        while (startNum < endNum) {
            counter--;
            int mid = (startNum + endNum)/2;
            String midContent = browser.getPageContent(buildQueryLink(keyword, mid)).get();
            if(isSimilarity(midContent,endContent)) {
                endNum = mid - 1;
            } else {
                startNum = mid;
                if (endNum - startNum == 1) return startNum;
            }
            if(counter <= 0 ) break;
        }
        logger.error("error because to much time in binary search end page num");
        return startNum;
    }
    /**
     * 获取递增过程中第一个空页的页码
     * @param keyword
     * @return
     */
    private int incrementNum(String keyword) {
        int cur = 1;
        String preContent = browser.getPageContent(buildQueryLink(keyword, cur)).get(), curContent;
        while (true) {
            cur *= 2;
            curContent = browser.getPageContent(buildQueryLink(keyword, cur)).get();
            if (isSimilarity(preContent, curContent)) break; //如果两个页面相似，则都是空页
            preContent = curContent;
        }
        return cur/2;//返回第一个空页的页码
    }
    /**
     * 判断两个页页面是否相识（这里没有必要使用NLP的方法进行分词）
     * @param doc1
     * @param doc2
     * @return
     */
    private boolean isSimilarity(String doc1, String doc2) {
        LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();//用于计算文本距离
        return distance.apply(doc1.trim(), doc2.trim()) < 20;
    }

    /**
     * QueryLinkService只依赖于WebBrowser
     * @param browser
     */
    public QueryLinkService(WebBrowser browser) {
        this.browser = browser;
    }
    /**
     * 获取关键词能拿到的所有链接
     * @param keyword 关键词
     * @return
     */
    public List<String> getQueryLinks(String keyword) {
        List<String> queryLinks = new ArrayList<>();
        int num = getTotalPageNum(keyword);
        for (int i=1 ; 1 <= num ; i++) {
            queryLinks.add(buildQueryLink(keyword, i));
        }
        this.totalLinkNum = queryLinks.size();
        return queryLinks;
    }

    /**
     * 判断链接是否属于分页链接
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
     * 从queryLink指向的页面中获取信息链接
     * @param queryLink
     * @return
     */
    public List<String> getInfoLinks(String queryLink) {
        List<String> links = browser.getAllLinks(queryLink);
        if (links.size() == 0) {//记录失败的查询链接数量
            this.failedLinkNum++;
        }
        links = links.stream().filter(link -> {//去除链接中分页链接
            if (link.startsWith(Constant.webSite.getPrefix())) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
        return links;
    }

}
