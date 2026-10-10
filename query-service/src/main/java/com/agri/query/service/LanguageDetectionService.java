package com.agri.query.service;

import org.springframework.stereotype.Service;

@Service
public class LanguageDetectionService {

    /**
     * A very simple heuristic to detect language based on unicode blocks.
     * If the text contains Tamil unicode characters, assume Tamil.
     * If it contains Devanagari (Hindi), assume Hindi.
     * Otherwise, default to English.
     */
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }

        boolean hasTamil = false;
        boolean hasDevanagari = false;

        for (char c : text.toCharArray()) {
            if (c >= '\u0B80' && c <= '\u0BFF') {
                hasTamil = true;
                break;
            } else if (c >= '\u0900' && c <= '\u097F') {
                hasDevanagari = true;
                break;
            }
        }

        if (hasTamil) {
            return "ta";
        } else if (hasDevanagari) {
            return "hi";
        }

        return "en";
    }
}
