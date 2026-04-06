package com.channyanh.channyanhweb.common;

import com.google.gson.Gson;
import com.channyanh.channyanhweb.common.banwarden.BanWarden;
import com.channyanh.channyanhweb.common.banwarden.hooks.AdvancedbanHook;
import com.channyanh.channyanhweb.common.banwarden.hooks.LibertybansHook;
import com.channyanh.channyanhweb.common.banwarden.hooks.LitebansHook;
import com.channyanh.channyanhweb.common.banwarden.hooks.NullHook;
import com.channyanh.channyanhweb.common.enums.BanWardenPluginType;
import com.channyanh.channyanhweb.common.enums.PlatformType;
import com.channyanh.channyanhweb.common.interfaces.ChannyAnhWEBPlugin;
import com.channyanh.channyanhweb.common.interfaces.banwarden.BanWardenHook;
import com.channyanh.channyanhweb.common.interfaces.commander.CommonCommander;
import com.channyanh.channyanhweb.common.interfaces.logging.CommonLogger;
import com.channyanh.channyanhweb.common.interfaces.schedulers.CommonScheduler;
import com.channyanh.channyanhweb.common.interfaces.webquery.CommonWebQuery;
import lombok.Getter;
import lombok.Setter;
import net.skinsrestorer.api.SkinsRestorer;
import okhttp3.OkHttpClient;

@Setter
@Getter
public class ChannyAnhWEBCommon {
    @Getter
    private static ChannyAnhWEBCommon instance;
    private CommonLogger logger;
    private CommonScheduler scheduler;
    private CommonWebQuery webQuery;
    private CommonCommander commander;
    private PlatformType platformType;
    private Gson gson;
    private ChannyAnhWEBPlugin plugin;
    private SkinsRestorer skinsRestorerApi;
    private BanWardenPluginType banWardenPluginType;
    private BanWarden banWarden;
    public static String PLUGIN_MESSAGE_CHANNEL = "channyanhweb:main";
    public OkHttpClient httpClient = new OkHttpClient();

    public ChannyAnhWEBCommon() {
        instance = this;
    }

    public void initBanWarden(BanWardenPluginType banWardenPluginType) {
        this.banWardenPluginType = banWardenPluginType;

        BanWardenHook hook = switch (banWardenPluginType) {
            case LITEBANS -> new LitebansHook();
            case LIBERTYBANS -> new LibertybansHook();
            case ADVANCEDBAN -> new AdvancedbanHook();
            default -> new NullHook();
        };

        banWarden = new BanWarden(hook);
        logger.info("[BanWarden] Hooked into " + banWardenPluginType.name());
    }
}
