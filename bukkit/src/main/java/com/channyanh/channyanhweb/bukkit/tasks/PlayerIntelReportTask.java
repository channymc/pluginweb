package com.channyanh.channyanhweb.bukkit.tasks;

import com.google.gson.Gson;
import com.channyanh.channyanhweb.bukkit.ChannyAnhWEBBukkit;
import com.channyanh.channyanhweb.common.data.PlayerSessionIntelData;
import com.channyanh.channyanhweb.bukkit.utils.PlayerIntelUtil;

import java.util.Map;

public class PlayerIntelReportTask implements Runnable {
    public final Gson gson;

    public PlayerIntelReportTask() {
        this.gson = ChannyAnhWEBBukkit.getPlugin().getGson();
    }

    @Override
    public void run() {
        // Get list of all session
        Map<String, PlayerSessionIntelData> playerSessionIntelDataMap = ChannyAnhWEBBukkit.getPlugin().getPlayerSessionIntelDataMap();

        // Loop thru each
        for (PlayerSessionIntelData playerSessionData : playerSessionIntelDataMap.values()) {
            // Check if session player is online
            PlayerIntelUtil.reportPlayerIntel(playerSessionData, false);
        }
    }
}
