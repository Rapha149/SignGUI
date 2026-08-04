package de.rapha149.signgui.util.scheduler;

import de.rapha149.signgui.util.PlatformDetector;
import de.rapha149.signgui.util.SchedulerAdapter;

/**
 * Provides the platform-appropriate {@link SchedulerAdapter}.
 * <p>
 * Prefers Paper/Folia region schedulers whenever {@code RegionScheduler} is present
 * (covers Folia and modern Paper). Falls back to the Bukkit scheduler otherwise.
 */
public final class SchedulerFactory {

    private static volatile SchedulerAdapter cachedAdapter;

    private SchedulerFactory() {
    }

    public static SchedulerAdapter getScheduler() {
        SchedulerAdapter adapter = cachedAdapter;
        if (adapter != null) {
            return adapter;
        }
        synchronized (SchedulerFactory.class) {
            if (cachedAdapter == null) {
                if (PlatformDetector.hasRegionScheduler() && FoliaSchedulerAdapter.isAvailable()) {
                    cachedAdapter = new FoliaSchedulerAdapter();
                } else {
                    cachedAdapter = new BukkitSchedulerAdapter();
                }
            }
            return cachedAdapter;
        }
    }
}
