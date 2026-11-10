package com.folium.backend.web.controllers;

import com.folium.backend.dto.request.ProcessBatchRequest;
import com.folium.backend.dto.response.ProcessingJobResponse;
import com.folium.backend.service.application.DocumentProcessingService;
import com.folium.backend.service.application.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/processing")
public class ProcessingController {

    private final DocumentProcessingService documentProcessingService;
    private final DocumentService documentService;
    private final Executor processingExecutor;

    public ProcessingController(DocumentProcessingService documentProcessingService,
                                DocumentService documentService,
                                @Qualifier("processingExecutor") Executor processingExecutor) {
        this.documentProcessingService = documentProcessingService;
        this.documentService = documentService;
        this.processingExecutor = processingExecutor;
    }

    @PostMapping("/{documentId}")
    public ProcessingJobResponse processDocument(@PathVariable Long documentId,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return ProcessingJobResponse.from(
                documentProcessingService.processDocument(documentId, jwt.getSubject()));
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Integer> processBatch(@Valid @RequestBody ProcessBatchRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        String ownerEmail = jwt.getSubject();
        List<Long> ids = request.documentIds().stream().distinct().toList();

        ids.forEach(id -> documentService.getDocument(id, ownerEmail));

        ids.forEach(id -> processingExecutor.execute(() -> {
            try {
                documentProcessingService.processDocument(id, ownerEmail);
            } catch (Exception e) {
                String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                documentProcessingService.markFailed(id, ownerEmail, reason);
            }
        }));

        return Map.of("queued", ids.size());
    }

    @GetMapping("/{documentId}/jobs")
    public List<ProcessingJobResponse> getJobs(@PathVariable Long documentId,
                                              @AuthenticationPrincipal Jwt jwt) {
        return documentProcessingService.getJobs(documentId, jwt.getSubject()).stream()
                .map(ProcessingJobResponse::from)
                .toList();
    }
}
