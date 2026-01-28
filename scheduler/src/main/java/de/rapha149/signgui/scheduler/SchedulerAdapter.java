package de.rapha149.signgui.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter interface for scheduling tasks across different server implementations.
 * Supports both Bukkit/Paper and Folia schedulers.
 */
public interface SchedulerAdapter {

    /**
     * Runs a task on the next server tick (main thread for Bukkit, global region for Folia).
     *
     * @param plugin   the plugin owning this task
     * @param runnable the task to execute
     */
    void runNextTick(Plugin plugin, Runnable runnable);

    /**
     * Runs a task after a delay (main thread for Bukkit, global region for Folia).
     *
     * @param plugin   the plugin owning this task
     * @param runnable the task to execute
     * @param delayTicks the delay in ticks before execution
     */
    void runNextTick(Plugin plugin, Runnable runnable, long delayTicks);

    /**
     * Runs a task asynchronously.
     *
     * @param plugin   the plugin owning this task
     * @param runnable the task to execute
     */
    void runAsync(Plugin plugin, Runnable runnable);

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param plugin   the plugin owning this task
     * @param runnable the task to execute
     * @param delayTicks the delay in ticks before execution
     */
    void runAsync(Plugin plugin, Runnable runnable, long delayTicks);

    /**
     * Runs a task on the thread that owns the given entity.
     * On Bukkit, this runs on the main thread.
     * On Folia, this runs on the entity's region thread.
     *
     * @param plugin   the plugin owning this task
     * @param entity   the entity to run the task for
     * @param runnable the task to execute
     * @param retired  called if the entity is removed before the task executes (may be null)
     */
    void runAtEntity(Plugin plugin, Entity entity, Runnable runnable, @Nullable Runnable retired);

    /**
     * Runs a task on the thread that owns the given entity after a delay.
     * On Bukkit, this runs on the main thread.
     * On Folia, this runs on the entity's region thread.
     *
     * @param plugin   the plugin owning this task
     * @param entity   the entity to run the task for
     * @param runnable the task to execute
     * @param retired  called if the entity is removed before the task executes (may be null)
     * @param delayTicks the delay in ticks before execution
     */
    void runAtEntity(Plugin plugin, Entity entity, Runnable runnable, @Nullable Runnable retired, long delayTicks);

    /**
     * Runs a task on the thread that owns the given location.
     * On Bukkit, this runs on the main thread.
     * On Folia, this runs on the region thread that owns the location.
     *
     * @param plugin   the plugin owning this task
     * @param location the location to run the task at
     * @param runnable the task to execute
     */
    void runAtLocation(Plugin plugin, Location location, Runnable runnable);

    /**
     * Runs a task on the thread that owns the given location after a delay.
     * On Bukkit, this runs on the main thread.
     * On Folia, this runs on the region thread that owns the location.
     *
     * @param plugin   the plugin owning this task
     * @param location the location to run the task at
     * @param runnable the task to execute
     * @param delayTicks the delay in ticks before execution
     */
    void runAtLocation(Plugin plugin, Location location, Runnable runnable, long delayTicks);
}
