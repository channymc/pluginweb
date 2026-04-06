package com.channyanh.channyanhweb.velocity.logging;

import com.channyanh.channyanhweb.common.interfaces.logging.CommonLogger;
import com.channyanh.channyanhweb.velocity.ChannyAnhWEBVelocity;

public class VelocityLogger implements CommonLogger {
    private final ChannyAnhWEBVelocity plugin;

    public VelocityLogger(ChannyAnhWEBVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warning(String message) {
        plugin.getLogger().warn(message);
    }

    @Override
    public void error(String message) {
        plugin.getLogger().error(message);
    }
}
