package com.channyanh.channyanhweb.bungee.hooks.skinsrestorer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.channyanh.channyanhweb.bungee.ChannyAnhWEBBungee;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.property.SkinProperty;

import java.util.function.Consumer;

public class SkinsRestorerHook implements Consumer<SkinApplyEvent> {
    @Override
    public void accept(SkinApplyEvent event) {
        LoggingUtil.debug("SkinsRestorerHook.onSkinApplyEvent");
        if (event.isCancelled()) {
            return;
        }
        ProxiedPlayer player = event.getPlayer(ProxiedPlayer.class);
        SkinProperty skinProperty = event.getProperty();

        if (player != null && !skinProperty.getValue().isEmpty()) {
            String skinPropertyJson = ChannyAnhWEBBungee.getPlugin().getGson().toJson(skinProperty);
            String skinTextureId = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
            String playerUuid = player.getUniqueId().toString();

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UpdatePlayerSkin");
            out.writeUTF(playerUuid);
            out.writeUTF(skinPropertyJson);
            out.writeUTF(skinTextureId);

            ChannyAnhWEBBungee.getPlugin().getProxy().getScheduler().runAsync(ChannyAnhWEBBungee.getPlugin(), () -> {
                player.getServer().sendData(ChannyAnhWEBCommon.PLUGIN_MESSAGE_CHANNEL, out.toByteArray());
            });
        }
    }
}
