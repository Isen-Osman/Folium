package com.folium.backend.service.application;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record DocumentFile(Resource resource, MediaType contentType, String fileName) {
}
