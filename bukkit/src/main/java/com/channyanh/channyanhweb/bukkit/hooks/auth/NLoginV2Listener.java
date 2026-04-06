package com.channyanh.channyanhweb.bukkit.hooks.auth;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

/**
 * Reflection-based listener for nLogin v2 API.
 * Registers for com.nickuc.login.api.event.bukkit.auth.AuthenticateEvent
 * without requiring a compile-time dependency on nLogin.
 */
public class NLoginV2Listener implements Listener {

    @SuppressWarnings("unchecked")
    public static void register() throws Exception {
        Class<? extends Event> authEventClass = (Class<? extends Event>)
                Class.forName("com.nickuc.login.api.event.bukkit.auth.AuthenticateEvent");

        // Find getPlayer() method on the event
        Method getPlayerMethod = authEventClass.getMethod("getPlayer");

        Bukkit.getPluginManager().registerEvent(
                authEventClass,
                new NLoginV2Listener(),
                EventPriority.MONITOR,
                (listener, event) -> {
                    try {
                        Player player = (Player) getPlayerMethod.invoke(event);
                        NLoginHook.onPlayerAuthenticated(player);
                    } catch (Exception e) {
                        ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[AuthHook] Error handling nLogin auth event: " + e.getMessage());
                    }
                },
                ChannyAnhWEBBukkit.getPlugin(),
                true // ignoreCancelled
        );
        ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] Registered nLogin v2 AuthenticateEvent via reflection.");
    }
}
