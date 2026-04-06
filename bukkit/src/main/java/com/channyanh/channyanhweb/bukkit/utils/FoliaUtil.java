package com.channyanh.channyanhweb.bukkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class for Folia/Paper compatibility.
 * Detects at runtime whether the server is running Folia and provides
 * appropriate scheduling methods.
 */
public class FoliaUtil {

    private static Boolean isFolia = null;

    /**
     * Check if the server is running Folia.
     * Cached after first call.
     */
    public static boolean isFolia() {
        if (isFolia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                isFolia = true;
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
        }
        return isFolia;
    }

    /**
     * Schedule an async repeating task that works on both Folia and Paper/Spigot.
     * On Folia: uses Bukkit.getAsyncScheduler()
     * On Paper/Spigot: uses BukkitScheduler.runTaskTimerAsynchronously()
     *
     * @param plugin    The plugin instance
     * @param task      The runnable to execute
     * @param delayTicks    Initial delay in ticks (converted to ms for Folia)
     * @param periodTicks   Period in ticks (converted to ms for Folia)
     */
    public static void scheduleAsyncRepeating(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (isFolia()) {
            // Folia: use AsyncScheduler with milliseconds
            long delayMs = delayTicks * 50L; // 1 tick = 50ms
            long periodMs = periodTicks * 50L;
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                java.lang.reflect.Method runAtFixedRate = asyncScheduler.getClass().getMethod(
                        "runAtFixedRate",
                        org.bukkit.plugin.Plugin.class,
                        java.util.function.Consumer.class,
                        long.class,
                        long.class,
                        java.util.concurrent.TimeUnit.class
                );
                // Create a Consumer<ScheduledTask> that wraps our Runnable
                java.util.function.Consumer<?> consumer = (scheduledTask) -> task.run();
                runAtFixedRate.invoke(asyncScheduler, plugin, consumer, delayMs, periodMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                plugin.getLogger().warning("[FoliaUtil] Failed to schedule via Folia AsyncScheduler, falling back to Bukkit: " + e.getMessage());
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
            }
        } else {
            // Paper/Spigot: standard Bukkit scheduler
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        }
    }

    /**
     * Run a task asynchronously with a delay on both Folia and Paper/Spigot.
     *
     * @param plugin     The plugin instance
     * @param task       The runnable to execute
     * @param delayTicks Delay in ticks before execution (converted to ms for Folia)
     */
    public static void runAsync(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (isFolia()) {
            long delayMs = Math.max(1, delayTicks * 50L);
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                java.lang.reflect.Method runDelayed = asyncScheduler.getClass().getMethod(
                        "runDelayed",
                        org.bukkit.plugin.Plugin.class,
                        java.util.function.Consumer.class,
                        long.class,
                        java.util.concurrent.TimeUnit.class
                );
                java.util.function.Consumer<?> consumer = (scheduledTask) -> task.run();
                runDelayed.invoke(asyncScheduler, plugin, consumer, delayMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                plugin.getLogger().warning("[FoliaUtil] Failed to run delayed async via Folia, falling back to Bukkit: " + e.getMessage());
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
            }
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    /**
     * Run a task asynchronously (one-shot) on both Folia and Paper/Spigot.
     */
    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (isFolia()) {
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                java.lang.reflect.Method runNow = asyncScheduler.getClass().getMethod(
                        "runNow",
                        org.bukkit.plugin.Plugin.class,
                        java.util.function.Consumer.class
                );
                java.util.function.Consumer<?> consumer = (scheduledTask) -> task.run();
                runNow.invoke(asyncScheduler, plugin, consumer);
            } catch (Exception e) {
                plugin.getLogger().warning("[FoliaUtil] Failed to run async via Folia, falling back to Bukkit: " + e.getMessage());
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}
