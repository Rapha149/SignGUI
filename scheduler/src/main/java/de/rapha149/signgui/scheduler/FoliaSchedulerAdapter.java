package de.rapha149.signgui.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Scheduler adapter implementation for Folia servers.
 * Tasks are scheduled on the appropriate region threads.
 */
public class FoliaSchedulerAdapter implements SchedulerAdapter {

    @Override
    public void runNextTick(Plugin plugin, Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> runnable.run());
    }

    @Override
    public void runNextTick(Plugin plugin, Runnable runnable, long delayTicks) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delayTicks);
    }

    @Override
    public void runAsync(Plugin plugin, Runnable runnable) {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
    }

    @Override
    public void runAsync(Plugin plugin, Runnable runnable, long delayTicks) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, task -> runnable.run(), delayTicks * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public void runAtEntity(Plugin plugin, Entity entity, Runnable runnable, Runnable retired) {
        entity.getScheduler().run(plugin, task -> runnable.run(), retired);
    }

    @Override
    public void runAtEntity(Plugin plugin, Entity entity, Runnable runnable, Runnable retired, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, task -> runnable.run(), retired, delayTicks);
    }

    @Override
    public void runAtLocation(Plugin plugin, Location location, Runnable runnable) {
        Bukkit.getRegionScheduler().run(plugin, location, task -> runnable.run());
    }

    @Override
    public void runAtLocation(Plugin plugin, Location location, Runnable runnable, long delayTicks) {
        Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> runnable.run(), delayTicks);
    }
}
