package com.channyanh.channyanhweb.common.actions;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.data.ServerIntelData;
import com.channyanh.channyanhweb.common.exceptions.HttpException;
import com.channyanh.channyanhweb.common.responses.HttpResponse;
import com.channyanh.channyanhweb.common.utils.ChannyAnhWEBHttpUtil;

public class ReportServerIntel {
    private static final ChannyAnhWEBCommon common = ChannyAnhWEBCommon.getInstance();

    public static void reportSync(ServerIntelData data) throws Exception {
        String payload = common.getGson().toJson(data);
        HttpResponse response = ChannyAnhWEBHttpUtil.post(ChannyAnhWEBHttpUtil.SERVER_INTEL_REPORT_ROUTE, payload, null);

        if (!response.isSuccessful()) {
            throw new HttpException(response, "ReportServerIntel.reportSync");
        }
    }
}
