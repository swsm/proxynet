package com.swsm.proxynet.common.handler;

import com.swsm.proxynet.common.model.ProxyNetMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_INFO_SIZE;
import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_TOTAL_SIZE;
import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_TYPE_SIZE;

/**
 * @author liujie
 * @date 2023-04-15
 */
public class ProxyNetMessageDecoder extends LengthFieldBasedFrameDecoder {
    public ProxyNetMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf newIn = (ByteBuf) super.decode(ctx, in);
        if (newIn == null) {
            return null;
        }

        if (newIn.readableBytes() < PROXY_MESSAGE_TOTAL_SIZE) {
            return null;
        }

        int frameLength = newIn.readInt();
        if (newIn.readableBytes() < frameLength) {
            return null;
        }
        ProxyNetMessage proxyNetMessage = new ProxyNetMessage();
        byte type = newIn.readByte();
        proxyNetMessage.setType(type);

        int infoLength = newIn.readInt();
        byte[] infoBytes = new byte[infoLength];
        newIn.readBytes(infoBytes);
        proxyNetMessage.setInfo(new String(infoBytes));

        byte[] data = new byte[frameLength - PROXY_MESSAGE_TYPE_SIZE - PROXY_MESSAGE_INFO_SIZE - infoLength];
        newIn.readBytes(data);
        proxyNetMessage.setData(data);

        newIn.release();

        return proxyNetMessage;
    }
}
