package http;

import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
  void testHtmlUnit() {
    ExecutorService service = Executors.newFixedThreadPool(10);
    Set<Future> runSet = new HashSet<>();
    for (int i = 0 ; i < 10 ; i++) {
      runSet.add(service.submit(() -> {
        webBrowser.getAllLinks("http://s.zhaobiao.cn/s?queryword=%BC%A4&searchtype=zb&field=super&currentpage=101");
      }));
    }
    while(Utils.isRun(runSet));
  }
}
