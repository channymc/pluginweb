package com.channyanh.channyanhweb.common.banwarden.hooks;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.data.PunishmentData;
import com.channyanh.channyanhweb.common.enums.BanWardenPunishmentType;
import com.channyanh.channyanhweb.common.enums.BanWardenSyncType;
import com.channyanh.channyanhweb.common.interfaces.banwarden.BanWardenHook;

public class NullHook implements BanWardenHook {
    public static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();

    @Override
    public String punish(BanWardenPunishmentType type, String punishmentString) {
        warn();
        return null;
    }

    @Override
    public boolean pardon(BanWardenPunishmentType type, String victim, String reason, String admin) {
        warn();
        return false;
    }

    @Override
    public PunishmentData getPunishment(String punishmentId) {
        warn();
        return null;
    }

    @Override
    public void sync(BanWardenSyncType type) {
        warn();
    }

    private void warn() {
        common.getLogger().warning("[NullHook] No ban plugin found. Please contact plugin developer if you see this message.");
    }
}
