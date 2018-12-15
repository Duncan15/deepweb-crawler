package index;

import com.cufe.deepweb.common.index.IndexClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class ClientTest {
  static IndexClient client;
  @BeforeAll
  static void init() {
    client = new IndexClient.Builder(Paths.get("F:/experiment/Index6.6.1/Indexjieba21")).setReadOnly().build();
  }
  @AfterAll
  static void exit() throws Exception {
    client.close();
  }
  @Test
  void testPrintFields() {
    System.out.println(client.getDocSize());
    Map<String, Object> infoMap = client.getIndexInfo();
    Set<String> fields = (Set<String>) infoMap.get("fields");
    fields.forEach(System.out::println);
    System.out.println(infoMap.get("leaves"));
  }
  @Test
  void testPrintDocSize() {
    System.out.println(client.getDocSize());
  }

}
