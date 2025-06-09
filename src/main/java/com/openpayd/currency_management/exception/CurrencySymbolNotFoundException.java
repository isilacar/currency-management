package com.openpayd.currency_management.exception;

public class CurrencySymbolNotFoundException extends RuntimeException{

    public CurrencySymbolNotFoundException(String message){
        super(message);
    }
}