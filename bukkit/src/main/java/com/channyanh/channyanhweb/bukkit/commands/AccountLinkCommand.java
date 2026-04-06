package com.channyanh.channyanhweb.bukkit.commands;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.bukkit.hooks.auth.AuthPluginHook;
import com.channyanh.channyanhweb.common.actions.LinkAccount;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.responses.GenericApiResponse;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;
import com.channyanh.channyanhweb.bukkit.utils.FoliaUtil;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccountLinkCommand implements CommandExecutor {
    private final boolean isConfirmationEnabled = ChannyAnhWEBBukkit.getPlugin().getIsPlayerLinkConfirmationEnabled();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            ChannyAnhWEBBukkit.getPlugin().getLogger().info("Error: Only players can execute that command.");
            return false;
        }

        // Block /link if auth plugin is active and player hasn't logged in yet
        if (AuthPluginHook.isAuthPluginActive() && !AuthPluginHook.isAuthenticated(player.getUniqueId())) {
            ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(
                    MineDown.parse("&cPlease login first before linking your account!")
            );
            return true;
        }

        PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());

        // Already linked
        if (playerData != null && playerData.is_verified) {
            for (String line : ChannyAnhWEBBukkit.getPlugin().getPlayerLinkInitAlreadyLinkedMessage()) {
                line = line.replace("{WEB_URL}", ChannyAnhWEBBukkit.getPlugin().getApiHost());
                line = line.replace("{LINK_URL}", ChannyAnhWEBHttpUtil.getUrl(ChannyAnhWEBHttpUtil.ACCOUNT_LINK_ROUTE));
                ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
            }
            return true;
        }

        // Send Init message if only /link
        if (strings.length == 0) {
            List<String> withoutParamsMessage = ChannyAnhWEBBukkit.getPlugin().getPlayerLinkInitMessage();
            for (String line : withoutParamsMessage) {
                line = line.replace("{LINK_URL}", ChannyAnhWEBHttpUtil.getUrl(ChannyAnhWEBHttpUtil.ACCOUNT_LINK_ROUTE));
                line = line.replace("{WEB_URL}", ChannyAnhWEBBukkit.getPlugin().getApiHost());
                ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
            }
            return true;
        }

        // Send Linking message if /link <otp>
        String otpCode = strings[0];
        if (isConfirmationEnabled) {
            ConcurrentHashMap<String, String> pendingVerifications = ChannyAnhWEBBukkit.getPlugin().getPlayerLinkPendingVerificationMap();
            if (otpCode.equalsIgnoreCase("confirm")) {
                String pendingOtp = pendingVerifications.get(player.getUniqueId().toString());
                if (pendingOtp == null) {
                    ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(
                            MineDown.parse("&cNo OTP pending confirmation. Please enter your OTP first.")
                    );
                    return true;
                }
                String processingMessage = ChannyAnhWEBBukkit.getPlugin().getProcessingMessage();
                ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(processingMessage));
                linkAccount(player, pendingOtp);
                pendingVerifications.remove(player.getUniqueId().toString());
            } else if (otpCode.equalsIgnoreCase("deny") || otpCode.equalsIgnoreCase("cancel")) {
                pendingVerifications.remove(player.getUniqueId().toString());
                String cancelledMessage = ChannyAnhWEBBukkit.getPlugin().getCancelledMessage();
                ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(cancelledMessage));
            } else {
                pendingVerifications.put(player.getUniqueId().toString(), otpCode);

                String playerLinkConfirmationTitle = ChannyAnhWEBBukkit.getPlugin().getPlayerLinkConfirmationTitle();
                String playerLinkConfirmationSubtitle = ChannyAnhWEBBukkit.getPlugin().getPlayerLinkConfirmationSubtitle();
                if (!playerLinkConfirmationSubtitle.isBlank() || !playerLinkConfirmationTitle.isBlank()) {
                    final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(6000), Duration.ofMillis(1000));
                    final Title title = Title.title(MineDown.parse(playerLinkConfirmationTitle), MineDown.parse(playerLinkConfirmationSubtitle), times);
                    ChannyAnhWEBBukkit.getPlugin().adventure().player(player).showTitle(title);
                }

                for (String line : ChannyAnhWEBBukkit.getPlugin().getPlayerLinkConfirmationMessage()) {
                    line = line.replace("{LINK_URL}", ChannyAnhWEBHttpUtil.getUrl(ChannyAnhWEBHttpUtil.ACCOUNT_LINK_ROUTE));
                    line = line.replace("{WEB_URL}", ChannyAnhWEBBukkit.getPlugin().getApiHost());
                    ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                }
            }
        } else {
            linkAccount(player, otpCode);
        }

        return true;
    }

    private void linkAccount(Player player, String otpCode) {
        FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
            try {
                GenericApiResponse response = LinkAccount.link(
                        player.getUniqueId().toString(),
                        otpCode,
                        ChannyAnhWEBBukkit.getPlugin().getApiServerId()
                );

                if (response.getCode() != 200) {
                    for (String line : ChannyAnhWEBBukkit.getPlugin().getPlayerLinkErrorMessage()) {
                        line = line.replace("{ERROR_MESSAGE}", response.getMessage());
                        ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                    }
                } else {
                    for (String line : ChannyAnhWEBBukkit.getPlugin().getPlayerLinkSuccessMessage()) {
                        line = line.replace("{LINK_URL}", ChannyAnhWEBHttpUtil.getUrl(ChannyAnhWEBHttpUtil.ACCOUNT_LINK_ROUTE));
                        line = line.replace("{WEB_URL}", ChannyAnhWEBBukkit.getPlugin().getApiHost());
                        ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                    }
                }
            } catch (Exception e) {
                for (String line : ChannyAnhWEBBukkit.getPlugin().getPlayerLinkErrorMessage()) {
                    line = line.replace("{WEB_URL}", ChannyAnhWEBBukkit.getPlugin().getApiHost());
                    line = line.replace("{ERROR_MESSAGE}", "Unknown error! Please contact admin.");
                    ChannyAnhWEBBukkit.getPlugin().adventure().player(player).sendMessage(MineDown.parse(line));
                }
            }
        });
    }
}
