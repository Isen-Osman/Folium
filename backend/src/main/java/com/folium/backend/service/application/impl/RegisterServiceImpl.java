package com.folium.backend.service.application.impl;

import com.folium.backend.dto.response.RegisterRowResponse;
import com.folium.backend.model.domain.ExtractedField;
import com.folium.backend.repository.DocumentRepository;
import com.folium.backend.repository.ExtractedDataRepository;
import com.folium.backend.service.application.RegisterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RegisterServiceImpl implements RegisterService {

    private final DocumentRepository documentRepository;
    private final ExtractedDataRepository extractedDataRepository;

    public RegisterServiceImpl(DocumentRepository documentRepository,
                               ExtractedDataRepository extractedDataRepository) {
        this.documentRepository = documentRepository;
        this.extractedDataRepository = extractedDataRepository;
    }

    @Override
    public List<RegisterRowResponse> getRegister(String ownerEmail) {
        return documentRepository.findAllByOwner_Email(ownerEmail).stream()
                .map(document -> RegisterRowResponse.from(document, fieldsOf(document.getId())))
                .toList();
    }

    private Map<String, String> fieldsOf(Long documentId) {
        return extractedDataRepository.findByDocumentId(documentId)
                .map(data -> {
                    Map<String, String> fields = new LinkedHashMap<>();
                    for (ExtractedField field : data.getFields()) {
                        if (field.getName() != null && field.getValue() != null) {
                            fields.put(field.getName(), field.getValue());
                        }
                    }
                    return fields;
                })
                .orElseGet(Map::of);
    }
}
