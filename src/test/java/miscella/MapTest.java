package miscella;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapTest {
    @Test
    void testMapIterator() {
        Map<String, Integer> map = new HashMap<String, Integer>() {
            {
                put("a", 1);
                put("b", 2);
                put("c", 3);
            }
        };
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            if (entry.getKey().equals("a")) {
                iterator.remove();
            }
        }
        System.out.println(map.size());
    }
}
