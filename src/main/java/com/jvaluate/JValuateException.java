package com.jvaluate;

public class JValuateException extends Exception {

    private static final long serialVersionUID = 4664456874499611218L;

    private String errorCode = "Unknown_Exception";

    public JValuateException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }


}