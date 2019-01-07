package http;

import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
    webBrowser = new HtmlUnitBrowser.Builder().build();
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
