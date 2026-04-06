package com.channyanh.channyanhweb.velocity.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.channyanh.channyanhweb.velocity.ChannyAnhWEBVelocity;
import com.channyanh.channyanhweb.velocity.utils.SkinUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.property.SkinProperty;

public class ServerConnectedListener {
    @Subscribe
    public void sendUpdateSkinMessageOnServerConnected(ServerConnectedEvent event) {
        if (!ChannyAnhWEBVelocity.getPlugin().getHasSkinsRestorer()) {
            return;
        }

        Player player = event.getPlayer();
        RegisteredServer server = event.getServer();
        ChannyAnhWEBVelocity.getPlugin().getProxyServer().getScheduler().buildTask(ChannyAnhWEBVelocity.getPlugin(), () -> {
            SkinProperty skinProperty = SkinUtil.getSkinForPlayer(player.getUniqueId(), player.getUsername());
            if (skinProperty != null) {
                String skinPropertyJson = ChannyAnhWEBVelocity.getPlugin().getGson().toJson(skinProperty);
                String skinTextureId = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
                String playerUuid = player.getUniqueId().toString();

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("UpdatePlayerSkin");
                out.writeUTF(playerUuid);
                out.writeUTF(skinPropertyJson);
                out.writeUTF(skinTextureId);
                server.sendPluginMessage(ChannyAnhWEBVelocity.PLUGIN_MESSAGE_CHANNEL, out.toByteArray());
            }
        }).delay(1, java.util.concurrent.TimeUnit.SECONDS).schedule();  // Run after 1 seconds as plugin message channel is not ready yet.
    }
}
