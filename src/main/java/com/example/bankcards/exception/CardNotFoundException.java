package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException{
 public CardNotFoundException(Long id){
     super("Card not found by id "+id);
 }
    public CardNotFoundException(String message){
        super(message);
    }
}
