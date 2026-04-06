package com.channyanh.channyanhweb.bungee.tasks;

import com.sun.management.OperatingSystemMXBean;
import com.channyanh.channyanhweb.bungee.ChannyAnhWEBBungee;
import com.channyanh.channyanhweb.common.actions.ReportServerIntel;
import com.channyanh.channyanhweb.common.data.ServerIntelData;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.utils.SystemUtil;
import net.md_5.bungee.api.ProxyServer;

import java.lang.management.ManagementFactory;

public class ServerIntelReportTask implements Runnable {
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @Override
    public void run() {
        LoggingUtil.debug("--- SENDING SERVER INTEL ---");
        ServerIntelData serverIntelData = new ServerIntelData();
        ProxyServer proxyServer = ChannyAnhWEBBungee.getPlugin().getProxy();

        serverIntelData.max_players = proxyServer.getConfig().getPlayerLimit();
        serverIntelData.online_players = proxyServer.getOnlineCount();
        serverIntelData.max_memory = Runtime.getRuntime().maxMemory() / 1024;
        serverIntelData.total_memory = Runtime.getRuntime().totalMemory() / 1024;
        serverIntelData.free_memory = Runtime.getRuntime().freeMemory() / 1024;

        serverIntelData.available_cpu_count = osBean.getAvailableProcessors();
        serverIntelData.cpu_load = SystemUtil.getAverageCpuLoad();
        serverIntelData.uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        serverIntelData.server_version = ChannyAnhWEBBungee.getPlugin().getProxy().getVersion();
        serverIntelData.server_session_id = ChannyAnhWEBBungee.getPlugin().getServerSessionId();
        serverIntelData.free_disk_in_kb = SystemUtil.getFreeDiskSpaceInKiloBytes();
        serverIntelData.server_id = ChannyAnhWEBBungee.getPlugin().getApiServerId();
        try {
            ReportServerIntel.reportSync(serverIntelData);
        } catch (Exception e) {
            LoggingUtil.warning("Failed to report server intel: " + e.getMessage());
        }
    }
}
