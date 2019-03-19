package miscella;

import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.util.*;

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
    @Test void testAlg() {
        Scanner sc = new Scanner(System.in);
        List<Integer> list = new ArrayList<>();
        while (sc.hasNext()) {
            list.add(sc.nextInt());
        }
        Collections.sort(list);
        Integer[] a = list.toArray(new Integer[0]);
        List<String> l = new ArrayList<>();
        l.toArray(new String[0]);

    }
}
