import java.util.*;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

public class ThriftStore {
    private final Map<String, Integer> inventory = new HashMap<>(); // tracks the number of items in each section
    private final Map<String, Integer> customerInterest = new HashMap<>(); // tracks interest in each section to prioritise restocking
    private final Queue<Item> deliveryBox = new LinkedList<>(); // queue of items waiting to be stocked
    private final Lock deliveryLock = new ReentrantLock(); // lock for synchronizing access to the delivery box
    private final Map<String, Lock> sectionLocks = new HashMap<>(); // locks for each section to synchronise access
    private final Map<String, Condition> sectionConditions = new HashMap<>(); // conditions for each section to manage waiting customers and assistants

    // Constructor initialises the thrift store with predefined sections.
    public ThriftStore() {
        for (String section : Config.SECTIONS) {
            inventory.put(section, 0); // Initialise inventory for each section (currently set to 0) TODO: Move this to config
            customerInterest.put(section, 0); // Initialise customer interest for each section
            sectionLocks.put(section, new ReentrantLock()); // Initialise a lock for each section
            sectionConditions.put(section, sectionLocks.get(section).newCondition()); // Initialise a condition for each section
        }
    }

    // Handles receiving a delivery of items, adding them to the delivery box.
    public void receiveDelivery(List<Item> items) {
        deliveryLock.lock();
        try {
            deliveryBox.addAll(items);
        } finally {
            deliveryLock.unlock();
        }
    }

    // Retrieves items from the delivery box for stocking, clearing the box afterwards
    public List<Item> getItemsToStock() {
        deliveryLock.lock();
        try {
            List<Item> items = new ArrayList<>(deliveryBox);
            deliveryBox.clear();
            return items;
        } finally {
            deliveryLock.unlock();
        }
    }

    // Checks if items can be stocked in a section without exceeding the max
    public boolean canStock(String section, int numItems) {
        Lock lock = sectionLocks.get(section);
        lock.lock();
        try {
            return inventory.get(section) + numItems <= Config.SECTION_MAX_ITEMS;
        } finally {
            lock.unlock();
        }
    }

    // Stocks items into a specified section and signals any waiting threads.
    public void stockItems(String section, int numItems) {
        Lock lock = sectionLocks.get(section);
        lock.lock();
        try {
            inventory.put(section, inventory.get(section) + numItems);
            sectionConditions.get(section).signalAll(); // Signal waiting customers or assistants.
        } finally {
            lock.unlock();
        }
    }

    // Checks if a customer can take an item from a section.
    public boolean canTakeItem(String section) {
        Lock lock = sectionLocks.get(section);
        lock.lock();
        try {
            return inventory.get(section) > 0;
        } finally {
            lock.unlock();
        }
    }

    // removes an item from a section's inventory when a customer takes it.
    public void takeItem(String section) {
        Lock lock = sectionLocks.get(section);
        lock.lock();
        try {
            inventory.put(section, inventory.get(section) - 1);
        } finally {
            lock.unlock();
        }
    }

    // Waits for an item to be available in a specified section.
    public void waitForItem(String section) throws InterruptedException {
        Lock lock = sectionLocks.get(section);
        lock.lock();
        try {
            while (inventory.get(section) == 0) {
                sectionConditions.get(section).await(); // Wait until an item is available
            }
        } finally {
            lock.unlock();
        }
    }

    // Increases customer interest in a section, used to prioritize restocking
    public void increaseCustomerInterest(String section) {
        customerInterest.put(section, customerInterest.get(section) + 1);
    }

    // Decreases customer interest in a section after an item has been taken.
    public void decreaseCustomerInterest(String section) {
        customerInterest.put(section, Math.max(0, customerInterest.get(section) - 1));
    }

    // Determines which sections should be prioritised for restocking based on customer interest
    public List<String> getPrioritizedSections() {
        return customerInterest.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // Sort sections by descending interest
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}