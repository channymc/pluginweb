package com.channyanh.channyanhweb.velocity.schedulers;

import com.channyanh.channyanhweb.common.interfaces.schedulers.CommonScheduler;
import com.channyanh.channyanhweb.velocity.ChannyAnhWEBVelocity;

public class VelocityScheduler implements CommonScheduler {
    private final ChannyAnhWEBVelocity plugin;

    public VelocityScheduler(ChannyAnhWEBVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        plugin.getProxyServer().getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public void runAsync(Runnable runnable) {
        plugin.getProxyServer().getScheduler().buildTask(plugin, runnable).schedule();
    }
}
