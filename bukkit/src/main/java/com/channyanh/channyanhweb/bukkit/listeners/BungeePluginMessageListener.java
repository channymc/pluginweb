package com.channyanh.channyanhweb.bukkit.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class BungeePluginMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] bytes) {
        if (!channel.equals(ChannyAnhWEBCommon.PLUGIN_MESSAGE_CHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subchannel = in.readUTF();

        if (subchannel.equals("UpdatePlayerSkin")) {
            String playerUuid = in.readUTF();
            String skinPropertyJson = in.readUTF();
            String skinTextureId = in.readUTF();

            // Update the player's skin in the cache
            ChannyAnhWEBBukkit.getPlugin().getPlayerSkinCache().put(playerUuid, new String[]{skinPropertyJson, skinTextureId});
            // Update the player's skin in the session data
            updateInPlayerSessionData(playerUuid, skinPropertyJson, skinTextureId);
        }
    }

    private void updateInPlayerSessionData(String playerUuid, String skinPropertyJson, String skinTextureId) {
        // Update the player's skin in the session data
        PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(playerUuid);
        if (playerData == null) {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        if (playerSessionIntelData == null) {
            return;
        }

        playerSessionIntelData.skin_property = skinPropertyJson;
        playerSessionIntelData.skin_texture_id = skinTextureId;
    }
}
