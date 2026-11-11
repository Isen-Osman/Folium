package com.folium.backend.dto.response;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentStatus;
import com.folium.backend.service.domain.InvoiceFieldParser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record RegisterRowResponse(
        Long documentId,
        String fileName,
        DocumentStatus status,
        String invoiceNumber,
        LocalDate date,
        String issuer,
        BigDecimal amount,
        boolean needsReview
) {

    private static final String INVOICE_NUMBER = "Број на фактура";
    private static final String DATE = "Датум";
    private static final String ISSUER = "Издавач";
    private static final String AMOUNT = "Вкупен износ";

    public static RegisterRowResponse from(Document document, Map<String, String> fields) {
        String invoiceNumber = trimToNull(fields.get(INVOICE_NUMBER));
        String issuer = trimToNull(fields.get(ISSUER));
        LocalDate date = InvoiceFieldParser.parseDate(fields.get(DATE));
        BigDecimal amount = InvoiceFieldParser.parseAmount(fields.get(AMOUNT));

        boolean needsReview = invoiceNumber == null
                || issuer == null
                || date == null
                || amount == null;

        return new RegisterRowResponse(
                document.getId(),
                document.getFileName(),
                document.getStatus(),
                invoiceNumber,
                date,
                issuer,
                amount,
                needsReview
        );
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
