package com.swsm.proxynet.client.handler;

import com.alibaba.fastjson.JSON;
import com.swsm.proxynet.common.cache.ChannelRelationCache;
import com.swsm.proxynet.common.model.CommandRespMessage;
import com.swsm.proxynet.common.model.ProxyNetMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Slf4j
public class ClientTargetChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        Channel targetChannel = channelHandlerContext.channel();
        Channel clientChannel = ChannelRelationCache.getClientChannel(targetChannel);
        if (clientChannel == null) {
            log.warn("目的端返回给代理端信息，但代理端和服务端的channel已关闭");
            targetChannel.close();
            return;
        }
        String userId = ChannelRelationCache.getUserIdByTargetChannel(targetChannel);
        if (userId == null) {
            log.warn("目的端返回给代理端信息，但用户端和服务端的channel已关闭");
            targetChannel.close();
            return;
        }
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        clientChannel.writeAndFlush(ProxyNetMessage.buildCommandMessage(
                ProxyNetMessage.COMMAND_RESP, JSON.toJSONString(new CommandRespMessage(userId, bytes))));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelRelationCache.removeTargetChannel(ctx.channel());
        ChannelRelationCache.removeTargetChannelToClientChannel(ctx.channel());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ChannelRelationCache.removeTargetChannel(ctx.channel());
        ChannelRelationCache.removeTargetChannelToClientChannel(ctx.channel());
        ctx.close();
    }

    public static void main(String[] args) {
        String ss = "SgAAAAo1LjcuMjYADAAAAE9DOh82DXx/AP/3CAIA/4EVAAAAAAAAAAAAAG4xGidPGBcfPgFbdABteXNxbF9uYXRpdmVfcGFzc3dvcmQA";
        byte[] bytes = ss.getBytes(Charset.forName("utf-8"));
        System.out.println(bytes);
    }
    
}
