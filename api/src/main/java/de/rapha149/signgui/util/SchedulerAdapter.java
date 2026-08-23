package de.rapha149.signgui.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Adapter interface for scheduling tasks across different server platforms.
 */
public interface SchedulerAdapter {
    
    /**
     * Runs a task synchronously on the main thread or appropriate region thread.
     *
     * @param plugin The plugin instance
     * @param task The task to run
     */
    void runTask(JavaPlugin plugin, Runnable task);
    
    /**
     * Runs a task synchronously on the main thread or appropriate region thread.
     *
     * @param plugin The plugin instance
     * @param entity The entity to run the task for (used for Folia region scheduling)
     * @param task The task to run
     */
    void runTask(JavaPlugin plugin, Entity entity, Runnable task);
    
    /**
     * Runs a task synchronously at a specific location.
     *
     * @param plugin The plugin instance
     * @param location The location to run the task at (used for Folia region scheduling)
     * @param task The task to run
     */
    void runTask(JavaPlugin plugin, Location location, Runnable task);
    
    /**
     * Runs a task asynchronously.
     *
     * @param plugin The plugin instance
     * @param task The task to run
     */
    void runTaskAsynchronously(JavaPlugin plugin, Runnable task);
    
    /**
     * Schedules a delayed task synchronously.
     *
     * @param plugin The plugin instance
     * @param task The task to run
     * @param delayTicks The delay in ticks
     */
    void runTaskLater(JavaPlugin plugin, Runnable task, long delayTicks);
    
    /**
     * Schedules a delayed task synchronously for an entity.
     *
     * @param plugin The plugin instance
     * @param entity The entity to run the task for (used for Folia region scheduling)
     * @param task The task to run
     * @param delayTicks The delay in ticks
     */
    void runTaskLater(JavaPlugin plugin, Entity entity, Runnable task, long delayTicks);
    
    /**
     * Schedules a delayed task synchronously at a location.
     *
     * @param plugin The plugin instance
     * @param location The location to run the task at (used for Folia region scheduling)
     * @param task The task to run
     * @param delayTicks The delay in ticks
     */
    void runTaskLater(JavaPlugin plugin, Location location, Runnable task, long delayTicks);
    
    /**
     * Checks if the current thread is the main server thread or appropriate region thread.
     *
     * @return true if on the main/region thread, false otherwise
     */
    boolean isOnMainThread();
}
