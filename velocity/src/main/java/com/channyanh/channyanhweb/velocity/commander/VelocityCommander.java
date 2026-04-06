package com.channyanh.channyanhweb.velocity.commander;

import com.channyanh.channyanhweb.common.interfaces.commander.CommonCommander;
import com.channyanh.channyanhweb.velocity.ChannyAnhWEBVelocity;

public class VelocityCommander implements CommonCommander {
    private final ChannyAnhWEBVelocity plugin;

    public VelocityCommander(ChannyAnhWEBVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void dispatchCommand(String command) {
        this.plugin.getProxyServer().getCommandManager().executeAsync(this.plugin.getProxyServer().getConsoleCommandSource(), command);
    }
}
