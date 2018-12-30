package de.blom.httpwebserver.exception;

public class WrongContentTypeException extends RuntimeException {
    public WrongContentTypeException(String message){
        super(message);
    }
}
