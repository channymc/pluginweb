package com.channyanh.channyanhweb.bukkit.hooks.auth;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.bukkit.listeners.PlayerJoinLeaveListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Hook for nLogin authentication plugin.
 * Listens for nLogin's authentication event and triggers deferred player processing.
 *
 * nLogin fires com.nickuc.login.api.event.bukkit.auth.AuthenticateEvent
 * when a player successfully authenticates (login or register).
 */
public class NLoginHook implements Listener {

    public static void register() {
        // Try nLogin v2 API first (reflection-based, no compile dependency)
        try {
            NLoginV2Listener.register();
            return;
        } catch (Exception e) {
            // v2 not available
        }

        // Try nLogin v1 API
        try {
            NLoginV1Listener.register();
            return;
        } catch (Exception e) {
            // v1 not available
        }

        // Fallback: generic command listener for /login, /register
        ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[AuthHook] nLogin API classes not found. Using generic command fallback.");
        Bukkit.getPluginManager().registerEvents(new NLoginGenericListener(), ChannyAnhWEBBukkit.getPlugin());
    }

    /**
     * Process a player after they authenticate via nLogin.
     */
    static void onPlayerAuthenticated(Player player) {
        if (player == null || !player.isOnline()) return;

        ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] Player " + player.getName() + " authenticated via nLogin. Processing deferred join...");
        AuthPluginHook.markAuthenticated(player.getUniqueId());
        PlayerJoinLeaveListener.processDeferredJoin(player);
    }
}
