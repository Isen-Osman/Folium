package com.folium.backend.service.application;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentType;

import java.util.List;

public interface DocumentService {
    List<Document> getDocuments();

    Document getDocument(Long id);

    Document createDocument(String fileName, DocumentType type);

    void deleteDocument(Long id);
}
