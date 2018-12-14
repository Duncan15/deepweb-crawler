package index;

import com.cufe.deepweb.common.index.IndexClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class ClientTest {
  static IndexClient client;
  @BeforeAll
  static void init() {
    client = new IndexClient.Builder(Paths.get("F:/experiment/tmp/Indexjieba21")).setReadOnly().build();
  }
  @Test
  void testPrintFields() {
    Map<String, Object> infoMap = client.getIndexInfo();
    Set<String> fields = (Set<String>) infoMap.get("fields");
    fields.forEach(System.out::println);
    System.out.println(infoMap.get("leaves"));
  }
}
