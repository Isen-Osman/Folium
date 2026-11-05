package com.folium.backend.service.domain;

import com.folium.backend.model.enums.DocumentStatus;

public interface DocumentValidationService {
    void validateTransition(DocumentStatus current, DocumentStatus next);
}
