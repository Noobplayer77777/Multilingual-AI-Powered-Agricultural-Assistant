package com.agri.query.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageDetectionServiceTest {

    private final LanguageDetectionService service = new LanguageDetectionService();

    @Test
    void testDetectEnglish() {
        assertEquals("en", service.detectLanguage("What crop should I grow in summer?"));
        assertEquals("en", service.detectLanguage("Tomato pest control"));
    }

    @Test
    void testDetectTamil() {
        assertEquals("ta", service.detectLanguage("நான் என்ன பயிர் வளர்க்க வேண்டும்?"));
        assertEquals("ta", service.detectLanguage("தக்காளி இலை கருகல் நோய்"));
    }

    @Test
    void testDetectHindi() {
        assertEquals("hi", service.detectLanguage("मुझे कौन सी फसल उगानी चाहिए?"));
        assertEquals("hi", service.detectLanguage("टमाटर की वैज्ञानिक खेती"));
    }

    @Test
    void testDetectEmptyOrNull() {
        assertEquals("en", service.detectLanguage(null));
        assertEquals("en", service.detectLanguage("   "));
    }
}
