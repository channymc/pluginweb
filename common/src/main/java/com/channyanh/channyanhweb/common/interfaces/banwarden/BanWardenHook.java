package com.channyanh.channyanhweb.common.interfaces.banwarden;

import com.channyanh.channyanhweb.common.data.PunishmentData;
import com.channyanh.channyanhweb.common.enums.BanWardenPunishmentType;
import com.channyanh.channyanhweb.common.enums.BanWardenSyncType;

public interface BanWardenHook {
    String punish(BanWardenPunishmentType type, String punishmentString);
    boolean pardon(BanWardenPunishmentType type, String victim, String reason, String admin);
    PunishmentData getPunishment(String punishmentId);
    void sync(BanWardenSyncType type);
}
