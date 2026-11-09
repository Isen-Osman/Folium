package com.folium.backend.extraction.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FieldExtractionService {

    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private static final Pattern PHONE = Pattern.compile(
            "(?<![\\d+])\\+\\d{1,3}[\\d\\s().-]{6,}\\d(?!\\d)");

    private static final Pattern DATE = Pattern.compile(
            "\\b(?:\\d{1,2}[./]\\d{1,2}[./]\\d{2,4}|\\d{4}-\\d{2}-\\d{2})\\b");

    private static final Pattern IBAN = Pattern.compile(
            "\\b[A-Z]{2}\\d{2}[A-Z0-9]{11,30}\\b");

    private static final Pattern AMOUNT = Pattern.compile(
            "\\d{1,3}(?:[.,\\s]\\d{3})*(?:[.,]\\d{2})?");

    private static final List<String> ISSUER_LABELS = List.of(
            "издавач", "добавувач", "продавач", "issuer", "vendor", "seller");

    private static final List<String> TOTAL_LABELS = List.of(
            "вкупен износ", "за плаќање", "вкупно", "grand total", "amount due", "total");

    private static final List<String> INVOICE_LABELS = List.of(
            "фактура бр", "фактура", "invoice no", "invoice #", "invoice");

    public Map<String, String> extract(String text) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return fields;
        }

        putFirst(fields, "Email", EMAIL, text);
        putFirst(fields, "IBAN", IBAN, text);
        putFirst(fields, "Датум", DATE, text);
        putFirst(fields, "Телефон", PHONE, text);

        putLineAfterLabel(fields, "Издавач", ISSUER_LABELS, text);
        putAmountAfterLabel(fields, "Вкупен износ", TOTAL_LABELS, text);
        putLineAfterLabel(fields, "Број на фактура", INVOICE_LABELS, text);

        return fields;
    }

    private void putFirst(Map<String, String> fields, String name, Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            fields.put(name, matcher.group().trim());
        }
    }

    private void putLineAfterLabel(Map<String, String> fields,
                                   String name,
                                   List<String> labels,
                                   String text) {
        for (String line : text.split("\\R")) {
            String lower = line.toLowerCase(Locale.ROOT);
            for (String label : labels) {
                int index = lower.indexOf(label);
                if (index < 0) {
                    continue;
                }
                String value = line.substring(index + label.length())
                        .replaceFirst("^[\\s:#.=,-]+", "")
                        .trim();
                if (!value.isBlank()) {
                    fields.put(name, value);
                    return;
                }
            }
        }
    }

    private void putAmountAfterLabel(Map<String, String> fields,
                                     String name,
                                     List<String> labels,
                                     String text) {
        for (String line : text.split("\\R")) {
            String lower = line.toLowerCase(Locale.ROOT);
            for (String label : labels) {
                int index = lower.indexOf(label);
                if (index < 0) {
                    continue;
                }
                Matcher matcher = AMOUNT.matcher(line.substring(index + label.length()));
                if (matcher.find()) {
                    fields.put(name, matcher.group().trim());
                    return;
                }
            }
        }
    }
}
