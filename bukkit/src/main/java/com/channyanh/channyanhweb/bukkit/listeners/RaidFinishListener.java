package com.channyanh.channyanhweb.bukkit.listeners;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;

public class RaidFinishListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRaidFinish(RaidFinishEvent event) {
        if (event.getWinners().isEmpty()) {
            // No winners in the raid, ignore the event
            return;
        }

        for (Player winner : event.getWinners()) {
            PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(winner.getUniqueId().toString());
            if (playerData == null) {
                continue;
            }

            PlayerSessionIntelData playerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
            playerSessionIntelData.raids_won_xmin = playerSessionIntelData.raids_won_xmin + 1;
        }
    }
}
