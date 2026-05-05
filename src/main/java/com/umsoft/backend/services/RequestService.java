package com.umsoft.backend.services;

import com.umsoft.backend.dtos.RequestDto;
import com.umsoft.backend.dtos.ResponseDto;
import com.umsoft.backend.entities.Request;

public interface RequestService {
    ResponseDto processRequest(RequestDto requestDto);
    Request getRequestStatus(Long id);
}