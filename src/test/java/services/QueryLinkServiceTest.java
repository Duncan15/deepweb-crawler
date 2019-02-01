package services;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.dedu.RAMMD5Dedutor;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.HtmlUnitFactory;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.common.orm.model.WebSite;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.QueryLinkService;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class QueryLinkServiceTest {
    static QueryLinkService service;
    @BeforeAll
    static void init() {
        Deduplicator dedu = new RAMMD5Dedutor();
        //the global cookie manager
        CookieManager cookieManager = new CookieManager();

        //configure the web brawser
        GenericObjectPoolConfig<WebClient> config = new GenericObjectPoolConfig<>();
        //config.setBlockWhenExhausted(false);
        //limit the maximum browser number to 5
        config.setMaxTotal(5);
        WebBrowser browser = new HtmlUnitBrowser(new GenericObjectPool<WebClient>(new HtmlUnitFactory(cookieManager, 90_000), config));
        service = new QueryLinkService(browser, dedu);
        Constant.webSite = new WebSite();
        Constant.webSite.setPrefix("http://121.194.104.120:8080/SogouT/Search?");
    }
    @Test
    void testCollectLinks() {
        List<String> links = service.getInfoLinks("http://121.194.104.120:8080/SogouT/Search?keyword=produce&Submit=Search&curpage=279");
        for (String e : links) {
            System.out.println(e);
        }
    }
    @AfterAll
    static void close() {
        service.clearThreadResource();
    }
}
