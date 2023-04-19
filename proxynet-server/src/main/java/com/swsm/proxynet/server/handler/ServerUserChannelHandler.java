package com.swsm.proxynet.server.handler;

import com.alibaba.fastjson.JSON;
import com.swsm.proxynet.common.cache.ChannelRelationCache;
import com.swsm.proxynet.common.model.CommandInfoMessage;
import com.swsm.proxynet.common.model.ProxyNetMessage;
import com.swsm.proxynet.server.config.ProxyConfig;
import com.swsm.proxynet.server.util.ConfigUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Slf4j
public class ServerUserChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    private Integer serverPort;
    
    public ServerUserChannelHandler(int serverPort) {
        super();
        this.serverPort = serverPort;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        Channel clientToServerChannel = ChannelRelationCache.getClientToServerChannel(serverPort);
        if (clientToServerChannel == null) {
            log.warn("当前用户channel对应的客户端channel还未存在");
            channelHandlerContext.close();
        }
        // 转发用户端请求
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        ProxyConfig.ProxyInfo proxyInfo = ConfigUtil.getProxyInfo(serverPort);
        CommandInfoMessage commandInfoMessage = new CommandInfoMessage(channelHandlerContext.channel().id(), 
                proxyInfo.getTargetIp(), proxyInfo.getTargetPort(), bytes);
        clientToServerChannel.writeAndFlush(ProxyNetMessage.buildCommandMessage(ProxyNetMessage.COMMAND_INFO, JSON.toJSONString(commandInfoMessage)));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("用户连接到服务器...");
        Channel userChannel = ctx.channel();
        Channel clientToServerChannel = ChannelRelationCache.getClientToServerChannel(serverPort);
        if (clientToServerChannel == null) {
            log.warn("客户端和服务端channel关闭了，告诉用户端此时不能访问");
            ctx.close();
        } else {
            ChannelRelationCache.putClientUserChannelRelation(clientToServerChannel, userChannel);
            ChannelRelationCache.putUserChannel(serverPort, userChannel);
            String userId = UUID.randomUUID().toString();
            ChannelRelationCache.putUserIdToUserChannel(userId, userChannel);
            ProxyConfig.ProxyInfo proxyInfo = ConfigUtil.getProxyInfo(serverPort);
            if (proxyInfo == null) {
                log.warn("服务端未配置该代理，告诉用户端此时不能访问");
                ctx.close();
            }
            userChannel.config().setAutoRead(false);
            clientToServerChannel.writeAndFlush(ProxyNetMessage.buildConnectMessage(userId, proxyInfo.getTargetIp(), proxyInfo.getTargetPort()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ChannelRelationCache.removeUserChannel(serverPort, ctx.channel().id());
        ChannelRelationCache.removeClientUserChannelRelation(ctx.channel().id());
        ctx.close();
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("用户channel出现异常", cause);
        ChannelRelationCache.removeUserChannel(serverPort, ctx.channel().id());
        ChannelRelationCache.removeClientUserChannelRelation(ctx.channel().id());
        ctx.close();
        
    }
}
