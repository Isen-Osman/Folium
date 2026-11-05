package com.folium.backend.ocr.service;

import com.folium.backend.ocr.enums.OcrLanguage;
import com.folium.backend.ocr.model.OcrResult;

public interface OcrService {

    OcrResult recognize(byte[] image, OcrLanguage language);
}
