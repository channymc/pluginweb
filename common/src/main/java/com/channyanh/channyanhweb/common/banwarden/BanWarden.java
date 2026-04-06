package com.channyanh.channyanhweb.common.banwarden;

import com.channyanh.channyanhweb.common.enums.BanWardenPunishmentType;
import com.channyanh.channyanhweb.common.enums.BanWardenSyncType;
import com.channyanh.channyanhweb.common.interfaces.banwarden.BanWardenHook;

public class BanWarden {
    private final BanWardenHook hook;

    public BanWarden(BanWardenHook hook) {
        this.hook = hook;
    }

    public void sync(BanWardenSyncType type) {
        hook.sync(type);
    }

    public boolean pardon(BanWardenPunishmentType type, String victim, String reason, String admin) {
        return hook.pardon(type, victim, reason, admin);
    }
}
