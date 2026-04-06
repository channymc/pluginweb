package com.channyanh.channyanhweb.common.actions;

import com.google.gson.JsonObject;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.exceptions.HttpException;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import com.channyanh.channyanhweb.common.responses.PlayerWhoisApiResponse;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;

public class WhoisQuery {
    public static PlayerWhoisApiResponse playerSync(String uuid, String username, String ipAddress, Boolean exact) throws Exception {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        if (ipAddress != null) {
            data.addProperty("ip_address", ipAddress);
        }
        if (uuid != null) {
            data.addProperty("uuid", uuid);
        }
        if (exact) {
            data.addProperty("only_exact_result", true);
        }

        String payloadString = data.toString();
        HttpResponse resp = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.FETCH_PLAYER_WHOIS_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "WhoisQuery.playerSync");
        }
        if (body == null) {
            throw new HttpException(resp, "WhoisQuery.playerSync");
        }

        PlayerWhoisApiResponse response = ChannyAnhWEBCommon.getInstance().getGson().fromJson(body, PlayerWhoisApiResponse.class);
        response.setCode(resp.code());

        return response;
    }
}
