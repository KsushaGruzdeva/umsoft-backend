package com.umsoft.backend.repositories;

import com.umsoft.backend.entities.Email;
import com.umsoft.backend.entities.Request;
import java.util.List;

public interface EmailRepository {
    List<Email> findAll();
    Email findById(Long id);
    Email create(Email email);
    Email update(Email email);
    void delete(Email email);
    List<Email> findByRequest(Request request);
    List<Email> findByStatus(String status);
}