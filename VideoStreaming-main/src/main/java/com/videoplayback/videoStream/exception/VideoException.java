package com.videoplayback.videoStream.exception;

public class VideoException extends RuntimeException {
    public VideoException(String message) {
        super(message);
    }

    public VideoException(String message, Throwable cause) {
        super(message, cause);
    }
}

