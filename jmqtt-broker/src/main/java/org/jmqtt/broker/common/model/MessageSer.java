package org.jmqtt.broker.common.model;

public class MessageSer {

    private  byte[] bytes;
    private MessagePayload messagePayload;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public MessagePayload getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(MessagePayload messagePayload) {
        this.messagePayload = messagePayload;
    }
}
