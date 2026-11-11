package com.folium.backend.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long id,
        String name,
        String surname,
        String email
) {
}
