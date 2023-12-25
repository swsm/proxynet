package com.swsm.proxynet.common.handler;

import com.swsm.proxynet.common.model.ProxyNetMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_INFO_SIZE;
import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_TYPE_SIZE;

/**
 * @author liujie
 * @date 2023-04-15
 */
public class ProxyNetMessageEncoder extends MessageToByteEncoder<ProxyNetMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProxyNetMessage proxyNetMessage, ByteBuf byteBuf) throws Exception {
        int bodyLength = PROXY_MESSAGE_TYPE_SIZE + PROXY_MESSAGE_INFO_SIZE;
        byte[] infoBytes = null;
        if (proxyNetMessage.getInfo() != null) {
            infoBytes = proxyNetMessage.getInfo().getBytes();
            bodyLength += infoBytes.length;
        }

        if (proxyNetMessage.getData() != null) {
            bodyLength += proxyNetMessage.getData().length;
        }

        byteBuf.writeInt(bodyLength);

        byteBuf.writeByte(proxyNetMessage.getType());

        if (infoBytes != null) {
            byteBuf.writeInt(infoBytes.length);
            byteBuf.writeBytes(infoBytes);
        } else {
            byteBuf.writeInt(0x00);
        }

        if (proxyNetMessage.getData() != null) {
            byteBuf.writeBytes(proxyNetMessage.getData());
        }
    }
}
