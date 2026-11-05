package com.folium.backend.extraction.service.impl;

import com.folium.backend.extraction.model.TextExtractionResult;
import com.folium.backend.extraction.service.TextExtractionService;
import com.folium.backend.model.enums.DocumentType;
import com.folium.backend.ocr.enums.OcrLanguage;
import com.folium.backend.ocr.service.OcrService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class DocumentTextExtractionService implements TextExtractionService {

    private static final int OCR_DPI = 300;

    private final OcrService ocrService;

    public DocumentTextExtractionService(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public TextExtractionResult extract(byte[] file, DocumentType type, OcrLanguage language) {

        String text = switch (type) {
            case IMAGE -> ocrService.recognize(file, language).text();
            case PDF -> extractPdf(file, language);
            case DOCX -> extractDocx(file);
        };

        return new TextExtractionResult(text, type);
    }

    private String extractPdf(byte[] file, OcrLanguage language) {
        try (PDDocument document = Loader.loadPDF(file)) {
            String text = new PDFTextStripper().getText(document);

            if (!text.isBlank()) {
                return text;
            }

            return ocrPdf(document, language);
        } catch (IOException e) {
            throw new IllegalStateException("PDF text extraction failed", e);
        }
    }


    private String ocrPdf(PDDocument document, OcrLanguage language) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        StringBuilder text = new StringBuilder();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage image = renderer.renderImageWithDPI(page, OCR_DPI);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);

            text.append(ocrService.recognize(output.toByteArray(), language).text())
                    .append('\n');
        }

        return text.toString();
    }

    private String extractDocx(byte[] file) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(file))) {
            return new XWPFWordExtractor(document).getText();
        } catch (IOException e) {
            throw new IllegalStateException("DOCX text extraction failed", e);
        }
    }
}
