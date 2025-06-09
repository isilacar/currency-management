package com.openpayd.currency_management.exception;


public record ErrorResponse(String timestamp,
                            String errorCode,
                            String message,
                            String description) {}

