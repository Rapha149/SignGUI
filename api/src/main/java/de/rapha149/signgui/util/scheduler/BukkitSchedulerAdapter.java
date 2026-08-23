package de.rapha149.signgui.util.scheduler;

import de.rapha149.signgui.util.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Standard Bukkit/Spigot/Paper scheduler implementation.
 * Also used for CanvasMC and Archlight which are compatible with Bukkit API.
 */
public class BukkitSchedulerAdapter implements SchedulerAdapter {
    
    @Override
    public void runTask(JavaPlugin plugin, Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    @Override
    public void runTask(JavaPlugin plugin, Entity entity, Runnable task) {
        runTask(plugin, task);
    }
    
    @Override
    public void runTask(JavaPlugin plugin, Location location, Runnable task) {
        runTask(plugin, task);
    }
    
    @Override
    public void runTaskAsynchronously(JavaPlugin plugin, Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        } else {
            task.run();
        }
    }
    
    @Override
    public void runTaskLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
    
    @Override
    public void runTaskLater(JavaPlugin plugin, Entity entity, Runnable task, long delayTicks) {
        runTaskLater(plugin, task, delayTicks);
    }
    
    @Override
    public void runTaskLater(JavaPlugin plugin, Location location, Runnable task, long delayTicks) {
        runTaskLater(plugin, task, delayTicks);
    }
    
    @Override
    public boolean isOnMainThread() {
        return Bukkit.isPrimaryThread();
    }
}
