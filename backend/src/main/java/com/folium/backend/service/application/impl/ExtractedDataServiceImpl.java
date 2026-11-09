package com.folium.backend.service.application.impl;

import com.folium.backend.dto.request.UpdateFieldsRequest;
import com.folium.backend.model.domain.ExtractedData;
import com.folium.backend.model.domain.ExtractedField;
import com.folium.backend.repository.ExtractedDataRepository;
import com.folium.backend.service.application.ExtractedDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    @Transactional
    public ExtractedData replaceFields(Long documentId, List<UpdateFieldsRequest.Field> fields) {
        ExtractedData extractedData = getByDocumentId(documentId);

        extractedData.getFields().clear();
        for (UpdateFieldsRequest.Field input : fields) {
            ExtractedField field = new ExtractedField();
            field.setExtractedData(extractedData);
            field.setName(input.name());
            field.setValue(input.value());
            extractedData.getFields().add(field);
        }

        return extractedDataRepository.save(extractedData);
    }
}
