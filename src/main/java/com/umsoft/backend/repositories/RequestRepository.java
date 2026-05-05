package com.umsoft.backend.repositories;

import com.umsoft.backend.entities.Request;
import com.umsoft.backend.entities.Submitter;
import java.util.List;
import java.util.Optional;

public interface RequestRepository {
    List<Request> findAll();
    Request findById(Long id);
    Request create(Request request);
    Request update(Request request);
    void delete(Request request);
    List<Request> findBySubmitter(Submitter submitter);
    List<Request> findByStatus(String status);
    Optional<Request> findByIdWithDetails(Long id);
}