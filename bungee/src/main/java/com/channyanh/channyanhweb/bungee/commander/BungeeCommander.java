package com.channyanh.channyanhweb.bungee.commander;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.interfaces.commander.CommonCommander;
import net.md_5.bungee.api.ProxyServer;

public class BungeeCommander implements CommonCommander {
    @Override
    public void dispatchCommand(String command) {
        ChannyAnhWEBCommon.getInstance().getScheduler().run(() -> ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command));
    }
}
