package com.cufe.deepweb.common.http.simulate;

import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * the abstract class of link collector
 * this class defines the the collector's behaviour
 * the implementation of this class must be thread-safe
 */
public abstract class LinkCollector {
    private Logger logger = LoggerFactory.getLogger(LinkCollector.class);

    /**
     * thread-safe cleaner
     */
    protected HtmlCleaner cleaner;
    /**
     * the pattern to recognize url
     */
    protected Pattern pattern;
    private static final String re = "(https?:/|\\.)?(/([\\w-]+(\\.)?)+)+(\\?(([\\w-]+(\\.)?)+=((/?([\\w-]+|[\\u4e00-\\u9fa5]+)+(\\.)?)+)?(&)?)+)?";
    public LinkCollector() {
        this.cleaner = new HtmlCleaner();
        this.pattern = Pattern.compile(re);
    }

    private String buildLink(String href, URL url) {
        if (StringUtils.isNotBlank(href)) {
            Matcher m = pattern.matcher(href);
            if (m.lookingAt()) {//match the prefix of href, because sometimes the href is not format
                href = href.substring(0, m.end());
                return href;
            }
        }
        return null;
    }

    /**
     * collect the links fit this collector's demand
     * @param content
     * @param url the URL which could get this content
     * @return
     */
    public List<Info> collect(String content, URL url, String infoLinkXpath, String payloadXpath) {
        List<Info> links = null;
        TagNode rootNode = null;
        try {
            rootNode = cleaner.clean(content);
        } catch (Exception ex) {
            logger.error("exception happen when parse html content", ex);
            return Collections.emptyList();
        }

        //if want to use xpath to indicate the information address, should point out infoLinkXpath and payloadXpath
        if (StringUtils.isNotBlank(infoLinkXpath) && StringUtils.isNotBlank(payloadXpath)) {
            links = collectByXpath(rootNode, infoLinkXpath, payloadXpath);
        } else {
            links = commonCollect(rootNode);
        }

        //resolve all the relative address to absolute address
        links.stream().forEach(link -> {
            link.fixUrl(url);

        });


        logger.trace("queryLink:{} infoLinks:{}", url.toString(), Arrays.toString(links.toArray()));
        return privateOp(links);
    }

    public List<Info> collectByXpath(TagNode root, String infoLinkXpath, String payloadXpath) {
        List<Info> links = new ArrayList<>();
        String[] slices = payloadXpath.split(",");//0:xpath 1:attribute
        String payloadName = null;
        if (slices.length >= 2) {
            payloadName = slices[1];
        }
        try {
            Object[] is = root.evaluateXPath(infoLinkXpath);
            Object[] ps = root.evaluateXPath(payloadXpath);
            int len = is.length > ps.length ? ps.length : is.length;
            for (int i = 0; i < len; i ++) {
                TagNode node = (TagNode) is[i];
                String href = node.getAttributeByName("href");
                node = (TagNode) ps[i];
                String payload = "";
                if (payloadName == null) {
                    Map<String, String> attributes = node.getAttributes();
                    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                        payload += attribute.getValue();
                    }
                } else {
                    payload = node.getAttributeByName(payloadName);
                }
                links.add(Info.link(href).addPayLoad(Constant.FT_INDEX_FIELD, payload));
            }
        } catch (XPatherException ex) {
            logger.error("xpath format error");
        }
        return links;
    }

    public List<Info> commonCollect(TagNode root) {
        List<Info> links = new ArrayList<>();
        TagNode[] nodes = root.getElementsByName("a", true);
        for (TagNode node : nodes) {
            String href = node.getAttributeByName("href");
            if (StringUtils.isNotBlank(href)) {
                Matcher m = pattern.matcher(href);
                if (m.lookingAt()) {//match the prefix of href, because sometimes the href is not format
                    href = href.substring(0, m.end());
                    links.add(Info.link(href));
                }
            }
        }
        return links;
    }

    public abstract List<Info> privateOp(List<Info> links);

}
