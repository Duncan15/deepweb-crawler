package http;

import com.cufe.deepweb.common.http.client.ApacheClient;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ClientTest {
  private static CusHttpClient client;
  @BeforeAll
  static void init() {
    client = new ApacheClient.Builder().build();
  }
  @Test
  void testClient() {
    System.out.println(client.getContent("http://zb.zhaobiao.cn/bidding_v_22122045.html?q=produce&his=1").get());
  }
}
