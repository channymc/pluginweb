package com.channyanh.channyanhweb.common.banwarden.hooks;

import com.channyanh.channyanhweb.common.data.PunishmentData;
import com.channyanh.channyanhweb.common.enums.BanWardenPunishmentType;
import com.channyanh.channyanhweb.common.enums.BanWardenSyncType;
import com.channyanh.channyanhweb.common.interfaces.banwarden.BanWardenHook;

public class AdvancedbanHook implements BanWardenHook {

    @Override
    public String punish(BanWardenPunishmentType type, String punishmentString) {
        return "";
    }

    @Override
    public boolean pardon(BanWardenPunishmentType type, String victim, String reason, String admin) {
        return false;
    }

    @Override
    public PunishmentData getPunishment(String punishmentId) {
        return null;
    }

    @Override
    public void sync(BanWardenSyncType type) {

    }
}
