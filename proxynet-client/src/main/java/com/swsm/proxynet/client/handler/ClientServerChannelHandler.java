package com.swsm.proxynet.client.handler;

import com.alibaba.fastjson.JSON;
import com.swsm.proxynet.client.config.ConfigUtil;
import com.swsm.proxynet.client.init.SpringInitRunner;
import com.swsm.proxynet.common.Constants;
import com.swsm.proxynet.common.cache.ChannelRelationCache;
import com.swsm.proxynet.common.model.CommandInfoMessage;
import com.swsm.proxynet.common.model.CommandMessage;
import com.swsm.proxynet.common.model.ConnectMessage;
import com.swsm.proxynet.common.model.ProxyNetMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Slf4j
public class ClientServerChannelHandler extends SimpleChannelInboundHandler<ProxyNetMessage> {
    
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyNetMessage proxyNetMessage) throws Exception {
        if (proxyNetMessage.getType() == ProxyNetMessage.CONNECT) {
            executeConnect(proxyNetMessage, channelHandlerContext);
        } else if (proxyNetMessage.getType() == ProxyNetMessage.COMMAND) {
            executeCommand(proxyNetMessage, channelHandlerContext);
        }
    }

    private void executeCommand(ProxyNetMessage proxyNetMessage, ChannelHandlerContext channelHandlerContext) {
        Channel clientChannel = channelHandlerContext.channel();
        CommandMessage commandMessage = JSON.parseObject(proxyNetMessage.getInfo(), CommandMessage.class);
        if (ProxyNetMessage.COMMAND_INFO.equals(commandMessage.getType())) {
            CommandInfoMessage commandInfoMessage = JSON.parseObject(commandMessage.getMessage(), CommandInfoMessage.class);
            Channel targetChannel = clientChannel.attr(Constants.NEXT_CHANNEL).get();
            if (targetChannel == null) {
                log.info("targetInfo={}的客户端还未和代理客户端建立连接", commandInfoMessage.getTargetIp() + ":" + commandInfoMessage.getTargetPort());
                return;
            }
            ByteBuf data = channelHandlerContext.alloc().buffer(proxyNetMessage.getData().length);
            data.writeBytes(proxyNetMessage.getData());
            targetChannel.writeAndFlush(data);
        }
    }

    private void executeConnect(ProxyNetMessage proxyNetMessage, ChannelHandlerContext channelHandlerContext) {
        ConnectMessage connectMessage = JSON.parseObject(proxyNetMessage.getInfo(), ConnectMessage.class);
        log.info("收到服务端发送的connect消息:{}", proxyNetMessage);
        log.info("向目标服务={}发起连接...", proxyNetMessage);
        try {
            SpringInitRunner.bootstrapForTarget.connect(connectMessage.getTargetIp(), connectMessage.getTargetPort())
                    .addListener((ChannelFutureListener) future -> {
                        Channel targetChannel = future.channel();
                        if (future.isSuccess()) {
                            targetChannel.config().setOption(ChannelOption.AUTO_READ, false);
                            log.info("向目标服务={}发起连接 成功...", proxyNetMessage);

                            String serverIp = ConfigUtil.getServerIp();
                            int serverPort = ConfigUtil.getServerPort();
                            SpringInitRunner.bootstrapForServer.connect(serverIp, serverPort)
                                    .addListener((ChannelFutureListener) future2 -> {
                                        if (future2.isSuccess()) {
                                            Channel newClientChannel = future2.channel();
                                            log.info("监控--clinetChannelId={},newClientChannelId={},realServerChannelId={},visitorId={}", channelHandlerContext.channel().id().asLongText(), newClientChannel.id().asLongText(),targetChannel.id().asLongText(), connectMessage.getUserId());
                                            newClientChannel.attr(Constants.NEXT_CHANNEL).set(targetChannel);
                                            targetChannel.attr(Constants.NEXT_CHANNEL).set(newClientChannel);
                                            ChannelRelationCache.putUserIdToTargetChannel(connectMessage.getUserId(), targetChannel);
                                            targetChannel.attr(Constants.VISITOR_ID).set(connectMessage.getUserId());
                                            newClientChannel.writeAndFlush(ProxyNetMessage.buildConnectRespMessage("连接目标服务端成功", true, connectMessage.getUserId()));
                                            targetChannel.config().setOption(ChannelOption.AUTO_READ, true);
                                        } else {
                                            log.error("客户端向服务端发起新连接失败");
                                            channelHandlerContext.close();
                                        }
                                    });
                            
                            ChannelRelationCache.putTargetChannel(connectMessage.getTargetIp(), connectMessage.getTargetPort(), targetChannel);
                            ChannelRelationCache.putUserIdToTargetChannel(connectMessage.getUserId(), targetChannel);
                            ChannelRelationCache.putTargetChannelToClientChannel(targetChannel, channelHandlerContext.channel());
                            channelHandlerContext.writeAndFlush(ProxyNetMessage.buildConnectRespMessage("连接目标服务端成功", true, connectMessage.getUserId()));
                        } else {
                            log.info("向目标服务={}发起连接 失败...", proxyNetMessage);
                            channelHandlerContext.writeAndFlush(ProxyNetMessage.buildConnectRespMessage("连接目标服务端异常", false, connectMessage.getUserId()));
                        }
                    }).sync();
        } catch (InterruptedException e) {
            log.error("客户端连接目标服务器出现异常", e);
            channelHandlerContext.writeAndFlush(ProxyNetMessage.buildConnectRespMessage("连接目标服务端异常", false, connectMessage.getUserId()));
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }
}
