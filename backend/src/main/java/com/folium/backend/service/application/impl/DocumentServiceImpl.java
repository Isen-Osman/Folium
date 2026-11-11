package com.folium.backend.service.application.impl;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.domain.User;
import com.folium.backend.model.enums.DocumentType;
import com.folium.backend.repository.DocumentRepository;
import com.folium.backend.repository.ExtractedDataRepository;
import com.folium.backend.repository.ProcessingJobRepository;
import com.folium.backend.repository.UserRepository;
import com.folium.backend.service.application.DocumentFile;
import com.folium.backend.service.application.DocumentService;
import com.folium.backend.service.domain.DocumentTypeDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ExtractedDataRepository extractedDataRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final UserRepository userRepository;
    private final Path uploadDir;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               ExtractedDataRepository extractedDataRepository,
                               ProcessingJobRepository processingJobRepository,
                               UserRepository userRepository,
                               @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.extractedDataRepository = extractedDataRepository;
        this.processingJobRepository = processingJobRepository;
        this.userRepository = userRepository;
        this.uploadDir = Path.of(uploadDir);
    }

    @Override
    public List<Document> getDocuments(String ownerEmail) {
        return documentRepository.findAllByOwner_Email(ownerEmail);
    }

    @Override
    public Document getDocument(Long id, String ownerEmail) {
        return documentRepository.findByIdAndOwner_Email(id, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    @Override
    public DocumentFile getFile(Long id, String ownerEmail) {
        Document document = getDocument(id, ownerEmail);
        String filePath = document.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        return new DocumentFile(
                new FileSystemResource(path),
                contentType(document),
                document.getFileName());
    }

    private MediaType contentType(Document document) {
        if (document.getType() == DocumentType.PDF) {
            return MediaType.APPLICATION_PDF;
        }
        if (document.getType() == DocumentType.DOCX) {
            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        return MediaTypeFactory.getMediaType(document.getFileName())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    @Transactional
    public Document createDocument(MultipartFile file, String ownerEmail) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }

        DocumentType type = DocumentTypeDetector.detect(fileName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported file type. Allowed: PDF, DOCX, PNG, JPG, JPEG, GIF, BMP, TIFF, WEBP"));

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {

            Files.createDirectories(uploadDir);

            String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = uploadDir.resolve(UUID.randomUUID() + "-" + safeName);

            // 3) Zacuvaj gi bajtovite od upload-ot na disk.
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // 4) Zapisi go dokumentot vo bazata, vklucuvajki go patot do fajlot.
            Document document = new Document();
            document.setOwner(owner);
            document.setFileName(fileName);
            document.setType(type);
            document.setFilePath(target.toString());
            return documentRepository.save(document);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    @Override
    @Transactional
    public Document renameDocument(Long id, String fileName, String ownerEmail) {
        Document document = getDocument(id, ownerEmail);
        document.setFileName(fileName);
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, String ownerEmail) {
        Document document = getDocument(id, ownerEmail);

        processingJobRepository.deleteByDocumentId(id);
        extractedDataRepository.deleteByDocumentId(id);
        documentRepository.delete(document);
        documentRepository.flush();

        deleteStoredFile(document.getFilePath());
    }

    // Best-effort brisenje na fajlot; bazata e vazna, fajlot e sekundaren.
    private void deleteStoredFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException ignored) {
            // Ne go prekinuvame brisenjeto na dokumentot поради fajl.
        }
    }
}
