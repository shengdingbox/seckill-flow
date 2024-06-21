package com.seckillflow.core;


public class RetryableException extends RuntimeException {

    public RetryableException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
