package org.jmqtt.broker.common.model;

/**
 * 1+8+8+8
 */
public class MessagePayload {

    private Flag flag; //起始符号位 0xff开头
    private MessagePayloadType type; // 1字节
    private long from; //8字节
    private long to; //8字节
    private long timestamp;//8字节
    private String content;//内容

    
    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }


  
    public MessagePayloadType getType() {
        return type;
    }

    public void setType(MessagePayloadType type) {
        this.type = type;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public enum Flag{

        QMTT_IM(0x7F);

        private int value;

        

        private Flag(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static MessagePayload.Flag valueOf(int type) {
            MessagePayload.Flag[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                MessagePayload.Flag t = var1[var3];
                if (t.value == type) {
                    return t;
                }
            }
            throw new IllegalArgumentException("unknown message Flag: " + type);
        }

       

        


    }

    public enum MessagePayloadType {
        TEXT(0x01),
        IMAGE(0x02),
        VOICE(0x03),
        VIDEO(0x04);


        private int value;

        private MessagePayloadType(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static MessagePayload.MessagePayloadType valueOf(int type) {
            MessagePayload.MessagePayloadType[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                MessagePayload.MessagePayloadType t = var1[var3];
                if (t.value == type) {
                    return t;
                }
            }
            throw new IllegalArgumentException("unknown message type: " + type);
        }
    }

    @Override
    public String toString() {
        return "MessagePayload [content=" + content + ", flag=" + flag + ", from=" + from + ", timestamp=" + timestamp
                + ", to=" + to + ", type=" + type + "]";
    }


  
   
   
}
