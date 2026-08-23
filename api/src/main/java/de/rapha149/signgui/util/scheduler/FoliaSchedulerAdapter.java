package de.rapha149.signgui.util.scheduler;

import de.rapha149.signgui.util.PlatformDetector;
import de.rapha149.signgui.util.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Region-aware scheduler for Folia and modern Paper.
 * <p>
 * Uses reflection so the API module can still compile against older Spigot baselines
 * while calling {@code EntityScheduler} / {@code RegionScheduler} /
 * {@code GlobalRegionScheduler} / {@code AsyncScheduler} at runtime.
 * <p>
 * On Folia, never falls back to {@link BukkitSchedulerAdapter} (that API is unsafe there).
 */
public class FoliaSchedulerAdapter implements SchedulerAdapter {

    private static final Logger LOGGER = Logger.getLogger("SignGUI");

    private static final Method GET_REGION_SCHEDULER;
    private static final Method GET_GLOBAL_REGION_SCHEDULER;
    private static final Method GET_ASYNC_SCHEDULER;
    private static final Method REGION_EXECUTE;
    private static final Method REGION_RUN_DELAYED;
    private static final Method GLOBAL_RUN;
    private static final Method GLOBAL_RUN_DELAYED;
    private static final Method ASYNC_RUN_NOW;
    private static final Method ENTITY_GET_SCHEDULER;
    private static final Method ENTITY_EXECUTE;
    private static final Method ENTITY_RUN_DELAYED;
    private static final Method IS_OWNED_BY_CURRENT_REGION;
    private static final boolean AVAILABLE;

    static {
        Method getRegionScheduler = null;
        Method getGlobalRegionScheduler = null;
        Method getAsyncScheduler = null;
        Method regionExecute = null;
        Method regionRunDelayed = null;
        Method globalRun = null;
        Method globalRunDelayed = null;
        Method asyncRunNow = null;
        Method entityGetScheduler = null;
        Method entityExecute = null;
        Method entityRunDelayed = null;
        Method isOwnedByCurrentRegion = null;
        boolean available = false;

        try {
            Class<?> regionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            Class<?> globalRegionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> asyncSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");

            Class<?> serverClass = org.bukkit.Server.class;
            getRegionScheduler = serverClass.getMethod("getRegionScheduler");
            getGlobalRegionScheduler = serverClass.getMethod("getGlobalRegionScheduler");
            getAsyncScheduler = serverClass.getMethod("getAsyncScheduler");

            regionExecute = regionSchedulerClass.getMethod("execute", org.bukkit.plugin.Plugin.class, Location.class, Runnable.class);
            regionRunDelayed = regionSchedulerClass.getMethod("runDelayed", org.bukkit.plugin.Plugin.class, Location.class,
                    Consumer.class, long.class);
            globalRun = globalRegionSchedulerClass.getMethod("run", org.bukkit.plugin.Plugin.class, Consumer.class);
            globalRunDelayed = globalRegionSchedulerClass.getMethod("runDelayed", org.bukkit.plugin.Plugin.class,
                    Consumer.class, long.class);
            asyncRunNow = asyncSchedulerClass.getMethod("runNow", org.bukkit.plugin.Plugin.class, Consumer.class);

            entityGetScheduler = Entity.class.getMethod("getScheduler");
            entityExecute = entitySchedulerClass.getMethod("execute", org.bukkit.plugin.Plugin.class, Runnable.class,
                    Runnable.class, long.class);
            entityRunDelayed = entitySchedulerClass.getMethod("runDelayed", org.bukkit.plugin.Plugin.class,
                    Consumer.class, Runnable.class, long.class);

            try {
                Class<?> regionizedServerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                isOwnedByCurrentRegion = regionizedServerClass.getMethod("isOwnedByCurrentRegion", Location.class);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                // Paper without Folia still exposes region schedulers; ownership check is Folia-only.
            }

            available = true;
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.WARNING, "SignGUI: Paper/Folia region schedulers unavailable: " + e.getMessage());
        }

        GET_REGION_SCHEDULER = getRegionScheduler;
        GET_GLOBAL_REGION_SCHEDULER = getGlobalRegionScheduler;
        GET_ASYNC_SCHEDULER = getAsyncScheduler;
        REGION_EXECUTE = regionExecute;
        REGION_RUN_DELAYED = regionRunDelayed;
        GLOBAL_RUN = globalRun;
        GLOBAL_RUN_DELAYED = globalRunDelayed;
        ASYNC_RUN_NOW = asyncRunNow;
        ENTITY_GET_SCHEDULER = entityGetScheduler;
        ENTITY_EXECUTE = entityExecute;
        ENTITY_RUN_DELAYED = entityRunDelayed;
        IS_OWNED_BY_CURRENT_REGION = isOwnedByCurrentRegion;
        AVAILABLE = available;
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    @Override
    public void runTask(JavaPlugin plugin, Runnable task) {
        try {
            Object globalScheduler = GET_GLOBAL_REGION_SCHEDULER.invoke(Bukkit.getServer());
            GLOBAL_RUN.invoke(globalScheduler, plugin, (Consumer<Object>) scheduledTask -> safeRun(task));
        } catch (ReflectiveOperationException e) {
            handleFailure(plugin, "global run", e, task);
        }
    }

    @Override
    public void runTask(JavaPlugin plugin, Entity entity, Runnable task) {
        try {
            Object entityScheduler = ENTITY_GET_SCHEDULER.invoke(entity);
            // delay=0 runs immediately when the current region owns the entity.
            ENTITY_EXECUTE.invoke(entityScheduler, plugin, (Runnable) () -> safeRun(task), null, 0L);
        } catch (ReflectiveOperationException e) {
            handleFailure(plugin, "entity run", e, task);
        }
    }

    @Override
    public void runTask(JavaPlugin plugin, Location location, Runnable task) {
        try {
            if (IS_OWNED_BY_CURRENT_REGION != null) {
                boolean owned = (boolean) IS_OWNED_BY_CURRENT_REGION.invoke(null, location);
                if (owned) {
                    safeRun(task);
                    return;
                }
            }
            Object regionScheduler = GET_REGION_SCHEDULER.invoke(Bukkit.getServer());
            REGION_EXECUTE.invoke(regionScheduler, plugin, location, (Runnable) () -> safeRun(task));
        } catch (ReflectiveOperationException e) {
            handleFailure(plugin, "region run", e, task);
        }
    }

    @Override
    public void runTaskAsynchronously(JavaPlugin plugin, Runnable task) {
        try {
            Object asyncScheduler = GET_ASYNC_SCHEDULER.invoke(Bukkit.getServer());
            ASYNC_RUN_NOW.invoke(asyncScheduler, plugin, (Consumer<Object>) scheduledTask -> safeRun(task));
        } catch (ReflectiveOperationException e) {
            handleFailure(plugin, "async run", e, task);
        }
    }

    @Override
    public void runTaskLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        try {
            Object globalScheduler = GET_GLOBAL_REGION_SCHEDULER.invoke(Bukkit.getServer());
            GLOBAL_RUN_DELAYED.invoke(globalScheduler, plugin,
                    (Consumer<Object>) scheduledTask -> safeRun(task), Math.max(1L, delayTicks));
        } catch (ReflectiveOperationException e) {
            handleFailure(plugin, "global delayed run", e, task);
        }
    }

    @Override
    public void runTaskLater(JavaPlugin plugin, Entity entity, Runnable task, long delayTicks) {
        try {
            Object entityScheduler = ENTITY_GET_SCHEDULER.invoke(entity);
            ENTITY_RUN_DELAYED.invoke(entityScheduler, plugin,
                    (Consumer<Object>) scheduledTask -> safeRun(task), null, Math.max(1L, delayTicks));
        } catch (ReflectiveOperationException e) {
            handleFailure(plugin, "entity delayed run", e, task);
        }
    }

    @Override
    public void runTaskLater(JavaPlugin plugin, Location location, Runnable task, long delayTicks) {
        try {
            Object regionScheduler = GET_REGION_SCHEDULER.invoke(Bukkit.getServer());
            REGION_RUN_DELAYED.invoke(regionScheduler, plugin, location,
                    (Consumer<Object>) scheduledTask -> safeRun(task), Math.max(1L, delayTicks));
        } catch (ReflectiveOperationException e) {
            handleFailure(plugin, "region delayed run", e, task);
        }
    }

    @Override
    public boolean isOnMainThread() {
        return Bukkit.isPrimaryThread();
    }

    private static void safeRun(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "SignGUI scheduled task failed", t);
        }
    }

    private void handleFailure(JavaPlugin plugin, String what, Exception e, Runnable task) {
        LOGGER.log(Level.SEVERE, "SignGUI failed Folia/Paper " + what, e);
        if (PlatformDetector.isFolia()) {
            throw new IllegalStateException("SignGUI cannot fall back to BukkitScheduler on Folia (" + what + ")", e);
        }
        if (plugin != null) {
            new BukkitSchedulerAdapter().runTask(plugin, task);
        } else {
            safeRun(task);
        }
    }
}
