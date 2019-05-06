package com.cufe.deepweb.common.http.simulate;

import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
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


    /**
     * collect the links fit this collector's demand
     * @param content
     * @param url the URL which could get this content
     * @return
     */
    public List<Info> collect(String content, URL url, String infoLinkXpath, String payloadXpath) {
        logger.trace("search result content:{} for url:{}", content, url.toString());
        List<Info> links = null;
        TagNode rootNode = null;
        try {
            rootNode = cleaner.clean(content);
        } catch (Exception ex) {
            logger.error("exception happen when parse html content", ex);
            return Collections.emptyList();
        }

        //if want to use xpath to indicate the information address, should point out infoLinkXpath and payloadXpath
        if (StringUtils.isNotBlank(infoLinkXpath)) {
            links = collectByXpath(rootNode, infoLinkXpath, payloadXpath);
        } else {
            links = commonCollect(rootNode);
        }

        //resolve all the relative address to absolute address
        links.stream().forEach(link -> {
            link.fixUrl(url);

        });


        logger.trace("queryLink:{} infoLinks:{}", url.toString(), Arrays.toString(links.toArray()));
        links = privateOp(links);
        return links;
    }

    public List<Info> collectByXpath(TagNode root, String infoLinkXpath, String payloadXpath) {
        List<Info> links = new ArrayList<>();
        try {
            Object[] is = root.evaluateXPath(infoLinkXpath);
            for (Object i : is) {
                TagNode node = (TagNode) i;
                String href = node.getAttributeByName("href");
                links.add(Info.link(href));
            }
        } catch (XPatherException ex) {
            logger.error("info-link xpath format error");
        }

        if (Objects.nonNull(payloadXpath)) {
            String[] slices = payloadXpath.split(",");//0:xpath 1:attribute
            try {
                Object[] ps = root.evaluateXPath(slices[0]);
                if (links.size() == ps.length) {
                    for (int i = 0; i < ps.length; i++) {
                        TagNode node = (TagNode) ps[i];
                        String payload = "";
                        Map<String, String> attributes = node.getAttributes();
                        for (int j = 1; j < slices.length; j++) {//if have pointed some attribute name, just collect them
                            payload += attributes.get(slices[j]);
                        }
                        if (slices.length == 1) {//if haven't specified any attribute name, just collect all of them
                            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                                payload += attribute.getValue();
                            }
                        }
                        links.get(i).addPayLoad(Constant.FT_INDEX_FIELD, payload);
                    }
                }
            } catch (XPatherException ex) {
                logger.error("payload xpath format error");
            }
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
                if (m.matches()) {//match the href
                    links.add(Info.link(href));
                }
            }
        }
        return links;
    }

    public abstract List<Info> privateOp(List<Info> links);

}
