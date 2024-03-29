import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
    private static final Random random = new Random(); // Random generator for various random behaviors.
    public static int tickCount = 0; // Global tick count to track the simulation time

    // sleeps the current thread for a specified number of milliseconds
    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Re-interrupt the thread to handle the interruption elsewhere
        }
    }

    // generates a random wait time for customers before attempting to buy another item.
    public static int getRandomWaitTime() {
        // Return a random wait time based on a predetermined multiplier and the tick duration.
        return (int) (random.nextDouble() * 20 * Config.TICK_DURATION);
    }

    // generates a delivery of 10 random items to be stocked in the thrift store.
    public static List<Item> generateDelivery() {
        // create a stream of 10 items, each assigned to a random section.
        return IntStream.range(0, 10).mapToObj(i -> {
            String section = Config.SECTIONS[random.nextInt(Config.SECTIONS.length)];
            return new Item("Item-" + i, section); // create a new item with a unique name and assigned section.
        }).collect(Collectors.toList());
    }

    // Determines if a delivery should occur based on a probability
    public static boolean shouldDeliver() {
        // Check if the random value falls within the probability of a delivery occurring
        return random.nextDouble() < 1.0 / Config.DELIVERY_FREQUENCY;
    }

    // Generates a log message for various actions within the thrift store simulation.
    public static String getLogMessage(int id, String action) {
        // format a basic log message including the tick count, thread ID, entity (assistant or customer), and the action performed.
        return String.format("<Tick-%d> <Thread-%d> %s=%d %s", tickCount, Thread.currentThread().getId(), getEntityName(id), id, action);
    }

    public static String getLogMessage(int id, String action, String section) {
        return getLogMessage(id, action) + " " + section;
    }

    public static String getLogMessage(int id, String action, String section, int count) {
        return getLogMessage(id, action, section) + " " + count;
    }

    private static String getEntityName(int id) {
        if (id < Config.ASSISTANTS) {
            return "Assistant";
        } else {
            return "Customer";
        }
    }

    // Increments the global tick count, representing the passage of time in the simulation.
    public static void incrementTickCount() {
        tickCount++;
    }
}