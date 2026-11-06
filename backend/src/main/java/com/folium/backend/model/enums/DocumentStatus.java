package com.folium.backend.model.enums;

import jakarta.persistence.Enumerated;

public enum DocumentStatus {
    UPLOADED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
