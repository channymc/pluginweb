package com.channyanh.channyanhweb.bukkit.commands;


import com.channyanh.channyanhweb.bukkit.utils.FoliaUtil;
import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.utils.WhoisUtil;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PlayerWhoisCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        // Do nothing if the feature is disabled.
        if (!ChannyAnhWEBBukkit.getPlugin().getIsWhoisOnCommandEnabled()) {
            return false;
        }

        String username = null;
        if (strings.length > 0) {
            username = strings[0];
        }
        boolean shouldBroadcast = false;
        Player senderPlayer = null;
        if ((commandSender instanceof Player)) {
            senderPlayer = (Player) commandSender;
        } else {
            shouldBroadcast = true;
        }

        if (username == null) {
            if (senderPlayer != null) {
                username = senderPlayer.getName();
            } else {
                ChannyAnhWEBBukkit.getPlugin().getLogger().info("Username is required! Eg: ww notch");
                return false;
            }
        }

        // Check if Player is online
        Player player = Bukkit.getPlayerExact(username);
        String uuid = null;
        String ipAddress = null;
        if (player != null && player.isOnline()) {
            uuid = player.getUniqueId().toString();
            ipAddress = Objects.requireNonNull(player.getAddress()).getHostString();
        }

        this.handleWhois(uuid, username, ipAddress, shouldBroadcast, senderPlayer);
        return true;
    }

    private void handleWhois(String uuid, String username, String ipAddress, Boolean shouldBroadcast, Player senderPlayer) {
        FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
            try {
                Boolean isFromJoinEvent = false;
                Boolean isRanByAdminPlayer = senderPlayer != null && senderPlayer.hasPermission("channyanhweb.admin");
                List<String> sayList = WhoisUtil.forPlayerSync(
                        username,
                        uuid,
                        ipAddress,
                        shouldBroadcast,
                        isFromJoinEvent,
                        isRanByAdminPlayer,
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
                        Tell(senderPlayer, line);
                    }
                }
            } catch (Exception e) {
                LoggingUtil.warntrace(e);
            }
        });
    }

    private void Tell(Player player, String message) {
        if (player == null) {
            ChannyAnhWEBBukkit.getPlugin().adventure().players().sendMessage(MineDown.parse(message));
        } else {
            ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(message));
        }
    }
}
