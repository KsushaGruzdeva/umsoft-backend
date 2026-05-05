package com.umsoft.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RequestDto {

    @NotBlank(message = "ФИО обязательно")
    private String fio;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Описание задачи обязательно")
    private String task;

    private String phone;

    public RequestDto() {}

    public RequestDto(String fio, String email, String task, String phone) {
        this.fio = fio;
        this.email = email;
        this.task = task;
        this.phone = phone;
    }

    public String getFio() {
        return fio;
    }

    public String getEmail() {
        return email;
    }

    public String getTask() {
        return task;
    }

    public String getPhone() {
        return phone;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}