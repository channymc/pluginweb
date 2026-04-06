package com.channyanh.channyanhweb.bukkit.hooks.auth;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.bukkit.listeners.PlayerJoinLeaveListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

/**
 * Reflection-based hook for AuthMe authentication plugin.
 * Registers for fr.xephi.authme.events.LoginEvent without compile dependency.
 */
public class AuthMeHook implements Listener {

    @SuppressWarnings("unchecked")
    public static void register() throws Exception {
        Class<? extends Event> loginEventClass = (Class<? extends Event>)
                Class.forName("fr.xephi.authme.events.LoginEvent");

        Method getPlayerMethod = loginEventClass.getMethod("getPlayer");

        Bukkit.getPluginManager().registerEvent(
                loginEventClass,
                new AuthMeHook(),
                EventPriority.MONITOR,
                (listener, event) -> {
                    try {
                        Player player = (Player) getPlayerMethod.invoke(event);
                        onPlayerAuthenticated(player);
                    } catch (Exception e) {
                        ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[AuthHook] Error handling AuthMe login event: " + e.getMessage());
                    }
                },
                ChannyAnhWEBBukkit.getPlugin(),
                true
        );
        ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] Registered AuthMe LoginEvent via reflection.");
    }

    static void onPlayerAuthenticated(Player player) {
        if (player == null || !player.isOnline()) return;

        ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] Player " + player.getName() + " authenticated via AuthMe. Processing deferred join...");
        AuthPluginHook.markAuthenticated(player.getUniqueId());
        PlayerJoinLeaveListener.processDeferredJoin(player);
    }
}
