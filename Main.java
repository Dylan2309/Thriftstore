import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ThriftStore store = new ThriftStore(); // Create the thrift store object.
        // Create a fixed thread pool to manage assistants, customers, and delivery threads.
        ExecutorService executor = Executors.newFixedThreadPool(Config.ASSISTANTS + Config.CUSTOMERS + 1);

        // Create and execute assistant threads.
        for (int i = 0; i < Config.ASSISTANTS; i++) {
            executor.execute(new Assistant(store, i));
        }

        // Create and execute customer threads.
        for (int i = 0; i < Config.CUSTOMERS; i++) {
            executor.execute(new Customer(store, i + Config.ASSISTANTS));
        }

        // Create and execute a thread to simulate deliveries arriving at the store.
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // check if a delivery should arrive based on a probability check
                if (Util.shouldDeliver()) {
                    List<Item> items = Util.generateDelivery(); // Generate a list of items to be delivered.
                    store.receiveDelivery(items); // Deliver items to the store.
                    // Log the delivery event.
                    System.out.println(Util.getLogMessage(0, "delivery", items.stream().collect(Collectors.groupingBy(Item::section, Collectors.counting())).toString()));
                }
                // Wait for the duration of a tick before continuing the loop.
                Util.sleep(Config.TICK_DURATION);
                // Increment the global tick count after each tick
                Util.incrementTickCount();
            }
        });

        // shut down the executor service after all tasks are completed
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}
