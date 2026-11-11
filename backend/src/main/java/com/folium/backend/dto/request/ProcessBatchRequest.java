package com.folium.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProcessBatchRequest(
        @NotEmpty List<Long> documentIds
) {
}
