package com.channyanh.channyanhweb.common.banwarden.hooks;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.actions.ReportPlayerPunishment;
import com.channyanh.channyanhweb.common.data.PunishmentData;
import com.channyanh.channyanhweb.common.enums.BanWardenPluginType;
import com.channyanh.channyanhweb.common.enums.BanWardenPunishmentType;
import com.channyanh.channyanhweb.common.enums.BanWardenSyncType;
import com.channyanh.channyanhweb.common.interfaces.banwarden.BanWardenHook;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import space.arim.libertybans.api.*;
import space.arim.libertybans.api.event.PostPardonEvent;
import space.arim.libertybans.api.event.PostPunishEvent;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.libertybans.api.select.SelectionPredicate;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;
import space.arim.omnibus.events.EventConsumer;
import space.arim.omnibus.events.ListenerPriorities;
import space.arim.omnibus.util.concurrent.ReactionStage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LibertybansHook implements BanWardenHook {
    public static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();
    private final Omnibus omnibus;
    private final LibertyBans libertyBans;
    private static final int RATE_LIMIT_DELAY_SECONDS = 1;
    private static final int CHUNK_SIZE = 50;

    public LibertybansHook() {
        this.omnibus = OmnibusProvider.getOmnibus();
        this.libertyBans = omnibus.getRegistry().getProvider(LibertyBans.class).orElseThrow();

        registerEventListeners();
    }

    @Override
    public String punish(BanWardenPunishmentType type, String punishmentString) {
        return "";
    }

    @Override
    public boolean pardon(BanWardenPunishmentType type, String victim, String reason, String admin) {
        switch (type) {
            case BAN -> common.getCommander().dispatchCommand("libertybans:unban " + victim);
            case MUTE -> common.getCommander().dispatchCommand("libertybans:unmute " + victim);
            case WARN -> common.getCommander().dispatchCommand("libertybans:unwarn " + victim);
            default -> LoggingUtil.error("[LitebansHook] Pardon -> Unknown punishment type: " + type);
        }
        return true;
    }

    @Override
    public PunishmentData getPunishment(String punishmentId) {
        return null;
    }

    @Override
    public void sync(BanWardenSyncType type) {
        if (type != BanWardenSyncType.ACTIVE) {
            // info that LibertyBans only supports active bans syncing atm
            LoggingUtil.warning("[BanWarden] Note: LibertyBans integration only supports syncing active bans atm.");
        }

        syncActivePunishments().thenAccept(aVoid -> {
            LoggingUtil.debug("[BanWarden] Syncing of punishments from LibertyBans completed.");
        });
    }

    private void registerEventListeners() {
        EventConsumer<PostPunishEvent> punishListener = new EventConsumer<>() {
            @Override
            public void accept(PostPunishEvent event) {
                Punishment punishment = event.getPunishment();
                ChannyAnhWEBCommon.getInstance().getScheduler().runAsync(() -> {
                    try {
                        PunishmentData data = convertPunishmentToData(punishment, true, null, "punish");
                        ReportPlayerPunishment.reportSync(data);
                    } catch (Exception e) {
                        LoggingUtil.error("[BanWarden] PunishEvent -> Error reporting event to ChannyAnhWEB: " + e.getMessage());
                    }
                });
            }
        };

        EventConsumer<PostPardonEvent> pardonListener = new EventConsumer<>() {
            @Override
            public void accept(PostPardonEvent event) {
                Punishment punishment = event.getPunishment();
                Operator operator = event.getOperator();
                ChannyAnhWEBCommon.getInstance().getScheduler().runAsync(() -> {
                    try {
                        PunishmentData data = convertPunishmentToData(punishment, false, operator, "pardon");
                        ReportPlayerPunishment.reportSync(data);
                    } catch (Exception e) {
                        LoggingUtil.error("[BanWarden] PardonEvent -> Error reporting event to ChannyAnhWEB: " + e.getMessage());
                    }
                });
            }
        };

        omnibus.getEventBus().registerListener(PostPunishEvent.class, ListenerPriorities.NORMAL, punishListener);
        omnibus.getEventBus().registerListener(PostPardonEvent.class, ListenerPriorities.NORMAL, pardonListener);
    }

    private CompletableFuture<Void> syncActivePunishments() {
        AtomicInteger totalPunishments = new AtomicInteger(0);
        AtomicInteger offset = new AtomicInteger(0);

        return CompletableFuture.<Void>completedFuture(null).thenCompose(new Function<Void, ReactionStage<Void>>() {
            @Override
            public ReactionStage<Void> apply(Void unused) {
                return fetchPunishmentChunk(CHUNK_SIZE, offset.get())
                        .thenCompose(punishmentList -> {
                            int fetchedCount = punishmentList.size();
                            totalPunishments.addAndGet(fetchedCount);

                            // Process the current chunk
                            List<PunishmentData> punishmentDataList = new ArrayList<>();
                            punishmentList.forEach(punishment -> {
                                PunishmentData data = convertPunishmentToData(punishment, true, null, "sync");
                                punishmentDataList.add(data);
                            });

                            // Report to ChannyAnhWEB with API
                            try {
                                if (!punishmentDataList.isEmpty()) {
                                    LoggingUtil.info("Syncing batch of " + punishmentDataList.size() + " punishments to ChannyAnhWEB...");
                                    ReportPlayerPunishment.syncSync(punishmentDataList);
                                }
                            } catch (Exception e) {
                                LoggingUtil.error("[BanWarden] Error syncing punishments to ChannyAnhWEB: " + e.getMessage());
                            }

                            if (fetchedCount == CHUNK_SIZE) {
                                // If we fetched a full chunk, there might be more bans
                                offset.addAndGet(CHUNK_SIZE);

                                // some delay to avoid rate limit.
                                try {
                                    Thread.sleep(TimeUnit.SECONDS.toMillis(RATE_LIMIT_DELAY_SECONDS));
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                return apply(null); // Recurse to fetch the next chunk
                            } else {
                                // We've fetched all bans
                                LoggingUtil.info("[BanWarden] Finished syncing total of " + totalPunishments.get() + " punishments from LibertyBans.");
                                return CompletableFuture.completedFuture(null);
                            }
                        });
            }
        });
    }

    private ReactionStage<List<Punishment>> fetchPunishmentChunk(int limit, int offset) {
        return libertyBans.getSelector()
                .selectionBuilder()
                .selectActiveOnly()
                .types(SelectionPredicate.matchingAnyOf(PunishmentType.BAN, PunishmentType.MUTE, PunishmentType.WARN))
                .skipFirstRetrieved(offset)
                .limitToRetrieve(limit)
                .build()
                .getAllSpecificPunishments();
    }

    private BanWardenPunishmentType getBanWardenPunishmentType(PunishmentType type) {
        return switch (type) {
            case BAN -> BanWardenPunishmentType.BAN;
            case MUTE -> BanWardenPunishmentType.MUTE;
            case WARN -> BanWardenPunishmentType.WARN;
            case KICK -> BanWardenPunishmentType.KICK;
            default -> BanWardenPunishmentType.UNKNOWN;
        };
    }

    private PunishmentData convertPunishmentToData(Punishment punishment, boolean isActive, Operator pardonOperator, String fromEvent) {
        PunishmentData punishmentData = new PunishmentData();
        punishmentData.plugin_name = BanWardenPluginType.LIBERTYBANS.name().toLowerCase();
        punishmentData.plugin_punishment_id = String.valueOf(punishment.getIdentifier());
        punishmentData.type = getBanWardenPunishmentType(punishment.getType()).name().toLowerCase();
        punishmentData.start_at = punishment.getStartDateSeconds() * 1000;
        punishmentData.end_at = punishment.getEndDateSeconds() * 1000;
        punishmentData.reason = punishment.getReason();
        punishmentData.is_active = isActive;
        punishmentData.from_event = fromEvent;

        // Server scope
        if (punishment.getScope().appliesTo("*")) {
            punishmentData.server_scope = "*";
        } else {
            punishmentData.server_scope = "local"; // TODO: how to get which server this ban apply to. (ie, server_origin)
        }

        // victim
        if (punishment.getVictim().getType() == Victim.VictimType.PLAYER) {
            PlayerVictim victim = (PlayerVictim) punishment.getVictim();
            punishmentData.uuid = victim.getUUID().toString();
            punishmentData.is_ipban = false;
        } else if (punishment.getVictim().getType() == Victim.VictimType.COMPOSITE) {
            CompositeVictim victim = (CompositeVictim) punishment.getVictim();
            punishmentData.uuid = victim.getUUID().toString();
            punishmentData.ip_address = victim.getAddress().toInetAddress().getHostAddress();
            punishmentData.is_ipban = true;
        } else {
            AddressVictim victim = (AddressVictim) punishment.getVictim();
            punishmentData.ip_address = victim.getAddress().toInetAddress().getHostAddress();
            punishmentData.is_ipban = true;
        }

        // creator
        if (punishment.getOperator().getType() == Operator.OperatorType.PLAYER) {
            PlayerOperator operator = (PlayerOperator) punishment.getOperator();
            punishmentData.creator_uuid = operator.getUUID().toString();
            punishmentData.creator_username = null;
        } else {
            punishmentData.creator_username = "CONSOLE";
        }

        // pardon operator
        if (pardonOperator != null) {
            punishmentData.removed_at = System.currentTimeMillis();
            if (pardonOperator.getType() == Operator.OperatorType.PLAYER) {
                PlayerOperator operator = (PlayerOperator) pardonOperator;
                punishmentData.remover_uuid = operator.getUUID().toString();
                punishmentData.remover_username = null;
            } else {
                punishmentData.remover_username = "CONSOLE";
            }
        }

        return punishmentData;
    }
}
