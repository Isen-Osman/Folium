package com.folium.backend.web.controllers;

import com.folium.backend.dto.response.ProcessingJobResponse;
import com.folium.backend.service.application.DocumentProcessingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/processing")
public class ProcessingController {

    private final DocumentProcessingService documentProcessingService;

    public ProcessingController(DocumentProcessingService documentProcessingService) {
        this.documentProcessingService = documentProcessingService;
    }

    @PostMapping("/{documentId}")
    public ProcessingJobResponse processDocument(@PathVariable Long documentId) {
        return ProcessingJobResponse.from(
                documentProcessingService.processDocument(documentId));
    }

    @GetMapping("/{documentId}/jobs")
    public List<ProcessingJobResponse> getJobs(@PathVariable Long documentId) {
        return documentProcessingService.getJobs(documentId).stream()
                .map(ProcessingJobResponse::from)
                .toList();
    }
}
