package com.channyanh.channyanhweb.common.actions;

import com.google.gson.JsonObject;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.exceptions.HttpException;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;

public class ReportServerChat {
    private static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();

    public static void reportAsync(String type, String message, String causerUsername, String causerUuid) {
        common.getScheduler().runAsync(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("type", type);
                payload.addProperty("chat", message);
                payload.addProperty("causer_username", causerUsername);
                payload.addProperty("causer_uuid", causerUuid);
                payload.addProperty("server_id", common.getPlugin().getApiServerId());

                HttpResponse response = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.REPORT_SERVER_CHAT_ROUTE, payload.toString(), null);

                if (!response.isSuccessful()) {
                    throw new HttpException(response, "ReportServerChat.reportAsync");
                }
            } catch (Exception e) {
                LoggingUtil.warning(e.getMessage());
            }
        });
    }
}
