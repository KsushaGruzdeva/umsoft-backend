package com.umsoft.backend.services;

import com.umsoft.backend.entities.Request;

public interface EmailService {
    void sendRequestEmail(Request request, String category, String summary, String assignedEmail);
    void sendConfirmationToClient(Request request);
}