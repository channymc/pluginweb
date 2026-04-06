package com.channyanh.channyanhweb.bukkit.listeners;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.actions.ReportServerChat;
import com.channyanh.channyanhweb.common.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!ChannyAnhWEBBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        // Report Chat
        String message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        ReportServerChat.reportAsync(
                "player-chat",
                message,
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId().toString()
        );

        // Update player last active timestamp since chat is activity
        PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(event.getPlayer().getUniqueId().toString());
        if (playerData != null) {
            playerData.last_active_timestamp = System.currentTimeMillis();
        }
    }
}
