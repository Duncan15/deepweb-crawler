package miscella;

import com.cufe.deepweb.common.dedu.Deduplicator;
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
}
