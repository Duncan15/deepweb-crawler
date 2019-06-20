package miscella;

import org.apache.http.impl.io.ChunkedInputStream;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    @Test void testMiscella() {
        float a = 1.5f -1;
        if (a == 0.5) {
            System.out.println("hello");
        }
    }
}
