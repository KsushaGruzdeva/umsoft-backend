package com.umsoft.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.umsoft.backend.dtos.RequestDto;
import com.umsoft.backend.dtos.ResponseDto;
import com.umsoft.backend.entities.Request;
import com.umsoft.backend.services.RequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:3000")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping
    public ResponseEntity<ResponseDto> createRequest(@Valid @RequestBody RequestDto requestDto) {
        System.out.println("📥 Получен POST запрос на создание заявки от: " + requestDto.getEmail());

        try {
            ResponseDto response = requestService.processRequest(requestDto);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при обработке заявки: " + e.getMessage());
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

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getRequestStatus(@PathVariable Long id) {
        System.out.println("📥 Получен GET запрос на получение статуса заявки: " + id);

        try {
            Request request = requestService.getRequestStatus(id);

            if (request == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseDto(false, "Заявка не найдена", null, null)
                );
            }

            String message = getStatusMessage(request.getStatus());

            return ResponseEntity.ok(
                    new ResponseDto(true, message, request.getId(), request.getStatus())
            );
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении статуса заявки: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseDto(false, "Ошибка при получении статуса: " + e.getMessage(), null, null)
            );
        }
    }

    private String getStatusMessage(String status) {
        if (status == null) return "Статус неизвестен";

        switch (status) {
            case "NEW":
                return "Заявка принята, ожидает обработки";
            case "PROCESSING":
                return "Заявка обрабатывается";
            case "CLASSIFIED":
                return "Заявка классифицирована";
            case "SENT":
                return "Заявка отправлена в отдел";
            case "ERROR":
                return "При обработке заявки произошла ошибка";
            case "COMPLETED":
                return "Заявка успешно обработана";
            default:
                return "Статус: " + status;
        }
    }
}