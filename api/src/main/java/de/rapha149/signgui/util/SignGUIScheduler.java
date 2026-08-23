package de.rapha149.signgui.util;

import de.rapha149.signgui.SignGUIBridge;
import de.rapha149.signgui.util.scheduler.SchedulerFactory;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Advanced SignGUI scheduling facade for Folia, Paper, and Bukkit.
 * <p>
 * Prefers Paper/Folia region schedulers ({@code EntityScheduler}, {@code RegionScheduler},
 * {@code GlobalRegionScheduler}, {@code AsyncScheduler}) and falls back to the Bukkit
 * scheduler on platforms that do not expose them.
 * <p>
 * On Folia, a plugin must be bound via {@link #setPlugin(JavaPlugin)} (usually through
 * {@code SignGUIBuilder#callHandlerSynchronously(JavaPlugin)}) before opening a sign GUI.
 */
public final class SignGUIScheduler {

    private static final ScheduledExecutorService LEGACY_FALLBACK =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "SignGUI-LegacyScheduler");
                t.setDaemon(true);
                return t;
            });

    private static volatile JavaPlugin plugin;

    private SignGUIScheduler() {
    }

    /**
     * Binds the owning plugin used for Folia/Paper region scheduling.
     *
     * @param plugin the plugin instance, may be null to clear
     */
    public static void setPlugin(JavaPlugin plugin) {
        SignGUIScheduler.plugin = plugin;
        if (plugin != null) {
            // Wire NMS reopen delays through Folia/Paper entity schedulers.
            SignGUIBridge.setDelayedScheduler(SignGUIScheduler::runLater);
        }
    }

    /**
     * @return the bound plugin, or null if none was set
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Runs {@code task} on the player's owning region/entity thread (or the main thread on Bukkit).
     */
    public static void run(Player player, Runnable task) {
        JavaPlugin p = requirePluginForFolia();
        if (p != null && player != null) {
            SchedulerFactory.getScheduler().runTask(p, player, task);
            return;
        }
        if (p != null) {
            SchedulerFactory.getScheduler().runTask(p, task);
            return;
        }
        task.run();
    }

    /**
     * Runs {@code task} on the region that owns {@code location}.
     */
    public static void runAt(Location location, Runnable task) {
        JavaPlugin p = requirePluginForFolia();
        if (p != null && location != null && location.getWorld() != null) {
            SchedulerFactory.getScheduler().runTask(p, location, task);
            return;
        }
        if (p != null) {
            SchedulerFactory.getScheduler().runTask(p, task);
            return;
        }
        task.run();
    }

    /**
     * Runs {@code task} on the global region / main thread.
     */
    public static void runGlobal(Runnable task) {
        JavaPlugin p = requirePluginForFolia();
        if (p != null) {
            SchedulerFactory.getScheduler().runTask(p, task);
            return;
        }
        task.run();
    }

    /**
     * Runs {@code task} off the tick threads (Folia {@code AsyncScheduler} / Bukkit async).
     */
    public static void runAsync(Runnable task) {
        JavaPlugin p = requirePluginForFolia();
        if (p != null) {
            SchedulerFactory.getScheduler().runTaskAsynchronously(p, task);
            return;
        }
        LEGACY_FALLBACK.execute(task);
    }

    /**
     * Delayed entity-owned execution used by NMS wrappers when reopening a sign (~4 ticks ≈ 200ms).
     */
    public static void runLater(Player player, Location location, Runnable task, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        JavaPlugin p = plugin;
        de.rapha149.signgui.util.SchedulerAdapter scheduler = SchedulerFactory.getScheduler();

        if (p != null && player != null && player.isOnline()) {
            scheduler.runTaskLater(p, player, task, delay);
            return;
        }
        if (p != null && location != null && location.getWorld() != null) {
            scheduler.runTaskLater(p, location, task, delay);
            return;
        }
        if (p != null) {
            scheduler.runTaskLater(p, task, delay);
            return;
        }

        if (PlatformDetector.isFolia()) {
            throw new IllegalStateException(
                    "SignGUI on Folia requires callHandlerSynchronously(plugin) so delayed opens can use the entity scheduler.");
        }

        LEGACY_FALLBACK.schedule(task, delay * 50L, TimeUnit.MILLISECONDS);
    }

    private static JavaPlugin requirePluginForFolia() {
        JavaPlugin p = plugin;
        if (p == null && PlatformDetector.isFolia()) {
            throw new IllegalStateException(
                    "SignGUI on Folia requires callHandlerSynchronously(plugin) (or SignGUIScheduler.setPlugin).");
        }
        return p;
    }
}
