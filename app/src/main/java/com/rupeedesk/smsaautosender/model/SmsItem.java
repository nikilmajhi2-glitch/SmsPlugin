package com.rupeedesk.smsaautosender.model;

public class SmsItem {
    private String recipient;
    private String message;
    private long scheduledTime;
    private boolean sent;

    public SmsItem() {}

    public SmsItem(String recipient, String message, long scheduledTime, boolean sent) {
        this.recipient = recipient;
        this.message = message;
        this.scheduledTime = scheduledTime;
        this.sent = sent;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
