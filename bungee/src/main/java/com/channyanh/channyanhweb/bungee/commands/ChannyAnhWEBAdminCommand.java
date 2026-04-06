package com.channyanh.channyanhweb.bungee.commands;

import com.channyanh.channyanhweb.bungee.ChannyAnhWEBBungee;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.enums.BanWardenSyncType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ChannyAnhWEBAdminCommand extends Command {
    private final ChannyAnhWEBBungee plugin;

    public ChannyAnhWEBAdminCommand(ChannyAnhWEBBungee plugin) {
        super("channyanhwebb", "channyanhweb.admin", "cawb", "mtxb", "mtxp");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender);
            return;
        }

        String firstArg = args[0];
        if (firstArg.equalsIgnoreCase("help") || firstArg.equalsIgnoreCase("?")) {
            usage(sender);
            return;
        }

        if (firstArg.equalsIgnoreCase("banwarden:sync")) {
            if (sender instanceof ProxiedPlayer) {
                sender.sendMessage(new ComponentBuilder("This command can only be run from console.")
                        .color(ChatColor.RED)
                        .create());
                return;
            }

            if (!plugin.getIsBanWardenEnabled()) {
                sender.sendMessage(new ComponentBuilder("BanWarden is not enabled, cannot sync bans.")
                        .color(ChatColor.RED)
                        .create());
                return;
            }

            String secondArg = args.length > 1 ? args[1].toLowerCase() : "all";
            sender.sendMessage(new ComponentBuilder("[BanWarden] Syncing " + secondArg + " punishments to web, plz check server logs for progress...")
                    .color(ChatColor.GREEN)
                    .create());
            banwardenSyncBans(secondArg);
            return;
        }
    }

    private void usage(CommandSender sender) {
        // Header
        sender.sendMessage(new ComponentBuilder("ChannyAnhWEB Admin Commands (Bungee):")
                .color(ChatColor.AQUA)
                .create());

        // BanWarden Sync Command
        sender.sendMessage(new ComponentBuilder("/channyanhwebb banwarden:sync")
                .color(ChatColor.GREEN)
                .create());
        sender.sendMessage(new ComponentBuilder("   Sync bans from ban plugin to ChannyAnhWEB website.")
                .color(ChatColor.GRAY)
                .create());

        // Help Command
        sender.sendMessage(new ComponentBuilder("/channyanhwebb help")
                .color(ChatColor.GREEN)
                .create());
        sender.sendMessage(new ComponentBuilder("   Shows help message.")
                .color(ChatColor.GRAY)
                .create());
    }

    private void banwardenSyncBans(String typeString) {
        BanWardenSyncType syncType = switch (typeString) {
            case "active" -> BanWardenSyncType.ACTIVE;
            case "inactive" -> BanWardenSyncType.INACTIVE;
            default -> BanWardenSyncType.ALL;
        };
        ChannyAnhWEBCommon.getInstance().getBanWarden().sync(syncType);
    }
}