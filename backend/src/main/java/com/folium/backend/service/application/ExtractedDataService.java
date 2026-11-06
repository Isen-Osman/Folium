package com.folium.backend.service.application;

import com.folium.backend.model.domain.ExtractedData;

public interface ExtractedDataService {
    ExtractedData getByDocumentId(Long documentId);

    ExtractedData save(ExtractedData extractedData);
}
