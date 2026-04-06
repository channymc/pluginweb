package com.channyanh.channyanhweb.common.actions;

import com.google.gson.JsonObject;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.data.PunishmentData;
import com.channyanh.channyanhweb.common.exceptions.HttpException;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;

import java.io.IOException;
import java.util.List;

public class ReportPlayerPunishment {
    private static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();

    public static void syncSync(List<PunishmentData> data) throws HttpException, IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("server_id", common.getPlugin().getApiServerId());
        payload.add("punishments", common.getGson().toJsonTree(data));

        HttpResponse response = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.BANWARDEN_SYNC_PUNISHMENT_ROUTE, payload.toString(), null);

        if (!response.isSuccessful()) {
            throw new HttpException(response, "ReportServerChat.reportAsync");
        }
    }

    public static void reportSync(PunishmentData data) throws HttpException, IOException {
        JsonObject payload = common.getGson().toJsonTree(data).getAsJsonObject();
        payload.addProperty("server_id", common.getPlugin().getApiServerId());

        HttpResponse response = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.BANWARDEN_REPORT_PUNISHMENT_ROUTE, payload.toString(), null);

        if (!response.isSuccessful()) {
            throw new HttpException(response, "ReportServerChat.upsertSync");
        }
    }
}
