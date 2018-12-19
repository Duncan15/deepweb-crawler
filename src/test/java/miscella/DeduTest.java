package miscella;

import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.dedu.RAMDocIDDedutor;
import com.cufe.deepweb.common.dedu.RAMMD5Dedutor;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;

import java.security.MessageDigest;

public class DeduTest {
  @Test
  void testDedu() {
    Deduplicator dedu = new RAMMD5Dedutor();
    for (int i =0 ; i <= 20 ; i++) {
      System.out.println(dedu.add("http://zhaobiao.cn"));
    }
  }
  @Test
  void testHash() throws Exception{
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    byte[] ans = md5.digest("md5hhhh".getBytes());
    System.out.println(DatatypeConverter.printHexBinary(ans));
  }
  @Test
  void testRAMDocIDDedutorMemoryCost() {
    int num = 10908792;
    Utils.logMemorySize();
    Deduplicator deduplicator = new RAMDocIDDedutor();
    for (int i = 0 ; i < num ; i ++) {
      deduplicator.add(i);
    }
    Utils.logMemorySize();
  }
}
