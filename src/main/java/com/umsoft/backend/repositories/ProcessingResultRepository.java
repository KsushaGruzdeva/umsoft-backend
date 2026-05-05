package com.umsoft.backend.repositories;

import com.umsoft.backend.entities.ProcessingResult;
import com.umsoft.backend.entities.Request;
import java.util.List;
import java.util.Optional;

public interface ProcessingResultRepository {
    List<ProcessingResult> findAll();
    ProcessingResult findById(Long id);
    ProcessingResult create(ProcessingResult processingResult);
    ProcessingResult update(ProcessingResult processingResult);
    void delete(ProcessingResult processingResult);
    Optional<ProcessingResult> findByRequest(Request request);
}