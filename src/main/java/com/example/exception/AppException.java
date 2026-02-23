package com.example.exception;

public class AppException extends RuntimeException {
    private Errorcode errorcode;

    public AppException(Errorcode errorcode) {
        super(errorcode.getMessage());
        this.errorcode = errorcode;
    }

    public Errorcode getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(Errorcode errorcode) {
        this.errorcode = errorcode;
    }
}
