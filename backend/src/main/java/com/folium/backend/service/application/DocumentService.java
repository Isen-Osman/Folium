package com.folium.backend.service.application;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    List<Document> getDocuments(String ownerEmail);

    Document getDocument(Long id, String ownerEmail);

    Document createDocument(MultipartFile file, DocumentType type, String ownerEmail);

    Document renameDocument(Long id, String fileName, String ownerEmail);

    void deleteDocument(Long id, String ownerEmail);
}
