package com.folium.backend.service.domain;

import com.folium.backend.model.enums.DocumentType;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class DocumentTypeDetector {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "tif", "tiff", "webp");

    private DocumentTypeDetector() {
    }

    public static Optional<DocumentType> detect(String fileName) {
        if (fileName == null) {
            return Optional.empty();
        }

        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return Optional.empty();
        }

        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);

        return switch (extension) {
            case "pdf" -> Optional.of(DocumentType.PDF);
            case "docx" -> Optional.of(DocumentType.DOCX);
            default -> IMAGE_EXTENSIONS.contains(extension)
                    ? Optional.of(DocumentType.IMAGE)
                    : Optional.empty();
        };
    }
}
