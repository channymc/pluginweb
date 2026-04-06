package com.channyanh.channyanhweb.velocity.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import com.channyanh.channyanhweb.velocity.ChannyAnhWEBVelocity;

public class PluginUtil {
    public static boolean checkIfPluginEnabled(String plugin) {
        ProxyServer proxyServer = ChannyAnhWEBVelocity.getPlugin().getProxyServer();
        return proxyServer.getPluginManager().getPlugin(plugin).isPresent();
    }
}
