package com.folium.backend.dto.response;

import com.folium.backend.model.domain.ProcessingJob;
import com.folium.backend.model.enums.DocumentStatus;

import java.time.Instant;

public record ProcessingJobResponse(
        Long id,
        Long documentId,
        DocumentStatus status,
        Instant startedAt,
        Instant finishedAt,
        String errorMessage
) {

    public static ProcessingJobResponse from(ProcessingJob job) {
        return new ProcessingJobResponse(
                job.getId(),
                job.getDocument().getId(),
                job.getStatus(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getErrorMessage()
        );
    }
}
