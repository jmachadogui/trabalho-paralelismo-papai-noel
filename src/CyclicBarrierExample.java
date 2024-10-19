import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

class Task implements Runnable {
    private final CyclicBarrier barrier;

    public Task(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " is waiting at the barrier...");
            // Each thread waits at the barrier
            barrier.await();
            System.out.println(Thread.currentThread().getName() + " has crossed the barrier!");

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}

public class CyclicBarrierExample {
    public  static  void  main(String[] args) throws InterruptedException {
        // Create a CyclicBarrier for 3 threads
        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println("All parties have arrived. Let's proceed!");
        });

        // Create and start 3 threads
        for (int i = 1; i <= 3; i++) {
            new Thread(new Task(barrier), "Thread-" + i).start();
        }
    }
}
