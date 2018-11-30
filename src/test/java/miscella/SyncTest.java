package miscella;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncTest {
  @Test
  void testSync() {
    ExecutorService service = Executors.newFixedThreadPool(10);
    List<Integer> list = new ArrayList<>();
    for (int i = 0 ; i < 1000 ; i++) {
      list.add(i);
    }
    list.forEach(i -> {
      service.execute(() -> {
        System.out.println(i);
      });
    });
  }
}
