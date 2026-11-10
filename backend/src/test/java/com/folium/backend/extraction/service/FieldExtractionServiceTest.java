package com.folium.backend.extraction.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FieldExtractionServiceTest {

    private final FieldExtractionService service = new FieldExtractionService();

    @Test
    void extractsStructuredFieldsFromInvoiceText() {
        String text = """
                ФАКТУРА бр. 2024-17
                Датум: 12.03.2026
                Издавач: Acme DOO
                Email: info@acme.com
                Тел: +389 70 123 456
                IBAN: MK07250120000058984
                Вкупно: 1.250,00
                """;

        Map<String, String> fields = service.extract(text);

        assertThat(fields)
                .containsEntry("Број на фактура", "2024-17")
                .containsEntry("Датум", "12.03.2026")
                .containsEntry("Издавач", "Acme DOO")
                .containsEntry("Email", "info@acme.com")
                .containsEntry("Телефон", "+389 70 123 456")
                .containsEntry("IBAN", "MK07250120000058984")
                .containsEntry("Вкупен износ", "1.250,00");
    }

    @Test
    void extractsFieldsFromEnglishInvoiceWithLabelOnOwnLine() {
        String text = """
                ACME occ INVOICE
                info@acme.com | +389 70 123 456
                No. 2024-17
                Issue date: March 12, 2026
                BILL FROM
                Acme DOO
                Email: info@acme.com
                Phone: +389 70 123 456
                INVOICE DETAILS
                Professional services 1 MKD 1,250.00
                TOTAL DUE
                MKD 1,250.00
                PAYMENT INFORMATION
                Bank account (IBAN) MK07250120000058984
                Thank you for your business.
                ACME DOO »* info@acme.com + +389 70 123 456
                """;

        Map<String, String> fields = service.extract(text);

        assertThat(fields)
                .containsEntry("Email", "info@acme.com")
                .containsEntry("IBAN", "MK07250120000058984")
                .containsEntry("Телефон", "+389 70 123 456")
                .containsEntry("Датум", "March 12, 2026")
                .containsEntry("Издавач", "Acme DOO")
                .containsEntry("Вкупен износ", "1,250.00")
                .containsEntry("Број на фактура", "2024-17");
    }

    @Test
    void returnsEmptyMapForBlankText() {
        assertThat(service.extract("  ")).isEmpty();
        assertThat(service.extract(null)).isEmpty();
    }
}
