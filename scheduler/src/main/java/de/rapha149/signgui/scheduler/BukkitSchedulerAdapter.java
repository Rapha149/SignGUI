package de.rapha149.signgui.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Scheduler adapter implementation for Bukkit/Paper/Spigot servers.
 * All tasks run on the main server thread except for async tasks.
 */
public class BukkitSchedulerAdapter implements SchedulerAdapter {

    @Override
    public void runNextTick(Plugin plugin, Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runNextTick(Plugin plugin, Runnable runnable, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    @Override
    public void runAsync(Plugin plugin, Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void runAsync(Plugin plugin, Runnable runnable, long delayTicks) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
    }

    @Override
    public void runAtEntity(Plugin plugin, Entity entity, Runnable runnable, @Nullable Runnable retired) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (entity.isValid()) {
                runnable.run();
            } else if (retired != null) {
                retired.run();
            }
        });
    }

    @Override
    public void runAtEntity(Plugin plugin, Entity entity, Runnable runnable, @Nullable Runnable retired, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid()) {
                runnable.run();
            } else if (retired != null) {
                retired.run();
            }
        }, delayTicks);
    }

    @Override
    public void runAtLocation(Plugin plugin, Location location, Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runAtLocation(Plugin plugin, Location location, Runnable runnable, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }
}
