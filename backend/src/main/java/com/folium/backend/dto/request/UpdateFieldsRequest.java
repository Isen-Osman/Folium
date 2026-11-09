package com.folium.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateFieldsRequest(
        @NotNull @Valid List<Field> fields
) {

    public record Field(
            @NotBlank String name,
            String value
    ) {
    }
}
