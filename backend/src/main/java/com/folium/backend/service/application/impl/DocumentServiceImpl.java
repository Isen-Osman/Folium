package com.folium.backend.service.application.impl;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentType;
import com.folium.backend.repository.DocumentRepository;
import com.folium.backend.repository.ExtractedDataRepository;
import com.folium.backend.repository.ProcessingJobRepository;
import com.folium.backend.service.application.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ExtractedDataRepository extractedDataRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final Path uploadDir;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               ExtractedDataRepository extractedDataRepository,
                               ProcessingJobRepository processingJobRepository,
                               @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.extractedDataRepository = extractedDataRepository;
        this.processingJobRepository = processingJobRepository;
        this.uploadDir = Path.of(uploadDir);
    }

    @Override
    public List<Document> getDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
    }

    @Override
    @Transactional
    public Document createDocument(MultipartFile file, DocumentType type) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }

        try {

            Files.createDirectories(uploadDir);

            // Bezbedno ime: bez patanja (../) i bez cudni znaci.
            // UUID sprecuva dvajca fajlovi so isto ime da se prepisat.
            String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = uploadDir.resolve(UUID.randomUUID() + "-" + safeName);

            // 3) Zacuvaj gi bajtovite od upload-ot na disk.
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // 4) Zapisi go dokumentot vo bazata, vklucuvajki go patot do fajlot.
            Document document = new Document();
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
    public Document renameDocument(Long id, String fileName) {
        Document document = getDocument(id);
        document.setFileName(fileName);
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        Document document = getDocument(id);

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
