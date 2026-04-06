package com.channyanh.channyanhweb.bukkit.listeners;


import com.channyanh.channyanhweb.bukkit.utils.FoliaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.actions.ReportPlayerIntel;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.data.PlayerDeathData;
import com.channyanh.channyanhweb.common.data.PlayerPvpKillData;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Date;

public class EntityDeathEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Gson gson = new GsonBuilder().serializeNulls().create();

        // System.out.println("SIZE OF PLAYER DATA MAP " + ChannyAnhWEB.getPlugin().playersDataMap.size());
        // System.out.println("SIZE OF PLAYER SESSION MAP " + ChannyAnhWEB.getPlugin().playerSessionIntelDataMap.size());

        // Player Death
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PlayerData victimPlayerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(victim.getUniqueId().toString());
            if (victimPlayerData == null) {
                ChannyAnhWEBBukkit.getPlugin().getLogger().warning("Failed to send death data. Cannot find player in playerData HashMap");
                return;
            }

            // Increment player death for victim in Session
            PlayerSessionIntelData victimSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(victimPlayerData.session_uuid);
            victimSessionIntelData.deaths = victimSessionIntelData.deaths + 1;
            victimSessionIntelData.deaths_xmin = victimSessionIntelData.deaths_xmin + 1;

            // Init Building Death Data
            PlayerDeathData playerDeathData = new PlayerDeathData();
            playerDeathData.player_uuid = victim.getUniqueId().toString();
            playerDeathData.player_username = victim.getName();
            playerDeathData.died_at = new Date().getTime();
            playerDeathData.session_uuid = victimPlayerData.session_uuid;
            playerDeathData.world_name = victim.getWorld().getName();
            playerDeathData.world_location = gson.toJson(victim.getLocation().serialize());
            playerDeathData.cause = victim.getLastDamageCause() != null ? victim.getLastDamageCause().getCause().name() : null;

            // Player Death by Human: PVP
            if (event.getEntity().getKiller() != null) {
                Player killer = event.getEntity().getKiller();
                playerDeathData.killer_uuid = killer.getUniqueId().toString();
                playerDeathData.killer_username = killer.getName();

                PlayerData killerPlayerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(killer.getUniqueId().toString());
                if (killerPlayerData != null) {
                    // Increment player pvp kills for killer in Session
                    PlayerSessionIntelData killerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(killerPlayerData.session_uuid);
                    killerSessionIntelData.player_kills = killerSessionIntelData.player_kills + 1;
                    killerSessionIntelData.player_kills_xmin = killerSessionIntelData.player_kills_xmin + 1;

                    // Make and send PvP Kill Report
                    PlayerPvpKillData playerPvpKillData = new PlayerPvpKillData();
                    playerPvpKillData.killer_uuid = killer.getUniqueId().toString();
                    playerPvpKillData.killer_username = killer.getName();
                    playerPvpKillData.victim_uuid = victim.getUniqueId().toString();
                    playerPvpKillData.victim_username = victim.getName();
                    playerPvpKillData.killed_at = new Date().getTime();
                    playerPvpKillData.session_uuid = killerPlayerData.session_uuid;
                    playerPvpKillData.world_name = killer.getWorld().getName();
                    playerPvpKillData.world_location = gson.toJson(killer.getLocation().serialize());
                    playerPvpKillData.weapon = killer.getInventory().getItemInMainHand().getType().toString();

                    // REPORT HTTP
                    FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
                        try {
                            ReportPlayerIntel.reportPvpKillSync(playerPvpKillData);
                        } catch (Exception e) {
                            ChannyAnhWEBBukkit.getPlugin().getLogger().warning(e.getMessage());
                        }
                    });
                }
            }
            // Player death by Mob
            else if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent lastDamageCause) {
                playerDeathData.killer_entity_id = lastDamageCause.getDamager().getType().toString();
                playerDeathData.killer_entity_name = lastDamageCause.getDamager().getName();
            }

            FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
                try {
                    ReportPlayerIntel.reportDeathSync(playerDeathData);
                } catch (Exception e) {
                    ChannyAnhWEBBukkit.getPlugin().getLogger().warning(e.getMessage());
                }
            });
        }
        // Mob Death by Player
        else if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();

            // Increment player mob kills for killer in Session
            PlayerData killerPlayerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(killer.getUniqueId().toString());
            if (killerPlayerData != null) {
                PlayerSessionIntelData killerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(killerPlayerData.session_uuid);
                killerSessionIntelData.mob_kills = killerSessionIntelData.mob_kills + 1;
                killerSessionIntelData.mob_kills_xmin = killerSessionIntelData.mob_kills_xmin + 1;
            }
        }
    }
}
