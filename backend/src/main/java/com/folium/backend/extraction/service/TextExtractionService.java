package com.folium.backend.extraction.service;

import com.folium.backend.extraction.model.TextExtractionResult;
import com.folium.backend.model.enums.DocumentType;
import com.folium.backend.ocr.enums.OcrLanguage;

public interface TextExtractionService {

    TextExtractionResult extract(byte[] file, DocumentType type, OcrLanguage language);
}
