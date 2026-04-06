package com.channyanh.channyanhweb.bukkit.listeners;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            // We are only interested in player-vs-player damage events
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        PlayerData attackerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(attacker.getUniqueId().toString());
        PlayerData targetData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(target.getUniqueId().toString());

        if (attackerData == null || targetData == null) {
            return;
        }

        PlayerSessionIntelData attackerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(attackerData.session_uuid);
        PlayerSessionIntelData targetSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(targetData.session_uuid);

        double damageAmount = event.getFinalDamage();

        attackerSessionIntelData.pvp_damage_given_xmin += damageAmount;
        targetSessionIntelData.pvp_damage_taken_xmin += damageAmount;
    }
}
