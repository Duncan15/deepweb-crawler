package miscella;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class SyntaxTest {
    @Test
    void testPath(){
        System.out.println(Paths.get("F:/abcd/","efgs/sdfa/").toString());
    }
}
