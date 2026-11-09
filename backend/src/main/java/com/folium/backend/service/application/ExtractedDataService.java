package com.folium.backend.service.application;

import com.folium.backend.dto.request.UpdateFieldsRequest;
import com.folium.backend.model.domain.ExtractedData;

import java.util.List;

public interface ExtractedDataService {
    ExtractedData getByDocumentId(Long documentId);

    ExtractedData save(ExtractedData extractedData);

    ExtractedData replaceFields(Long documentId, List<UpdateFieldsRequest.Field> fields);
}
