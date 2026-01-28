package de.rapha149.signgui.scheduler;

/**
 * Factory class for creating the appropriate scheduler adapter based on server type.
 * Automatically detects whether the server is running Folia or Bukkit/Paper.
 */
public final class SchedulerAdapterFactory {

    private static final boolean IS_FOLIA;
    private static volatile SchedulerAdapter instance;

    static {
        IS_FOLIA = detectFolia();
    }

    private SchedulerAdapterFactory() {
        // Utility class
    }

    /**
     * Detects if the server is running Folia by checking for Folia-specific classes.
     */
    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Returns whether the server is running Folia.
     *
     * @return true if running on Folia, false otherwise
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Gets the singleton scheduler adapter instance.
     * Creates the appropriate adapter based on the detected server type.
     *
     * @return the scheduler adapter instance
     */
    public static SchedulerAdapter getScheduler() {
        if (instance == null) {
            synchronized (SchedulerAdapterFactory.class) {
                if (instance == null) {
                    instance = IS_FOLIA ? new FoliaSchedulerAdapter() : new BukkitSchedulerAdapter();
                }
            }
        }
        return instance;
    }
}
