package com.folium.backend.repository;

import com.folium.backend.model.domain.ProcessingJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {

    List<ProcessingJob> findByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);
}
