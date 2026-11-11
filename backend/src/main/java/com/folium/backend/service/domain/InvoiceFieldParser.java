package com.folium.backend.service.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public final class InvoiceFieldParser {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
    );

    private InvoiceFieldParser() {
    }

    public static BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = raw
                .replace('\u00A0', ' ')
                .replace('\u202F', ' ')
                .replaceAll("[^0-9.,-]", "")
                .trim();

        if (!cleaned.matches(".*\\d.*")) {
            return null;
        }

        boolean negative = cleaned.startsWith("-");
        cleaned = cleaned.replace("-", "");

        int lastDot = cleaned.lastIndexOf('.');
        int lastComma = cleaned.lastIndexOf(',');
        int decimalPos = Math.max(lastDot, lastComma);

        String integerPart = cleaned;
        String fraction = "";

        if (decimalPos >= 0) {
            int digitsAfter = cleaned.length() - decimalPos - 1;
            if (digitsAfter == 1 || digitsAfter == 2) {
                integerPart = cleaned.substring(0, decimalPos);
                fraction = cleaned.substring(decimalPos + 1);
            }
        }

        String digits = integerPart.replaceAll("[.,]", "")
                + (fraction.isEmpty() ? "" : "." + fraction.replaceAll("[.,]", ""));

        try {
            BigDecimal value = new BigDecimal(digits);
            return negative ? value.negate() : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = raw.trim();
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(cleaned, format);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
