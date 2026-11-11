package com.folium.backend.web.controllers;

import com.folium.backend.dto.request.RenameDocumentRequest;
import com.folium.backend.dto.request.UpdateFieldsRequest;
import com.folium.backend.dto.response.DocumentResponse;
import com.folium.backend.dto.response.ExtractedDataResponse;
import com.folium.backend.dto.response.RegisterRowResponse;
import com.folium.backend.service.application.DocumentFile;
import com.folium.backend.service.application.DocumentService;
import com.folium.backend.service.application.ExtractedDataService;
import com.folium.backend.service.application.RegisterService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ExtractedDataService extractedDataService;
    private final RegisterService registerService;

    public DocumentController(DocumentService documentService,
                              ExtractedDataService extractedDataService,
                              RegisterService registerService) {
        this.documentService = documentService;
        this.extractedDataService = extractedDataService;
        this.registerService = registerService;
    }

    @GetMapping
    public List<DocumentResponse> getDocuments(@AuthenticationPrincipal Jwt jwt) {
        return documentService.getDocuments(jwt.getSubject()).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @GetMapping("/register")
    public List<RegisterRowResponse> getRegister(@AuthenticationPrincipal Jwt jwt) {
        return registerService.getRegister(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocument(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return DocumentResponse.from(documentService.getDocument(id, jwt.getSubject()));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        DocumentFile file = documentService.getFile(id, jwt.getSubject());
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(file.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(file.resource());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(@RequestParam("file") MultipartFile file,
                                           @AuthenticationPrincipal Jwt jwt) {
        return DocumentResponse.from(documentService.createDocument(file, jwt.getSubject()));
    }

    @PatchMapping("/{id}")
    public DocumentResponse renameDocument(@PathVariable Long id,
                                           @RequestBody RenameDocumentRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return DocumentResponse.from(
                documentService.renameDocument(id, request.fileName(), jwt.getSubject()));
    }

    @GetMapping("/{id}/extracted-data")
    public ExtractedDataResponse getExtractedData(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        documentService.getDocument(id, jwt.getSubject());
        return ExtractedDataResponse.from(extractedDataService.getByDocumentId(id));
    }

    @PutMapping("/{id}/extracted-data/fields")
    public ExtractedDataResponse updateFields(@PathVariable Long id,
                                              @Valid @RequestBody UpdateFieldsRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        documentService.getDocument(id, jwt.getSubject());
        return ExtractedDataResponse.from(extractedDataService.replaceFields(id, request.fields()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        documentService.deleteDocument(id, jwt.getSubject());
    }
}
