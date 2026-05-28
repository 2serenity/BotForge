package com.rofls.botforge.models;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BotTemplateTest {
    @Test
    public void customTemplateRoundTripsThroughJson() throws Exception {
        BotTemplate template = new BotTemplate(
                "custom_1",
                "My Bot",
                "Reusable script",
                "Средний",
                "from botforge import bot",
                false,
                100L,
                200L
        );

        JSONObject json = template.toJson();
        BotTemplate restored = BotTemplate.fromJson(json);

        assertEquals("custom_1", restored.getId());
        assertEquals("My Bot", restored.getName());
        assertEquals("Reusable script", restored.getDescription());
        assertEquals("Средний", restored.getDifficulty());
        assertEquals("from botforge import bot", restored.getDefaultScript());
        assertFalse(restored.isBuiltIn());
        assertEquals(100L, restored.getCreatedAt());
        assertEquals(200L, restored.getUpdatedAt());
    }
}
