import java.util.concurrent.Semaphore;

class Database {
    private final Semaphore semaphore = new Semaphore(5);  // Max 5 threads allowed

    public void accessDatabase() {
        try {
            // Acquire a permit (this blocks if no permits are available)
            System.out.println(Thread.currentThread().getName() + " is waiting for a permit...");
            semaphore.acquire();
            System.out.println(Thread.currentThread().getName() + " acquired a permit and is accessing the database.");

            // Simulate work with the database
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(Thread.currentThread().getName() + " is releasing the permit.");
            semaphore.release();  // Release the permit
        }
    }
}

public class SemaphoreExample {
    public static  void main(String[] args) {
        Database db = new Database();

        // Create 6 threads trying to access the database
        for (int i = 1; i <= 6; i++) {
            new Thread(db::accessDatabase, "Thread-" + i).start();
        }
    }
}
