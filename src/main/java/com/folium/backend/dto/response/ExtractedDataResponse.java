package com.folium.backend.dto.response;

import com.folium.backend.model.domain.ExtractedData;
import com.folium.backend.model.domain.ExtractedField;

import java.time.Instant;
import java.util.List;

public record ExtractedDataResponse(
        Long id,
        Long documentId,
        String rawText,
        Instant createdAt,
        List<Field> fields
) {

    public static ExtractedDataResponse from(ExtractedData data) {
        return new ExtractedDataResponse(
                data.getId(),
                data.getDocument().getId(),
                data.getRawText(),
                data.getCreatedAt(),
                data.getFields().stream().map(Field::from).toList()
        );
    }

    public record Field(Long id, String name, String value) {

        public static Field from(ExtractedField field) {
            return new Field(field.getId(), field.getName(), field.getValue());
        }
    }
}
