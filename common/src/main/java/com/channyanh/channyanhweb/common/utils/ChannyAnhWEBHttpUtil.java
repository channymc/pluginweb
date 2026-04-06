package com.channyanh.channyanhweb.common.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChannyAnhWEBHttpUtil {
    public static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();
    public static final String ACCOUNT_LINK_ROUTE = "/user/linked-players";
    public static final String VERIFY_ACCOUNT_LINK_ROUTE = "/api/v1/account-link/verify";
    public static final String REPORT_SERVER_CHAT_ROUTE = "/api/v1/server/chat";
    public static final String REPORT_SERVER_CONSOLE_ROUTE = "/api/v1/server/console";
    public static final String FETCH_PLAYER_WHOIS_ROUTE = "/api/v1/player/whois";
    public static final String FETCH_PLAYER_DATA_ROUTE = "/api/v1/player/data";
    public static final String SERVER_INTEL_REPORT_ROUTE = "/api/v1/intel/server/report";
    public static final String PLAYER_INTEL_SESSION_INIT_ROUTE = "/api/v1/intel/player/session-init";
    public static final String PLAYER_INTEL_EVENT_REPORT_ROUTE = "/api/v1/intel/player/report/event";
    public static final String PLAYER_INTEL_REPORT_PVP_KILL_ROUTE = "/api/v1/intel/player/report/pvp-kill";
    public static final String PLAYER_INTEL_REPORT_DEATH_ROUTE = "/api/v1/intel/player/report/death";
    public static final String BANWARDEN_REPORT_PUNISHMENT_ROUTE = "/api/v1/banwarden/report/punishment";
    public static final String BANWARDEN_SYNC_PUNISHMENT_ROUTE = "/api/v1/banwarden/sync/punishment";

    public static HttpResponse get(String path, Map<String, String> params, Map<String, String> headers) throws IOException {
        String ChannyAnhWEBApiHost = common.getPlugin().getApiHost();
        if (ChannyAnhWEBApiHost != null) {
            ChannyAnhWEBApiHost = StringUtils.stripEnd(ChannyAnhWEBApiHost, "/");
        }
        String ChannyAnhWEBApiKey = common.getPlugin().getApiKey();
        String ChannyAnhWEBApiSecret = common.getPlugin().getApiSecret();

        // Add current timestamp in milliseconds to the params
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String uri = ChannyAnhWEBApiHost + path;
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(uri)).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            httpBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String fullUriToSign = httpBuilder.build().toString();
        headers = generateHeaders(headers, ChannyAnhWEBApiKey, ChannyAnhWEBApiSecret, fullUriToSign);

        return HttpUtil.get(uri, params, headers);
    }

    public static HttpResponse post(String path, String payload, Map<String, String> headers) throws IOException {
        String ChannyAnhWEBApiHost = common.getPlugin().getApiHost();
        if (ChannyAnhWEBApiHost != null) {
            ChannyAnhWEBApiHost = StringUtils.stripEnd(ChannyAnhWEBApiHost, "/");
        }
        String ChannyAnhWEBApiKey = common.getPlugin().getApiKey();
        String ChannyAnhWEBApiSecret = common.getPlugin().getApiSecret();

        // Add current timestamp in milliseconds to the payload
        JsonElement payloadJson = common.getGson().fromJson(payload, JsonElement.class);
        JsonObject finalPayload = new JsonObject();
        finalPayload.addProperty("timestamp", System.currentTimeMillis());
        finalPayload.add("data", payloadJson);
        String payloadString = finalPayload.toString();

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String uri = ChannyAnhWEBApiHost + path;
        headers = generateHeaders(headers, ChannyAnhWEBApiKey, ChannyAnhWEBApiSecret, payloadString);

        return HttpUtil.post(uri, payloadString, headers);
    }

    @NotNull
    private static Map<String, String> generateHeaders(Map<String, String> headers, String ChannyAnhWEBApiKey, String ChannyAnhWEBApiSecret, String payloadString) {
        String signature = CryptoUtil.getHmacSignature(ChannyAnhWEBApiSecret, payloadString);
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("x-api-key", ChannyAnhWEBApiKey);
        headers.put("x-signature", signature);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }

    public static String getUrl(String route) {
        String ChannyAnhWEBApiHost = common.getPlugin().getApiHost();
        if (ChannyAnhWEBApiHost != null) {
            ChannyAnhWEBApiHost = StringUtils.stripEnd(ChannyAnhWEBApiHost, "/");
        }
        return ChannyAnhWEBApiHost + route;
    }
}
