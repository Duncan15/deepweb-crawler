package com.cufe.deepweb.crawler.service.querys;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.http.client.resp.JsonContent;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.crawler.Constant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * current implementation of JSON based queryLinkService only support using HTTP GET method to query
 * other HTTP methods is left for future implementation
 */
public class JsonBaseQueryLinkService extends QueryLinkService {
    /**
     * the http tool for this queryLinkService to access site.
     */
    private CusHttpClient httpClient;
    public JsonBaseQueryLinkService(WebBrowser browser, Deduplicator dedu, CusHttpClient httpClient) {
        super(browser, dedu);
        this.httpClient = httpClient;
    }

    /**
     * build queryLink for JsonBaseQueryLinkService
     * @param keyword
     * @param pageNum
     * @return
     */
    private String buildQueryLink(String keyword, int pageNum) {
        try {
            keyword = URLEncoder.encode(keyword, Constant.extraConf.getCharset());
        } catch (UnsupportedEncodingException ex) {
            //ignored
        }
        String queryLink = Constant.jsonBaseConf.getPrefix();
        List<String> paramList = new ArrayList<>();
        paramList.add(Constant.jsonBaseConf.getParamQuery() + "=" + keyword);
        String[] pgParams = Constant.jsonBaseConf.getPageStrategy().split(",");
        paramList.add(Constant.jsonBaseConf.getParamPage() + "=" + parsePageParameterAndGetTagetNum(pgParams, pageNum));
        if (StringUtils.isNotBlank(Constant.jsonBaseConf.getConstString())) {
            paramList.add(Constant.jsonBaseConf.getConstString());
        }
        if (!queryLink.endsWith("?")) {
            queryLink += "?";
        }
        queryLink += StringUtils.join(paramList, "&");
        return queryLink;
    }

    /**
     * there is two methods to get total page number in json base queryLink service.
     * 1. directly get the total number from a json response (ordinary pagination program would implement this)
     * 2. if can't get by method 1, use the incremental method like that in urlBaseQueryLinkService
     *
     * @param keyword
     * @return
     */
    private int getTotalPageNum(String keyword) {
        //current implementation just use the first method
        JsonContent content = (JsonContent) httpClient.getContent(buildQueryLink(keyword, 1));
        JsonNode node = content.getRoot();
        String totalAddress = Constant.jsonBaseConf.getTotalAddress();
        String[] addrs = totalAddress.split(".");
        for (int i = 0; i < addrs.length; i++) {
            if (addrs[i].startsWith("[")) {
                //the current address format is [num]
                String numStr = addrs[i].substring(1, addrs[i].length() - 1);
                int num = Integer.parseInt(numStr);
                ArrayNode cur = (ArrayNode) node;
                node = cur.get(num);
            } else {
                //the current address format is name
                ObjectNode cur =(ObjectNode) node;
                node = cur.get(addrs[i]);
            }
        }
        Integer total = null;
        if (node.isInt()) {
            total = node.intValue();
        } else if (node.isTextual()) {
            total = Integer.parseInt(node.textValue());
        }

        if (total == null) {
            total = 0;
        }
        return total;
    }

    public QueryLinks getQueryLinks(String keyword) {
        int total = getTotalPageNum(keyword);
        return new JsonBaseQueryLinks(total, keyword);
    }



    public class JsonBaseQueryLinks extends QueryLinks {

        JsonBaseQueryLinks(int pageNum, String keyword) {
            super(pageNum, keyword);
        }

        @Override
        protected String buildQueryLink(String keyword, int pageNum) {
            return JsonBaseQueryLinkService.this.buildQueryLink(keyword, pageNum);
        }
    }
}
