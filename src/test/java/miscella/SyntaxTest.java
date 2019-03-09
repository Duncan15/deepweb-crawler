package miscella;

import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class SyntaxTest {
    @Test
    void testPath(){
        System.out.println(Paths.get("F:/abcd/","efgs/sdfa/").toString());
    }
    @Test
    void testEncode() throws Exception {


        Integer a = new Integer(2);
        Integer b = new Integer(2);
        System.out.println(a == b);
    }
}
