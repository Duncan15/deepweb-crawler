package http;

import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class BrowserTest {
  private static WebBrowser webBrowser;
  @BeforeAll
  static void init() {
    //the global cookie manager
    CookieManager cookieManager = new CookieManager();

    webBrowser =  new HtmlUnitBrowser(cookieManager, 90_000);
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
  @Test
  void testGCHtmlUnit() throws Exception {
    Runnable t = new Runnable() {
      public void run() {
        try {
          int i = 1;
          while (--i > 0 ) {
            Optional p = webBrowser.getPageContent("http://www.zhaobiao.cn");
          }
        } catch (Exception ex) {

        }
      }
    };
    new Thread(t).start();
    new Thread(t).start();
    new Thread(t).start();
    new Thread(t).start();
    new Thread(t).start();
    Thread.sleep(1000 * 60 * 30);
  }
}
