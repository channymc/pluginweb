package com.channyanh.channyanhweb.common.actions;

import com.google.gson.JsonObject;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.exceptions.HttpException;
import com.channyanh.channyanhweb.common.responses.GenericApiResponse;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;

public class FetchPlayerData {
    private static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();

    public static GenericApiResponse getSync(String username, String uuid) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("uuid", uuid);
        payload.addProperty("server_id", common.getPlugin().getApiServerId());
        String payloadString = payload.toString();
        HttpResponse resp = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.FETCH_PLAYER_DATA_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "FetchPlayerData.getSync");
        }
        if (body == null) {
            throw new HttpException(resp, "FetchPlayerData.getSync");
        }

        GenericApiResponse response = ChannyAnhWEBCommon.getInstance().getGson().fromJson(body, GenericApiResponse.class);
        response.setCode(resp.code());
        return response;
    }
}
