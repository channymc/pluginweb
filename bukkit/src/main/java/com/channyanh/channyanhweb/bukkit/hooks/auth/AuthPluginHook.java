package com.channyanh.channyanhweb.bukkit.hooks.auth;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.bukkit.utils.PluginUtil;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central auth plugin hook manager for nLogin, AuthMe, etc.
 * When an auth plugin is detected, PlayerJoinLeaveListener defers
 * session/data processing until the player actually authenticates.
 */
public class AuthPluginHook {

    private static boolean hasAuthPlugin = false;
    private static String authPluginName = "none";

    // Set of UUIDs that have been authenticated this session
    private static final Set<UUID> authenticatedPlayers = ConcurrentHashMap.newKeySet();

    /**
     * Detect and register hooks for supported auth plugins.
     * Called from onEnable().
     */
    public static void init() {
        if (PluginUtil.checkIfPluginEnabled("nLogin")) {
            hasAuthPlugin = true;
            authPluginName = "nLogin";
            ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] nLogin detected! Will defer player processing until authentication.");
            try {
                NLoginHook.register();
            } catch (Exception e) {
                ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[AuthHook] Failed to hook into nLogin API: " + e.getMessage());
                ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] Falling back to delayed join processing.");
                hasAuthPlugin = false;
            }
        } else if (PluginUtil.checkIfPluginEnabled("AuthMe")) {
            hasAuthPlugin = true;
            authPluginName = "AuthMe";
            ChannyAnhWEBBukkit.getPlugin().getLogger().info("[AuthHook] AuthMe detected! Will defer player processing until authentication.");
            try {
                AuthMeHook.register();
            } catch (Exception e) {
                ChannyAnhWEBBukkit.getPlugin().getLogger().warning("[AuthHook] Failed to hook into AuthMe: " + e.getMessage());
                hasAuthPlugin = false;
            }
        }
    }

    /**
     * Whether an auth plugin is active and we need to gate player processing.
     */
    public static boolean isAuthPluginActive() {
        return hasAuthPlugin;
    }

    /**
     * Get the name of the detected auth plugin.
     */
    public static String getAuthPluginName() {
        return authPluginName;
    }

    /**
     * Mark a player as authenticated (called by auth plugin hooks).
     */
    public static void markAuthenticated(UUID uuid) {
        authenticatedPlayers.add(uuid);
    }

    /**
     * Check if a player has been authenticated.
     */
    public static boolean isAuthenticated(UUID uuid) {
        if (!hasAuthPlugin) return true; // No auth plugin = always authenticated
        return authenticatedPlayers.contains(uuid);
    }

    /**
     * Remove authentication status (called on quit).
     */
    public static void removePlayer(UUID uuid) {
        authenticatedPlayers.remove(uuid);
    }
}
