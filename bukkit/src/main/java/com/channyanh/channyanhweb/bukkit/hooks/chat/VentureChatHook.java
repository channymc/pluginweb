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
 * Reflection-based hook for VentureChat plugin.
 * No compile-time dependency on VentureChat JAR required.
 */
public class VentureChatHook implements Listener {

    @SuppressWarnings("unchecked")
    public static void register() throws Exception {
        Class<? extends Event> eventClass = (Class<? extends Event>)
                Class.forName("mineverse.Aust1n46.chat.api.events.VentureChatEvent");

        Method getChannelMethod = eventClass.getMethod("getChannel");
        Method getChatMethod = eventClass.getMethod("getChat");
        Method getFormatMethod = eventClass.getMethod("getFormat");
        Method getMineverseChatPlayerMethod = eventClass.getMethod("getMineverseChatPlayer");

        // ChatChannel methods
        Class<?> chatChannelClass = Class.forName("mineverse.Aust1n46.chat.channel.ChatChannel");
        Method isDefaultChannelMethod = chatChannelClass.getMethod("isDefaultchannel");

        // MineverseChatPlayer methods
        Class<?> chatPlayerClass = Class.forName("mineverse.Aust1n46.chat.api.MineverseChatPlayer");
        Method getPlayerMethod = chatPlayerClass.getMethod("getPlayer");

        Bukkit.getPluginManager().registerEvent(
                eventClass,
                new VentureChatHook(),
                EventPriority.HIGHEST,
                (listener, event) -> {
                    try {
                        Object chatChannel = getChannelMethod.invoke(event);
                        if (chatChannel == null) return;

                        boolean isDefault = (boolean) isDefaultChannelMethod.invoke(chatChannel);
                        if (!isDefault) return;

                        String chatMessage = (String) getChatMethod.invoke(event);
                        Object chatPlayer = getMineverseChatPlayerMethod.invoke(event);
                        if (chatPlayer == null) return;

                        Player player = (Player) getPlayerMethod.invoke(chatPlayer);
                        if (player == null) return;

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
                        ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[VentureChatHook] Error: " + e.getMessage());
                    }
                },
                ChannyAnhWEBBukkit.getPlugin(),
                true
        );
    }
}
