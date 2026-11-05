package com.folium.backend.service.application;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    List<Document> getDocuments();

    Document getDocument(Long id);

    Document createDocument(MultipartFile file, DocumentType type);

    Document renameDocument(Long id, String fileName);

    void deleteDocument(Long id);
}
