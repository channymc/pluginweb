package com.channyanh.channyanhweb.bukkit.hooks.skinsrestorer;

import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.data.PlayerData;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class SkinsRestorerHook implements Consumer<SkinApplyEvent> {
    @Override
    public void accept(SkinApplyEvent event) {
        LoggingUtil.info("SkinsRestorerHook.onSkinApplyEvent");
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer(Player.class);
        SkinProperty skinProperty = event.getProperty();

        PlayerData playerData = ChannyAnhWEBBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            LoggingUtil.warning("PlayerData not found while listening to SkinApplyEvent");
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = ChannyAnhWEBBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        if (skinProperty.getValue().isEmpty()) {
            playerSessionIntelData.skin_property = null;
            playerSessionIntelData.skin_texture_id = null;
        } else {
            playerSessionIntelData.skin_property = ChannyAnhWEBBukkit.getPlugin().getGson().toJson(skinProperty);
            playerSessionIntelData.skin_texture_id = PropertyUtils.getSkinTextureUrlStripped(skinProperty);
        }
    }
}
