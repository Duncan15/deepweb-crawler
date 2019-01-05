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
    HtmlPage page = webClient.getPage("http://www.baidu.com");
    HtmlCleaner cleaner = new HtmlCleaner();
    Object[] os = cleaner.clean(page.asXml()).evaluateXPath("//body");
    TagNode t = (TagNode)os[0];
  }
}
