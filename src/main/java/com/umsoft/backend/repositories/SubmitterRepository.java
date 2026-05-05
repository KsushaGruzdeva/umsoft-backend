package com.umsoft.backend.repositories;

import com.umsoft.backend.entities.Submitter;
import java.util.List;
import java.util.Optional;

public interface SubmitterRepository {
    List<Submitter> findAll();
    Submitter findById(Long id);
    Submitter create(Submitter submitter);
    Submitter update(Submitter submitter);
    void delete(Submitter submitter);
    Optional<Submitter> findByEmail(String email);
    boolean existsByEmail(String email);
}