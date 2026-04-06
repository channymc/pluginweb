package com.channyanh.channyanhweb.bukkit.listeners;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null)
        {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        playerSessionIntelData.items_placed_xmin = playerSessionIntelData.items_placed_xmin + 1;
        playerSessionIntelData.items_used_xmin = playerSessionIntelData.items_used_xmin + 1;
    }
}
