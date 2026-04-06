package com.channyanh.channyanhweb.bukkit.hooks.auth;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.bukkit.utils.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.reflect.Method;

/**
 * Generic fallback listener when nLogin API event classes aren't found.
 * Uses two strategies to detect authentication:
 * 1. Tries reflection on nLogin's internal API to check auth status after /login or /register
 * 2. Falls back to movement-based detection (nLogin blocks movement until auth)
 */
public class NLoginGenericListener implements Listener {

    private static Method isAuthenticatedMethod = null;
    private static Object nLoginApiInstance = null;
    private static boolean reflectionFailed = false;

    static {
        // Try to find nLogin's auth check method via reflection
        try {
            Class<?> apiClass = Class.forName("com.nickuc.login.api.nLoginAPI");
            Method getInstanceMethod = apiClass.getMethod("getApi");
            nLoginApiInstance = getInstanceMethod.invoke(null);
            isAuthenticatedMethod = nLoginApiInstance.getClass().getMethod("isAuthenticated", org.bukkit.entity.Player.class);
        } catch (Exception e) {
            reflectionFailed = true;
        }
    }

    /**
     * Check if a player is authenticated via nLogin reflection.
     * Returns null if reflection is not available.
     */
    private static Boolean isNLoginAuthenticated(Player player) {
        if (reflectionFailed || isAuthenticatedMethod == null || nLoginApiInstance == null) {
            return null;
        }
        try {
            return (Boolean) isAuthenticatedMethod.invoke(nLoginApiInstance, player);
        } catch (Exception e) {
            return null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().trim();
        if (msg.startsWith("/login ") || msg.startsWith("/register ") ||
                msg.startsWith("/l ") || msg.startsWith("/reg ")) {
            Player player = event.getPlayer();
            // Poll auth status with retries after the command
            pollAuthStatus(player, 0);
        }
    }

    /**
     * If reflection-based auth check is not available, fall back to movement detection.
     * nLogin blocks player movement until authenticated. When a player successfully
     * moves (not just head rotation), they must be authenticated.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only active when reflection failed (otherwise polling handles it)
        if (!reflectionFailed) return;

        Player player = event.getPlayer();

        // Skip if already authenticated (fast path for most players)
        if (AuthPluginHook.isAuthenticated(player.getUniqueId())) return;

        // Only consider actual position changes (not just head rotation)
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Player can physically move = they are authenticated by nLogin
        if (player.isOnline()) {
            NLoginHook.onPlayerAuthenticated(player);
        }
    }

    /**
     * Poll nLogin auth status with retries (reflection-based).
     * Tries up to 5 times with 20-tick (1 second) intervals.
     */
    private void pollAuthStatus(Player player, int attempt) {
        if (attempt >= 5) return;

        FoliaUtil.runAsync(ChannyAnhWEBBukkit.getPlugin(), () -> {
            if (!player.isOnline() || AuthPluginHook.isAuthenticated(player.getUniqueId())) {
                return;
            }

            Boolean authResult = isNLoginAuthenticated(player);
            if (authResult != null && authResult) {
                NLoginHook.onPlayerAuthenticated(player);
            } else if (authResult != null) {
                // Not yet authenticated, retry
                pollAuthStatus(player, attempt + 1);
            }
            // If authResult is null (reflection failed), movement listener handles it
        }, 20L * (attempt + 1)); // 1s, 2s, 3s, 4s, 5s
    }
}
