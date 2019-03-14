package services;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.dedu.RAMMD5Dedutor;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.common.orm.model.UrlBaseConf;
import com.cufe.deepweb.common.orm.model.WebSite;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.UrlBaseQueryLinkService;
import com.gargoylesoftware.htmlunit.CookieManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class QueryLinkServiceTest {
    static UrlBaseQueryLinkService service;
    @BeforeAll
    static void init() {
        Deduplicator dedu = new RAMMD5Dedutor();
        //the global cookie manager
        CookieManager cookieManager = new CookieManager();

        WebBrowser browser = new HtmlUnitBrowser(cookieManager, 90_000);
        service = new UrlBaseQueryLinkService(browser, dedu);
        Constant.urlBaseConf = new UrlBaseConf();
        Constant.urlBaseConf.setPrefix("http://121.194.104.120:8080/SogouT/Search?");
    }
    @Test
    void testCollectLinks() {
        List<Info> links = service.getInfoLinks("http://121.194.104.120:8080/SogouT/Search?keyword=produce&Submit=Search&curpage=279");
        for (Info e : links) {
            System.out.println(e.getUrl());
        }
    }
    @AfterAll
    static void close() {
        service.clearThreadResource();
    }
}
