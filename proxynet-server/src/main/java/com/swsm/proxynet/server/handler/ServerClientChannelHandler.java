package com.swsm.proxynet.server.handler;

import com.alibaba.fastjson.JSON;
import com.swsm.proxynet.common.cache.ChannelRelationCache;
import com.swsm.proxynet.common.model.CommandMessage;
import com.swsm.proxynet.common.model.CommandRespMessage;
import com.swsm.proxynet.common.model.ConnectRespMessage;
import com.swsm.proxynet.common.model.ProxyNetMessage;
import com.swsm.proxynet.server.config.ProxyConfig;
import com.swsm.proxynet.server.util.ConfigUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Slf4j
public class ServerClientChannelHandler extends SimpleChannelInboundHandler<ProxyNetMessage> {
    

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyNetMessage proxyNetMessage) throws Exception {
        log.info("收到客户端的请求:{}", proxyNetMessage);
        if (proxyNetMessage.getType() == ProxyNetMessage.COMMAND) {
            executeCommand(channelHandlerContext, proxyNetMessage);
        } else if (proxyNetMessage.getType() == ProxyNetMessage.CONNECT_RESP) {
            executeConnectResp(proxyNetMessage, channelHandlerContext);
        }
    }

    private void executeConnectResp(ProxyNetMessage proxyNetMessage, ChannelHandlerContext channelHandlerContext) {
        ConnectRespMessage connectRespMessage = JSON.parseObject(proxyNetMessage.getInfo(), ConnectRespMessage.class);
        boolean isSuccess = connectRespMessage.getResult();
        log.info("客户端与目标服务连接结果={}", connectRespMessage);
        if (!isSuccess) {
            log.info("客户端与目标服务连接没有成功，关闭所有连接此目标服务的channel", connectRespMessage);
            List<Channel> userChannelList = ChannelRelationCache.getUserChannelList(channelHandlerContext.channel().id());
            for (Channel channel : userChannelList) {
                channel.close();
            }
            return;
        }
        Channel userChannel = ChannelRelationCache.getUserChannel(connectRespMessage.getUserId());
        if (userChannel == null) {
            log.warn("用户id={}的channel已经断开!", connectRespMessage.getUserId());
            return;
        }
        userChannel.config().setAutoRead(true);
    }

    private void executeCommand(ChannelHandlerContext channelHandlerContext, ProxyNetMessage proxyNetMessage) {
        Channel clientChannel = channelHandlerContext.channel();
        String info = proxyNetMessage.getInfo();
        CommandMessage commandMessage = JSON.parseObject(info, CommandMessage.class);
        if (commandMessage.getType().equals(ProxyNetMessage.COMMAND_AUTH)) {
            for (ProxyConfig.ProxyInfo proxyInfo : ConfigUtil.getProxyInfos()) {
                if (proxyInfo.getClientId().equals(Integer.valueOf(commandMessage.getMessage()))) {
                    log.info("clientId={}的客户端和服务端已建立连接并完成认证", proxyInfo.getClientId());
                    ChannelRelationCache.putClientChannel(proxyInfo.getServerPort(), clientChannel);
                    log.info("server启动对应用户监听服务端 监听用户请求...");
                    startUserServer(proxyInfo);
                }
            }
        } else if (commandMessage.getType().equals(ProxyNetMessage.COMMAND_RESP)) {
            CommandRespMessage commandRespMessage = JSON.parseObject(commandMessage.getMessage(), CommandRespMessage.class);
            Channel userChannel = ChannelRelationCache.getUserChannel(commandRespMessage.getUserId());
            if (userChannel == null) {
                log.warn("用户端和服务端的连接已断开...");
                return;
            }
            ByteBuf buf = channelHandlerContext.alloc().buffer(proxyNetMessage.getData().length);
            buf.writeBytes(proxyNetMessage.getData());
            userChannel.writeAndFlush(buf);
        }
    }

    private void startUserServer(ProxyConfig.ProxyInfo proxyInfo) {
        log.info("启动 用户监听服务端,proxyInfo={}...", proxyInfo);
        ServerBootstrap bootstrapForUser = new ServerBootstrap();
        bootstrapForUser.group(new NioEventLoopGroup(4), new NioEventLoopGroup(8))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ServerUserChannelHandler(proxyInfo.getServerPort()));
                    }
                });
        try {
            bootstrapForUser.bind(proxyInfo.getServerPort()).sync();
        } catch (Exception e) {
            log.error("启动 用户监听服务端出现异常", e);
        }
        log.info("启动 用户监听服务端 成功...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ChannelRelationCache.removeClientChannel(ctx.channel().id());
        super.exceptionCaught(ctx, cause);
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }
}
