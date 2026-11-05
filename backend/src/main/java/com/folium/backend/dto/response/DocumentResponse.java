package com.folium.backend.dto.response;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentStatus;
import com.folium.backend.model.enums.DocumentType;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String fileName,
        DocumentType type,
        DocumentStatus status,
        Instant uploadedAt
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getType(),
                document.getStatus(),
                document.getUploadedAt()
        );
    }
}
