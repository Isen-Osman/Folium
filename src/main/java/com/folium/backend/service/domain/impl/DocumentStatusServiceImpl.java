package com.folium.backend.service.domain.impl;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentStatus;
import com.folium.backend.repository.DocumentRepository;
import com.folium.backend.service.domain.DocumentStatusService;
import com.folium.backend.service.domain.DocumentValidationService;
import org.springframework.stereotype.Service;

@Service
public class DocumentStatusServiceImpl implements DocumentStatusService {

    private final DocumentRepository documentRepository;
    private final DocumentValidationService documentValidationService;

    public DocumentStatusServiceImpl(DocumentRepository documentRepository,
                                     DocumentValidationService documentValidationService) {
        this.documentRepository = documentRepository;
        this.documentValidationService = documentValidationService;
    }

    @Override
    public void changeStatus(Document document, DocumentStatus status) {
        documentValidationService.validateTransition(document.getStatus(), status);
        document.setStatus(status);
        documentRepository.save(document);
    }
}
