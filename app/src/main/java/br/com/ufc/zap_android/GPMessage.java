package br.com.ufc.zap_android;

public class GPMessage {
    private String text;
    private long timeStamp;
    private String fromId;
    private String toId;

    public GPMessage(){}

    public GPMessage(String text, long timeStamp, String fromId) {

        this.text = text;
        this.timeStamp = timeStamp;
        this.fromId = fromId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }
}
