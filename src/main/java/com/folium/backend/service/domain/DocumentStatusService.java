package com.folium.backend.service.domain;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentStatus;

public interface DocumentStatusService {
    void changeStatus(Document document, DocumentStatus status);
}
