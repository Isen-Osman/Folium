package com.folium.backend.ocr.enums;

public enum OcrLanguage {

    ENGLISH("eng"),
    MACEDONIAN("mkd");

    private final String code;

    OcrLanguage(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
