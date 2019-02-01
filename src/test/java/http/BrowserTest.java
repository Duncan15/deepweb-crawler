package http;

import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.HtmlUnitFactory;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BrowserTest {
  private static WebBrowser webBrowser;
  @BeforeAll
  static void init() {
    //the global cookie manager
    CookieManager cookieManager = new CookieManager();

    //configure the web brawser
    GenericObjectPoolConfig<WebClient> config = new GenericObjectPoolConfig<>();
    //config.setBlockWhenExhausted(false);
    //limit the maximum browser number to 5
    config.setMaxTotal(5);
    webBrowser = new HtmlUnitBrowser(new GenericObjectPool<WebClient>(new HtmlUnitFactory(cookieManager, 90_000), config));
  }
  @Test
  void testHtmlUnit() throws IOException, XPatherException {
    WebClient webClient = new WebClient();
    HtmlPage page = webClient.getPage("http://blog.cufercwc.cn");
    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode tagNode = cleaner.clean(page.asXml());
    TagNode[] nodes = tagNode.getElementsByName("a", true);
    for (TagNode node : nodes) {
      Map<String, String> map = node.getAttributes();
      System.out.println(map.get("href"));
    }
  }
}
