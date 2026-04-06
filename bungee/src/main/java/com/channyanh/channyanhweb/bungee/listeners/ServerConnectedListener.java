package com.channyanh.channyanhweb.bungee.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.channyanh.channyanhweb.bungee.ChannyAnhWEBBungee;
import com.channyanh.channyanhweb.bungee.utils.SkinUtil;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.property.SkinProperty;

public class ServerConnectedListener implements Listener {
    @EventHandler
    public void sendUpdateSkinMessageOnServerConnected(ServerConnectedEvent event) {
        if (!ChannyAnhWEBBungee.getPlugin().getHasSkinsRestorer()) {
            return;
        }

        ProxiedPlayer player = event.getPlayer();
        ChannyAnhWEBBungee.getPlugin().getProxy().getScheduler().runAsync(ChannyAnhWEBBungee.getPlugin(), () -> {
            SkinProperty skinProperty = SkinUtil.getSkinForPlayer(player.getUniqueId(), player.getName());
            if (skinProperty != null) {
                String skinPropertyJson = ChannyAnhWEBBungee.getPlugin().getGson().toJson(skinProperty);
                String skinTextureId = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
                String playerUuid = player.getUniqueId().toString();

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("UpdatePlayerSkin");
                out.writeUTF(playerUuid);
                out.writeUTF(skinPropertyJson);
                out.writeUTF(skinTextureId);
                event.getServer().sendData(ChannyAnhWEBCommon.PLUGIN_MESSAGE_CHANNEL, out.toByteArray());
            }
        });
    }
}
