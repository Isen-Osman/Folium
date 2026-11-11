package com.folium.backend.service.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceFieldParserTest {

    @Test
    void parsesAmountsWithDifferentFormats() {
        assertThat(InvoiceFieldParser.parseAmount("1,250.00")).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(InvoiceFieldParser.parseAmount("1.250,00")).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(InvoiceFieldParser.parseAmount("1 250,00")).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(InvoiceFieldParser.parseAmount("MKD 1,250.00")).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(InvoiceFieldParser.parseAmount("1250")).isEqualByComparingTo(new BigDecimal("1250"));
        assertThat(InvoiceFieldParser.parseAmount("1.250")).isEqualByComparingTo(new BigDecimal("1250"));
        assertThat(InvoiceFieldParser.parseAmount("12.5")).isEqualByComparingTo(new BigDecimal("12.5"));
        assertThat(InvoiceFieldParser.parseAmount("-500,00")).isEqualByComparingTo(new BigDecimal("-500.00"));
    }

    @Test
    void returnsNullForMissingOrInvalidAmounts() {
        assertThat(InvoiceFieldParser.parseAmount(null)).isNull();
        assertThat(InvoiceFieldParser.parseAmount("   ")).isNull();
        assertThat(InvoiceFieldParser.parseAmount("N/A")).isNull();
    }

    @Test
    void parsesDatesWithDifferentFormats() {
        assertThat(InvoiceFieldParser.parseDate("2026-03-12")).isEqualTo(LocalDate.of(2026, 3, 12));
        assertThat(InvoiceFieldParser.parseDate("12.03.2026")).isEqualTo(LocalDate.of(2026, 3, 12));
        assertThat(InvoiceFieldParser.parseDate("12/03/2026")).isEqualTo(LocalDate.of(2026, 3, 12));
        assertThat(InvoiceFieldParser.parseDate("March 12, 2026")).isEqualTo(LocalDate.of(2026, 3, 12));
        assertThat(InvoiceFieldParser.parseDate("Mar 12, 2026")).isEqualTo(LocalDate.of(2026, 3, 12));
        assertThat(InvoiceFieldParser.parseDate("12 Mar 2026")).isEqualTo(LocalDate.of(2026, 3, 12));
    }

    @Test
    void returnsNullForMissingOrInvalidDates() {
        assertThat(InvoiceFieldParser.parseDate(null)).isNull();
        assertThat(InvoiceFieldParser.parseDate("   ")).isNull();
        assertThat(InvoiceFieldParser.parseDate("not a date")).isNull();
    }
}
