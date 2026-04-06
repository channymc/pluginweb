package com.channyanh.channyanhweb.common.webquery;

import com.google.gson.JsonObject;
import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;
import com.channyanh.channyanhweb.common.utils.LoggingUtil;
import com.channyanh.channyanhweb.common.webquery.protocol.WebQueryProtocol;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class WebQueryChannelHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String response = null;
        String status;
        String clientIp = ctx.channel().remoteAddress().toString();
        try {
            response = WebQueryProtocol.processInput(s);
            status = "ok";

            if (response == null) {
                ChannyAnhWEBCommon.getInstance().getLogger().error("[WebQuery]["+clientIp+"] Error processing input: output is null.");
                status = "error";
            }
        } catch (Exception e) {
            ChannyAnhWEBCommon.getInstance().getLogger().error("[WebQuery]["+clientIp+"] Error processing input: " + e.getMessage());
            LoggingUtil.trace(e);
            status = "error";
        }
        JsonObject object = new JsonObject();
        object.addProperty("status", status);
        object.addProperty("data", response);
        ctx.channel().writeAndFlush(ChannyAnhWEBCommon.getInstance().getGson().toJson(object) + "\r\n").addListener(ChannelFutureListener.CLOSE);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String clientIp = ctx.channel().remoteAddress().toString();
        ChannyAnhWEBCommon.getInstance().getLogger().error("[WebQuery]["+clientIp+"] Exception caught: " + cause.getMessage());
        if (ChannyAnhWEBCommon.getInstance().getPlugin().getIsDebugMode()) {
            cause.printStackTrace();
        }
        ctx.close();
    }
}
