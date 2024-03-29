import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Assistant implements Runnable {
    private final ThriftStore store;
    private final int id;
    private int ticksSinceLastBreak = 0; // tracks the number of ticks since the last break.

    public Assistant(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
    }

    // Handles the stocking of items from the delivery box into the store sections.
    private void stockItems() {
        List<Item> items = store.getItemsToStock(); // Retrieve items to stock
        if (!items.isEmpty()) {
            // Determine which sections need items most urgently.
            List<String> prioritizedSections = store.getPrioritizedSections();
            Map<String, Integer> itemsToStock = new HashMap<>();

            // Count how many items are available for each prioritized section.
            for (String section : prioritizedSections) {
                int count = (int) items.stream().filter(item -> item.section().equals(section)).count();
                if (count > 0) {
                    itemsToStock.put(section, count);
                }
            }

            // Stock each section with items until all items are stocked or no more items are left
            while (!itemsToStock.isEmpty()) {
                int totalItems = itemsToStock.values().stream().mapToInt(Integer::intValue).sum();
                int itemsToTake = Math.min(totalItems, Config.ASSISTANT_CAN_CARRY);

                for (String section : prioritizedSections) {
                    int count = itemsToStock.getOrDefault(section, 0);
                    if (count > 0) {
                        int itemsToRemove = Math.min(count, itemsToTake);
                        // Check if it's possible to stock the section and do so if possible
                        if (store.canStock(section, itemsToRemove)) {
                            System.out.println(Util.getLogMessage(id, "began_stocking_section", section, itemsToRemove));
                            int stockTimeCost = Config.STOCKING_DURATION + itemsToRemove;
                            Util.sleep(stockTimeCost * Config.TICK_DURATION);
                            store.stockItems(section, itemsToRemove);
                            System.out.println(Util.getLogMessage(id, "finished_stocking_section", section, itemsToRemove));
                            itemsToStock.put(section, count - itemsToRemove);
                            itemsToTake -= itemsToRemove;
                        }
                    }
                    if (itemsToTake == 0) {
                        break;
                    }
                }

                // remove sections that no longer need stocking.
                itemsToStock.entrySet().removeIf(entry -> entry.getValue() == 0);

                // If there are still items to stock, return to the delivery area to reassess.
                if (!itemsToStock.isEmpty()) {
                    System.out.println(Util.getLogMessage(id, "moved_to_section", "delivery_area", itemsToStock.values().stream().mapToInt(Integer::intValue).sum()));
                    Util.sleep(Config.STOCKING_DURATION * Config.TICK_DURATION);
                }
            }
        }
    }

    // Simulates the assistant taking a break
    private void takeBreak() {
        System.out.println(Util.getLogMessage(id, "began_break"));
        Util.sleep(Config.ASSISTANT_REST_DURATION * Config.TICK_DURATION);
        System.out.println(Util.getLogMessage(id, "finished_break"));
    }

    // Main run method where the assistant's tasks are executed in a loop.
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // check if it's time for the assistant to take a break.
            if (ticksSinceLastBreak >= Config.ASSISTANT_REST_FREQUENCY) {
                takeBreak();
                ticksSinceLastBreak = 0;
            } else {
                // perform item stocking tasks.
                stockItems();
                ticksSinceLastBreak++;
            }
            // wait for the duration of a tick before continuing the loop.
            Util.sleep(Config.TICK_DURATION);
        }
    }
}
