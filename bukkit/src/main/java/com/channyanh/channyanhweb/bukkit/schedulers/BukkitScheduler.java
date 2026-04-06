package com.channyanh.channyanhweb.bukkit.schedulers;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.bukkit.utils.FoliaUtil;
import com.channyanh.channyanhweb.common.interfaces.schedulers.CommonScheduler;
import org.bukkit.Bukkit;

public class BukkitScheduler implements CommonScheduler {
    private final ChannyAnhWEBBukkit plugin;
    public BukkitScheduler(ChannyAnhWEBBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        if (FoliaUtil.isFolia()) {
            // Folia: run on global region scheduler via reflection
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                java.lang.reflect.Method runMethod = globalScheduler.getClass().getMethod(
                        "run", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class);
                java.util.function.Consumer<?> consumer = (task) -> runnable.run();
                runMethod.invoke(globalScheduler, plugin, consumer);
            } catch (Exception e) {
                // Fallback: just run it directly (already on main thread in most cases)
                runnable.run();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    @Override
    public void runAsync(Runnable runnable) {
        FoliaUtil.runAsync(plugin, runnable);
    }
}
