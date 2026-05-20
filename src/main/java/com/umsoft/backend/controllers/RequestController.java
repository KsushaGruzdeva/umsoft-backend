package com.umsoft.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umsoft.backend.dtos.RequestDto;
import com.umsoft.backend.dtos.ResponseDto;
import com.umsoft.backend.services.RequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping
    public ResponseEntity<ResponseDto> createRequest(@Valid @RequestBody RequestDto requestDto) {
        System.out.println("Получен POST запрос на создание заявки от: " + requestDto.getEmail());

        try {
            ResponseDto response = requestService.processRequest(requestDto);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке заявки: " + e.getMessage());
            e.printStackTrace();

            ResponseDto errorResponse = new ResponseDto(
                    false,
                    "Произошла ошибка при обработке заявки: " + e.getMessage(),
                    null,
                    "ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}