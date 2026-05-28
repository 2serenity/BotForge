package com.rofls.botforge.telegram;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TelegramApiClientTest {
    @Test
    public void splitMessageTextKeepsShortTextAsSingleChunk() {
        List<String> chunks = TelegramApiClient.splitMessageText("hello");

        assertEquals(1, chunks.size());
        assertEquals("hello", chunks.get(0));
    }

    @Test
    public void splitMessageTextSplitsLongTextByTelegramLimit() {
        String text = repeat("a", TelegramApiClient.MAX_MESSAGE_LENGTH + 10);

        List<String> chunks = TelegramApiClient.splitMessageText(text);

        assertEquals(2, chunks.size());
        assertEquals(TelegramApiClient.MAX_MESSAGE_LENGTH, chunks.get(0).length());
        assertEquals(10, chunks.get(1).length());
    }

    @Test
    public void splitMessageTextDoesNotBreakSurrogatePairAtBoundary() {
        String text = repeat("a", TelegramApiClient.MAX_MESSAGE_LENGTH - 1) + "\uD83D\uDE80tail";

        List<String> chunks = TelegramApiClient.splitMessageText(text);

        assertEquals(2, chunks.size());
        assertFalse(Character.isHighSurrogate(chunks.get(0).charAt(chunks.get(0).length() - 1)));
        assertTrue(chunks.get(1).startsWith("\uD83D\uDE80"));
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
