package com.umsoft.backend.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование RequestDto")
public class RequestDtoTest {

    @Test
    @DisplayName("Установка и получение полей должны работать корректно")
    void testGettersAndSetters() {
        RequestDto dto = new RequestDto();
        dto.setFio("Тестовый Пользователь");
        dto.setEmail("test@example.com");
        dto.setPhone("+79001234567");
        dto.setTask("Тестовое описание задачи");

        assertEquals("Тестовый Пользователь", dto.getFio());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("+79001234567", dto.getPhone());
        assertEquals("Тестовое описание задачи", dto.getTask());
    }

    @Test
    @DisplayName("ResponseDto должен корректно создаваться")
    void testResponseDtoCreation() {
        ResponseDto response = new ResponseDto(true, "Успешно", 123L, "SENT");

        assertTrue(response.isSuccess());
        assertEquals("Успешно", response.getMessage());
        assertEquals(123L, response.getRequestId());
        assertEquals("SENT", response.getStatus());
    }

    @Test
    @DisplayName("ResponseDto с ошибкой должен корректно создаваться")
    void testErrorResponseDtoCreation() {
        ResponseDto errorResponse = new ResponseDto(false, "Ошибка обработки", null, "ERROR");

        assertFalse(errorResponse.isSuccess());
        assertEquals("Ошибка обработки", errorResponse.getMessage());
        assertNull(errorResponse.getRequestId());
        assertEquals("ERROR", errorResponse.getStatus());
    }
}