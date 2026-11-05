package com.folium.backend.service.domain.impl;

import com.folium.backend.model.enums.DocumentStatus;
import com.folium.backend.service.domain.DocumentValidationService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class DocumentValidationServiceImpl implements DocumentValidationService {

    private static final Map<DocumentStatus, Set<DocumentStatus>> ALLOWED_TRANSITIONS = Map.of(
            DocumentStatus.UPLOADED, Set.of(DocumentStatus.PENDING, DocumentStatus.PROCESSING, DocumentStatus.FAILED),
            DocumentStatus.PENDING, Set.of(DocumentStatus.PROCESSING, DocumentStatus.FAILED),
            DocumentStatus.PROCESSING, Set.of(DocumentStatus.COMPLETED, DocumentStatus.FAILED),
            DocumentStatus.COMPLETED, Set.of(),
            DocumentStatus.FAILED, Set.of(DocumentStatus.PENDING)
    );

    @Override
    public void validateTransition(DocumentStatus current, DocumentStatus next) {
        if (current == next) {
            return;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) {
            throw new IllegalStateException(
                    "Invalid document status transition: " + current + " -> " + next);
        }
    }
}
