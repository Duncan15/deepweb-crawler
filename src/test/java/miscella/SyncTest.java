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

  private volatile boolean exit = false;
  @Test
  void testWaitNotify() {

    Thread thread1 = new Thread() {
      public void run() {
        System.out.println(Thread.currentThread().getName() + "start");
        synchronized (this) {
          while (!exit) {
            try {
              this.wait();
            } catch (InterruptedException ex) {
              //ignored
            }
          }
          System.out.println(Thread.currentThread().getName() + "exit");
        }
      }
    };

    Thread thread2 = new Thread() {
      public void run() {
        System.out.println(Thread.currentThread().getName() + "start");
        synchronized (thread1) {
          while (!exit) {
            try {
              thread1.wait();
            } catch (InterruptedException ex) {
              //ignored
            }
          }
          System.out.println(Thread.currentThread().getName() + "exit");
        }
      }
    };
    Thread thread3 = new Thread() {
      public void run() {
        synchronized (thread1) {
          System.out.println(Thread.currentThread().getName() + "start");
          exit = true;
          thread1.notifyAll();
          System.out.println(Thread.currentThread().getName() + "exit");
        }

      }
    };

    thread1.start();
    thread2.start();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      //ignored
    }
    thread3.start();
  }
}
