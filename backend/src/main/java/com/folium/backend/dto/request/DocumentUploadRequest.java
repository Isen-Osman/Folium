package com.folium.backend.dto.request;

import com.folium.backend.model.enums.DocumentType;

public record DocumentUploadRequest(String fileName, DocumentType type) {
}
