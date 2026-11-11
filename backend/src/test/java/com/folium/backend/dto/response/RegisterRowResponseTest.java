package com.folium.backend.dto.response;

import com.folium.backend.model.domain.Document;
import com.folium.backend.model.enums.DocumentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRowResponseTest {

    @Test
    void mapsCompleteInvoiceWithoutReview() {
        Map<String, String> fields = Map.of(
                "Број на фактура", "2024-17",
                "Датум", "March 12, 2026",
                "Издавач", "Acme",
                "Вкупен износ", "1,250.00"
        );

        RegisterRowResponse row = RegisterRowResponse.from(document(), fields);

        assertThat(row.documentId()).isEqualTo(7L);
        assertThat(row.fileName()).isEqualTo("invoice.pdf");
        assertThat(row.status()).isEqualTo(DocumentStatus.COMPLETED);
        assertThat(row.invoiceNumber()).isEqualTo("2024-17");
        assertThat(row.date()).isEqualTo(LocalDate.of(2026, 3, 12));
        assertThat(row.issuer()).isEqualTo("Acme");
        assertThat(row.amount()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(row.needsReview()).isFalse();
    }

    @Test
    void flagsMissingOrInvalidFieldsForReview() {
        Map<String, String> fields = Map.of(
                "Број на фактура", "2024-17",
                "Датум", "not a date",
                "Издавач", " ",
                "Вкупен износ", "N/A"
        );

        RegisterRowResponse row = RegisterRowResponse.from(document(), fields);

        assertThat(row.invoiceNumber()).isEqualTo("2024-17");
        assertThat(row.date()).isNull();
        assertThat(row.issuer()).isNull();
        assertThat(row.amount()).isNull();
        assertThat(row.needsReview()).isTrue();
    }

    private Document document() {
        Document document = new Document();
        document.setId(7L);
        document.setFileName("invoice.pdf");
        document.setStatus(DocumentStatus.COMPLETED);
        return document;
    }
}
