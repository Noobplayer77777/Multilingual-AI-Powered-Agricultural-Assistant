package com.agri.query.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LanguageDetectionServiceTest {

    private final LanguageDetectionService service = new LanguageDetectionService();

    @Test
    public void testDetectLanguage_English() {
        assertEquals("en", service.detectLanguage("What crop should I grow in monsoon?"));
    }

    @Test
    public void testDetectLanguage_Tamil() {
        assertEquals("ta", service.detectLanguage("நான் என்ன பயிர் வளர்க்க வேண்டும்?"));
    }

    @Test
    public void testDetectLanguage_Hindi() {
        assertEquals("hi", service.detectLanguage("मुझे कौन सी फसल उगानी चाहिए?"));
    }

    @Test
    public void testDetectLanguage_EmptyOrNull() {
        assertEquals("en", service.detectLanguage(""));
        assertEquals("en", service.detectLanguage(null));
    }
}
