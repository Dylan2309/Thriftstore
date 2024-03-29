public class Config {
    public static final String[] SECTIONS = {
            "electronics",
            "clothing",
            "furniture",
            "toys",
            "sporting goods",
            "books"
    };
    public static final int DELIVERY_FREQUENCY = 100; // Average frequency of deliveries per day (1000 ticks)
    public static final int TICK_DURATION = 100; // Duration of a tick in milliseconds
    public static final int ASSISTANTS = 5; // Number of assistant threads in the simulation
    public static final int ASSISTANT_CAN_CARRY = 10; // Maximum number of items an assistant can carry
    public static final int STOCKING_DURATION = 10; // Base time it takes for an assistant to stock to a section
    public static final int SECTION_MAX_ITEMS = 10; // Maximum number of items that can be stocked in each section
    public static final int ASSISTANT_REST_FREQUENCY = 200; // Ticks after which an assistant takes a break
    public static final int ASSISTANT_REST_DURATION = 150; // Duration of an assistant's break in ticks
    public static final int CUSTOMERS = 5; // Number of customer threads in the simulation
}