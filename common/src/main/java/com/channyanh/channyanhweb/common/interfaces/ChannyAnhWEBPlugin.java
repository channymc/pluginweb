package com.channyanh.channyanhweb.common.interfaces;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;

public interface ChannyAnhWEBPlugin {
    ChannyAnhWEBCommon getCommon();
    String getApiKey();
    String getApiSecret();
    String getApiServerId();
    String getApiHost();
    String getServerSessionId();
    Boolean getIsDebugMode();
}
