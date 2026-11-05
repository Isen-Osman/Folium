package com.folium.backend.service.application.impl;

import com.folium.backend.model.domain.ExtractedData;
import com.folium.backend.repository.ExtractedDataRepository;
import com.folium.backend.service.application.ExtractedDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ExtractedDataServiceImpl implements ExtractedDataService {

    private final ExtractedDataRepository extractedDataRepository;

    public ExtractedDataServiceImpl(ExtractedDataRepository extractedDataRepository) {
        this.extractedDataRepository = extractedDataRepository;
    }

    @Override
    public ExtractedData getByDocumentId(Long documentId) {
        return extractedDataRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new NoSuchElementException("Extracted data not found for document: " + documentId));
    }

    @Override
    @Transactional
    public ExtractedData save(ExtractedData extractedData) {
        return extractedDataRepository.save(extractedData);
    }
}
