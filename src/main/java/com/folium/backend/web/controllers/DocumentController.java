package com.folium.backend.web.controllers;

import com.folium.backend.dto.request.DocumentUploadRequest;
import com.folium.backend.dto.request.RenameDocumentRequest;
import com.folium.backend.dto.response.DocumentResponse;
import com.folium.backend.dto.response.ExtractedDataResponse;
import com.folium.backend.service.application.DocumentService;
import com.folium.backend.service.application.ExtractedDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ExtractedDataService extractedDataService;

    public DocumentController(DocumentService documentService,
                              ExtractedDataService extractedDataService) {
        this.documentService = documentService;
        this.extractedDataService = extractedDataService;
    }

    @GetMapping
    public List<DocumentResponse> getDocuments() {
        return documentService.getDocuments().stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocument(@PathVariable Long id) {
        return DocumentResponse.from(documentService.getDocument(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(@RequestBody DocumentUploadRequest request) {
        return DocumentResponse.from(
                documentService.createDocument(request.fileName(), request.type()));
    }

    @PatchMapping("/{id}")
    public DocumentResponse renameDocument(@PathVariable Long id,
                                           @RequestBody RenameDocumentRequest request) {
        return DocumentResponse.from(
                documentService.renameDocument(id, request.fileName()));
    }

    @GetMapping("/{id}/extracted-data")
    public ExtractedDataResponse getExtractedData(@PathVariable Long id) {
        return ExtractedDataResponse.from(extractedDataService.getByDocumentId(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }
}
