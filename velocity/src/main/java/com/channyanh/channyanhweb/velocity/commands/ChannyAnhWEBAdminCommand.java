package com.channyanh.channyanhweb.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.enums.BanWardenSyncType;
import com.channyanh.channyanhweb.velocity.ChannyAnhWEBVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChannyAnhWEBAdminCommand implements SimpleCommand {
    private final ChannyAnhWEBVelocity plugin;

    public ChannyAnhWEBAdminCommand(ChannyAnhWEBVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            usage(source);
            return;
        }

        String firstArg = args[0];
        if (firstArg.equalsIgnoreCase("help") || firstArg.equalsIgnoreCase("?")) {
            usage(source);
            return;
        }

        if (firstArg.equalsIgnoreCase("banwarden:sync")) {
            if (!(source instanceof ConsoleCommandSource)) {
                source.sendMessage(Component.text("This command can only be run from console.", NamedTextColor.RED));
                return;
            }

            if (!plugin.getIsBanWardenEnabled()) {
                source.sendMessage(Component.text("[BanWarden] BanWarden is not enabled, cannot sync bans.", NamedTextColor.RED));
                return;
            }

            String secondArg = args.length > 1 ? args[1].toLowerCase() : "all";
            source.sendMessage(Component.text("[BanWarden] Syncing " + secondArg + " punishments to web, plz check server logs for progress...", NamedTextColor.GREEN));
            banwardenSyncBans(secondArg);
            return;
        }
    }

    private void usage(CommandSource source) {
        source.sendMessage(Component.text("ChannyAnhWEB Admin Commands:", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/channyanhwebv banwarden:sync", NamedTextColor.GREEN));
        source.sendMessage(Component.text("   Sync bans from ban plugin to ChannyAnhWEB website.", NamedTextColor.GRAY));
        source.sendMessage(Component.text("/channyanhwebv help", NamedTextColor.GREEN));
        source.sendMessage(Component.text("   Shows help message.", NamedTextColor.GRAY));
    }

    private void banwardenSyncBans(String typeString) {
        BanWardenSyncType syncType = switch (typeString) {
            case "active" -> BanWardenSyncType.ACTIVE;
            case "inactive" -> BanWardenSyncType.INACTIVE;
            default -> BanWardenSyncType.ALL;
        };
        ChannyAnhWEBCommon.getInstance().getBanWarden().sync(syncType);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("channyanhweb.admin");
    }
}