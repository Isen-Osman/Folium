package com.folium.backend.repository;

import com.folium.backend.model.domain.ExtractedData;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractedDataRepository extends JpaRepository<ExtractedData, Long> {

    Optional<ExtractedData> findByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);
}
