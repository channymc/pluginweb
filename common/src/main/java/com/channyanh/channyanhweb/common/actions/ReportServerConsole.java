package com.channyanh.channyanhweb.common.actions;

import com.google.gson.JsonObject;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.exceptions.HttpException;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;

import java.io.IOException;

public class ReportServerConsole {
    private static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();

    public static void reportSync(String log) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("log", log);
        payload.addProperty("server_id", common.getPlugin().getApiServerId());

        HttpResponse response = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.REPORT_SERVER_CONSOLE_ROUTE, payload.toString(), null);

        if (!response.isSuccessful()) {
            throw new HttpException(response, "ReportServerConsole.reportSync");
        }
    }
}
