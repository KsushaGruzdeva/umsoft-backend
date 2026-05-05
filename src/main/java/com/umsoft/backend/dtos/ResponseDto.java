package com.umsoft.backend.dtos;

public class ResponseDto {
    private boolean success;
    private String message;
    private Long requestId;
    private String status;

    public ResponseDto() {}

    public ResponseDto(boolean success, String message, Long requestId, String status) {
        this.success = success;
        this.message = message;
        this.requestId = requestId;
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}