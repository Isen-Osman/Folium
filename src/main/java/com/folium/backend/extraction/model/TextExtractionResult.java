package com.folium.backend.extraction.model;

import com.folium.backend.model.enums.DocumentType;

public record TextExtractionResult(String text, DocumentType type) {
}
