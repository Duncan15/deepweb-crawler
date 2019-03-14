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
    System.out.println(client.getContent("https://www.scoot.co.uk/find/building-services-in-uk"));
  }
}
