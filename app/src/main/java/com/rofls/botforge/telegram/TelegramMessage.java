package com.rofls.botforge.telegram;

public class TelegramMessage {
    private final long messageId;
    private final long chatId;
    private final long userId;
    private final String firstName;
    private final String username;
    private final String text;

    public TelegramMessage(long messageId, long chatId, long userId, String firstName, String username, String text) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.userId = userId;
        this.firstName = firstName;
        this.username = username;
        this.text = text;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getChatId() {
        return chatId;
    }

    public long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text == null ? "" : text;
    }
}
