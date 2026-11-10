package com.folium.backend.service.application;

import com.folium.backend.model.domain.ProcessingJob;

import java.util.List;

public interface DocumentProcessingService {
    ProcessingJob processDocument(Long documentId, String ownerEmail);

    void markFailed(Long documentId, String ownerEmail, String reason);

    List<ProcessingJob> getJobs(Long documentId, String ownerEmail);
}
