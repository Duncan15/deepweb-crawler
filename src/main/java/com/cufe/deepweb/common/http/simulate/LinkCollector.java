package com.cufe.deepweb.common.http.simulate;

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                if (href.startsWith(".") || href.startsWith("/")) {
                    try {
                        href = new URL(url, href).toString();
                        return href;
                    } catch (MalformedURLException ex) {
                        logger.error("url {} format error", href);
                        //if happen format error, just jump over this href
                    }
                } else return href;
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
    public List<String> collect(String content, URL url, String infoLinkXpath) {
        List<String> links = commonOp(content,url, infoLinkXpath);
        logger.trace("queryLink:{} infoLinks:{}", url.toString(), Arrays.toString(links.toArray()));
        return filter(links);
    }
    public List<String> commonOp(String content, URL url, String infoLinkXpath) {
        List<String> links = new ArrayList<>();
        TagNode rootNode = null;
        try {
            rootNode = cleaner.clean(content);
        } catch (Exception ex) {
            logger.error("exception happen when parse html content", ex);
            return Collections.emptyList();
        }

        if (!StringUtils.isBlank(infoLinkXpath)) {
            try {
                Object[] as = rootNode.evaluateXPath(infoLinkXpath);
                for (Object a : as) {
                    TagNode node = (TagNode) a;
                    String href = node.getAttributeByName("href");
                    if ((href = buildLink(href,url)) != null) {
                        links.add(href);
                    }
                }
            } catch (XPatherException ex) {
                logger.error("xpath format error");
            }
        }
        if (!links.isEmpty()) return links;

        TagNode[] nodes = rootNode.getElementsByName("a", true);
        for (TagNode node : nodes) {
            String href = node.getAttributeByName("href");
            if ((href = buildLink(href, url)) != null) {
                links.add(href);
            }
        }
        return links;
    }
    public abstract List<String> filter(List<String> links);

}
