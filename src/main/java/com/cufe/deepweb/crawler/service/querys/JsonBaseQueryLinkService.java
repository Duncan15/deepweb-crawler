package com.cufe.deepweb.crawler.service.querys;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.http.client.resp.JsonContent;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * the JsonPointer of target data
     */
    private JsonPointer totalAddressJsonPtr;
    private JsonPointer contentJsonPtr;

    private String[] linkRules;
    private String[] payloadRules;
    public JsonBaseQueryLinkService(WebBrowser browser, Deduplicator dedu, CusHttpClient httpClient) {
        super(browser, dedu);
        this.httpClient = httpClient;
        totalAddressJsonPtr = JsonPointer.compile(Constant.jsonBaseConf.getTotalAddress());
        if (StringUtils.isNotBlank(Constant.jsonBaseConf.getContentAddress())) {
            contentJsonPtr = JsonPointer.compile(Constant.jsonBaseConf.getContentAddress());
        }

        linkRules = Constant.jsonBaseConf.getLinkRule().split("\\+");
        payloadRules = Constant.jsonBaseConf.getPayloadRule().split("\\+");
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
        JsonContent content = httpClient.getJSON(buildQueryLink(keyword, 1));
        JsonNode node = content.getRoot();
        node = node.at(totalAddressJsonPtr);
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
        this.totalLinkNum = total;
        return new JsonBaseQueryLinks(total, keyword);
    }

    /**
     * get the infoLinks from target query page
     * @param queryLink
     * @return
     */
    public List<Info> getInfoLinks(String queryLink) {
        JsonContent content = httpClient.getJSON(queryLink);//directly cast to json content, maybe cause exception
        if (content == null) return Collections.emptyList();
        JsonNode node = content.getRoot();
        if (contentJsonPtr != null) {
            node = node.at(contentJsonPtr);
        }


        //if can't find the target node by JsonPointer or the target node is not a ArrayNode, just return empty list
        if (node.isMissingNode() || !(node instanceof ArrayNode)) {
            failedLinkNum++;
            return Collections.emptyList();
        }
        List<Info> list = new ArrayList<>();

        ArrayNode arr = (ArrayNode) node;
        for (JsonNode e : arr) {

            //special code
            String filename = null;


            StringBuilder sbLink = new StringBuilder();
            boolean valid = true;
            for (String rule : linkRules) {

                if (rule.startsWith("[")) {
                    sbLink.append(rule.substring(1, rule.length() - 1));
                } else {
                    JsonNode dtNode = e.at(rule);
                    if (dtNode.isMissingNode()) {
                        valid = false;
                        break;
                    }
                    String partValue = "";
                    if (dtNode.isTextual()) {
                        partValue += dtNode.textValue();
                    } else if (dtNode.isInt()) {
                        partValue += dtNode.intValue();
                    }
                    //remove the html tag from json content if exists
                    while (partValue.indexOf("<") >= 0) {
                        int start = partValue.indexOf("<");
                        int end = partValue.indexOf(">");
                        partValue = partValue.substring(0, start) + partValue.substring(end + 1);
                    }

                    //special code
                    if (rule.contains("filename")) {
                        filename = partValue;
                    }

                    if (partValue.indexOf("/") < 0) {
                        try {
                            partValue = URLEncoder.encode(partValue, Constant.extraConf.getCharset());
                        } catch (UnsupportedEncodingException ex) {
                            //ignored
                        }
                    }

                    sbLink.append(partValue);
                }
            }

            //TODO: can use collector to implement the filter logic in the future
            //dedu operation
            if (!valid || !dedu.add(sbLink.toString())) {
                continue;
            }

            StringBuilder sbLoad = new StringBuilder();
            for (String rule : payloadRules) {
                JsonNode dtNode = e.at(rule);
                if (dtNode.isTextual()) {
                    sbLoad.append(dtNode.textValue());
                } else if (dtNode.isInt()) {
                    sbLoad.append(dtNode.intValue());
                }
                sbLoad.append(" ");
            }


            list.add(Info.link(sbLink.toString()).addPayLoad(Constant.FT_INDEX_FIELD, sbLoad.toString()).addPayLoad("filename", filename).addPayLoad("link", sbLink.toString()));
        }
        return list;
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
    class InfoLinkCollector {

    }
}
