package org.jmqtt.broker.remoting.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.MessageHeader;
import org.jmqtt.broker.common.model.MessagePayload;
import org.jmqtt.broker.common.model.MessageSer;
import java.util.List;

/**
 * transfer message from Message and MqttMessage
 */
public class MessageUtil {

    public static byte[] readBytesFromByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    /**
     * 解析内容
     *
     * @param byteBuf
     * @return
     */
    public static MessageSer readBytesFromByteBufMessgePayload(ByteBuf byteBuf) {
        MessageSer messageSer = new MessageSer();
        ByteBuf copyByteBuf = byteBuf.copy();
        byte[] bytes = new byte[copyByteBuf.readableBytes()];


        copyByteBuf.readBytes(bytes);
        messageSer.setBytes(bytes);
        while (byteBuf.readableBytes() > 0) {
            if (!doDecode(byteBuf, messageSer)) {
                break;
            }
        }
        return messageSer;
    }

    /**
     * //大于 1+1+8+8+8
     *
     * @param msg
     * @return
     */
    private static boolean doDecode(ByteBuf msg, MessageSer messageSer) {
        //这个表示头长度的字节数。
        //1.msg由包头以及包体组成，小于2byte不处理丢弃掉。
        if (msg.readableBytes() < 26) {
            return false;
        }
        //2.我们标记一下当前的readIndex的位置
        msg.markReaderIndex();
        //3.解析
        // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
        byte flag = msg.readByte();//1字节
        byte type = msg.readByte();//1字节
        long from = msg.readLong();//8字节
        long to = msg.readLong();//8字节
        long timestamp = msg.readLong();//8字节
        MessagePayload messagePayload = new MessagePayload();
        messagePayload.setFlag(MessagePayload.Flag.valueOf(flag));
        messagePayload.setType(MessagePayload.MessagePayloadType.valueOf(type));
        messagePayload.setFrom(from);
        messagePayload.setTo(to);
        messagePayload.setTimestamp(timestamp);
        messageSer.setMessagePayload(messagePayload);
        int len = msg.readableBytes();
        if (len <= 0) {
            msg.resetReaderIndex();
            return false;
        }
        byte[] body = new byte[len];
        msg.readBytes(body);
        messagePayload.setContent(new String(body));

        if (msg.readableBytes() > 0) {
            return true;
        }

        return false;

    }


    public static MqttUnsubAckMessage getUnSubAckMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttUnsubAckMessage(fixedHeader, idVariableHeader);
    }

    public static int getMessageId(MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader idVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        return idVariableHeader.messageId();
    }

    public static int getMinQos(int qos1, int qos2) {
        if (qos1 < qos2) {
            return qos1;
        }
        return qos2;
    }

    public static MqttMessage getPubRelMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttMessage(fixedHeader, idVariableHeader);
    }

    public static MqttPublishMessage getPubMessage(Message message, boolean dup) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, dup, MqttQoS.valueOf((int) message.getHeader(MessageHeader.QOS)), false, 0);
        MqttPublishVariableHeader publishVariableHeader = new MqttPublishVariableHeader((String) message.getHeader(MessageHeader.TOPIC), message.getMsgId());
        ByteBuf heapBuf;
        if (message.getPayload() == null) {
            heapBuf = Unpooled.EMPTY_BUFFER;
        } else {
            heapBuf = Unpooled.wrappedBuffer((byte[]) message.getPayload());
        }
        return new MqttPublishMessage(fixedHeader, publishVariableHeader, heapBuf);
    }

    public static MqttMessage getSubAckMessage(int messageId, List<Integer> qos) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttSubAckPayload subAckPayload = new MqttSubAckPayload(qos);
        return new MqttSubAckMessage(fixedHeader, idVariableHeader, subAckPayload);
    }

    public static MqttMessage getPingRespMessage() {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader);
        return mqttMessage;
    }

    public static MqttMessage getPubComMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttMessage getPubRecMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttMessage getPubRecMessage(int messageId, boolean isDup) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, isDup, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttPubAckMessage getPubAckMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(fixedHeader, idVariableHeader);
    }

    public static MqttConnAckMessage getConnectAckMessage(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader variableHeade = new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(fixedHeader, variableHeade);
    }
}
