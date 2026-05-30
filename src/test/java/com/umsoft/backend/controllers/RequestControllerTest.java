package com.umsoft.backend.controllers;

import com.umsoft.backend.dtos.RequestDto;
import com.umsoft.backend.dtos.ResponseDto;
import com.umsoft.backend.services.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование REST контроллера")
public class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private RequestDto validRequestDto;

    @BeforeEach
    void setUp() {
        validRequestDto = new RequestDto();
        validRequestDto.setFio("Иванов Иван Иванович");
        validRequestDto.setEmail("ivan@example.com");
        validRequestDto.setPhone("+79001234567");
        validRequestDto.setTask("Разработать мобильное приложение");
    }

    @Test
    @DisplayName("POST /api/requests с валидными данными должен вернуть 201 CREATED")
    void testValidRequest_Returns201() {
        ResponseDto mockResponse = new ResponseDto(true, "Заявка успешно принята", 1L, "NEW");
        when(requestService.processRequest(any(RequestDto.class))).thenReturn(mockResponse);

        ResponseEntity<ResponseDto> response = requestController.createRequest(validRequestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Заявка успешно принята", response.getBody().getMessage());
    }

    @Test
    @DisplayName("При ошибке в сервисе должен вернуть 500 INTERNAL SERVER ERROR")
    void testServiceException_Returns500() {
        when(requestService.processRequest(any(RequestDto.class)))
                .thenThrow(new RuntimeException("Ошибка подключения к БД"));

        ResponseEntity<ResponseDto> response = requestController.createRequest(validRequestDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Ошибка"));
    }
}