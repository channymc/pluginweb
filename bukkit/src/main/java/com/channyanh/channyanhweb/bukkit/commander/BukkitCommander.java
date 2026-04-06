package com.channyanh.channyanhweb.bukkit.commander;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.interfaces.commander.CommonCommander;
import org.bukkit.Bukkit;

public class BukkitCommander implements CommonCommander {
    @Override
    public void dispatchCommand(String command) {
        ChannyAnhWEBCommon.getInstance().getScheduler().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
