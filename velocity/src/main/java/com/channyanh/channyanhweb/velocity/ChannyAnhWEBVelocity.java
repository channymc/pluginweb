package com.channyanh.channyanhweb.velocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.enums.BanWardenPluginType;
import com.channyanh.channyanhweb.common.enums.PlatformType;
import com.channyanh.channyanhweb.common.interfaces.ChannyAnhWEBPlugin;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.webquery.WebQueryServer;
import com.channyanh.channyanhweb.velocity.commander.VelocityCommander;
import com.channyanh.channyanhweb.velocity.commands.ChannyAnhWEBAdminCommand;
import com.channyanh.channyanhweb.velocity.hooks.skinsrestorer.SkinsRestorerHook;
import com.channyanh.channyanhweb.velocity.listeners.ServerConnectedListener;
import com.channyanh.channyanhweb.velocity.logging.VelocityLogger;
import com.channyanh.channyanhweb.velocity.schedulers.VelocityScheduler;
import com.channyanh.channyanhweb.velocity.tasks.ServerIntelReportTask;
import com.channyanh.channyanhweb.velocity.utils.PluginUtil;
import com.channyanh.channyanhweb.velocity.webquery.VelocityWebQuery;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
@Plugin(
        id = "channyanhweb",
        name = "ChannyAnhWEB",
        authors = {"ChannyAnh"},
        version = BuildConstants.VERSION,
        dependencies = {
                @Dependency(id = "skinsrestorer", optional = true),
                @Dependency(id = "litebans", optional = true)
        }
)
public class ChannyAnhWEBVelocity implements ChannyAnhWEBPlugin {
    @Inject
    private Logger logger;
    @Inject
    @DataDirectory
    private Path dataPath;
    @Inject
    private ProxyServer proxyServer;
    @Inject
    private Metrics.Factory metricsFactory;
    @Inject
    private PluginContainer pluginContainer;
    @Getter
    private static ChannyAnhWEBVelocity plugin;

    private WebQueryServer webQueryServer;
    private Gson gson = null;
    private ChannyAnhWEBCommon common;
    private YamlDocument config;

    private Boolean isEnabled;
    private Boolean isDebugMode;
    private String apiKey;
    private String apiSecret;
    private String apiServerId;
    private String apiHost;
    private Boolean isConsoleLogEnabled;
    private String webQueryHost;
    private int webQueryPort;
    private List<String> webQueryWhitelistedIps;
    private Boolean isServerIntelEnabled;
    public String serverSessionId;
    public Boolean isAllowOnlyWhitelistedCommandsFromWeb;
    public List<String> whitelistedCommandsFromWeb;
    public ConcurrentHashMap<String, String> joinAddressCache = new ConcurrentHashMap<>();
    public Boolean hasSkinsRestorer = false;
    public Boolean isSkinsRestorerHookEnabled;
    public Boolean isBanWardenEnabled = false;
    public static final MinecraftChannelIdentifier PLUGIN_MESSAGE_CHANNEL = MinecraftChannelIdentifier.from(ChannyAnhWEBCommon.PLUGIN_MESSAGE_CHANNEL);

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        plugin = this;

        // Load config
        loadConfig();
        initVariables();
        if (!isEnabled) {
            logger.warn("Plugin disabled from config.yml");
            return;
        }
        // Disable plugin if host, key, secret or server-id is not there
        if (apiHost == null || apiKey == null || apiSecret == null || apiServerId == null || apiHost.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty() || apiServerId.isEmpty()) {
            logger.error("Plugin disabled due to no API information");
            return;
        }

        // GSON builder
        gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

        // Setup Common
        common = new ChannyAnhWEBCommon();
        common.setPlugin(this);
        common.setPlatformType(PlatformType.VELOCITY);
        common.setGson(gson);
        common.setLogger(new VelocityLogger(this));
        common.setCommander(new VelocityCommander(this));
        common.setScheduler(new VelocityScheduler(this));
        common.setWebQuery(new VelocityWebQuery(this));
        initBanWarden(common);

        // init Bstats
        initBstats();

        // Start web query server
        startWebQueryServer();

        // Hook into plugins
        if (PluginUtil.checkIfPluginEnabled("skinsrestorer")) {
            hasSkinsRestorer = setupSkinsRestorer();
        }

        // Register Channels
        proxyServer.getChannelRegistrar().register(PLUGIN_MESSAGE_CHANNEL);

        // Register Listeners
        proxyServer.getEventManager().register(this, new ServerConnectedListener());

        // Register Commands
        proxyServer.getCommandManager().register("channyanhwebv", new ChannyAnhWEBAdminCommand(this), "cawv", "mtxv", "mtxp");

        // Register Tasks
        if (isServerIntelEnabled) {
            proxyServer.getScheduler().buildTask(plugin, new ServerIntelReportTask()).delay(60L, TimeUnit.SECONDS).repeat(60L, TimeUnit.SECONDS).schedule();
        }
    }

    private void startWebQueryServer() {
        webQueryServer = new WebQueryServer(webQueryHost, webQueryPort, webQueryWhitelistedIps);
        webQueryServer.start();
    }

    private void loadConfig() {
        // Create and update the file
        try {
            config = YamlDocument.create(new File(getDataPath().toFile(), "config.yml"), Objects.requireNonNull(getClass().getResourceAsStream("/velocityConfig.yml")), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());
        } catch (IOException ex) {
            LoggingUtil.warntrace(ex);
        }
    }

    private void initVariables() {
        isEnabled = config.getBoolean("enabled");
        isDebugMode = config.getBoolean("debug-mode");
        apiKey = config.getString("api-key");
        isEnabled = config.getBoolean("enabled", true);
        isDebugMode = config.getBoolean("debug-mode", false);
        apiKey = config.getString("api-key", null);
        apiSecret = config.getString("api-secret", null);
        apiServerId = config.getString("server-id", null);
        apiHost = config.getString("api-host", null);
        webQueryHost = config.getString("webquery-host", null);
        webQueryPort = config.getInt("webquery-port", 25575);
        webQueryWhitelistedIps = config.getStringList("webquery-whitelisted-ips");
        isServerIntelEnabled = config.getBoolean("report-server-intel", false);
        isConsoleLogEnabled = config.getBoolean("enable-consolelog", false);
        isAllowOnlyWhitelistedCommandsFromWeb = config.getBoolean("allow-only-whitelisted-commands-from-web", false);
        whitelistedCommandsFromWeb = config.getStringList("whitelisted-commands-from-web");
        isSkinsRestorerHookEnabled = config.getBoolean("enable-skinsrestorer-hook", true);
        isBanWardenEnabled = config.getBoolean("enable-banwarden", true);
        serverSessionId = UUID.randomUUID().toString();
    }

    private void initBstats() {
        int pluginId = 26168;
        Metrics metrics = metricsFactory.make(this, pluginId);
    }

    private Boolean setupSkinsRestorer() {
        if (!isSkinsRestorerHookEnabled) {
            logger.info("SkinsRestorer is found! But SkinsRestorer hook is disabled in config.");
            return false;
        }

        logger.info("Hooking into SkinsRestorer...");

        // Add SkinsRestorerHook
        try {
            common.setSkinsRestorerApi(SkinsRestorerProvider.get());
            common.getSkinsRestorerApi().getEventBus().subscribe(pluginContainer, SkinApplyEvent.class, new SkinsRestorerHook());

            // Warn if SkinsRestorer is not compatible with v15
            if (!VersionProvider.isCompatibleWith("15")) {
                logger.warn("ChannyAnhWEB supports SkinsRestorer v15, but " + VersionProvider.getVersionInfo() + " is installed. There may be errors!");
            }
            logger.info("Hooked into SkinsRestorer!");
            return true;
        } catch (Exception e) {
            logger.warn("ChannyAnhWEB failed to hook into SkinsRestorer!");
            logger.warn("Error: " + e.getMessage());
            return false;
        }
    }


    private void initBanWarden(ChannyAnhWEBCommon common) {
        if (!isBanWardenEnabled) {
            logger.warn("[BanWarden] BanWarden is disabled in config.yml");
            return;
        }

        // set which ban plugin is enabled.
        if (PluginUtil.checkIfPluginEnabled("litebans")) {
            common.initBanWarden(BanWardenPluginType.LITEBANS);
        } else if (PluginUtil.checkIfPluginEnabled("libertybans")) {
            common.initBanWarden(BanWardenPluginType.LIBERTYBANS);
        } else if (PluginUtil.checkIfPluginEnabled("advancedban")) {
            common.initBanWarden(BanWardenPluginType.ADVANCEDBAN);
        } else {
            isBanWardenEnabled = false;
            logger.warn("[BanWarden] No supported BanWarden plugin found.");
            return;
        }
    }
}
