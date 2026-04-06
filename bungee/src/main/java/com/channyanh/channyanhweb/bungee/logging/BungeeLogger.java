package com.channyanh.channyanhweb.bungee.logging;

import com.channyanh.channyanhweb.bungee.ChannyAnhWEBBungee;
import com.channyanh.channyanhweb.common.interfaces.logging.CommonLogger;

import java.util.logging.Level;

public class BungeeLogger implements CommonLogger {
    private final ChannyAnhWEBBungee plugin;

    public BungeeLogger(ChannyAnhWEBBungee plugin) {
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
