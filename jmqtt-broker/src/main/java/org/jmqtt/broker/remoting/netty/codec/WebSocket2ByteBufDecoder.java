package org.jmqtt.broker.remoting.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.slf4j.Logger;


public class WebSocket2ByteBufDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    private static final Logger log = JmqttLogger.brokerlog;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame binaryWebSocketFrame, List<Object> list) throws Exception {
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        LogUtil.debug(log,"byteBuf = {}",byteBuf);
        byteBuf.retain();
        list.add(byteBuf);
    }
}
