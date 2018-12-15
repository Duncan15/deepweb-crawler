package os;

import org.junit.jupiter.api.Test;

public class OsTest {
    @Test
    void processNumTest() {
        System.out.println(Runtime.getRuntime().availableProcessors());
    }
}
