package com.channyanh.channyanhweb.bukkit.listeners;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.actions.ReportServerChat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.BroadcastMessageEvent;

public class ServerBroadcastListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerBroadcastMessage(BroadcastMessageEvent event) {
        if (!ChannyAnhWEBBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        LoggingUtil.info("Server Broadcasting: " + event.getMessage());

        ReportServerChat.reportAsync(
                "server-broadcast",
                event.getMessage(),
                null,
                null
        );
    }
}
