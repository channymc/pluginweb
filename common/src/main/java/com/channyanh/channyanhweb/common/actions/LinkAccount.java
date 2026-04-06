package com.channyanh.channyanhweb.common.actions;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.exceptions.HttpException;
import com.channyanh.channyanhweb.common.responses.GenericApiResponse;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;

import java.util.HashMap;

public class LinkAccount {
    public static GenericApiResponse link(String playerUuid, String otpCode, String serverId) throws Exception {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("uuid", playerUuid);
        payload.put("server_id", serverId);
        payload.put("otp", otpCode);
        String payloadString = ChannyAnhWEBCommon.getInstance().getGson().toJson(payload);
        HttpResponse resp = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.VERIFY_ACCOUNT_LINK_ROUTE, payloadString, null);
        String body = resp.body();
        if (body == null) {
            throw new HttpException(resp, "LinkAccount.link");
        }

        GenericApiResponse response = ChannyAnhWEBCommon.getInstance().getGson().fromJson(body, GenericApiResponse.class);
        response.setCode(resp.code());

        return response;
    }
}
