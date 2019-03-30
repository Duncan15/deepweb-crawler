package com.cufe.deepweb.crawler.service.querys;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.query.ApiBasedQuery;
import com.cufe.deepweb.crawler.service.querys.query.Query;
import com.cufe.deepweb.common.http.simulate.LinkCollector;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.crawler.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ApiBaseQueryLinkService extends QueryLinkService {
    private Logger logger = LoggerFactory.getLogger(ApiBaseQueryLinkService.class);
    public ApiBaseQueryLinkService(WebBrowser browser, Deduplicator dedu) {
        super(browser, dedu);
        this.collector = new InfoLinkCollector();
    }
    private ApiBasedQuery buildQuery(String keyword) {
        return Query.asApiBased(Constant.apiBaseConf.getPrefix(), Constant.apiBaseConf.getInputXpath(), Constant.apiBaseConf.getSubmitXpath(), keyword);
    }
    public List<Info> getInfoLinks(String keyword) {
        this.totalLinkNum++;
        ApiBasedQuery query = buildQuery(keyword);
        List<Info> links = browser.getAllLinks(query, collector);
        if (links.size() == 0) {
            this.failedLinkNum++;
            return Collections.emptyList();
        }
        logger.trace("keyword:{} infoLinks:{}", keyword, Arrays.toString(links.toArray()));
        return links;
    }

    class InfoLinkCollector extends LinkCollector {
        //TODO: should implement in detail
        @Override
        public List<Info> privateOp(List<Info> links) {
            links = links.stream().filter(link -> {//remove the repeated links
                return dedu.add(link.getUrl());
            }).collect(Collectors.toList());
            return links;
        }
    }
}
