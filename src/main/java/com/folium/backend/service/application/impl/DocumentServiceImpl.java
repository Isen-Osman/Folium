package com.folium.backend.service.application.impl;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentType;
import com.folium.backend.repository.DocumentRepository;
import com.folium.backend.service.application.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public List<Document> getDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
    }

    @Override
    @Transactional
    public Document createDocument(String fileName, DocumentType type) {
        Document document = new Document();
        document.setFileName(fileName);
        document.setType(type);
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        documentRepository.delete(getDocument(id));
    }
}
