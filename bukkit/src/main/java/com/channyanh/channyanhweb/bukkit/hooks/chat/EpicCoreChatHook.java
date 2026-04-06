package com.channyanh.channyanhweb.bukkit.hooks.chat;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.actions.ReportServerChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

/**
 * Reflection-based hook for EpicCore chat plugin.
 * No compile-time dependency on EpicCore JAR required.
 */
public class EpicCoreChatHook implements Listener {

    @SuppressWarnings("unchecked")
    public static void register() throws Exception {
        Class<? extends Event> eventClass = (Class<? extends Event>)
                Class.forName("io.signality.Modules.Chat.EpicCoreChatEvent");

        Method getChannelMethod = eventClass.getMethod("getChannel");
        Method getPlayerMethod = eventClass.getMethod("getPlayer");
        Method getMessageMethod = eventClass.getMethod("getMessage");
        Method getFormatMethod = eventClass.getMethod("getFormat");

        Bukkit.getPluginManager().registerEvent(
                eventClass,
                new EpicCoreChatHook(),
                EventPriority.HIGHEST,
                (listener, event) -> {
                    try {
                        String channel = (String) getChannelMethod.invoke(event);
                        if (!"global".equals(channel)) return;

                        Player player = (Player) getPlayerMethod.invoke(event);
                        if (player == null) return;

                        String chatMessage = (String) getMessageMethod.invoke(event);
                        String format = (String) getFormatMethod.invoke(event);
                        String message = String.format(format, player.getDisplayName(), chatMessage) + ' ' + chatMessage;
                        message = ChatColor.translateAlternateColorCodes('&', message);

                        ReportServerChat.reportAsync(
                                "player-chat",
                                message,
                                player.getName(),
                                player.getUniqueId().toString()
                        );
                    } catch (Exception e) {
                        ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[EpicCoreChatHook] Error: " + e.getMessage());
                    }
                },
                ChannyAnhWEBBukkit.getPlugin(),
                true
        );
    }
}
