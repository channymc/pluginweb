package com.channyanh.channyanhweb.bukkit.listeners;

import com.channyanh.channyanhweb.bukkit.hooks.auth.AuthPluginHook;
import com.channyanh.channyanhweb.bukkit.utils.FoliaUtil;
import com.google.gson.Gson;
import com.viaversion.viaversion.api.Via;
import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.bukkit.utils.PlayerUtil;
import com.channyanh.channyanhweb.bukkit.utils.SkinUtil;
import com.channyanh.channyanhweb.common.actions.FetchPlayerData;
import com.channyanh.channyanhweb.common.actions.ReportPlayerIntel;
import com.channyanh.channyanhweb.common.actions.ReportServerChat;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import com.channyanh.channyanhweb.common.data.PlayerWorldStatsIntelData;
import com.channyanh.channyanhweb.common.responses.GenericApiResponse;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;
import com.channyanh.channyanhweb.common.utils.VersionUtil;
import com.channyanh.channyanhweb.common.utils.WhoisUtil;
import de.themoep.minedown.adventure.MineDown;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class PlayerJoinLeaveListener implements Listener {

    private static PlayerJoinLeaveListener instance;

    public PlayerJoinLeaveListener() {
        instance = this;
    }

    public static PlayerJoinLeaveListener getInstance() {
        return instance;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        // If an auth plugin (nLogin/AuthMe) is active, defer ALL processing
        // until the player actually authenticates. The auth hook will call
        // processDeferredJoin() after successful authentication.
        if (AuthPluginHook.isAuthPluginActive()) {
            ChannyAnhWEBBukkit.getPlugin().getLogger().info(
                    "[AuthHook] Deferring join processing for " + p.getName() +
                    " until " + AuthPluginHook.getAuthPluginName() + " authentication.");
            return;
        }

        // No auth plugin - process immediately
        processJoin(event.getPlayer(), event);
    }

    /**
     * Called by auth plugin hooks (NLoginHook, AuthMeHook) after the player authenticates.
     * Performs all the deferred join processing (data fetch, whois, session init, etc.)
     */
    public static void processDeferredJoin(Player player) {
        if (player == null || !player.isOnline()) return;
        if (instance == null) return;

        // Create a synthetic context for the deferred processing
        instance.addPlayerToPlayerDataMapAndStartSessionForPlayer(player);

        if (!PlayerUtil.isVanished(player)) {
            instance.postSendJoinChatlog(player);
            instance.broadcastWhoisForPlayer(player);
        }

        if (ChannyAnhWEBBukkit.getPlugin().getIsRemindPlayerToLinkEnabled()) {
            FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
                instance.runAccountLinkReminder(player);
            }, 20L * 30L);
        }
    }

    /**
     * Process join immediately (when no auth plugin is present).
     */
    private void processJoin(Player p, PlayerJoinEvent event) {
        this.addPlayerToPlayerDataMapAndStartSession(event);

        if (!PlayerUtil.isVanished(p)) {
            this.postSendChatlog(event);
            this.broadcastWhoisForPlayer(event.getPlayer());
        }

        if (ChannyAnhWEBBukkit.getPlugin().getIsRemindPlayerToLinkEnabled()) {
            FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
                runAccountLinkReminder(event.getPlayer());
            }, 20L * 30L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        // send chatlog to web
        this.postSendChatlog(event);

        // remove player from playerDataList list
        this.removePlayerAndSessionFromDataMap(event);

        // Remove from joinAddressCache
        ChannyAnhWEBBukkit.getPlugin().joinAddressCache.remove(event.getPlayer().getUniqueId().toString());

        // Remove from playerSkinCache
        ChannyAnhWEBBukkit.getPlugin().getPlayerSkinCache().remove(event.getPlayer().getUniqueId().toString());

        // Remove auth state
        AuthPluginHook.removePlayer(event.getPlayer().getUniqueId());
    }

    private void postSendChatlog(PlayerEvent event) {
        if (!ChannyAnhWEBBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        String chatMessage = "";
        String chatType = "";
        if (event instanceof PlayerJoinEvent) {
            chatType = "player-join";
            chatMessage = ((PlayerJoinEvent) event).getJoinMessage();
        } else {
            chatType = "player-leave";
            chatMessage = ((PlayerQuitEvent) event).getQuitMessage();
        }
        ReportServerChat.reportAsync(
                chatType,
                chatMessage,
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId().toString()
        );
    }

    /**
     * Send a join chatlog for deferred join (no PlayerJoinEvent available).
     */
    private void postSendJoinChatlog(Player player) {
        if (!ChannyAnhWEBBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }
        ReportServerChat.reportAsync(
                "player-join",
                player.getName() + " joined the game",
                player.getName(),
                player.getUniqueId().toString()
        );
    }

    /**
     * Start session for deferred join (no PlayerJoinEvent available).
     * Used when auth plugin (nLogin/AuthMe) gates the join processing.
     */
    private void addPlayerToPlayerDataMapAndStartSessionForPlayer(Player player) {
        String userName = player.getName();
        String uuid = player.getUniqueId().toString();
        FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
            try {
                GenericApiResponse response = FetchPlayerData.getSync(userName, uuid);
                PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().getCommon().getGson().fromJson(response.getData(), PlayerData.class);
                playerData.last_active_timestamp = System.currentTimeMillis();

                PlayerSessionIntelData playerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().getCommon().getGson().fromJson(response.getData(), PlayerSessionIntelData.class);
                playerSessionIntelData.session_uuid = UUID.randomUUID().toString();
                playerSessionIntelData.session_started_at = new Date().getTime();
                playerSessionIntelData.ip_address = player.getAddress() != null ? player.getAddress().getHostString() : "127.1.1.1";
                playerSessionIntelData.display_name = ChatColor.stripColor(player.getDisplayName());
                playerSessionIntelData.is_op = player.isOp();
                playerSessionIntelData.resetStats();
                try {
                    playerSessionIntelData.join_address = ChannyAnhWEBBukkit.getPlugin().joinAddressCache.get(player.getUniqueId().toString());
                } catch (Exception e) {
                    playerSessionIntelData.join_address = null;
                }

                int playerPing;
                try {
                    playerPing = player.getPing();
                } catch (NoSuchMethodError e) {
                    playerPing = 0;
                }
                playerSessionIntelData.player_ping = playerPing;

                if (ChannyAnhWEBBukkit.getPlugin().getHasViaVersion()) {
                    int playerProtocolVersion = Via.getAPI().getPlayerVersion(player.getUniqueId());
                    playerSessionIntelData.minecraft_version = VersionUtil.getMinecraftVersionFromProtoId(playerProtocolVersion);
                }
                if (ChannyAnhWEBBukkit.getPlugin().getHasSkinsRestorer()) {
                    updateSkinDataInPlayerIntel(playerSessionIntelData, player);
                }

                playerSessionIntelData.server_id = ChannyAnhWEBBukkit.getPlugin().getApiServerId();
                playerSessionIntelData.players_world_stat_intel = new HashMap<>();
                for (World world : ChannyAnhWEBBukkit.getPlugin().getServer().getWorlds()) {
                    playerSessionIntelData.players_world_stat_intel.put(world.getName(), new PlayerWorldStatsIntelData(world.getName()));
                }

                playerData.session_uuid = playerSessionIntelData.session_uuid;
                ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.put(playerSessionIntelData.session_uuid, playerSessionIntelData);
                ChannyAnhWEBBukkit.getPlugin().playersDataMap.put(playerData.uuid, playerData);

                ReportPlayerIntel.initSessionSync(playerSessionIntelData);
            } catch (Exception e) {
                LoggingUtil.warntrace(e);
            }
        });
    }

    private void broadcastWhoisForPlayer(Player player) {
        String username = player.getName();
        String ipAddress = Objects.requireNonNull(player.getAddress()).getHostString();
        String uuid = player.getUniqueId().toString();
        Boolean shouldBroadcastOnJoin = ChannyAnhWEBBukkit.getPlugin().getIsWhoisOnPlayerJoinEnabled();

        FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
            List<String> sayList = WhoisUtil.forPlayerSync(
                    username,
                    uuid,
                    ipAddress,
                    shouldBroadcastOnJoin,
                    true,
                    null,
                    ChannyAnhWEBBukkit.getPlugin().getWhoisNoMatchFoundMessage(),
                    ChannyAnhWEBBukkit.getPlugin().getWhoisPlayerOnFirstJoinMessage(),
                    ChannyAnhWEBBukkit.getPlugin().getWhoisPlayerOnJoinMessage(),
                    ChannyAnhWEBBukkit.getPlugin().getWhoisPlayerOnCommandMessage(),
                    ChannyAnhWEBBukkit.getPlugin().getWhoisPlayerOnAdminCommandMessage(),
                    ChannyAnhWEBBukkit.getPlugin().getWhoisMultiplePlayersTitleMessage(),
                    ChannyAnhWEBBukkit.getPlugin().getWhoisMultiplePlayersListMessage()
            );
            if (sayList != null) {
                for (String line : sayList) {
                    ChannyAnhWEBBukkit.getPlugin().adventure().players().sendMessage(MineDown.parse(line));
                }
            }
        });
    }

    private void addPlayerToPlayerDataMapAndStartSession(PlayerJoinEvent event) {
        String userName = event.getPlayer().getName();
        String uuid = event.getPlayer().getUniqueId().toString();
        FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
            try {
                GenericApiResponse response = FetchPlayerData.getSync(userName, uuid);
                PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().getCommon().getGson().fromJson(response.getData(), PlayerData.class);
                playerData.last_active_timestamp = System.currentTimeMillis();

                PlayerSessionIntelData playerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().getCommon().getGson().fromJson(response.getData(), PlayerSessionIntelData.class);
                playerSessionIntelData.session_uuid = UUID.randomUUID().toString();
                playerSessionIntelData.session_started_at = new Date().getTime();
                playerSessionIntelData.ip_address = event.getPlayer().getAddress() != null ? event.getPlayer().getAddress().getHostString() : "127.1.1.1";
                playerSessionIntelData.display_name = ChatColor.stripColor(event.getPlayer().getDisplayName());
                playerSessionIntelData.session_started_at = new Date().getTime();
                playerSessionIntelData.is_op = event.getPlayer().isOp();
                playerSessionIntelData.resetStats(); // Becoz play_time, afk_time is getting set as total of player from api response.
                try {
                    playerSessionIntelData.join_address = ChannyAnhWEBBukkit.getPlugin().joinAddressCache.get(event.getPlayer().getUniqueId().toString());
                } catch (Exception e) {
                    playerSessionIntelData.join_address = null;
                }

                int playerPing;
                try {
                    playerPing = event.getPlayer().getPing();
                } catch (NoSuchMethodError e) {
                    playerPing = 0;
                }
                playerSessionIntelData.player_ping = playerPing;

                if (ChannyAnhWEBBukkit.getPlugin().getHasViaVersion()) {
                    int playerProtocolVersion = Via.getAPI().getPlayerVersion(event.getPlayer().getUniqueId());
                    playerSessionIntelData.minecraft_version = VersionUtil.getMinecraftVersionFromProtoId(playerProtocolVersion);
                }
                if (ChannyAnhWEBBukkit.getPlugin().getHasSkinsRestorer()) {
                    updateSkinDataInPlayerIntel(playerSessionIntelData, event.getPlayer());
                }

                playerSessionIntelData.server_id = ChannyAnhWEBBukkit.getPlugin().getApiServerId();
                // Init world stats hashmap for each world
                playerSessionIntelData.players_world_stat_intel = new HashMap<>();
                for (World world : ChannyAnhWEBBukkit.getPlugin().getServer().getWorlds()) {
                    playerSessionIntelData.players_world_stat_intel.put(world.getName(), new PlayerWorldStatsIntelData(world.getName()));
                }

                playerData.session_uuid = playerSessionIntelData.session_uuid;
                ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.put(playerSessionIntelData.session_uuid, playerSessionIntelData);
                ChannyAnhWEBBukkit.getPlugin().playersDataMap.put(playerData.uuid, playerData);

                ReportPlayerIntel.initSessionSync(playerSessionIntelData);
            } catch (Exception e) {
                LoggingUtil.warntrace(e);
            }
        });
    }

    private void updateSkinDataInPlayerIntel(PlayerSessionIntelData playerSessionIntelData, Player player) {
        if (ChannyAnhWEBBukkit.getPlugin().getHasSkinsRestorerInProxyMode()) {
            // get from playerSkinCache (from Bungee)
            String[] skinArr = ChannyAnhWEBBukkit.getPlugin().getPlayerSkinCache().get(player.getUniqueId().toString());
            if (skinArr != null) {
                playerSessionIntelData.skin_property = skinArr[0];
                playerSessionIntelData.skin_texture_id = skinArr[1];
            }
        } else {
            // get from SkinsRestorer API
            SkinProperty skin = SkinUtil.getSkinForPlayer(player.getUniqueId(), player.getName());
            if (skin != null) {
                playerSessionIntelData.skin_property = ChannyAnhWEBBukkit.getPlugin().getGson().toJson(skin);
                playerSessionIntelData.skin_texture_id = PropertyUtils.getSkinTextureUrlStripped(skin);
            }
        }
    }

    private void removePlayerAndSessionFromDataMap(PlayerQuitEvent event) {
        Gson gson = ChannyAnhWEBBukkit.getPlugin().getGson();
        String key = event.getPlayer().getUniqueId().toString();

        PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(key);
        if (playerData != null) {
            LoggingUtil.debug("REPORT FINAL SESSION END ON PLAYER QUIT");
            PlayerSessionIntelData leftPlayerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
            if (leftPlayerSessionIntelData == null) {
                // Session not found (player may have quit before session was fully initialized)
                ChannyAnhWEBBukkit.getPlugin().playersDataMap.remove(key);
                return;
            }
            leftPlayerSessionIntelData.session_ended_at = new Date().getTime();

            // Get Vault Plugin Data.
            leftPlayerSessionIntelData.vault_balance = ChannyAnhWEBBukkit.getVaultEconomy() != null ? ChannyAnhWEBBukkit.getVaultEconomy().getBalance(event.getPlayer()) : 0;
            if (ChannyAnhWEBBukkit.getVaultPermission() != null && ChannyAnhWEBBukkit.getVaultPermission().hasGroupSupport()) {
                leftPlayerSessionIntelData.vault_groups = ChannyAnhWEBBukkit.getVaultPermission().getPlayerGroups(event.getPlayer());
            }

            // Player Inventory
            if (ChannyAnhWEBBukkit.getPlugin().getIsSendInventoryDataToPlayerIntel()) {
                leftPlayerSessionIntelData.inventory = gson.toJson(event.getPlayer().getInventory().getContents());
                leftPlayerSessionIntelData.ender_chest = gson.toJson(event.getPlayer().getEnderChest().getContents());
            }

            // Player World location
            leftPlayerSessionIntelData.world_location = gson.toJson(event.getPlayer().getLocation().serialize());
            leftPlayerSessionIntelData.world_name = event.getPlayer().getWorld().getName();

            // REMOVE SESSION TO MAP
            ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.remove(playerData.session_uuid);
            FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        ReportPlayerIntel.reportEventSync(leftPlayerSessionIntelData);
                    } catch (Exception e) {
                        ChannyAnhWEBBukkit.getPlugin().getLogger().warning(e.getMessage());
                    }
                }
            });
        }

        // Remove player data map.
        ChannyAnhWEBBukkit.getPlugin().playersDataMap.remove(key);
    }

    private void runAccountLinkReminder(Player player) {
        // If player is not online, return.
        if (!player.isOnline()) {
            return;
        }

        Boolean isAlreadyLinkedReminderEnabled = ChannyAnhWEBBukkit.getPlugin().getIsRemindPlayerWhenAlreadyLinkedEnabled();
        PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());

        if (playerData == null) {
            return;
        }
        if (playerData.is_verified && !isAlreadyLinkedReminderEnabled) {
            return;
        }

        List<String> messageList = playerData.is_verified ? ChannyAnhWEBBukkit.getPlugin().getRemindPlayerWhenAlreadyLinkedMessage() : ChannyAnhWEBBukkit.getPlugin().getRemindPlayerToLinkMessage();
        for (String line : messageList) {
            line = line.replace("{LINK_URL}", ChannyAnhWEBHttpUtil.getUrl(ChannyAnhWEBHttpUtil.ACCOUNT_LINK_ROUTE));
            line = line.replace("{WEB_URL}", ChannyAnhWEBBukkit.getPlugin().getApiHost());
            line = line.replace("{PROFILE_URL}", playerData.profile_link.isBlank() ? "-" : playerData.profile_link);
            ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
        }
    }
}
