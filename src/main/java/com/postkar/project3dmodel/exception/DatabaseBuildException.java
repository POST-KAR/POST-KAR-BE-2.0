package com.postkar.project3dmodel.exception;

public class DatabaseBuildException extends RuntimeException {
    public DatabaseBuildException(String message) {
        super(message);
    }
    
    public DatabaseBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
