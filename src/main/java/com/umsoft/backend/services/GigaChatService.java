package com.umsoft.backend.services;

import com.umsoft.backend.dtos.ClassificationResult;

public interface GigaChatService {
    ClassificationResult classifyAndSummarize(String text);
}