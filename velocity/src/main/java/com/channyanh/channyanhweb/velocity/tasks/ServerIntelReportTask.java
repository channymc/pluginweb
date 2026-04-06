package com.channyanh.channyanhweb.velocity.tasks;

import com.sun.management.OperatingSystemMXBean;
import com.velocitypowered.api.proxy.ProxyServer;
import com.channyanh.channyanhweb.common.actions.ReportServerIntel;
import com.channyanh.channyanhweb.common.data.ServerIntelData;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.utils.SystemUtil;
import com.channyanh.channyanhweb.velocity.ChannyAnhWEBVelocity;

import java.lang.management.ManagementFactory;

public class ServerIntelReportTask implements Runnable {
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @Override
    public void run() {
        LoggingUtil.debug("--- SENDING SERVER INTEL ---");
        ServerIntelData serverIntelData = new ServerIntelData();
        ProxyServer proxyServer = ChannyAnhWEBVelocity.getPlugin().getProxyServer();

        serverIntelData.max_players = proxyServer.getConfiguration().getShowMaxPlayers();
        serverIntelData.online_players = proxyServer.getPlayerCount();
        serverIntelData.max_memory = Runtime.getRuntime().maxMemory() / 1024;
        serverIntelData.total_memory = Runtime.getRuntime().totalMemory() / 1024;
        serverIntelData.free_memory = Runtime.getRuntime().freeMemory() / 1024;

        serverIntelData.available_cpu_count = osBean.getAvailableProcessors();
        serverIntelData.cpu_load = SystemUtil.getAverageCpuLoad();
        serverIntelData.uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        serverIntelData.server_version = proxyServer.getVersion().getVersion();
        serverIntelData.server_session_id = ChannyAnhWEBVelocity.getPlugin().getServerSessionId();
        serverIntelData.free_disk_in_kb = SystemUtil.getFreeDiskSpaceInKiloBytes();
        serverIntelData.server_id = ChannyAnhWEBVelocity.getPlugin().getApiServerId();
        try {
            ReportServerIntel.reportSync(serverIntelData);
        } catch (Exception e) {
            LoggingUtil.warning("Failed to report server intel: " + e.getMessage());
        }
    }
}
