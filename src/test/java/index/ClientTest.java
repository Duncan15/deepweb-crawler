package index;

import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.index.IndexClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.*;

public class ClientTest {
  static IndexClient client;
  @BeforeAll
  static void init() {
    client = new IndexClient.Builder(Paths.get("/Users/cwc/Desktop/fulltext")).setReadOnly().build();

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

  @Test
  void printDocumentContent() {
    int total = client.getDocSize();
    Set<Integer> docIDSet = new HashSet<>();
    Random r = new Random();
    for (int i = 0; i <= 10; i++) {
      docIDSet.add(r.nextInt(total));
    }
    List<String> content = client.loadDocuments("fulltext", docIDSet);
    content.forEach(System.out::println);
  }
  @Test
  void searchAndPrint() {
    client.loadDocuments(new ArrayList<>(client.search("fulltext", "浪潮"))).forEach(map -> {
      System.out.println(map.get("filename") + map.get("link"));
    });
  }
  @Test
  void testDocSetMapMemory() {
    System.out.println("record doc set map memory");
    Utils.logMemorySize();
    Map map = client.getDocSetMap("body", 0.02, 0.15);
    Utils.logMemorySize();
  }
}
