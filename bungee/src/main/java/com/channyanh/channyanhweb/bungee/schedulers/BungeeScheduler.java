package com.channyanh.channyanhweb.bungee.schedulers;

import com.channyanh.channyanhweb.bungee.ChannyAnhWEBBungee;
import com.channyanh.channyanhweb.common.interfaces.schedulers.CommonScheduler;

public class BungeeScheduler implements CommonScheduler {
    private final ChannyAnhWEBBungee plugin;
    public BungeeScheduler(ChannyAnhWEBBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void runAsync(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }
}
