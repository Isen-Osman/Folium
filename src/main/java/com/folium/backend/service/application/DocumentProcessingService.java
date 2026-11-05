package com.folium.backend.service.application;

import com.folium.backend.model.domain.ProcessingJob;

import java.util.List;

public interface DocumentProcessingService {
    ProcessingJob processDocument(Long documentId);

    List<ProcessingJob> getJobs(Long documentId);
}
