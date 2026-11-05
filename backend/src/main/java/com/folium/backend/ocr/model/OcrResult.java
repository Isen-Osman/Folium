package com.folium.backend.ocr.model;

import com.folium.backend.ocr.enums.OcrLanguage;

public record OcrResult(String text, OcrLanguage language) {
}
