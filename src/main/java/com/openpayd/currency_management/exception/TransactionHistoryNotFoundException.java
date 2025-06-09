package com.openpayd.currency_management.exception;

public class TransactionHistoryNotFoundException extends RuntimeException{
    
    public TransactionHistoryNotFoundException(String message){
        super(message);
    }
}
