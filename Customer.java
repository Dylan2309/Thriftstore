import java.util.Random;

public class Customer implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random(); // random generator for selecting sections and simulating behavior.

    public Customer(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
    }

    // main run method where the customer's actions are performed in a loop
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // Randomly select a section from which to attempt to buy an item.
            String section = Config.SECTIONS[random.nextInt(Config.SECTIONS.length)];
            // Increase interest in the selected section to influence assistant stocking priorities
            store.increaseCustomerInterest(section);
            try {
                int waitTicks = 0; // Tracks how long the customer waits for an item
                // Wait until the item is available in the selected section.
                while (!store.canTakeItem(section)) {
                    long startTime = System.currentTimeMillis();
                    store.waitForItem(section);
                    long endTime = System.currentTimeMillis();
                    waitTicks += (int) ((endTime - startTime) / Config.TICK_DURATION);
                }
                // take the item from the section.
                store.takeItem(section);
                // log the event of taking an item.
                System.out.println(Util.getLogMessage(id, "collected_from_section", section + " waited_ticks=" + waitTicks));
                // decrease interest in the section as the need has been fulfilled.
                store.decreaseCustomerInterest(section);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Simulate time before the customer attempts to buy another item.
            Util.sleep(Util.getRandomWaitTime());
        }
    }
}