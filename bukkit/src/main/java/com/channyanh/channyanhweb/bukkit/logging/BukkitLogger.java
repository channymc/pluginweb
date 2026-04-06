package com.channyanh.channyanhweb.bukkit.logging;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.interfaces.logging.CommonLogger;

import java.util.logging.Level;

public class BukkitLogger implements CommonLogger {
    private final ChannyAnhWEBBukkit plugin;

    public BukkitLogger(ChannyAnhWEBBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        plugin.getLogger().log(Level.FINE, message);
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warning(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void error(String message) {
        plugin.getLogger().severe(message);
    }
}
