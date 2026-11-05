package com.folium.backend.service.application.impl;

import com.folium.backend.extraction.model.TextExtractionResult;
import com.folium.backend.extraction.service.TextExtractionService;
import com.folium.backend.model.domain.Document;
import com.folium.backend.model.domain.ExtractedData;
import com.folium.backend.model.domain.ProcessingJob;
import com.folium.backend.model.enums.DocumentStatus;
import com.folium.backend.ocr.enums.OcrLanguage;
import com.folium.backend.repository.ProcessingJobRepository;
import com.folium.backend.service.application.DocumentProcessingService;
import com.folium.backend.service.application.DocumentService;
import com.folium.backend.service.application.ExtractedDataService;
import com.folium.backend.service.domain.DocumentStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DocumentProcessingServiceImpl implements DocumentProcessingService {

    private final DocumentService documentService;
    private final DocumentStatusService documentStatusService;
    private final ProcessingJobRepository processingJobRepository;
    private final TextExtractionService textExtractionService;
    private final ExtractedDataService extractedDataService;

    public DocumentProcessingServiceImpl(DocumentService documentService,
                                         DocumentStatusService documentStatusService,
                                         ProcessingJobRepository processingJobRepository,
                                         TextExtractionService textExtractionService,
                                         ExtractedDataService extractedDataService) {
        this.documentService = documentService;
        this.documentStatusService = documentStatusService;
        this.processingJobRepository = processingJobRepository;
        this.textExtractionService = textExtractionService;
        this.extractedDataService = extractedDataService;
    }

    @Override
    @Transactional
    public ProcessingJob processDocument(Long documentId) {
        Document document = documentService.getDocument(documentId);
        documentStatusService.changeStatus(document, DocumentStatus.PROCESSING);

        ProcessingJob job = new ProcessingJob();
        job.setDocument(document);
        job.setStatus(DocumentStatus.PROCESSING);
        job.setStartedAt(Instant.now());
        job = processingJobRepository.save(job);

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of(document.getFilePath()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read document file", e);
        }

        TextExtractionResult result = textExtractionService.extract(
                bytes, document.getType(), OcrLanguage.ENGLISH);

        ExtractedData extractedData = new ExtractedData();
        extractedData.setDocument(document);
        extractedData.setRawText(result.text());
        extractedDataService.save(extractedData);

        documentStatusService.changeStatus(document, DocumentStatus.COMPLETED);
        job.setStatus(DocumentStatus.COMPLETED);
        job.setFinishedAt(Instant.now());

        return processingJobRepository.save(job);
    }

    @Override
    public List<ProcessingJob> getJobs(Long documentId) {
        return processingJobRepository.findByDocumentId(documentId);
    }
}
