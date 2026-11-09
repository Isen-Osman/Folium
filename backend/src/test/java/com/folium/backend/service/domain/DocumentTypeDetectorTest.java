package com.folium.backend.service.domain;

import com.folium.backend.model.enums.DocumentType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentTypeDetectorTest {

    @Test
    void detectsPdfCaseInsensitive() {
        assertEquals(Optional.of(DocumentType.PDF), DocumentTypeDetector.detect("invoice.PDF"));
    }

    @Test
    void detectsDocx() {
        assertEquals(Optional.of(DocumentType.DOCX), DocumentTypeDetector.detect("report.docx"));
    }

    @Test
    void detectsCommonImageExtensions() {
        assertEquals(Optional.of(DocumentType.IMAGE), DocumentTypeDetector.detect("scan.png"));
        assertEquals(Optional.of(DocumentType.IMAGE), DocumentTypeDetector.detect("scan.JPG"));
        assertEquals(Optional.of(DocumentType.IMAGE), DocumentTypeDetector.detect("scan.jpeg"));
        assertEquals(Optional.of(DocumentType.IMAGE), DocumentTypeDetector.detect("scan.tiff"));
    }

    @Test
    void rejectsUnsupportedExtension() {
        assertTrue(DocumentTypeDetector.detect("malware.exe").isEmpty());
        assertTrue(DocumentTypeDetector.detect("notes.txt").isEmpty());
    }

    @Test
    void rejectsMissingOrEmptyExtension() {
        assertTrue(DocumentTypeDetector.detect(null).isEmpty());
        assertTrue(DocumentTypeDetector.detect("noextension").isEmpty());
        assertTrue(DocumentTypeDetector.detect("trailing.").isEmpty());
    }
}
