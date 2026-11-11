package com.folium.backend.service.application;

import com.folium.backend.dto.response.RegisterRowResponse;

import java.util.List;

public interface RegisterService {
    List<RegisterRowResponse> getRegister(String ownerEmail);
}
