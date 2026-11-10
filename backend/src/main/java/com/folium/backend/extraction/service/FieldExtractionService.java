package com.folium.backend.extraction.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FieldExtractionService {

    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private static final Pattern PHONE = Pattern.compile(
            "(?<![\\d+])\\+\\d{1,3}[\\d\\s().-]{6,}\\d(?!\\d)");

    private static final Pattern DATE = Pattern.compile(
            "\\b(?:\\d{1,2}[./]\\d{1,2}[./]\\d{2,4}"
                    + "|\\d{4}-\\d{2}-\\d{2}"
                    + "|(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?\\s+\\d{1,2},?\\s+\\d{4}"
                    + "|\\d{1,2}\\s+(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?\\s+\\d{4})\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern IBAN = Pattern.compile(
            "\\b[A-Z]{2}\\d{2}[A-Z0-9]{11,30}\\b");

    private static final Pattern AMOUNT = Pattern.compile(
            "\\d{1,3}(?:[.,\\s]\\d{3})*(?:[.,]\\d{2})?");

    private static final List<String> ISSUER_LABELS = List.of(
            "издавач", "добавувач", "продавач", "bill from", "issuer", "vendor", "seller");

    private static final List<String> TOTAL_LABELS = List.of(
            "вкупен износ", "grand total", "amount due", "total due",
            "за плаќање", "вкупно", "total");

    private static final List<String> INVOICE_LABELS = List.of(
            "фактура бр", "фактура број", "број на фактура",
            "invoice number", "invoice no", "invoice #", "no.");

    private static final Set<String> NOT_A_VALUE = Set.of(
            "details", "information", "due", "dues", "from", "bill",
            "to", "date", "no", "invoice", "total");

    public Map<String, String> extract(String text) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return fields;
        }

        putFirst(fields, "Email", EMAIL, text);
        putFirst(fields, "IBAN", IBAN, text);
        putFirst(fields, "Датум", DATE, text);
        putFirst(fields, "Телефон", PHONE, text);

        String[] lines = text.split("\\R");

        putIfPresent(fields, "Издавач", candidateAfterLabel(lines, ISSUER_LABELS));
        putAmount(fields, "Вкупен износ", candidateAfterLabel(lines, TOTAL_LABELS));
        putInvoiceNumber(fields, candidateAfterLabel(lines, INVOICE_LABELS));

        return fields;
    }

    private void putFirst(Map<String, String> fields, String name, Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            fields.put(name, matcher.group().trim());
        }
    }

    private void putIfPresent(Map<String, String> fields, String name, String value) {
        if (value != null && !value.isBlank()) {
            fields.put(name, value);
        }
    }

    private void putAmount(Map<String, String> fields, String name, String candidate) {
        if (candidate == null) {
            return;
        }
        Matcher matcher = AMOUNT.matcher(candidate);
        if (matcher.find()) {
            fields.put(name, matcher.group().trim());
        }
    }

    private void putInvoiceNumber(Map<String, String> fields, String candidate) {
        if (candidate != null && candidate.chars().anyMatch(Character::isDigit)) {
            fields.put("Број на фактура", candidate);
        }
    }

    /**
     * Looks for a label and returns its value. Tries the rest of the label's line
     * first; if that is empty (or just a section word), takes the next non-blank line.
     */
    private String candidateAfterLabel(String[] lines, List<String> labels) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String lower = line.toLowerCase(Locale.ROOT);
            for (String label : labels) {
                int index = lower.indexOf(label);
                if (index < 0) {
                    continue;
                }

                String rest = clean(line.substring(index + label.length()));
                if (isValue(rest)) {
                    return rest;
                }

                String next = nextNonBlank(lines, i + 1);
                if (next != null) {
                    return clean(next);
                }
            }
        }
        return null;
    }

    private String nextNonBlank(String[] lines, int start) {
        for (int i = start; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                return line;
            }
        }
        return null;
    }

    private String clean(String value) {
        return value.replaceFirst("^[\\s:#.=,-]+", "").trim();
    }

    private boolean isValue(String value) {
        return !value.isBlank() && !NOT_A_VALUE.contains(value.toLowerCase(Locale.ROOT));
    }
}
