package services;

import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.dedu.RAMMD5Dedutor;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.common.orm.model.WebSite;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.QueryLinkService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class QueryLinkServiceTest {
    static QueryLinkService service;
    @BeforeAll
    static void init() {
        Deduplicator dedu = new RAMMD5Dedutor();
        WebBrowser browser = new HtmlUnitBrowser.Builder().build();
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
