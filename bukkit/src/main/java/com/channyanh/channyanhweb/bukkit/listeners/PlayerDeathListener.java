package com.channyanh.channyanhweb.bukkit.listeners;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.actions.ReportServerChat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!ChannyAnhWEBBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        // If death is of a valid human player then only proceed
        if (event.getEntity().getPlayer() != null) {
            ReportServerChat.reportAsync(
                    "player-death",
                    event.getDeathMessage(),
                    event.getEntity().getPlayer().getName(),
                    event.getEntity().getPlayer().getUniqueId().toString()
            );
        }
    }
}
