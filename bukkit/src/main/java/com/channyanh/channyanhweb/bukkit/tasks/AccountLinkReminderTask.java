package com.channyanh.channyanhweb.bukkit.tasks;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.entity.Player;

import java.util.List;

public class AccountLinkReminderTask implements Runnable {
    @Override
    public void run() {
        Boolean isAlreadyLinkedReminderEnabled = ChannyAnhWEBBukkit.getPlugin().getIsRemindPlayerWhenAlreadyLinkedEnabled();

        for (Player player : ChannyAnhWEBBukkit.getPlugin().getServer().getOnlinePlayers()) {
            PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());

            if (playerData == null) {
                continue;
            }
            if (playerData.is_verified && !isAlreadyLinkedReminderEnabled) {
                continue;
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
}
