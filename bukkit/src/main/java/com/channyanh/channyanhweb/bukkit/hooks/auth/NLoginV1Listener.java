package com.channyanh.channyanhweb.bukkit.hooks.auth;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

/**
 * Reflection-based listener for nLogin v1 API.
 * Registers for com.nickuc.login.api.event.bukkit.auth.BukkitLoginEvent
 */
public class NLoginV1Listener implements Listener {

    @SuppressWarnings("unchecked")
    public static void register() throws Exception {
        Class<? extends Event> authEventClass = (Class<? extends Event>)
                Class.forName("com.nickuc.login.api.event.bukkit.auth.BukkitLoginEvent");

        Method getPlayerMethod = authEventClass.getMethod("getPlayer");

        Bukkit.getPluginManager().registerEvent(
                authEventClass,
                new NLoginV1Listener(),
                EventPriority.MONITOR,
                (listener, event) -> {
                    try {
                        Player player = (Player) getPlayerMethod.invoke(event);
                        NLoginHook.onPlayerAuthenticated(player);
                    } catch (Exception e) {
                        ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[AuthHook] Error handling nLogin v1 auth event: " + e.getMessage());
                    }
                },
                ChannyAnhWEBBukkit.getPlugin(),
                true
        );
        ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] Registered nLogin v1 BukkitLoginEvent via reflection.");
    }
}
